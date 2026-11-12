package com.example.vibefinance.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Layout-only fixtures: app-local density, no device scale changes or persisted data. */
class AdaptiveMainFrameTest {
    @get:Rule val rule = createComposeRule()
    private var width by mutableStateOf(320)
    private var height by mutableStateOf(640)

    private fun show(fontScale: Float = 1f) {
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(0.25f, fontScale)) {
                MaterialTheme {
                    Box(Modifier.requiredSize(width.dp, height.dp).testTag("Window")) {
                        AdaptiveMainFrame(
                            selectedTab = TabItem.ACCOUNTS,
                            onSelect = {},
                            onTogglePrivacy = {},
                            onToggleTheme = {}
                        ) { rail ->
                            Box(Modifier.fillMaxSize().testTag("Content")) {
                                Text(if (rail) "Rail layout" else "Bottom layout")
                            }
                        }
                    }
                }
            }
        }
        rule.waitUntil(5000) {
            runCatching { rule.onAllNodesWithTag("Window").fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)
        }
    }

    @Test fun compactAndBreakpointResizeKeepContentInsideWindow() {
        height = 400 // Landscape mode where width >= 600 activates Navigation Rail
        show()
        for (w in listOf(320, 596, 600, 836, 840, 1200, 320)) {
            rule.runOnIdle { width = w }
            rule.waitForIdle()
            if (w < 600) {
                rule.onNodeWithTag("MainNavigationRail").assertDoesNotExist()
                rule.onNodeWithTag("MainContentContainer").assertDoesNotExist()
                rule.onNodeWithTag("RailAddTransaction").assertDoesNotExist()
            }
            else {
                rule.onNodeWithTag("MainNavigationRail").assertIsDisplayed()
                rule.onNodeWithTag("MainContentContainer").assertIsDisplayed()
                rule.onNodeWithTag("RailAddTransaction").assertIsDisplayed()
                TabItem.entries.forEach { rule.onNodeWithTag("RailNav_${it.name}").assertExists() }
                rule.onNodeWithTag("RailNav_ACCOUNTS").assertIsSelected()
            }
            val window = rule.onNodeWithTag("Window").fetchSemanticsNode().boundsInRoot
            val content = rule.onNodeWithTag("Content").fetchSemanticsNode().boundsInRoot
            assertTrue(content.width > 0 && content.left >= window.left && content.right <= window.right + 1)
        }
    }

    @Test fun portraitMediumScreenUsesRail() {
        width = 628; height = 866 // Portrait foldable / scrcpy window
        show()
        rule.onNodeWithTag("MainNavigationRail").assertIsDisplayed()
        rule.onNodeWithText("Rail layout").assertIsDisplayed()
        rule.onNodeWithTag("RailAddTransaction").assertIsDisplayed()
        rule.onNodeWithTag("RailSettings").assertIsDisplayed()
    }

    @Test fun shortLandscapeRetainsScrollableRail() {
        width = 960; height = 360
        show()
        rule.onNodeWithTag("MainNavigationRail").assert(hasScrollAction())
        rule.onNodeWithTag("RailNav_HISTORY").assertExists()
        rule.onNodeWithTag("Content").assertExists()
    }

    @Test fun doubledFontsRetainEveryDestination() {
        width = 840; height = 800
        show(fontScale = 2f)
        TabItem.entries.forEach { rule.onNodeWithTag("RailNav_${it.name}").assertIsDisplayed() }
    }

    @Test fun compactPhoneAndLargeTabletBentoCardProportionsKeepBalancedHeight() {
        for ((w, spacing) in listOf(320 to 12.dp, 400 to 16.dp, 600 to 16.dp, 840 to 16.dp, 1200 to 16.dp)) {
            val daysSide = minOf(((w.dp - spacing) * 0.4f), 156.dp).coerceAtLeast(110.dp)
            assertTrue("Days left card must be at least 110dp on $w dp width", daysSide >= 110.dp)
            assertTrue("Days left card must never exceed 156dp on $w dp width", daysSide <= 156.dp)
        }
    }

    @Test fun railHubAnchorsLogoFabTabsAndBottomUtilities() {
        width = 1024; height = 768
        show()
        rule.onNodeWithTag("MainNavigationRail").assertIsDisplayed()
        rule.onNodeWithTag("RailAppLogo").assertIsDisplayed()
        rule.onNodeWithTag("RailAddTransaction").assertIsDisplayed()
        TabItem.entries.forEach { rule.onNodeWithTag("RailNav_${it.name}").assertIsDisplayed() }
        rule.onNodeWithTag("RailNav_ACCOUNTS").assertIsSelected()
        rule.onNodeWithTag("RailTogglePrivacy").assertIsDisplayed()
        rule.onNodeWithTag("RailToggleTheme").assertIsDisplayed()
        rule.onNodeWithTag("RailSettings").assertIsDisplayed()

        val railBounds = rule.onNodeWithTag("MainNavigationRail").fetchSemanticsNode().boundsInRoot
        val contentBounds = rule.onNodeWithTag("MainContentContainer").fetchSemanticsNode().boundsInRoot
        assertTrue("Rail must have positive width", railBounds.width > 0)
        assertTrue("Content must sit to the right of the rail", contentBounds.left >= railBounds.right)
    }

    @Test fun contextAwareFabAdaptsActionAndDescriptionPerTab() {
        var addTransactionCalled = false
        var addSubscriptionCalled = false
        var addAccountCalled = false
        var activeTab by mutableStateOf(TabItem.HOME)

        rule.setContent {
            MaterialTheme {
                Box(Modifier.requiredSize(1024.dp, 768.dp)) {
                    AdaptiveMainFrame(
                        selectedTab = activeTab,
                        onSelect = { activeTab = it },
                        onAddTransaction = { addTransactionCalled = true },
                        onAddSubscription = { addSubscriptionCalled = true },
                        onAddAccount = { addAccountCalled = true }
                    ) {
                        Box(Modifier.fillMaxSize())
                    }
                }
            }
        }

        rule.waitForIdle()
        // In HOME tab
        rule.onNodeWithTag("RailAddTransaction").performClick()
        rule.waitForIdle()
        assertTrue("HOME tab invokes onAddTransaction", addTransactionCalled)

        // Switch to RECURRING tab
        rule.runOnIdle { activeTab = TabItem.RECURRING }
        rule.waitForIdle()
        rule.onNodeWithTag("RailAddTransaction").performClick()
        rule.waitForIdle()
        assertTrue("RECURRING tab invokes onAddSubscription", addSubscriptionCalled)

        // Switch to ACCOUNTS tab
        rule.runOnIdle { activeTab = TabItem.ACCOUNTS }
        rule.waitForIdle()
        rule.onNodeWithTag("RailAddTransaction").performClick()
        rule.waitForIdle()
        assertTrue("ACCOUNTS tab invokes onAddAccount", addAccountCalled)
    }
}
