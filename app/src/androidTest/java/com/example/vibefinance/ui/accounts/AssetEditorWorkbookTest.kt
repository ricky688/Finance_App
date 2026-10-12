package com.example.vibefinance.ui.accounts

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.*
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.util.FinancialDataImportEngine
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Read-only workbook navigation: production importer with memory-only AccountsScreen state. */
@RunWith(AndroidJUnit4::class)
class AssetEditorWorkbookTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun importedAccountsOpenAndCancel_withoutChangingRecords() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        assumeTrue(InstrumentationRegistry.getArguments().getString("assetWorkbook") == "true")
        val context = instrumentation.targetContext
        val descriptor = instrumentation.uiAutomation.executeShellCommand("cat /data/local/tmp/asset-editor-test.xlsx")
        val imported = android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).use {
            FinancialDataImportEngine.parseStream(it, "asset-editor-test.xlsx")
        }
        assertNull(imported.error)
        assertEquals(868, imported.rawTransactions.size)
        assertEquals(14, imported.detectedAccounts.size)
        val accounts =
            imported.detectedAccounts.mapIndexed { index, raw ->
                AccountEntity(
                    id = index + 1L,
                    name = raw.name,
                    type = raw.detectedType,
                    balance = 0.0,
                    icon = "wallet",
                )
            }
        val ids = accounts.associate { it.name to it.id }
        val rows =
            imported.rawTransactions.mapIndexed { index, raw ->
                TransactionEntity(
                    index + 1L,
                    raw.amount,
                    raw.category,
                    raw.timestamp,
                    ids.getValue(raw.sourceAccountName),
                    if (raw.isTransfer) ids[raw.destinationAccountName] else null,
                    description = raw.description,
                )
            }
        assertEquals(
            52831.49,
            rows.filter { it.amount > 0 && it.toAccountId == null }.sumOf { it.amount },
            .001,
        )
        var mutations = 0
        rule.setContent {
            MaterialTheme {
                AccountsScreen(
                    FinanceUiState(isLoading = false, accounts = accounts, transactions = rows),
                    { mutations++ },
                )
            }
        }
        val target = accounts.first()
        rule.onNode(hasScrollAction()).performScrollToNode(hasText(target.name))
        rule.onNodeWithText(target.name).performScrollTo().performClick()
        rule.waitUntil(8_000) {
            rule.onAllNodesWithTag("AssetEditorSheet").fetchSemanticsNodes().size == 1
        }
        rule
            .onNode(
                hasText(context.getString(R.string.ah_account_card_name)) and hasSetTextAction()
            )
            .performScrollTo()
            .assertTextContains(target.name)
        rule.onNodeWithTag("AssetEditorPreview").assertExists()
        rule.onNodeWithContentDescription(context.getString(R.string.btn_close)).performClick()
        rule.waitUntil(8_000) {
            rule.onAllNodesWithTag("AssetEditorSheet").fetchSemanticsNodes().isEmpty()
        }
        assertEquals(0, mutations)
        assertEquals(14, accounts.size)
        assertEquals(868, rows.size)
    }
}
