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
 * Tier 1: Feature Coverage Test Suite.
 * Covers requirements R1 through R5 with at least 5 dedicated unit tests per feature (25 tests total).
 */
class Tier1FeatureCoverageTest {

    // =========================================================================
    // FEATURE 1 (R1): DaysLeftCard 1:1 Aspect Ratio & Bento Responsive Stacking
    // =========================================================================

    @Test
    fun testR1_01_DaysLeftCardEnforcesSquareAspectRatio() {
        val decision = BentoContract.evaluateBentoLayout(viewportWidthDp = 400f, fontScale = 1.0f)
        assertTrue("DaysLeftCard must strictly maintain 1:1 square aspect ratio", decision.isDaysSquare)
    }

    @Test
    fun testR1_02_DaysLeftCardMatchesAdjacentCardHeightInRow() {
        val decision = BentoContract.evaluateBentoLayout(viewportWidthDp = 400f, fontScale = 1.0f)
        assertFalse("Standard viewport must use horizontal Row", decision.isStacked)
        // In Row mode, Bento container sets Modifier.height(daysSide) for both cards
        assertEquals("Days card size should match daysSide", 155.2f, decision.daysCardSizeDp, 0.1f)
    }

    @Test
    fun testR1_03_BentoUsesHorizontalRowOnStandardWidth() {
        val decision = BentoContract.evaluateBentoLayout(viewportWidthDp = 360f, fontScale = 1.0f)
        assertFalse("360dp width at 1.0x fontScale must stay in horizontal Row", decision.isStacked)
        assertTrue("Budget card must receive positive horizontal allocation", decision.budgetCardWidthDp > 100f)
    }

    @Test
    fun testR1_04_BentoSwitchesToVerticalColumnOnNarrowWidth() {
        val decision = BentoContract.evaluateBentoLayout(viewportWidthDp = 320f, fontScale = 1.0f)
        assertTrue("320dp narrow width must switch to vertical Column stacking", decision.isStacked)
        assertEquals("Stacked budget card must occupy 100% viewport width", 320f, decision.budgetCardWidthDp, 0.01f)
    }

    @Test
    fun testR1_05_DaysLeftCardClampedBetween120And156InColumn() {
        val narrowDecision = BentoContract.evaluateBentoLayout(viewportWidthDp = 320f, fontScale = 1.0f)
        assertTrue("Narrow layout must be stacked", narrowDecision.isStacked)
        assertTrue("DaysLeftCard size must be bounded between 120dp and 156dp", narrowDecision.daysCardSizeDp in 120f..156f)
        assertEquals("DaysLeftCard size on 320dp viewport is 134.4dp", 134.4f, narrowDecision.daysCardSizeDp, 0.1f)

        val extremeNarrow = BentoContract.evaluateBentoLayout(viewportWidthDp = 280f, fontScale = 1.0f)
        assertTrue("DaysLeftCard size must be clamped at min 120dp", extremeNarrow.daysCardClampedMin)
        assertEquals(120f, extremeNarrow.daysCardSizeDp, 0.01f)

        val wideStackedDecision = BentoContract.evaluateBentoLayout(viewportWidthDp = 450f, fontScale = 1.5f)
        assertTrue("Large fontScale must trigger stacking", wideStackedDecision.isStacked)
        assertTrue("DaysLeftCard size must not exceed 156dp", wideStackedDecision.daysCardSizeDp <= 156f)
        assertTrue("DaysLeftCard size must be clamped at max 156dp", wideStackedDecision.daysCardClampedMax)
        assertEquals(156f, wideStackedDecision.daysCardSizeDp, 0.01f)
    }

    // =========================================================================
    // FEATURE 2 (R2): Large Negative Monetary Amounts Single Line & Stable Baseline
    // =========================================================================

    @Test
    fun testR2_01_NegativeAmountFormattedWithLeadingSignBeforeCurrency() {
        val formatted = MonetaryTypographyContract.formatSignedAmount(-12345.67, currencyPrefix = "$")
        assertEquals("Negative amount must format as '-$12,345.67' rather than '$-12,345.67'", "-$12,345.67", formatted)

        val positiveFormatted = MonetaryTypographyContract.formatSignedAmount(2439.50, currencyPrefix = "$")
        assertEquals("Positive amount must maintain standard '$2,439.50'", "$2,439.50", positiveFormatted)
    }

    @Test
    fun testR2_02_NegativeSignCurrencyAndDigitsInSingleContinuousTextFlow() {
        val formatted = MonetaryTypographyContract.formatSignedAmount(-999999.99, currencyPrefix = "$")
        assertTrue("Negative sign, currency, and digits must belong to a single text flow",
            MonetaryTypographyContract.verifySingleTextFlow(formatted))
        assertFalse("Text flow must not contain line breaks", formatted.contains("\n"))
    }

