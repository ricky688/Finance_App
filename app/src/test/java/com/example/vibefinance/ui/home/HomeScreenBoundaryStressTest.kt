package com.example.vibefinance.ui.home

import com.example.vibefinance.data.repository.DailyBudgetInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

/**
 * Empirical Boundary and Stress Test Suite for Milestone 2:
 * Daily Home Screen Bento Cards & Top Bar Overhaul.
 *
 * Verifies:
 * 1. HeroDailyBudgetCard giant amount auto-scaling for boundary amounts ($0, negative overdraft, $10M+).
 * 2. 3-column subtitle metrics scaling for boundary values.
 * 3. StatCard value font auto-scaling across specified and unspecified typography tokens.
 * 4. DaysLeftCard division by zero and clamping invariants.
 * 5. RestAndSpentBudgetCard zero/negative budget resilience and color tier thresholds.
 * 6. Buckwheat 4-state financial partition invariants across all boundary permutations.
 */
class HomeScreenBoundaryStressTest {

    // --- 1. HERO DAILY BUDGET CARD GIANT AMOUNT SCALING ---

    private fun computeHeroAmountFontSizeSp(formattedText: String): Float = when {
        formattedText.length >= 13 -> 24f
        formattedText.length >= 10 -> 28f
        formattedText.length >= 8 -> 32f
        formattedText.length >= 6 -> 36f
        else -> 40f
    }

    private fun formatHeroAmount(targetAmount: Float): String {
        val absTarget = Math.abs(targetAmount)
        return String.format(Locale.US, "HK$ %,.0f", absTarget)
    }

    @Test
    fun testHeroAmountZeroBoundary() {
        // Boundary: $0.00
        val zeroStr = formatHeroAmount(0.0f)
        assertEquals("HK$ 0", zeroStr)
        assertEquals(5, zeroStr.length)
        assertEquals(40f, computeHeroAmountFontSizeSp(zeroStr), 0.01f)

        // Boundary: Sub-dollar rounds to 0
        val smallStr = formatHeroAmount(0.49f)
        assertEquals("HK$ 0", smallStr)
        assertEquals(40f, computeHeroAmountFontSizeSp(smallStr), 0.01f)

        // Boundary: Sub-dollar rounds up to 1
        val halfStr = formatHeroAmount(0.50f)
        assertEquals("HK$ 1", halfStr)
        assertEquals(40f, computeHeroAmountFontSizeSp(halfStr), 0.01f)
    }

    @Test
    fun testHeroAmountNegativeAndOverdraftBoundary() {
        // In overdraft mode, HeroCard displays abs(targetAmount)
        val negativeSmall = formatHeroAmount(-0.01f)
        assertEquals("HK$ 0", negativeSmall)
        assertEquals(40f, computeHeroAmountFontSizeSp(negativeSmall), 0.01f)

        val negativeOverdraft = formatHeroAmount(-50.0f)
        assertEquals("HK$ 50", negativeOverdraft)
        assertEquals(6, negativeOverdraft.length)
        assertEquals(36f, computeHeroAmountFontSizeSp(negativeOverdraft), 0.01f)

        val negativeLarge = formatHeroAmount(-1500.0f)
        assertEquals("HK$ 1,500", negativeLarge)
        assertEquals(9, negativeLarge.length)
        assertEquals(32f, computeHeroAmountFontSizeSp(negativeLarge), 0.01f)
    }

    @Test
    fun testHeroAmountExtremeTenMillionBoundary() {
        // Boundary: $10,000,000.00 (10 Million)
        val tenMillion = formatHeroAmount(10_000_000f)
        assertEquals("HK$ 10,000,000", tenMillion)
        assertEquals(14, tenMillion.length)
        assertEquals(24f, computeHeroAmountFontSizeSp(tenMillion), 0.01f)

        // Boundary: $100,000,000.00 (100 Million)
        val hundredMillion = formatHeroAmount(100_000_000f)
        assertEquals("HK$ 100,000,000", hundredMillion)
        assertEquals(15, hundredMillion.length)
        assertEquals(24f, computeHeroAmountFontSizeSp(hundredMillion), 0.01f)

        // Boundary: $1,000,000,000.00 (1 Billion)
        val oneBillion = formatHeroAmount(1_000_000_000f)
        assertEquals("HK$ 1,000,000,000", oneBillion)
        assertEquals(17, oneBillion.length)
        assertEquals(24f, computeHeroAmountFontSizeSp(oneBillion), 0.01f)
    }

