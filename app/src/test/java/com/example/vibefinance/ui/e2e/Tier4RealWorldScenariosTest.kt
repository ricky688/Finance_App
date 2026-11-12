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
 * Tier 4: Real-World Application Workload Scenarios Test Suite.
 * Simulates complete end-to-end user workflows and state transitions under authentic conditions.
 */
class Tier4RealWorldScenariosTest {

    @Test
    fun testScenario_01_BudgetOverdraftOnCompactPhone() {
        // Step 1: User on compact phone (360dp width) with moderate accessibility font scale (1.25x)
        val viewportWidth = 360f
        val fontScale = 1.25f
        val remainingBudget = -1450.50
        val totalBudget = 3000.00
        val daysLeft = 12
        val totalDays = 30

        // Step 2: Format overdraft amount
        val formattedRemaining = MonetaryTypographyContract.formatSignedAmount(remainingBudget, "$")
        assertEquals("-$1,450.50", formattedRemaining)
        assertTrue(MonetaryTypographyContract.verifySingleTextFlow(formattedRemaining))

        // Step 3: Check font scaling fits in card
        val availableWidth = 140f
        val calculatedFontSp = MonetaryTypographyContract.computeAdaptiveFontSizeSp(
            formattedRemaining, availableWidth, fontScale = fontScale
        )
        assertTrue("Font size must scale gracefully", calculatedFontSp in 12f..22f)

        // Step 4: DaysLeftCard progress and geometry
        val progress = calculateDaysLeftProgress(daysLeft, totalDays)
        assertEquals(0.6f, progress, 0.01f) // (30 - 12) / 30 = 0.60

        // Step 5: Bento layout resolution
        val bento = BentoContract.evaluateBentoLayout(viewportWidth, fontScale = fontScale)
        // With 360dp and 1.25x font scale, stays in Row if amount fits
        assertFalse("Moderate font scale fits side-by-side", bento.isStacked)
        assertTrue("DaysLeftCard strictly square", bento.isDaysSquare)
    }

    @Test
    fun testScenario_02_FoldableBookModeUnfoldTransition() {
        // Step 1: User starts on folded cover screen (380dp) in History tab, opens detail #1001
        var state = BackNavigationState(
            canShowBoth = false,
            selectedId = 1001L,
            hasActiveDialog = false,
            isImeVisible = false
        )
        assertTrue("BackHandler enabled on folded screen with detail open",
            BackNavigationContract.isBackHandlerEnabled(state))
        assertEquals("CONSUMED_BY_APP_NAVIGATE_TO_LIST", BackNavigationContract.resolveBackActionPriority(state))

        // Step 2: User unfolds device into Book mode (separating hinge at [330dp, 370dp], total width 700dp)
        val hinge = HingeBounds(leftDp = 330f, rightDp = 370f, isVertical = true, isSeparating = true)
        val dualPane = AdaptivePartitionContract.evaluateDualPane(viewportWidthDp = 700f, hinge = hinge)

        assertTrue("Foldable book mode triggers dual pane", dualPane.canShowBoth)
        assertEquals(330f, dualPane.leftPaneWidthDp, 0.01f)
        assertEquals(330f, dualPane.rightPaneWidthDp, 0.01f)
        assertEquals(0f, dualPane.horizontalSpacerSizeDp, 0.01f)

        // Step 3: BackHandler state automatically re-evaluates with canShowBoth = true
        state = state.copy(canShowBoth = true)
        assertFalse("BackHandler becomes disabled because both list and detail are visible concurrently",
            BackNavigationContract.isBackHandlerEnabled(state))
        assertEquals("NOOP_DUAL_PANE_BOTH_VISIBLE", BackNavigationContract.resolveBackActionPriority(state))
    }

