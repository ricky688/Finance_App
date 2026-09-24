package com.example.vibefinance.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PaymentNotificationListenerTest {

    @Test
    fun testParseGoogleWalletNotification() {
        val title = "Sushiro HK"
        val text = "金額：HK$157.00；付款卡：AEON Mastercard Credit Card ••2101"
        val pkg = "com.google.android.apps.walletnfcrel"

        val parsed = PaymentNotificationListener.parseNotification(title, text, pkg)
        assertNotNull(parsed)
        assertEquals(157.00, parsed!!.amount, 0.001)
        assertEquals("Sushiro HK", parsed.merchant)
        assertEquals("AEON Mastercard Credit Card ••2101", parsed.assetName)
    }

    @Test
    fun testParseSamsungWalletSmartOctopusNotification() {
        val title = "Smart Octopus HK$7.70 Citybus"
        val text = "Remaining value: HK$245.50"
        val pkg = "com.samsung.android.spay"

        val parsed = PaymentNotificationListener.parseNotification(title, text, pkg)
        assertNotNull(parsed)
        assertEquals(7.70, parsed!!.amount, 0.001)
        assertEquals("Citybus", parsed.merchant)
        assertEquals("Smart Octopus", parsed.assetName)
    }

    @Test
    fun testParseBankAlertNotification() {
        val title = "HSBC Card Transaction Alert"
        val text = "You spent HK$350.00 at PARKnSHOP Supermarket using Visa ••4002"
        val pkg = "com.hsbc.hbap.mobilebanking"

        val parsed = PaymentNotificationListener.parseNotification(title, text, pkg)
        assertNotNull(parsed)
        assertEquals(350.00, parsed!!.amount, 0.001)
        assertEquals("HSBC", parsed.assetName)
    }

    @Test
    fun testParsePayMeNotification() {
        val title = "PayMe"
        val text = "Paid HK$45.00 to Starbucks"
        val pkg = "hk.com.hsbc.payme"

        val parsed = PaymentNotificationListener.parseNotification(title, text, pkg)
        assertNotNull(parsed)
        assertEquals(45.00, parsed!!.amount, 0.001)
        assertEquals("PayMe", parsed.assetName)
    }

    @Test
    fun testDetermineCategoryFoodAndDrink() {
        val cat = PaymentNotificationListener.determineCategory("Sushiro HK")
        assertEquals("Food & Drink", cat)
    }

    @Test
    fun testDetermineCategoryTransport() {
        val cat = PaymentNotificationListener.determineCategory("Citybus")
        assertEquals("Transport", cat)
    }

    @Test
    fun testDetermineCategoryGroceries() {
        val cat = PaymentNotificationListener.determineCategory("PARKnSHOP Supermarket")
        assertEquals("Groceries", cat)
    }

    @Test
    fun testDetermineCategoryUtilities() {
        val cat = PaymentNotificationListener.determineCategory("CLP Power HK")
        assertEquals("Utilities", cat)
    }

    @Test
    fun testParseFpsNotification() {
        val title = "FPS 轉數快"
        val text = "Sent HK$120.00 via FPS to Pacific Coffee"
        val pkg = "hk.com.hkicl.fps"

        val parsed = PaymentNotificationListener.parseNotification(title, text, pkg)
        assertNotNull(parsed)
        assertEquals(120.00, parsed!!.amount, 0.001)
        assertEquals("FPS", parsed.assetName)
    }

    @Test
    fun testParseAlipayNotification() {
        val title = "AlipayHK"
        val text = "Spent HK$65.00 at 7-Eleven"
        val pkg = "hk.alipay.payment"

        val parsed = PaymentNotificationListener.parseNotification(title, text, pkg)
        assertNotNull(parsed)
        assertEquals(65.00, parsed!!.amount, 0.001)
        assertEquals("Alipay", parsed.assetName)
    }

    @Test
    fun testParseWeChatPayNotification() {
        val title = "WeChat Pay HK"
        val text = "Paid HK$32.00 at Circle K"
        val pkg = "com.tencent.mm"

        val parsed = PaymentNotificationListener.parseNotification(title, text, pkg)
        assertNotNull(parsed)
        assertEquals(32.00, parsed!!.amount, 0.001)
        assertEquals("WeChat Pay", parsed.assetName)
    }

    @Test
    fun testParseSlightlyDifferentWordingTraditionalChinese() {
        val title = "中銀香港"
        val text = "成功付款 HK$15.00 於 7-Eleven"
        val pkg = "com.boc.bank"

        val parsed = PaymentNotificationListener.parseNotification(title, text, pkg)
        assertNotNull(parsed)
        assertEquals(15.00, parsed!!.amount, 0.001)
        assertEquals("Bank of China", parsed.assetName)
    }

    @Test
    fun testParseSlightlyDifferentWordingHsbcExpense() {
        val title = "HSBC Alert"
        val text = "支出 $420.00 於 百佳超級市場"
        val pkg = "com.hsbc.hbap"

        val parsed = PaymentNotificationListener.parseNotification(title, text, pkg)
        assertNotNull(parsed)
        assertEquals(420.00, parsed!!.amount, 0.001)
        assertEquals("HSBC", parsed.assetName)
    }

    @Test
    fun testParseSlightlyDifferentWordingTransferredText() {
        val title = "PayMe Transaction"
        val text = "Transferred HK$250.00 to Starbucks"
        val pkg = "hk.com.hsbc.payme"

        val parsed = PaymentNotificationListener.parseNotification(title, text, pkg)
        assertNotNull(parsed)
        assertEquals(250.00, parsed!!.amount, 0.001)
        assertEquals("PayMe", parsed.assetName)
    }

    @Test
    fun testNonPaymentNotificationReturnsNull() {
        val title = "System Update"
        val text = "Your phone software is up to date."
        val pkg = "com.android.settings"

        val parsed = PaymentNotificationListener.parseNotification(title, text, pkg)
        assertNull(parsed)
    }

    @Test
    fun testSystemNotificationWithNumbersReturnsNull() {
        // e.g. Play Store update notification
        val title = "Google Play Store"
        val text = "2 updates available: Google and Google Messages (45.5 MB)"
        val pkg = "com.android.vending"

        val parsed = PaymentNotificationListener.parseNotification(title, text, pkg)
        assertNull(parsed)
    }

    @Test
    fun testOtpVerificationCodeReturnsNull() {
        val title = "HSBC Security"
        val text = "Your HSBC one-time password OTP is 492019 for payment of HK$500.00 at Apple Store. Do not share."
        val pkg = "com.hsbc.hbap.mobilebanking"

        val parsed = PaymentNotificationListener.parseNotification(title, text, pkg)
        assertNull(parsed)
    }

    @Test
    fun testMarketingPromoBannerReturnsNull() {
        val title = "HSBC Special Offer"
        val text = "Get up to $500 discount when you apply for the new HSBC Visa Signature Card today!"
        val pkg = "com.hsbc.hbap.mobilebanking"

        val parsed = PaymentNotificationListener.parseNotification(title, text, pkg)
        assertNull(parsed)
    }

    @Test
    fun testLoginAlertReturnsNull() {
        val title = "Security Alert"
        val text = "New login detected on your Chase account from Windows Chrome at 08:30 PM."
        val pkg = "com.chase.sig.android"

        val parsed = PaymentNotificationListener.parseNotification(title, text, pkg)
        assertNull(parsed)
    }

    @Test
    fun testInterceptableAppMatching() {
        val googlePay = com.example.vibefinance.data.entity.InterceptableApp.GOOGLE_PAY
        // Real Google Wallet package
        org.junit.Assert.assertTrue(googlePay.matches("com.google.android.apps.walletnfcrel", "Sushiro", "金額：HK$100"))
        // Generic Google app or system package must NOT match
        org.junit.Assert.assertFalse(googlePay.matches("com.google.android.gms", "System", "Update"))
        org.junit.Assert.assertFalse(googlePay.matches("com.google.android.apps.messaging", "Mom", "Hello $50"))
        org.junit.Assert.assertFalse(googlePay.matches("com.google.android.googlequicksearchbox", "News", "Article"))

        val weChat = com.example.vibefinance.data.entity.InterceptableApp.WECHAT_PAY
        // Regular WeChat chat must NOT match
        org.junit.Assert.assertFalse(weChat.matches("com.tencent.mm", "Friend", "Hey check this out"))
        // Official WeChat Pay notification MUST match
        org.junit.Assert.assertTrue(weChat.matches("com.tencent.mm", "微信支付", "已付款 HK$50.00"))
    }
}
