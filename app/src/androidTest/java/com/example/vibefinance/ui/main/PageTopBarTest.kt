package com.example.vibefinance.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.ui.FinanceUiState
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** ARTEMIS/ADB verified a small down-scroll and return on Daily before these tests.
 * Real animated shared bar + Scaffold/list in memory; no financial records are changed.
 */
@RunWith(AndroidJUnit4::class)
class PageTopBarTest {
    @get:Rule val rule = createComposeRule()
    private lateinit var list: LazyListState
    private lateinit var state: PageTopBarState
    private lateinit var scope: CoroutineScope
    private var density = 1f

    private fun show(tab: TabItem = TabItem.HOME, wide: Boolean = false, large: Boolean = false, count: Int = 30) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            val original = LocalDensity.current
            density = original.density
            CompositionLocalProvider(LocalDensity provides Density(original.density, if (large) 1.7f else original.fontScale)) {
                list = rememberLazyListState()
                state = rememberPageTopBarState(list)
                scope = rememberCoroutineScope()
                val connection = remember(state) { object : NestedScrollConnection {
                    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                        state.onScroll(available.y)
                        return Offset.Zero
                    }
                } }
                MaterialTheme {
                    Scaffold(Modifier.fillMaxSize().nestedScroll(connection), topBar = {
                        ExpressiveCollapsingTopBar(tab, FinanceUiState(isLoading = false), state.expanded,
                            rememberHazeState(), backgroundObscured = true, actions = {})
                    }) { padding ->
                        // Same compact overlay / wide inset contract used by MainScreen.
                        LazyColumn(state = list,
                            modifier = Modifier.fillMaxSize().padding(top = if (wide) padding.calculateTopPadding() else 0.dp)
                                .testTag("PageList"),
                            contentPadding = PaddingValues(top = (if (wide) 0.dp else padding.calculateTopPadding()) + 16.dp)) {
                            items(count) { index ->
                                Text("Row $index", Modifier.fillMaxWidth().height(100.dp).testTag("Row_$index"))
                            }
                        }
                    }
                }
            }
        }
        advance(1_000)
    }

    private fun advance(ms: Long) { rule.mainClock.advanceTimeBy(ms); rule.waitForIdle() }
    private fun scroll(index: Int, offsetDp: Int = 0) {
        rule.runOnIdle { scope.launch { list.scrollToItem(index, (offsetDp * density).toInt()) } }
        advance(32)
    }
    private fun barBottom() = rule.onNodeWithTag("MainTopBar").fetchSemanticsNode().boundsInRoot.bottom
    private fun firstTop() = rule.onNodeWithTag("Row_0").fetchSemanticsNode().boundsInRoot.top
    private fun assertRestored() {
        advance(1_000)
        rule.onNodeWithTag("MainSecondaryTopBar").assertIsDisplayed()
        assertEquals(16f * density, firstTop() - barBottom(), 1f)
    }

    private fun collapseAndRestore(tab: TabItem, wide: Boolean = false, large: Boolean = false) {
        show(tab, wide, large)
        val expandedBottom = barBottom()
        scroll(0, 60)
        advance(1_000)
        rule.onNodeWithTag("MainSecondaryTopBar").assertDoesNotExist()
        assertTrue(barBottom() < expandedBottom - 40f * density)
        assertTrue("Collapsed title must not reserve its old blank band", firstTop() <= barBottom() + 16f * density)
        scroll(0)
        assertRestored()
    }

    @Test fun dailyCollapsesWithoutGapAndRestoresAtTop() = collapseAndRestore(TabItem.HOME)
    @Test fun assetsCollapsesWithoutGapAndRestoresAtTop() = collapseAndRestore(TabItem.ACCOUNTS)
    @Test fun recurringCollapsesWithoutGapAndRestoresAtTop() = collapseAndRestore(TabItem.RECURRING)
    @Test fun historyCollapsesWithoutGapAndRestoresAtTop() = collapseAndRestore(TabItem.HISTORY)
    @Test fun wideInsetLayoutDoesNotReserveTheExpandedTitle() = collapseAndRestore(TabItem.HOME, wide = true)
    @Test fun largeTextUsesMeasuredHeaderHeight() = collapseAndRestore(TabItem.HOME, large = true)

    @Test fun scrollingUpInMiddleDoesNotExpandUntilLeadingSpaceReturns() {
        show()
        scroll(10)
        advance(1_000)
        rule.runOnIdle { state.onScroll(30f) }
        scroll(8)
        advance(1_000)
        assertFalse(state.expanded)
        scroll(0, 16)
        assertRestoredNearTop()
        scroll(0)
        assertRestored()
    }

    private fun assertRestoredNearTop() {
        advance(1_000)
        rule.onNodeWithTag("MainSecondaryTopBar").assertIsDisplayed()
    }

    @Test fun shortContentKeepsExpandedTitleWithoutScrollOscillation() {
        show(count = 1)
        repeat(3) {
            rule.onNodeWithTag("PageList").performTouchInput { swipeUp() }
            advance(1_000)
            assertRestored()
        }
    }
}
