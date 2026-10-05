package com.example.vibefinance.ui.accounts

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.ui.FinanceIntent
import com.example.vibefinance.ui.FinanceUiState
import java.util.concurrent.CopyOnWriteArrayList
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Card Design and its payment-network menu were explored with ARTEMIS on Waydroid first. */
@RunWith(AndroidJUnit4::class)
class CardPaymentNetworkEditorTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun unionPayAndJcb_saveAndReopenWithTheNetworkSelected_withoutAnIssuerEditor() {
        val bank = AccountEntity(
            id = 701,
            name = "Network editor bank fixture",
            type = AccountType.BANK,
            balance = 500.0,
            icon = "bank"
        )
        val initialCard = AccountEntity(
            id = 702,
            name = CARD_NAME,
            type = AccountType.CC,
            balance = 125.0,
            icon = "cc",
            creditLimit = 5000.0,
            cardProtocol = "visa",
            cardIssuer = "hsbc"
        )
        var state by mutableStateOf(FinanceUiState(isLoading = false, accounts = listOf(bank, initialCard)))
        val savedCards = CopyOnWriteArrayList<AccountEntity>()
        rule.setContent {
            MaterialTheme {
                AccountsScreen(
                    state = state,
                    onIntent = { intent ->
                        if (intent is FinanceIntent.SaveAccount) {
                            savedCards += intent.account
                            state = state.copy(accounts = state.accounts.map { account ->
                                if (account.id == intent.account.id) intent.account else account
                            })
                        }
                    }
                )
            }
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val designTab = context.getString(R.string.ah_tab_design)
        val protocolLabel = context.getString(R.string.ah_protocol)
        val issuerLabel = context.getString(R.string.ah_issuer)
        val editorTitle = context.getString(R.string.ah_modify_account, CARD_NAME)
        // Read-only Material text fields may omit SetText, but still expose EditableText.
        // Both alternatives identify the field rather than its repeated section heading.
        val protocolField = hasText(protocolLabel) and (
            hasSetTextAction() or SemanticsMatcher.keyIsDefined(SemanticsProperties.EditableText)
        )

        fun waitFor(matcher: SemanticsMatcher) {
            rule.waitUntil(TIMEOUT_MILLIS) {
                rule.onAllNodes(matcher).fetchSemanticsNodes().size == 1
            }
        }

        fun openCardDesign(expectedNetwork: String) {
            waitFor(hasScrollAction())
            // LazyColumn must compose a card below the bank before its name can be clicked.
            rule.onNode(hasScrollAction()).performScrollToNode(hasText(CARD_NAME))
            rule.onNodeWithText(CARD_NAME).performScrollTo().performClick()
            waitFor(hasText(editorTitle))
            waitFor(hasText(designTab) and hasClickAction())
            rule.onNode(hasText(designTab) and hasClickAction()).assertIsDisplayed().performClick()
            waitFor(protocolField)
            rule.onNode(protocolField).performScrollTo()
                .assertIsDisplayed().assertTextContains(expectedNetwork)
            rule.onNodeWithText(issuerLabel, useUnmergedTree = true).assertDoesNotExist()
        }

        fun waitForEditorToClose() {
            rule.waitUntil(TIMEOUT_MILLIS) {
                rule.onAllNodesWithText(editorTitle).fetchSemanticsNodes().isEmpty()
            }
        }

        openCardDesign("Visa")
        listOf("UnionPay" to "unionpay", "JCB" to "jcb").forEachIndexed { index, (label, canonical) ->
            rule.onNode(protocolField).performClick()
            waitFor(hasText(label) and hasClickAction())
            rule.onNode(hasText(label) and hasClickAction()).assertIsDisplayed().performClick()
            rule.onNode(protocolField).assertTextContains(label)
            rule.onNodeWithText(context.getString(R.string.btn_save_changes)).assertIsDisplayed().performClick()
            rule.waitUntil(TIMEOUT_MILLIS) { savedCards.size == index + 1 }
            val saved = savedCards[index]
            assertEquals(initialCard.id, saved.id)
            assertEquals(canonical, saved.cardProtocol)
            assertEquals("Legacy issuer data must survive removal of its editor", initialCard.cardIssuer, saved.cardIssuer)
            assertEquals(initialCard.balance, saved.balance, 0.0)
            assertEquals(bank, state.accounts.first { it.id == bank.id })
            waitForEditorToClose()
            openCardDesign(label)
        }

        // End the regression without leaving an open editor or issuing an extra save.
        rule.onNodeWithContentDescription(context.getString(R.string.btn_close)).performClick()
        waitForEditorToClose()
        assertEquals(2, savedCards.size)
    }

    private companion object {
        const val CARD_NAME = "Payment network card fixture"
        const val TIMEOUT_MILLIS = 5_000L
    }
}
