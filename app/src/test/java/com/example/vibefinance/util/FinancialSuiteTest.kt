package com.example.vibefinance.util

import com.example.vibefinance.data.entity.*
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class FinancialSuiteTest {
    private val zone = ZoneId.of("Asia/Hong_Kong")
    private fun calculator(date: String, zone: ZoneId = this.zone) = CreditCardCycleCalculator(Clock.fixed(LocalDate.parse(date).atTime(12, 0).atZone(zone).toInstant(), zone))
    private fun card(billing: Int? = 31, due: Int? = 5) = AccountEntity(id = 1, name = "Card", type = AccountType.CC, balance = 999.0,
        icon = "credit_card", billingDate = billing, paymentDate = due, creditLimit = 500.0)
    @Test fun closingDayStaysInCycleAndShortMonthsClamp() {
        val feb = calculator("2024-02-29").currentCycle(card())
        assertEquals(LocalDate.parse("2024-02-01"), feb.start)
        assertEquals(LocalDate.parse("2024-02-29"), feb.closing)
        val march = calculator("2024-03-01").currentCycle(card())
        assertEquals(LocalDate.parse("2024-03-01"), march.start)
        assertEquals(LocalDate.parse("2024-03-31"), march.closing)
        assertEquals(LocalDate.parse("2025-02-28"), calculator("2025-02-28").currentCycle(card()).closing)
    }
    @Test fun missingOrInvalidClosingUsesCalendarMonth() {
        for (day in listOf(null, 0, 32)) {
            val cycle = calculator("2026-01-10").currentCycle(card(day))
            assertEquals(LocalDate.parse("2026-01-01"), cycle.start)
            assertEquals(LocalDate.parse("2026-01-31"), cycle.closing)
        }
    }
    @Test fun postedCreditsReduceCycleSpendButTransfersAdjustmentsAndFutureEntriesDoNot() {
        val calc = calculator("2026-10-15")
        val time = LocalDate.parse("2026-10-10").atStartOfDay(zone).toInstant().toEpochMilli()
        fun tx(amount: Double) = TransactionEntity(amount = amount, category = "Test", accountId = 1, timestamp = time)
        val records = listOf(tx(100.0), tx(-20.0), tx(50.0).copy(toAccountId = 2),
            tx(99.0).copy(isBalanceAdjustment = true), tx(30.0).copy(timestamp = time + 20 * 86400000L), tx(400.0).copy(accountId = 2))
        val metric = calc.metrics(card(), records)
        assertEquals(80.0, metric.spent, 0.001)
        assertEquals(0.16f, metric.utilization!!, 0.001f)
        assertEquals(420.0, metric.remaining!!, 0.001)
        assertEquals(999.0, card().balance, 0.0)
    }
    @Test fun halfOpenBoundariesExcludePriorAndFollowingCycles() {
        val calc = calculator("2026-10-31")
        val cycle = calc.currentCycle(card())
        fun tx(timestamp: Long) = TransactionEntity(amount = 10.0, category = "Test", accountId = 1, timestamp = timestamp)
        assertEquals(10.0, calc.metrics(card(), listOf(tx(cycle.startMillis - 1), tx(cycle.startMillis), tx(cycle.endExclusiveMillis))).spent, 0.0)
    }
    @Test fun utilizationClampsButCreditsCanIncreaseCycleAllowance() {
        val calc = calculator("2026-10-15")
        val timestamp = calc.currentCycle(card()).startMillis
        fun metrics(amount: Double) = calc.metrics(card(), listOf(TransactionEntity(amount = amount, category = "Other", timestamp = timestamp, accountId = 1)))
        assertEquals(1f, metrics(900.0).utilization!!, 0f)
        assertEquals(0.0, metrics(900.0).remaining!!, 0.0)
        assertEquals(0f, metrics(-20.0).utilization!!, 0f)
        assertEquals(520.0, metrics(-20.0).remaining!!, 0.0)
        assertNull(calc.metrics(card().copy(creditLimit = null), emptyList()).utilization)
    }
    @Test fun dueDateFallbackAndMonthEndAreConsistent() {
        val calc = calculator("2025-02-28")
        assertEquals(LocalDate.parse("2025-02-28"), calc.nextDue(card(due = 31)))
        assertEquals(LocalDate.parse("2025-03-05"), calc.nextDue(card().copy(paymentDeadline = 0)))
        assertEquals(LocalDate.parse("2025-03-10"), calc.nextDue(card().copy(paymentDeadline = 10)))
        assertNull(calc.nextDue(card(due = null)))
    }
    @Test fun dueReminderUsesStrictlyEarlierStatement() {
        val calc = calculator("2026-05-15")
        val cycle = calc.statementCycle(card(billing = 15), LocalDate.parse("2026-05-15"))
        assertEquals(LocalDate.parse("2026-03-16"), cycle.start)
        assertEquals(LocalDate.parse("2026-04-15"), cycle.closing)
        assertEquals(LocalDate.parse("2026-04-01"), calc.statementCycle(card(null), LocalDate.parse("2026-05-05")).start)
    }
    @Test fun dayBoundariesFollowDstInsteadOfFixed24HourArithmetic() {
        val calc = calculator("2026-03-15", ZoneId.of("America/New_York"))
        val cycle = calc.currentCycle(card(null))
        assertEquals(743L, (cycle.endExclusiveMillis - cycle.startMillis) / 3600000L)
    }
    private fun keys(vararg keys: String): KeypadCalculatorEngine.State = keys.fold(KeypadCalculatorEngine.State()) { state, key -> KeypadCalculatorEngine.press(state, key) }
    @Test fun calculatorUsesDecimalPrecisionAndEvaluatesOnApply() {
        val state = keys("0", ".", "1", "+", "0", ".", "2")
        assertEquals("0.3", KeypadCalculatorEngine.result(state.expression)?.toPlainString())
        assertEquals("0.30", KeypadCalculatorEngine.press(state, "=").expression)
        assertEquals("7", KeypadCalculatorEngine.press(KeypadCalculatorEngine.press(state, "="), "7").expression)
    }
    @Test fun calculatorReplacesOperatorsAndPreventsInvalidOperands() {
        assertEquals("1-2", keys("1", "+", "-", "2").expression)
        assertEquals("0", keys("+", "-").expression)
        assertEquals("1.23", keys("1", ".", "2", ".", "3", "4").expression)
        assertNull(KeypadCalculatorEngine.result("1+"))
        assertEquals("1", KeypadCalculatorEngine.preview("1+")?.toPlainString())
    }
    @Test fun negativeResultsRemainVisibleAndClearAndBackspaceAreSafe() {
        val result = keys("1", "-", "2", "=")
        assertEquals("-1.00", result.expression)
        assertEquals("-0.50", keys("1", "-", "2", "=", "+", "0", ".", "5", "=").expression)
        assertEquals("0", KeypadCalculatorEngine.press(result, "clear").expression)
        assertEquals("0", keys("back").expression)
    }
}
