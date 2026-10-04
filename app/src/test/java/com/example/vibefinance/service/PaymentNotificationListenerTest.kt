package com.example.vibefinance.service

import com.example.vibefinance.data.entity.AccountType
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
        assertTrue(PendingPaymentStore.canRememberChoice(
            generic.copy(assetHint = "Smart Octopus"), account.copy(name = "Wallet (Cash)", type = AccountType.CASH)
        ))
        assertTrue(PendingPaymentStore.canRememberChoice(
            generic.copy(assetHint = "Smart Octopus", cardLast4 = "9821"), account.copy(name = "Any Account", cardLast4 = "9821")
        ))
        assertFalse(PendingPaymentStore.canRememberChoice(
            generic.copy(assetHint = "Smart Octopus"), account.copy(name = "AEON card", type = AccountType.CC)
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
    fun samsungWalletBocGoUnionPayTransitNotificationParsesAndMatchesBocGoAccounts() {
        val title = "BOC Go unionpay Diamond Card"
        val text = "transit-ticket THE KOWLOOHHONGKONG HKG HK$3.60"
        val pkg = "com.samsung.android.spay"

        assertTrue(PaymentNotificationListener.isPaymentNotification(title, text))

        val parsed = PaymentNotificationListener.parseNotification(title, text, pkg)
        assertNotNull(parsed)
        assertEquals(3.60, parsed!!.amount, 0.001)
        assertEquals(title, parsed.assetName)
        assertEquals("transit-ticket THE KOWLOOHHONGKONG HKG", parsed.merchant)
        assertEquals("Transport", PaymentNotificationListener.determineCategory(parsed.merchant))
        assertEquals("Transport", PaymentNotificationListener.determineCategory("THE KOWLOOHHONGKONG HKG"))

        assertTrue(PendingPaymentStore.isBocGoHint(parsed.assetName))
        assertTrue(PendingPaymentStore.isBocGoHint("BOC Go UnionPay Diamond"))
        assertTrue(PendingPaymentStore.isBocGoHint("BOC Go Card"))
        assertTrue(PendingPaymentStore.isBocGoHint("中銀 Go"))
        assertFalse(PendingPaymentStore.isBocGoHint("HSBC Visa Card"))

        val payment = PendingPayment(
            id = "samsung_boc_go",
            fingerprint = "samsung_boc_go_fp",
            sourcePackage = pkg,
            assetHint = parsed.assetName,
            merchant = parsed.merchant,
            amount = parsed.amount,
            detectedAt = 100L
        )

        val bocGoAccounts = listOf(
            com.example.vibefinance.data.entity.AccountEntity(
                id = 101L,
                name = "BOC Go",
                type = com.example.vibefinance.data.entity.AccountType.CC,
                balance = 0.0,
                icon = "credit_card"
            ),
            com.example.vibefinance.data.entity.AccountEntity(
                id = 102L,
                name = "BOC Go Card",
                type = com.example.vibefinance.data.entity.AccountType.CC,
                balance = 0.0,
                icon = "credit_card"
            ),
            com.example.vibefinance.data.entity.AccountEntity(
                id = 103L,
                name = "中銀 Go",
                type = com.example.vibefinance.data.entity.AccountType.CC,
                balance = 0.0,
                icon = "credit_card"
            ),
            com.example.vibefinance.data.entity.AccountEntity(
                id = 104L,
                name = "中銀 Go 卡",
                type = com.example.vibefinance.data.entity.AccountType.CC,
                balance = 0.0,
                icon = "credit_card"
            ),
            com.example.vibefinance.data.entity.AccountEntity(
                id = 105L,
                name = "BOC UnionPay",
                type = com.example.vibefinance.data.entity.AccountType.CC,
                balance = 0.0,
                icon = "credit_card"
            ),
            com.example.vibefinance.data.entity.AccountEntity(
                id = 106L,
                name = "中銀信用卡",
                type = com.example.vibefinance.data.entity.AccountType.CC,
                balance = 0.0,
                icon = "credit_card"
            ),
            com.example.vibefinance.data.entity.AccountEntity(
                id = 107L,
                name = "BOC",
                type = com.example.vibefinance.data.entity.AccountType.CC,
                balance = 0.0,
                icon = "credit_card"
            )
        )

        for (acc in bocGoAccounts) {
            assertTrue("Should be able to remember choice for ${acc.name}", PendingPaymentStore.canRememberChoice(payment, acc))
        }

        // findBocGoMatch resolves the best account
        val matched = PendingPaymentStore.findBocGoMatch(bocGoAccounts)
        assertNotNull(matched)
        assertEquals("BOC Go", matched!!.name)

        // Non-BOC account cannot be bound
        val hsbc = com.example.vibefinance.data.entity.AccountEntity(
            id = 200L,
            name = "HSBC Red Card",
            type = com.example.vibefinance.data.entity.AccountType.CC,
            balance = 0.0,
            icon = "credit_card"
        )
        assertFalse(PendingPaymentStore.canRememberChoice(payment, hsbc))
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

    @Test
    fun testSmartOctopusNotificationWithBalanceRemaining() {
        val title = "Samsung Wallet"
        val text = "Smart Octopus HK$12.6 7-Eleven 餘額 HK$214.0"
        val pkg = "com.samsung.android.spay"

        assertTrue(PaymentNotificationListener.isPaymentNotification(title, text))

        val parsed = PaymentNotificationListener.parseNotification(title, text, pkg)
        assertNotNull(parsed)
        assertEquals(12.6, parsed!!.amount, 0.001)
        assertEquals("7-Eleven", parsed.merchant)
        assertEquals(214.0, parsed.balanceRemaining!!, 0.001)
        assertEquals("Smart Octopus", parsed.assetName)

        // Chinese merchant variant
        val parsedMcdonalds = PaymentNotificationListener.parseNotification(
            title,
            "Smart Octopus HK$33.0 麥當勞 餘額 HK$181.0",
            pkg
        )
        assertNotNull(parsedMcdonalds)
        assertEquals(33.0, parsedMcdonalds!!.amount, 0.001)
        assertEquals("麥當勞", parsedMcdonalds.merchant)
        assertEquals(181.0, parsedMcdonalds.balanceRemaining!!, 0.001)

        // English Balance variant
        val parsedStarbucks = PaymentNotificationListener.parseNotification(
            "Samsung Pay",
            "Smart Octopus HK$28.5 Starbucks Balance HK$120.0",
            pkg
        )
        assertNotNull(parsedStarbucks)
        assertEquals(28.5, parsedStarbucks!!.amount, 0.001)
        assertEquals("Starbucks", parsedStarbucks.merchant)
        assertEquals(120.0, parsedStarbucks.balanceRemaining!!, 0.001)

        // Negative balance variant
        val parsedNegative = PaymentNotificationListener.parseNotification(
            title,
            "Smart Octopus HK$50.0 惠康 餘額 -HK$15.0",
            pkg
        )
        assertNotNull(parsedNegative)
        assertEquals(50.0, parsedNegative!!.amount, 0.001)
        assertEquals("惠康", parsedNegative.merchant)
        assertEquals(-15.0, parsedNegative.balanceRemaining!!, 0.001)
    }

    @Test
    fun testRealAndroidOctopusNotifications() {
        val title = "Android版八達通"
        val pkg = "com.octopuscards.nfc_reader"

        val samples = listOf(
            Triple("八達通: 在 九巴 / 龍運 支付 HKD 5.8。餘額: HKD 56.0", 5.8, "九巴 / 龍運") to (56.0 to "Transport"),
            Triple("八達通: 在 餐飲/會所 支付 HKD 29.0。餘額: HKD 61.8", 29.0, "餐飲/會所") to (61.8 to "Food & Drink"),
            Triple("八達通: 在 港鐵 支付 HKD 4.9。餘額: HKD 90.8", 4.9, "港鐵") to (90.8 to "Transport"),
            Triple("八達通: 在 港鐵 支付 HKD 3.2。餘額: HKD 95.7", 3.2, "港鐵") to (95.7 to "Transport"),
            Triple("八達通: 在 7-Eleven 支付 HKD 5.0。餘額: HKD 98.9", 5.0, "7-Eleven") to (98.9 to "Groceries"),
            Triple("八達通: 在 港鐵 支付 HKD 4.9。餘額: HKD 103.9", 4.9, "港鐵") to (103.9 to "Transport"),
            Triple("八達通: 在 零售 支付 HKD 18.0。餘額: HKD 108.8", 18.0, "零售") to (108.8 to "Shopping")
        )

        for ((input, expected) in samples) {
            val (text, expAmount, expMerchant) = input
            val (expBalance, expCategory) = expected

            assertTrue("Guard must recognize: $text", PaymentNotificationListener.isPaymentNotification(title, text))

            val parsed = PaymentNotificationListener.parseNotification(title, text, pkg)
            assertNotNull("Failed to parse: $text", parsed)
            assertEquals("Amount mismatch for $text", expAmount, parsed!!.amount, 0.001)
            assertEquals("Merchant mismatch for $text", expMerchant, parsed.merchant)
            assertNotNull("Balance missing for $text", parsed.balanceRemaining)
            assertEquals("Balance mismatch for $text", expBalance, parsed.balanceRemaining!!, 0.001)
            assertEquals("八達通", parsed.assetName)

            val cat = PaymentNotificationListener.determineCategory(parsed.merchant, title)
            assertEquals("Category mismatch for ${parsed.merchant}", expCategory, cat)
        }

        // Overdraft / Negative balance variants
        val overdraft1 = PaymentNotificationListener.parseNotification(title, "八達通: 在 港鐵 支付 HKD 4.9。餘額: -HKD 15.0", pkg)
        assertNotNull(overdraft1)
        assertEquals(4.9, overdraft1!!.amount, 0.001)
        assertEquals("港鐵", overdraft1.merchant)
        assertEquals(-15.0, overdraft1.balanceRemaining!!, 0.001)

        val overdraft2 = PaymentNotificationListener.parseNotification(title, "八達通: 在 港鐵 支付 HKD 4.9。餘額: HKD -12.5", pkg)
        assertNotNull(overdraft2)
        assertEquals(4.9, overdraft2!!.amount, 0.001)
        assertEquals(-12.5, overdraft2.balanceRemaining!!, 0.001)

        // English alert variants
        val english1 = PaymentNotificationListener.parseNotification("Octopus on Android", "Octopus: Paid HKD 12.5 at Starbucks. Balance: HKD 120.0", pkg)
        assertNotNull(english1)
        assertEquals(12.5, english1!!.amount, 0.001)
        assertEquals("Starbucks", english1.merchant)
        assertEquals(120.0, english1.balanceRemaining!!, 0.001)
        assertEquals("Food & Drink", PaymentNotificationListener.determineCategory(english1.merchant))

        val english2 = PaymentNotificationListener.parseNotification("Android Octopus", "Paid HKD 5.8 at KMB. Remaining value: -HKD 5.0", pkg)
        assertNotNull(english2)
        assertEquals(5.8, english2!!.amount, 0.001)
        assertEquals("KMB", english2.merchant)
        assertEquals(-5.0, english2.balanceRemaining!!, 0.001)
        assertEquals("Transport", PaymentNotificationListener.determineCategory(english2.merchant))
    }

    @Test
    fun testIrrelevantOctopusAppNotificationsAreRejected() {
        val pkg = "com.octopuscards.nfc_reader"

        val irrelevantAlerts = listOf(
            "八達通" to "【最新推廣】在 麥當勞 支付享 $10 回贈",
            "八達通銀包" to "八達通: 在 零售 消費滿 HKD 100 獎賞",
            "Octopus" to "Paid HKD 50 to receive special promo",
            "Octopus Wallet" to "Your monthly statement is ready for review",
            "最新優惠推廣" to "八達通: 於 7-Eleven 支付即減 $5",
            "八達通" to "您的八達通銀包餘額不足，請增值",
            "八達通卡" to "查閱卡片餘額 HKD 56.0"
        )

        for ((title, text) in irrelevantAlerts) {
            assertFalse("Must reject irrelevant notification guard for title: $title", PaymentNotificationListener.isPaymentNotification(title, text))
            val parsed = PaymentNotificationListener.parseNotification(title, text, pkg)
            assertNull("Must NOT parse non-Android Octopus alert from Octopus app (title: $title)", parsed)
        }
    }
}


