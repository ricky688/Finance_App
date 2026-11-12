package com.example.vibefinance.util

import com.example.vibefinance.data.InMemoryDatabase
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28, 34])
class SyncBalanceIntegrityTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private fun fixture() = JSONObject("""{
      "creditCardSourceTransfersReconciled":true,
      "nextAccountId":40,"nextTransactionId":100,"nextSubscriptionId":20,"nextDiscountShopId":30,
      "accounts":[{"id":1,"name":"Card","type":"CC","balance":37.25},
                  {"id":2,"name":"Wallet","type":"DEBIT","balance":123.45}],
      "transactions":[{"id":1,"amount":300,"category":"Transfer","timestamp":1000,
                       "accountId":1,"toAccountId":2,"description":"Top up"}],
      "subscriptions":[],"budgets":[],"categoryLimits":{},
      "savedAspects":["Custom"],"discountShops":[],"notificationTemplates":[],
      "futureMetadata":{"preserve":true}
    }""")

    @Test fun mergePreservesLocalMetadataAndHighWaterCountersWithoutMutatingInputs() {
        val local = fixture()
        val remote = JSONObject(local.toString()).put("creditCardSourceTransfersReconciled", false)
            .put("nextTransactionId", 500).put("savedAspects", JSONArray().put("Remote"))
        val before = local.toString()
        val merged = SyncMergeEngine.merge(local, remote).mergedJson
        assertTrue(merged.getBoolean("creditCardSourceTransfersReconciled"))
        assertEquals("Custom", merged.getJSONArray("savedAspects").getString(0))
        assertTrue(merged.has("discountShops")); assertTrue(merged.has("notificationTemplates"))
        assertTrue(merged.getJSONObject("futureMetadata").getBoolean("preserve"))
        assertEquals(40L, merged.getLong("nextAccountId"))
        assertEquals(500L, merged.getLong("nextTransactionId"))
        assertEquals(20L, merged.getLong("nextSubscriptionId"))
        assertEquals(30L, merged.getLong("nextDiscountShopId"))
        merged.getJSONObject("futureMetadata").put("preserve", false)
        assertEquals(before, local.toString())
    }

    @Test fun repeatedChangedMergesAndRelaunchNeverReplayHistoricalTransferCorrection() {
        var local = fixture()
        repeat(3) { round ->
            val remote = JSONObject(local.toString())
            remote.getJSONArray("transactions").put(JSONObject().put("id", 10 + round)
                .put("amount", 0).put("timestamp", 2000 + round).put("accountId", 2)
                .put("category", "Other").put("description", "Remote fixture $round"))
            val result = SyncMergeEngine.merge(local, remote)
            assertTrue(result.localChanged)
            InMemoryDatabase.applySyncedData(context, result.mergedJson)
            InMemoryDatabase.reloadAfterFullRestore(context)
            assertEquals(37.25, InMemoryDatabase.accounts.value.first { it.id == 1L }.balance, 0.001)
            assertEquals(123.45, InMemoryDatabase.accounts.value.first { it.id == 2L }.balance, 0.001)
            local = InMemoryDatabase.snapshotForBackup()
            assertEquals(2 + round, InMemoryDatabase.transactions.value.size)
            assertTrue(local.getBoolean("creditCardSourceTransfersReconciled"))
        }
    }

    @Test fun missingOrFalseMigrationMarkerRejectsBeforeAnyDiskOrStateChange() {
        InMemoryDatabase.applySyncedData(context, fixture())
        val file = File(context.filesDir, "vibefinance_data.json")
        val bytes = file.readBytes()
        for (missing in listOf(true, false)) {
            val unsafe = fixture()
            if (missing) unsafe.remove("creditCardSourceTransfersReconciled")
            else unsafe.put("creditCardSourceTransfersReconciled", false)
            try {
                InMemoryDatabase.applySyncedData(context, unsafe)
                fail("Unsafe sync snapshot must be rejected")
            } catch (_: IllegalArgumentException) { }
            assertArrayEquals(bytes, file.readBytes())
            assertEquals(37.25, InMemoryDatabase.accounts.value.first { it.id == 1L }.balance, 0.001)
        }
    }
}
