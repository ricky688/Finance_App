package com.example.vibefinance.ui.accounts

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.ui.FinanceIntent
import com.example.vibefinance.ui.FinanceUiState
import java.util.concurrent.CopyOnWriteArrayList
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** ARTEMIS verified the live Add Asset nickname field, keyboard and Back dismissal first.
 * Uses the real IME with explicit visibility waits; all drafts and callbacks stay in memory.
 */
@RunWith(AndroidJUnit4::class)
class AssetEditorPreviewTest {
    @get:Rule val rule = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun addBank_previewFollowsTextEntry() = checkPreview(edit = false, AccountType.BANK)
    @Test fun addCard_previewFollowsTextEntry() = checkPreview(edit = false, AccountType.CC)
    @Test fun editBank_previewFollowsTextEntry() = checkPreview(edit = true, AccountType.BANK)
    @Test fun editCard_previewFollowsTextEntry() = checkPreview(edit = true, AccountType.CC)

    private fun checkPreview(edit: Boolean, type: AccountType) {
        val account = AccountEntity(id = if (edit) 8701 else 0, name = "Preview fixture",
            type = type, balance = 125.0, icon = "credit_card")
        val saves = CopyOnWriteArrayList<FinanceIntent.SaveAccount>()
        rule.setContent {
            MaterialTheme {
                AccountsScreen(
                    state = FinanceUiState(isLoading = false, accounts = if (edit) listOf(account) else emptyList()),
                    initialPrefillAccount = if (edit) null else account,
                    onIntent = { if (it is FinanceIntent.SaveAccount) saves += it }
                )
            }
        }
        if (edit) {
            rule.onNode(hasScrollAction()).performScrollToNode(hasText(account.name))
            rule.onNodeWithText(account.name).performScrollTo().performClick()
        }
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("AssetEditorSheet").fetchSemanticsNodes().size == 1 }
        waitForPreview(visible = true)

        val name = field(R.string.ah_account_card_name)
        name.performScrollTo().performClick()
        waitForPreview(visible = false)
        name.performTextReplacement("旅行 銀行")
        val nickname = field(R.string.assets_identity_nickname)
        nickname.performScrollTo().performClick()
        nickname.performTextReplacement("Travel card")
        waitForPreview(visible = false)

        closeSoftKeyboard()
        waitForPreview(visible = true)
        name.performScrollTo().assertTextContains("旅行 銀行")
        nickname.performScrollTo().assertTextContains("Travel card")

        // A focused field can reopen the IME after Back; numeric fields use the same behavior.
        val digits = field(R.string.assets_identity_last_four)
        digits.performScrollTo().performClick()
        waitForPreview(visible = false)
        digits.performTextReplacement("7890")
        closeSoftKeyboard()
        waitForPreview(visible = true)
        digits.assertTextContains("7890")
        rule.onNodeWithContentDescription(context.getString(R.string.btn_close)).performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("AssetEditorSheet").fetchSemanticsNodes().isEmpty() }
        assertTrue("Typing and cancelling must never save the account", saves.isEmpty())
    }

    private fun field(label: Int) = rule.onNode(hasText(context.getString(label)) and hasSetTextAction())

    private fun waitForPreview(visible: Boolean) {
        rule.waitUntil(8_000) {
            rule.onAllNodesWithTag("AssetEditorPreview").fetchSemanticsNodes().isNotEmpty() == visible
        }
        rule.waitForIdle()
        if (visible) rule.onNodeWithTag("AssetEditorPreview").assertIsDisplayed()
    }
}
