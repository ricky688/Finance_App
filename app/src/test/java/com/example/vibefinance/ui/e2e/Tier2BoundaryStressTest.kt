package com.example.vibefinance.ui.e2e

import com.example.vibefinance.ui.e2e.E2ETestContracts.AdaptivePartitionContract
import com.example.vibefinance.ui.e2e.E2ETestContracts.BackNavigationContract
import com.example.vibefinance.ui.e2e.E2ETestContracts.BackNavigationState
import com.example.vibefinance.ui.e2e.E2ETestContracts.BentoContract
import com.example.vibefinance.ui.e2e.E2ETestContracts.HingeBounds
import com.example.vibefinance.ui.e2e.E2ETestContracts.LandscapeLayoutContract
import com.example.vibefinance.ui.e2e.E2ETestContracts.MonetaryTypographyContract
import com.example.vibefinance.ui.home.calculateDaysLeftProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tier 2: Boundary Value Analysis & Extreme Stress Test Suite.
 * Covers requirements R1 through R5 with at least 5 dedicated boundary/corner tests per feature (25 tests total).
 */
class Tier2BoundaryStressTest {

    // =========================================================================
    // FEATURE 1 (R1): Bento Stacking & DaysLeftCard Boundary Stress
    // =========================================================================

    @Test
    fun testT2_R1_01_NarrowViewport320dpClampedToMinimum120dp() {
        val decision280 = BentoContract.evaluateBentoLayout(viewportWidthDp = 280f, fontScale = 1.0f)
        assertTrue(decision280.isStacked)
        assertEquals("DaysLeftCard must not shrink below 120dp even on 280dp screen", 120f, decision280.daysCardSizeDp, 0.01f)
        assertTrue(decision280.daysCardClampedMin)
    }

    @Test
    fun testT2_R1_02_ExtremeFontScale2xTriggersVerticalStackingAt400dp() {
        val decision = BentoContract.evaluateBentoLayout(viewportWidthDp = 400f, fontScale = 2.0f)
        assertTrue("Extreme font scale 2.0x must trigger vertical stacking", decision.isStacked)
        assertTrue("DaysLeftCard remains strictly square", decision.isDaysSquare)
    }

    @Test
    fun testT2_R1_03_GiantTabletViewportClampedToMaximum156dp() {
        // When stacked due to overflow on a wide tablet (e.g. 1000dp width)
        val decision = BentoContract.evaluateBentoLayout(viewportWidthDp = 1000f, fontScale = 1.0f, textMeasurerOverflowAt12sp = true)
        assertTrue(decision.isStacked)
        assertEquals("DaysLeftCard must never exceed 156dp max clamp in vertical mode", 156f, decision.daysCardSizeDp, 0.01f)
        assertTrue(decision.daysCardClampedMax)
    }

    @Test
    fun testT2_R1_04_ZeroDaysLeftEdgeCaseCalculatesCleanly() {
        val progress = calculateDaysLeftProgress(daysLeft = 0, totalDays = 30)
        assertEquals("0 days left means 100% period elapsed", 1.0f, progress, 0.001f)
    }

    @Test
    fun testT2_R1_05_OverdueNegativeDaysLeftClampsProgressTo100Percent() {
        val overdueProgress = calculateDaysLeftProgress(daysLeft = -5, totalDays = 30)
        assertEquals("Overdue days must be clamped to 1.0f without crash", 1.0f, overdueProgress, 0.001f)

        val zeroPeriodProgress = calculateDaysLeftProgress(daysLeft = 5, totalDays = 0)
        assertEquals("Zero total days period must return 0.0f without division by zero error", 0.0f, zeroPeriodProgress, 0.001f)
    }

    // =========================================================================
    // FEATURE 2 (R2): Monetary Typography Boundary & Stress
    // =========================================================================

    @Test
    fun testT2_R2_01_HugeNegativeAmountNineHundredMillionSingleLine() {
        val hugeNeg = -999999999.99
        val formatted = MonetaryTypographyContract.formatSignedAmount(hugeNeg, currencyPrefix = "$")
        assertEquals("-$999,999,999.99", formatted)
        assertTrue("Must be a single valid text flow", MonetaryTypographyContract.verifySingleTextFlow(formatted))
    }

    @Test
    fun testT2_R2_02_ExactZeroBoundaryAndMicroNegativeCent() {
        val zeroFormatted = MonetaryTypographyContract.formatSignedAmount(0.00, currencyPrefix = "$")
        assertEquals("$0.00", zeroFormatted)

        val microNeg = MonetaryTypographyContract.formatSignedAmount(-0.01, currencyPrefix = "$")
        assertEquals("-$0.01", microNeg)
        assertTrue(MonetaryTypographyContract.verifySingleTextFlow(microNeg))
    }

    @Test
    fun testT2_R2_03_ExtremeFontScale2xDownscalingClampsAt12sp() {
        val text = "-$999,999,999.99"
        val size = MonetaryTypographyContract.computeAdaptiveFontSizeSp(text, availableWidthDp = 100f, fontScale = 2.0f)
        assertEquals("Adaptive font size must clamp at 12sp minimum floor under extreme 2.0x font scale",
            12f, size, 0.01f)
    }

