package com.example.vibefinance.util

import android.content.Context
import android.net.Uri
import android.util.AtomicFile
import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.service.PendingPaymentStore
import org.json.JSONObject
import java.io.File
import java.util.UUID

data class FullBackupPreview(
    val createdAt: Long,
    val appVersion: String,
    val accounts: Int,
    val transactions: Int,
    val subscriptions: Int,
    val images: Int,
    val unavailableApps: List<String>
)

/** A validated, private staging directory; no password or external URI is retained. */
class PreparedFullBackup internal constructor(
    internal val directory: File,
    internal val data: JSONObject,
    internal val preferences: JSONObject,
    val preview: FullBackupPreview
) : AutoCloseable { override fun close() { directory.deleteRecursively() } }

object FullAppBackupEngine {
    private const val JOURNAL = "full_restore_journal"
    private const val MARKER = "state.json"
    private const val DATA = "vibefinance_data.json"

    /** Notification routing takes its own monitor before the data lock; always retain that order. */
    private fun <T> coordinated(block: () -> T): T = synchronized(PendingPaymentStore) {
        synchronized(InMemoryDatabase.diskIoLock) { block() }
    }

    fun export(context: Context, uri: Uri, password: CharArray?) {
        val stage = File(context.cacheDir, "full_backup_${UUID.randomUUID()}")
        try {
            if (!stage.mkdirs()) throw BackupException(BackupError.STORAGE)
            val files = linkedMapOf<String, File>()
            val metadata = coordinated {
                val data = InMemoryDatabase.snapshotForBackup()
                val prefs = BackupStateCodec.preferences(context)
                val accounts = data.getJSONArray("accounts")
                val copied = mutableMapOf<String, String>()
                for (i in 0 until accounts.length()) {
                    val account = accounts.getJSONObject(i)
                    val source = account.optString("cardImageUri").takeIf { it.isNotBlank() } ?: continue
                    val name = copied[source] ?: "media/${copied.size}.img".also { name ->
                        val target = File(stage, name).apply { parentFile!!.mkdirs() }
                        try {
                            val sourceUri = Uri.parse(source)
                            val input = if (sourceUri.scheme == null || sourceUri.scheme == "file") {
                                File(sourceUri.path ?: source).inputStream()
                            } else context.contentResolver.openInputStream(sourceUri)
                                ?: throw BackupException(BackupError.MISSING_IMAGE)
                            input.use { stream -> target.outputStream().use { BackupArchive.copyLimited(stream, it, BackupArchive.MAX_ENTRY_BYTES) } }
                            if (target.length() == 0L) throw BackupException(BackupError.MISSING_IMAGE)
                        } catch (e: Exception) { throw BackupException(BackupError.MISSING_IMAGE, e) }
                        copied[source] = name; files[name] = target
                    }
                    account.put("cardImageUri", name)
                }
                val dataFile = File(stage, "data.json").apply { writeText(data.toString()) }
                val prefsFile = File(stage, "preferences.json").apply { writeText(prefs.toString()) }
                files["data.json"] = dataFile; files["preferences.json"] = prefsFile
                try { BackupStateCodec.validateData(data, prefs, files.keys) }
                catch (e: Exception) { throw BackupException(BackupError.INVALID_STATE, e) }
                require(files.values.sumOf { it.length() } <= BackupArchive.MAX_BYTES - 1024 * 1024)
                JSONObject().put("createdAt", System.currentTimeMillis())
                    .put("appVersion", context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "")
                    .put("counts", BackupStateCodec.counts(data))
            }
            val archive = File(stage, "payload.zip")
            BackupArchive.pack(files, metadata, archive)
            context.contentResolver.openOutputStream(uri, "wt")?.use { BackupArchive.write(archive, it, password) }
                ?: throw BackupException(BackupError.STORAGE)
        } catch (e: Exception) {
            // A failed export is not a valid backup; remove its partially written document when supported.
            runCatching { android.provider.DocumentsContract.deleteDocument(context.contentResolver, uri) }
            if (e is BackupException) throw e
            throw BackupException(BackupError.STORAGE, e)
        } finally { password?.fill('\u0000'); stage.deleteRecursively() }
    }