    @Test
    fun testScenario_03_TransactionDetailNoteEditingAndBackKeyDismissal() {
        // Step 1: User in single-pane mode opens detail record #4001
        var state = BackNavigationState(
            canShowBoth = false,
            selectedId = 4001L,
            hasActiveDialog = false,
            isImeVisible = false
        )
        assertTrue("Step 1: BackHandler is enabled to exit detail", BackNavigationContract.isBackHandlerEnabled(state))

        // Step 2: User taps Note input field; IME software keyboard appears
        state = state.copy(isImeVisible = true)
        assertFalse("Step 2: BackHandler disabled while IME is open", BackNavigationContract.isBackHandlerEnabled(state))
        assertEquals("CONSUMED_BY_SYSTEM_IME", BackNavigationContract.resolveBackActionPriority(state))

        // Step 3: First back press consumed by system IME -> IME closes
        state = state.copy(isImeVisible = false)
        assertTrue("Step 3: BackHandler restored to enabled after IME dismissal", BackNavigationContract.isBackHandlerEnabled(state))
        assertEquals("CONSUMED_BY_APP_NAVIGATE_TO_LIST", BackNavigationContract.resolveBackActionPriority(state))

        // Step 4: Second back press navigates from detail back to list
        state = state.copy(selectedId = null)
        assertFalse("Step 4: BackHandler disabled after returning to list", BackNavigationContract.isBackHandlerEnabled(state))
        assertEquals("CONSUMED_BY_SYSTEM_APP_EXIT_OR_PREV_SCREEN", BackNavigationContract.resolveBackActionPriority(state))
    }

    @Test
    fun testScenario_04_InVehicleLandscapeQuickExpenseEntry() {
        // Step 1: Car mount dock orientation: 640dp width, 400dp height
        val layout = LandscapeLayoutContract.evaluateLandscapeLayout(widthDp = 640f, heightDp = 400f)
        assertTrue("Activates compact dual column layout", layout.isDualColumn)

        // Step 2: Mode switcher buttons retain 48dp height
        assertEquals(48f, layout.modeSwitcherMinHeightDp, 0.01f)

        // Step 3: Amount clear button touch target is at least 48dp
        assertEquals(48f, layout.clearButtonSizeDp, 0.01f)

        // Step 4: Validate typed amount formatting
        val formatted = MonetaryTypographyContract.formatSignedAmount(50.00, "$")
        assertEquals("$50.00", formatted)
        assertTrue(MonetaryTypographyContract.verifySingleTextFlow(formatted))
    }

    @Test
    fun testScenario_05_ExtremeFinancialVarianceAndAccessibilityStress() {
        // Step 1: Deep debt user on ultra-narrow phone (320dp) with large accessibility font (1.75x)
        val viewportWidth = 320f
        val fontScale = 1.75f
        val extremeNegativeDebt = -12450890.00

        val formattedDebt = MonetaryTypographyContract.formatSignedAmount(extremeNegativeDebt, "$")
        assertEquals("-$12,450,890.00", formattedDebt)
        assertTrue(MonetaryTypographyContract.verifySingleTextFlow(formattedDebt))

        // Step 2: Check overflow at 12sp floor
        val overflows = MonetaryTypographyContract.doesOverflowAtMinFloor(
            formattedDebt, availableWidthDp = 100f, fontScale = fontScale
        )
        assertTrue(overflows)

        // Step 3: Bento layout resolution
        val bento = BentoContract.evaluateBentoLayout(viewportWidth, fontScale = fontScale, textMeasurerOverflowAt12sp = overflows)
        assertTrue("Must switch to stacked Column layout", bento.isStacked)
        assertEquals("Full width granted to budget card", 320f, bento.budgetCardWidthDp, 0.01f)

        // Step 4: DaysLeftCard clamped and square
        assertTrue("DaysLeftCard must be strictly square", bento.isDaysSquare)
        // 320 * 0.42 = 134.4dp
        assertEquals(134.4f, bento.daysCardSizeDp, 0.1f)
        assertTrue("Size is within [120, 156] bounds", bento.daysCardSizeDp in 120f..156f)
    }
}
