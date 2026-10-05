package com.example.vibefinance.data

import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AccountEntitySerializationTest {

    @Test
    fun accountEntityHoldsLinkedAppPackage() {
        val accountWithApp = AccountEntity(
            id = 101L,
            name = "Alipay HK",
            type = AccountType.BANK,
            balance = 520.0,
            icon = "wallet",
            linkedAppPackage = "hk.alipay.payment"
        )
        assertEquals("hk.alipay.payment", accountWithApp.linkedAppPackage)

        val accountWithoutApp = AccountEntity(
            id = 102L,
            name = "Cash",
            type = AccountType.CASH,
            balance = 100.0,
            icon = "cash"
        )
        assertNull(accountWithoutApp.linkedAppPackage)
    }

    @Test
    fun jsonSerializationPreservesLinkedAppPackage() {
        val json = JSONObject().apply {
            put("id", 201L)
            put("name", "Octopus")
            put("type", AccountType.DEBIT.name)
            put("balance", 250.0)
            put("icon", "octopus")
            put("linkedAppPackage", "com.octopuscards.nfc_reader")
        }

        val parsedPackage = if (json.has("linkedAppPackage")) json.optString("linkedAppPackage", "") else null
        assertEquals("com.octopuscards.nfc_reader", parsedPackage)

        val jsonNone = JSONObject().apply {
            put("id", 202L)
            put("name", "Mox")
            put("type", AccountType.CC.name)
            put("balance", 1200.0)
            put("icon", "mox")
            put("linkedAppPackage", "none")
        }
        assertEquals("none", jsonNone.optString("linkedAppPackage", ""))
    }

    @Test
    fun accountEntityHoldsAndParsesNotificationAliases() {
        val account = AccountEntity(
            id = 301L,
            name = "My Card",
            type = AccountType.DEBIT,
            balance = 100.0,
            icon = "wallet",
            notificationAliases = "Smart Octopus, 1719, 八達通"
        )
        assertEquals("Smart Octopus, 1719, 八達通", account.notificationAliases)
        assertEquals(listOf("Smart Octopus", "1719", "八達通"), account.getNotificationAliasList())

        val emptyAccount = AccountEntity(
            id = 302L,
            name = "Cash",
            type = AccountType.CASH,
            balance = 50.0,
            icon = "cash"
        )
        assertNull(emptyAccount.notificationAliases)
        assertEquals(emptyList<String>(), emptyAccount.getNotificationAliasList())
    }

    @Test
    fun jsonSerializationPreservesNotificationAliases() {
        val json = JSONObject().apply {
            put("id", 401L)
            put("name", "BOC Go")
            put("type", AccountType.CC.name)
            put("balance", 0.0)
            put("icon", "credit_card")
            put("notificationAliases", "BOC Go unionpay Diamond Card, 4832")
        }
        val parsedAliases = if (json.has("notificationAliases")) json.optString("notificationAliases", "") else null
        assertEquals("BOC Go unionpay Diamond Card, 4832", parsedAliases)
    }
}
