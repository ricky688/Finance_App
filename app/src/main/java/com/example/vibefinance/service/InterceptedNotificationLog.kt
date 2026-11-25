package com.example.vibefinance.service

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class InterceptedNotificationStatus {
    LOGGED,
    PENDING,
    IGNORED,
    FILTERED
}

data class InterceptedNotificationEntry(
    val id: String,
    val packageName: String,
    val title: String,
    val text: String,
    val receivedAt: Long,
    val status: InterceptedNotificationStatus,
    val paymentId: String? = null,
    val amount: Double? = null,
    val merchant: String? = null
)

/**
 * Local ring-buffer store (max 100 items) of intercepted notifications from monitored apps for
 * on-device diagnostics and quick custom template creation. Intentionally excluded from backups.
 */
object InterceptedNotificationLog {
    private const val PREFS_NAME = "intercepted_notification_log_prefs"
    private const val ENTRIES_KEY = "entries"
    private const val MAX_ENTRIES = 100

    @Volatile
    private var initialized = false
    private val _entries = MutableStateFlow<List<InterceptedNotificationEntry>>(emptyList())
    val entries: StateFlow<List<InterceptedNotificationEntry>> = _entries.asStateFlow()

    @Synchronized
    fun initialize(context: Context) {
        if (initialized) return
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(ENTRIES_KEY, null)
        if (!raw.isNullOrBlank()) {
            _entries.value = decodeEntries(raw)
        }
        initialized = true
    }

    @Synchronized
    fun record(
        context: Context,
        packageName: String,
        title: String,
        text: String,
        receivedAt: Long,
        status: InterceptedNotificationStatus,
        paymentId: String? = null,
        amount: Double? = null,
        merchant: String? = null
    ) {
        initialize(context)
        val cleanPkg = packageName.trim()
        val cleanTitle = title.trim()
        val cleanText = text.trim()
        if (cleanPkg.isBlank() && cleanTitle.isBlank() && cleanText.isBlank()) return

        val entry = InterceptedNotificationEntry(
            id = UUID.randomUUID().toString(),
            packageName = cleanPkg,
            title = cleanTitle,
            text = cleanText,
            receivedAt = receivedAt.takeIf { it > 0L } ?: System.currentTimeMillis(),
            status = status,
            paymentId = paymentId,
            amount = amount,
            merchant = merchant?.takeIf { it.isNotBlank() }
        )

        val current = _entries.value.toMutableList()
        val existingIndex = current.indexOfFirst {
            (paymentId != null && it.paymentId == paymentId) ||
                (it.packageName == cleanPkg &&
                    it.title == cleanTitle &&
                    it.text == cleanText &&
                    kotlin.math.abs(it.receivedAt - entry.receivedAt) <= 5_000L)
        }
        if (existingIndex >= 0) {
            val prev = current[existingIndex]
            current[existingIndex] = entry.copy(
                id = prev.id,
                paymentId = entry.paymentId ?: prev.paymentId,
                amount = entry.amount ?: prev.amount,
                merchant = entry.merchant ?: prev.merchant
            )
        } else {
            current.add(0, entry)
        }

        val capped = current.sortedByDescending { it.receivedAt }.take(MAX_ENTRIES)
        _entries.value = capped
        persist(context, capped)
    }

    @Synchronized
    fun updateStatusByPaymentId(
        context: Context,
        paymentId: String,
        status: InterceptedNotificationStatus,
        amount: Double? = null,
        merchant: String? = null
    ) {
        initialize(context)
        val current = _entries.value
        val idx = current.indexOfFirst { it.paymentId == paymentId }
        if (idx < 0) return
        val updated = current.toMutableList()
        val existing = updated[idx]
        updated[idx] = existing.copy(
            status = status,
            amount = amount ?: existing.amount,
            merchant = merchant?.takeIf { it.isNotBlank() } ?: existing.merchant
        )
        _entries.value = updated
        persist(context, updated)
    }

    @Synchronized
    fun clear(context: Context) {
        initialize(context)
        _entries.value = emptyList()
        persist(context, emptyList())
    }

    private fun persist(context: Context, list: List<InterceptedNotificationEntry>) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val arr = JSONArray()
        list.forEach { entry ->
            arr.put(JSONObject().apply {
                put("id", entry.id)
                put("packageName", entry.packageName)
                put("title", entry.title)
                put("text", entry.text)
                put("receivedAt", entry.receivedAt)
                put("status", entry.status.name)
                entry.paymentId?.let { put("paymentId", it) }
                entry.amount?.let { put("amount", it) }
                entry.merchant?.let { put("merchant", it) }
            })
        }
        prefs.edit().putString(ENTRIES_KEY, arr.toString()).apply()
    }

    private fun decodeEntries(raw: String): List<InterceptedNotificationEntry> = runCatching {
        val arr = JSONArray(raw)
        val out = ArrayList<InterceptedNotificationEntry>(arr.length())
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val status = runCatching {
                InterceptedNotificationStatus.valueOf(obj.optString("status", "FILTERED"))
            }.getOrDefault(InterceptedNotificationStatus.FILTERED)
            out.add(
                InterceptedNotificationEntry(
                    id = obj.optString("id").ifBlank { UUID.randomUUID().toString() },
                    packageName = obj.optString("packageName", ""),
                    title = obj.optString("title", ""),
                    text = obj.optString("text", ""),
                    receivedAt = obj.optLong("receivedAt", 0L),
                    status = status,
                    paymentId = obj.optString("paymentId").takeIf { it.isNotBlank() },
                    amount = obj.optDouble("amount", Double.NaN).takeIf { it.isFinite() },
                    merchant = obj.optString("merchant").takeIf { it.isNotBlank() }
                )
            )
        }
        out.sortedByDescending { it.receivedAt }.take(MAX_ENTRIES)
    }.getOrDefault(emptyList())
}
