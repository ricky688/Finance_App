@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.nestedscroll.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.data.entity.*
import com.example.vibefinance.data.repository.DailyBudgetInfo
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.history.HistoryScreen
import com.example.vibefinance.ui.preferences.*
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs

/** Header Select, long press, scrolling and close explored with ARTEMIS/ADB on Waydroid first. */
@RunWith(AndroidJUnit4::class)
class HistorySelectionMotionTest {
    @get:Rule val rule = createComposeRule()
    private lateinit var list: LazyListState
    private lateinit var scope: CoroutineScope
    private lateinit var barState: PageTopBarState
    private var density = 1f
    private var backdrop = Color.Unspecified
    private var mutations = 0
    private fun show(motion: MotionLevel = MotionLevel.FULL, dark: Boolean = false, wideLarge: Boolean = false, compactLarge: Boolean = false) {
        rule.mainClock.autoAdvance = false
        val now = System.currentTimeMillis() - 1000
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()
        val finance = FinanceUiState(isLoading = false,
            accounts = listOf(AccountEntity(1, "Bank", type = AccountType.BANK, balance = 500.0, icon = "bank")),
            transactions = (1L..40L).map { TransactionEntity(it, 10.0, "Other", now, 1, description = "Fixture $it") },
            budgetInfo = DailyBudgetInfo(2500.0, 400.0, 2100.0, 100.0, 100.0, 20,
                today.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli(),
                today.withDayOfMonth(today.lengthOfMonth()).atStartOfDay(zone).toInstant().toEpochMilli()))
        rule.setContent {
            val original = LocalDensity.current
            val fixtureDensity = Density(if (wideLarge) original.density * .5f else original.density,
                if (wideLarge || compactLarge) 1.7f else original.fontScale)
            density = fixtureDensity.density
            CompositionLocalProvider(LocalDensity provides fixtureDensity,
                LocalExperience provides ExperiencePreferences(motion = motion)) {
                list = rememberLazyListState()
                scope = rememberCoroutineScope()
                barState = rememberPageTopBarState(list)
                val connection = remember { object : NestedScrollConnection {
                    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                        barState.onScroll(available.y)
                        return Offset.Zero
                    }
                } }
                MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme(),
                    motionScheme = MotionScheme.expressive()) {
                    backdrop = MaterialTheme.colorScheme.background
                    Scaffold(Modifier.fillMaxSize().nestedScroll(connection), containerColor = backdrop, topBar = {
                        ExpressiveCollapsingTopBar(TabItem.HISTORY, finance, barState.expanded,
                            rememberHazeState(), backgroundObscured = true, actions = {})
                    }) { padding ->
                        HistoryScreen(finance, { mutations++ }, Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            topContentPadding = padding.calculateTopPadding() + 16.dp, listState = list)
                    }
                }
            }
        }
        advance(1500)
    }
    private fun advance(ms: Long) { rule.mainClock.advanceTimeBy(ms); rule.waitForIdle() }
    private fun viewport() = rule.onNodeWithTag("HistoryRecordViewport")
    private fun top() = viewport().fetchSemanticsNode().boundsInRoot.top
    private fun barBottom() = rule.onNodeWithTag("MainTopBar").fetchSemanticsNode().boundsInRoot.bottom
    private fun open() = rule.onNodeWithTag("HistorySelectOpen").performSemanticsAction(SemanticsActions.OnClick) { it() }
    private fun close() = rule.onNodeWithTag("HistoryBatchClose").performClick()

    @Test fun toolbarExpandsThroughIntermediateFrames_withoutCollapsingSecondBar_orCoveringRecords() {
        show()
        rule.onNodeWithTag("MainSecondaryTopBar").assertIsDisplayed()
        val headerBottom = barBottom()
        val initialTop = top()
        val anchor = list.firstVisibleItemIndex to list.firstVisibleItemScrollOffset
        open(); advance(64)
        val intermediateTop = top()
        assertTrue(intermediateTop > initialTop + 1f)
        advance(1500)
        assertTrue(top() > intermediateTop + 1f)
        rule.onNodeWithTag("MainSecondaryTopBar").assertIsDisplayed()
        assertEquals(headerBottom, barBottom(), 1f)
        assertEquals(anchor, list.firstVisibleItemIndex to list.firstVisibleItemScrollOffset)
        val toolbar = rule.onNodeWithTag("HistoryBatchToolbar").fetchSemanticsNode().boundsInRoot
        assertTrue(toolbar.top >= headerBottom)
        assertTrue("Toolbar occupies its own space", toolbar.bottom <= top())
        close(); advance(64); open(); advance(1500)
        rule.onNodeWithTag("HistoryBatchToolbar").assertIsDisplayed()
        assertEquals(headerBottom, barBottom(), 1f)
        close(); advance(1500)
        assertEquals(initialTop, top(), 1f)
        rule.onNodeWithTag("MainSecondaryTopBar").assertIsDisplayed()
        assertEquals(0, mutations)
    }

    @Test fun longPressPreservesCollapsedHeader_andRecordsFadeAtTheDockEdge_inDarkTheme() {
        show(dark = true)
        rule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_1"))
        val rowIndex = list.layoutInfo.visibleItemsInfo.first { it.key == 1L }.index
        rule.runOnIdle { scope.launch { list.scrollToItem(rowIndex) } }
        advance(1500)
        assertFalse(barState.expanded)
        val anchor = list.firstVisibleItemIndex to list.firstVisibleItemScrollOffset
        rule.onNodeWithTag("TransactionRow_1").performTouchInput { longClick() }
        advance(1500)
        rule.onNodeWithTag("HistorySelected_1").assertIsOn()
        rule.onNodeWithTag("TransactionRow_1").assertIsSelected()
        assertFalse(barState.expanded)
        assertEquals(anchor, list.firstVisibleItemIndex to list.firstVisibleItemScrollOffset)
        rule.runOnIdle { scope.launch { list.scrollToItem(rowIndex, (24 * density).toInt()) } }
        advance(1500)
        val pixels = viewport().captureToImage().toPixelMap()
        val x = pixels.width - (8 * density).toInt()
        fun distance(color: Color) = abs(color.red - backdrop.red) + abs(color.green - backdrop.green) + abs(color.blue - backdrop.blue)
        val edge = distance(pixels[x, 1])
        val inside = distance(pixels[x, (20 * density).toInt()])
        assertTrue("Scrolling record fades into the backdrop at the dock edge ($edge < $inside)", edge + .02f < inside)
        rule.onNodeWithTag("TransactionRow_1").performTouchInput { swipeLeft() }
        advance(1500)
        assertEquals(0, mutations)
        rule.onNodeWithTag("HistorySelected_1").assertIsOn()
    }

    @Test fun minimalMotionSwitchesImmediately_andClosingRestoresTheViewport() {
        show(MotionLevel.MINIMAL)
        val initialTop = top()
        open(); advance(32)
        val selectedTop = top()
        advance(1500)
        assertEquals(selectedTop, top(), 1f)
        rule.onNodeWithTag("MainSecondaryTopBar").assertIsDisplayed()
        close(); advance(32)
        rule.onNodeWithTag("HistoryBatchToolbar").assertDoesNotExist()
        assertEquals(initialTop, top(), 1f)
    }

    @Test fun reducedMotionWideLargeFontToolbarKeepsEveryActionInsideItsBounds() {
        show(MotionLevel.REDUCED, wideLarge = true)
        val headerBottom = barBottom()
        open(); advance(32)
        advance(1000)
        assertEquals(headerBottom, barBottom(), 1f)
        assertActionsInsideToolbar()
        assertEquals(0, mutations)
    }

    @Test fun compactLargeFontSelectionKeepsEveryActionClearOfTheRecords() {
        show(MotionLevel.REDUCED, compactLarge = true)
        open(); advance(1500)
        assertActionsInsideToolbar()
        rule.onNodeWithTag("MainSecondaryTopBar").assertIsDisplayed()
    }

    private fun assertActionsInsideToolbar() {
        val toolbar = rule.onNodeWithTag("HistoryBatchToolbar").fetchSemanticsNode().boundsInRoot
        listOf("HistoryBatchClose", "HistoryBatchSelectAll", "HistoryBatchCategory", "HistoryBatchAccount", "HistoryBatchDelete").forEach {
            val bounds = rule.onNodeWithTag(it).fetchSemanticsNode().boundsInRoot
            assertTrue("$it stays inside toolbar", bounds.left >= toolbar.left - 1 && bounds.right <= toolbar.right + 1 &&
                bounds.top >= toolbar.top - 1 && bounds.bottom <= toolbar.bottom + 1)
        }
        assertTrue(toolbar.bottom <= top())
        assertEquals(0, mutations)
    }
}
