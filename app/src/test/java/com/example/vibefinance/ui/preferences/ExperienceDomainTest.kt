package com.example.vibefinance.ui.preferences

import com.example.vibefinance.data.entity.*
import com.example.vibefinance.ui.history.*
import com.example.vibefinance.util.FinancialDataImportEngine
import java.io.File
import java.time.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** History search/filter and privacy flows were explored with ARTEMIS on Waydroid first. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExperienceDomainTest {
    private val zone = ZoneId.of("Asia/Hong_Kong")
    private fun at(date: String) = LocalDate.parse(date).atStartOfDay(zone).toInstant().toEpochMilli()
    private val accounts = listOf(AccountEntity(id = 1, name = "銀行", nickname = "Daily Visa", type = AccountType.CC, balance = 10.0, icon = "cc"),
        AccountEntity(id = 2, name = "Cash", type = AccountType.CASH, balance = 10.0, icon = "wallet"))
    private fun tx(id: Long, amount: Double, date: String = "2026-09-01", account: Long = 1, to: Long? = null,
        category: String = "Food", note: String = "Coffee 咖啡") = TransactionEntity(id, amount, category, at(date), account, to, description = note)

    @Test fun filtersIntersectAndSearchIncludesNicknamesUnicodeAndDestination() {
        val rows = listOf(tx(1, 10.0), tx(2, 20.0, account = 2), tx(3, 10.0, category = "Transport"), tx(4, 10.0, to = 2))
        assertEquals(listOf(1L), HistoryQuery("咖啡 Visa", 1, "Food", HistoryKind.EXPENSE,
            "2026-09-01", "2026-09-01", "5", "15").apply(rows, accounts, zone).map { it.id })
        assertEquals(listOf(4L), HistoryQuery("Cash", 2, kind = HistoryKind.TRANSFER).apply(rows, accounts, zone).map { it.id })
    }
    @Test fun endDateIncludesItsWholeLocalDayAndAmountUsesMagnitude() {
        val endOfDay = tx(1, -25.0).copy(timestamp = at("2026-09-02") - 1)
        val nextDay = tx(2, -25.0, "2026-09-02")
        assertEquals(listOf(endOfDay), HistoryQuery(kind = HistoryKind.INCOME, through = "2026-09-01", minimum = "25", maximum = "25")
            .apply(listOf(endOfDay, nextDay), accounts, zone))
    }
    @Test fun invalidInputNeverSilentlyDropsAConstraint() {
        listOf(HistoryQuery(from = "2026-02-30"), HistoryQuery(minimum = "NaN"), HistoryQuery(maximum = "Infinity"),
            HistoryQuery(minimum = "-1"), HistoryQuery(minimum = "20", maximum = "10"),
            HistoryQuery(from = "2026-10-01", through = "2026-09-01"), HistoryQuery(through = "+999999999-12-31")).forEach {
            assertFalse(it.valid()); assertTrue(it.apply(listOf(tx(1, 10.0)), accounts, zone).isEmpty())
        }
    }
    @Test fun adjustmentIsNotAnExpenseAndFiltersByItsActualDelta() {
        val adjustment = tx(1, 0.0).copy(isBalanceAdjustment = true, balanceAdjustmentDelta = -12.5)
        assertTrue(HistoryQuery(kind = HistoryKind.EXPENSE).apply(listOf(adjustment), accounts).isEmpty())
        assertEquals(listOf(adjustment), HistoryQuery(kind = HistoryKind.ADJUSTMENT, minimum = "12.5", maximum = "12.5").apply(listOf(adjustment), accounts))
    }
    @Test fun suppliedWorkbookRemains868Records14AccountsAnd52831Expenses() {
        val file = listOf(File("../財務管家_27-9-2026.xlsx"), File("財務管家_27-9-2026.xlsx")).first { it.exists() }
        val imported = file.inputStream().use { FinancialDataImportEngine.parseStream(it, file.name) }
        assertNull(imported.error); assertEquals(868, imported.rawTransactions.size); assertEquals(14, imported.detectedAccounts.size)
        val names = imported.detectedAccounts.mapIndexed { index, raw -> AccountEntity(id = index + 1L, name = raw.name, type = raw.detectedType, balance = 0.0, icon = "wallet") }
        val ids = names.associate { it.name to it.id }
        val transactions = imported.rawTransactions.mapIndexed { index, raw -> TransactionEntity(index + 1L, raw.amount, raw.category,
            raw.timestamp, ids.getValue(raw.sourceAccountName), if (raw.isTransfer) ids[raw.destinationAccountName] else null, description = raw.description) }
        assertEquals(868, HistoryQuery().apply(transactions, names, zone).size)
        assertEquals(52831.49, HistoryQuery(kind = HistoryKind.EXPENSE).apply(transactions, names, zone).sumOf { it.amount }, 0.001)
        val first = transactions.first { it.toAccountId == null && it.amount > 0 }
        val exact = HistoryQuery(account = first.accountId, category = first.category, minimum = "${first.amount}", maximum = "${first.amount}")
        assertTrue(exact.apply(transactions, names, zone).contains(first))
    }
    @Test fun privacyRedactsCurrenciesAndDecimalsWithoutHidingDatesOrCounts() {
        listOf("HK$12,345.67", "HK$ 0", "-US$50.00", "€12.00", "USD 9.90", "12.50").forEach { assertFalse(redactAmounts(it).any(Char::isDigit)) }
        assertEquals("2026-10-09", redactAmounts("2026-10-09")); assertEquals("14 accounts", redactAmounts("14 accounts"))
        assertEquals("25%", redactAmounts("25%")); assertEquals("Cloud · ••••", redactAmounts("Cloud · HK$10.00"))
    }
    @Test fun motionReadsAppChoiceAndRespectsSystemDisabledAnimations() {
        val context = RuntimeEnvironment.getApplication()
        val prefs = context.getSharedPreferences("vibe_finance_prefs", 0)
        android.provider.Settings.Global.putFloat(context.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        val scale = AppMotionScale()
        prefs.edit().putString("motion_level", "REDUCED").commit(); scale.update(context); assertEquals(.35f, scale.scaleFactor)
        prefs.edit().putString("motion_level", "MINIMAL").commit(); scale.update(context); assertEquals(0f, scale.scaleFactor)
        prefs.edit().putString("motion_level", "FULL").commit()
        android.provider.Settings.Global.putFloat(context.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 0f)
        scale.update(context); assertEquals(0f, scale.scaleFactor)
    }
}
