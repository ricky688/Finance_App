package com.example.vibefinance.data.repository

import android.content.Context
import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.data.entity.TransactionEntity
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DebitAccountSemanticsTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        InMemoryDatabase.initialize(context)
        InMemoryDatabase.clearAllTables()
    }

    @After
    fun tearDown() {
        InMemoryDatabase.clearAllTables()
    }

    @Test
    fun debitExpensesAndTransfersChangeAvailableBalanceInsteadOfDebt() = runTest {
        val debitId = InMemoryDatabase.insertAccount(
            AccountEntity(name = "Everyday debit", type = AccountType.DEBIT, balance = 100.0, icon = "debit_card")
        )
        val bankId = InMemoryDatabase.insertAccount(
            AccountEntity(name = "Savings", type = AccountType.BANK, balance = 200.0, icon = "bank")
        )
        val creditId = InMemoryDatabase.insertAccount(
            AccountEntity(name = "Credit", type = AccountType.CC, balance = 50.0, icon = "credit_card")
        )
        val repository = TransactionRepository()
        val timestamp = System.currentTimeMillis() - 1_000L

        repository.insertTransaction(
            TransactionEntity(amount = 25.0, category = "Food", timestamp = timestamp, accountId = debitId)
        )
        assertEquals(75.0, InMemoryDatabase.accounts.value.first { it.id == debitId }.balance, 0.001)
        assertFalse(InMemoryDatabase.transactions.value.last().sourceWasCreditCard!!)

        repository.insertTransaction(
            TransactionEntity(amount = -10.0, category = "Income", timestamp = timestamp, accountId = debitId)
        )
        assertEquals(85.0, InMemoryDatabase.accounts.value.first { it.id == debitId }.balance, 0.001)

        repository.insertTransaction(
            TransactionEntity(amount = 20.0, category = "Transfer", timestamp = timestamp,
                accountId = debitId, toAccountId = bankId)
        )
        assertEquals(65.0, InMemoryDatabase.accounts.value.first { it.id == debitId }.balance, 0.001)
        assertEquals(220.0, InMemoryDatabase.accounts.value.first { it.id == bankId }.balance, 0.001)

        repository.insertTransaction(
            TransactionEntity(amount = 15.0, category = "Card Payment", timestamp = timestamp,
                accountId = debitId, toAccountId = creditId)
        )
        assertEquals(50.0, InMemoryDatabase.accounts.value.first { it.id == debitId }.balance, 0.001)
        assertEquals(35.0, InMemoryDatabase.accounts.value.first { it.id == creditId }.balance, 0.001)
    }

    @Test
    fun debitIdentityAndTypeSurviveDiskReloadWhileOlderSnapshotsKeepNullDefaults() {
        val id = InMemoryDatabase.insertAccount(
            AccountEntity(
                name = "Bank of China Debit",
                nickname = "Travel card",
                type = AccountType.DEBIT,
                balance = 123.45,
                icon = "debit_card",
                cardLast4 = "2468",
                accentColorKey = "coral"
            )
        )
        InMemoryDatabase.accounts.value = emptyList()
        InMemoryDatabase.initialize(context)
        val reloaded = InMemoryDatabase.accounts.value.single { it.id == id }
        assertEquals(AccountType.DEBIT, reloaded.type)
        assertEquals("Travel card", reloaded.nickname)
        assertEquals("2468", reloaded.cardLast4)
        assertEquals("coral", reloaded.accentColorKey)

        val diskFile = File(context.filesDir, "vibefinance_data.json")
        val olderSnapshot = JSONObject(diskFile.readText())
        val savedAccount = olderSnapshot.getJSONArray("accounts").getJSONObject(0)
        savedAccount.remove("nickname")
        savedAccount.remove("accentColorKey")
        diskFile.writeText(olderSnapshot.toString())
        InMemoryDatabase.accounts.value = emptyList()
        InMemoryDatabase.initialize(context)
        val legacyAccount = InMemoryDatabase.accounts.value.single { it.id == id }
        assertEquals(AccountType.DEBIT, legacyAccount.type)
        assertNull(legacyAccount.nickname)
        assertNull(legacyAccount.accentColorKey)
    }

    @Test
    fun notificationExpenseAndManualBalanceCorrectionPreserveDebitAssetSigns() {
        val debitId = InMemoryDatabase.insertAccount(
            AccountEntity(name = "Debit", type = AccountType.DEBIT, balance = 100.0, icon = "debit_card")
        )
        assertTrue(
            InMemoryDatabase.insertNotificationExpenseIfAbsent(
                TransactionEntity(
                    amount = 3.60,
                    category = "Transport",
                    timestamp = System.currentTimeMillis(),
                    accountId = debitId,
                    groupId = "notification:debit-test"
                )
            )
        )
        assertEquals(96.40, InMemoryDatabase.accounts.value.single().balance, 0.001)
        assertFalse(InMemoryDatabase.transactions.value.single().sourceWasCreditCard!!)

        val corrected = InMemoryDatabase.accounts.value.single().copy(balance = 101.40)
        InMemoryDatabase.updateAccountWithBalanceAdjustment(corrected)
        assertEquals(101.40, InMemoryDatabase.accounts.value.single().balance, 0.001)
        val adjustment = InMemoryDatabase.transactions.value.last()
        assertTrue(adjustment.isBalanceAdjustment)
        assertEquals(-5.0, adjustment.amount, 0.001)
        assertEquals(5.0, adjustment.balanceAdjustmentDelta!!, 0.001)
    }
}
