package com.example.vibefinance.ui.accounts

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vibefinance.R
import com.example.vibefinance.util.InstalledAppInfo
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Modify Wallet's picker, search and selection path was explored with ARTEMIS and ADB on Waydroid. */
@RunWith(AndroidJUnit4::class)
class AppPickerDialogTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun pendingCatalog_showsLoadingAndSearchFiltersTheCompletedCatalog() {
        val catalog = CompletableDeferred<List<InstalledAppInfo>>()
        val started = AtomicBoolean(false)
        val octopus = app("Octopus (Test)", "com.example.octopus")
        val calculator = app("Calculator", "com.example.calculator")
        showPicker(loadApps = {
            started.set(true)
            catalog.await()
        })

        rule.waitUntil(TIMEOUT_MILLIS) { started.get() }
        waitForTag(LOADING_TAG)
        rule.onNodeWithText(text(R.string.assets_linked_app_dialog_title)).assertIsDisplayed()
        rule.onNodeWithTag(LOADING_TAG).assertIsDisplayed()
        rule.onNodeWithText(text(R.string.assets_linked_app_empty)).assertDoesNotExist()

        rule.onNodeWithTag(SEARCH_TAG).performTextReplacement("octopus")
        rule.onNodeWithTag(SEARCH_TAG).assertTextContains("octopus")
        rule.onNodeWithTag(LOADING_TAG).assertIsDisplayed()
        rule.onNodeWithText(text(R.string.assets_linked_app_empty)).assertDoesNotExist()

