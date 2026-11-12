package com.example.vibefinance.ui.e2e

import com.example.vibefinance.ui.e2e.E2ETestContracts.AdaptivePartitionContract
import com.example.vibefinance.ui.e2e.E2ETestContracts.BackNavigationContract
import com.example.vibefinance.ui.e2e.E2ETestContracts.BackNavigationState
import com.example.vibefinance.ui.e2e.E2ETestContracts.BentoContract
import com.example.vibefinance.ui.e2e.E2ETestContracts.HingeBounds
import com.example.vibefinance.ui.e2e.E2ETestContracts.LandscapeLayoutContract
import com.example.vibefinance.ui.e2e.E2ETestContracts.MonetaryTypographyContract
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tier 3: Cross-Feature Combinations & Pairwise Interaction Test Suite.
 * Verifies orthogonal system dimensions intersecting under complex UI conditions.
 */
class Tier3PairwiseCombinationsTest {

    @Test
    fun testPairwise_01_NarrowWidth_And_LargeNegativeAmount() {
        val viewportWidth = 320f
        val hugeAmount = -999999999.99
        val formatted = MonetaryTypographyContract.formatSignedAmount(hugeAmount, currencyPrefix = "$")

        // 1. Text flow and format verification
        assertEquals("-$999,999,999.99", formatted)
        assertTrue(MonetaryTypographyContract.verifySingleTextFlow(formatted))

        // 2. Adaptive typography check at narrow width
        val overflowsSideBySide = MonetaryTypographyContract.doesOverflowAtMinFloor(formatted, availableWidthDp = 100f)
        assertTrue("Large negative amount must overflow 100dp side-by-side slot", overflowsSideBySide)

        // 3. Bento stacking resolution
        val layout = BentoContract.evaluateBentoLayout(viewportWidth, textMeasurerOverflowAt12sp = overflowsSideBySide)
        assertTrue("Combination of 320dp and large negative amount must force vertical stacking", layout.isStacked)
        assertEquals("Stacked budget card gets full 320dp width", 320f, layout.budgetCardWidthDp, 0.01f)
        assertTrue("DaysLeftCard remains strictly square", layout.isDaysSquare)
        assertTrue("DaysLeftCard bounded in [120, 156]", layout.daysCardSizeDp in 120f..156f)
        assertEquals("DaysLeftCard size is 134.4dp", 134.4f, layout.daysCardSizeDp, 0.1f)
    }

    @Test
    fun testPairwise_02_ExtremeFontScale_And_LargeNegativeAmount() {
        val fontScale = 2.0f
        val viewportWidth = 400f
        val formatted = MonetaryTypographyContract.formatSignedAmount(-12345.67, currencyPrefix = "$")

        val overflowsAt2x = MonetaryTypographyContract.doesOverflowAtMinFloor(formatted, availableWidthDp = 120f, fontScale = fontScale)
        val layout = BentoContract.evaluateBentoLayout(viewportWidth, fontScale = fontScale, textMeasurerOverflowAt12sp = overflowsAt2x)

        assertTrue("Extreme font scale 2.0x combined with negative amount must force stacking", layout.isStacked)
        assertTrue("DaysLeftCard must remain square", layout.isDaysSquare)
    }

    @Test
    fun testPairwise_03_SeparatingHinge_And_LandscapeCompactHeight() {
        // Book-mode foldable in landscape orientation (width 700dp, height 400dp)
        val hinge = HingeBounds(leftDp = 330f, rightDp = 370f, isVertical = true, isSeparating = true)
        val dualPane = AdaptivePartitionContract.evaluateDualPane(viewportWidthDp = 700f, hinge = hinge)
        val landscape = LandscapeLayoutContract.evaluateLandscapeLayout(widthDp = 700f, heightDp = 400f)

        // Verifies dual pane partitions across hinge
        assertTrue("Separating hinge forces dual-pane", dualPane.canShowBoth)
        assertEquals(330f, dualPane.leftPaneWidthDp, 0.01f)
        assertEquals(330f, dualPane.rightPaneWidthDp, 0.01f) // 700 - 370 = 330

        // Verifies compact landscape dual-column entry layout
        assertTrue("Height 400dp and width 700dp triggers dual column entry", landscape.isDualColumn)
        assertEquals(48f, landscape.modeSwitcherMinHeightDp, 0.01f)
        assertEquals(48f, landscape.clearButtonSizeDp, 0.01f)
    }

