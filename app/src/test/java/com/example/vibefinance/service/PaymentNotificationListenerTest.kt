package com.example.vibefinance.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentNotificationListenerTest {

    @Test
    fun refundsAndCreditsAreNotSpending() {
        val alerts = listOf(
            "Your card payment of HK$200.00 was refunded" to "com.hsbc.hbap.mobilebanking",
            "HK$200.00 credited to your card after a purchase reversal" to "com.hsbc.hbap.mobilebanking",
            "退款成功，HK$200.00 已退回信用卡" to "hk.alipay.payment"
        )
        for ((message, source) in alerts) {
            assertFalse(PaymentNotificationListener.isPaymentNotification("Payment alert", message))
            assertNull(PaymentNotificationListener.parseNotification("Payment alert", message, source))
        }
    }

    @Test
    fun incomingTransfersAndDepositsAreNotSpending() {
        val alerts = listOf(
            "You received HK$2,500.00 via FPS from Alex",
            "Incoming transfer: HK$2,500.00 deposited into your account",
            "收款成功，轉數快 HK$2,500.00 已存入您的戶口"
        )
        for (message in alerts) {
            assertFalse(PaymentNotificationListener.isPaymentNotification("FPS payment", message))
            assertNull(PaymentNotificationListener.parseNotification("FPS payment", message, "hk.com.hkicl.fps"))
        }
    }

    @Test
    fun walletTopUpsAndBalanceOnlyAlertsAreNotSpending() {
        assertNull(PaymentNotificationListener.parseNotification(
            "Octopus payment", "Top-up HK$100.00 completed", "com.samsung.android.spay"
        ))
        assertNull(PaymentNotificationListener.parseNotification(
            "Card payment", "Available balance HK$5,000.00", "com.hsbc.hbap.mobilebanking"
        ))
        assertNull(PaymentNotificationListener.parseNotification(
            "Card payment reminder", "HK$500.00 is due tomorrow", "com.hsbc.hbap.mobilebanking"
        ))
        assertNull(PaymentNotificationListener.parseNotification(
            "Card payment", "Payment failed for HK$500.00", "com.hsbc.hbap.mobilebanking"
        ))
    }

    @Test
    fun multipleDifferentAmountsAreNotBookedAsAnExpense() {
        val title = "Card payment"
        val text = "You spent HK$20.00 at Store; a second amount HK$5,000.00 is shown"
        assertTrue(PaymentNotificationListener.isPaymentNotification(title, text))
        assertNull(PaymentNotificationListener.parseNotification(
            title, text, "com.hsbc.hbap.mobilebanking"
        ))
        assertNull(PaymentNotificationListener.parseNotification(
            title, "Available balance HK$5,000.00 after payment HK$20.00",
            "com.hsbc.hbap.mobilebanking"
        ))
    }

    @Test
    fun explicitOutgoingPaymentStillParses() {
        val outgoing = PaymentNotificationListener.parseNotification(
            "FPS transfer", "Sent HK$120.00 via FPS to Pacific Coffee", "hk.com.hkicl.fps"
        )
        assertNotNull(outgoing)
        assertEquals(120.00, outgoing!!.amount, 0.001)
        assertNotNull(PaymentNotificationListener.parseNotification(
            "Samsung Wallet", "OCL* OCTOPUS AD1741037 HK$300.00", "com.samsung.android.spay"
        ))
        val googleWallet = PaymentNotificationListener.parseNotification(
            "Sushiro HK", "Amount: HK$42.00; Card: Visa ••2101",
            "com.google.android.apps.walletnfcrel"
        )
        assertNotNull(googleWallet)
        assertEquals(42.00, googleWallet!!.amount, 0.001)
    }

    @Test
    fun foreignCurrencyPaymentIsNotRecordedAsHongKongDollars() {
        val parsed = PaymentNotificationListener.parseNotification(
            "Card payment",
            "You spent USD 42.00 at a store",
            "com.hsbc.hbap.mobilebanking"
        )
        assertNull(parsed)
    }

    @Test
    fun rememberChoiceRequiresASpecificAccountHint() {
        val account = com.example.vibefinance.data.entity.AccountEntity(
            id = 7L,
            name = "AEON card",
            type = com.example.vibefinance.data.entity.AccountType.CC,
            balance = 0.0,
            icon = "credit_card"
        )
        val generic = PendingPayment(
            id = "generic",
            fingerprint = "generic",
            sourcePackage = "com.example.wallet",
            assetHint = "Google Pay",
            merchant = "Store",
            amount = 42.0,
            detectedAt = 1L
        )
        assertFalse(PendingPaymentStore.canRememberChoice(generic, account))
        assertTrue(PendingPaymentStore.canRememberChoice(
            generic.copy(assetHint = "AEON card ••4001"), account
        ))
        assertTrue(PendingPaymentStore.canRememberChoice(
            generic.copy(assetHint = "PayMe"), account.copy(name = "PayMe")
        ))
        assertFalse(PendingPaymentStore.canRememberChoice(
            generic.copy(assetHint = "HSBC"), account.copy(name = "HSBC")
        ))
        assertTrue(PendingPaymentStore.canRememberChoice(
            generic.copy(assetHint = "Smart Octopus"), account.copy(name = "八達通")
        ))
        assertTrue(PendingPaymentStore.canRememberChoice(
            generic.copy(assetHint = "Smart Octopus"), account.copy(name = "Octopus")
        ))
        assertFalse(PendingPaymentStore.canRememberChoice(
            generic.copy(assetHint = "Smart Octopus"), account.copy(name = "AEON card")
        ))
    }

    @Test
    fun bocGoUnionPayTransitTicketPurchaseKeepsTheCardName() {
        val title = "BOC Go UnionPay Diamond"
        val text = "15:00 Transit-ticket THE KOWLOOHONGKONG HKG HK$3.60"
        assertTrue(PaymentNotificationListener.isPaymentNotification(title, text))

        val payment = PaymentNotificationListener.parseNotification(
            title, text, "com.bankofchina.bochk.mobileapplication"
        )
        assertNotNull(payment)
        assertEquals(3.60, payment!!.amount, 0.001)
        assertEquals(title, payment.assetName)
        assertEquals("Transit-ticket THE KOWLOOHONGKONG HKG", payment.merchant)
        assertEquals("Transport", PaymentNotificationListener.determineCategory(payment.merchant))

        val related = PaymentNotificationListener.parseNotification(
            title, "Transit ticket MTR HKG 5.50 HKD", "com.bankofchina.bochk.mobileapplication"
        )
        assertEquals(5.50, related!!.amount, 0.001)
        assertEquals(title, related.assetName)
    }

    @Test
    fun bocGoUnionPayTransitRuleDoesNotTurnOtherAlertsIntoSpending() {
        val title = "BOC Go UnionPay Diamond"
        val rejected = listOf(
            "Transit-ticket THE KOWLOOHONGKONG HKG HK$3.60 refunded",
            "Transit-ticket THE KOWLOOHONGKONG HKG HK$3.60, available balance HK$100.00",
            "Transit-ticket THE KOWLOOHONGKONG HKG HK$3.60 and HK$4.20",
            "Promotion: Transit-ticket THE KOWLOOHONGKONG HKG HK$3.60",
            "Transit-ticket HK$3.60",
            "Transit-ticket THE KOWLOOHONGKONG HKG USD 3.60"
        )
        rejected.forEach { text ->
            assertNull(PaymentNotificationListener.parseNotification(
                title, text, "com.bankofchina.bochk.mobileapplication"
            ))
        }
        assertNull(PaymentNotificationListener.parseNotification(
            title, "Card update HK$3.60", "com.bankofchina.bochk.mobileapplication"
        ))
    }

    @Test
    fun shortenedBocCardTitleCannotRememberAnAutomaticAccountChoice() {
        val account = com.example.vibefinance.data.entity.AccountEntity(
            id = 17L,
            name = "BOC Go UnionPay Diamond",
            type = com.example.vibefinance.data.entity.AccountType.CC,
            balance = 0.0,
            icon = "credit_card"
        )
        val payment = PendingPayment(
            id = "boc",
            fingerprint = "boc",
            sourcePackage = "com.bankofchina.bochk.mobileapplication",
            assetHint = account.name,
            merchant = "Transit-ticket THE KOWLOOHONGKONG HKG",
            amount = 3.60,
            detectedAt = 1L
        )
        assertTrue(PendingPaymentStore.canRememberChoice(payment, account))
        assertFalse(PendingPaymentStore.canRememberChoice(
            payment.copy(assetHint = "BOC Go UnionPay Diamond..."), account
        ))
        assertFalse(PendingPaymentStore.canRememberChoice(
            payment.copy(assetHint = "BOC Go UnionPay Dia…"), account
        ))
    }

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
    fun smartOctopusMtrFareUsesTitleAmountWhenOctopusAppPostsIt() {
        val title = "Smart Octopus HK$12.6 港鐵"
        val text = "Remaining value: HK$245.50"
        val parsed = PaymentNotificationListener.parseNotification(
            title, text, "com.octopuscards.nfc_reader"
        )

        assertNotNull(parsed)
        assertEquals(12.6, parsed!!.amount, 0.001)
        assertEquals("港鐵", parsed.merchant)
        assertEquals("Smart Octopus", parsed.assetName)
        assertEquals("Transport", PaymentNotificationListener.determineCategory(parsed.merchant))
    }

    @Test
    fun testParseBankAlertNotification() {
        val title = "HSBC Card Transaction Alert"
        val text = "You spent HK$350.00 at PARKnSHOP Supermarket using Visa ••4002"
        val pkg = "com.hsbc.hbap.mobilebanking"

        val parsed = PaymentNotificationListener.parseNotification(title, text, pkg)
        assertNotNull(parsed)
        assertEquals(350.00, parsed!!.amount, 0.001)
        assertEquals("Visa ••4002", parsed.assetName)
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

    @Test
    fun testAuthenticCardLast4Extraction() {
        // English formats
        assertEquals("4321", PaymentNotificationListener.extractCardLast4("Your Visa card ending in 4321 was charged HK$150.00 at Starbucks."))
        assertEquals("9876", PaymentNotificationListener.extractCardLast4("You spent HK$88.00 with your card ending with 9876."))
        assertEquals("2101", PaymentNotificationListener.extractCardLast4("Amount: HK$42.00; Card: Visa ••2101", "Sushiro HK"))
        assertEquals("5678", PaymentNotificationListener.extractCardLast4("Paid HK$200.00 with Mastercard **** 5678 at Wellcome"))
        assertEquals("1122", PaymentNotificationListener.extractCardLast4("Amex 1122 charged HK$500.00 at Citysuper"))

        // Chinese formats
        assertEquals("8888", PaymentNotificationListener.extractCardLast4("您的中銀信用卡尾號8888已於壽司郎成功消費HK$180.00"))
        assertEquals("1234", PaymentNotificationListener.extractCardLast4("恒生信用卡末4位1234於09:30已扣款HK$120.00"))
        assertEquals("9999", PaymentNotificationListener.extractCardLast4("付款卡：恒生信用卡 (9999)"))
        assertEquals("2345", PaymentNotificationListener.extractCardLast4("Visa卡(尾號2345)消費港幣$50.00"))
        assertEquals("6789", PaymentNotificationListener.extractCardLast4("信用卡末四位 6789 已完成付款 HK$300.00"))

        // Non-card payments MUST return null (strictly deterministic, never random)
        assertNull(PaymentNotificationListener.extractCardLast4("Sent HK$120.00 via FPS to Pacific Coffee"))
        assertNull(PaymentNotificationListener.extractCardLast4("OCL* OCTOPUS AD1741037 HK$300.00"))
        assertNull(PaymentNotificationListener.extractCardLast4("You paid HK$50.00 with PayMe to ABC Bakery"))
    }

    @Test
    fun testParseNotificationCarriesCardLast4() {
        val googleWallet = PaymentNotificationListener.parseNotification(
            "Sushiro HK",
            "Amount: HK$42.00; Card: Visa ••2101",
            "com.google.android.apps.walletnfcrel"
        )
        assertNotNull(googleWallet)
        assertEquals("2101", googleWallet!!.cardLast4)

        val bankAlert = PaymentNotificationListener.parseNotification(
            "HSBC Alert",
            "Your HSBC Visa card ending in 4321 was charged HK$150.00 at Starbucks",
            "com.hsbc.hbap.mobilebanking"
        )
        assertNotNull(bankAlert)
        assertEquals("4321", bankAlert!!.cardLast4)

        val fpsPayment = PaymentNotificationListener.parseNotification(
            "FPS transfer",
            "Sent HK$120.00 via FPS to Pacific Coffee",
            "hk.com.hkicl.fps"
        )
        assertNotNull(fpsPayment)
        assertNull(fpsPayment!!.cardLast4) // Strictly null, never random
    }
}
