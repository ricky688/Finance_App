package com.example.vibefinance.data.entity

import org.json.JSONObject
import java.util.UUID

/**
 * Supported transaction types parsed from notifications.
 */
enum class ParsedTransactionType {
    EXPENSE,
    INCOME,
    REPAYMENT,
    TRANSFER
}

/**
 * A slot assigned to a token segment during interactive tagging.
 */
enum class SlotType {
    NONE,
    AMOUNT,
    MERCHANT,
    CARD_LAST4,
    TYPE
}

/**
 * Token segment with an assigned semantic slot.
 */
data class SlotToken(
    val index: Int,
    val text: String,
    val slot: SlotType = SlotType.NONE
)

data class TemplateMatchResult(
    val amount: Double,
    val merchant: String,
    val cardLast4: String?,
    val transactionType: ParsedTransactionType
)

/**
 * A user-defined or adaptive notification parsing template.
 */
data class NotificationTemplate(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val sourcePackage: String = "",
    val rawSample: String = "",
    val regexPattern: String,
    val amountGroup: Int = 1,
    val merchantGroup: Int = 2,
    val cardLast4Group: Int? = null,
    val transactionType: ParsedTransactionType = ParsedTransactionType.EXPENSE,
    val defaultAccountId: Long? = null,
    val matchCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun match(text: String): TemplateMatchResult? {
        val regex = runCatching { Regex(regexPattern, RegexOption.IGNORE_CASE) }.getOrNull() ?: return null
        val match = regex.find(text) ?: return null

        val amountStr = match.groups[amountGroup]?.value?.replace(",", "")?.trim() ?: return null
        val amount = amountStr.toDoubleOrNull() ?: return null
        if (amount <= 0.0) return null

        val merchant = match.groups[merchantGroup]?.value?.trim() ?: return null
        val card = cardLast4Group?.let { g -> match.groups[g]?.value?.trim() }

        return TemplateMatchResult(
            amount = amount,
            merchant = merchant,
            cardLast4 = card,
            transactionType = transactionType
        )
    }

    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("name", name)
        obj.put("sourcePackage", sourcePackage)
        obj.put("rawSample", rawSample)
        obj.put("regexPattern", regexPattern)
        obj.put("amountGroup", amountGroup)
        obj.put("merchantGroup", merchantGroup)
        cardLast4Group?.let { obj.put("cardLast4Group", it) }
        obj.put("transactionType", transactionType.name)
        defaultAccountId?.let { obj.put("defaultAccountId", it) }
        obj.put("matchCount", matchCount)
        obj.put("createdAt", createdAt)
        return obj
    }

    companion object {
        fun fromJson(json: JSONObject): NotificationTemplate? {
            val id = json.optString("id").takeIf { it.isNotBlank() } ?: return null
            val pattern = json.optString("regexPattern").takeIf { it.isNotBlank() } ?: return null
            val typeStr = json.optString("transactionType", ParsedTransactionType.EXPENSE.name)
            val type = runCatching { ParsedTransactionType.valueOf(typeStr) }.getOrDefault(ParsedTransactionType.EXPENSE)
            val cardGroup = if (json.has("cardLast4Group") && !json.isNull("cardLast4Group")) json.getInt("cardLast4Group") else null
            val defaultAcc = if (json.has("defaultAccountId") && !json.isNull("defaultAccountId")) json.getLong("defaultAccountId") else null

            return NotificationTemplate(
                id = id,
                name = json.optString("name", "Custom Template"),
                sourcePackage = json.optString("sourcePackage", ""),
                rawSample = json.optString("rawSample", ""),
                regexPattern = pattern,
                amountGroup = json.optInt("amountGroup", 1),
                merchantGroup = json.optInt("merchantGroup", 2),
                cardLast4Group = cardGroup,
                transactionType = type,
                defaultAccountId = defaultAcc,
                matchCount = json.optInt("matchCount", 0),
                createdAt = json.optLong("createdAt", System.currentTimeMillis())
            )
        }

        /**
         * Builds a regex template from marked tokens and original text.
         */
        fun compileFromTokens(
            tokens: List<SlotToken>,
            originalText: String,
            name: String,
            sourcePackage: String,
            transactionType: ParsedTransactionType,
            defaultAccountId: Long? = null
        ): NotificationTemplate {
            var currentGroup = 1
            var amountGrp = 1
            var merchantGrp = 2
            var cardGrp: Int? = null

            val patternBuilder = StringBuilder("(?i)")

            tokens.forEachIndexed { idx, token ->
                if (idx > 0) {
                    patternBuilder.append("\\s*")
                }
                when (token.slot) {
                    SlotType.AMOUNT -> {
                        amountGrp = currentGroup++
                        // Captures currency symbols optionally and decimal amount
                        patternBuilder.append("(?:HKD|HK\\$|\\$|USD|TWD|RMB|￥|€|£)?\\s*([0-9]+(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?)")
                    }
                    SlotType.MERCHANT -> {
                        merchantGrp = currentGroup++
                        if (idx == tokens.lastIndex) {
                            patternBuilder.append("([^，,。\\n]+)")
                        } else {
                            patternBuilder.append("([^，,。\\n]+?)")
                        }
                    }
                    SlotType.CARD_LAST4 -> {
                        cardGrp = currentGroup++
                        patternBuilder.append("(?:card|尾號|末四位|ending in|acct|•*|x*)?\\s*(\\d{4})")
                    }
                    SlotType.TYPE, SlotType.NONE -> {
                        patternBuilder.append(Regex.escape(token.text))
                    }
                }
            }

            return NotificationTemplate(
                name = name,
                sourcePackage = sourcePackage,
                rawSample = originalText,
                regexPattern = patternBuilder.toString(),
                amountGroup = amountGrp,
                merchantGroup = merchantGrp,
                cardLast4Group = cardGrp,
                transactionType = transactionType,
                defaultAccountId = defaultAccountId
            )
        }
    }
}
