package com.example.vibefinance.wear

import android.content.Context
import android.os.SystemClock
import android.provider.Settings
import android.util.AtomicFile
import com.example.vibefinance.wear.contract.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File

internal data class CachedCompanion(
    val snapshot: WearCompanionSnapshot? = null,
    val fresh: Boolean = false,
    val stale: Boolean = false,
    val expired: Boolean = false
)

/**
 * Two-stage companion cache (0-15m fresh, 15m-2h stale-readable, hard expiry at 2h/midnight/revocation).
 * Watermark and payload live outside Android backup; hard expiry wipes all monetary bytes immediately.
 */
internal class CompanionCache(private val context: Context) {
    private val file get() = AtomicFile(File(context.noBackupFilesDir, "companion-cache-v1"))
    private val queueFile get() = AtomicFile(File(context.noBackupFilesDir, "pending-commands-v1"))
    private fun boot() = Settings.Global.getInt(context.contentResolver, Settings.Global.BOOT_COUNT, -1)

    private data class Record(
        val node: String,
        val revision: Long,
        val received: Long,
        val boot: Int,
        val payload: ByteArray
    )

    private fun load(): Record? = try {
        DataInputStream(file.openRead()).use { input ->
            val node = input.readUTF()
            val revision = input.readLong()
            val received = input.readLong()
            val boot = input.readInt()
            val size = input.readInt()
            require(size in 0..WearCompanionSnapshot.MAX_PAYLOAD_BYTES)
            Record(node, revision, received, boot, ByteArray(size).also { input.readFully(it) })
        }
    } catch (_: Exception) {
        null
    }

    private fun save(record: Record) {
        val stream = file.startWrite()
        try {
            val out = DataOutputStream(stream)
            out.writeUTF(record.node)
            out.writeLong(record.revision)
            out.writeLong(record.received)
            out.writeInt(record.boot)
            out.writeInt(record.payload.size)
            out.write(record.payload)
            out.flush()
            file.finishWrite(stream)
        } catch (error: Exception) {
            file.failWrite(stream)
            throw error
        }
    }

    fun read(
        now: Long = System.currentTimeMillis(),
        elapsed: Long = SystemClock.elapsedRealtime()
    ): CachedCompanion = synchronized(lock) {
        val record = load() ?: return@synchronized CachedCompanion()
        if (record.payload.isEmpty()) return@synchronized CachedCompanion(expired = true)
        val snapshot = runCatching { WearCompanionSnapshot.decode(record.payload) }.getOrNull()
        if (snapshot == null || !snapshot.isUsable(now) || record.boot != boot() ||
            record.received > elapsed || elapsed - record.received >= WearCompanionSnapshot.MAX_STALE_AGE_MS
        ) {
            save(record.copy(payload = byteArrayOf()))
            return@synchronized CachedCompanion(expired = true)
        }
        val isFresh = snapshot.isFresh(now) && (elapsed - record.received < WearCompanionSnapshot.FRESH_AGE_MS)
        CachedCompanion(snapshot = snapshot, fresh = isFresh, stale = !isFresh, expired = false)
    }

    fun accept(
        node: String,
        payload: ByteArray,
        allowedPhones: Set<String>,
        now: Long = System.currentTimeMillis(),
        elapsed: Long = SystemClock.elapsedRealtime()
    ): Boolean = synchronized(lock) {
        require(node in allowedPhones && allowedPhones.size == 1)
        val incoming = WearCompanionSnapshot.decode(payload)
        val previous = load()
        if (previous?.node == node && incoming.revision <= previous.revision) return@synchronized false
        val bytes = if (incoming.isUsable(now)) payload else byteArrayOf()
        save(Record(node, incoming.revision, elapsed, boot(), bytes))
        changes.value += 1
        true
    }

    fun queuedCommands(): List<WearCommand> = synchronized(queueLock) {
        try {
            DataInputStream(queueFile.openRead()).use { input ->
                val count = input.readInt()
                require(count in 0..MAX_QUEUED_COMMANDS)
                List(count) {
                    val len = input.readInt()
                    require(len in 1..WearCommand.MAX_BYTES)
                    WearCommand.decode(ByteArray(len).also { input.readFully(it) })
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun enqueueOfflineCommand(command: WearCommand) = synchronized(queueLock) {
        val current = queuedCommands().filterNot { it.requestId == command.requestId }.takeLast(MAX_QUEUED_COMMANDS - 1)
        saveQueue(current + command)
        changes.value += 1
    }

    fun removeQueuedCommand(requestId: String) = synchronized(queueLock) {
        val current = queuedCommands()
        val next = current.filterNot { it.requestId == requestId }
        if (next.size != current.size) {
            saveQueue(next)
            changes.value += 1
        }
    }

    private fun saveQueue(commands: List<WearCommand>) {
        val stream = queueFile.startWrite()
        try {
            val out = DataOutputStream(stream)
            out.writeInt(commands.size)
            for (cmd in commands) {
                val bytes = cmd.encode()
                out.writeInt(bytes.size)
                out.write(bytes)
            }
            out.flush()
            queueFile.finishWrite(stream)
        } catch (error: Exception) {
            queueFile.failWrite(stream)
            throw error
        }
    }

    companion object {
        private const val MAX_QUEUED_COMMANDS = 10
        private val lock = Any()
        private val queueLock = Any()
        val changes = MutableStateFlow(0L)
    }
}
