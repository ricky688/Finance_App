package com.example.vibefinance.util

import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [24, 34])
class BackupArchiveTest {
    @Rule @JvmField val temporary = org.junit.rules.TemporaryFolder()
    @Test fun plainArchiveRoundTripVerifiesEveryFile() {
        val data = temporary.newFile().apply { writeText("測試") }
        val preferences = temporary.newFile().apply { writeText("{}") }
        val zip = temporary.newFile()
        BackupArchive.pack(mapOf("data.json" to data, "preferences.json" to preferences), JSONObject().put("createdAt", 1), zip)
        val bytes = ByteArrayOutputStream().also { BackupArchive.write(zip, it, null) }.toByteArray()
        val decoded = temporary.newFile()
        assertFalse(BackupArchive.read(bytes.inputStream(), decoded, null))
        val directory = temporary.newFolder()
        assertEquals(BackupArchive.VERSION, BackupArchive.unpack(decoded, directory).getInt("formatVersion"))
        assertEquals("測試", File(directory, "data.json").readText())
    }
    @Test fun damagedChecksumAndUnknownEntriesAreRejected() {
        val data = temporary.newFile().apply { writeText("{}") }
        val zip = temporary.newFile()
        ZipOutputStream(zip.outputStream()).use { output ->
            mapOf("manifest.json" to "{\"formatVersion\":${BackupArchive.VERSION},\"checksums\":{\"data.json\":\"wrong\",\"preferences.json\":\"wrong\"}}",
                "data.json" to "{}", "preferences.json" to "{}").forEach { (name, content) -> output.putNextEntry(ZipEntry(name)); output.write(content.toByteArray()); output.closeEntry() }
        }
        invalid { BackupArchive.unpack(zip, temporary.newFolder()) }
        listOf("../data.json", "/data.json", "media/../escape", "unknown.json").forEach { name ->
            ZipOutputStream(zip.outputStream()).use { it.putNextEntry(ZipEntry(name)); it.write(data.readBytes()); it.closeEntry() }
            invalid { BackupArchive.unpack(zip, temporary.newFolder()) }
        }
    }
    @Test fun duplicateEntriesAndSizeLimitsAreRejected() {
        val zip = temporary.newFile()
        ZipOutputStream(zip.outputStream()).use { output ->
            listOf("data.json", "aaaa.json").forEach { output.putNextEntry(ZipEntry(it)); output.write("{}".toByteArray()); output.closeEntry() }
        }
        // ZIP writers prohibit duplicates; replace equal-length filenames in both headers.
        zip.writeBytes(String(zip.readBytes(), Charsets.ISO_8859_1).replace("aaaa.json", "data.json").toByteArray(Charsets.ISO_8859_1))
        invalid { BackupArchive.unpack(zip, temporary.newFolder()) }
        invalid { BackupArchive.copyLimited(ByteArray(17).inputStream(), ByteArrayOutputStream(), 16) }
    }
    @Test fun newerEnvelopeAndManifestVersionsAreRejected() {
        val header = ByteArrayOutputStream().also { DataOutputStream(it).apply { write("VIBEBK01".toByteArray()); writeInt(BackupArchive.VERSION + 1); writeBoolean(false) } }
        try { BackupArchive.read(header.toByteArray().inputStream(), temporary.newFile(), null); fail() }
        catch (e: BackupException) { assertEquals(BackupError.NEWER_VERSION, e.reason) }
        val zip = temporary.newFile()
        ZipOutputStream(zip.outputStream()).use { output ->
            mapOf("manifest.json" to "{\"formatVersion\":${BackupArchive.VERSION + 1}}", "data.json" to "{}", "preferences.json" to "{}").forEach {
                output.putNextEntry(ZipEntry(it.key)); output.write(it.value.toByteArray()); output.closeEntry()
            }
        }
        try { BackupArchive.unpack(zip, temporary.newFolder()); fail() }
        catch (e: BackupException) { assertEquals(BackupError.NEWER_VERSION, e.reason) }
    }
    @Test fun preferenceTypesAndUnknownStoresAreRejected() {
        val prefs = JSONObject().apply { BackupStateCodec.preferenceStores.forEach { put(it, JSONObject()) } }
        prefs.getJSONObject("vibe_finance_prefs").put("theme_mode", JSONObject().put("type", "int").put("value", 3))
        try { BackupStateCodec.validatePreferences(prefs); fail() } catch (_: IllegalArgumentException) { }
        prefs.getJSONObject("vibe_finance_prefs").remove("theme_mode")
        prefs.put("unknown", JSONObject())
        try { BackupStateCodec.validatePreferences(prefs); fail() } catch (_: IllegalArgumentException) { }
    }
    @Test fun encryptedArchiveAuthenticatesMultipleChunksAndRejectsTruncationAndTampering() {
        val payload = temporary.newFile().apply { writeBytes(ByteArray(150_000) { (it % 255).toByte() }) }
        val output = ByteArrayOutputStream()
        BackupArchive.write(payload, output, "秘密🔒".toCharArray())
        val encoded = output.toByteArray()
        val decoded = temporary.newFile()
        assertTrue(BackupArchive.read(encoded.inputStream(), decoded, "秘密🔒".toCharArray()))
        assertArrayEquals(payload.readBytes(), decoded.readBytes())
        val modified = encoded.copyOf().apply { this[100] = (this[100].toInt() xor 1).toByte() }
        listOf(modified, encoded.copyOf(encoded.size - 21)).forEach { bytes ->
            try { BackupArchive.read(bytes.inputStream(), decoded, "秘密🔒".toCharArray()); fail("Unauthenticated backup accepted") }
            catch (e: BackupException) { assertEquals(BackupError.WRONG_PASSWORD, e.reason) }
            assertFalse(decoded.exists())
        }
    }
    @Test fun versionOneBackupsRemainReadable() {
        val data = temporary.newFile().apply { writeText("{}") }
        val prefs = temporary.newFile().apply { writeText("{}") }
        val manifest = JSONObject().put("formatVersion", 1).put("checksums", JSONObject()
            .put("data.json", BackupArchive.sha256(data)).put("preferences.json", BackupArchive.sha256(prefs)))
        val zip = temporary.newFile()
        ZipOutputStream(zip.outputStream()).use { output ->
            mapOf("manifest.json" to manifest.toString(), "data.json" to data.readText(), "preferences.json" to prefs.readText()).forEach { (name, content) ->
                output.putNextEntry(ZipEntry(name)); output.write(content.toByteArray()); output.closeEntry()
            }
        }
        val oldBackup = ByteArrayOutputStream().also { bytes ->
            DataOutputStream(bytes).apply { write("VIBEBK01".toByteArray()); writeInt(1); writeBoolean(false); write(zip.readBytes()) }
        }
        val decoded = temporary.newFile()
        assertFalse(BackupArchive.read(oldBackup.toByteArray().inputStream(), decoded, null))
        assertEquals(1, BackupArchive.unpack(decoded, temporary.newFolder()).getInt("formatVersion"))
    }
    private fun invalid(action: () -> Unit) {
        try { action(); fail("Invalid archive accepted") } catch (e: BackupException) { assertEquals(BackupError.INVALID_FILE, e.reason) }
    }
}
