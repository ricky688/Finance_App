package com.example.vibefinance.data

import com.example.vibefinance.data.entity.NotificationTemplate
import com.example.vibefinance.data.entity.ParsedTransactionType
import com.example.vibefinance.data.entity.SlotToken
import com.example.vibefinance.data.entity.SlotType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import com.example.vibefinance.service.PaymentNotificationListener
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NotificationTemplateTest {

    @Test
    fun compileFromTokensAndMatchExpense() {
        val rawText = "您已在 星巴克 消費 HK$45.00 卡號 1234"
        val tokens = listOf(
            SlotToken(0, "您已在", SlotType.NONE),
            SlotToken(1, "星巴克", SlotType.MERCHANT),
            SlotToken(2, "消費", SlotType.NONE),
            SlotToken(3, "HK$45.00", SlotType.AMOUNT),
            SlotToken(4, "卡號", SlotType.NONE),
            SlotToken(5, "1234", SlotType.CARD_LAST4)
        )

        val template = NotificationTemplate.compileFromTokens(
            tokens = tokens,
            originalText = rawText,
            name = "HSBC Visa 模板",
            sourcePackage = "com.hsbc.hbap.mobilebanking",
            transactionType = ParsedTransactionType.EXPENSE
        )

        val result = template.match("您已在 太平洋咖啡 消費 HK$58.50 卡號 1234")
        assertNotNull(result)
        assertEquals(58.50, result!!.amount, 0.001)
        assertEquals("太平洋咖啡", result.merchant)
        assertEquals("1234", result.cardLast4)
        assertEquals(ParsedTransactionType.EXPENSE, result.transactionType)
    }

    @Test
    fun compileFromTokensAndMatchIncome() {
        val rawText = "薪金入帳 HK$32000.00 來自 科技公司"
        val tokens = listOf(
            SlotToken(0, "薪金入帳", SlotType.NONE),
            SlotToken(1, "HK$32000.00", SlotType.AMOUNT),
            SlotToken(2, "來自", SlotType.NONE),
            SlotToken(3, "科技公司", SlotType.MERCHANT)
        )

        val template = NotificationTemplate.compileFromTokens(
            tokens = tokens,
            originalText = rawText,
            name = "薪金入帳模板",
            sourcePackage = "com.hangseng.banking",
            transactionType = ParsedTransactionType.INCOME
        )

        val result = template.match("薪金入帳 HK$35000.00 來自 科技公司")
        assertNotNull(result)
        assertEquals(35000.00, result!!.amount, 0.001)
        assertEquals("科技公司", result.merchant)
        assertEquals(ParsedTransactionType.INCOME, result.transactionType)
    }

    @Test
    fun jsonSerializationRoundtrip() {
        val template = NotificationTemplate(
            id = "test-template-1",
            name = "渣打卡消費模板",
            sourcePackage = "com.sc.hk",
            rawSample = "SCB alert",
            regexPattern = "(?i)spent\\s+([0-9.]+)\\s+at\\s+(.+)",
            amountGroup = 1,
            merchantGroup = 2,
            cardLast4Group = null,
            transactionType = ParsedTransactionType.EXPENSE,
            matchCount = 5
        )

        val json = template.toJson()
        val deserialized = NotificationTemplate.fromJson(json)

        assertNotNull(deserialized)
        assertEquals(template.id, deserialized!!.id)
        assertEquals(template.name, deserialized.name)
        assertEquals(template.sourcePackage, deserialized.sourcePackage)
        assertEquals(template.regexPattern, deserialized.regexPattern)
        assertEquals(template.amountGroup, deserialized.amountGroup)
        assertEquals(template.merchantGroup, deserialized.merchantGroup)
        assertEquals(template.transactionType, deserialized.transactionType)
        assertEquals(template.matchCount, deserialized.matchCount)
    }

    @Test
    fun inMemoryDatabaseTemplateOperations() {
        val template = NotificationTemplate(
            id = "db-test-template",
            name = "測試模板",
            sourcePackage = "com.test.bank",
            regexPattern = "(?i)Paid\\s+HK\\$\\s*([0-9.]+)\\s+to\\s+([^,]+)",
            amountGroup = 1,
            merchantGroup = 2,
            transactionType = ParsedTransactionType.EXPENSE
        )

        InMemoryDatabase.saveNotificationTemplate(template)

        val matchPair = InMemoryDatabase.findMatchingTemplate("com.test.bank", "Paid HK$ 100.00 to Supermarket")
        assertNotNull(matchPair)
        val (matchedTemplate, matchResult) = matchPair!!
        assertEquals(100.00, matchResult.amount, 0.001)
        assertEquals("Supermarket", matchResult.merchant)
        assertEquals("db-test-template", matchedTemplate.id)

        InMemoryDatabase.deleteNotificationTemplate(template.id)
        val matchAfterDelete = InMemoryDatabase.findMatchingTemplate("com.test.bank", "Paid HK$ 100.00 to Supermarket")
        assertNull(matchAfterDelete)
    }

    @Test
    fun testFpsReceiveTransferEndToEndWithTemplate() {
        val rawText = "收款成功：收到轉帳 HK$ 500.00 來自 陳大文"
        val tokens = listOf(
            SlotToken(0, "收款成功：收到轉帳", SlotType.NONE),
            SlotToken(1, "HK$ 500.00", SlotType.AMOUNT),
            SlotToken(2, "來自", SlotType.NONE),
            SlotToken(3, "陳大文", SlotType.MERCHANT)
        )

        val template = NotificationTemplate.compileFromTokens(
            tokens = tokens,
            originalText = rawText,
            name = "轉數快收款模板",
            sourcePackage = "hk.com.hkicl.fps",
            transactionType = ParsedTransactionType.INCOME
        )

        InMemoryDatabase.saveNotificationTemplate(template)

        try {
            val title = "轉數快"
            val text = "收款成功：收到轉帳 HK$ 800.00 來自 李小龍"
            val pkg = "hk.com.hkicl.fps"

            // 1. Template enables intercept even for non-expense alerts
            assertTrue(
                "Template must allow intercept",
                PaymentNotificationListener.isPaymentNotification(title, text, pkg)
            )

            // 2. Parse notification correctly tags as INCOME
            val parsed = PaymentNotificationListener.parseNotification(title, text, pkg)
            assertNotNull(parsed)
            assertEquals(800.00, parsed!!.amount, 0.001)
            assertEquals("李小龍", parsed.merchant)
            assertEquals("INCOME", parsed.transactionType)
        } finally {
            InMemoryDatabase.deleteNotificationTemplate(template.id)
        }
    }

    @Test
    fun testBankReceiveTransferBalanceIncreaseWhenAccepted() {
        val rawText = "收到來自 周杰倫 的轉帳 HK$ 1200.00"
        val tokens = listOf(
            SlotToken(0, "收到來自", SlotType.NONE),
            SlotToken(1, "周杰倫", SlotType.MERCHANT),
            SlotToken(2, "的轉帳", SlotType.NONE),
            SlotToken(3, "HK$ 1200.00", SlotType.AMOUNT)
        )

        val template = NotificationTemplate.compileFromTokens(
            tokens = tokens,
            originalText = rawText,
            name = "銀行轉入收款",
            sourcePackage = "com.hangseng.banking",
            transactionType = ParsedTransactionType.INCOME
        )

        InMemoryDatabase.saveNotificationTemplate(template)

        val context = org.robolectric.RuntimeEnvironment.getApplication()
        InMemoryDatabase.initialize(context)

        val account = com.example.vibefinance.data.entity.AccountEntity(
            id = 99L,
            name = "恒生儲蓄戶口",
            type = com.example.vibefinance.data.entity.AccountType.BANK,
            balance = 5000.0,
            icon = "bank"
        )
        InMemoryDatabase.accounts.value = listOf(account)

        try {
            val title = "恒生銀行"
            val text = "收到來自 周杰倫 的轉帳 HK$ 1500.00"
            val pkg = "com.hangseng.banking"

            val parsed = PaymentNotificationListener.parseNotification(title, text, pkg)
            assertNotNull("Template should parse receiving alert", parsed)
            assertEquals(1500.00, parsed!!.amount, 0.001)
            assertEquals("周杰倫", parsed.merchant)
            assertEquals("INCOME", parsed.transactionType)

            // Enqueue and accept payment into account
            val queued = com.example.vibefinance.service.PendingPaymentStore.enqueue(
                context = context,
                sourcePackage = pkg,
                assetHint = parsed.assetName,
                merchant = parsed.merchant,
                amount = parsed.amount,
                detectedAt = System.currentTimeMillis(),
                transactionType = parsed.transactionType
            )
            assertNotNull(queued)

            val accepted = com.example.vibefinance.service.PendingPaymentStore.accept(
                context = context,
                paymentId = queued!!.id,
                accountId = account.id,
                rememberChoice = false
            )
            assertTrue("Payment must be accepted", accepted)

            // Check that transaction is recorded as income (excluded from daily budget)
            val tx = InMemoryDatabase.transactions.value.firstOrNull { it.description == "周杰倫" }
            assertNotNull("Transaction must be recorded", tx)
            assertEquals(-1500.00, tx!!.amount, 0.001) // Income is represented as negative expense
            assertTrue("Income must be excluded from daily budget", tx.isExcludedFromDailyBudget)
        } finally {
            InMemoryDatabase.deleteNotificationTemplate(template.id)
        }
    }

    @Test
    fun testOctopusTopUpAtomicTransferBalances() {
        val context = org.robolectric.RuntimeEnvironment.getApplication()
        InMemoryDatabase.initialize(context)
        InMemoryDatabase.transactions.value = emptyList()
        com.example.vibefinance.service.PendingPaymentStore.initialize(context)
        val pField = com.example.vibefinance.service.PendingPaymentStore::class.java.getDeclaredField("_pending")
        pField.isAccessible = true
        (pField.get(com.example.vibefinance.service.PendingPaymentStore) as kotlinx.coroutines.flow.MutableStateFlow<List<com.example.vibefinance.service.PendingPayment>>).value = emptyList()
        val sField = com.example.vibefinance.service.PendingPaymentStore::class.java.getDeclaredField("seen")
        sField.isAccessible = true
        (sField.get(com.example.vibefinance.service.PendingPaymentStore) as MutableMap<*, *>).clear()

        val ccAccount = com.example.vibefinance.data.entity.AccountEntity(
            id = 201L,
            name = "恒生信用卡",
            cardLast4 = "1691",
            type = com.example.vibefinance.data.entity.AccountType.CC,
            balance = 1000.0,
            icon = "credit_card"
        )
        val octopusAccount = com.example.vibefinance.data.entity.AccountEntity(
            id = 202L,
            name = "八達通",
            type = com.example.vibefinance.data.entity.AccountType.CASH,
            balance = 50.0,
            icon = "octopus"
        )
        InMemoryDatabase.accounts.value = listOf(ccAccount, octopusAccount)

        val title = "恒生信用卡"
        val text = "你已於08/10/2026以信用卡最後數字1691於OCL* OCTOPUS AD1315289 簽賬HK$300.00"
        val pkg = "com.hangseng.rbmobile"

        val parsed = PaymentNotificationListener.parseNotification(title, text, pkg)
        assertNotNull(parsed)
        assertTrue(parsed!!.isTopUp)
        assertEquals("TRANSFER", parsed.transactionType)

        val queued = com.example.vibefinance.service.PendingPaymentStore.enqueue(
            context = context,
            sourcePackage = pkg,
            assetHint = parsed.assetName,
            merchant = parsed.merchant,
            amount = parsed.amount,
            detectedAt = System.currentTimeMillis(),
            cardLast4 = parsed.cardLast4,
            isTopUp = parsed.isTopUp,
            transactionType = parsed.transactionType
        )
        assertNotNull(queued)

        val accepted = com.example.vibefinance.service.PendingPaymentStore.accept(
            context = context,
            paymentId = queued!!.id,
            accountId = ccAccount.id,
            rememberChoice = false
        )
        assertTrue("Top-up transfer must be accepted", accepted)

        val recordedTx = InMemoryDatabase.transactions.value.firstOrNull { it.groupId == "notification:${queued.id}" }
        assertNotNull("Transfer transaction must be recorded", recordedTx)
        assertEquals(300.0, recordedTx!!.amount, 0.001)
        assertEquals("Top-up", recordedTx.category)
        assertEquals(ccAccount.id, recordedTx.accountId)
        assertEquals(octopusAccount.id, recordedTx.toAccountId)
        assertTrue("Must be excluded from daily expense budget", recordedTx.isExcludedFromDailyBudget)

        // Verify atomic balances: CC debt increases by 300 (1000 -> 1300), Octopus balance increases by 300 (50 -> 350)
        val accountsMap = InMemoryDatabase.accounts.value.associateBy { it.id }
        assertEquals(1300.0, accountsMap[ccAccount.id]?.balance ?: 0.0, 0.001)
        assertEquals(350.0, accountsMap[octopusAccount.id]?.balance ?: 0.0, 0.001)
    }

    @Test
    fun testOctopusTopUpCrossPackageDeduplication() {
        val context = org.robolectric.RuntimeEnvironment.getApplication()
        InMemoryDatabase.initialize(context)
        InMemoryDatabase.transactions.value = emptyList()
        com.example.vibefinance.service.PendingPaymentStore.initialize(context)
        val pField = com.example.vibefinance.service.PendingPaymentStore::class.java.getDeclaredField("_pending")
        pField.isAccessible = true
        (pField.get(com.example.vibefinance.service.PendingPaymentStore) as kotlinx.coroutines.flow.MutableStateFlow<List<com.example.vibefinance.service.PendingPayment>>).value = emptyList()
        val sField = com.example.vibefinance.service.PendingPaymentStore::class.java.getDeclaredField("seen")
        sField.isAccessible = true
        (sField.get(com.example.vibefinance.service.PendingPaymentStore) as MutableMap<*, *>).clear()

        val now = System.currentTimeMillis()
        val first = com.example.vibefinance.service.PendingPaymentStore.enqueue(
            context = context,
            sourcePackage = "com.hangseng.rbmobile",
            assetHint = "Card ••1691",
            merchant = "OCL* OCTOPUS AD1315289",
            amount = 300.0,
            detectedAt = now,
            cardLast4 = "1691",
            isTopUp = true,
            transactionType = "TRANSFER"
        )
        assertNotNull("First alert must be enqueued", first)

        // Concurrent Samsung Wallet alert for the same top-up within 10s
        val second = com.example.vibefinance.service.PendingPaymentStore.enqueue(
            context = context,
            sourcePackage = "com.samsung.android.spay",
            assetHint = "Smart Octopus",
            merchant = "OCL* OCTOPUS AD1315289",
            amount = 300.0,
            detectedAt = now + 10_000L,
            isTopUp = true,
            transactionType = "TRANSFER"
        )
        assertNull("Duplicate top-up notification from another package must be dropped", second)
    }
}
