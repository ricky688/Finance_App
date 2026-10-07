package com.example.vibefinance.service

import com.example.vibefinance.data.entity.ParsedTransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionGrammarEngineTest {

    @Test
    fun parseChinesePrepositionAnchors() {
        val testCases = listOf(
            Triple("消費通知", "在 星巴克 消費 HK$45.00 卡號末四位 1234", "星巴克" to 45.0),
            Triple("扣款提醒", "於 壽司郎 支出 120.50 元", "壽司郎" to 120.50),
            Triple("付款通知", "向 屈臣氏 付款 HK$88.00 完成", "屈臣氏" to 88.00),
            Triple("交易通知", "商戶：百佳超級市場 消費金額 HK$ 250.00", "百佳超級市場" to 250.00)
        )

        for ((title, text, expected) in testCases) {
            val result = TransactionGrammarEngine.parse(title, text, "com.generic.bank")
            assertNotNull("Expected parse result for: $text", result)
            assertEquals("Merchant mismatch for: $text", expected.first, result!!.merchant)
            assertEquals("Amount mismatch for: $text", expected.second, result.amount, 0.001)
            assertEquals(ParsedTransactionType.EXPENSE, result.intent)
            assertTrue(result.confidence >= 0.9f)
        }
    }

    @Test
    fun parseEnglishPrepositionAnchors() {
        val testCases = listOf(
            Triple("Card Alert", "Transaction of HKD 128.50 at Starbucks on card ending in 5678", "Starbucks" to 128.50),
            Triple("Payment Confirmation", "Paid HK$ 64.00 to McDonald's via card 4321", "McDonald's" to 64.0),
            Triple("Bank Alert", "HK$ 350.00 charged by Apple Store on card ending in 9988", "Apple Store" to 350.0),
            Triple("Transaction Alert", "Merchant: Wellcome, Amount: HK$ 150.00", "Wellcome" to 150.0)
        )

        for ((title, text, expected) in testCases) {
            val result = TransactionGrammarEngine.parse(title, text, "com.generic.bank")
            assertNotNull("Expected parse result for: $text", result)
            assertEquals("Merchant mismatch for: $text", expected.first, result!!.merchant)
            assertEquals("Amount mismatch for: $text", expected.second, result.amount, 0.001)
            assertEquals(ParsedTransactionType.EXPENSE, result.intent)
            assertTrue(result.confidence >= 0.9f)
        }
    }

    @Test
    fun extractCardLast4Digits() {
        val textWithEnding = "Paid HK$ 100.00 at Store ending in 7788"
        assertEquals("7788", TransactionGrammarEngine.extractCardLast4("Alert", textWithEnding))

        val textWithZhCard = "在 百貨公司 刷卡 HK$500.00 卡號尾號 3322"
        assertEquals("3322", TransactionGrammarEngine.extractCardLast4("通知", textWithZhCard))
    }

    @Test
    fun intentDetection() {
        // Income
        val incomeSalary = TransactionGrammarEngine.detectIntent("薪金通知", "薪金存入 HK$35,000.00 來自 科技公司")
        assertEquals(ParsedTransactionType.INCOME, incomeSalary)

        val salaryEn = TransactionGrammarEngine.detectIntent("Bank Alert", "Salary credited HK$ 42,000.00 to your account")
        assertEquals(ParsedTransactionType.INCOME, salaryEn)

        // Repayment
        val repaymentZh = TransactionGrammarEngine.detectIntent("還款通知", "信用卡還款 HK$ 5,000.00 成功完成")
        assertEquals(ParsedTransactionType.REPAYMENT, repaymentZh)

        val repaymentEn = TransactionGrammarEngine.detectIntent("Card Services", "Card payment repayment HK$ 1,200.00 received")
        assertEquals(ParsedTransactionType.REPAYMENT, repaymentEn)

        // Expense
        val expenseZh = TransactionGrammarEngine.detectIntent("扣款通知", "在 超級市場 消費 HK$ 80.00")
        assertEquals(ParsedTransactionType.EXPENSE, expenseZh)
    }

    @Test
    fun negativeFilterRejectsSpamOtpAndSecurity() {
        val spamTexts = listOf(
            "Your OTP is 123456 for online purchase verification" to "OTP Alert",
            "您的安全碼是 889900，請勿洩漏給他人" to "驗證碼",
            "信用卡最新推廣：高達 10% 現金回贈，立即申請" to "優惠推廣",
            "登入通知：您的戶口於新裝置登入" to "安全性提醒",
            "Your monthly e-statement is ready to view" to "Monthly Statement",
            "Payment reminder: HK$ 500.00 is due on 15 Oct" to "Reminder"
        )

        for ((text, title) in spamTexts) {
            assertTrue("Should be detected as spam/security: $text", TransactionGrammarEngine.isSpamOrSecurityNotice(title, text))
            assertNull("Engine parse must reject spam/security: $text", TransactionGrammarEngine.parse(title, text, "com.bank.app"))
        }
    }
}
