package com.example.vibefinance.ui.e2e

import java.util.Locale

/**
 * Authoritative Specification Contracts and Opaque-Box Evaluators for the 5 Core Hard Override UI/UX Refinements:
 * - R1: DaysLeftCard 1:1 Aspect Ratio & Bento Responsive Stacking
 * - R2: Large Negative Monetary Amounts Single Line & Stable Baseline (12sp Floor)
 * - R3: Dual-Pane Partition Threshold (724dp) & Separating Folding Feature Support
 * - R4: Landscape Short Height Layout (<480dp / >=600dp) & 48dp Minimum Touch Target Protection
 * - R5: State-Aware Back Navigation Priority Protection
 */
object E2ETestContracts {

    // =========================================================================
    // R1: Bento Responsive Stacking & DaysLeftCard 1:1 Aspect Ratio
    // =========================================================================

    data class BentoLayoutDecision(
        val isStacked: Boolean,
        val budgetCardWidthDp: Float,
        val daysCardSizeDp: Float,
        val isDaysSquare: Boolean,
        val daysCardClampedMin: Boolean,
        val daysCardClampedMax: Boolean
    )

    object BentoContract {
        const val NARROW_WIDTH_THRESHOLD_DP = 340f
        const val DAYS_CARD_MIN_SIZE_DP = 120f
        const val DAYS_CARD_MAX_SIZE_DP = 156f
        const val BENTO_SPACING_DP = 12f

        fun evaluateBentoLayout(
            viewportWidthDp: Float,
            fontScale: Float = 1.0f,
            textMeasurerOverflowAt12sp: Boolean = false
        ): BentoLayoutDecision {
            val needsStacking = viewportWidthDp < NARROW_WIDTH_THRESHOLD_DP ||
                    fontScale > 1.4f ||
                    textMeasurerOverflowAt12sp

            return if (needsStacking) {
                val rawDaysSize = viewportWidthDp * 0.42f
                val clampedDaysSize = minOf(rawDaysSize, DAYS_CARD_MAX_SIZE_DP).coerceAtLeast(DAYS_CARD_MIN_SIZE_DP)
                BentoLayoutDecision(
                    isStacked = true,
                    budgetCardWidthDp = viewportWidthDp,
                    daysCardSizeDp = clampedDaysSize,
                    isDaysSquare = true, // Aspect ratio strictly 1f
                    daysCardClampedMin = rawDaysSize <= DAYS_CARD_MIN_SIZE_DP,
                    daysCardClampedMax = rawDaysSize >= DAYS_CARD_MAX_SIZE_DP
                )
            } else {
                val availableWidth = viewportWidthDp - BENTO_SPACING_DP
                val daysSide = minOf(availableWidth * 0.4f, DAYS_CARD_MAX_SIZE_DP).coerceAtLeast(110f)
                val budgetCardWidth = availableWidth - daysSide
                BentoLayoutDecision(
                    isStacked = false,
                    budgetCardWidthDp = budgetCardWidth,
                    daysCardSizeDp = daysSide,
                    isDaysSquare = true,
                    daysCardClampedMin = false,
                    daysCardClampedMax = (availableWidth * 0.4f) >= DAYS_CARD_MAX_SIZE_DP
                )
            }
        }
    }

    // =========================================================================
    // R2: Monetary Typography & TextMeasurer Adaptive Sizing
    // =========================================================================

    object MonetaryTypographyContract {
        const val MIN_FONT_SIZE_FLOOR_SP = 12f
        const val BASE_FONT_SIZE_SP = 22f

        /**
         * Standard signed financial format:
         * Positive: "$1,234.56"
         * Negative: "-$1,234.56" (Sign precedes currency prefix)
         */
        fun formatSignedAmount(amount: Double, currencyPrefix: String = "$"): String {
            val isNegative = amount < 0.0
            val absAmount = Math.abs(amount)
            val formatted = String.format(Locale.US, "%,.2f", absAmount)
            return if (isNegative) "-$currencyPrefix$formatted" else "$currencyPrefix$formatted"
        }

        /**
         * Verifies that the negative sign, currency symbol, and amount digits
         * are unified in a single continuous string without separators or split texts.
         */
        fun verifySingleTextFlow(formattedAmount: String): Boolean {
            // Must not contain separate leading spaces or broken sign fragments
            val regex = Regex("""^-[A-Za-z$€£¥₹₩]+[0-9,]+\.[0-9]{2}$|^[A-Za-z$€£¥₹₩]+[0-9,]+\.[0-9]{2}$""")
            return regex.matches(formattedAmount)
        }

        /**
         * Emulates binary search font scaling using text length, fontScale, and available container width.
         */
        fun computeAdaptiveFontSizeSp(
            formattedAmount: String,
            availableWidthDp: Float,
            fontScale: Float = 1.0f,
            baseFontSizeSp: Float = BASE_FONT_SIZE_SP,
            minFloorSp: Float = MIN_FONT_SIZE_FLOOR_SP
        ): Float {
            // Approximate glyph width ratio in sp for bold tabular financial numbers: ~0.55 * fontSizeSp * fontScale
            val charCount = formattedAmount.length
            var low = minFloorSp
            var high = baseFontSizeSp
            var bestFit = minFloorSp

            for (i in 0 until 12) { // 12-step binary search
                val mid = (low + high) / 2f
                val estimatedWidthDp = charCount * (mid * 0.58f * fontScale)
                if (estimatedWidthDp <= availableWidthDp) {
                    bestFit = mid
                    low = mid
                } else {
                    high = mid
                }
            }
            return bestFit.coerceAtLeast(minFloorSp)
        }

