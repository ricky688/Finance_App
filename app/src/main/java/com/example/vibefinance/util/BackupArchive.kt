package com.example.vibefinance.util

import org.json.JSONObject
import java.io.*
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

enum class BackupError { INVALID_FILE, NEWER_VERSION, PASSWORD_REQUIRED, WRONG_PASSWORD, MISSING_IMAGE, STORAGE, INVALID_STATE }
class BackupException(val reason: BackupError, cause: Throwable? = null) : IOException(reason.name, cause)

/** Portable envelope: versioned ZIP, optionally authenticated and encrypted in its entirety. */
internal object BackupArchive {
    // Version 2 preserves per-expense icons. Continue accepting backups made before icons existed.
    const val VERSION = 2
    const val MAX_BYTES = 512L * 1024 * 1024
    const val MAX_ENTRY_BYTES = 128L * 1024 * 1024
    const val ITERATIONS = 1_300_000
    private val magic = "VIBEBK01".toByteArray(Charsets.US_ASCII)

    fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            while (true) { val n = input.read(buffer); if (n < 0) break; digest.update(buffer, 0, n) }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun pack(files: Map<String, File>, metadata: JSONObject, destination: File) {
        require(files.keys.all(::safeName))
        val checksums = JSONObject()
        files.forEach { (name, file) -> checksums.put(name, sha256(file)) }
        val manifest = JSONObject(metadata.toString()).put("formatVersion", VERSION).put("checksums", checksums)
        ZipOutputStream(destination.outputStream().buffered()).use { zip ->
            zip.putNextEntry(ZipEntry("manifest.json"))
            zip.write(manifest.toString().toByteArray(Charsets.UTF_8)); zip.closeEntry()
            files.forEach { (name, file) ->
                zip.putNextEntry(ZipEntry(name)); file.inputStream().use { it.copyTo(zip) }; zip.closeEntry()
            }
        }
    }

    fun write(zip: File, output: OutputStream, password: CharArray?) {
        if (zip.length() > MAX_BYTES) throw BackupException(BackupError.INVALID_FILE)
        val encrypted = password != null
        val salt = if (encrypted) ByteArray(16).also { SecureRandom().nextBytes(it) } else byteArrayOf()
        val nonce = if (encrypted) ByteArray(12).also { SecureRandom().nextBytes(it) } else byteArrayOf()
        val header = ByteArrayOutputStream().also { bytes ->
            DataOutputStream(bytes).apply {
                write(magic); writeInt(VERSION); writeBoolean(encrypted)
                if (encrypted) { writeInt(ITERATIONS); write(salt); write(nonce) }
            }
        }.toByteArray()
        output.write(header)
        if (encrypted) {
            val key = deriveKey(password, salt)
            try {
                val encoded = DataOutputStream(output)
                zip.inputStream().use { input ->
                    val buffer = ByteArray(64 * 1024)
                    var index = 0
                    while (true) {
                        var n = 0
                        while (n < buffer.size) {
                            val read = input.read(buffer, n, buffer.size - n)
                            if (read < 0) break
                            n += read
                        }
                        val last = n == 0
                        val cipher = chunkCipher(Cipher.ENCRYPT_MODE, key, nonce, header, index++, last)
                        val chunk = if (last) cipher.doFinal() else cipher.doFinal(buffer, 0, n)
                        encoded.writeBoolean(last); encoded.writeInt(chunk.size); encoded.write(chunk)
                        if (last) break
                    }
                }
                encoded.flush()
            } finally { key.fill(0) }
        } else zip.inputStream().use { it.copyTo(output) }
    }

