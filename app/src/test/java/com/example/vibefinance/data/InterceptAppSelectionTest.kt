package com.example.vibefinance.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InterceptAppSelectionTest {

    @Test
    fun legacySelectionsResolveAndMissingPackageLinksArePreserved() {
        val installed = setOf(
            "com.google.android.apps.walletnfcrel",
            "com.google.android.apps.wallet",
            "com.tencent.mm",
            "com.example.mail"
        )
        val selection = setOf("google_pay", "wechat_pay", "com.example.mail", "com.fake.wallet")

        assertEquals(
            installed + "com.fake.wallet",
            InMemoryDatabase.migrateInterceptAppSelections(selection, installed)
        )
    }

    @Test
    fun emptySelectionRemainsEmpty() {
        assertEquals(
            emptySet<String>(),
            InMemoryDatabase.migrateInterceptAppSelections(
                emptySet(),
                setOf("com.google.android.apps.walletnfcrel")
            )
        )
    }

    @Test
    fun selectedChatAppRequiresPaymentNotificationAndAnotherPackageStaysDisabled() {
        val previousSelection = InMemoryDatabase.selectedInterceptApps.value
        val previousLoggingState = InMemoryDatabase.isNotificationLoggingEnabled
        try {
            InMemoryDatabase.isNotificationLoggingEnabled = true
            InMemoryDatabase.selectedInterceptApps.value = setOf("com.tencent.mm", "jp.naver.line.android")

            assertFalse(InMemoryDatabase.isAppInterceptEnabled("com.tencent.mm", "New message", "Hello"))
            assertTrue(InMemoryDatabase.isAppInterceptEnabled("com.tencent.mm", "微信支付", "HK\$20.00"))
            assertFalse(InMemoryDatabase.isAppInterceptEnabled("jp.naver.line.android", "New message", "Hello"))
            assertTrue(InMemoryDatabase.isAppInterceptEnabled("jp.naver.line.android", "LINE Pay", "HK\$30.00"))
            assertFalse(InMemoryDatabase.isAppInterceptEnabled("com.google.android.apps.walletnfcrel", "Payment", "HK\$20.00"))
        } finally {
            InMemoryDatabase.selectedInterceptApps.value = previousSelection
            InMemoryDatabase.isNotificationLoggingEnabled = previousLoggingState
        }
    }

    @Test
    fun testOctopusAppRequiresAndroidOctopusTitle() {
        val previousSelection = InMemoryDatabase.selectedInterceptApps.value
        val previousLoggingState = InMemoryDatabase.isNotificationLoggingEnabled
        val octopusPkg = "com.octopuscards.nfc_reader"
        try {
            InMemoryDatabase.isNotificationLoggingEnabled = true
            InMemoryDatabase.selectedInterceptApps.value = setOf(octopusPkg)

            // Valid Android Octopus titles
            assertTrue(InMemoryDatabase.isAppInterceptEnabled(octopusPkg, "Android版八達通", "八達通: 在 港鐵 支付 HKD 4.9。餘額: HKD 90.8"))
            assertTrue(InMemoryDatabase.isAppInterceptEnabled(octopusPkg, "Android 八逹通", "八達通: 在 九巴 / 龍運 支付 HKD 5.8。餘額: HKD 56.0"))
            assertTrue(InMemoryDatabase.isAppInterceptEnabled(octopusPkg, "Android版八逹通", "八達通: 在 7-Eleven 支付 HKD 5.0。餘額: HKD 98.9"))
            assertTrue(InMemoryDatabase.isAppInterceptEnabled(octopusPkg, "Android Octopus", "Paid HKD 12.5 at Starbucks"))
            assertTrue(InMemoryDatabase.isAppInterceptEnabled(octopusPkg, "Octopus on Android", "Paid HKD 5.8 at KMB"))

            // Irrelevant notifications from Octopus app MUST be rejected even if text contains "八達通" or "支付"
            assertFalse(InMemoryDatabase.isAppInterceptEnabled(octopusPkg, "八達通", "【最新推廣】在 麥當勞 支付享 $10 回贈"))
            assertFalse(InMemoryDatabase.isAppInterceptEnabled(octopusPkg, "八達通銀包", "在 零售 消費滿 HKD 100 獎賞"))
            assertFalse(InMemoryDatabase.isAppInterceptEnabled(octopusPkg, "八達通優惠", "立即領取優惠券"))
            assertFalse(InMemoryDatabase.isAppInterceptEnabled(octopusPkg, "Octopus", "Your monthly statement is ready"))
            assertFalse(InMemoryDatabase.isAppInterceptEnabled(octopusPkg, "Octopus Wallet", "Welcome offer"))
            assertFalse(InMemoryDatabase.isAppInterceptEnabled(octopusPkg, "最新優惠推廣", "八達通: 於 7-Eleven 支付即減 $5"))
        } finally {
            InMemoryDatabase.selectedInterceptApps.value = previousSelection
            InMemoryDatabase.isNotificationLoggingEnabled = previousLoggingState
        }
    }
}