        catalog.complete(listOf(octopus, calculator))
        waitForText(octopus.appName)
        waitForTagToDisappear(LOADING_TAG)
        rule.onNodeWithText(octopus.appName).assertIsDisplayed()
        rule.onNodeWithText(calculator.appName).assertDoesNotExist()
        rule.onNodeWithText(text(R.string.assets_linked_app_empty)).assertDoesNotExist()
    }

    @Test
    fun cancelDuringLoading_dismissesAndCancelsTheCatalogLoad() {
        val catalog = CompletableDeferred<List<InstalledAppInfo>>()
        val started = AtomicBoolean(false)
        val cancelled = AtomicBoolean(false)
        val dismissals = AtomicInteger(0)
        val selections = AtomicInteger(0)
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
            onAppSelected = { selections.incrementAndGet() },
            onDismiss = { dismissals.incrementAndGet() }
        )

        rule.waitUntil(TIMEOUT_MILLIS) { started.get() }
        waitForTag(LOADING_TAG)
        rule.onNodeWithText(text(android.R.string.cancel)).performClick()

        rule.waitUntil(TIMEOUT_MILLIS) { cancelled.get() && dismissals.get() == 1 }
        waitForTagToDisappear(SEARCH_TAG)
        rule.onNodeWithTag(LOADING_TAG).assertDoesNotExist()
        rule.onNodeWithText(text(R.string.assets_linked_app_dialog_title)).assertDoesNotExist()
        assertEquals(0, selections.get())
    }

    @Test
    fun selectingAnApp_callsSelectionOnceAndDismissesTheDialog() {
        val octopus = app("Octopus (Test)", "com.example.octopus")
        val selection = AtomicReference<String?>(null)
        val selections = AtomicInteger(0)
        val dismissals = AtomicInteger(0)
        showPicker(
            loadApps = { listOf(octopus) },
            onAppSelected = {
                selection.set(it)
                selections.incrementAndGet()
            },
            onDismiss = { dismissals.incrementAndGet() }
        )

        waitForText(octopus.appName)
        rule.onNodeWithText(octopus.appName).assertIsDisplayed().performClick()

        rule.waitUntil(TIMEOUT_MILLIS) { dismissals.get() == 1 }
        waitForTagToDisappear(SEARCH_TAG)
        assertEquals(octopus.packageName, selection.get())
        assertEquals(1, selections.get())
        rule.onNodeWithText(text(R.string.assets_linked_app_dialog_title)).assertDoesNotExist()
    }

    @Test
    fun largeCatalog_requestsIconsOnlyAsRowsAreComposed() {
        val apps = List(150) { index ->
            app("Mock App ${index.toString().padStart(3, '0')}", "com.example.mockapp$index")
        }
        val requestedIcons = ConcurrentHashMap.newKeySet<String>()
        val icons = CompletableDeferred<Bitmap?>()
        showPicker(
            loadApps = { apps },
            loadIcon = { _, packageName ->
                requestedIcons.add(packageName)
                icons.await()
            }
        )

        waitForText(apps.first().appName)
        rule.waitUntil(TIMEOUT_MILLIS) { apps.first().packageName in requestedIcons }
        rule.waitForIdle()
        rule.onNodeWithTag(LIST_TAG).assertIsDisplayed()
        rule.onNodeWithText(apps.first().appName).assertIsDisplayed()
        assertTrue("The picker must not request the entire icon catalog", requestedIcons.size < apps.size)
        assertFalse("An offscreen app must not load its icon yet", apps.last().packageName in requestedIcons)

        // A package search brings a distant app into composition without walking every list row.
        rule.onNodeWithTag(SEARCH_TAG).performTextReplacement(apps.last().packageName)
        waitForText(apps.last().appName)
        rule.waitUntil(TIMEOUT_MILLIS) { apps.last().packageName in requestedIcons }
        rule.onNodeWithText(apps.last().appName).assertIsDisplayed()
        rule.onNodeWithText(apps.first().appName).assertDoesNotExist()
        assertTrue("Filtering must not trigger icons for the whole catalog", requestedIcons.size < apps.size)
    }

    @Test
    fun catalogFailure_showsRetryAndRetryLoadsTheApps() {
        val attempts = AtomicInteger(0)
        val retryCatalog = CompletableDeferred<List<InstalledAppInfo>>()
        val octopus = app("Octopus (Test)", "com.example.octopus")
        showPicker(loadApps = {
            if (attempts.incrementAndGet() == 1) {
                throw IllegalStateException("Injected catalog failure")
            }
            retryCatalog.await()
        })

        waitForText(text(R.string.assets_linked_app_retry))
        rule.onNodeWithText(text(R.string.assets_linked_app_load_failed)).assertIsDisplayed()
        rule.onNodeWithText(text(R.string.assets_linked_app_empty)).assertDoesNotExist()
        rule.onNodeWithText(text(R.string.assets_linked_app_retry)).performClick()

        rule.waitUntil(TIMEOUT_MILLIS) { attempts.get() == 2 }
        waitForTag(LOADING_TAG)
        rule.onNodeWithText(text(R.string.assets_linked_app_load_failed)).assertDoesNotExist()
        rule.onNodeWithText(text(R.string.assets_linked_app_empty)).assertDoesNotExist()
        retryCatalog.complete(listOf(octopus))

        waitForText(octopus.appName)
        waitForTagToDisappear(LOADING_TAG)
        rule.onNodeWithText(octopus.appName).assertIsDisplayed()
        rule.onNodeWithText(text(R.string.assets_linked_app_retry)).assertDoesNotExist()
        assertEquals(2, attempts.get())
    }

    private fun showPicker(
        loadApps: suspend (Context) -> List<InstalledAppInfo>,
        loadIcon: suspend (Context, String) -> Bitmap? = { _, _ -> null },
        onAppSelected: (String?) -> Unit = {},
        onDismiss: () -> Unit = {}
    ) {
        rule.setContent {
            var isOpen by remember { mutableStateOf(true) }
            MaterialTheme {
                if (isOpen) {
                    AppPickerDialog(
                        currentPackage = null,
                        onAppSelected = onAppSelected,
                        onDismissRequest = {
                            isOpen = false
                            onDismiss()
                        },
                        loadApps = loadApps,
                        loadIcon = loadIcon
                    )
                }
            }
        }
    }

    private fun waitForTag(tag: String) {
        rule.waitUntil(TIMEOUT_MILLIS) {
            rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForTagToDisappear(tag: String) {
        rule.waitUntil(TIMEOUT_MILLIS) {
            rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isEmpty()
        }
    }

    private fun waitForText(value: String) {
        rule.waitUntil(TIMEOUT_MILLIS) {
            rule.onAllNodesWithText(value).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun text(resourceId: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resourceId)

    private fun app(name: String, packageName: String) = InstalledAppInfo(
        packageName = packageName,
        appName = name,
        iconBitmap = null
    )

    private companion object {
        const val TIMEOUT_MILLIS = 5_000L
        const val SEARCH_TAG = "QuickLaunchAppSearch"
        const val LIST_TAG = "QuickLaunchAppList"
        const val LOADING_TAG = "QuickLaunchAppLoading"
    }
}
