package com.example.vibefinance.ui.accounts

import androidx.lifecycle.ViewModelStore
import com.example.vibefinance.data.DataSeeder
import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.data.entity.*
import com.example.vibefinance.data.repository.*
import com.example.vibefinance.ui.*
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AssetEditorSaveOperationTest {
    private val accounts = AccountRepository()
    private lateinit var vm: FinanceViewModel
    private val store = ViewModelStore()
    private val context
        get() = RuntimeEnvironment.getApplication()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        InMemoryDatabase.initialize(context)
        InMemoryDatabase.clearAllTables()
        val transactions = TransactionRepository()
        val budgets = BudgetRepository()
        val subscriptions = SubscriptionRepository(transactions)
        vm =
            FinanceViewModel(
                accounts,
                transactions,
                budgets,
                subscriptions,
                DataSeeder(accounts, transactions, budgets, subscriptions),
                context,
            )
        store.put("fixture", vm)
    }

    @After
    fun cleanUp() {
        store.clear()
        File(context.filesDir, "vibefinance_data.json.new").deleteRecursively()
        InMemoryDatabase.clearAllTables()
        Dispatchers.resetMain()
    }

    private suspend fun awaitResult(predicate: (AccountSaveResult?) -> Boolean) =
        withContext(Dispatchers.Default) {
            withTimeout(5_000) { vm.accountSaveResult.first(predicate) }
        }

    @Test
    fun insertionAcknowledgesActualId_andDuplicateRequestCannotInsertTwice() = runTest {
        val sameNameId = accounts.insertAccount(ACCOUNT)
        val request = FinanceIntent.SaveAccount(ACCOUNT, editorRequestId = "add")
        vm.dispatch(request)
        val result = awaitResult { it is AccountSaveResult.Saved } as AccountSaveResult.Saved
        assertNotEquals(sameNameId, result.account.id)
        assertEquals(result.account, InMemoryDatabase.accounts.value.last())
        vm.dispatch(request)
        assertEquals(2, InMemoryDatabase.accounts.value.size)
    }

    @Test
    fun failureIsObservable_andSameRequestCanRetry() = runTest {
        val blocked = File(context.filesDir, "vibefinance_data.json.new").apply { mkdir() }
        val request = FinanceIntent.SaveAccount(ACCOUNT, editorRequestId = "retry")
        vm.dispatch(request)
        val failed = awaitResult { it is AccountSaveResult.Failed } as AccountSaveResult.Failed
        assertEquals("retry", failed.requestId)
        assertTrue(failed.message.isNotBlank())
        assertTrue(InMemoryDatabase.accounts.value.isEmpty())
        blocked.deleteRecursively()
        vm.dispatch(request)
        val saved = awaitResult { it is AccountSaveResult.Saved } as AccountSaveResult.Saved
        assertEquals(saved.account, InMemoryDatabase.accounts.value.single())
    }

    @Test
    fun unchangedOpeningBalanceDoesNotOverwriteConcurrentLedgerChanges() = runTest {
        val id = accounts.insertAccount(ACCOUNT.copy(balance = 125.0))
        val opening = InMemoryDatabase.accounts.value.single()
        accounts.updateBalance(id, 99.0)
        vm.dispatch(
            FinanceIntent.SaveAccount(opening.copy(nickname = "New nickname"), 125.0, "edit")
        )
        val saved = awaitResult { it is AccountSaveResult.Saved } as AccountSaveResult.Saved
        assertEquals(99.0, saved.account.balance, 0.0)
        assertEquals("New nickname", InMemoryDatabase.accounts.value.single().nickname)
        assertTrue(InMemoryDatabase.transactions.value.isEmpty())
    }

    private companion object {
        val ACCOUNT =
            AccountEntity(name = "Same name", type = AccountType.BANK, balance = 0.0, icon = "bank")
    }
}
