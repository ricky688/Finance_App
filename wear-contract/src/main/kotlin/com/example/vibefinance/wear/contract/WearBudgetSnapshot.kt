package com.example.vibefinance.wear.contract

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.time.ZoneId

enum class BudgetStatus { READY, HIDDEN, NO_BUDGET, ERROR }

/** Display-only contract: all finance calculations and money formatting stay on the phone. */
data class WearBudgetSnapshot(
    val revision: Long,
    val status: BudgetStatus,
    val generatedAt: Long,
    val expiresAt: Long,
    val calculationDate: String,
    val zoneId: String,
    val dailyAmount: String = "",
    val periodAmount: String = "",
    val daysLeft: Int = 0,
    val motion: String = "MINIMAL"
) {
    fun isCurrent(now: Long): Boolean = now >= generatedAt && now < expiresAt

    fun encode(): ByteArray {
        validate()
        return ByteArrayOutputStream().also { bytes ->
            DataOutputStream(bytes).use { out ->
                out.writeInt(VERSION)
                out.writeLong(revision)
                out.writeUTF(status.name)
                out.writeLong(generatedAt)
                out.writeLong(expiresAt)
                out.writeUTF(calculationDate)
                out.writeUTF(zoneId)
                out.writeUTF(dailyAmount)
                out.writeUTF(periodAmount)
                out.writeInt(daysLeft)
                out.writeUTF(motion)
            }
        }.toByteArray().also { require(it.size <= 4096) }
    }

    private fun validate() {
        require(revision > 0 && generatedAt > 0)
        require(expiresAt > generatedAt && expiresAt - generatedAt <= MAX_AGE_MS)
        val zone = ZoneId.of(zoneId)
        val date = java.time.LocalDate.parse(calculationDate)
        require(java.time.Instant.ofEpochMilli(generatedAt).atZone(zone).toLocalDate() == date)
        require(expiresAt <= date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli())
        require(daysLeft >= 0 && motion in setOf("FULL", "REDUCED", "MINIMAL"))
        if (status == BudgetStatus.READY) {
            require(daysLeft > 0 && amountPattern.matches(dailyAmount) && amountPattern.matches(periodAmount))
        } else {
            require(dailyAmount.isEmpty() && periodAmount.isEmpty() && daysLeft == 0)
        }
    }

    companion object {
        const val VERSION = 1
        const val MAX_AGE_MS = 15 * 60 * 1000L
        const val DATA_PATH = "/vibefinance/budget/v1"
        const val REFRESH_PATH = "/vibefinance/budget/refresh/v1"
        const val PHONE_CAPABILITY = "vibefinance_budget_phone_v1"
        const val PAYLOAD_KEY = "snapshot"
        private val amountPattern = Regex("-?HK\\$ (?:0|[1-9][0-9]{0,2}(?:,[0-9]{3})*)(?:\\.[0-9]{2})?")

        fun decode(bytes: ByteArray): WearBudgetSnapshot {
            require(bytes.size in 1..4096)
            return DataInputStream(ByteArrayInputStream(bytes)).use { input ->
                require(input.readInt() == VERSION)
                WearBudgetSnapshot(
                    input.readLong(), BudgetStatus.valueOf(input.readUTF()), input.readLong(),
                    input.readLong(), input.readUTF(), input.readUTF(), input.readUTF(),
                    input.readUTF(), input.readInt(), input.readUTF()
                ).also { require(input.available() == 0); it.validate() }
            }
        }
    }
}
