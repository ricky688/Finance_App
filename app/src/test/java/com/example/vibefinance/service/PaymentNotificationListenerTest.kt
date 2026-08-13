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
}