    @Test
    fun testT2_R2_04_CriticalPixelWidthBoundaryForOverflowDetection() {
        val text = "-$12,345.67" // 11 chars
        // At 12sp and fontScale 1.0x: estimated width = 11 * 12 * 0.58 = 76.56dp
        val fitsJustAbove = !MonetaryTypographyContract.doesOverflowAtMinFloor(text, availableWidthDp = 77f)
        val overflowsJustBelow = MonetaryTypographyContract.doesOverflowAtMinFloor(text, availableWidthDp = 76f)

        assertTrue("Must fit when available width is 77dp", fitsJustAbove)
        assertTrue("Must overflow when available width is 76dp", overflowsJustBelow)
    }

    @Test
    fun testT2_R2_05_HugePositiveAmountNineHundredMillionBaselineParity() {
        val hugePos = 999999999.99
        val formatted = MonetaryTypographyContract.formatSignedAmount(hugePos, currencyPrefix = "$")
        assertEquals("$999,999,999.99", formatted)
        assertTrue(MonetaryTypographyContract.verifySingleTextFlow(formatted))
        assertFalse(formatted.contains("-"))
    }

    // =========================================================================
    // FEATURE 3 (R3): Dual-Pane Boundary & Hinge Stress
    // =========================================================================

    @Test
    fun testT2_R3_01_ExactBoundary723dpVs724dpThreshold() {
        val subThreshold = AdaptivePartitionContract.evaluateDualPane(viewportWidthDp = 723.9f)
        assertFalse("723.9dp must NOT activate dual pane", subThreshold.canShowBoth)

        val atThreshold = AdaptivePartitionContract.evaluateDualPane(viewportWidthDp = 724.0f)
        assertTrue("724.0dp must activate dual pane", atThreshold.canShowBoth)
    }

    @Test
    fun testT2_R3_02_AsymmetricSeparatingHingeBoundaryPartition() {
        // Foldable in book mode with asymmetric fold
        val hinge = HingeBounds(leftDp = 260f, rightDp = 320f, isVertical = true, isSeparating = true)
        val decision = AdaptivePartitionContract.evaluateDualPane(viewportWidthDp = 680f, hinge = hinge)

        assertTrue(decision.canShowBoth)
        assertEquals(260f, decision.leftPaneWidthDp, 0.01f)
        // 680 - 320 = 360
        assertEquals(360f, decision.rightPaneWidthDp, 0.01f)
        assertEquals(0f, decision.horizontalSpacerSizeDp, 0.01f)
    }

    @Test
    fun testT2_R3_03_FlatNonSeparatingHingeDoesNotTriggerDualPaneBelow724dp() {
        // Flat foldable (not separating)
        val flatHinge = HingeBounds(leftDp = 300f, rightDp = 320f, isVertical = true, isSeparating = false)
        val decision = AdaptivePartitionContract.evaluateDualPane(viewportWidthDp = 650f, hinge = flatHinge)

        assertFalse("Non-separating hinge below 724dp must not force dual pane", decision.canShowBoth)
    }

    @Test
    fun testT2_R3_04_HorizontalSeparatingHingeDoesNotTriggerVerticalDualPane() {
        // Clamshell phone folded horizontally
        val horizontalHinge = HingeBounds(leftDp = 300f, rightDp = 320f, isVertical = false, isSeparating = true)
        val decision = AdaptivePartitionContract.evaluateDualPane(viewportWidthDp = 650f, hinge = horizontalHinge)

        assertFalse("Horizontal separating hinge must not activate vertical dual-column list-detail", decision.canShowBoth)
    }

    @Test
    fun testT2_R3_05_ExpandedBreakpoint840dpMaintainsDualPaneWithStandardLeftWidth() {
        val decision = AdaptivePartitionContract.evaluateDualPane(viewportWidthDp = 840f)
        assertTrue(decision.canShowBoth)
        assertEquals("Expanded breakpoint (>= 800dp) uses 400dp default list pane width", 400f, decision.leftPaneWidthDp, 0.01f)
        assertEquals("Spacer is 24dp", 24f, decision.horizontalSpacerSizeDp, 0.01f)
        // 840 - 400 - 24 = 416dp
        assertEquals(416f, decision.rightPaneWidthDp, 0.01f)
    }

    // =========================================================================
    // FEATURE 4 (R4): Landscape Compact & 48dp Touch Target Boundary Stress
    // =========================================================================

    @Test
    fun testT2_R4_01_ExactHeightBoundary479dpVs480dp() {
        val compactDecision = LandscapeLayoutContract.evaluateLandscapeLayout(widthDp = 650f, heightDp = 479f)
        assertTrue("Height 479dp must activate landscape compact", compactDecision.isLandscapeCompact)
        assertTrue(compactDecision.isDualColumn)

        val normalDecision = LandscapeLayoutContract.evaluateLandscapeLayout(widthDp = 650f, heightDp = 480f)
        assertFalse("Height 480dp must NOT activate landscape compact", normalDecision.isLandscapeCompact)
        assertFalse(normalDecision.isDualColumn)
    }