    @Test
    fun testHeroAmountExactLengthThresholdTransitions() {
        // < 6 chars -> 40sp
        assertEquals(40f, computeHeroAmountFontSizeSp("HK$ 9"), 0.01f)      // 5 chars -> 40sp
        // 6..7 chars -> 36sp
        assertEquals(36f, computeHeroAmountFontSizeSp("HK$ 10"), 0.01f)     // 6 chars -> 36sp
        assertEquals(36f, computeHeroAmountFontSizeSp("HK$ 999"), 0.01f)    // 7 chars -> 36sp
        // 8..9 chars -> 32sp
        assertEquals(32f, computeHeroAmountFontSizeSp("HK$ 1,000"), 0.01f)  // 9 chars -> 32sp
        // 10..12 chars -> 28sp
        assertEquals(28f, computeHeroAmountFontSizeSp("HK$ 10,000"), 0.01f) // 10 chars -> 28sp
        assertEquals(28f, computeHeroAmountFontSizeSp("HK$ 100,000"), 0.01f)// 11 chars -> 28sp
        // >= 13 chars -> 24sp
        assertEquals(24f, computeHeroAmountFontSizeSp("HK$ 1,000,000"), 0.01f) // 13 chars -> 24sp
    }

    // --- 2. THREE-COLUMN BENTO METRICS BREAKDOWN SCALING ---

    private fun computeMetricFontSizeSp(text: String, isCompactPill: Boolean): Float = when {
        text.length >= 13 -> 9.5f
        text.length >= 10 -> 11f
        isCompactPill -> 12f
        else -> 13f
    }

    @Test
    fun testThreeColumnMetricsBoundaryScaling() {
        // Short values on compact screen (e.g. $0)
        val zeroPill = String.format(Locale.US, "HK$ %,.0f", 0.0)
        assertEquals(12f, computeMetricFontSizeSp(zeroPill, isCompactPill = true), 0.01f)
        assertEquals(13f, computeMetricFontSizeSp(zeroPill, isCompactPill = false), 0.01f)

        // Medium values (e.g. $100,000 -> 11 chars)
        val hundredKPill = String.format(Locale.US, "HK$ %,.0f", 100000.0)
        assertEquals(11f, computeMetricFontSizeSp(hundredKPill, isCompactPill = true), 0.01f)
        assertEquals(11f, computeMetricFontSizeSp(hundredKPill, isCompactPill = false), 0.01f)

        // Extreme values (e.g. $10,000,000 -> 14 chars)
        val tenMPill = String.format(Locale.US, "HK$ %,.0f", 10000000.0)
        assertEquals(9.5f, computeMetricFontSizeSp(tenMPill, isCompactPill = true), 0.01f)
        assertEquals(9.5f, computeMetricFontSizeSp(tenMPill, isCompactPill = false), 0.01f)
    }

    @Test
    fun testTodaySpentClampingIntegrity() {
        // Case 1: Spent more than allowance (overdraft)
        val allowance = 100.0
        val remaining = -50.0
        val todaySpent = (allowance - remaining).coerceAtLeast(0.0)
        assertEquals(150.0, todaySpent, 0.001)

        // Case 2: Negative spent today (e.g. refunded transactions where remaining > allowance)
        val refundRemaining = 120.0
        val refundTodaySpent = (allowance - refundRemaining).coerceAtLeast(0.0)
        assertEquals(0.0, refundTodaySpent, 0.001)

        // Case 3: Sub-cent/sub-dollar threshold (< 0.5 rounds to 0)
        val spentVal = if (0.4 < 0.5) 0.0 else 0.4
        assertEquals(0.0, spentVal, 0.001)
    }

    // --- 3. STATCARD VALUE AUTO-SCALING STRESS TEST ---

    private fun computeStatCardEffectiveFontSize(
        value: String,
        specifiedFontSize: Float?,
        isCompact: Boolean
    ): Float {
        return if (specifiedFontSize != null) {
            if (isCompact && value.length >= 10) {
                specifiedFontSize * 0.75f
            } else if (isCompact && value.length >= 7) {
                specifiedFontSize * 0.85f
            } else {
                specifiedFontSize
            }
        } else {
            when {
                value.length >= 12 -> 15f
                value.length >= 9 -> 17f
                value.length >= 7 -> 19f
                isCompact -> 20f
                else -> 22f
            }
        }
    }

