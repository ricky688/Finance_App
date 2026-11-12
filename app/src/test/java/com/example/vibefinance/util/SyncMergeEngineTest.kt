package com.example.vibefinance.util

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SyncMergeEngineTest {

    @Test
    fun testMergeDisjointTransactionsPreservesBoth() {
        val local = JSONObject().apply {
            put("transactions", JSONArray().apply {
                put(JSONObject().apply {
                    put("id", 1L)
                    put("amount", 50.0)
                    put("category", "FOOD")
                    put("timestamp", 1000L)
                    put("accountId", 10L)
                    put("description", "Lunch")
                })
            })
        }

        val remote = JSONObject().apply {
            put("transactions", JSONArray().apply {
                put(JSONObject().apply {
                    put("id", 2L)
                    put("amount", 20.0)
                    put("category", "TRANSPORT")
                    put("timestamp", 2000L)
                    put("accountId", 10L)
                    put("description", "Bus")
                })
            })
        }

        val result = SyncMergeEngine.merge(local, remote)
        assertTrue(result.localChanged)
        assertTrue(result.remoteChanged)

        val txArr = result.mergedJson.getJSONArray("transactions")
        assertEquals(2, txArr.length())
        // Should be sorted by timestamp descending
        assertEquals("Bus", txArr.getJSONObject(0).getString("description"))
        assertEquals("Lunch", txArr.getJSONObject(1).getString("description"))
    }

    @Test
    fun testMergeDuplicateTransactionsDeduplicates() {
        val txJson = JSONObject().apply {
            put("id", 1L)
            put("amount", 50.0)
            put("category", "FOOD")
            put("timestamp", 1000L)
            put("accountId", 10L)
            put("description", "Lunch")
        }

        val local = JSONObject().apply {
            put("transactions", JSONArray().apply { put(txJson) })
        }
        val remote = JSONObject().apply {
            put("transactions", JSONArray().apply { put(JSONObject(txJson.toString())) })
        }

        val result = SyncMergeEngine.merge(local, remote)
        val txArr = result.mergedJson.getJSONArray("transactions")
        assertEquals(1, txArr.length())
        assertEquals("Lunch", txArr.getJSONObject(0).getString("description"))
    }

    @Test
    fun testMergeAccountsUnion() {
        val local = JSONObject().apply {
            put("accounts", JSONArray().apply {
                put(JSONObject().apply {
                    put("id", 1L)
                    put("name", "Cash")
                    put("balance", 100.0)
                })
            })
        }

        val remote = JSONObject().apply {
            put("accounts", JSONArray().apply {
                put(JSONObject().apply {
                    put("id", 2L)
                    put("name", "Bank")
                    put("balance", 500.0)
                })
            })
        }

        val result = SyncMergeEngine.merge(local, remote)
        val accArr = result.mergedJson.getJSONArray("accounts")
        assertEquals(2, accArr.length())
    }
}
