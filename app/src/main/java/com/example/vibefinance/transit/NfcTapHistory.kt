package com.example.vibefinance.transit

import com.example.vibefinance.util.GnssPoint
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

internal data class NfcTapCandidate(
    val id: String, val at: Long, var excluded: Boolean = false,
    var latitude: Double? = null, var longitude: Double? = null, var accuracy: Float? = null
)

/** Short-lived local evidence, never financial records or card data. All callers serialize access. */
internal class NfcTapHistory(
    val taps: MutableList<NfcTapCandidate> = mutableListOf(),
    private val payments: MutableList<Long> = mutableListOf()
) {
    fun prune(now: Long) {
        taps.removeAll { it.at <= 0 || it.at > now || now - it.at > RETENTION_MS }
        while (taps.size > 32) taps.removeAt(0)
        payments.removeAll { it > now + 2_000 || now - it > 60_000 }
        while (payments.size > 32) payments.removeAt(0)
    }

    fun begin(at: Long): String {
        prune(at)
        val tap = NfcTapCandidate(UUID.randomUUID().toString(), at,
            payments.any { it - at in -2_000L..30_000L })
        taps.add(tap)
        prune(at)
        return tap.id
    }

    fun payment(at: Long, now: Long) {
        prune(now)
        if (at !in (now - 60_000)..(now + 2_000)) return
        if (at !in payments) payments.add(at)
        while (payments.size > 32) payments.removeAt(0)
        // Ambiguous nearby contacts remain excluded evidence, rather than guessing which was paid.
        taps.filter { at - it.at in -2_000L..30_000L }.forEach { it.excluded = true }
    }

    fun locate(id: String, fix: GnssPoint, now: Long): Boolean {
        prune(now)
        val tap = taps.firstOrNull { it.id == id && !it.excluded } ?: return false
        val accuracy = fix.accuracyMeters ?: return false
        if (fix.isMock || accuracy !in 0.01f..100f ||
            !fix.latitude.isFinite() || fix.latitude !in -90.0..90.0 ||
            !fix.longitude.isFinite() || fix.longitude !in -180.0..180.0 ||
            now - fix.timestamp !in 0L..15_000L || fix.timestamp - tap.at !in -15_000L..10_000L) return false
        tap.latitude = fix.latitude; tap.longitude = fix.longitude; tap.accuracy = accuracy
        return true
    }

    fun origins(exitAt: Long): List<TimestampedBreadcrumb> = taps.mapNotNull { tap ->
        val lat = tap.latitude ?: return@mapNotNull null
        val lng = tap.longitude ?: return@mapNotNull null
        val accuracy = tap.accuracy ?: return@mapNotNull null
        if (tap.excluded || exitAt - tap.at !in 300_000L..RETENTION_MS) null
        else TimestampedBreadcrumb(tap.at, lat, lng, accuracyMeters = accuracy)
    }

    fun closeJourney(exitAt: Long) { taps.removeAll { it.at <= exitAt - 300_000L } }

    fun encode(): String = JSONObject().put("taps", JSONArray().apply {
        taps.forEach { tap -> put(JSONObject().put("id", tap.id).put("at", tap.at).put("excluded", tap.excluded).apply {
            tap.latitude?.let { put("lat", it) }; tap.longitude?.let { put("lng", it) }; tap.accuracy?.let { put("accuracy", it) }
        }) }
    }).put("payments", JSONArray(payments)).toString()

    companion object {
        const val RETENTION_MS = 2 * 60 * 60 * 1000L
        fun decode(raw: String?, now: Long): NfcTapHistory {
            if (raw == null || raw.length > 32_768) return NfcTapHistory()
            return try {
                val root = JSONObject(raw)
                val taps = root.optJSONArray("taps") ?: JSONArray()
                val payments = root.optJSONArray("payments") ?: JSONArray()
                if (taps.length() > 32 || payments.length() > 32) return NfcTapHistory()
                NfcTapHistory(MutableList(taps.length()) { i ->
                    val t = taps.getJSONObject(i)
                    val lat = if (t.has("lat")) t.getDouble("lat") else null
                    val lng = if (t.has("lng")) t.getDouble("lng") else null
                    val accuracy = if (t.has("accuracy")) t.getDouble("accuracy").toFloat() else null
                    require(lat == null || lat.isFinite() && lat in -90.0..90.0)
                    require(lng == null || lng.isFinite() && lng in -180.0..180.0)
                    require(accuracy == null || accuracy in 0.01f..100f)
                    NfcTapCandidate(t.getString("id").also { require(it.length in 1..64) }, t.getLong("at"),
                        t.getBoolean("excluded"), lat, lng, accuracy)
                }, MutableList(payments.length()) { payments.getLong(it) }).apply { prune(now) }
            } catch (_: Exception) { NfcTapHistory() }
        }
    }
}

/** Coalesce RF edges, not payments: the first ON is actionable even for a very short tap. */
internal class NfcContactGate {
    private var on = false
    private var lastEdge: Long? = null
    private var seen = false
    fun accept(detected: Boolean, elapsed: Long): Boolean {
        val repeated = on
        val merged = seen && lastEdge?.let { elapsed - it in 0L..250L } == true
        on = detected; lastEdge = elapsed
        if (!detected || repeated || merged) return false
        seen = true
        return true
    }
}
