package com.example.vibefinance.ui.history

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
class BatchOperationResultTest {
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

    private suspend fun completed(id: String) = withContext(Dispatchers.Default) {
        withTimeout(5_000) { vm.batchResult.first { it?.requestId == id && !it.processing }!! }
    }

    @Test fun undoAcknowledgesRequestAndDuplicateDeliveryDoesNotReapplyBalances() = runTest {
        val accountId = accounts.insertAccount(AccountEntity(name = "Bank", type = AccountType.BANK, balance = 100.0, icon = "bank"))
        TransactionRepository().insertTransaction(TransactionEntity(amount = 10.0, category = "Other", accountId = accountId, timestamp = System.currentTimeMillis() - 1000))
        val records = InMemoryDatabase.transactions.value.toList()
        val deleted = TransactionRepository().batchTransactions(records, com.example.vibefinance.data.BatchTransactionOperation.Delete)!!
        val request = FinanceIntent.UndoBatch(deleted, "undo-once")
        vm.dispatch(request)
        assertTrue(completed(request.requestId).success)
        vm.dispatch(request)
        assertEquals(records, InMemoryDatabase.transactions.value)
        assertEquals(90.0, InMemoryDatabase.accounts.value.single { it.id == accountId }.balance, 0.0)
        assertTrue(vm.batchResult.value!!.success)
        val collision = request.copy(requestId = "collision")
        vm.dispatch(collision)
        assertNotNull(completed(collision.requestId).error)
        assertEquals(records, InMemoryDatabase.transactions.value)
        assertEquals(90.0, InMemoryDatabase.accounts.value.single { it.id == accountId }.balance, 0.0)
    }
}