    fun prepare(context: Context, uri: Uri, password: CharArray?): PreparedFullBackup {
        val stage = File(context.cacheDir, "full_restore_${UUID.randomUUID()}")
        try {
            if (!stage.mkdirs()) throw BackupException(BackupError.STORAGE)
            val archive = File(stage, "payload.zip")
            context.contentResolver.openInputStream(uri)?.use { BackupArchive.read(it, archive, password) }
                ?: throw BackupException(BackupError.STORAGE)
            val manifest = BackupArchive.unpack(archive, stage)
            archive.delete()
            val data = JSONObject(File(stage, "data.json").readText())
            val prefs = JSONObject(File(stage, "preferences.json").readText())
            val names = manifest.getJSONObject("checksums").keys().asSequence().toSet()
            BackupStateCodec.validateData(data, prefs, names)
            val counts = BackupStateCodec.counts(data)
            val storedCounts = manifest.getJSONObject("counts")
            counts.keys().forEach { require(counts.getInt(it) == storedCounts.getInt(it)) }
            val createdAt = manifest.getLong("createdAt").also { require(it > 0) }
            val accounts = data.getJSONArray("accounts")
            val unavailable = (0 until accounts.length()).mapNotNull { index ->
                accounts.getJSONObject(index).optString("linkedAppPackage").takeIf { it.isNotBlank() && it != "none" }
            }.distinct().filterNot { LocalAppManager.isAppInstalled(context, it) }
            return PreparedFullBackup(stage, data, prefs, FullBackupPreview(createdAt, manifest.getString("appVersion"),
                counts.getInt("accounts"), counts.getInt("transactions"), counts.getInt("subscriptions"),
                names.count { it.startsWith("media/") }, unavailable))
        } catch (e: Exception) {
            stage.deleteRecursively()
            if (e is BackupException) throw e
            val reason = when (e) {
                is SecurityException -> BackupError.STORAGE
                is java.util.zip.ZipException, is java.io.EOFException -> BackupError.INVALID_FILE
                is java.io.IOException -> BackupError.STORAGE
                else -> BackupError.INVALID_FILE
            }
            throw BackupException(reason, e)
        } finally { password?.fill('\u0000') }
    }

    /** Commit all stores, then publish state; a durable undo journal also covers process death. */
    fun restore(context: Context, prepared: PreparedFullBackup) = coordinated {
        val journal = File(context.filesDir, JOURNAL)
        val mediaName = "restored_media_${UUID.randomUUID()}"
        val media = File(context.filesDir, mediaName)
        try {
            recoverInterruptedRestore(context)
            check(journal.mkdirs())
            val current = File(context.filesDir, DATA)
            if (current.exists()) atomicWrite(File(journal, "old_data.json"), AtomicFile(current).readFully())
            atomicWrite(File(journal, "old_preferences.json"), BackupStateCodec.preferences(context).toString().toByteArray())
            val marker = JSONObject().put("committed", false).put("hadData", current.exists()).put("mediaDirectory", mediaName)
            atomicWrite(File(journal, MARKER), marker.toString().toByteArray())
            val restoredData = JSONObject(prepared.data.toString())
            val accounts = restoredData.getJSONArray("accounts")
            for (i in 0 until accounts.length()) {
                val account = accounts.getJSONObject(i)
                val name = account.optString("cardImageUri").takeIf { it.isNotBlank() } ?: continue
                val target = File(media, name.removePrefix("media/"))
                target.parentFile!!.mkdirs()
                if (!target.exists()) {
                    prepared.directory.resolve(name).inputStream().use { input ->
                        target.outputStream().use { out -> input.copyTo(out); out.fd.sync() }
                    }
                }
                account.put("cardImageUri", target.absolutePath)
            }
            atomicWrite(current, restoredData.toString().toByteArray())
            BackupStateCodec.applyPreferences(context, prepared.preferences)
            marker.put("committed", true)
            atomicWrite(File(journal, MARKER), marker.toString().toByteArray())
            InMemoryDatabase.reloadAfterFullRestore(context)
            PendingPaymentStore.reloadAfterFullRestore(context)
            journal.deleteRecursively()
        } catch (e: Exception) {
            recoverInterruptedRestore(context)
            InMemoryDatabase.reloadAfterFullRestore(context)
            PendingPaymentStore.reloadAfterFullRestore(context)
            if (e is BackupException) throw e
            throw BackupException(BackupError.STORAGE, e)
        } finally { prepared.close() }
    }

    /** Startup runs before any new staging directories or persistent-store readers exist. */
    fun recoverAtStartup(context: Context) {
        recoverInterruptedRestore(context)
        context.cacheDir.listFiles().orEmpty().filter {
            it.isDirectory && it.name.matches(Regex("full_(backup|restore)_[a-f0-9-]+"))
        }.forEach { it.deleteRecursively() }
    }

    /** Invoked by Application before activity/service initialization; rollback is repeatable. */
    fun recoverInterruptedRestore(context: Context) = coordinated {
        val journal = File(context.filesDir, JOURNAL)
        if (!journal.exists()) return@coordinated
        val markerFile = File(journal, MARKER)
        if (!markerFile.exists() && !File(journal, "$MARKER.bak").exists()) { journal.deleteRecursively(); return@coordinated }
        val marker = JSONObject(String(AtomicFile(markerFile).readFully(), Charsets.UTF_8))
        if (!marker.getBoolean("committed")) {
            val prefs = JSONObject(File(journal, "old_preferences.json").readText())
            BackupStateCodec.applyPreferences(context, prefs)
            val current = AtomicFile(File(context.filesDir, DATA))
            if (marker.getBoolean("hadData")) atomicWrite(current.baseFile, File(journal, "old_data.json").readBytes())
            else current.delete()
            val name = marker.getString("mediaDirectory")
            require(name.matches(Regex("restored_media_[a-f0-9-]+")))
            File(context.filesDir, name).deleteRecursively()
        }
        journal.deleteRecursively()
    }

    private fun atomicWrite(file: File, bytes: ByteArray) {
        val atomic = AtomicFile(file)
        val stream = atomic.startWrite()
        try { stream.write(bytes); atomic.finishWrite(stream) }
        catch (e: Exception) { atomic.failWrite(stream); throw e }
    }
}
