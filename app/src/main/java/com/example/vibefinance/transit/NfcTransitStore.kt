package com.example.vibefinance.transit

import android.content.Context
import com.example.vibefinance.util.GnssPoint
import java.io.File

/** Kept in noBackupFilesDir: transient locations must not enter app/cloud backups. */
internal object NfcTransitStore {
    private fun file(context: Context) = File(context.noBackupFilesDir, "nfc_transit_candidates.json")

    @Synchronized
    private fun <T> update(context: Context, now: Long, block: (NfcTapHistory) -> T): T {
        val target = file(context)
        val raw = try { android.util.AtomicFile(target).openRead().use { stream ->
            val bytes = ByteArray(32_769)
            var size = 0
            while (size < bytes.size) {
                val read = stream.read(bytes, size, bytes.size - size)
                if (read < 0) break
                size += read
            }
            if (size > 32_768) null else String(bytes, 0, size, Charsets.UTF_8)
        } } catch (_: Exception) { null }
        val history = NfcTapHistory.decode(raw, now)
        val result = block(history)
        val atomic = android.util.AtomicFile(target)
        var stream: java.io.FileOutputStream? = null
        try {
            stream = atomic.startWrite()
            stream.write(history.encode().toByteArray(Charsets.UTF_8))
            atomic.finishWrite(stream)
        } catch (e: Exception) {
            atomic.failWrite(stream)
            // No stale evidence may survive a failed exclusion or journey close.
            atomic.delete()
            throw e
        }
        return result
    }

    fun begin(context: Context, at: Long): String? = update(context, at) { history ->
        history.begin(at).takeUnless { id -> history.taps.first { it.id == id }.excluded }
    }
    fun locate(context: Context, id: String, fix: GnssPoint): Boolean = update(context, System.currentTimeMillis()) {
        it.locate(id, fix, System.currentTimeMillis())
    }
    fun payment(context: Context, at: Long) = update(context, System.currentTimeMillis()) { it.payment(at, System.currentTimeMillis()) }
    fun origins(context: Context, exitAt: Long): List<TimestampedBreadcrumb> = update(context, System.currentTimeMillis()) { it.origins(exitAt) }
    fun closeJourney(context: Context, exitAt: Long) = update(context, System.currentTimeMillis()) { it.closeJourney(exitAt) }
    @Synchronized fun clear(context: Context) = android.util.AtomicFile(file(context)).delete()
}
