package com.example.vibefinance.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.data.entity.SubscriptionEntity
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.accounts.AccountsScreen
import com.example.vibefinance.ui.history.HistoryScreen
import com.example.vibefinance.ui.recurring.RecurringScreen
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** ARTEMIS/ADB explored all three pages, their scrolling, controls and empty states first.
 * Fixtures stay in Compose memory: no financial records or preferences are replaced. */
@RunWith(AndroidJUnit4::class)
class PageContentEntranceTest {
    @get:Rule val rule = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val accounts = (1L..24L).map {
        AccountEntity(it, "Account ${it.toString().padStart(2, '0')}", type = AccountType.CASH,
            balance = 100.0, icon = "Savings")
    }
    private val now = System.currentTimeMillis()
    private val subscriptions = (1L..24L).map {
        SubscriptionEntity(it, "Bill $it", 20.0, "Entertainment", "Monthly", now + 86_400_000L, 1L)
    }
    private val transactions = (1L..24L).map {
        TransactionEntity(id = it, amount = 12.0, category = "Food", accountId = 1L,
            timestamp = now - it * 60_000L, description = "Expense $it")
    }

    @Test
    fun assets_cardsArriveOnScroll_withoutLayoutJumps_orReplayAfterDataUpdatesAndDisposal() {
        var state by mutableStateOf(FinanceUiState(isLoading = false, accounts = accounts))
        host { AccountsScreen(state, {}) }
        assertArrival("AssetsArrival:summary")
        scrollToTag("AssetsList", "AssetsArrival:account:12")
        assertArrival("AssetsArrival:account:12")
        assertSettledAfterUpdate("AssetsArrival:account:12") {
            state = state.copy(accounts = accounts.map { it.copy(balance = 200.0) })
        }
        assertSettledOnReturn("AssetsList", "AssetsArrival:summary", "AssetsArrival:account:12")
    }

    @Test
    fun recurring_eventsArriveOnScroll_andSeenEventsStaySettledAfterUpdates() {
        var state by mutableStateOf(FinanceUiState(isLoading = false,
            accounts = accounts.take(1), subscriptions = subscriptions))
        host { RecurringScreen(state, {}) }
        assertArrival("RecurringArrival:summary")
        scrollToTag("RecurringList", "RecurringArrival:subscription:12")
        assertArrival("RecurringArrival:subscription:12")
        assertSettledAfterUpdate("RecurringArrival:subscription:12") {
            state = state.copy(subscriptions = subscriptions.map { it.copy(amount = 25.0) })
        }
        assertSettledOnReturn("RecurringList", "RecurringArrival:summary", "RecurringArrival:subscription:12")
    }

    @Test
    fun history_eventsArriveOnScroll_andRemainInteractiveAfterArrival() {
        host { HistoryScreen(FinanceUiState(isLoading = false,
            accounts = accounts.take(1), transactions = transactions), {}) }
        assertArrival("HistoryArrival:analytics")
        scrollToTag("HistoryList", "HistoryArrival:transaction:12")
        assertArrival("HistoryArrival:transaction:12")
        assertSettledOnReturn("HistoryList", "HistoryArrival:analytics", "HistoryArrival:transaction:12")
        rule.onNodeWithTag("TransactionRow_12").performClick()
        rule.mainClock.advanceTimeBy(1_000)
        rule.waitUntil(5_000) {
            rule.onAllNodes(hasTestTag("EditTransactionSave")).fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithTag("EditTransactionSave").assertExists()
    }

    @Test
    fun contentBehindMovingBars_waitsUntilTheUnobscuredViewportReachesIt() {
        var bottomInset by mutableStateOf(200.dp)
        host {
            ContentEntranceViewport(Modifier.width(360.dp).height(440.dp),
                bottomVisibilityInset = bottomInset, tagPrefix = "BarsArrival") {
                Column {
                    Spacer(Modifier.height(300.dp))
                    PageContentEntrance("card") {
                        Box(Modifier.fillMaxWidth().height(100.dp).background(Color.White))
                    }
                }
            }
        }
        rule.mainClock.advanceTimeBy(1_000)
        assertTrue("A card behind the navigation bar must remain unrevealed",
            brightness(rule.onNodeWithTag("BarsArrival:card").captureToImage()) < 2f)
        rule.runOnIdle { bottomInset = 0.dp }
        assertArrival("BarsArrival:card")
    }

    @Test
    fun recurring_emptyStateHasNoOneTapServiceSuggestions_andOrdinaryAddStillOpensForm() {
        rule.setContent { MaterialTheme { RecurringScreen(FinanceUiState(isLoading = false), {}) } }
        rule.onNodeWithTag("RecurringList")
            .performScrollToNode(hasTestTag("RecurringArrival:empty"))
        rule.onNodeWithText(context.getString(R.string.ui_recurring_quick_presets)).assertDoesNotExist()
        rule.onNodeWithText(context.getString(R.string.btn_add_subscription)).performClick()
        rule.waitUntil(5_000) {
            rule.onAllNodes(hasText(context.getString(R.string.sub_name_label)) and hasSetTextAction()).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun host(content: @Composable () -> Unit) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme(surface = Color(0xFFCCCCCC),
                surfaceVariant = Color(0xFFCCCCCC), surfaceContainerLow = Color(0xFFCCCCCC))) {
                Box(Modifier.width(360.dp).height(440.dp).background(Color.Black)) { content() }
            }
        }
        rule.mainClock.advanceTimeByFrame()
        rule.waitForIdle()
    }

