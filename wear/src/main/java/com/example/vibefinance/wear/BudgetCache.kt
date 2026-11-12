package com.example.vibefinance.wear

import android.content.Context
import android.os.SystemClock
import android.provider.Settings
import android.util.AtomicFile
import com.example.vibefinance.wear.contract.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.DataInputStream
import java.io.DataOutputStream

internal data class CachedBudget(val snapshot: WearBudgetSnapshot? = null, val expired: Boolean = false)

/** Cache and replay watermark live outside Android backup, including after the amount expires. */
internal class BudgetCache(private val context: Context) {
    private val file get() = AtomicFile(java.io.File(context.noBackupFilesDir, "budget-cache-v1"))
    private fun boot() = Settings.Global.getInt(context.contentResolver, Settings.Global.BOOT_COUNT, -1)
    private data class Record(val node: String, val revision: Long, val received: Long, val boot: Int, val payload: ByteArray)

    private fun load(): Record? = try {
        DataInputStream(file.openRead()).use { input ->
            val node = input.readUTF()
            val revision = input.readLong()
            val received = input.readLong()
            val boot = input.readInt()
            val size = input.readInt()
            require(size in 0..4096)
            Record(node, revision, received, boot, ByteArray(size).also { input.readFully(it) })
        }
    } catch (_: Exception) { null }

    private fun save(record: Record) {
        val stream = file.startWrite()
        try {
            val out = DataOutputStream(stream)
            out.writeUTF(record.node); out.writeLong(record.revision)
            out.writeLong(record.received); out.writeInt(record.boot)
            out.writeInt(record.payload.size); out.write(record.payload); out.flush()
            file.finishWrite(stream)
        } catch (error: Exception) {
            file.failWrite(stream)
            throw error
        }
    }

    fun read(now: Long = System.currentTimeMillis(), elapsed: Long = SystemClock.elapsedRealtime()): CachedBudget = synchronized(lock) {
        val record = load() ?: return@synchronized CachedBudget()
        if (record.payload.isEmpty()) return@synchronized CachedBudget(expired = true)
        val snapshot = runCatching { WearBudgetSnapshot.decode(record.payload) }.getOrNull()
        if (snapshot == null || !snapshot.isCurrent(now) || record.boot != boot() ||
            record.received > elapsed || elapsed - record.received >= WearBudgetSnapshot.MAX_AGE_MS) {
            save(record.copy(payload = byteArrayOf()))
            return@synchronized CachedBudget(expired = true)
        }
        CachedBudget(snapshot)
    }

    fun accept(node: String, payload: ByteArray, allowedPhones: Set<String>,
        now: Long = System.currentTimeMillis(), elapsed: Long = SystemClock.elapsedRealtime()): Boolean = synchronized(lock) {
        require(node in allowedPhones && allowedPhones.size == 1)
        val incoming = WearBudgetSnapshot.decode(payload)
        val previous = load()
        if (previous?.node == node && incoming.revision <= previous.revision) return@synchronized false
        // Expired packets still advance the watermark, preventing an older visible packet from reviving.
        val bytes = if (incoming.isCurrent(now)) payload else byteArrayOf()
        save(Record(node, incoming.revision, elapsed, boot(), bytes))
        changes.value += 1
        true
    }

    companion object {
        private val lock = Any()
        val changes = MutableStateFlow(0L)
    }
}
