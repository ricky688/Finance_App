package com.example.vibefinance.data

import com.example.vibefinance.data.entity.TransactionEntity
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TransactionLocationSerializationTest {

    @Test
    fun transactionEntityHoldsLocationFields() {
        val txWithLoc = TransactionEntity(
            id = 101L,
            amount = 65.5,
            category = "Food & Dining",
            timestamp = 1700000000000L,
            accountId = 1L,
            description = "Lunch at Central",
            latitude = 22.2819,
            longitude = 114.1582,
            locationName = "Central, Hong Kong"
        )
        assertEquals(22.2819, txWithLoc.latitude!!, 0.0001)
        assertEquals(114.1582, txWithLoc.longitude!!, 0.0001)
        assertEquals("Central, Hong Kong", txWithLoc.locationName)

        val txWithoutLoc = TransactionEntity(
            id = 102L,
            amount = 20.0,
            category = "Transport",
            timestamp = 1700000000000L,
            accountId = 1L
        )
        assertNull(txWithoutLoc.latitude)
        assertNull(txWithoutLoc.longitude)
        assertNull(txWithoutLoc.locationName)
    }

    @Test
    fun jsonSerializationPreservesLocationCoordinatesAndName() {
        val tx = TransactionEntity(
            id = 201L,
            amount = 120.0,
            category = "Shopping",
            timestamp = 1700000000000L,
            accountId = 2L,
            description = "Bookstore",
            latitude = 22.3193,
            longitude = 114.1694,
            locationName = "Mong Kok, Kowloon"
        )

        val json = JSONObject().apply {
            put("id", tx.id)
            put("amount", tx.amount)
            put("category", tx.category)
            put("timestamp", tx.timestamp)
            put("accountId", tx.accountId)
            put("description", tx.description)
            tx.latitude?.let { put("latitude", it) }
            tx.longitude?.let { put("longitude", it) }
            tx.locationName?.let { put("locationName", it) }
        }

        val parsedLat = if (json.has("latitude") && !json.isNull("latitude")) json.optDouble("latitude") else null
        val parsedLng = if (json.has("longitude") && !json.isNull("longitude")) json.optDouble("longitude") else null
        val parsedLoc = if (json.has("locationName") && !json.isNull("locationName")) json.optString("locationName") else null

        assertEquals(22.3193, parsedLat!!, 0.0001)
        assertEquals(114.1694, parsedLng!!, 0.0001)
        assertEquals("Mong Kok, Kowloon", parsedLoc)
    }

    @Test
    fun jsonDeserializationHandlesNullLocationFieldsSafely() {
        val json = JSONObject().apply {
            put("id", 202L)
            put("amount", 50.0)
            put("category", "Entertainment")
            put("timestamp", 1700000000000L)
            put("accountId", 1L)
        }

        val parsedLat = if (json.has("latitude") && !json.isNull("latitude")) json.optDouble("latitude") else null
        val parsedLng = if (json.has("longitude") && !json.isNull("longitude")) json.optDouble("longitude") else null
        val parsedLoc = if (json.has("locationName") && !json.isNull("locationName")) json.optString("locationName") else null

        assertNull(parsedLat)
        assertNull(parsedLng)
        assertNull(parsedLoc)
    }
}