        fun doesOverflowAtMinFloor(
            formattedAmount: String,
            availableWidthDp: Float,
            fontScale: Float = 1.0f,
            minFloorSp: Float = MIN_FONT_SIZE_FLOOR_SP
        ): Boolean {
            val estimatedWidthDp = formattedAmount.length * (minFloorSp * 0.58f * fontScale)
            return estimatedWidthDp > availableWidthDp
        }
    }

    // =========================================================================
    // R3: Dual-Pane Partition Threshold & Folding Hinge Support
    // =========================================================================

    data class HingeBounds(
        val leftDp: Float,
        val rightDp: Float,
        val isVertical: Boolean,
        val isSeparating: Boolean
    )

    data class DualPaneDecision(
        val canShowBoth: Boolean,
        val leftPaneWidthDp: Float,
        val rightPaneWidthDp: Float,
        val horizontalSpacerSizeDp: Float,
        val separatesAcrossHinge: Boolean
    )

    object AdaptivePartitionContract {
        const val DUAL_PANE_THRESHOLD_DP = 724f // 340dp list + 360dp detail + 24dp gap

        fun evaluateDualPane(
            viewportWidthDp: Float,
            hinge: HingeBounds? = null
        ): DualPaneDecision {
            val hasSeparatingVerticalHinge = hinge != null && hinge.isVertical && hinge.isSeparating

            return if (hasSeparatingVerticalHinge) {
                val leftWidth = hinge!!.leftDp
                val rightWidth = (viewportWidthDp - hinge.rightDp).coerceAtLeast(0f)
                DualPaneDecision(
                    canShowBoth = true,
                    leftPaneWidthDp = leftWidth,
                    rightPaneWidthDp = rightWidth,
                    horizontalSpacerSizeDp = 0f, // Hinge itself acts as boundary spacer
                    separatesAcrossHinge = true
                )
            } else {
                val canShowBoth = viewportWidthDp >= DUAL_PANE_THRESHOLD_DP
                val leftWidth = if (canShowBoth) {
                    if (viewportWidthDp >= 800f) 400f else 340f
                } else {
                    viewportWidthDp
                }
                val spacer = if (canShowBoth) 24f else 0f
                val rightWidth = if (canShowBoth) (viewportWidthDp - leftWidth - spacer).coerceAtLeast(0f) else 0f
                DualPaneDecision(
                    canShowBoth = canShowBoth,
                    leftPaneWidthDp = leftWidth,
                    rightPaneWidthDp = rightWidth,
                    horizontalSpacerSizeDp = spacer,
                    separatesAcrossHinge = false
                )
            }
        }
    }

    // =========================================================================
    // R4: Landscape Short Height & 48dp Touch Target Protection
    // =========================================================================

    data class LandscapeLayoutDecision(
        val isLandscapeCompact: Boolean,
        val isDualColumn: Boolean,
        val leftColumnRole: String,
        val rightColumnRole: String,
        val modeSwitcherMinHeightDp: Float,
        val clearButtonSizeDp: Float
    )

    object LandscapeLayoutContract {
        const val COMPACT_HEIGHT_THRESHOLD_DP = 480f
        const val COMPACT_WIDTH_MIN_DP = 600f
        const val MIN_TOUCH_TARGET_DP = 48f

        fun evaluateLandscapeLayout(
            widthDp: Float,
            heightDp: Float
        ): LandscapeLayoutDecision {
            val isCompactLandscape = heightDp < COMPACT_HEIGHT_THRESHOLD_DP && widthDp >= COMPACT_WIDTH_MIN_DP
            return if (isCompactLandscape) {
                LandscapeLayoutDecision(
                    isLandscapeCompact = true,
                    isDualColumn = true,
                    leftColumnRole = "TOP_BAR_AND_CONNECTED_MODE_SWITCHER_AND_AMOUNT",
                    rightColumnRole = "SCROLLABLE_CATEGORIES_ACCOUNTS_AND_KEYPAD",
                    modeSwitcherMinHeightDp = MIN_TOUCH_TARGET_DP,
                    clearButtonSizeDp = MIN_TOUCH_TARGET_DP
                )
            } else {
                LandscapeLayoutDecision(
                    isLandscapeCompact = false,
                    isDualColumn = false,
                    leftColumnRole = "SINGLE_COLUMN_HERO",
                    rightColumnRole = "NONE",
                    modeSwitcherMinHeightDp = MIN_TOUCH_TARGET_DP,
                    clearButtonSizeDp = MIN_TOUCH_TARGET_DP
                )
            }
        }
    }

    // =========================================================================
    // R5: State-Aware Back Navigation Priority
    // =========================================================================

    data class BackNavigationState(
        val canShowBoth: Boolean,
        val selectedId: Long?,
        val hasActiveDialog: Boolean,
        val isImeVisible: Boolean
    )

    object BackNavigationContract {
        /**
         * BackHandler.enabled must strictly be:
         * !canShowBoth && selectedId != null && !hasActiveDialog && !isImeVisible
         */
        fun isBackHandlerEnabled(state: BackNavigationState): Boolean {
            return !state.canShowBoth &&
                    (state.selectedId != null) &&
                    !state.hasActiveDialog &&
                    !state.isImeVisible
        }

        fun resolveBackActionPriority(state: BackNavigationState): String {
            return when {
                state.canShowBoth -> "NOOP_DUAL_PANE_BOTH_VISIBLE"
                state.isImeVisible -> "CONSUMED_BY_SYSTEM_IME"
                state.hasActiveDialog -> "CONSUMED_BY_ACTIVE_DIALOG"
                state.selectedId != null -> "CONSUMED_BY_APP_NAVIGATE_TO_LIST"
                else -> "CONSUMED_BY_SYSTEM_APP_EXIT_OR_PREV_SCREEN"
            }
        }
    }
}
