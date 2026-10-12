package com.example.vibefinance.ui.accounts

import androidx.compose.runtime.saveable.SaverScope
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AssetEditorStateTest {
    @Test
    fun sessionSaverRetainsEveryPrefillFieldAndOrigin() {
        listOf(null, "none", "unavailable.bank.app").forEach { linkedApp ->
            val account =
                AccountEntity(
                    id = 41,
                    name = "銀行 💳",
                    nickname = "旅行",
                    type = AccountType.CC,
                    balance = -13.25,
                    icon = "custom",
                    creditLimit = 9000.0,
                    billingDate = 4,
                    paymentDate = 8,
                    paymentDeadline = 11,
                    cardTheme = "ocean",
                    accentColorKey = "secondary",
                    cardLast4 = "8765",
                    cardProtocol = "unionpay",
                    cardIssuer = "legacy",
                    cardPattern = "neon waves",
                    cardImageUri = "/private/卡片.png",
                    customImageAspectRatio = "16:9",
                    cardBgOffsetX = -.25f,
                    cardBgOffsetY = .3f,
                    cardBgScale = 1.45f,
                    minSpendThreshold = 650.0,
                    linkedAppPackage = linkedApp,
                    notificationAliases = "旅行, 測試",
                )
            val state =
                AssetEditorState().apply {
                    open(prefill = account, paymentId = "payment", tab = "HISTORY")
                }
            val saved = with(AssetEditorState.Saver) { SaverScope { true }.save(state) }
            val restored = AssetEditorState.Saver.restore(requireNotNull(saved))!!
            assertTrue(restored.isOpen)
            assertEquals(state.sessionId, restored.sessionId)
            assertEquals(account, restored.prefillAccount)
            assertEquals("payment", restored.originPaymentId)
            assertEquals("HISTORY", restored.originTab)
        }
    }

    @Test
    fun reopeningCreatesNewIdentityAndClearsOriginAndPrefill() {
        val state =
            AssetEditorState().apply {
                open(
                    prefill =
                        AccountEntity(
                            name = "A",
                            type = AccountType.BANK,
                            balance = 0.0,
                            icon = "bank",
                        ),
                    paymentId = "payment",
                    tab = "DAILY",
                )
            }
        val firstId = state.sessionId
        state.close()
        assertFalse(state.isOpen)
        state.open(accountId = 55)
        assertNotEquals(firstId, state.sessionId)
        assertEquals(55L, state.accountId)
        assertNull(state.prefillAccount)
        assertNull(state.originPaymentId)
        assertNull(state.originTab)
    }
}