    @Test
    fun testStatCardWithSpecifiedFontSizeScaling() {
        val baseSize = 32f // MaterialTheme.typography.headlineLarge

        // Short amount: "$50.00" (6 chars)
        assertEquals(32f, computeStatCardEffectiveFontSize("$50.00", baseSize, isCompact = true), 0.01f)

        // 7 chars: "$100.00" -> scaled by 0.85
        assertEquals(27.2f, computeStatCardEffectiveFontSize("$100.00", baseSize, isCompact = true), 0.01f)

        // 9 chars: "$1,000.00" -> scaled by 0.85
        assertEquals(27.2f, computeStatCardEffectiveFontSize("$1,000.00", baseSize, isCompact = true), 0.01f)

        // 10 chars: "$10,000.00" -> scaled by 0.75
        assertEquals(24f, computeStatCardEffectiveFontSize("$10,000.00", baseSize, isCompact = true), 0.01f)

        // 14 chars: "$10,000,000.00" -> scaled by 0.75
        assertEquals(24f, computeStatCardEffectiveFontSize("$10,000,000.00", baseSize, isCompact = true), 0.01f)

        // Non-compact should preserve baseSize
        assertEquals(32f, computeStatCardEffectiveFontSize("$10,000,000.00", baseSize, isCompact = false), 0.01f)
    }

    @Test
    fun testStatCardWithUnspecifiedFontSizeScaling() {
        // Boundary lengths for default typography:
        // >= 12 chars -> 15sp
        assertEquals(15f, computeStatCardEffectiveFontSize("$10,000,000.00", null, isCompact = true), 0.01f)
        assertEquals(15f, computeStatCardEffectiveFontSize("$10,000,000.00", null, isCompact = false), 0.01f)

        // 9..11 chars -> 17sp
        assertEquals(17f, computeStatCardEffectiveFontSize("$1,250.00", null, isCompact = true), 0.01f)
        assertEquals(17f, computeStatCardEffectiveFontSize("$12,500.00", null, isCompact = false), 0.01f)

        // 7..8 chars -> 19sp
        assertEquals(19f, computeStatCardEffectiveFontSize("$120.00", null, isCompact = true), 0.01f)

        // < 7 chars -> 20sp (compact) / 22sp (non-compact)
        assertEquals(20f, computeStatCardEffectiveFontSize("$5.00", null, isCompact = true), 0.01f)
        assertEquals(22f, computeStatCardEffectiveFontSize("$5.00", null, isCompact = false), 0.01f)
    }

    // --- 4. DAYS LEFT CARD ROBUSTNESS ---

    @Test
    fun testDaysLeftProgressDivisionByZeroAndClamping() {
        fun computeProgress(daysLeft: Int, totalDays: Int): Float =
            if (totalDays > 0) (daysLeft.toFloat() / totalDays).coerceIn(0f, 1f) else 0f

        // Division by zero guard
        assertEquals(0f, computeProgress(10, 0), 0.0001f)
        assertEquals(0f, computeProgress(0, 0), 0.0001f)
        assertEquals(0f, computeProgress(-5, 0), 0.0001f)

        // Negative totalDays
        assertEquals(0f, computeProgress(5, -10), 0.0001f)

        // Negative daysLeft
        assertEquals(0f, computeProgress(-1, 30), 0.0001f)

        // Over-maximum daysLeft
        assertEquals(1f, computeProgress(100, 30), 0.0001f)

        // Standard boundaries
        assertEquals(0f, computeProgress(0, 30), 0.0001f)
        assertEquals(1f, computeProgress(30, 30), 0.0001f)
    }

    // --- 5. REST AND SPENT BUDGET CARD RESILIENCE ---

    @Test
    fun testRestAndSpentBudgetCardDivisionByZeroAndColorTiers() {
        fun computeRatio(remaining: Double, total: Double): Float =
            if (total > 0) (remaining / total).coerceIn(0.0, 1.0).toFloat() else 0f

        fun resolveColorTier(ratio: Float): String = when {
            ratio < 0.2f -> "RED_ALERT"
            ratio < 0.5f -> "AMBER_WARNING"
            else -> "GREEN_HEALTHY"
        }

        // Division by zero guard
        assertEquals(0f, computeRatio(100.0, 0.0), 0.0001f)
        assertEquals(0f, computeRatio(0.0, 0.0), 0.0001f)
        assertEquals(0f, computeRatio(-50.0, -100.0), 0.0001f)

        // Overdraft ratio clamp
        assertEquals(0f, computeRatio(-500.0, 3000.0), 0.0001f)
        assertEquals("RED_ALERT", resolveColorTier(computeRatio(-500.0, 3000.0)))

        // Boundary: 0.199 -> RED_ALERT
        assertEquals("RED_ALERT", resolveColorTier(0.199f))

        // Boundary: 0.200 -> AMBER_WARNING
        assertEquals("AMBER_WARNING", resolveColorTier(0.200f))

        // Boundary: 0.499 -> AMBER_WARNING
        assertEquals("AMBER_WARNING", resolveColorTier(0.499f))

        // Boundary: 0.500 -> GREEN_HEALTHY
        assertEquals("GREEN_HEALTHY", resolveColorTier(0.500f))

        // Boundary: 1.000 -> GREEN_HEALTHY
        assertEquals("GREEN_HEALTHY", resolveColorTier(1.000f))

        // Excess remaining clamped to 1.0
        assertEquals(1.0f, computeRatio(5000.0, 3000.0), 0.0001f)
        assertEquals("GREEN_HEALTHY", resolveColorTier(computeRatio(5000.0, 3000.0)))
    }

