package com.example.vibefinance.ui.accounts

import android.view.View
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsCompat
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.*
import com.example.vibefinance.ui.*
import java.util.concurrent.CopyOnWriteArrayList
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ARTEMIS hierarchy/screenshots + ADB verified Add, keyboard-first Back, discard/keep, natural
 * scrolling and system photo-picker cancellation on Waydroid 2026-10-11. Every mutation below is an
 * in-memory callback, never the installed user's ledger.
 */
@RunWith(AndroidJUnit4::class)
class AssetEditorContractTest {
    @get:Rule val rule = createComposeRule()
    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var editor: AssetEditorState
    private lateinit var view: View
    private var result by mutableStateOf<AccountSaveResult?>(null)
    private val requests = CopyOnWriteArrayList<FinanceIntent>()
    private val created = CopyOnWriteArrayList<AccountEntity>()
    private var cancellations = 0

    private fun text(id: Int) = context.getString(id)

    private fun field(id: Int) = rule.onNode(hasText(text(id)) and hasSetTextAction())

    private fun waitOpen(open: Boolean = true) {
        rule.waitUntil(8_000) {
            rule.onAllNodesWithTag("AssetEditorSheet").fetchSemanticsNodes().isNotEmpty() == open
        }
        rule.waitForIdle()
    }

