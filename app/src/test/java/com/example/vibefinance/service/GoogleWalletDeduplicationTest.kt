package com.example.vibefinance.service

import android.app.NotificationManager
import android.content.Context
import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
class GoogleWalletDeduplicationTest {
    private val context: Context
        get() = RuntimeEnvironment.getApplication()

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
    fun testAreCompanionExpenseAlertsValidation() {
        val now = 100_000L

        // 1. Google Wallet + Mox Bank with bilingual Sushiro and same card
        assertTrue(
            "Google Wallet + Mox Bank at Sushiro on same card must be companion alerts",
            PendingPaymentStore.areCompanionExpenseAlerts(
                pkg1 = "com.google.android.apps.walletnfcrel", hint1 = "Google Pay", title1 = "Sushiro HK",
                merchant1 = "Sushiro HK", amount1 = 157.0, time1 = now, card1 = "2101",
                pkg2 = "com.mox.app", hint2 = "Mox Card", title2 = "交易已完成",
                merchant2 = "壽司郎", amount2 = 157.0, time2 = now + 3000L, card2 = "2101"
            )
        )

        // 2. Google Wallet generic merchant + Bank specific merchant
        assertTrue(
            "Generic Google Pay Merchant must match specific bank merchant",
            PendingPaymentStore.areCompanionExpenseAlerts(
                pkg1 = "com.google.android.apps.walletnfcrel", hint1 = "Google Pay", title1 = "Google Pay",
                merchant1 = "Google Pay Merchant", amount1 = 40.78, time1 = now, card1 = "1234",
                pkg2 = "com.mox.app", hint2 = "Mox Bank", title2 = "消費回贈",
                merchant2 = "GOOGLE", amount2 = 40.78, time2 = now + 2000L, card2 = null
            )
        )

        // 3. Different physical cards must NOT be companion alerts
        assertFalse(
            "Different card numbers must not be merged",
            PendingPaymentStore.areCompanionExpenseAlerts(
                pkg1 = "com.google.android.apps.walletnfcrel", hint1 = "Google Pay", title1 = "Sushiro HK",
                merchant1 = "Sushiro HK", amount1 = 157.0, time1 = now, card1 = "2101",
                pkg2 = "com.mox.app", hint2 = "Mox Card", title2 = "壽司郎",
                merchant2 = "壽司郎", amount2 = 157.0, time2 = now + 2000L, card2 = "9999"
            )
        )

        // 4. Different merchants (e.g. Starbucks vs 7-Eleven) must NOT be companion alerts
        assertFalse(
            "Completely different merchants must not be merged",
            PendingPaymentStore.areCompanionExpenseAlerts(
                pkg1 = "com.google.android.apps.walletnfcrel", hint1 = "Google Pay", title1 = "Starbucks",
                merchant1 = "Starbucks", amount1 = 42.0, time1 = now, card1 = "2101",
                pkg2 = "com.mox.app", hint2 = "Mox Card", title2 = "7-Eleven",
                merchant2 = "7-Eleven", amount2 = 42.0, time2 = now + 2000L, card2 = "2101"
            )
        )

        // 5. Time window exceeding 90 seconds must NOT be companion alerts
        assertFalse(
            "Alerts separated by >90s must not be merged",
            PendingPaymentStore.areCompanionExpenseAlerts(
                pkg1 = "com.google.android.apps.walletnfcrel", hint1 = "Google Pay", title1 = "Sushiro HK",
                merchant1 = "Sushiro HK", amount1 = 157.0, time1 = now, card1 = "2101",
                pkg2 = "com.mox.app", hint2 = "Mox Card", title2 = "壽司郎",
                merchant2 = "壽司郎", amount2 = 157.0, time2 = now + 95_000L, card2 = "2101"
            )
        )

        // 6. Amount mismatch must NOT be companion alerts
        assertFalse(
            "Different amounts must not be merged",
            PendingPaymentStore.areCompanionExpenseAlerts(
                pkg1 = "com.google.android.apps.walletnfcrel", hint1 = "Google Pay", title1 = "Sushiro HK",
                merchant1 = "Sushiro HK", amount1 = 157.0, time1 = now, card1 = "2101",
                pkg2 = "com.mox.app", hint2 = "Mox Card", title2 = "壽司郎",
                merchant2 = "壽司郎", amount2 = 158.0, time2 = now + 2000L, card2 = "2101"
            )
        )
    }

