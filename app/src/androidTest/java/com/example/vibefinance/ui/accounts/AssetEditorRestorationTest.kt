package com.example.vibefinance.ui.accounts

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.ui.FinanceIntent
import com.example.vibefinance.ui.FinanceUiState
import java.util.concurrent.CopyOnWriteArrayList
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** ARTEMIS/ADB explored Add Asset, scroll-edge sheet dismissal and real rotation first.
 * Fixtures and save callbacks stay in memory; these checks never write the user's ledger.
 */
@RunWith(AndroidJUnit4::class)
class AssetEditorRestorationTest {
    @get:Rule val rule = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val saves = CopyOnWriteArrayList<FinanceIntent.SaveAccount>()
    private fun text(id: Int) = context.getString(id)

    private fun show(edit: Boolean): StateRestorationTester {
        val restoration = StateRestorationTester(rule)
        restoration.setContent {
            MaterialTheme {
                AccountsScreen(
                    state = FinanceUiState(isLoading = false, accounts = if (edit) listOf(CARD) else emptyList()),
                    onIntent = { if (it is FinanceIntent.SaveAccount) saves += it }
                )
            }
        }
        val label = if (edit) CARD.name else text(R.string.ah_add_asset)
        rule.onNode(hasScrollAction()).performScrollToNode(hasText(label))
        rule.onNodeWithText(label).performScrollTo().performClick()
        waitForSheet()
        return restoration
    }

    private fun waitForSheet() {
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("AssetEditorSheet").fetchSemanticsNodes().size == 1 }
        rule.waitForIdle()
    }

    private fun field(label: Int): SemanticsNodeInteraction = rule.onNode(hasText(text(label)) and hasSetTextAction())
    private fun replace(label: Int, value: String) {
        field(label).performScrollTo().performTextReplacement(value)
        rule.waitForIdle()
    }
    private fun checkField(label: Int, value: String) {
        field(label).performScrollTo().assertTextContains(value)
    }
    private fun tab(label: Int) {
        rule.onNode(hasText(text(label)) and hasClickAction()).performClick()
        rule.waitForIdle()
    }
    private fun networkField() = rule.onNode(hasText(text(R.string.ah_protocol)) and
        SemanticsMatcher.keyIsDefined(SemanticsProperties.EditableText))
    private fun selectNetwork() {
        tab(R.string.ah_tab_design)
        networkField().performScrollTo().performClick()
        rule.onNode(hasText("UnionPay") and hasClickAction()).performClick()
        rule.waitForIdle()
    }

    @Test fun newDraft_survivesSavedStateRestore_includingSelectedTab_andSavesOnce() {
        val restoration = show(edit = false)
        replace(R.string.ah_account_card_name, "Rotation 銀行")
        replace(R.string.assets_identity_nickname, "旅行 💳")
        replace(R.string.assets_identity_last_four, "7890")
        replace(R.string.ah_current_balance_amount, "1234.56")
        selectNetwork()
        restoration.emulateSavedInstanceStateRestore()
        waitForSheet()
        networkField().performScrollTo().assertTextContains("UnionPay") // Design tab is restored too.
        tab(R.string.ah_tab_details)
        checkField(R.string.ah_account_card_name, "Rotation 銀行")
        checkField(R.string.assets_identity_nickname, "旅行 💳")
        checkField(R.string.assets_identity_last_four, "7890")
        checkField(R.string.ah_current_balance_amount, "1234.56")
        assertTrue(saves.isEmpty())
        rule.onNodeWithText(text(R.string.ah_create_asset)).performClick()
        rule.waitUntil(5_000) { saves.size == 1 }
        val save = saves.single()
        assertEquals(0L, save.account.id)
        assertEquals("Rotation 銀行", save.account.name)
        assertEquals("旅行 💳", save.account.nickname)
        assertEquals(AccountType.BANK, save.account.type)
        assertEquals(1234.56, save.account.balance, 0.0)
        assertEquals("7890", save.account.cardLast4)
        assertEquals("unionpay", save.account.cardProtocol)
        assertNull(save.originalBalance)
    }

    @Test fun editDraft_survivesSavedStateRestore_withoutLosingAccountIdentityOrCustomization() {
        val restoration = show(edit = true)
        replace(R.string.ah_account_card_name, "Edited 卡")
        replace(R.string.assets_identity_nickname, "New nickname")
        replace(R.string.assets_identity_last_four, "9876")
        selectNetwork()
        restoration.emulateSavedInstanceStateRestore()
        waitForSheet()
        networkField().performScrollTo().assertTextContains("UnionPay")
        tab(R.string.ah_tab_details)
        checkField(R.string.ah_account_card_name, "Edited 卡")
        checkField(R.string.assets_identity_nickname, "New nickname")
        checkField(R.string.assets_identity_last_four, "9876")
        assertTrue(saves.isEmpty())
        rule.onNodeWithText(text(R.string.btn_save_changes)).performClick()
        rule.waitUntil(5_000) { saves.size == 1 }
        val save = saves.single()
        assertEquals(CARD.copy(name = "Edited 卡", nickname = "New nickname", cardLast4 = "9876",
            cardProtocol = "unionpay"), save.account)
        assertEquals(CARD.balance, save.originalBalance!!, 0.0)
    }

    @Test fun addEditor_edgeDragsKeepSheetBoundsFixed_andCancelDoesNotSave() = checkEdgeDrags(false)
    @Test fun editEditor_edgeDragsKeepSheetBoundsFixed_andCancelDoesNotSave() = checkEdgeDrags(true)

    private fun checkEdgeDrags(edit: Boolean) {
        show(edit)
        val sheet = rule.onNodeWithTag("AssetEditorSheet")
        val scroll = rule.onNodeWithTag("AssetEditorScrollContent")
        val bounds = sheet.fetchSemanticsNode().boundsInRoot
        listOf(false, true).forEach { bottom ->
            scroll.performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, if (bottom) 100_000f else -100_000f) }
            rule.waitForIdle()
            scroll.performTouchInput {
                val start = Offset(width * 0.95f, if (bottom) height * 0.8f else height * 0.2f)
                val end = Offset(start.x, if (bottom) height * 0.2f else height * 0.8f)
                down(start)
                moveTo(end, delayMillis = 350)
            }
            rule.waitForIdle()
            assertEquals("The sheet must stay fixed during an edge drag", bounds, sheet.fetchSemanticsNode().boundsInRoot)
            scroll.performTouchInput { up() }
            rule.waitForIdle()
            assertEquals(bounds, sheet.fetchSemanticsNode().boundsInRoot)
        }
        rule.onNodeWithContentDescription(text(R.string.btn_close)).performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("AssetEditorSheet").fetchSemanticsNodes().isEmpty() }
        assertTrue(saves.isEmpty())
    }

    private companion object {
        val CARD = AccountEntity(id = 8101, name = "Rotation card fixture", type = AccountType.CC,
            balance = 123.0, icon = "credit_card", nickname = "Old nickname", creditLimit = 9000.0,
            billingDate = 5, paymentDate = 20, paymentDeadline = 23,
            cardTheme = "default", accentColorKey = "secondary", cardLast4 = "1234",
            cardProtocol = "visa", cardIssuer = "hsbc", cardPattern = "neon waves",
            customImageAspectRatio = "1.586:1", cardBgOffsetX = 0.25f, cardBgOffsetY = -0.2f,
            cardBgScale = 1.2f, minSpendThreshold = 600.0, linkedAppPackage = "none",
            notificationAliases = "銀聯, RotationAlias")
    }
}