    private fun show(
        account: AccountEntity? = null,
        wide: Boolean = false,
        large: Boolean = false,
        dark: Boolean = false,
    ): StateRestorationTester {
        val restoration = StateRestorationTester(rule)
        restoration.setContent {
            editor = rememberAssetEditorState()
            view = LocalView.current
            LaunchedEffect(Unit) {
                if (!editor.isOpen)
                    editor.open(
                        accountId = account?.id,
                        prefill = if (account == null) PREFILL else null,
                        paymentId = "payment-42",
                        tab = "HISTORY",
                    )
            }
            val deviceDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides
                    if (wide) Density(0.5f, if (large) 1.5f else 1f)
                    else
                        Density(deviceDensity.density, if (large) 1.5f else deviceDensity.fontScale)
            ) {
                MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                    Box(Modifier.fillMaxSize()) {
                        AssetEditorHost(
                            FinanceUiState(isLoading = false, accounts = listOfNotNull(account)),
                            editor,
                            { requests += it },
                            result,
                            { created += it },
                            { cancellations++ },
                        )
                    }
                }
            }
        }
        waitOpen()
        return restoration
    }

    @Test
    fun unchangedClose_dismissesImmediately_andReturnsContext() {
        show()
        assertEquals("payment-42", editor.originPaymentId)
        assertEquals("HISTORY", editor.originTab)
        rule.onNodeWithContentDescription(text(R.string.btn_close)).performClick()
        waitOpen(false)
        assertEquals(1, cancellations)
        assertTrue(requests.isEmpty())
    }

    @Test
    fun dirtyClose_keepEditing_thenDiscard_neverSaves() {
        show()
        field(R.string.ah_account_card_name).performScrollTo().performTextReplacement("旅行 💳")
        rule.onNodeWithContentDescription(text(R.string.btn_close)).performClick()
        rule.onNodeWithTag("AssetEditorKeepEditing").performClick()
        field(R.string.ah_account_card_name).assertTextContains("旅行 💳")
        rule.onNodeWithContentDescription(text(R.string.btn_close)).performClick()
        rule.onNodeWithTag("AssetEditorDiscard").performClick()
        waitOpen(false)
        assertEquals(1, cancellations)
        assertTrue(requests.isEmpty())
    }

    @Test
    fun systemBack_closesImeFirst_thenConfirmsDirtyDraft() {
        show()
        field(R.string.ah_account_card_name)
            .performScrollTo()
            .performClick()
            .performTextReplacement("Keyboard draft")
        rule.waitUntil(8_000) {
            WindowInsetsCompat.toWindowInsetsCompat(view.rootWindowInsets)
                .isVisible(WindowInsetsCompat.Type.ime())
        }
        rule.onNodeWithTag("AssetEditorPreview").assertExists()
        pressBack()
        rule.waitUntil(8_000) {
            !WindowInsetsCompat.toWindowInsetsCompat(view.rootWindowInsets)
                .isVisible(WindowInsetsCompat.Type.ime())
        }
        rule.onNodeWithTag("AssetEditorSheet").assertExists()
        pressBack()
        rule.onNodeWithTag("AssetEditorDiscard").assertIsDisplayed().performClick()
        waitOpen(false)
    }

    @Test
    fun saveIsAcknowledged_noDuplicate_failureKeepsDraft_retryReturnsActualIdOnce() {
        show()
        field(R.string.ah_account_card_name).performScrollTo().performTextReplacement("Same name")
        val button = rule.onNodeWithTag("AssetEditorSave")
        button.performSemanticsAction(SemanticsActions.OnClick) { click ->
            click()
            click()
        }
        rule.waitUntil(5_000) { requests.size == 1 }
        button.assertIsNotEnabled().performClick()
        assertEquals(1, requests.size)
        val first = requests.single() as FinanceIntent.SaveAccount
        rule.runOnIdle {
            result = AccountSaveResult.Failed(first.editorRequestId!!, "Storage unavailable")
        }
        rule.onNodeWithTag("AssetEditorSaveError").assertTextContains("Storage unavailable")
        field(R.string.ah_account_card_name).assertTextContains("Same name")
        button.assertIsEnabled().performClick()
        rule.waitUntil(5_000) { requests.size == 2 }
        val retry = requests.last() as FinanceIntent.SaveAccount
        assertNotEquals(first.editorRequestId, retry.editorRequestId)
        val saved = retry.account.copy(id = 98234)
        rule.runOnIdle { result = AccountSaveResult.Saved(retry.editorRequestId!!, saved) }
        waitOpen(false)
        assertEquals(listOf(saved), created)
        assertEquals(0, cancellations)
    }

    @Test
    fun sessionPrefillAndNotificationOrigin_surviveRestoration() {
        val restoration = show()
        val id = editor.sessionId
        field(R.string.assets_identity_nickname)
            .performScrollTo()
            .performTextReplacement("Restored nickname")
        restoration.emulateSavedInstanceStateRestore()
        waitOpen()
        assertEquals(id, editor.sessionId)
        assertEquals(PREFILL, editor.prefillAccount)
        assertEquals("payment-42", editor.originPaymentId)
        assertEquals("HISTORY", editor.originTab)
        field(R.string.assets_identity_nickname)
            .performScrollTo()
            .assertTextContains("Restored nickname")
        rule.onNodeWithContentDescription(text(R.string.btn_close)).performClick()
        rule.onNodeWithTag("AssetEditorDiscard").assertIsDisplayed()
    }

    @Test
    fun deleteIsEditOnly_andRequiresConfirmation() {
        show(PREFILL.copy(id = 72))
        rule.onNodeWithText(text(R.string.btn_delete)).performScrollTo().performClick()
        assertTrue(requests.isEmpty())
        rule.onNodeWithText(text(R.string.ah_delete_account_title)).assertIsDisplayed()
        rule.onNodeWithTag("AssetEditorDeleteConfirm").performClick()
        waitOpen(false)
        assertEquals(72L, (requests.single() as FinanceIntent.DeleteAccount).account.id)
    }

    @Test
    fun wideDarkLargeText_previewHasSeparatePane_whileFormScrolls() {
        show(PREFILL.copy(id = 73), wide = true, large = true, dark = true)
        rule.onNodeWithTag("AssetEditorPreviewPane").assertWidthIsEqualTo(260.dp)
        rule.onNodeWithTag("AssetEditorPreview").assertHeightIsEqualTo(160.dp)
        val before = rule.onNodeWithTag("AssetEditorPreview").fetchSemanticsNode().boundsInRoot
        rule.onNodeWithTag("AssetEditorScrollContent").performSemanticsAction(
            SemanticsActions.ScrollBy
        ) {
            it(0f, 100_000f)
        }
        rule.waitForIdle()
        assertEquals(
            before,
            rule.onNodeWithTag("AssetEditorPreview").fetchSemanticsNode().boundsInRoot,
        )
        rule.onNodeWithTag("AssetEditorSave").assertIsDisplayed()
    }

    @Test
    fun photoPanZoom_surviveRestoration_andAreIncludedInSave() {
        val file = java.io.File(context.cacheDir, "asset-editor-photo-fixture.png")
        val bitmap =
            android.graphics.Bitmap.createBitmap(64, 64, android.graphics.Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(android.graphics.Color.BLUE)
        file.outputStream().use {
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
        try {
            val account =
                PREFILL.copy(
                    id = 74,
                    cardImageUri = file.absolutePath,
                    cardBgOffsetX = 0f,
                    cardBgOffsetY = 0f,
                    cardBgScale = 1f,
                )
            val restoration = show(account)
            rule.onNodeWithTag("AssetEditorPreview").performTouchInput {
                pinch(
                    androidx.compose.ui.geometry.Offset(width * .35f, height * .5f),
                    androidx.compose.ui.geometry.Offset(width * .65f, height * .5f),
                    androidx.compose.ui.geometry.Offset(width * .2f, height * .5f),
                    androidx.compose.ui.geometry.Offset(width * .8f, height * .5f),
                    durationMillis = 600,
                )
            }
            rule.onNodeWithTag("AssetEditorPreview").performTouchInput {
                swipe(
                    androidx.compose.ui.geometry.Offset(width * .45f, height * .5f),
                    androidx.compose.ui.geometry.Offset(width * .6f, height * .55f),
                    durationMillis = 400,
                )
            }
            restoration.emulateSavedInstanceStateRestore()
            waitOpen()
            rule.onNodeWithTag("AssetEditorPreview").assertExists()
            rule.onNodeWithTag("AssetEditorSave").performClick()
            rule.waitUntil(5_000) { requests.size == 1 }
            val saved = (requests.single() as FinanceIntent.SaveAccount).account
            assertEquals(file.absolutePath, saved.cardImageUri)
            assertTrue("Pinch zoom must survive restoration", saved.cardBgScale > 1f)
            assertTrue("Photo pan must survive restoration", saved.cardBgOffsetX > 0f)
        } finally {
            file.delete()
        }
    }

    private companion object {
        val PREFILL =
            AccountEntity(
                name = "Notification bank",
                type = AccountType.BANK,
                balance = 0.0,
                icon = "bank",
                cardLast4 = "1234",
                linkedAppPackage = "not.installed.bank",
                notificationAliases = "旅行, 商店",
                cardBgScale = 1.25f,
            )
    }
}