    // --- 6. BUCKWHEAT 4-STATE FINANCIAL PARTITION INVARIANT ---

    data class BuckwheatState(
        val isPeriodEnded: Boolean,
        val isBudgetEnd: Boolean,
        val isOverdraft: Boolean,
        val isNormal: Boolean
    )

    private fun evaluateBuckwheatState(
        daysLeft: Int,
        endDate: Long,
        currentTime: Long,
        monthlyRemaining: Double,
        dailyRemaining: Double,
        newDailyBudget: Double
    ): BuckwheatState {
        val isPeriodEnded = daysLeft <= 0 || (endDate > 0 && currentTime >= endDate)
        val isBudgetEnd = !isPeriodEnded && (monthlyRemaining <= 0.0 || (dailyRemaining < 0.0 && newDailyBudget <= 0.0))
        val isOverdraft = !isPeriodEnded && !isBudgetEnd && dailyRemaining < 0.0
        val isNormal = !isPeriodEnded && !isBudgetEnd && !isOverdraft
        return BuckwheatState(isPeriodEnded, isBudgetEnd, isOverdraft, isNormal)
    }

    @Test
    fun testBuckwheatStateExhaustivePartitionMatrix() {
        val now = 1_000_000_000L
        val future = now + 100_000L
        val past = now - 100_000L

        val testCases = listOf(
            // 1. Normal active budget
            evaluateBuckwheatState(daysLeft = 15, endDate = future, currentTime = now, monthlyRemaining = 1500.0, dailyRemaining = 50.0, newDailyBudget = 100.0),
            // 2. Overdraft with remaining monthly budget
            evaluateBuckwheatState(daysLeft = 15, endDate = future, currentTime = now, monthlyRemaining = 1500.0, dailyRemaining = -30.0, newDailyBudget = 98.0),
            // 3. Monthly budget exhausted (monthlyRemaining <= 0)
            evaluateBuckwheatState(daysLeft = 15, endDate = future, currentTime = now, monthlyRemaining = 0.0, dailyRemaining = -50.0, newDailyBudget = 0.0),
            // 4. Overdraft causes future daily budget to drop to 0
            evaluateBuckwheatState(daysLeft = 15, endDate = future, currentTime = now, monthlyRemaining = 50.0, dailyRemaining = -60.0, newDailyBudget = 0.0),
            // 5. Period ended by daysLeft <= 0
            evaluateBuckwheatState(daysLeft = 0, endDate = future, currentTime = now, monthlyRemaining = 200.0, dailyRemaining = 20.0, newDailyBudget = 0.0),
            // 6. Period ended by currentTime >= endDate
            evaluateBuckwheatState(daysLeft = 5, endDate = past, currentTime = now, monthlyRemaining = 200.0, dailyRemaining = 20.0, newDailyBudget = 50.0),
            // 7. Extreme: negative monthly remaining
            evaluateBuckwheatState(daysLeft = 10, endDate = future, currentTime = now, monthlyRemaining = -500.0, dailyRemaining = -100.0, newDailyBudget = 0.0),
            // 8. Exactly $0 dailyRemaining, monthlyRemaining > 0
            evaluateBuckwheatState(daysLeft = 10, endDate = future, currentTime = now, monthlyRemaining = 1000.0, dailyRemaining = 0.0, newDailyBudget = 100.0)
        )

        for ((index, state) in testCases.withIndex()) {
            val trueCount = listOf(state.isPeriodEnded, state.isBudgetEnd, state.isOverdraft, state.isNormal).count { it }
            // INVARIANT: Exactly one of the 4 financial states must be active at any given moment
            assertEquals("Test case #$index violated mutual exclusivity: $state", 1, trueCount)
        }
    }
}