    /** Authentication is completed before ZIP parsing or exposing a restore preview. */
    fun read(input: InputStream, destination: File, password: CharArray?): Boolean {
        val data = DataInputStream(input)
        val headerBytes = ByteArrayOutputStream()
        val header = DataOutputStream(headerBytes)
        val foundMagic = ByteArray(magic.size).also { data.readFully(it); header.write(it) }
        if (!foundMagic.contentEquals(magic)) throw BackupException(BackupError.INVALID_FILE)
        val version = data.readInt().also { header.writeInt(it) }
        if (version > VERSION) throw BackupException(BackupError.NEWER_VERSION)
        if (version !in 1..VERSION) throw BackupException(BackupError.INVALID_FILE)
        val flag = data.readUnsignedByte().also { header.writeByte(it) }
        if (flag !in 0..1) throw BackupException(BackupError.INVALID_FILE)
        val encrypted = flag == 1
        if (encrypted) {
            val iterations = data.readInt().also { header.writeInt(it) }
            if (iterations != ITERATIONS) throw BackupException(BackupError.INVALID_FILE)
            val salt = ByteArray(16).also { data.readFully(it); header.write(it) }
            val nonce = ByteArray(12).also { data.readFully(it); header.write(it) }
            if (password == null) throw BackupException(BackupError.PASSWORD_REQUIRED)
            val key = deriveKey(password, salt)
            try {
                destination.outputStream().use { output ->
                    var index = 0
                    var total = 0L
                    while (true) {
                        val lastByte = data.readUnsignedByte()
                        if (lastByte !in 0..1) throw BackupException(BackupError.INVALID_FILE)
                        val last = lastByte == 1
                        val size = data.readInt()
                        if (size !in 16..(64 * 1024 + 16) || (last && size != 16)) throw BackupException(BackupError.INVALID_FILE)
                        val chunk = ByteArray(size).also { data.readFully(it) }
                        val cipher = chunkCipher(Cipher.DECRYPT_MODE, key, nonce, headerBytes.toByteArray(), index++, last)
                        val plaintext = cipher.doFinal(chunk)
                        if (!last && plaintext.isEmpty()) throw BackupException(BackupError.INVALID_FILE)
                        total += plaintext.size
                        if (total > MAX_BYTES || index > MAX_BYTES / (64 * 1024) + 1) throw BackupException(BackupError.INVALID_FILE)
                        output.write(plaintext)
                        if (last) { if (data.read() != -1) throw BackupException(BackupError.INVALID_FILE); break }
                    }
                }
            } catch (e: Exception) {
                destination.delete()
                if (e is BackupException) throw e
                throw BackupException(BackupError.WRONG_PASSWORD, e)
            } finally { key.fill(0) }
        } else destination.outputStream().use { copyLimited(data, it, MAX_BYTES) }
        return encrypted
    }

    fun unpack(zipFile: File, directory: File): JSONObject {
        // Require a complete central directory as well as authenticated file contents.
        val centralNames = mutableSetOf<String>()
        ZipFile(zipFile).use { central ->
            val entries = central.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (entry.isDirectory || !safeName(entry.name) || !centralNames.add(entry.name) || centralNames.size > 10_000)
                    throw BackupException(BackupError.INVALID_FILE)
            }
        }
        val names = mutableSetOf<String>()
        var total = 0L
        ZipInputStream(zipFile.inputStream().buffered()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                val name = entry.name
                if (entry.isDirectory || !safeName(name) || !names.add(name) || names.size > 10_000) {
                    throw BackupException(BackupError.INVALID_FILE)
                }
                val target = File(directory, name)
                target.parentFile!!.mkdirs()
                val limit = when (name) { "manifest.json" -> 1024L * 1024; "data.json", "preferences.json" -> 64L * 1024 * 1024; else -> MAX_ENTRY_BYTES }
                target.outputStream().use { total += copyLimited(zip, it, minOf(limit, MAX_BYTES - total)) }
                zip.closeEntry()
            }
        }
        if (names != centralNames) throw BackupException(BackupError.INVALID_FILE)
        if (!names.containsAll(listOf("manifest.json", "data.json", "preferences.json"))) throw BackupException(BackupError.INVALID_FILE)
        val manifest = JSONObject(File(directory, "manifest.json").readText())
        val version = manifest.getInt("formatVersion")
        if (version > VERSION) throw BackupException(BackupError.NEWER_VERSION)
        if (version !in 1..VERSION) throw BackupException(BackupError.INVALID_FILE)
        val hashes = manifest.getJSONObject("checksums")
        if (hashes.keys().asSequence().toSet() != names - "manifest.json") throw BackupException(BackupError.INVALID_FILE)
        names.filter { it != "manifest.json" }.forEach {
            if (sha256(File(directory, it)) != hashes.getString(it)) throw BackupException(BackupError.INVALID_FILE)
        }
        return manifest
    }

    private fun safeName(name: String): Boolean = name in listOf("manifest.json", "data.json", "preferences.json") ||
        name.matches(Regex("media/[0-9]+\\.img"))

    internal fun copyLimited(input: InputStream, output: OutputStream, limit: Long): Long {
        var count = 0L
        val buffer = ByteArray(8192)
        while (true) {
            val n = input.read(buffer); if (n < 0) break
            count += n
            if (count > limit) throw BackupException(BackupError.INVALID_FILE)
            output.write(buffer, 0, n)
        }
        return count
    }

    private fun deriveKey(password: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password, salt, ITERATIONS, 256)
        return try { SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded }
        finally { spec.clearPassword() }
    }

    /** Authenticated chunk indexes and a final empty chunk detect reordering and truncation.
     * Per-chunk GCM bounds memory on Android providers that buffer until authentication. */
    private fun chunkCipher(mode: Int, key: ByteArray, baseNonce: ByteArray, header: ByteArray, index: Int, last: Boolean): Cipher {
        val nonce = baseNonce.copyOf()
        for (i in 0..3) nonce[8 + i] = (nonce[8 + i].toInt() xor (index ushr (24 - i * 8))).toByte()
        return Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(mode, SecretKeySpec(key, "AES"), GCMParameterSpec(128, nonce))
            updateAAD(header)
            updateAAD(java.nio.ByteBuffer.allocate(5).putInt(index).put(if (last) 1.toByte() else 0.toByte()).array())
        }
    }
}
