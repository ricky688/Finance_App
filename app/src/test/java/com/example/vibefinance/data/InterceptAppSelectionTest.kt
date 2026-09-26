package com.example.vibefinance.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InterceptAppSelectionTest {

    @Test
    fun legacySelectionsBecomeOnlyInstalledPackageNames() {
        val installed = setOf(
            "com.google.android.apps.walletnfcrel",
            "com.google.android.apps.wallet",
            "com.tencent.mm",
            "com.example.mail"
        )
        val selection = setOf("google_pay", "wechat_pay", "com.example.mail", "com.fake.wallet")

        assertEquals(
            installed,
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
}
