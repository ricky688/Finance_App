package com.example.vibefinance.wear.contract

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class WearCompanionProtocolTest {
    private val now = Instant.parse("2026-11-20T04:00:00Z").toEpochMilli()

    private fun fullSnapshot() = WearCompanionSnapshot(
        revision = 10L,
        status = BudgetStatus.READY,
        generatedAt = now,
        freshUntil = now + WearCompanionSnapshot.FRESH_AGE_MS,
        expiresAt = now + WearCompanionSnapshot.MAX_STALE_AGE_MS,
        calculationDate = "2026-11-20",
        zoneId = "Asia/Hong_Kong",
        dailyAmount = "HK$ 320",
        compactDailyAmount = "$320",
        periodAmount = "HK$ 2,400.50",
        daysLeft = 8,
        dailyProgress = 0.72f,
        periodProgress = 0.45f,
        isOverspent = false,
        motion = "FULL",
        pendingEnabled = true,
        recentEnabled = true,
        pendingItems = listOf(
            WearPendingItem(
                id = "a".repeat(64),
                merchant = "Sushiro HK",
                amountText = "HK$ 157.00",
                transactionType = "EXPENSE",
                sourceLabel = "Mox Card ••2101",
                detectedAt = now - 5_000L,
                suggestedAccountId = 2L,
                suggestedAccountName = "MOX card",
                canConfirmIncome = false,
                canConfirmTransfer = false,
                candidateAccounts = listOf(
                    WearAccountOption(2L, "MOX card", "2101"),
                    WearAccountOption(3L, "Octopus", "")
                )
            )
        ),
        recentTransactions = listOf(
            WearRecentTransaction(
                id = 99L,
                groupId = "notification:" + "b".repeat(64),
                merchant = "Starbucks",
                amountText = "-HK$ 45.00",
                category = "Food & Drink",
                accountName = "MOX card",
                timestamp = now - 60_000L,
                canQuickUndo = true
            )
        ),
        upcomingBill = WearUpcomingBill("Netflix", "HK$ 78.00", "Nov 25", 5),
        quickEntryAccounts = listOf(
            WearAccountOption(1L, "Wallet (Cash)", ""),
            WearAccountOption(3L, "Octopus", "")
        ),
        quickEntryCategories = listOf("Food & Drink", "Transport", "Groceries", "Other")
    )

    @Test fun companionSnapshotRoundTripPreservesAllFields() {
        val original = fullSnapshot()
        val decoded = WearCompanionSnapshot.decode(original.encode())
        assertEquals(original, decoded)
        assertTrue(decoded.isFresh(now + 60_000L))
        assertFalse(decoded.isFresh(now + WearCompanionSnapshot.FRESH_AGE_MS))
        assertTrue(decoded.isUsable(now + WearCompanionSnapshot.FRESH_AGE_MS))
        assertFalse(decoded.isUsable(now + WearCompanionSnapshot.MAX_STALE_AGE_MS))
    }

    @Test fun hiddenSnapshotRejectsLeakedPendingOrRecentData() {
        assertThrows(IllegalArgumentException::class.java) {
            fullSnapshot().copy(
                status = BudgetStatus.HIDDEN,
                dailyAmount = "",
                compactDailyAmount = "",
                periodAmount = "",
                daysLeft = 0
            ).encode()
        }
    }

    @Test fun disabledFeatureFlagsRejectNonEmptyLists() {
        assertThrows(IllegalArgumentException::class.java) {
            fullSnapshot().copy(pendingEnabled = false).encode()
        }
        assertThrows(IllegalArgumentException::class.java) {
            fullSnapshot().copy(recentEnabled = false).encode()
        }
    }

    @Test fun commandAndResultRoundTripsValidatePayloads() {
        val accept = WearCommand("req-1", now, WearCommandAction.ACCEPT_PENDING, paymentId = "a".repeat(64), accountId = 2L)
        assertEquals(accept, WearCommand.decode(accept.encode()))

        val quickAdd = WearCommand("req-2", now, WearCommandAction.QUICK_ADD_EXPENSE, accountId = 1L, category = "Food & Drink", amount = 38.5, note = "Lunch")
        assertEquals(quickAdd, WearCommand.decode(quickAdd.encode()))

        assertThrows(IllegalArgumentException::class.java) {
            WearCommand("req-bad", now, WearCommandAction.QUICK_ADD_EXPENSE, accountId = 1L, category = "Food", amount = -5.0).encode()
        }

        val result = WearCommandResult("req-2", WearCommandStatus.SUCCESS, "Logged", 11L)
        assertEquals(result, WearCommandResult.decode(result.encode()))
    }
}