    @Test
    fun testR2_03_AdaptiveFontScalingDownscalesUnderConstrainedWidth() {
        val text = "-$12,345.67"
        val wideFontSize = MonetaryTypographyContract.computeAdaptiveFontSizeSp(text, availableWidthDp = 200f)
        val narrowFontSize = MonetaryTypographyContract.computeAdaptiveFontSizeSp(text, availableWidthDp = 100f)

        assertTrue("Constrained width must trigger downscaling", narrowFontSize < wideFontSize)
    }

    @Test
    fun testR2_04_AdaptiveFontScalingRespects12spMinimumFloor() {
        val hugeText = "-$999,999,999.99"
        val calculatedSize = MonetaryTypographyContract.computeAdaptiveFontSizeSp(hugeText, availableWidthDp = 50f)
        assertEquals("Font scaling must strictly adhere to 12sp minimum floor", 12f, calculatedSize, 0.01f)
    }

    @Test
    fun testR2_05_OverflowAt12spTriggersContainerVerticalStacking() {
        val hugeText = "-$999,999,999.99"
        // In side-by-side mode, available text width is ~110dp
        val overflowsSideBySide = MonetaryTypographyContract.doesOverflowAtMinFloor(hugeText, availableWidthDp = 110f)
        assertTrue("Huge negative amount must overflow side-by-side slot at 12sp", overflowsSideBySide)

        val layoutDecision = BentoContract.evaluateBentoLayout(viewportWidthDp = 360f, textMeasurerOverflowAt12sp = overflowsSideBySide)
        assertTrue("Amount overflow at 12sp floor must trigger parent vertical stacking", layoutDecision.isStacked)
    }

    // =========================================================================
    // FEATURE 3 (R3): Dual-Pane Partition Threshold (724dp) & Folding Feature
    // =========================================================================

    @Test
    fun testR3_01_DualPaneActivatesAt724dpThresholdWithoutHinge() {
        val decision = AdaptivePartitionContract.evaluateDualPane(viewportWidthDp = 724f)
        assertTrue("Width >= 724dp must activate dual-pane display", decision.canShowBoth)
        assertEquals("Left list pane should default to 340dp at 724dp width", 340f, decision.leftPaneWidthDp, 0.01f)
        assertEquals("Spacer between panes must be 24dp", 24f, decision.horizontalSpacerSizeDp, 0.01f)
        assertEquals("Detail pane gets remaining 360dp", 360f, decision.rightPaneWidthDp, 0.01f)
    }

    @Test
    fun testR3_02_SinglePaneMaintainedBelow724dpThresholdWithoutHinge() {
        val decision = AdaptivePartitionContract.evaluateDualPane(viewportWidthDp = 720f)
        assertFalse("Width < 724dp without hinge must remain in single-pane mode", decision.canShowBoth)
        assertEquals("Single pane occupies full width", 720f, decision.leftPaneWidthDp, 0.01f)
        assertEquals("No spacer in single-pane mode", 0f, decision.horizontalSpacerSizeDp, 0.01f)
    }

    @Test
    fun testR3_03_SeparatingVerticalHingeOverridesWidthToDualPane() {
        val hinge = HingeBounds(leftDp = 280f, rightDp = 320f, isVertical = true, isSeparating = true)
        val decision = AdaptivePartitionContract.evaluateDualPane(viewportWidthDp = 600f, hinge = hinge)
        assertTrue("Separating vertical hinge must force dual-pane mode even below 724dp", decision.canShowBoth)
        assertTrue("Must indicate separation across hinge", decision.separatesAcrossHinge)
    }

    @Test
    fun testR3_04_LeftPanePartitionStrictlyBoundedByHingeLeft() {
        val hinge = HingeBounds(leftDp = 300f, rightDp = 340f, isVertical = true, isSeparating = true)
        val decision = AdaptivePartitionContract.evaluateDualPane(viewportWidthDp = 700f, hinge = hinge)
        assertEquals("Left pane width must strictly align with hinge left boundary", 300f, decision.leftPaneWidthDp, 0.01f)
    }

    @Test
    fun testR3_05_RightPanePartitionStrictlyBoundedByHingeRight() {
        val hinge = HingeBounds(leftDp = 300f, rightDp = 340f, isVertical = true, isSeparating = true)
        val decision = AdaptivePartitionContract.evaluateDualPane(viewportWidthDp = 700f, hinge = hinge)
        assertEquals("Right pane width must strictly stop at hinge right boundary (700 - 340 = 360)", 360f, decision.rightPaneWidthDp, 0.01f)
        assertEquals("Horizontal spacer is 0dp when partitioned by physical hinge", 0f, decision.horizontalSpacerSizeDp, 0.01f)
    }

    // =========================================================================
    // FEATURE 4 (R4): Landscape Short Height Layout & 48dp Touch Targets
    // =========================================================================

    @Test
    fun testR4_01_LandscapeCompactActivatesDualColumnWhenHeightUnder480AndWidthOver600() {
        val decision = LandscapeLayoutContract.evaluateLandscapeLayout(widthDp = 720f, heightDp = 400f)
        assertTrue("Landscape compact environment must activate isLandscapeCompact", decision.isLandscapeCompact)
        assertTrue("Landscape compact environment must activate dual column layout", decision.isDualColumn)
    }

