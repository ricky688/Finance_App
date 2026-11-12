package com.example.vibefinance.wear.contract

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class WearBudgetSnapshotTest {
    private val now = Instant.parse("2026-11-08T12:00:00Z").toEpochMilli()
    private fun ready() = WearBudgetSnapshot(1, BudgetStatus.READY, now, now + 900_000,
        "2026-11-08", "Asia/Hong_Kong", "-HK$ 1,234,567", "HK$ 2,439.50", 3)

    @Test fun roundTripRetainsFullAmounts() {
        assertEquals(ready(), WearBudgetSnapshot.decode(ready().encode()))
    }
    @Test fun expirationIsExclusiveAndClockRollbackHides() {
        assertTrue(ready().isCurrent(now))
        assertFalse(ready().isCurrent(now + 900_000))
        assertFalse(ready().isCurrent(now - 1))
    }
    @Test fun hiddenSnapshotCannotContainMoney() {
        assertThrows(IllegalArgumentException::class.java) { ready().copy(status = BudgetStatus.HIDDEN).encode() }
    }
    @Test fun rejectsLongLeaseAndCrossingMidnight() {
        assertThrows(IllegalArgumentException::class.java) { ready().copy(expiresAt = now + 900_001).encode() }
        val midnight = Instant.parse("2026-11-08T16:00:00Z").toEpochMilli()
        assertThrows(IllegalArgumentException::class.java) {
            ready().copy(generatedAt = midnight - 100, expiresAt = midnight + 1).encode()
        }
    }
    @Test fun rejectsUnknownVersionTruncationAndTrailingData() {
        val encoded = ready().encode()
        assertThrows(IllegalArgumentException::class.java) { WearBudgetSnapshot.decode(encoded.clone().also { it[3] = 2 }) }
        assertThrows(Exception::class.java) { WearBudgetSnapshot.decode(encoded.copyOf(12)) }
        assertThrows(IllegalArgumentException::class.java) { WearBudgetSnapshot.decode(encoded + 0) }
    }
    @Test fun rejectsInvalidAmountAndDayMetadata() {
        assertThrows(IllegalArgumentException::class.java) { ready().copy(dailyAmount = "HK$ 1...").encode() }
        assertThrows(IllegalArgumentException::class.java) { ready().copy(dailyAmount = "HK$ 1,,2").encode() }
        assertThrows(IllegalArgumentException::class.java) { ready().copy(daysLeft = 0).encode() }
        assertThrows(IllegalArgumentException::class.java) { ready().copy(calculationDate = "2026-11-07").encode() }
    }
}