    private fun scrollToTag(listTag: String, tag: String) {
        // These indexes belong to the deterministic memory fixture, not screen coordinates.
        // ScrollToIndex jumps without an animated-scroll wait; the stable target tag verifies
        // the resulting UI. This lets the paused clock capture the entrance's first frames.
        val fixtureIndex = when (tag) {
            "AssetsArrival:summary", "RecurringArrival:summary" -> 0
            "HistoryArrival:analytics" -> 1
            "AssetsArrival:account:12" -> 16 // summary, filter, title, mode, group, then 12th account
            "RecurringArrival:subscription:12" -> 15 // summary, chart, controls, group, then 12th bill
            "HistoryArrival:transaction:12" -> 14 // period, analytics, date, then 12th expense
            else -> error("No fixture index for $tag")
        }
        rule.onNodeWithTag(listTag).performSemanticsAction(SemanticsActions.ScrollToIndex) {
            assertTrue(it(fixtureIndex))
        }
        rule.mainClock.advanceTimeBy(48)
        rule.waitForIdle()
        rule.onNodeWithTag(tag).assertExists()
    }

    private fun assertArrival(tag: String) {
        rule.mainClock.advanceTimeByFrame()
        val node = rule.onNodeWithTag(tag)
        val bounds = node.fetchSemanticsNode().boundsInRoot
        val initial = brightness(node.captureToImage())
        rule.mainClock.advanceTimeBy(112)
        val middle = brightness(node.captureToImage())
        rule.mainClock.advanceTimeBy(1_000)
        val complete = brightness(node.captureToImage())
        assertTrue("$tag fades in when visible: $initial -> $middle", middle > initial + 4f)
        assertTrue("$tag finishes at full opacity: $middle -> $complete", complete > middle + 4f)
        assertEquals("Arrival must preserve layout geometry", bounds, node.fetchSemanticsNode().boundsInRoot)
    }

    private fun assertSettledAfterUpdate(tag: String, update: () -> Unit) {
        val node = rule.onNodeWithTag(tag)
        val before = brightness(node.captureToImage())
        rule.runOnIdle(update)
        rule.mainClock.advanceTimeBy(32)
        assertTrue("A financial update must not restart the entrance",
            abs(before - brightness(node.captureToImage())) < 6f)
        rule.mainClock.advanceTimeBy(1_000)
    }

    private fun assertSettledOnReturn(listTag: String, topTag: String, itemTag: String) {
        val before = brightness(rule.onNodeWithTag(itemTag).captureToImage())
        scrollToTag(listTag, topTag)
        rule.mainClock.advanceTimeBy(48)
        rule.onNodeWithTag(itemTag).assertDoesNotExist()
        scrollToTag(listTag, itemTag)
        rule.mainClock.advanceTimeBy(48)
        assertTrue("Lazy disposal must not replay an already seen arrival",
            abs(before - brightness(rule.onNodeWithTag(itemTag).captureToImage())) < 6f)
    }

    private fun brightness(bitmap: ImageBitmap): Float {
        val pixels = bitmap.toPixelMap()
        val values = mutableListOf<Float>()
        for (y in (bitmap.height * .25f).toInt() until (bitmap.height * .75f).toInt() step 3) {
            for (x in (bitmap.width * .2f).toInt() until (bitmap.width * .8f).toInt() step 3) {
                val pixel = pixels[x, y]
                values += (pixel.red + pixel.green + pixel.blue) * 255f / 3f
            }
        }
        return values.sorted()[values.size / 2]
    }
}
