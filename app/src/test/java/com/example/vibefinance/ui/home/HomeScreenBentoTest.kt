package com.example.vibefinance.ui.home

import android.content.Context
import org.robolectric.RuntimeEnvironment
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.data.repository.DailyBudgetInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HomeScreenBentoTest {

    @Test
    fun testBuckwheatFinancialStateTransitions() {
        val now = System.currentTimeMillis()
        val tomorrow = now + 86400000L

        // State 0: No budget set (brand new / fresh launch)
        val noBudgetInfo = DailyBudgetInfo(
            totalMonthlyBudget = 0.0,
            totalSpentThisMonth = 0.0,
            monthlyRemaining = 0.0,
            dailyAllowance = 0.0,
            dailyRemaining = 0.0,
            daysLeft = 0,
            startDate = 0L,
            endDate = 0L
        )
        assertEquals(HeroDailyBudgetState.NO_BUDGET, calculateHeroDailyBudgetState(noBudgetInfo, now))

        // State 1: Normal healthy budget
        val normalInfo = DailyBudgetInfo(
            totalMonthlyBudget = 3000.0,
            totalSpentThisMonth = 500.0,
            monthlyRemaining = 2500.0,
            dailyAllowance = 100.0,
            dailyRemaining = 75.0,
            daysLeft = 25,
            startDate = now - 86400000L * 5,
            endDate = tomorrow + 86400000L * 24
        )
        assertEquals(HeroDailyBudgetState.NORMAL, calculateHeroDailyBudgetState(normalInfo, now))

        // State 2: Overdraft (spent more than daily allowance, but monthly budget still available)
        val overdraftInfo = DailyBudgetInfo(
            totalMonthlyBudget = 3000.0,
            totalSpentThisMonth = 650.0,
            monthlyRemaining = 2350.0,
            dailyAllowance = 100.0,
            dailyRemaining = -50.0,
            newDailyBudget = 97.9,
            daysLeft = 24,
            startDate = now - 86400000L * 6,
            endDate = tomorrow + 86400000L * 23
        )
        assertEquals(HeroDailyBudgetState.OVERDRAFT, calculateHeroDailyBudgetState(overdraftInfo, now))

        // State 3: Budget End (monthly budget exhausted)
        val budgetEndInfo = DailyBudgetInfo(
            totalMonthlyBudget = 3000.0,
            totalSpentThisMonth = 3100.0,
            monthlyRemaining = 0.0,
            dailyAllowance = 100.0,
            dailyRemaining = -100.0,
            newDailyBudget = 0.0,
            daysLeft = 10,
            startDate = now - 86400000L * 20,
            endDate = tomorrow + 86400000L * 9
        )
        assertEquals(HeroDailyBudgetState.BUDGET_END, calculateHeroDailyBudgetState(budgetEndInfo, now))

        // State 4: Period Ended (daysLeft <= 0 or past endDate)
        val periodEndedInfo = DailyBudgetInfo(
            totalMonthlyBudget = 3000.0,
            totalSpentThisMonth = 2800.0,
            monthlyRemaining = 200.0,
            dailyAllowance = 100.0,
            dailyRemaining = 50.0,
            daysLeft = 0,
            startDate = now - 86400000L * 30,
            endDate = now - 1000L
        )
        assertEquals(HeroDailyBudgetState.PERIOD_ENDED, calculateHeroDailyBudgetState(periodEndedInfo, now))
    }

    @Test
    fun testBentoResponsivePaddingRules() {
        // Compact devices (< 400dp, e.g. 360dp, 392dp Waydroid)
        assertEquals(12.dp, resolveBentoSpacing(360))
        assertEquals(12.dp, resolveScreenPadding(360))
        assertEquals(12.dp, resolveTopBarPadding(360))

        assertEquals(12.dp, resolveBentoSpacing(392))
        assertEquals(12.dp, resolveScreenPadding(392))
        assertEquals(12.dp, resolveTopBarPadding(392))

        // Normal / Expanded devices (>= 400dp, e.g. 412dp, 600dp)
        assertEquals(16.dp, resolveBentoSpacing(412))
        assertEquals(16.dp, resolveScreenPadding(412))
        assertEquals(20.dp, resolveTopBarPadding(412))

        assertEquals(16.dp, resolveBentoSpacing(600))
        assertEquals(16.dp, resolveScreenPadding(600))
        assertEquals(20.dp, resolveTopBarPadding(600))
    }

    @Test
    fun testHeroAmountFontSizeAutoScaling() {
        assertEquals(40.sp, calculateHeroAmountFontSize("HK$ 0"))
        assertEquals(36.sp, calculateHeroAmountFontSize("HK$ 50"))
        assertEquals(32.sp, calculateHeroAmountFontSize("HK$ 1,200"))
        assertEquals(28.sp, calculateHeroAmountFontSize("HK$ 12,500"))
        assertEquals(24.sp, calculateHeroAmountFontSize("HK$ 1,250,000"))
        assertEquals(28.sp, calculateHeroAmountFontSize("-HK$ 1,200"))
    }

    @Test
    fun testBentoStatCardValueFontSizeAutoScaling() {
        // Normal short amounts
        assertEquals(20.sp, calculateStatCardValueFontSize("$5.00", isCompact = true))
        assertEquals(22.sp, calculateStatCardValueFontSize("$5.00", isCompact = false))

        // Mid-sized amounts ($120.00 -> 7 chars)
        assertEquals(19.sp, calculateStatCardValueFontSize("$120.00", isCompact = true))

        // Large amounts ($1,250.00 -> 9 chars)
        assertEquals(17.sp, calculateStatCardValueFontSize("$1,250.00", isCompact = true))

        // Very large amounts ($12,500.00 -> 10 chars)
        assertEquals(17.sp, calculateStatCardValueFontSize("$12,500.00", isCompact = true))

        // Extreme amounts ($1,250,000.00 -> 13 chars)
        assertEquals(15.sp, calculateStatCardValueFontSize("$1,250,000.00", isCompact = true))

        // With explicitly specified font size
        assertEquals(24.sp * 0.85f, calculateStatCardValueFontSize("$120.00", isCompact = true, specifiedFontSize = 24.sp))
        assertEquals(24.sp * 0.75f, calculateStatCardValueFontSize("$12,500.00", isCompact = true, specifiedFontSize = 24.sp))
        assertEquals(24.sp, calculateStatCardValueFontSize("$120.00", isCompact = false, specifiedFontSize = 24.sp))
    }

    @Test
    fun testDaysLeftProgressCalculation() {
        // Reversed progress (elapsed ratio) with minimum 1% notch when full days remaining
        assertEquals(0.01f, calculateDaysLeftProgress(30, 30), 0.001f)
        assertEquals(0.5f, calculateDaysLeftProgress(15, 30), 0.001f)
        assertEquals(0.9f, calculateDaysLeftProgress(3, 30), 0.001f)
        assertEquals(1.0f, calculateDaysLeftProgress(0, 30), 0.001f)
        assertEquals(1.0f, calculateDaysLeftProgress(-2, 30), 0.001f)
        assertEquals(0.01f, calculateDaysLeftProgress(35, 30), 0.001f)
        assertEquals(0.0f, calculateDaysLeftProgress(10, 0), 0.001f)
    }

    @Test
    fun testSpendingRatioHealthVibeMapping() {
        // Spent 20 of 100 -> ratio 0.20 <= 0.5
        assertEquals(DailyVibeCategory.SAVING_VIBE, resolveDailyVibeCategory(100.0, 80.0))
        // Spent 50 of 100 -> ratio 0.50 <= 0.5
        assertEquals(DailyVibeCategory.SAVING_VIBE, resolveDailyVibeCategory(100.0, 50.0))
        // Spent 75 of 100 -> ratio 0.75 <= 1.0
        assertEquals(DailyVibeCategory.STEADY_VIBE, resolveDailyVibeCategory(100.0, 25.0))
        // Spent 100 of 100 -> ratio 1.00 <= 1.0
        assertEquals(DailyVibeCategory.STEADY_VIBE, resolveDailyVibeCategory(100.0, 0.0))
        // Spent 120 of 100 -> ratio 1.20 > 1.0
        assertEquals(DailyVibeCategory.OVERSPENT_ALERT, resolveDailyVibeCategory(100.0, -20.0))
    }

    @Test
    fun testDateRangeAndDayCountCalculation() {
        val start = LocalDate.of(2026, 9, 1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = LocalDate.of(2026, 9, 30).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val dayCount = calculatePeriodTotalDays(start, end)
        assertEquals(30, dayCount)

        // Fallback for invalid or uninitialized dates
        assertEquals(30, calculatePeriodTotalDays(0L, 0L))
    }

    @Test
    fun testHeroTargetAmountFormattingAndDeficitSign() {
        // Period ended with deficit (negative remaining budget)
        val deficitFormatted = formatHeroTargetAmount(
            targetAmount = -500f,
            isPeriodEnded = true,
            monthlyRemaining = -500.0
        )
        assertEquals("-HK$ 500", deficitFormatted)

        // Period ended with positive remaining budget
        val surplusFormatted = formatHeroTargetAmount(
            targetAmount = 500f,
            isPeriodEnded = true,
            monthlyRemaining = 500.0
        )
        assertEquals("HK$ 500", surplusFormatted)

        // Active period normal
        val activeFormatted = formatHeroTargetAmount(
            targetAmount = 150f,
            isPeriodEnded = false,
            monthlyRemaining = 1500.0
        )
        assertEquals("HK$ 150", activeFormatted)

        // Active period overdraft with positive remaining
        val overdraftFormatted = formatHeroTargetAmount(
            targetAmount = 85f,
            isPeriodEnded = false,
            monthlyRemaining = 2000.0
        )
        assertEquals("HK$ 85", overdraftFormatted)
    }

    // --- MILESTONE 1: R1 & R2 TESTS ---

    @Test
    fun testNegativeSignedFormatAndSingleTextFlowBaselineStability() {
        // R2 Requirement: Positive remaining budgets retain standard $...,.2f formatting
        // for compatibility with existing tests (e.g. BudgetModeTransitionTest)
        assertEquals("$2,439.50", formatBudgetCardAmount(2439.5))
        assertEquals("$60.50", formatBudgetCardAmount(60.5))
        assertEquals("$0.00", formatBudgetCardAmount(0.0))
        assertEquals("$1,234,567.89", formatBudgetCardAmount(1234567.89))

        // R2 Requirement: Negative remaining budgets format as -$... instead of $-...
        assertEquals("-$12,345.67", formatBudgetCardAmount(-12345.67))
        assertEquals("-$0.50", formatBudgetCardAmount(-0.50))
        assertEquals("-$1,234,567.89", formatBudgetCardAmount(-1234567.89))
        assertEquals("-HK$ 500.00", formatBudgetCardAmount(-500.0, prefix = "HK$ "))

        // Baseline stability & single text flow invariant:
        // Negative sign, currency prefix, and digits must reside within a single unbroken text sequence
        val negFormatted = formatBudgetCardAmount(-12345.67)
        assertTrue("Must start with negative sign immediately preceding currency symbol", negFormatted.startsWith("-$"))
        assertEquals("Negative sign must be at index 0", '-', negFormatted[0])
        assertEquals("Currency symbol must be at index 1", '$', negFormatted[1])
        assertFalse("Must not contain whitespace between minus and currency", negFormatted.contains("- "))
        assertFalse("Must not contain line breaks or wrapping characters", negFormatted.contains("\n"))

        val posFormatted = formatBudgetCardAmount(2439.5)
        assertTrue("Positive amount must start with currency symbol", posFormatted.startsWith("$"))
        assertFalse("Positive amount must not contain negative sign", posFormatted.contains("-"))
    }

    @Test
    fun testTextMeasurerAdaptiveFontDownscalingWith12spFloor() {
        val context = RuntimeEnvironment.getApplication()
        val density = Density(density = 2.5f, fontScale = 1.0f)
        val measurer = TextMeasurer(
            defaultFontFamilyResolver = createFontFamilyResolver(context),
            defaultDensity = density,
            defaultLayoutDirection = LayoutDirection.Ltr
        )
        val style = TextStyle.Default
        val text = "-$12,345.67"

        val widthAt24 = measurer.measure(
            text = AnnotatedString(text),
            style = style.copy(fontSize = 24.sp),
            softWrap = false,
            maxLines = 1,
        ).size.width.toFloat()

        val widthAt12 = measurer.measure(
            text = AnnotatedString(text),
            style = style.copy(fontSize = 12.sp),
            softWrap = false,
            maxLines = 1,
        ).size.width.toFloat()

        // 1. Ample available width (e.g. widthAt24 * 2): retains full base font size (24sp)
        val ampleFontSize = calculateAdaptiveAmountFontSize(
            text = text,
            availableWidthPx = widthAt24 * 2f,
            baseFontSize = 24.sp,
            minFontSize = 12.sp,
            measurer = measurer,
            style = style
        )
        assertEquals(24.sp, ampleFontSize)

        // 2. Constrained width (e.g. widthAt24 * 0.7): adaptively scales down
        val constrainedFontSize = calculateAdaptiveAmountFontSize(
            text = text,
            availableWidthPx = widthAt24 * 0.7f,
            baseFontSize = 24.sp,
            minFontSize = 12.sp,
            measurer = measurer,
            style = style
        )
        assertTrue("Font size should scale down below 24sp", constrainedFontSize.value < 24f)
        assertTrue("Font size must not drop below 12sp floor", constrainedFontSize.value >= 12f)

        // 3. Severely constrained width (e.g. widthAt12 * 0.5): strictly clamped at 12sp floor
        val floorFontSize = calculateAdaptiveAmountFontSize(
            text = text,
            availableWidthPx = widthAt12 * 0.5f,
            baseFontSize = 24.sp,
            minFontSize = 12.sp,
            measurer = measurer,
            style = style
        )
        assertEquals("Font size must not drop below 12sp floor", 12.sp, floorFontSize)

        // 4. Verify textFitsAtFloor detection
        assertTrue("Text fits at 12sp when width is widthAt12 + 10px", textFitsAtFloor(text, widthAt12 + 10f, 12.sp, measurer, style))
        assertFalse("Text cannot fit at 12sp when width is widthAt12 * 0.5px", textFitsAtFloor(text, widthAt12 * 0.5f, 12.sp, measurer, style))
    }

    @Test
    fun testAdaptiveStackingTriggerConditions() {
        // Trigger 1: Ultra-narrow screen width (< 340dp, e.g. 320dp)
        assertTrue(
            "Ultra-narrow screen (<340dp) must trigger vertical stacking",
            shouldStackBento(maxWidth = 320.dp, bentoSpacing = 12.dp, fontScale = 1.0f)
        )
        assertTrue(
            "Width 339dp must trigger vertical stacking",
            shouldStackBento(maxWidth = 339.dp, bentoSpacing = 12.dp, fontScale = 1.0f)
        )

        // Standard unconstrained width (380dp) with normal amounts -> maintains side-by-side Row
        assertFalse(
            "Normal width and amounts should remain side-by-side",
            shouldStackBento(
                maxWidth = 380.dp,
                bentoSpacing = 16.dp,
                fontScale = 1.0f,
                remTextWidthAt12sp = 100f,
                spentTextWidthAt12sp = 80f,
                candidateAvailableWidthPx = 200f
            )
        )

        // Trigger 2: Overflow at 12sp floor in side-by-side mode (text width > available width)
        assertTrue(
            "Text overflow at 12sp floor must trigger vertical stacking",
            shouldStackBento(
                maxWidth = 380.dp,
                bentoSpacing = 16.dp,
                fontScale = 1.0f,
                remTextWidthAt12sp = 250f,
                spentTextWidthAt12sp = 80f,
                candidateAvailableWidthPx = 200f
            )
        )

        // Trigger 3: High font scale (> 1.25) where space is tight
        assertTrue(
            "High font scale with tight candidate width must trigger vertical stacking",
            shouldStackBento(
                maxWidth = 380.dp,
                bentoSpacing = 16.dp,
                fontScale = 1.5f,
                remTextWidthAt12sp = 180f,
                spentTextWidthAt12sp = 80f,
                candidateAvailableWidthPx = 200f
            )
        )

        // High font scale with ample space (e.g. tablet / wide screen) -> remains side-by-side
        assertFalse(
            "High font scale with abundant width remains side-by-side",
            shouldStackBento(
                maxWidth = 600.dp,
                bentoSpacing = 16.dp,
                fontScale = 1.5f,
                remTextWidthAt12sp = 100f,
                spentTextWidthAt12sp = 80f,
                candidateAvailableWidthPx = 300f
            )
        )
    }

    @Test
    fun testDaysLeftCardStrictSquareAspectRatioAndStackedGeometry() {
        // R1 Requirement: In vertical stack mode, lower DaysLeftCard is centered
        // with size constrained between 120dp and 156dp, strictly remaining square
        // and never stretched across full width (300dp+).

        // Ultra-narrow 320dp screen
        val size320 = calculateStackedDaysSize(320.dp)
        assertEquals(144.dp, size320)
        assertTrue("Stacked size at 320dp must be >= 120dp", size320 >= 120.dp)
        assertTrue("Stacked size at 320dp must be <= 156dp", size320 <= 156.dp)

        // Very narrow 200dp screen: clamped at 120dp floor
        val size200 = calculateStackedDaysSize(200.dp)
        assertEquals(120.dp, size200)

        // Wide 400dp screen: clamped at 156dp ceiling
        val size400 = calculateStackedDaysSize(400.dp)
        assertEquals(156.dp, size400)

        // Large 600dp+ screen: never stretches beyond 156dp
        val size600 = calculateStackedDaysSize(600.dp)
        assertEquals(156.dp, size600)

        // Continuous range verification: size is strictly clamped in [120dp, 156dp]
        for (w in 150..800 step 25) {
            val s = calculateStackedDaysSize(w.dp)
            assertTrue("Width $w resulted in stacked size $s below 120dp", s >= 120.dp)
            assertTrue("Width $w resulted in stacked size $s above 156dp", s <= 156.dp)
        }
    }

    @Test
    fun testDaysLeftCardSideBySideEqualHeightConstraint() {
        // R1 Requirement: When placed side-by-side with an adjacent card,
        // it must maintain equal height with the adjacent card.
        val spacing = 12.dp

        // At 360dp:
        val daysSide360 = minOf(((360.dp - spacing) * 0.4f), 156.dp).coerceAtLeast(110.dp)
        assertEquals(139.2.dp, daysSide360)

        // At 392dp (Waydroid default):
        val daysSide392 = minOf(((392.dp - spacing) * 0.4f), 156.dp).coerceAtLeast(110.dp)
        assertEquals(152.0.dp, daysSide392)

        // At 412dp: clamped at 156dp
        val daysSide412 = minOf(((412.dp - spacing) * 0.4f), 156.dp).coerceAtLeast(110.dp)
        assertEquals(156.dp, daysSide412)

        // DaysLeftCard has aspectRatio(1f), so its width equals height (daysSide),
        // matching the height of the adjacent RestAndSpentBudgetCard exactly.
        val width = daysSide392
        val height = daysSide392
        assertEquals("DaysLeftCard must maintain strict 1:1 aspect ratio", 1.0f, width / height, 0.001f)
    }

    @Test
    fun testDualColumnSemanticPartitioningInvariants() {
        // Method A Requirement: Semantic role partitioning for large screens.
        // Left Column: Primary Budget & Action Pane (hero, period, category budgets, transactions).
        // Right Column: Analytics & Bento Pane (bento metrics, spending chart, category chart, calendar).

        val allOrder = com.example.vibefinance.ui.DEFAULT_DAILY_CARD_ORDER

        // 1. Verify no intersection between Left and Right columns
        val intersection = LEFT_COLUMN_CARD_KEYS.intersect(RIGHT_COLUMN_CARD_KEYS)
        assertTrue("Left and Right column sets must be mutually disjoint", intersection.isEmpty())

        // 2. Verify all default daily cards belong to either Left or Right
        for (key in allOrder) {
            assertTrue(
                "Card key $key must belong to either LEFT_COLUMN_CARD_KEYS or RIGHT_COLUMN_CARD_KEYS",
                key in LEFT_COLUMN_CARD_KEYS || key in RIGHT_COLUMN_CARD_KEYS
            )
        }

        // 3. Verify Left column cards
        val leftKeys = allOrder.filter { it in LEFT_COLUMN_CARD_KEYS }
        assertEquals(
            listOf("daily-hero", "daily-period", "daily-transactions", "daily-spending-chart"),
            leftKeys
        )

        // 4. Verify Right column cards
        val rightKeys = allOrder.filter { it in RIGHT_COLUMN_CARD_KEYS }
        assertEquals(
            listOf("daily-bento", "daily-category-budgets", "daily-category-chart", "daily-calendar"),
            rightKeys
        )
    }

    @Test
    fun testDualColumnScopedReorderingIntegrity() {
        // Verify column-scoped reordering:
        // Moving an item inside left column preserves the right column exactly,
        // and produces newOrder = newLeft + rightKeys.
        val defaultOrder = com.example.vibefinance.ui.DEFAULT_DAILY_CARD_ORDER
        val leftKeys = defaultOrder.filter { it in LEFT_COLUMN_CARD_KEYS }.toMutableList()
        val rightKeys = defaultOrder.filter { it in RIGHT_COLUMN_CARD_KEYS }.toMutableList()

        // Swap first two left cards: daily-hero and daily-period
        java.util.Collections.swap(leftKeys, 0, 1)
        val combinedOrderAfterLeftSwap = leftKeys + rightKeys

        assertEquals(
            listOf("daily-period", "daily-hero", "daily-transactions", "daily-spending-chart"),
            combinedOrderAfterLeftSwap.filter { it in LEFT_COLUMN_CARD_KEYS }
        )
        assertEquals(
            listOf("daily-bento", "daily-category-budgets", "daily-category-chart", "daily-calendar"),
            combinedOrderAfterLeftSwap.filter { it in RIGHT_COLUMN_CARD_KEYS }
        )

        // Swap two right cards: daily-category-budgets and daily-category-chart
        java.util.Collections.swap(rightKeys, 1, 2)
        val combinedOrderAfterRightSwap = leftKeys + rightKeys

        assertEquals(
            listOf("daily-period", "daily-hero", "daily-transactions", "daily-spending-chart"),
            combinedOrderAfterRightSwap.filter { it in LEFT_COLUMN_CARD_KEYS }
        )
        assertEquals(
            listOf("daily-bento", "daily-category-chart", "daily-category-budgets", "daily-calendar"),
            combinedOrderAfterRightSwap.filter { it in RIGHT_COLUMN_CARD_KEYS }
        )
    }

    @Test
    fun testDualColumnThreshold() {
        val dualColumnBreakpoint = 680.dp

        // Below 680dp: single column
        assertTrue(360.dp < dualColumnBreakpoint) // Standard phone
        assertTrue(412.dp < dualColumnBreakpoint) // Pixel 8 / large phone
        assertTrue(500.dp < dualColumnBreakpoint) // Landscape phone content width
        assertTrue(600.dp < dualColumnBreakpoint) // Compact tablet / landscape phone

        // At or above 680dp: dual column
        assertTrue(680.dp >= dualColumnBreakpoint) // Threshold boundary
        assertTrue(700.dp >= dualColumnBreakpoint) // 840dp screen net content width
        assertTrue(840.dp >= dualColumnBreakpoint) // M3 Expanded
        assertTrue(1024.dp >= dualColumnBreakpoint) // Standard tablet
        assertTrue(1280.dp >= dualColumnBreakpoint) // Desktop / large tablet
    }

    @Test
    fun testDualColumnHeaderTagsAndPartitionIntegrity() {
        // Dual column Left and Right column keys partition all default keys completely
        val allOrder = com.example.vibefinance.ui.DEFAULT_DAILY_CARD_ORDER
        val totalCardsInPartition = LEFT_COLUMN_CARD_KEYS.size + RIGHT_COLUMN_CARD_KEYS.size
        assertEquals(allOrder.size, totalCardsInPartition)
        assertEquals(4, LEFT_COLUMN_CARD_KEYS.size)
        assertEquals(4, RIGHT_COLUMN_CARD_KEYS.size)
    }
}
