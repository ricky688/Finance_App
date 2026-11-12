package com.example.vibefinance.wear

import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.data.entity.BudgetEntity
import com.example.vibefinance.data.entity.RolloverMode
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.data.repository.BudgetRepository
import com.example.vibefinance.wear.contract.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.time.*
import java.util.Locale

class WearBudgetPresentationTest {
    @Test fun bothRolloverModesUseTheProductionBudgetResultWithoutWritingData() = runTest {
        val budgets = InMemoryDatabase.budgets.value
        val transactions = InMemoryDatabase.transactions.value
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val start = today.minusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val now = System.currentTimeMillis()
        try {
            for (mode in RolloverMode.entries) {
                InMemoryDatabase.budgets.value = listOf(BudgetEntity("ACTIVE_PERIOD", 1000.0, start,
                    today.plusDays(3).atStartOfDay(zone).toInstant().toEpochMilli(), rolloverMode = mode))
                InMemoryDatabase.transactions.value = listOf(
                    TransactionEntity(id = 1, amount = 1500.0, category = "Food", accountId = 1, timestamp = now),
                    TransactionEntity(id = 2, amount = -100.0, category = "Food", accountId = 1, timestamp = now, isUnlinkedReimbursement = true))
                val info = BudgetRepository().getDailyAllowanceFlow().first()
                val snapshot = snapshotForWear(info, 1, now, zone, true, "REDUCED")
                assertEquals(BudgetStatus.READY, snapshot.status)
                assertEquals(String.format(Locale.US, "-HK$ %,.0f", kotlin.math.abs(info.dailyRemaining)), snapshot.dailyAmount)
                assertEquals(String.format(Locale.US, "HK$ %,.2f", info.monthlyRemaining), snapshot.periodAmount)
                assertEquals(info.daysLeft, snapshot.daysLeft)
                assertEquals("REDUCED", snapshot.motion)
                assertEquals(snapshot, WearBudgetSnapshot.decode(snapshot.encode()))
                val hidden = snapshotForWear(info, 2, now, zone, false, "MINIMAL")
                assertEquals(BudgetStatus.HIDDEN, hidden.status)
                assertTrue(hidden.dailyAmount.isEmpty() && hidden.periodAmount.isEmpty())
            }
        } finally {
            InMemoryDatabase.budgets.value = budgets
            InMemoryDatabase.transactions.value = transactions
        }
    }

    @Test fun noBudgetAndMidnightLeaseUseExplicitStates() {
        val now = Instant.parse("2026-11-08T15:59:50Z").toEpochMilli()
        val snapshot = snapshotForWear(null, 1, now, ZoneId.of("Asia/Hong_Kong"), true, "unknown")
        assertEquals(BudgetStatus.NO_BUDGET, snapshot.status)
        assertEquals(10_000, snapshot.expiresAt - now)
        assertEquals("MINIMAL", snapshot.motion)
        assertTrue(snapshot.dailyAmount.isEmpty())
    }
}
