package com.example.vibefinance.ui.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.ui.FinanceIntent
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.main.AdaptiveMainFrame
import com.example.vibefinance.ui.main.TabItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tests in-app full-screen OSM expansion, Google redirection retention, metadata repositioning, and rail isolation. */
@RunWith(AndroidJUnit4::class)
class TransactionFullScreenMapTest {
    @get:Rule val rule = createComposeRule()

    private val now = System.currentTimeMillis()
    private val accounts = listOf(
        AccountEntity(id = 1L, name = "Octopus", type = AccountType.BANK, balance = 500.0, icon = "wallet")
    )
    private val locatedTx = TransactionEntity(
        id = 101L,
        amount = 48.0,
        category = "Food",
        timestamp = now - 60_000L,
        accountId = 1L,
        description = "Central Cafe",
        latitude = 22.2819,
        longitude = 114.1582,
        locationName = "中環 Central"
    )
    private val unlocatedTx = TransactionEntity(
        id = 102L,
        amount = 22.0,
        category = "Shopping",
        timestamp = now - 120_000L,
        accountId = 1L,
        description = "No GPS Snack",
        latitude = null,
        longitude = null,
        locationName = null
    )

    @Test
    fun phoneFullScreenOsm_repositionSaveAndExitWithoutOverwrite() {
        var transactions by mutableStateOf(listOf(locatedTx, unlocatedTx))
        var editedTx: TransactionEntity? = null

        rule.setPhoneContent {
            MaterialTheme {
                HistoryScreen(
                    state = FinanceUiState(isLoading = false, accounts = accounts, transactions = transactions),
                    onIntent = { intent ->
                        if (intent is FinanceIntent.EditTransaction) {
                            editedTx = intent.newTx
                            transactions = transactions.map { if (it.id == intent.oldTx.id) intent.newTx else it }
                        }
                    }
                )
            }
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val googleMapsText = context.getString(R.string.tx_detail_open_in_map)

        rule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_101"))
        rule.onNodeWithTag("TransactionRow_101").performClick()
        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("OsmLocationCard").fetchSemanticsNodes().isNotEmpty()
        }

        // Google redirection remains visible alongside a single full-screen OSM trigger (no duplicate overlay badge).
        rule.onNodeWithText(googleMapsText).performScrollTo().assertIsDisplayed()
        rule.onAllNodesWithTag("OpenFullScreenOsmButton").assertCountEquals(1)
        rule.onAllNodesWithTag("ExpandOsmMapBadge").assertCountEquals(0)
        rule.onNodeWithTag("OpenFullScreenOsmButton").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("OpenFullScreenOsmButton").performClick()

        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("TransactionFullScreenMapOverlay").fetchSemanticsNodes().isNotEmpty()
        }

        val exitBounds = rule.onNodeWithTag("FullScreenMapExitButton").fetchSemanticsNode().boundsInRoot
        val overlayBounds = rule.onNodeWithTag("TransactionFullScreenMapOverlay").fetchSemanticsNode().boundsInRoot
        val redirectBounds = rule.onNodeWithTag("FullScreenMapGoogleRedirectButton").fetchSemanticsNode().boundsInRoot
        assertTrue("Exit button should be at top-left", exitBounds.left < overlayBounds.center.x && exitBounds.top < overlayBounds.center.y)
        assertTrue("Google redirection button should remain visible at top-right", redirectBounds.right > exitBounds.right)
        rule.onNodeWithTag("FullScreenMapControlCard").assertIsDisplayed()

