package com.example.vibefinance.util

import android.content.Context
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.data.entity.RolloverMode
import org.json.JSONArray
import org.json.JSONObject

/** Explicit stores and value types avoid restoring arbitrary files or losing Long/StringSet types. */
internal object BackupStateCodec {
    val preferenceStores = listOf("vibe_finance_prefs", "vibefinance_prefs", "assets_display",
        "cashback_rules_prefs", "vibe_merchant_rules", "pending_payment_choices")

    fun preferences(context: Context): JSONObject = JSONObject().apply {
        preferenceStores.forEach { name ->
            val values = JSONObject()
            context.getSharedPreferences(name, Context.MODE_PRIVATE).all.forEach { (key, value) ->
                val type = when (value) { is String -> "string"; is Boolean -> "boolean"; is Int -> "int"
                    is Long -> "long"; is Float -> "float"; is Set<*> -> "stringSet"
                    else -> throw BackupException(BackupError.INVALID_STATE) }
                values.put(key, JSONObject().put("type", type).put("value", if (value is Set<*>) JSONArray(value.toList().sortedBy { it.toString() }) else value))
            }
            put(name, values)
        }
    }

    fun validatePreferences(root: JSONObject) {
        require(root.keys().asSequence().toSet() == preferenceStores.toSet())
        preferenceStores.forEach { name ->
            val store = root.getJSONObject(name)
            store.keys().forEach { key ->
                require(key.isNotBlank())
                val item = store.getJSONObject(key)
                val value = item.get("value")
                when (item.getString("type")) {
                    "string" -> require(value is String)
                    "boolean" -> require(value is Boolean)
                    "int" -> require(value is Number && value.toDouble() == value.toInt().toDouble())
                    "long" -> require(value is Number && value.toString().toLongOrNull() != null)
                    "float" -> require(value is Number && value.toFloat().isFinite())
                    "stringSet" -> { require(value is JSONArray); for (i in 0 until value.length()) require(value.get(i) is String) }
                    else -> throw BackupException(BackupError.INVALID_FILE)
                }
            }
        }
        // Known consumer types must match, not merely the archive's own declared type.
        val known = mapOf(
            "vibe_finance_prefs" to mapOf("app_language" to "string", "theme_mode" to "string", "icon_shape" to "string",
                "appearance_palette" to "string", "appearance_contrast" to "int", "appearance_palette_style" to "string",
                "appearance_palette_seeds" to "string", "dynamic_color_enabled" to "boolean", "pure_black_dark_mode" to "boolean",
                "launch_animation_enabled" to "boolean", "hide_amounts" to "boolean", "app_lock_enabled" to "boolean",
                "motion_level" to "string", "blur_intensity" to "float"),
            "vibefinance_prefs" to mapOf("last_daily_recalc_date" to "string", "auto_apply_recalc_choice" to "boolean"),
            "assets_display" to mapOf("compact_mode" to "boolean"),
            "cashback_rules_prefs" to mapOf("selected_intercept_apps" to "stringSet", "notification_logging_enabled" to "boolean"),
            "pending_payment_choices" to mapOf("pending" to "string", "remembered_accounts" to "string", "seen_alerts" to "string")
        )
        known.forEach { (store, keys) -> keys.forEach { (key, type) ->
            root.getJSONObject(store).optJSONObject(key)?.let { require(it.getString("type") == type) }
        } }
        val appearance = root.getJSONObject("vibe_finance_prefs")
        appearance.optJSONObject("appearance_palette_style")?.let {
            com.example.vibefinance.theme.PaletteStyle.valueOf(it.getString("value"))
        }
        appearance.optJSONObject("appearance_palette_seeds")?.let {
            com.example.vibefinance.theme.decodePaletteSeeds(it.getString("value"))
        }
        appearance.optJSONObject("motion_level")?.let { require(it.getString("value") in setOf("FULL", "REDUCED", "MINIMAL")) }
        appearance.optJSONObject("blur_intensity")?.let {
            val value = it.getDouble("value"); require(value.isFinite() && value in 0.0..1.0)
        }
        root.getJSONObject("vibe_merchant_rules").keys().forEach { key ->
            require(root.getJSONObject("vibe_merchant_rules").getJSONObject(key).getString("type") == "string")
        }
    }