    @Test
    fun testR4_02_DualColumnSeparatesTopBarModeSwitcherFromSelectorsAndKeypad() {
        val decision = LandscapeLayoutContract.evaluateLandscapeLayout(widthDp = 700f, heightDp = 420f)
        assertEquals("Left column contains TopBar, Connected Mode Switcher, and Amount",
            "TOP_BAR_AND_CONNECTED_MODE_SWITCHER_AND_AMOUNT", decision.leftColumnRole)
        assertEquals("Right column contains scrollable categories, accounts, and keypad",
            "SCROLLABLE_CATEGORIES_ACCOUNTS_AND_KEYPAD", decision.rightColumnRole)
    }

    @Test
    fun testR4_03_ConnectedButtonsModeSwitcherPreserves48dpMinimumHeight() {
        val decision = LandscapeLayoutContract.evaluateLandscapeLayout(widthDp = 640f, heightDp = 360f)
        assertEquals("Connected Buttons mode switcher must maintain minimum 48dp height",
            48f, decision.modeSwitcherMinHeightDp, 0.01f)
    }

    @Test
    fun testR4_04_ClearButtonPreserves48dpMinimumTouchTargetBounds() {
        val decision = LandscapeLayoutContract.evaluateLandscapeLayout(widthDp = 640f, heightDp = 360f)
        assertEquals("Clear button touch target size must be at least 48dp",
            48f, decision.clearButtonSizeDp, 0.01f)
    }

    @Test
    fun testR4_05_PortraitAndTallLayoutsRemainSingleColumn() {
        val portraitDecision = LandscapeLayoutContract.evaluateLandscapeLayout(widthDp = 400f, heightDp = 800f)
        assertFalse("Portrait layout must not be dual column", portraitDecision.isDualColumn)

        val tallLandscapeDecision = LandscapeLayoutContract.evaluateLandscapeLayout(widthDp = 800f, heightDp = 600f)
        assertFalse("Tall landscape (>= 480dp height) must not activate compact dual column", tallLandscapeDecision.isDualColumn)
    }

    // =========================================================================
    // FEATURE 5 (R5): State-Aware Back Navigation Priority Protection
    // =========================================================================

    @Test
    fun testR5_01_BackHandlerDisabledInDualPaneMode() {
        val state = BackNavigationState(
            canShowBoth = true,
            selectedId = 42L,
            hasActiveDialog = false,
            isImeVisible = false
        )
        assertFalse("BackHandler must be disabled in dual-pane mode (both panes visible)",
            BackNavigationContract.isBackHandlerEnabled(state))
        assertEquals("NOOP_DUAL_PANE_BOTH_VISIBLE", BackNavigationContract.resolveBackActionPriority(state))
    }

    @Test
    fun testR5_02_BackHandlerDisabledWhenNoDetailSelected() {
        val state = BackNavigationState(
            canShowBoth = false,
            selectedId = null,
            hasActiveDialog = false,
            isImeVisible = false
        )
        assertFalse("BackHandler must be disabled when no detail item is open",
            BackNavigationContract.isBackHandlerEnabled(state))
    }

    @Test
    fun testR5_03_BackHandlerEnabledInSinglePaneWithDetailSelectedAndNoOverlays() {
        val state = BackNavigationState(
            canShowBoth = false,
            selectedId = 101L,
            hasActiveDialog = false,
            isImeVisible = false
        )
        assertTrue("BackHandler must be enabled in single-pane with detail open and no overlays",
            BackNavigationContract.isBackHandlerEnabled(state))
        assertEquals("CONSUMED_BY_APP_NAVIGATE_TO_LIST", BackNavigationContract.resolveBackActionPriority(state))
    }

    @Test
    fun testR5_04_BackHandlerDisabledWhenImeKeyboardVisible() {
        val state = BackNavigationState(
            canShowBoth = false,
            selectedId = 101L,
            hasActiveDialog = false,
            isImeVisible = true // Software keyboard open
        )
        assertFalse("BackHandler must be disabled when IME is visible to let system close keyboard",
            BackNavigationContract.isBackHandlerEnabled(state))
        assertEquals("CONSUMED_BY_SYSTEM_IME", BackNavigationContract.resolveBackActionPriority(state))
    }

    @Test
    fun testR5_05_BackHandlerDisabledWhenActiveDialogPresent() {
        val state = BackNavigationState(
            canShowBoth = false,
            selectedId = 101L,
            hasActiveDialog = true, // Dialog open
            isImeVisible = false
        )
        assertFalse("BackHandler must be disabled when dialog is active to let dialog close first",
            BackNavigationContract.isBackHandlerEnabled(state))
        assertEquals("CONSUMED_BY_ACTIVE_DIALOG", BackNavigationContract.resolveBackActionPriority(state))
    }
}
