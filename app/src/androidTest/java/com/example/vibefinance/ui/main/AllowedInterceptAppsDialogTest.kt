package com.example.vibefinance.ui.main

import android.content.Context
import android.os.Looper
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vibefinance.R
import com.example.vibefinance.util.InstalledAppInfo
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** ARTEMIS verified slow loading, search, row selection, retry and dismissal on Waydroid. */
@RunWith(AndroidJUnit4::class)
class AllowedInterceptAppsDialogTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun pendingCatalogShowsLoadingAndSearchFiltersResultWithoutBlockingMainThread() {
        val catalog = CompletableDeferred<List<InstalledAppInfo>>()
        val offMain = AtomicBoolean(false)
        val selected = ConcurrentHashMap.newKeySet<String>()
        showPicker(
            loadApps = {
                offMain.set(Looper.myLooper() != Looper.getMainLooper())
                catalog.await()
            },
            onToggle = { selected.add(it) },
        )
        waitForTag("AllowedAppsLoading")
        rule.onNodeWithTag("AllowedAppsLoading").assertIsDisplayed()
        rule.onNodeWithText(text(R.string.allowed_apps_no_matches)).assertDoesNotExist()
        rule.onNodeWithTag("AllowedAppsSearch").performTextReplacement("octopus")
        rule.onNodeWithTag("AllowedAppsLoading").assertIsDisplayed()
        closeSoftKeyboard()
        catalog.complete(
            listOf(app("Octopus", "test.octopus"), app("Calculator", "test.calculator"))
        )
        waitForTag("AllowedAppsOption_test.octopus")
        rule.onNodeWithTag("AllowedAppsLoading").assertDoesNotExist()
        rule.onNodeWithTag("AllowedAppsOption_test.octopus").assertIsDisplayed().performClick()
        rule.onNodeWithTag("AllowedAppsOption_test.calculator").assertDoesNotExist()
        rule
            .onNode(
                isToggleable() and hasAnyAncestor(hasTestTag("AllowedAppsOption_test.octopus")),
                useUnmergedTree = true,
            )
            .assertIsOn()
        rule.runOnIdle {
            assertTrue(offMain.get())
            assertEquals(setOf("test.octopus"), selected)
        }
    }

    @Test
    fun confirmDuringLoadingCancelsScanWithoutChangingSelections() {
        val catalog = CompletableDeferred<List<InstalledAppInfo>>()
        val started = AtomicBoolean(false)
        val cancelled = AtomicBoolean(false)
        val dismissals = AtomicInteger()
        val toggles = AtomicInteger()
        showPicker(
            loadApps = {
                started.set(true)
                try {
                    catalog.await()
                } catch (cancellation: CancellationException) {
                    cancelled.set(true)
                    throw cancellation
                }
            },
            onToggle = { toggles.incrementAndGet() },
            onDismiss = { dismissals.incrementAndGet() },
        )
        rule.waitUntil(5_000) { started.get() }
        waitForTag("AllowedAppsLoading")
        rule.onNodeWithText(text(R.string.btn_confirm)).performClick()
        rule.waitUntil(5_000) { cancelled.get() && dismissals.get() == 1 }
        rule.onNodeWithTag("AllowedAppsSearch").assertDoesNotExist()
        assertEquals(0, toggles.get())
    }

    @Test
    fun failedCatalogCanRetryAndShowsEmptyOnlyAfterSuccessfulLoad() {
        val retry = CompletableDeferred<List<InstalledAppInfo>>()
        val attempts = AtomicInteger()
        showPicker(
            loadApps = {
                if (attempts.incrementAndGet() == 1) error("Simulated package scan failure")
                retry.await()
            }
        )
        rule.waitUntil(5_000) {
            rule
                .onAllNodesWithText(text(R.string.assets_linked_app_retry))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        rule.onNodeWithText(text(R.string.assets_linked_app_load_failed)).assertIsDisplayed()
        rule.onNodeWithText(text(R.string.allowed_apps_no_matches)).assertDoesNotExist()
        rule.onNodeWithText(text(R.string.assets_linked_app_retry)).performClick()
        waitForTag("AllowedAppsLoading")
        retry.complete(emptyList())
        rule.waitUntil(5_000) {
            rule
                .onAllNodesWithText(text(R.string.allowed_apps_no_matches))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        rule.onNodeWithTag("AllowedAppsLoading").assertDoesNotExist()
        rule.onNodeWithText(text(R.string.allowed_apps_no_matches)).assertIsDisplayed()
        assertEquals(2, attempts.get())
    }

    @Test
    fun largeCatalogLoadsIconsOnlyForComposedRowsAndScrollsToLastApp() {
        val iconRequests = ConcurrentHashMap.newKeySet<String>()
        val selected = ConcurrentHashMap.newKeySet<String>()
        val apps = (0..999).map { app("App $it", "test.app$it") }
        rule.setContent {
            var checked by remember { mutableStateOf(setOf("test.app0")) }
            MaterialTheme {
                AllowedInterceptAppsPicker(
                    checked,
                    onToggleApp = {
                        selected.add(it)
                        checked = if (it in checked) checked - it else checked + it
                    },
                    onDismiss = {},
                    loadApps = { apps },
                    loadIcon = { _, pkg ->
                        iconRequests.add(pkg)
                        null
                    },
                )
            }
        }
        waitForTag("AllowedAppsOption_test.app0")
        rule.waitUntil(5_000) { iconRequests.isNotEmpty() }
        rule.runOnIdle {
            assertTrue("Only visible/prefetched rows should request icons", iconRequests.size < 30)
        }
        rule
            .onNode(
                isToggleable() and hasAnyAncestor(hasTestTag("AllowedAppsOption_test.app0")),
                useUnmergedTree = true,
            )
            .assertIsOn()
        rule.onNodeWithTag("AllowedAppsList").performScrollToIndex(999)
        waitForTag("AllowedAppsOption_test.app999")
        rule.onNodeWithTag("AllowedAppsOption_test.app999").assertIsDisplayed().performClick()
        rule
            .onNode(
                isToggleable() and hasAnyAncestor(hasTestTag("AllowedAppsOption_test.app999")),
                useUnmergedTree = true,
            )
            .assertIsOn()
        rule.runOnIdle {
            assertEquals(setOf("test.app999"), selected)
            assertTrue("Scrolling must not decode the full catalog", iconRequests.size < 60)
        }
    }

    private fun showPicker(
        loadApps: suspend (Context) -> List<InstalledAppInfo>,
        onToggle: (String) -> Unit = {},
        onDismiss: () -> Unit = {},
    ) {
        rule.setContent {
            var shown by remember { mutableStateOf(true) }
            var checked by remember { mutableStateOf(emptySet<String>()) }
            MaterialTheme {
                if (shown)
                    AllowedInterceptAppsPicker(
                        checked,
                        onToggleApp = {
                            checked = if (it in checked) checked - it else checked + it
                            onToggle(it)
                        },
                        onDismiss = {
                            shown = false
                            onDismiss()
                        },
                        loadApps = loadApps,
                        loadIcon = { _, _ -> null },
                    )
            }
        }
    }

    private fun app(name: String, pkg: String) = InstalledAppInfo(pkg, name, null)

    private fun text(id: Int) =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    private fun waitForTag(tag: String) {
        rule.waitUntil(5_000) { rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
    }
}