    fun applyPreferences(context: Context, root: JSONObject) {
        validatePreferences(root)
        preferenceStores.forEach { name ->
            val editor = context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear()
            val store = root.getJSONObject(name)
            store.keys().forEach { key ->
                val item = store.getJSONObject(key)
                when (item.getString("type")) {
                    "string" -> editor.putString(key, item.getString("value"))
                    "boolean" -> editor.putBoolean(key, item.getBoolean("value"))
                    "int" -> editor.putInt(key, item.getInt("value"))
                    "long" -> editor.putLong(key, item.getLong("value"))
                    "float" -> editor.putFloat(key, item.getDouble("value").toFloat())
                    "stringSet" -> { val array = item.getJSONArray("value"); editor.putStringSet(key, (0 until array.length()).map { array.getString(it) }.toSet()) }
                }
            }
            if (!editor.commit()) throw BackupException(BackupError.STORAGE)
        }
    }

    fun counts(data: JSONObject): JSONObject = JSONObject().apply {
        listOf("accounts", "transactions", "budgets", "subscriptions", "discountShops").forEach { put(it, data.getJSONArray(it).length()) }
    }

    fun validateData(data: JSONObject, preferences: JSONObject, files: Set<String>) {
        fun records(key: String): List<JSONObject> {
            val array = data.getJSONArray(key)
            return (0 until array.length()).map { array.getJSONObject(it) }
        }
        fun positiveId(o: JSONObject, key: String = "id"): Long {
            val raw = o.get(key); require(raw is Number && raw.toString().toLongOrNull() != null)
            return o.getLong(key).also { require(it in 1 until Long.MAX_VALUE) }
        }
        fun finite(o: JSONObject, key: String) { require(o.get(key) is Number && o.getDouble(key).isFinite()) }
        fun unique(list: List<JSONObject>): Set<Long> {
            val ids = list.map { positiveId(it) }; require(ids.distinct().size == ids.size); return ids.toSet()
        }
        fun optional(o: JSONObject, key: String, check: () -> Unit) { if (o.has(key) && !o.isNull(key)) check() }
        val accounts = records("accounts")
        val accountIds = unique(accounts)
        val referencedMedia = mutableSetOf<String>()
        accounts.forEach { a ->
            require(a.get("name") is String && a.getString("name").isNotBlank())
            require(a.get("icon") is String)
            AccountType.valueOf(a.getString("type")); finite(a, "balance")
            listOf("creditLimit", "minSpendThreshold", "cardBgOffsetX", "cardBgOffsetY", "cardBgScale").forEach { key -> optional(a, key) { finite(a, key) } }
            listOf("nickname", "cardTheme", "accentColorKey", "cardLast4", "cardProtocol", "cardIssuer", "cardPattern", "customImageAspectRatio", "linkedAppPackage", "notificationAliases").forEach { key -> optional(a, key) { require(a.get(key) is String) } }
            listOf("billingDate", "paymentDate", "paymentDeadline").forEach { key -> optional(a, key) { require(a.get(key) is Number && a.getInt(key) in 1..31) } }
            optional(a, "cardImageUri") {
                val image = a.getString("cardImageUri")
                require(image.matches(Regex("media/[0-9]+\\.img")) && image in files)
                referencedMedia += image
            }
        }
        require(files.filter { it.startsWith("media/") }.toSet() == referencedMedia)
        val transactions = records("transactions"); unique(transactions)
        transactions.forEach { tx ->
            require(positiveId(tx, "accountId") in accountIds)
            optional(tx, "toAccountId") { require(positiveId(tx, "toAccountId") in accountIds) }
            finite(tx, "amount"); require(tx.get("timestamp") is Number && tx.getLong("timestamp") >= 0)
            require(tx.get("category") is String && tx.get("description") is String)
            listOf("sourceWasCreditCard", "destinationWasCreditCard", "isExcludedFromDailyBudget", "isBalanceAdjustment").forEach { key -> optional(tx, key) { require(tx.get(key) is Boolean) } }
            optional(tx, "balanceAdjustmentDelta") { finite(tx, "balanceAdjustmentDelta") }
            listOf("installmentNumber", "totalInstallments").forEach { key -> optional(tx, key) { require(tx.get(key) is Number && tx.getInt(key) > 0) } }
            optional(tx, "customIcon") {
                require(tx.get("customIcon") is String && com.example.vibefinance.data.entity.ExpenseIcon.normalize(tx.getString("customIcon")) == tx.getString("customIcon"))
            }
            optional(tx, "groupId") { require(tx.get("groupId") is String) }
        }
        val budgets = records("budgets")
        require(budgets.map { it.getString("id") }.distinct().size == budgets.size)
        budgets.forEach { b -> finite(b, "totalBudgetAmount"); require(b.getString("id").isNotBlank()); RolloverMode.valueOf(b.getString("rolloverMode"))
            require(b.get("startDate") is Number && b.get("endDate") is Number && b.getLong("endDate") >= b.getLong("startDate")) }
        val subscriptions = records("subscriptions"); unique(subscriptions)
        subscriptions.forEach { s ->
            require(positiveId(s, "accountId") in accountIds); finite(s, "amount")
            listOf("name", "category", "frequency").forEach { require(s.get(it) is String) }
            require(s.get("nextPaymentDate") is Number)
        }
        val shops = records("discountShops"); unique(shops)
        shops.forEach { s ->
            listOf("name", "aspect", "address", "iconCategory").forEach { require(s.get(it) is String) }
            finite(s, "latitude"); finite(s, "longitude"); require(s.get("isUserCreated") is Boolean)
            val offers = s.getJSONArray("offers")
            for (i in 0 until offers.length()) { val o = offers.getJSONObject(i); require(positiveId(o, "accountId") in accountIds); finite(o, "discountRate"); require(o.get("promoDescription") is String) }
        }
        val limits = data.getJSONObject("categoryLimits")
        limits.keys().forEach { require(limits.get(it) is Number && limits.getDouble(it).isFinite() && limits.getDouble(it) >= 0) }
        val rules = data.getJSONObject("categoryMergeRules")
        listOf("expense", "income").forEach { kind -> val map = rules.getJSONObject(kind); map.keys().forEach { require(map.get(it) is String) } }
        val aspects = data.getJSONArray("savedAspects"); for (i in 0 until aspects.length()) require(aspects.get(i) is String)
        listOf("nextAccountId", "nextTransactionId", "nextSubscriptionId", "nextDiscountShopId").forEach { positiveId(data, it) }
        require(data.get("creditCardSourceTransfersReconciled") == true)
        validatePreferences(preferences)
        val cashback = preferences.getJSONObject("cashback_rules_prefs")
        cashback.keys().forEach { key ->
            if (key.matches(Regex("[0-9]+_.+"))) {
                require(key.substringBefore('_').toLong() in accountIds)
                val item = cashback.getJSONObject(key)
                require(item.getString("type") == "string")
                require(item.getString("value").toDouble().isFinite())
            }
        }
        val inbox = preferences.getJSONObject("pending_payment_choices")
        fun string(key: String, default: String) = inbox.optJSONObject(key)?.getString("value") ?: default
        val remembered = JSONObject(string("remembered_accounts", "{}"))
        remembered.keys().forEach { require(remembered.getLong(it) in accountIds) }
        val pending = JSONArray(string("pending", "[]")); val pendingIds = mutableSetOf<String>()
        for (i in 0 until pending.length()) {
            val p = pending.getJSONObject(i)
            require(p.getString("id").isNotBlank() && pendingIds.add(p.getString("id")))
            finite(p, "amount"); require(p.getDouble("amount") > 0)
            listOf("fingerprint", "sourcePackage", "assetHint", "merchant").forEach { require(p.get(it) is String) }
            require(p.get("detectedAt") is Number)
            optional(p, "cardLast4") { require(p.get("cardLast4") is String) }
            optional(p, "balanceRemaining") { finite(p, "balanceRemaining") }
            optional(p, "isTopUp") { require(p.get("isTopUp") is Boolean) }
        }
        val seen = JSONObject(string("seen_alerts", "{}")); seen.keys().forEach { require(seen.get(it) is Number) }
    }
}
