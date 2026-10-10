package com.example.vibefinance.ui.main

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.service.PendingPayment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PendingPaymentChoiceDialogTest {

    @get:Rule
    val compose = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun unmatchedPaymentDisplaysBannerAndTriggersQuickAndDetailCreate() {
        val payment = PendingPayment(
            id = "test-p1",
            fingerprint = "fp-p1",
            sourcePackage = "com.hsbc.hbap.mobilebanking",
            assetHint = "HSBC",
            merchant = "PARKnSHOP",
            amount = 88.50,
            detectedAt = System.currentTimeMillis(),
            cardLast4 = "1691",
            transactionType = "EXPENSE"
        )
        // Existing account that does not match the payment
        val accounts = listOf(
            AccountEntity(id = 1L, name = "Cash Wallet", type = AccountType.CASH, balance = 200.0, icon = "wallet")
        )

        var quickCreatedAccount: AccountEntity? = null
        var detailCreatedAccount: AccountEntity? = null

        compose.setContent {
            MaterialTheme {
                PendingPaymentChoiceDialog(
                    payment = payment,
                    accounts = accounts,
                    saving = false,
                    onRecord = { _, _ -> },
                    onQuickCreate = { quickCreatedAccount = it },
                    onDetailCreate = { detailCreatedAccount = it },
                    onLater = {},
                    onIgnore = {}
                )
            }
        }

        // Verify unmatched card banner title and card name appear
        val bannerTitle = context.getString(R.string.pending_payment_unmatched_card_title)
        val quickCreateLabel = context.getString(R.string.pending_payment_quick_create)
        val detailCreateLabel = context.getString(R.string.pending_payment_detail_create)
        val addAssetLabel = context.getString(R.string.pending_payment_add_new_asset)

        compose.onNodeWithText(bannerTitle).assertIsDisplayed()
        compose.onNodeWithText("HSBC (•••• 1691)").assertIsDisplayed()

        // Test Quick Create click
        compose.onNodeWithText(quickCreateLabel).assertIsDisplayed().performClick()
        assertNotNull(quickCreatedAccount)
        assertEquals("HSBC (•••• 1691)", quickCreatedAccount?.name)
        assertEquals(AccountType.CC, quickCreatedAccount?.type)
        assertEquals("1691", quickCreatedAccount?.cardLast4)
        assertEquals("com.hsbc.hbap.mobilebanking", quickCreatedAccount?.linkedAppPackage)

        // Test Detail Create click
        compose.onNodeWithText(detailCreateLabel).assertIsDisplayed().performClick()
        assertNotNull(detailCreatedAccount)
        assertEquals("HSBC (•••• 1691)", detailCreatedAccount?.name)
        assertEquals(AccountType.CC, detailCreatedAccount?.type)

        // Test Add asset button click
        detailCreatedAccount = null
        compose.onNodeWithText(addAssetLabel).assertIsDisplayed().performClick()
        assertNotNull(detailCreatedAccount)
    }
}
