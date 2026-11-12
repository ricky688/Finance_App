package com.example.vibefinance.service

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.data.entity.InterceptableApp
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TransferDecisionNotificationTest {
    private val context: Context
        get() = RuntimeEnvironment.getApplication()

    private val paymentId = "b".repeat(64)

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

    @Test
    fun moxAppPackageIsInterceptable() {
        val moxApp = InterceptableApp.MOX_BANK
        assertTrue("Expected com.mox.app to be a supported package", moxApp.packageKeywords.contains("com.mox.app"))
        assertTrue("Expected com.mox.bank to be a supported package", moxApp.packageKeywords.contains("com.mox.bank"))
        assertTrue(moxApp.matches("com.mox.app", "轉賬", "你已成功轉賬 HKD 100.00"))
    }

    @Test
    fun moxTransferMatchesMoxBankInsteadOfMoxCard() {
        val moxCard = AccountEntity(id = 1L, name = "MOX card", type = AccountType.CC, balance = 0.0, icon = "credit_card")
        val moxBank = AccountEntity(id = 2L, name = "Mox", type = AccountType.BANK, balance = 1500.0, icon = "bank")
        val accounts = listOf(moxCard, moxBank)

        // Incoming transfer
        val incomingPayment = PendingPayment(
            id = "c".repeat(64),
            fingerprint = "fp1",
            sourcePackage = "com.mox.app",
            assetHint = "Mox Bank",
            merchant = "WEN R******",
            amount = 300.0,
            detectedAt = System.currentTimeMillis(),
            transactionType = "INCOME",
            rawTitle = "收到款項",
            rawText = "💰 💖 WEN R******已向你付款HKD300.00。"
        )
        val matchedIncoming = PendingPaymentStore.findMatchingAccount(incomingPayment, accounts)
        assertNotNull("Expected matched account for Mox incoming transfer", matchedIncoming)
        assertEquals("Must match Mox Bank (id=2), NOT MOX card", moxBank.id, matchedIncoming!!.id)
        assertEquals(AccountType.BANK, matchedIncoming.type)

        // Outgoing transfer
        val outgoingPayment = PendingPayment(
            id = "d".repeat(64),
            fingerprint = "fp2",
            sourcePackage = "com.mox.app",
            assetHint = "Mox Bank",
            merchant = "CHAN T** M***",
            amount = 500.0,
            detectedAt = System.currentTimeMillis(),
            transactionType = "TRANSFER",
            rawTitle = "轉賬成功",
            rawText = "你已成功向 CHAN T** M*** 轉賬 HKD 500.00。"
        )
        val matchedOutgoing = PendingPaymentStore.findMatchingAccount(outgoingPayment, accounts)
        assertNotNull("Expected matched account for Mox outgoing transfer", matchedOutgoing)
        assertEquals("Must match Mox Bank (id=2), NOT MOX card", moxBank.id, matchedOutgoing!!.id)
        assertEquals(AccountType.BANK, matchedOutgoing.type)

        // Expense with card indicator should match MOX card
        val cardExpense = PendingPayment(
            id = "e".repeat(64),
            fingerprint = "fp3",
            sourcePackage = "com.mox.app",
            assetHint = "Mox Card",
            merchant = "PARKnSHOP",
            amount = 88.0,
            detectedAt = System.currentTimeMillis(),
            transactionType = "EXPENSE",
            cardLast4 = "1234"
        )
        val matchedCard = PendingPaymentStore.findMatchingAccount(cardExpense, accounts)
        assertNotNull(matchedCard)
        assertEquals("Card spend must match MOX card", moxCard.id, matchedCard!!.id)
        assertEquals(AccountType.CC, matchedCard.type)
    }

    @Test
    fun transferNotificationCannotRememberCreditCard() {
        val moxCard = AccountEntity(id = 1L, name = "MOX card", type = AccountType.CC, balance = 0.0, icon = "credit_card")
        val transferPayment = PendingPayment(
            id = "f".repeat(64),
            fingerprint = "fp4",
            sourcePackage = "com.mox.app",
            assetHint = "Mox Bank",
            merchant = "Transfer Counterparty",
            amount = 200.0,
            detectedAt = System.currentTimeMillis(),
            transactionType = "TRANSFER"
        )
        assertFalse(
            "Transfer must not be allowed to remember a credit card account",
            PendingPaymentStore.canRememberChoice(transferPayment, moxCard)
        )
    }

    @Test
    fun transferDecisionNotificationShowsLiveOngoingWith30sTimeout() {
        val bank = AccountEntity(id = 10L, name = "Mox", type = AccountType.BANK, balance = 1000.0, icon = "bank")
        InMemoryDatabase.insertAccount(bank)

        val transferPayment = PendingPayment(
            id = paymentId,
            fingerprint = "fp-transfer",
            sourcePackage = "com.mox.app",
            assetHint = "Mox Bank",
            merchant = "WEN R******",
            amount = 300.0,
            detectedAt = System.currentTimeMillis(),
            transactionType = "TRANSFER",
            rawTitle = "轉賬",
            rawText = "已向你付款 HKD 300.00"
        )

        val notification = PaymentLoggingNotification.buildDecisionNotification(context, transferPayment, bank)
        assertNotNull(notification)
        assertEquals(PaymentLoggingNotification.TIMEOUT_MS, notification.timeoutAfter)
        assertEquals(30_000L, notification.timeoutAfter)
        assertTrue("Notification must be ongoing", notification.flags and android.app.Notification.FLAG_ONGOING_EVENT != 0)

        // Verify actions: Confirm (Mox), Decide, Ignore
        assertNotNull(notification.actions)
        assertTrue("Expected at least 3 actions on decision notification", notification.actions.size >= 3)

        val actionTitles = notification.actions.map { it.title.toString() }
        assertTrue("Expected confirm action with account name", actionTitles.any { it.contains("Mox") || it.contains("確認") || it.contains("Confirm") })
        assertTrue("Expected choose/decide action", actionTitles.any { it.contains("選擇") || it.contains("Choose") })
        assertTrue("Expected ignore action", actionTitles.any { it.contains("忽略") || it.contains("Ignore") })

        // Show decision notification and verify posted
        PaymentLoggingNotification.showDecision(context, transferPayment, bank)
        val shadowManager = shadowOf(context.getSystemService(NotificationManager::class.java))
        val active = shadowManager.allNotifications
        assertEquals(1, active.size)
    }

    @Test
    fun receiverConfirmsTransferDirectly() {
        val bankId = InMemoryDatabase.insertAccount(AccountEntity(name = "Mox", type = AccountType.BANK, balance = 500.0, icon = "bank"))
        val payment = PendingPaymentStore.enqueue(
            context = context,
            sourcePackage = "com.mox.app",
            assetHint = "Mox Bank",
            merchant = "Test Sender",
            amount = 150.0,
            detectedAt = System.currentTimeMillis(),
            transactionType = "TRANSFER"
        )!!

        PaymentLoggingNotification.showDecision(context, payment, InMemoryDatabase.accounts.value.single())

        // Simulate user clicking "Confirm" on notification
        val receiver = NotificationActionReceiver()
        val confirmIntent = Intent(NotificationActionReceiver.ACTION_CONFIRM_TRANSFER).apply {
            putExtra(NotificationActionReceiver.EXTRA_PAYMENT_ID, payment.id)
            putExtra(NotificationActionReceiver.EXTRA_ACCOUNT_ID, bankId)
        }
        receiver.onReceive(context, confirmIntent)

        // Wait for async IO
        Thread.sleep(100)

        // Verify payment is accepted and recorded into transactions
        assertTrue(PendingPaymentStore.pending.value.isEmpty())
        val txs = InMemoryDatabase.transactions.value
        assertEquals(1, txs.size)
        assertEquals(150.0, txs.single().amount, 0.001)
        assertEquals("Transfer", txs.single().category)

        // Verify notification is dismissed
        val shadowManager = shadowOf(context.getSystemService(NotificationManager::class.java))
        assertEquals(0, shadowManager.allNotifications.size)
    }

    @Test
    fun receiverIgnoresTransferDirectly() {
        InMemoryDatabase.insertAccount(AccountEntity(name = "Mox", type = AccountType.BANK, balance = 500.0, icon = "bank"))
        val payment = PendingPaymentStore.enqueue(
            context = context,
            sourcePackage = "com.mox.app",
            assetHint = "Mox Bank",
            merchant = "Spam Sender",
            amount = 99.0,
            detectedAt = System.currentTimeMillis(),
            transactionType = "TRANSFER"
        )!!

        PaymentLoggingNotification.showDecision(context, payment, null)

        // Simulate user clicking "Ignore" on notification
        val receiver = NotificationActionReceiver()
        val ignoreIntent = Intent(NotificationActionReceiver.ACTION_IGNORE_TRANSFER).apply {
            putExtra(NotificationActionReceiver.EXTRA_PAYMENT_ID, payment.id)
        }
        receiver.onReceive(context, ignoreIntent)

        // Wait for async IO
        Thread.sleep(100)

        // Verify payment is removed from pending, but not inserted into transactions
        assertTrue(PendingPaymentStore.pending.value.isEmpty())
        assertTrue(InMemoryDatabase.transactions.value.isEmpty())

        // Verify notification is dismissed
        val shadowManager = shadowOf(context.getSystemService(NotificationManager::class.java))
        assertEquals(0, shadowManager.allNotifications.size)
    }

    @Test
    fun moxDirectPaymentTransferParsesAndMatchesMoxBank() {
        val moxCard = AccountEntity(id = 8L, name = "MOX card", type = AccountType.CC, balance = 0.0, icon = "credit_card", linkedAppPackage = "com.mox.app")
        val moxBank = AccountEntity(id = 9L, name = "Mox bank", type = AccountType.BANK, balance = 1500.0, icon = "bank", linkedAppPackage = "com.mox.app")
        val smartOctopus = AccountEntity(id = 15L, name = "Smart Octopus", type = AccountType.DEBIT, balance = 200.0, icon = "credit_card")
        InMemoryDatabase.insertAccount(moxCard)
        InMemoryDatabase.insertAccount(moxBank)
        InMemoryDatabase.insertAccount(smartOctopus)

        val title = "直接付款成功"
        val text = "👍 你已成功向POON T** C***直接付款HKD300.00。"
        val pkg = "com.mox.app"

        // 1. Parse notification
        val parsed = PaymentNotificationListener.parseNotification(title, text, pkg)
        assertNotNull("Expected parse result for Mox direct payment", parsed)
        assertEquals(300.00, parsed!!.amount, 0.001)
        assertEquals("POON T** C***", parsed.merchant)
        assertEquals("TRANSFER", parsed.transactionType)
        assertFalse(parsed.isTopUp)

        // 2. Queue payment and verify decision notification is shown
        PaymentNotificationListener.queuePayment(context, parsed.copy(sourcePackage = pkg))

        val shadowManager = shadowOf(context.getSystemService(NotificationManager::class.java))
        val active = shadowManager.allNotifications
        assertEquals(1, active.size)
        val notif = active.first()
        assertEquals(30_000L, notif.timeoutAfter)
        assertTrue(notif.actions.any { it.title.toString().contains("Mox bank") })
    }

    @Test
    fun octopusBankTransferTopUpAutoLogsWhenRemembered() {
        val moxBankId = InMemoryDatabase.insertAccount(AccountEntity(name = "Mox bank", type = AccountType.BANK, balance = 1500.0, icon = "bank"))
        val smartOctopusId = InMemoryDatabase.insertAccount(AccountEntity(name = "Smart Octopus", type = AccountType.DEBIT, balance = 200.0, icon = "credit_card"))

        // Set remembered account mapping for octopus bank top up
        context.getSharedPreferences("pending_payment_choices", 0).edit()
            .putString("remembered_accounts", "{\"com.mox.app|八達通\":$moxBankId}")
            .commit()
        PendingPaymentStore.reloadAfterFullRestore(context)

        val title = "八達通"
        val text = "你已成功由銀行戶口轉賬 HKD 300.0 至八達通 *****1492。"
        val pkg = "com.octopuscards.nfc_reader"

        val parsed = PaymentNotificationListener.parseNotification(title, text, pkg)
        assertNotNull(parsed)
        assertTrue(parsed!!.isTopUp)
        assertEquals(300.00, parsed.amount, 0.001)
        assertEquals("1492", parsed.cardLast4)

        // Queue payment: should auto-log immediately because remembered account exists
        val handled = PaymentNotificationListener.queuePayment(context, parsed.copy(sourcePackage = pkg))
        assertTrue(handled)

        // Verify transaction logged as Top-up from Mox bank -> Smart Octopus
        val txs = InMemoryDatabase.transactions.value
        assertEquals(1, txs.size)
        val tx = txs.single()
        assertEquals("Top-up", tx.category)
        assertEquals(300.00, tx.amount, 0.001)
        assertEquals(moxBankId, tx.accountId)
        assertEquals(smartOctopusId, tx.toAccountId)

        // Verify Mox bank balance deducted by 300, Smart Octopus increased by 300
        val accMap = InMemoryDatabase.accounts.value.associateBy { it.id }
        assertEquals(1200.00, accMap[moxBankId]?.balance ?: 0.0, 0.001)
        assertEquals(500.00, accMap[smartOctopusId]?.balance ?: 0.0, 0.001)

        // Verify 1492 was bound to Smart Octopus, NOT Mox bank
        assertEquals("1492", accMap[smartOctopusId]?.cardLast4)
        assertNull(accMap[moxBankId]?.cardLast4)
    }

    @Test
    fun concurrentMoxDirectDebitAndOctopusTopUpMergeCleanly() {
        val moxBankId = InMemoryDatabase.insertAccount(AccountEntity(name = "Mox bank", type = AccountType.BANK, balance = 2000.0, icon = "bank"))
        val smartOctopusId = InMemoryDatabase.insertAccount(AccountEntity(name = "Smart Octopus", type = AccountType.DEBIT, balance = 100.0, icon = "credit_card"))

        // 1. Mox notification arrives first
        val moxParsed = PaymentNotificationListener.parseNotification(
            "直接付款成功",
            "👍 你已成功向POON T** C***直接付款HKD300.00。",
            "com.mox.app"
        )!!
        PaymentNotificationListener.queuePayment(context, moxParsed.copy(sourcePackage = "com.mox.app"))

        // Mox posted a decision notification
        val shadowManager = shadowOf(context.getSystemService(NotificationManager::class.java))
        assertEquals(1, shadowManager.allNotifications.size)
        assertEquals(1, PendingPaymentStore.pending.value.size)

        // 2. Octopus notification arrives 2 seconds later
        val octParsed = PaymentNotificationListener.parseNotification(
            "八達通",
            "你已成功由銀行戶口轉賬 HKD 300.0 至八達通 *****1492。",
            "com.octopuscards.nfc_reader"
        )!!
        PaymentNotificationListener.queuePayment(context, octParsed.copy(sourcePackage = "com.octopuscards.nfc_reader"))

        // The pending Mox decision must have been replaced and auto-logged!
        assertTrue(PendingPaymentStore.pending.value.isEmpty())
        val txs = InMemoryDatabase.transactions.value
        assertEquals(1, txs.size)
        val tx = txs.single()
        assertEquals("Top-up", tx.category)
        assertEquals(300.00, tx.amount, 0.001)
        assertEquals(moxBankId, tx.accountId)
        assertEquals(smartOctopusId, tx.toAccountId)

        val accMap = InMemoryDatabase.accounts.value.associateBy { it.id }
        assertEquals(1700.00, accMap[moxBankId]?.balance ?: 0.0, 0.001)
        assertEquals(400.00, accMap[smartOctopusId]?.balance ?: 0.0, 0.001)
    }
}
