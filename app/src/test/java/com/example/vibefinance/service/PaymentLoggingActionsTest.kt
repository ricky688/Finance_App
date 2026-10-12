package com.example.vibefinance.service

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.data.entity.TransactionEntity
import java.io.File
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PaymentLoggingActionsTest {
    private val context: Context
        get() = RuntimeEnvironment.getApplication()

    private val id = "a".repeat(64)
    private val key = "notification:$id"

    @Before
    fun before() {
        InMemoryDatabase.initialize(context)
        InMemoryDatabase.clearAllTables()
        context.getSharedPreferences("pending_payment_choices", 0).edit().clear().commit()
        PendingPaymentStore.reloadAfterFullRestore(context)
    }

    @After
    fun after() {
        File(context.filesDir, "vibefinance_data.json.new").deleteRecursively()
        InMemoryDatabase.clearAllTables()
        context.getSystemService(NotificationManager::class.java).cancelAll()
    }

    private fun account(type: AccountType, balance: Double = 500.0): Long =
        InMemoryDatabase.insertAccount(
            AccountEntity(name = "卡片銀行", type = type, balance = balance, icon = "bank")
        )

    private fun insert(source: Long, amount: Double = 42.0, destination: Long? = null) {
        assertTrue(
            InMemoryDatabase.insertNotificationExpenseIfAbsent(
                TransactionEntity(
                    accountId = source,
                    toAccountId = destination,
                    amount = amount,
                    category = "Food",
                    timestamp = System.currentTimeMillis() - 1000,
                    groupId = key,
                    description = "咖啡店 ☕",
                )
            )
        )
    }

    @Test
    fun expenseAndIncomeUndoRespectEveryAccountTypeAndAreIdempotent() {
        for (type in AccountType.entries) for (amount in listOf(42.0, -42.0)) {
            val source = account(type)
            insert(source, amount)
            assertTrue(PendingPaymentStore.undoPayment(context, id))
            assertFalse(PendingPaymentStore.undoPayment(context, id))
            assertEquals(
                500.0,
                InMemoryDatabase.accounts.value.single { it.id == source }.balance,
                0.001,
            )
            assertTrue(InMemoryDatabase.transactions.value.isEmpty())
        }
        InMemoryDatabase.initialize(context)
        assertTrue(InMemoryDatabase.accounts.value.all { it.balance == 500.0 })
        assertTrue(InMemoryDatabase.transactions.value.isEmpty())
    }

    @Test
    fun coldActionLoadsDiskBeforeReversingThePayment() {
        val source = account(AccountType.BANK)
        insert(source)
        InMemoryDatabase::class.java.getDeclaredField("appContext").apply {
            isAccessible = true
            set(InMemoryDatabase, null)
        }
        InMemoryDatabase.transactions.value = emptyList()
        InMemoryDatabase.accounts.value = emptyList()
        assertTrue(PendingPaymentStore.undoPayment(context, id))
        assertEquals(500.0, InMemoryDatabase.accounts.value.single().balance, 0.001)
        InMemoryDatabase.initialize(context)
        assertTrue(InMemoryDatabase.transactions.value.isEmpty())
    }

    @Test
    fun transferUndoRestoresBothAccountsUsingHistoricalTypes() {
        for (sourceType in listOf(AccountType.BANK, AccountType.CC)) {
            for (destinationType in listOf(AccountType.BANK, AccountType.CC)) {
                val source = account(sourceType)
                val destination = account(destinationType)
                insert(source, destination = destination)
                // Editing an account type must not reinterpret an already-recorded payment.
                val account = InMemoryDatabase.accounts.value.single { it.id == source }
                InMemoryDatabase.updateAccount(
                    account.copy(
                        type =
                            if (sourceType == AccountType.CC) AccountType.BANK else AccountType.CC
                    )
                )
                assertTrue(PendingPaymentStore.undoPayment(context, id))
                assertEquals(
                    500.0,
                    InMemoryDatabase.accounts.value.single { it.id == source }.balance,
                    0.001,
                )
                assertEquals(
                    500.0,
                    InMemoryDatabase.accounts.value.single { it.id == destination }.balance,
                    0.001,
                )
            }
        }
    }

    @Test
    fun concurrentUndoRefundsOnceAndDoesNotAffectAnotherReceipt() {
        val source = account(AccountType.BANK)
        insert(source)
        InMemoryDatabase.insertNotificationExpenseIfAbsent(
            TransactionEntity(
                accountId = source,
                amount = 5.0,
                category = "Food",
                timestamp = 0,
                groupId = "notification:${"b".repeat(64)}",
            )
        )
        val executor = Executors.newFixedThreadPool(4)
        try {
            val results =
                executor.invokeAll(
                    (1..8).map { Callable { PendingPaymentStore.undoPayment(context, id) } }
                )
            assertEquals(1, results.count { it.get() })
        } finally {
            executor.shutdownNow()
        }
        assertEquals(495.0, InMemoryDatabase.accounts.value.single().balance, 0.001)
        assertEquals(1, InMemoryDatabase.transactions.value.size)
    }

    @Test
    fun historyDeleteRacingUndoUsesOneRefund() {
        val source = account(AccountType.BANK)
        insert(source)
        val transaction = InMemoryDatabase.transactions.value.single()
        val executor = Executors.newFixedThreadPool(2)
        try {
            val results =
                executor.invokeAll(
                    listOf(
                        Callable { PendingPaymentStore.undoPayment(context, id) },
                        Callable { InMemoryDatabase.deleteTransactionWithBalances(transaction.id) },
                    )
                )
            assertEquals(1, results.count { it.get() })
        } finally {
            executor.shutdownNow()
        }
        assertEquals(500.0, InMemoryDatabase.accounts.value.single().balance, 0.001)
    }

    @Test
    fun failedUndoLeavesDiskAndPublishedStateUntouched_thenRetrySurvivesReload() {
        val source = account(AccountType.CC)
        insert(source)
        val accounts = InMemoryDatabase.accounts.value
        val transactions = InMemoryDatabase.transactions.value
        val disk = File(context.filesDir, "vibefinance_data.json")
        val bytes = disk.readText()
        val blocked = File(context.filesDir, "vibefinance_data.json.new")
        blocked.mkdir()
        try {
            PendingPaymentStore.undoPayment(context, id)
            fail("Must report a failed durable write")
        } catch (_: java.io.IOException) {}
        assertEquals(accounts, InMemoryDatabase.accounts.value)
        assertEquals(transactions, InMemoryDatabase.transactions.value)
        assertEquals(bytes, disk.readText())
        blocked.deleteRecursively()
        assertTrue(PendingPaymentStore.undoPayment(context, id))
        InMemoryDatabase.initialize(context)
        assertEquals(500.0, InMemoryDatabase.accounts.value.single().balance, 0.001)
        assertTrue(InMemoryDatabase.transactions.value.isEmpty())
    }

    @Test
    fun categoryOnlyEditPreservesBalancesAndLatestTransactionFields() {
        val source = account(AccountType.BANK)
        val destination = account(AccountType.CC)
        insert(source, destination = destination)
        val stored = InMemoryDatabase.transactions.value.single()
        InMemoryDatabase.updateTransactionWithBalances(
            stored.copy(description = "New note", customIcon = "☕"),
            stored,
        )
        val before = InMemoryDatabase.transactions.value.single()
        val balances = InMemoryDatabase.accounts.value
        assertTrue(PendingPaymentStore.changeCategory(context, id, "Transport"))
        assertEquals(
            before.copy(category = "Transport"),
            InMemoryDatabase.transactions.value.single(),
        )
        assertEquals(balances, InMemoryDatabase.accounts.value)
        InMemoryDatabase.initialize(context)
        assertEquals("Transport", InMemoryDatabase.transactions.value.single().category)
        assertFalse(PendingPaymentStore.changeCategory(context, "b".repeat(64), "Food"))
        assertFalse(PendingPaymentStore.undoPayment(context, "not-a-payment"))
    }

    @Test
    fun undonePaymentKeepsDeduplicationAndReportedBalanceIsRefunded() {
        val source = account(AccountType.BANK)
        val now = System.currentTimeMillis()
        val payment =
            PendingPaymentStore.enqueue(
                context,
                "com.octopuscards.octopus_app",
                "八達通",
                "港鐵",
                4.9,
                now,
                notificationKey = "stable-key",
                balanceRemaining = 20.0,
            )!!
        assertTrue(PendingPaymentStore.accept(context, payment.id, source, false))
        assertTrue(PendingPaymentStore.undoPayment(context, payment.id))
        assertEquals(24.9, InMemoryDatabase.accounts.value.single().balance, 0.001)
        assertNull(
            PendingPaymentStore.enqueue(
                context,
                "com.octopuscards.octopus_app",
                "八達通",
                "港鐵",
                4.9,
                now,
                notificationKey = "stable-key",
            )
        )
    }

    @Test
    fun receiptHasNativeTimeoutHighChannelAndPrivateImmutableUniqueActions() {
        val notification =
            PaymentLoggingNotification.build(context, id, "Receipt", "Merchant $42", 42.0)
        assertEquals(30_000L, notification.timeoutAfter)
        assertTrue(notification.flags and Notification.FLAG_ONGOING_EVENT != 0)
        assertTrue(notification.flags and Notification.FLAG_AUTO_CANCEL != 0)
        assertEquals(Notification.VISIBILITY_PRIVATE, notification.visibility)
        val manager = context.getSystemService(NotificationManager::class.java)
        assertEquals(
            NotificationManager.IMPORTANCE_HIGH,
            manager.getNotificationChannel("payment_live").importance,
        )
        assertEquals(2, notification.actions.size)
        val undo = notification.actions[0].actionIntent
        val edit = notification.actions[1].actionIntent
        assertTrue(undo.isImmutable)
        assertTrue(edit.isImmutable)
        assertTrue(shadowOf(undo).isBroadcast)
        assertTrue(shadowOf(edit).isActivity)
        assertEquals(
            key,
            shadowOf(undo)
                .savedIntent
                .getStringExtra(NotificationActionReceiver.EXTRA_TRANSACTION_KEY),
        )
        assertEquals(
            id,
            shadowOf(edit)
                .savedIntent
                .getStringExtra(NotificationActionReceiver.EXTRA_EDIT_PAYMENT),
        )
        val other =
            PaymentLoggingNotification.build(context, "b".repeat(64), "Receipt", "Other", 5.0)
        assertNotEquals(undo, other.actions[0].actionIntent)
        assertNotEquals(edit, other.actions[1].actionIntent)
    }

    @Test
    fun notificationActionUndoesInBackgroundAndCancelsOnlyItsReceipt() {
        val source = account(AccountType.BANK)
        insert(source)
        PaymentLoggingNotification.show(context, id, "Receipt", "Synthetic", 42.0)
        val manager = context.getSystemService(NotificationManager::class.java)
        val notification =
            shadowOf(manager).getNotification(id, PaymentLoggingNotification.notificationId(id))
        // Robolectric does not instantiate manifest receivers when dispatching PendingIntents.
        val receiverInstance = NotificationActionReceiver()
        context.registerReceiver(
            receiverInstance,
            android.content.IntentFilter(NotificationActionReceiver.ACTION_UNDO_PAYMENT),
            Context.RECEIVER_NOT_EXPORTED,
        )
        notification.actions[0].actionIntent.send()
        val deadline = System.nanoTime() + 5_000_000_000L
        while (
            System.nanoTime() < deadline &&
                shadowOf(manager)
                    .getNotification(id, PaymentLoggingNotification.notificationId(id)) != null
        ) {
            org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
            Thread.sleep(10)
        }
        assertNull(
            shadowOf(manager).getNotification(id, PaymentLoggingNotification.notificationId(id))
        )
        assertTrue(InMemoryDatabase.transactions.value.isEmpty())
        assertEquals(500.0, InMemoryDatabase.accounts.value.single().balance, 0.001)
        assertNull(shadowOf(RuntimeEnvironment.getApplication()).nextStartedActivity)
        val receiver =
            android.content.ComponentName(context, NotificationActionReceiver::class.java)
        assertFalse(context.packageManager.getReceiverInfo(receiver, 0).exported)
        context.unregisterReceiver(receiverInstance)
    }
}