    @Test
    fun testGoogleWalletPendingThenBankArrivesEnrichesAndDeduplicates() {
        val now = System.currentTimeMillis()

        // 1. Google Wallet arrives first, without a remembered account
        val gwallet = PaymentNotificationListener.ParsedPayment(
            amount = 157.00,
            merchant = "Google Pay Merchant",
            assetName = "Google Pay",
            sourcePackage = "com.google.android.apps.walletnfcrel",
            detectedAt = now,
            cardLast4 = "2101",
            rawTitle = "Google Pay",
            rawText = "金額：HK$157.00 付款卡：•••• 2101"
        )
        val enqueuedGoogle = PendingPaymentStore.enqueue(
            context = context,
            sourcePackage = gwallet.sourcePackage,
            assetHint = gwallet.assetName,
            merchant = gwallet.merchant,
            amount = gwallet.amount,
            detectedAt = gwallet.detectedAt,
            cardLast4 = gwallet.cardLast4,
            rawTitle = gwallet.rawTitle,
            rawText = gwallet.rawText
        )
        assertNotNull("Google Wallet alert should be enqueued", enqueuedGoogle)
        assertEquals(1, PendingPaymentStore.pending.value.size)

        // 2. Mox Bank alert arrives 3 seconds later for the same physical purchase
        val moxAlert = PaymentNotificationListener.ParsedPayment(
            amount = 157.00,
            merchant = "壽司郎",
            assetName = "Mox Card",
            sourcePackage = "com.mox.app",
            detectedAt = now + 3000L,
            cardLast4 = "2101",
            rawTitle = "交易已完成",
            rawText = "你已在 壽司郎 消費 HKD 157.00"
        )
        val enqueuedMox = PendingPaymentStore.enqueue(
            context = context,
            sourcePackage = moxAlert.sourcePackage,
            assetHint = moxAlert.assetName,
            merchant = moxAlert.merchant,
            amount = moxAlert.amount,
            detectedAt = moxAlert.detectedAt,
            cardLast4 = moxAlert.cardLast4,
            rawTitle = moxAlert.rawTitle,
            rawText = moxAlert.rawText
        )
        assertNotNull("Mox companion alert should return the merged payment", enqueuedMox)

        // There should still only be EXACTLY 1 pending payment in the inbox
        assertEquals(1, PendingPaymentStore.pending.value.size)
        val merged = PendingPaymentStore.pending.value.first()
        assertEquals("壽司郎", merged.merchant)
        assertEquals("Mox Card", merged.assetHint)
        assertEquals("com.mox.app", merged.sourcePackage)
        assertEquals("2101", merged.cardLast4)
    }

    @Test
    fun testGoogleWalletAutoLoggedThenBankAlertDropsAndUpgradesMerchant() {
        val now = System.currentTimeMillis()
        val moxAccount = AccountEntity(
            id = 100L,
            name = "Mox Card",
            type = AccountType.CC,
            balance = 0.0,
            icon = "credit_card",
            cardLast4 = "2101"
        )
        InMemoryDatabase.insertAccount(moxAccount)

        // 1. Google Wallet arrives and matches account via cardLast4 2101
        val gwallet = PaymentNotificationListener.ParsedPayment(
            amount = 157.00,
            merchant = "Google Pay Merchant",
            assetName = "Google Pay",
            sourcePackage = "com.google.android.apps.walletnfcrel",
            detectedAt = now,
            cardLast4 = "2101",
            rawTitle = "Google Pay",
            rawText = "金額：HK$157.00 付款卡：•••• 2101"
        )
        val logged = PaymentNotificationListener.queuePayment(context, gwallet)
        assertTrue("Google Wallet payment should be queued and auto-logged", logged)

        val txList1 = InMemoryDatabase.transactions.value.filter { kotlin.math.abs(it.amount - 157.0) < 0.01 }
        assertEquals(1, txList1.size)
        assertEquals("Google Pay Merchant", txList1.first().description)

        // 2. Mox Bank alert arrives 2 seconds later
        val moxAlert = PaymentNotificationListener.ParsedPayment(
            amount = 157.00,
            merchant = "壽司郎",
            assetName = "Mox Card",
            sourcePackage = "com.mox.app",
            detectedAt = now + 2000L,
            cardLast4 = "2101",
            rawTitle = "交易已完成",
            rawText = "你已在 壽司郎 消費 HKD 157.00"
        )
        val moxResult = PaymentNotificationListener.queuePayment(context, moxAlert)
        assertFalse("Mox companion alert should return false (dropped as duplicate)", moxResult)

        // Verify there is STILL exactly 1 transaction in DB, and its description upgraded to 壽司郎!
        val txList2 = InMemoryDatabase.transactions.value.filter { kotlin.math.abs(it.amount - 157.0) < 0.01 }
        assertEquals("Duplicate transaction must NOT be inserted into database", 1, txList2.size)
        assertEquals("Generic description must be upgraded to specific merchant", "壽司郎", txList2.first().description)
        assertEquals(0, PendingPaymentStore.pending.value.size)
    }