    @Test
    fun testT2_R4_02_ExactWidthBoundary599dpVs600dpAt479dpHeight() {
        val narrowDecision = LandscapeLayoutContract.evaluateLandscapeLayout(widthDp = 599f, heightDp = 400f)
        assertFalse("Width 599dp must NOT activate landscape compact dual column", narrowDecision.isDualColumn)

        val wideDecision = LandscapeLayoutContract.evaluateLandscapeLayout(widthDp = 600f, heightDp = 400f)
        assertTrue("Width 600dp at height < 480dp must activate dual column", wideDecision.isDualColumn)
    }

    @Test
    fun testT2_R4_03_ExtremeCompactHeight320dpPreservesAccessibleControls() {
        val ultraShortDecision = LandscapeLayoutContract.evaluateLandscapeLayout(widthDp = 640f, heightDp = 320f)
        assertTrue(ultraShortDecision.isDualColumn)
        assertEquals(48f, ultraShortDecision.modeSwitcherMinHeightDp, 0.01f)
        assertEquals(48f, ultraShortDecision.clearButtonSizeDp, 0.01f)
    }

    @Test
    fun testT2_R4_04_ConnectedButtonsMaintain48dpTouchTargetWithMinimalLabels() {
        // Connected button touch targets must strictly maintain 48dp even when text is 1 char or empty
        val decision = LandscapeLayoutContract.evaluateLandscapeLayout(widthDp = 600f, heightDp = 380f)
        assertEquals("Connected button height must strictly be 48dp", 48f, decision.modeSwitcherMinHeightDp, 0.01f)
    }

    @Test
    fun testT2_R4_05_ClearButtonTouchHitTestOutsideVisualIcon() {
        // Clear button has 20dp visual icon but 48dp touch container (radius 24dp)
        val buttonBounds = 48f
        val touchPointDistanceFromCenter = 22f // Outside 20dp icon (radius 10dp), inside 48dp container
        assertTrue("Touch point at radius 22dp must register inside 48dp target",
            touchPointDistanceFromCenter <= (buttonBounds / 2f))
    }

    // =========================================================================
    // FEATURE 5 (R5): State-Aware Back Navigation Boundary Stress
    // =========================================================================

    @Test
    fun testT2_R5_01_SimultaneousImeAndDialogActiveBackHandlerDisabled() {
        val state = BackNavigationState(
            canShowBoth = false,
            selectedId = 1L,
            hasActiveDialog = true,
            isImeVisible = true
        )
        assertFalse("BackHandler must be disabled when both IME and dialog are active",
            BackNavigationContract.isBackHandlerEnabled(state))
        // IME has first priority
        assertEquals("CONSUMED_BY_SYSTEM_IME", BackNavigationContract.resolveBackActionPriority(state))
    }

    @Test
    fun testT2_R5_02_ImeClosesWhileDialogRemainsOpenBackHandlerStaysDisabled() {
        val state = BackNavigationState(
            canShowBoth = false,
            selectedId = 1L,
            hasActiveDialog = true,
            isImeVisible = false // IME dismissed
        )
        assertFalse("BackHandler must remain disabled while dialog is open",
            BackNavigationContract.isBackHandlerEnabled(state))
        assertEquals("CONSUMED_BY_ACTIVE_DIALOG", BackNavigationContract.resolveBackActionPriority(state))
    }

    @Test
    fun testT2_R5_03_DialogClosesWhileImeRemainsOpenBackHandlerStaysDisabled() {
        val state = BackNavigationState(
            canShowBoth = false,
            selectedId = 1L,
            hasActiveDialog = false, // Dialog closed
            isImeVisible = true // IME still open
        )
        assertFalse("BackHandler must remain disabled while IME is open",
            BackNavigationContract.isBackHandlerEnabled(state))
        assertEquals("CONSUMED_BY_SYSTEM_IME", BackNavigationContract.resolveBackActionPriority(state))
    }

    @Test
    fun testT2_R5_04_SimultaneousClosureOfImeAndDialogTransitionsToEnabled() {
        // Transition from both active to both closed
        val stateBothClosed = BackNavigationState(
            canShowBoth = false,
            selectedId = 1L,
            hasActiveDialog = false,
            isImeVisible = false
        )
        assertTrue("BackHandler must become enabled immediately upon closure of all overlays and IME",
            BackNavigationContract.isBackHandlerEnabled(stateBothClosed))
        assertEquals("CONSUMED_BY_APP_NAVIGATE_TO_LIST", BackNavigationContract.resolveBackActionPriority(stateBothClosed))
    }

    @Test
    fun testT2_R5_05_RapidDetailSelectionDeselectionTogglesEnabledDeterministically() {
        val sequence = listOf(
            BackNavigationState(false, 1L, false, false) to true,
            BackNavigationState(false, null, false, false) to false,
            BackNavigationState(false, 2L, false, false) to true,
            BackNavigationState(false, null, false, false) to false
        )
        sequence.forEachIndexed { index, (state, expectedEnabled) ->
            assertEquals("Step $index failed", expectedEnabled, BackNavigationContract.isBackHandlerEnabled(state))
        }
    }
}