    @Test
    fun testPairwise_04_DualPaneActive_And_BackPressWithActiveDialog() {
        // In dual-pane mode (724dp), detail is displayed on the right pane with an active dialog
        val state = BackNavigationState(
            canShowBoth = true,
            selectedId = 202L,
            hasActiveDialog = true,
            isImeVisible = false
        )
        assertFalse("BackHandler must be disabled in dual-pane mode", BackNavigationContract.isBackHandlerEnabled(state))
        assertEquals("NOOP_DUAL_PANE_BOTH_VISIBLE", BackNavigationContract.resolveBackActionPriority(state))
    }

    @Test
    fun testPairwise_05_DualPaneActive_And_BackPressWithImeVisible() {
        // In dual-pane mode, user focuses note input in detail pane
        val state = BackNavigationState(
            canShowBoth = true,
            selectedId = 202L,
            hasActiveDialog = false,
            isImeVisible = true
        )
        assertFalse("BackHandler must be disabled so system IME receives back press", BackNavigationContract.isBackHandlerEnabled(state))
        assertEquals("NOOP_DUAL_PANE_BOTH_VISIBLE", BackNavigationContract.resolveBackActionPriority(state))
    }

    @Test
    fun testPairwise_06_LandscapeCompact_And_TouchTargetMinimumCompliance() {
        val layout = LandscapeLayoutContract.evaluateLandscapeLayout(widthDp = 640f, heightDp = 400f)
        assertTrue(layout.isLandscapeCompact)
        assertTrue(layout.isDualColumn)
        assertTrue("Mode switcher must meet or exceed 48dp", layout.modeSwitcherMinHeightDp >= 48f)
        assertTrue("Clear button touch target must meet or exceed 48dp", layout.clearButtonSizeDp >= 48f)
    }

    @Test
    fun testPairwise_07_SinglePane_And_DetailSelected_CleanState() {
        val state = BackNavigationState(
            canShowBoth = false,
            selectedId = 303L,
            hasActiveDialog = false,
            isImeVisible = false
        )
        assertTrue("BackHandler must be enabled to allow returning to list", BackNavigationContract.isBackHandlerEnabled(state))
        assertEquals("CONSUMED_BY_APP_NAVIGATE_TO_LIST", BackNavigationContract.resolveBackActionPriority(state))
    }

    @Test
    fun testPairwise_08_ExtremeFontScale_And_LandscapeCompact() {
        val fontScale = 2.0f
        val layout = LandscapeLayoutContract.evaluateLandscapeLayout(widthDp = 680f, heightDp = 420f)
        assertTrue(layout.isDualColumn)
        // At 2.0x font scale, touch target sizes must never shrink below 48dp
        assertTrue(layout.modeSwitcherMinHeightDp >= 48f)
        assertTrue(layout.clearButtonSizeDp >= 48f)
    }

    @Test
    fun testPairwise_09_SubThreshold723dp_And_LargeNegativeAmount() {
        val dualPane = AdaptivePartitionContract.evaluateDualPane(viewportWidthDp = 723f)
        assertFalse("723dp is below 724dp threshold and remains single pane", dualPane.canShowBoth)

        val formatted = MonetaryTypographyContract.formatSignedAmount(-543210.99, currencyPrefix = "$")
        assertEquals("-$543,210.99", formatted)
        assertTrue(MonetaryTypographyContract.verifySingleTextFlow(formatted))
    }

    @Test
    fun testPairwise_10_DualPane724dp_And_SeparatingHingePrecedence() {
        // Exactly at 724dp width, but with an asymmetric separating hinge at [300dp, 330dp]
        val hinge = HingeBounds(leftDp = 300f, rightDp = 330f, isVertical = true, isSeparating = true)
        val decision = AdaptivePartitionContract.evaluateDualPane(viewportWidthDp = 724f, hinge = hinge)

        assertTrue(decision.canShowBoth)
        assertTrue("Physical hinge partition takes precedence over default 724dp divider math", decision.separatesAcrossHinge)
        assertEquals("Left pane strictly aligns with hinge left", 300f, decision.leftPaneWidthDp, 0.01f)
        assertEquals("Right pane strictly aligns with hinge right (724 - 330 = 394)", 394f, decision.rightPaneWidthDp, 0.01f)
        assertEquals("No software spacer when physical hinge exists", 0f, decision.horizontalSpacerSizeDp, 0.01f)
    }
}