    @Test
    fun testBankAutoLoggedThenGoogleWalletAlertDrops() {
        val now = System.currentTimeMillis()
        val moxAccount = AccountEntity(
            id = 200L,
            name = "Mox Card",
            type = AccountType.CC,
            balance = 0.0,
            icon = "credit_card",
            cardLast4 = "2101"
        )
        InMemoryDatabase.insertAccount(moxAccount)

        // 1. Mox Bank alert arrives first and is auto-logged
        val moxAlert = PaymentNotificationListener.ParsedPayment(
            amount = 157.00,
            merchant = "壽司郎",
            assetName = "Mox Card",
            sourcePackage = "com.mox.app",
            detectedAt = now,
            cardLast4 = "2101",
            rawTitle = "交易已完成",
            rawText = "你已在 壽司郎 消費 HKD 157.00"
        )
        val moxLogged = PaymentNotificationListener.queuePayment(context, moxAlert)
        assertTrue("Mox payment should be auto-logged", moxLogged)

        val txList1 = InMemoryDatabase.transactions.value.filter { kotlin.math.abs(it.amount - 157.0) < 0.01 }
        assertEquals(1, txList1.size)
        assertEquals("壽司郎", txList1.first().description)

        // 2. Google Wallet arrives 2 seconds later
        val gwallet = PaymentNotificationListener.ParsedPayment(
            amount = 157.00,
            merchant = "Sushiro HK",
            assetName = "Google Pay",
            sourcePackage = "com.google.android.apps.walletnfcrel",
            detectedAt = now + 2000L,
            cardLast4 = "2101",
            rawTitle = "Sushiro HK",
            rawText = "金額：HK$157.00 付款卡：•••• 2101"
        )
        val gwalletResult = PaymentNotificationListener.queuePayment(context, gwallet)
        assertFalse("Google Wallet companion alert should return false (dropped as duplicate)", gwalletResult)

        // Database must STILL have only 1 transaction
        val txList2 = InMemoryDatabase.transactions.value.filter { kotlin.math.abs(it.amount - 157.0) < 0.01 }
        assertEquals(1, txList2.size)
        assertEquals("壽司郎", txList2.first().description)
    }

    @Test
    fun testGoogleWalletIgnoredThenBankAlertSilentlyDropped() {
        val now = System.currentTimeMillis()

        // 1. Google Wallet arrives and is enqueued as pending
        val gwallet = PaymentNotificationListener.ParsedPayment(
            amount = 88.00,
            merchant = "Some Cafe",
            assetName = "Google Pay",
            sourcePackage = "com.google.android.apps.walletnfcrel",
            detectedAt = now,
            cardLast4 = "2101"
        )
        val enqueued = PendingPaymentStore.enqueue(
            context = context,
            sourcePackage = gwallet.sourcePackage,
            assetHint = gwallet.assetName,
            merchant = gwallet.merchant,
            amount = gwallet.amount,
            detectedAt = gwallet.detectedAt,
            cardLast4 = gwallet.cardLast4
        )
        assertNotNull(enqueued)
        assertEquals(1, PendingPaymentStore.pending.value.size)

        // 2. User explicitly ignores the notification
        PendingPaymentStore.ignore(context, enqueued!!.id)
        assertEquals(0, PendingPaymentStore.pending.value.size)

        // 3. Bank alert arrives 3 seconds later
        val bankAlert = PaymentNotificationListener.ParsedPayment(
            amount = 88.00,
            merchant = "Some Cafe",
            assetName = "Mox Card",
            sourcePackage = "com.mox.app",
            detectedAt = now + 3000L,
            cardLast4 = "2101"
        )
        val bankResult = PaymentNotificationListener.queuePayment(context, bankAlert)
        assertFalse("Companion bank alert should be silently dropped after user ignored the wallet alert", bankResult)
        assertEquals(0, PendingPaymentStore.pending.value.size)
        assertEquals(0, InMemoryDatabase.transactions.value.filter { kotlin.math.abs(it.amount - 88.0) < 0.01 }.size)
    }

    @Test
    fun testDistinctPurchasesAreNotDropped() {
        val now = System.currentTimeMillis()

        // Starbucks HK$40
        val alert1 = PaymentNotificationListener.ParsedPayment(
            amount = 40.00,
            merchant = "Starbucks",
            assetName = "Google Pay",
            sourcePackage = "com.google.android.apps.walletnfcrel",
            detectedAt = now,
            cardLast4 = "2101"
        )
        val p1 = PendingPaymentStore.enqueue(
            context = context,
            sourcePackage = alert1.sourcePackage,
            assetHint = alert1.assetName,
            merchant = alert1.merchant,
            amount = alert1.amount,
            detectedAt = alert1.detectedAt,
            cardLast4 = alert1.cardLast4
        )
        assertNotNull(p1)

        // 7-Eleven HK$40 (different merchant within 90s on same card)
        val alert2 = PaymentNotificationListener.ParsedPayment(
            amount = 40.00,
            merchant = "7-Eleven",
            assetName = "Mox Card",
            sourcePackage = "com.mox.app",
            detectedAt = now + 15_000L,
            cardLast4 = "2101"
        )
        val p2 = PendingPaymentStore.enqueue(
            context = context,
            sourcePackage = alert2.sourcePackage,
            assetHint = alert2.assetName,
            merchant = alert2.merchant,
            amount = alert2.amount,
            detectedAt = alert2.detectedAt,
            cardLast4 = alert2.cardLast4
        )
        assertNotNull("Distinct purchase at different merchant must NOT be dropped", p2)
        assertEquals("Both distinct payments should be present in pending", 2, PendingPaymentStore.pending.value.size)
    }
}
