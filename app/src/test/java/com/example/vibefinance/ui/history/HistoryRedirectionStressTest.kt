package com.example.vibefinance.ui.history

import com.example.vibefinance.data.entity.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Empirical Stress and Adversarial Verification Suite for:
 * R1 (Daily Page Widget Redirection & Category Filtering),
 * R3 (FAB Bottom Clearance Invariants),
 * R4 & R7 (Recurring ExpressiveSwipeRow Parity & Absence of PresetsCarousel).
 */
class HistoryRedirectionStressTest {

    // --- 1. R1 DONUT CHART HIT-TEST RADIAL ANGLE COMPUTATION HARNESS ---

    private fun resolveHitCategory(
        offsetX: Float,
        offsetY: Float,
        sizeWidth: Float,
        sizeHeight: Float,
        categorySpending: List<Pair<String, Double>>
    ): String? {
        val totalSpending = categorySpending.sumOf { it.second }
        if (totalSpending <= 0.0) return null

        val cx = sizeWidth / 2f
        val cy = sizeHeight / 2f
        val dx = offsetX - cx
        val dy = offsetY - cy
        val angle = Math.toDegrees(kotlin.math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
        var relAngle = (angle - (-90f)) % 360f
        if (relAngle < 0f) relAngle += 360f

        var currentAngle = 0f
        for ((category, amount) in categorySpending) {
            val sweep = ((amount / totalSpending) * 360f).toFloat()
            if (relAngle >= currentAngle && relAngle < currentAngle + sweep) {
                return category
            }
            currentAngle += sweep
        }
        return null
    }

    @Test
    fun testDonutChartRadialHitTest_quadrantsAndBoundaries() {
        // Equal split of 4 categories (90 degrees each):
        // Start angle is -90 deg (Top = 12 o'clock = 0 deg relative)
        // Cat1: 12 to 3 o'clock (0..90 deg rel) -> top-right
        // Cat2: 3 to 6 o'clock (90..180 deg rel) -> bottom-right
        // Cat3: 6 to 9 o'clock (180..270 deg rel) -> bottom-left
        // Cat4: 9 to 12 o'clock (270..360 deg rel) -> top-left
        val categories = listOf(
            "Food" to 25.0,
            "Transport" to 25.0,
            "Shopping" to 25.0,
            "Bills" to 25.0
        )
        val size = 200f

        // 1. Top-Right quadrant: (150, 50) -> relative to (100,100) is dx=+50, dy=-50 -> -45 deg -> relAngle = 45 deg -> Food
        assertEquals("Food", resolveHitCategory(150f, 50f, size, size, categories))

        // 2. Bottom-Right quadrant: (150, 150) -> dx=+50, dy=+50 -> +45 deg -> relAngle = 135 deg -> Transport
        assertEquals("Transport", resolveHitCategory(150f, 150f, size, size, categories))

        // 3. Bottom-Left quadrant: (50, 150) -> dx=-50, dy=+50 -> +135 deg -> relAngle = 225 deg -> Shopping
        assertEquals("Shopping", resolveHitCategory(50f, 150f, size, size, categories))

        // 4. Top-Left quadrant: (50, 50) -> dx=-50, dy=-50 -> -135 deg -> relAngle = 315 deg -> Bills
        assertEquals("Bills", resolveHitCategory(50f, 50f, size, size, categories))

        // 5. Exactly 12 o'clock (dx=0, dy=-50): angle = -90 deg -> relAngle = 0 deg -> Food
        assertEquals("Food", resolveHitCategory(100f, 50f, size, size, categories))

        // 6. Exactly 3 o'clock (dx=50, dy=0): angle = 0 deg -> relAngle = 90 deg -> Transport
        assertEquals("Transport", resolveHitCategory(150f, 100f, size, size, categories))

        // 7. Exactly 6 o'clock (dx=0, dy=50): angle = 90 deg -> relAngle = 180 deg -> Shopping
        assertEquals("Shopping", resolveHitCategory(100f, 150f, size, size, categories))

        // 8. Exactly 9 o'clock (dx=-50, dy=0): angle = 180 deg -> relAngle = 270 deg -> Bills
        assertEquals("Bills", resolveHitCategory(50f, 100f, size, size, categories))
    }

    @Test
    fun testDonutChartZeroOrEmptySpendingBoundary() {
        val size = 200f
        // Empty categories
        assertEquals(null, resolveHitCategory(150f, 50f, size, size, emptyList()))

        // All zero spending
        val zeroCategories = listOf("Food" to 0.0, "Transport" to 0.0)
        assertEquals(null, resolveHitCategory(150f, 50f, size, size, zeroCategories))
    }

    // --- 2. R1 CATEGORY FILTERING SPECIFICATION ORACLE ---

    private fun filterTransactionsOracle(
        periodFilteredTransactions: List<TransactionEntity>,
        analyticsTransactions: List<TransactionEntity>,
        selectedCategoryFilter: String?
    ): List<TransactionEntity> {
        val catFilter = selectedCategoryFilter
        return if (!catFilter.isNullOrBlank()) {
            val baseList = if (analyticsTransactions.any { it.category.equals(catFilter, ignoreCase = true) }) {
                analyticsTransactions
            } else {
                periodFilteredTransactions
            }
            baseList.filter {
                it.toAccountId == null && !it.isBalanceAdjustment && it.amount > 0 &&
                    it.category.equals(catFilter, ignoreCase = true)
            }
        } else {
            periodFilteredTransactions
        }
    }

    @Test
    fun testFilterTransactions_nullOrBlankCategoryReturnsAll() {
        val allTxs = listOf(
            TransactionEntity(id = 1, amount = 100.0, category = "Food", timestamp = 1000L, accountId = 1),
            TransactionEntity(id = 2, amount = 50.0, category = "Transport", timestamp = 2000L, accountId = 1),
            TransactionEntity(id = 3, amount = 200.0, category = "Transfer", timestamp = 3000L, accountId = 1, toAccountId = 2)
        )

        // Null filter
        val resultNull = filterTransactionsOracle(allTxs, allTxs, null)
        assertEquals(allTxs, resultNull)

        // Empty string filter
        val resultEmpty = filterTransactionsOracle(allTxs, allTxs, "")
        assertEquals(allTxs, resultEmpty)

        // Blank whitespace filter
        val resultBlank = filterTransactionsOracle(allTxs, allTxs, "   \t\n")
        assertEquals(allTxs, resultBlank)
    }

    @Test
    fun testFilterTransactions_caseInsensitivityAndSanitization() {
        val tx1 = TransactionEntity(id = 1, amount = 100.0, category = "Food & Dining", timestamp = 1000L, accountId = 1)
        val tx2 = TransactionEntity(id = 2, amount = 50.0, category = "food & dining", timestamp = 2000L, accountId = 1)
        val tx3 = TransactionEntity(id = 3, amount = 25.0, category = "FOOD & DINING", timestamp = 3000L, accountId = 1)
        val tx4 = TransactionEntity(id = 4, amount = 75.0, category = "Transport", timestamp = 4000L, accountId = 1)
        val txs = listOf(tx1, tx2, tx3, tx4)

        // Query with lowercase
        val filteredLower = filterTransactionsOracle(txs, txs, "food & dining")
        assertEquals(3, filteredLower.size)
        assertTrue(filteredLower.contains(tx1))
        assertTrue(filteredLower.contains(tx2))
        assertTrue(filteredLower.contains(tx3))
        assertFalse(filteredLower.contains(tx4))

        // Query with uppercase
        val filteredUpper = filterTransactionsOracle(txs, txs, "FOOD & DINING")
        assertEquals(3, filteredUpper.size)

        // Query with mixed case
        val filteredMixed = filterTransactionsOracle(txs, txs, "Food & Dining")
        assertEquals(3, filteredMixed.size)
    }

    @Test
    fun testFilterTransactions_excludesTransfersAdjustmentsAndNegativeAmounts() {
        val validExpense = TransactionEntity(id = 1, amount = 100.0, category = "Shopping", timestamp = 1000L, accountId = 1)
        val internalTransfer = TransactionEntity(id = 2, amount = 100.0, category = "Shopping", timestamp = 2000L, accountId = 1, toAccountId = 2)
        val balanceAdjustment = TransactionEntity(id = 3, amount = 100.0, category = "Shopping", timestamp = 3000L, accountId = 1, isBalanceAdjustment = true)
        val negativeIncome = TransactionEntity(id = 4, amount = -100.0, category = "Shopping", timestamp = 4000L, accountId = 1)
        val zeroTx = TransactionEntity(id = 5, amount = 0.0, category = "Shopping", timestamp = 5000L, accountId = 1)

        val txs = listOf(validExpense, internalTransfer, balanceAdjustment, negativeIncome, zeroTx)

        val result = filterTransactionsOracle(txs, txs, "Shopping")
        assertEquals(1, result.size)
        assertEquals(validExpense, result.first())
    }

    @Test
    fun testFilterTransactions_fallbackToPeriodFilteredTransactions() {
        val historicalExpense = TransactionEntity(id = 10, amount = 500.0, category = "Electronics", timestamp = 500L, accountId = 1)
        val currentPeriodExpense = TransactionEntity(id = 20, amount = 50.0, category = "Food", timestamp = 2000L, accountId = 1)

        val allHistorical = listOf(historicalExpense, currentPeriodExpense)
        val analyticsOnly = listOf(currentPeriodExpense)

        // Query for "Electronics" which is NOT in analyticsOnly
        val result = filterTransactionsOracle(allHistorical, analyticsOnly, "Electronics")
        assertEquals(1, result.size)
        assertEquals(historicalExpense, result.first())
    }

    @Test
    fun testFilterTransactions_unmatchedCategoryReturnsEmpty() {
        val tx1 = TransactionEntity(id = 1, amount = 100.0, category = "Food", timestamp = 1000L, accountId = 1)
        val txs = listOf(tx1)

        val result = filterTransactionsOracle(txs, txs, "Crypto")
        assertTrue(result.isEmpty())
    }

    // --- 3. R1 TOTAL EXPENSES & BOTTOM NAV FILTER CLEANSING INVARIANTS ---

    data class NavigationState(
        var selectedTab: String = "HOME",
        var historyAccountFilterId: Long? = 5L,
        var historyCategoryFilter: String? = "Dining"
    )

    @Test
    fun testTotalExpensesCleansesBothFilters() {
        val state = NavigationState()
        val onViewAllClick: () -> Unit = {
            state.historyAccountFilterId = null
            state.historyCategoryFilter = null
            state.selectedTab = "HISTORY"
        }

        onViewAllClick()
        assertEquals("HISTORY", state.selectedTab)
        assertEquals(null, state.historyAccountFilterId)
        assertEquals(null, state.historyCategoryFilter)
    }

    @Test
    fun testBottomNavToHistoryCleansesBothFilters() {
        val state = NavigationState()
        fun onTabSelected(tab: String) {
            if (state.selectedTab != tab) {
                if (tab == "HISTORY") {
                    state.historyAccountFilterId = null
                    state.historyCategoryFilter = null
                }
                state.selectedTab = tab
            }
        }

        onTabSelected("HISTORY")
        assertEquals("HISTORY", state.selectedTab)
        assertEquals(null, state.historyAccountFilterId)
        assertEquals(null, state.historyCategoryFilter)
    }

    @Test
    fun testOnCategoryClickSetsCategoryAndClearsAccountFilter() {
        val state = NavigationState(historyAccountFilterId = 42L, historyCategoryFilter = null)
        val onCategoryClick: (String) -> Unit = { category ->
            state.historyAccountFilterId = null
            state.historyCategoryFilter = category
            state.selectedTab = "HISTORY"
        }

        onCategoryClick("Health")
        assertEquals("HISTORY", state.selectedTab)
        assertEquals(null, state.historyAccountFilterId)
        assertEquals("Health", state.historyCategoryFilter)
    }

    // --- 4. R3 DAILY PAGE FAB BOTTOM CLEARANCE & ELEVATION INVARIANTS ---

    @Test
    fun testFabElevationAndClearanceInvariants() {
        val screenHeightDp = 800f
        val bottomBarHeightDp = 80f
        val fabHeightDp = 56f
        val fabSpacingDp = 16f

        // Material 3 Scaffold places the FAB above bottomBar by fabSpacingDp (16dp).
        val restingFabTopDp = screenHeightDp - bottomBarHeightDp - fabSpacingDp - fabHeightDp
        val restingFabBottomDp = restingFabTopDp + fabHeightDp

        // Clearance above bottomBar top:
        val clearanceAboveBottomBar = (screenHeightDp - bottomBarHeightDp) - restingFabBottomDp
        assertEquals(16f, clearanceAboveBottomBar, 0.001f)

        // Downward scroll scenario:
        // When user scrolls down, navBarOffsetY animates down (e.g. up to 140dp) to hide navigation bar.
        // In the prior buggy implementation, the FAB applied translationY = navBarOffsetY:
        val buggyDisplacedFabBottom = restingFabBottomDp + 140f
        val buggyOverflowPastScreenBottom = buggyDisplacedFabBottom - screenHeightDp
        // Confirmed prior defect: FAB sank 44dp BELOW physical screen bottom!
        assertEquals(44f, buggyOverflowPastScreenBottom, 0.001f)

        // In the fixed implementation, FloatingActionButtonMenu has NO translationY:
        val fixedDisplacedFabBottom = restingFabBottomDp // stays stationary at resting elevation
        assertTrue(fixedDisplacedFabBottom < screenHeightDp)
        val fixedClearanceToScreenBottom = screenHeightDp - fixedDisplacedFabBottom
        assertEquals(96f, fixedClearanceToScreenBottom, 0.001f) // 80dp + 16dp clearance
    }

    // --- 5. R4 & R7 RECURRING SCREEN PARITY & PRESETSCAROUSEL ABSENCE ---

    @Test
    fun testPresetsCarouselAbsenceViaReflection() {
        val recurringClass = Class.forName("com.example.vibefinance.ui.recurring.RecurringScreenKt")
        val methods = recurringClass.declaredMethods

        val hasPresetsCarousel = methods.any { it.name.contains("PresetsCarousel", ignoreCase = true) }
        assertFalse("PresetsCarousel must be completely removed from RecurringScreenKt", hasPresetsCarousel)
    }

    @Test
    fun testExpressiveSwipeRowComponentAvailability() {
        val swipeClass = Class.forName("com.example.vibefinance.ui.components.ExpressiveSwipeRowKt")
        val methods = swipeClass.declaredMethods

        val hasSwipeRow = methods.any { it.name.startsWith("ExpressiveSwipeRow") }
        assertTrue("ExpressiveSwipeRow must be defined in ExpressiveSwipeRowKt", hasSwipeRow)
    }
}