        // Editing then exiting via top-left Exit must NOT overwrite transaction metadata.
        rule.onNodeWithTag("FullScreenMapLocationNameInput").performTextReplacement("Should Not Save")
        Espresso.closeSoftKeyboard()
        rule.onNodeWithTag("FullScreenMapExitButton").performClick()

        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("TransactionFullScreenMapOverlay").fetchSemanticsNodes().isEmpty()
        }
        assertNull("Exiting without Save must not mutate location metadata", editedTx)
        rule.onNodeWithTag("OsmLocationCard").assertIsDisplayed()

        // Re-open via mini-map tap, reposition via preset + edit location name, and Save.
        rule.onNodeWithTag("OsmMiniMapPreviewTap").performScrollTo().performClick()
        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("TransactionFullScreenMapOverlay").fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithTag("FullScreenMapPreset_mongkok").performScrollTo().performClick()
        rule.onNodeWithTag("FullScreenMapLocationNameInput").performTextReplacement("旺角新分店")
        Espresso.closeSoftKeyboard()
        rule.onNodeWithTag("FullScreenMapSaveButton").performClick()

        rule.waitUntil(5_000) {
            editedTx != null && rule.onAllNodesWithTag("TransactionFullScreenMapOverlay").fetchSemanticsNodes().isEmpty()
        }

        val saved = editedTx
        assertNotNull(saved)
        assertEquals(22.3193, saved!!.latitude!!, 0.0001)
        assertEquals(114.1694, saved.longitude!!, 0.0001)
        assertEquals("旺角新分店", saved.locationName)
        rule.onNodeWithTag("OsmLocationCard").assertIsDisplayed()
        rule.onNodeWithText("旺角新分店").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun noLocationTransaction_pickOnFullScreenOsmSavesCoordinates() {
        var transactions by mutableStateOf(listOf(locatedTx, unlocatedTx))
        var editedTx: TransactionEntity? = null

        rule.setPhoneContent {
            MaterialTheme {
                HistoryScreen(
                    state = FinanceUiState(isLoading = false, accounts = accounts, transactions = transactions),
                    onIntent = { intent ->
                        if (intent is FinanceIntent.EditTransaction) {
                            editedTx = intent.newTx
                            transactions = transactions.map { if (it.id == intent.oldTx.id) intent.newTx else it }
                        }
                    }
                )
            }
        }

        rule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_102"))
        rule.onNodeWithTag("TransactionRow_102").performClick()
        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("OsmNoLocationCard").fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithTag("PickOnFullScreenOsmButton").performScrollTo().performClick()
        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("TransactionFullScreenMapOverlay").fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithTag("FullScreenMapPreset_tsuen_wan").performScrollTo().performClick()
        rule.onNodeWithTag("FullScreenMapSaveButton").performClick()

        rule.waitUntil(5_000) {
            editedTx != null && rule.onAllNodesWithTag("TransactionFullScreenMapOverlay").fetchSemanticsNodes().isEmpty()
        }

        val saved = requireNotNull(editedTx)
        assertEquals(22.3713, saved.latitude!!, 0.0001)
        assertEquals(114.1141, saved.longitude!!, 0.0001)
        assertEquals("荃灣 Tsuen Wan", saved.locationName)
        rule.onNodeWithTag("OsmLocationCard").assertIsDisplayed()
        rule.onNodeWithText("荃灣 Tsuen Wan").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun largeScreenFullScreenOsm_staysInsideContentContainerAndDoesNotOverlayRail() {
        var transactions by mutableStateOf(listOf(locatedTx, unlocatedTx))

        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(0.5f, 1f)) {
                MaterialTheme {
                    Box(Modifier.requiredSize(960.dp, 720.dp)) {
                        AdaptiveMainFrame(
                            selectedTab = TabItem.HISTORY,
                            onSelect = {},
                            onTogglePrivacy = {},
                            onToggleTheme = {},
                        ) { _ ->
                            HistoryScreen(
                                state = FinanceUiState(isLoading = false, accounts = accounts, transactions = transactions),
                                onIntent = { intent ->
                                    if (intent is FinanceIntent.EditTransaction) {
                                        transactions = transactions.map { if (it.id == intent.oldTx.id) intent.newTx else it }
                                    }
                                },
                                modifier = Modifier.fillMaxSize(),
                                horizontalContentPadding = 16.dp,
                            )
                        }
                    }
                }
            }
        }

        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("MainNavigationRail").fetchSemanticsNodes().isNotEmpty() &&
                rule.onAllNodesWithTag("TransactionRow_101").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithTag("RailActiveIndicatorPill").assertIsDisplayed()

        rule.onNodeWithTag("TransactionRow_101").performClick()
        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("TransactionDetailPage").fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithTag("OpenFullScreenOsmButton").performScrollTo().performClick()
        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("TransactionFullScreenMapOverlay").fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithTag("MainNavigationRail").assertIsDisplayed()
        rule.onNodeWithTag("MainContentContainer").assertIsDisplayed()
        rule.onNodeWithTag("FullScreenMapExitButton").assertIsDisplayed()
        rule.onNodeWithTag("FullScreenMapGoogleRedirectButton").assertIsDisplayed()

        val railBounds = rule.onNodeWithTag("MainNavigationRail").fetchSemanticsNode().boundsInRoot
        val overlayBounds = rule.onNodeWithTag("TransactionFullScreenMapOverlay").fetchSemanticsNode().boundsInRoot
        val containerBounds = rule.onNodeWithTag("MainContentContainer").fetchSemanticsNode().boundsInRoot

        assertTrue(
            "Large-screen OSM overlay (${overlayBounds.left}) must not cover NavigationRail (${railBounds.right})",
            overlayBounds.left >= railBounds.right - 1f
        )
        assertTrue(
            "Large-screen OSM overlay (${overlayBounds.width}) should fill MainContentContainer (${containerBounds.width})",
            overlayBounds.width >= containerBounds.width - 4f
        )

        rule.onNodeWithTag("FullScreenMapExitButton").performClick()
        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("TransactionFullScreenMapOverlay").fetchSemanticsNodes().isEmpty() &&
                rule.onAllNodesWithTag("TransactionDetailPage").fetchSemanticsNodes().isNotEmpty()
        }

        // Verify equal spacing inside MainContentContainer on large-screen layouts.
        val barBounds = rule.onNodeWithTag("HistorySearchBar").fetchSemanticsNode().boundsInRoot
        val detailBounds = rule.onNodeWithTag("TransactionDetailPage").fetchSemanticsNode().boundsInRoot
        val leftMargin = barBounds.left - containerBounds.left
        val topMargin = barBounds.top - containerBounds.top
        val middleGap = detailBounds.left - barBounds.right
        val rightMargin = containerBounds.right - detailBounds.right
        assertTrue("Search bar top margin ($topMargin) should match left margin ($leftMargin)", topMargin >= 6f && kotlin.math.abs(topMargin - leftMargin) <= 3f)
        assertTrue("Middle gap ($middleGap) should match outer margin ($leftMargin)", kotlin.math.abs(middleGap - leftMargin) <= 3f)
        assertTrue("Right margin ($rightMargin) should match left margin ($leftMargin)", kotlin.math.abs(rightMargin - leftMargin) <= 3f)

        // Verify optimistic event preview update immediately upon saving location even before state emits.
        rule.onNodeWithTag("OpenFullScreenOsmButton").performScrollTo().performClick()
        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("TransactionFullScreenMapOverlay").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithTag("FullScreenMapPreset_tst").performScrollTo().performClick()
        rule.onNodeWithTag("FullScreenMapLocationNameInput").performTextReplacement("大螢幕新地標")
        Espresso.closeSoftKeyboard()
        rule.onNodeWithTag("FullScreenMapSaveButton").performClick()
        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("TransactionFullScreenMapOverlay").fetchSemanticsNodes().isEmpty() &&
                rule.onAllNodesWithText("大螢幕新地標").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithTag("OsmLocationCard").assertIsDisplayed()

        // Verify shared navigation element transition when opening and closing the search bar.
        rule.onNodeWithTag("HistorySearchNavSearch").assertIsDisplayed()
        rule.onAllNodesWithTag("HistorySearch").onFirst().performClick()
        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("HistorySearchNavBack").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onAllNodesWithTag("HistorySearchNavBack").onLast().performClick()
        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("HistorySearchNavSearch").fetchSemanticsNodes().isNotEmpty()
        }

        // Verify switching from selected TransactionRow_101 to TransactionRow_102 transitions cleanly through staggered exit & entrance.
        rule.onNodeWithTag("TransactionRow_102").performClick()
        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("OsmNoLocationCard").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithTag("OsmNoLocationCard").assertIsDisplayed()
    }
}
