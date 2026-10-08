package com.example.vibefinance.util

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.net.Uri
import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.data.entity.*
import com.example.vibefinance.service.PendingPaymentStore
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.junit.runner.RunWith
import java.io.File

/** Settings -> Data & privacy -> Back up app was explored on the device with ARTEMIS. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FullAppBackupEngineTest {
    private lateinit var context: Context
    @Before fun setup() {
        context = RuntimeEnvironment.getApplication()
        BackupStateCodec.preferenceStores.forEach { context.getSharedPreferences(it, 0).edit().clear().commit() }
        InMemoryDatabase.initialize(context)
        InMemoryDatabase.clearAllTables()
        PendingPaymentStore.reloadAfterFullRestore(context)
    }

    private fun account(): Long = InMemoryDatabase.insertAccount(AccountEntity(name = "測試 Card", type = AccountType.CC,
        balance = 12.5, icon = "credit_card", linkedAppPackage = "com.example.uninstalled.bank", notificationAliases = "測試,1234"))
    private fun backup(name: String = "test.vibebackup", password: CharArray? = null): File = File(context.cacheDir, name).also {
        FullAppBackupEngine.export(context, Uri.fromFile(it), password)
    }

    @Test fun suppliedWorkbookRoundTripPreservesAllDataSettingsImagesAndRedirections() {
        val workbook = listOf(File("../財務管家_27-9-2026.xlsx"), File("財務管家_27-9-2026.xlsx")).firstOrNull { it.exists() }
        Assume.assumeNotNull("The user-supplied workbook is a local verification dataset", workbook)
        val imported = workbook!!.inputStream().use { FinancialDataImportEngine.parseStream(it, workbook.name) }
        assertNull(imported.error); assertEquals(868, imported.rawTransactions.size)
        InMemoryDatabase.importFinancialData(imported.detectedAccounts.map { AccountEntity(name = it.name, type = it.detectedType, balance = 0.0, icon = "wallet") },
            imported.rawTransactions.map { InMemoryDatabase.ImportTransactionDraft(it.amount, it.category, it.timestamp,
                it.sourceAccountName, it.destinationAccountName, it.isTransfer, it.isIncome, it.description) }, true)
        val first = InMemoryDatabase.accounts.value.first()
        val image = File(context.filesDir, "card.png").apply { writeBytes(java.util.Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9Y9Zl1sAAAAASUVORK5CYII=")) }
        InMemoryDatabase.updateAccount(first.copy(cardImageUri = image.absolutePath, cardPattern = "wave", cardTheme = "mint",
            linkedAppPackage = "com.example.uninstalled.bank", notificationAliases = "銀行,1234", cardBgOffsetX = 0.2f, cardBgScale = 1.4f))
        InMemoryDatabase.insertBudget(BudgetEntity(id = "test-period", totalBudgetAmount = 4000.0, startDate = 1L, endDate = 2L))
        InMemoryDatabase.insertSubscription(SubscriptionEntity(name = "雲端", amount = 10.0, category = "Other", frequency = "Monthly", nextPaymentDate = 1L, accountId = first.id))
        InMemoryDatabase.insertDiscountShop(DiscountShop(name = "Cafe", aspect = "Dining", latitude = 22.0, longitude = 114.0,
            offers = listOf(ShopDiscountOffer(first.id, 5.0)), isUserCreated = true))
        InMemoryDatabase.saveAspect("自訂"); InMemoryDatabase.setCategoryLimit("Food", 80.0)
        InMemoryDatabase.setCashbackRule(first.id, "Food", 2.5)
        context.getSharedPreferences("vibe_finance_prefs", 0).edit().putString("icon_shape", "RANDOM").putString("theme_mode", "DARK")
            .putBoolean("launch_animation_enabled", false).commit()
        context.getSharedPreferences("assets_display", 0).edit().putBoolean("compact_mode", true).commit()
        context.getSharedPreferences("vibe_merchant_rules", 0).edit().putString("商店", "Food").commit()
        context.getSharedPreferences("vibefinance_prefs", 0).edit().putLong("typed_long", 9876543210L).putInt("typed_int", 7).putFloat("typed_float", 0.5f).commit()
        InMemoryDatabase.setInterceptApps(setOf("com.example.uninstalled.bank"))
        InMemoryDatabase.updateAccount(InMemoryDatabase.accounts.value[1].copy(linkedAppPackage = "none"))
        InMemoryDatabase.updateAccount(InMemoryDatabase.accounts.value[2].copy(linkedAppPackage = null))
        InMemoryDatabase.mergeCategories(CategoryKind.EXPENSE, setOf("Food"), "Other")
        val payment = org.json.JSONArray().put(JSONObject().put("id", "pending-test").put("fingerprint", "unicode-測試")
            .put("sourcePackage", "com.example.bank").put("assetHint", "Visa").put("merchant", "咖啡店")
            .put("amount", 18.0).put("detectedAt", System.currentTimeMillis()).put("isTopUp", false))
        context.getSharedPreferences("pending_payment_choices", 0).edit()
            .putString("pending", payment.toString())
            .putString("remembered_accounts", JSONObject().put("bank|visa", first.id).toString())
            .putString("seen_alerts", JSONObject().put("earlier", System.currentTimeMillis()).toString()).commit()
        PendingPaymentStore.reloadAfterFullRestore(context)
        val accountsBefore = InMemoryDatabase.accounts.value
        val expenses = InMemoryDatabase.transactions.value.filter { it.amount > 0 && it.toAccountId == null }.take(2)
        InMemoryDatabase.updateTransaction(expenses[0].copy(customIcon = "emoji:☕"))
        InMemoryDatabase.updateTransaction(expenses[1].copy(customIcon = "symbol:SHOPPING"))
        val transactionsBefore = InMemoryDatabase.transactions.value
        val preferencesBefore = BackupStateCodec.preferences(context)
        val file = backup()
        InMemoryDatabase.clearAllTables()
        context.getSharedPreferences("vibe_finance_prefs", 0).edit().clear().commit()
        val prepared = FullAppBackupEngine.prepare(context, Uri.fromFile(file), null)
        assertEquals(868, prepared.preview.transactions); assertEquals(14, prepared.preview.accounts); assertEquals(1, prepared.preview.images)
        assertEquals(listOf("com.example.uninstalled.bank"), prepared.preview.unavailableApps)
        FullAppBackupEngine.restore(context, prepared)
        assertFalse(context.getSharedPreferences("vibe_finance_prefs", 0).getBoolean("launch_animation_enabled", true))
        assertEquals(transactionsBefore, InMemoryDatabase.transactions.value)
        assertEquals(accountsBefore.map { it.copy(cardImageUri = null) }, InMemoryDatabase.accounts.value.map { it.copy(cardImageUri = null) })
        assertArrayEquals(image.readBytes(), File(InMemoryDatabase.accounts.value.first().cardImageUri!!).readBytes())
        assertEquals(52831.49, InMemoryDatabase.transactions.value.filter { it.amount > 0 && it.toAccountId == null }.sumOf { it.amount }, 0.0001)
        BackupStateCodec.preferenceStores.forEach { store ->
            val expected = preferencesBefore.getJSONObject(store); val actual = BackupStateCodec.preferences(context).getJSONObject(store)
            assertEquals(expected.keys().asSequence().toSet(), actual.keys().asSequence().toSet())
            expected.keys().forEach { key -> assertEquals(expected.getJSONObject(key).toString(), actual.getJSONObject(key).toString()) }
        }
        assertEquals("pending-test", PendingPaymentStore.pending.value.single().id)
        assertEquals("Other", InMemoryDatabase.categoryMergeRules.value.resolve("Food", CategoryKind.EXPENSE))
        assertEquals(1, InMemoryDatabase.budgets.value.size); assertEquals(1, InMemoryDatabase.subscriptions.value.size)
        assertEquals(1, InMemoryDatabase.discountShops.value.size); assertTrue(InMemoryDatabase.savedAspects.value.contains("自訂"))
        assertEquals(80.0, InMemoryDatabase.categoryLimits.value["Other"]!!, 0.0)
        InMemoryDatabase.reloadAfterFullRestore(context)
        assertEquals(868, InMemoryDatabase.transactions.value.size)
        val newAccount = account()
        assertTrue(newAccount > accountsBefore.maxOf { it.id })
        assertTrue(InMemoryDatabase.insertTransaction(TransactionEntity(accountId = newAccount, amount = 1.0,
            category = "Other", timestamp = 1L, description = "New after restore")) > transactionsBefore.maxOf { it.id })
        assertTrue(InMemoryDatabase.insertSubscription(SubscriptionEntity(name = "New", amount = 1.0,
            category = "Other", frequency = "Monthly", nextPaymentDate = 1L, accountId = newAccount)) > 1L)
        assertTrue(InMemoryDatabase.insertDiscountShop(DiscountShop(name = "New", aspect = "Other",
            latitude = 22.0, longitude = 114.0, offers = emptyList(), isUserCreated = true)) > 1L)
    }



    @Test fun startupRemovesAbandonedStagingWithoutRemovingOtherCacheFiles() {
        val export = File(context.cacheDir, "full_backup_123abc").apply { mkdirs(); resolve("data.json").writeText("private") }
        val restore = File(context.cacheDir, "full_restore_456def").apply { mkdirs(); resolve("payload.zip").writeText("partial") }
        val other = File(context.cacheDir, "other-file").apply { writeText("keep") }
        FullAppBackupEngine.recoverAtStartup(context)
        assertFalse(export.exists()); assertFalse(restore.exists()); assertTrue(other.exists())
    }

    @Test fun unavailableOutputPreservesStateWipesPasswordAndCleansTemporaryFiles() {
        account()
        val before = InMemoryDatabase.accounts.value
        val secret = "temporary-password".toCharArray()
        expect(BackupError.STORAGE) { FullAppBackupEngine.export(context, Uri.fromFile(context.filesDir), secret) }
        expect(BackupError.STORAGE) { FullAppBackupEngine.prepare(context, Uri.fromFile(File(context.filesDir, "unavailable.vibebackup")), null) }
        assertEquals(before, InMemoryDatabase.accounts.value)
        assertTrue(secret.all { it == '\u0000' })
        assertTrue(context.cacheDir.listFiles().orEmpty().none { it.name.startsWith("full_backup_") })
    }

    @Test fun committedRestoreJournalIsCleanedWithoutUndoingNewData() {
        val id = account()
        val journal = File(context.filesDir, "full_restore_journal").apply { mkdirs() }
        File(journal, "state.json").writeText(JSONObject().put("committed", true).toString())
        FullAppBackupEngine.recoverInterruptedRestore(context)
        InMemoryDatabase.reloadAfterFullRestore(context)
        assertEquals(id, InMemoryDatabase.accounts.value.single().id)
        assertFalse(journal.exists())
    }

    @Test fun duplicateIdsAndMissingAccountReferencesAreRejectedBeforeReplacement() {
        val id = account()
        val before = InMemoryDatabase.accounts.value
        val snapshot = InMemoryDatabase.snapshotForBackup()
        val prefs = BackupStateCodec.preferences(context)
        val files = setOf("data.json", "preferences.json")
        val duplicate = JSONObject(snapshot.toString())
        duplicate.getJSONArray("accounts").put(JSONObject(duplicate.getJSONArray("accounts").getJSONObject(0).toString()))
        try { BackupStateCodec.validateData(duplicate, prefs, files); fail("Duplicate IDs accepted") }
        catch (_: IllegalArgumentException) { }
        InMemoryDatabase.insertTransaction(TransactionEntity(accountId = id, amount = 2.0, category = "Food", timestamp = 1L, description = "Test"))
        val invalidReference = InMemoryDatabase.snapshotForBackup()
        invalidReference.getJSONArray("transactions").getJSONObject(0).put("accountId", id + 1000)
        try { BackupStateCodec.validateData(invalidReference, prefs, files); fail("Missing account accepted") }
        catch (_: IllegalArgumentException) { }
        assertEquals(before, InMemoryDatabase.accounts.value)
    }

    @Test fun emptyBackupReplacesCurrentDataAndCountersRemainUsable() {
        val prepared = FullAppBackupEngine.prepare(context, Uri.fromFile(backup()), null)
        account()
        FullAppBackupEngine.restore(context, prepared)
        assertTrue(InMemoryDatabase.accounts.value.isEmpty())
        assertTrue(InMemoryDatabase.transactions.value.isEmpty())
        InMemoryDatabase.reloadAfterFullRestore(context)
        assertTrue(InMemoryDatabase.accounts.value.isEmpty())
        assertTrue(account() > 0)
    }

    @Test fun encryptedBackupRequiresCorrectUnicodePasswordAndKeepsCurrentStateOnFailure() {
        val accountId = account()
        InMemoryDatabase.insertTransaction(TransactionEntity(accountId = accountId, amount = 9.9, category = "Food", timestamp = 1L,
            description = "咖啡", customIcon = "emoji:☕"))
        val transactionsBefore = InMemoryDatabase.transactions.value
        val before = InMemoryDatabase.accounts.value
        val password = "測試🔒password".toCharArray()
        val file = backup(password = password)
        assertTrue(password.all { it == '\u0000' })
        expect(BackupError.PASSWORD_REQUIRED) { FullAppBackupEngine.prepare(context, Uri.fromFile(file), null) }
        expect(BackupError.WRONG_PASSWORD) { FullAppBackupEngine.prepare(context, Uri.fromFile(file), "wrong".toCharArray()) }
        assertEquals(before, InMemoryDatabase.accounts.value)
        val prepared = FullAppBackupEngine.prepare(context, Uri.fromFile(file), "測試🔒password".toCharArray())
        FullAppBackupEngine.restore(context, prepared)
        assertEquals(before, InMemoryDatabase.accounts.value)
        assertEquals(transactionsBefore, InMemoryDatabase.transactions.value)
    }

    @Test fun malformedIconIsRejectedBeforeReplacingCurrentData() {
        val id = account()
        InMemoryDatabase.insertTransaction(TransactionEntity(accountId = id, amount = 9.9, category = "Food", timestamp = 1L))
        val snapshot = InMemoryDatabase.snapshotForBackup()
        val before = InMemoryDatabase.transactions.value
        listOf("symbol:UNKNOWN", "emoji:☕🍜", 42).forEach { icon ->
            snapshot.getJSONArray("transactions").getJSONObject(0).put("customIcon", icon)
            try {
                BackupStateCodec.validateData(snapshot, BackupStateCodec.preferences(context), setOf("data.json", "preferences.json"))
                fail("Malformed icon accepted: $icon")
            } catch (_: IllegalArgumentException) { }
            assertEquals(before, InMemoryDatabase.transactions.value)
        }
    }

    @Test fun cancellingPreviewDoesNotChangeDataAndCleansStaging() {
        account(); val before = InMemoryDatabase.accounts.value
        val prepared = FullAppBackupEngine.prepare(context, Uri.fromFile(backup()), null)
        val directory = prepared.directory
        prepared.close()
        assertFalse(directory.exists()); assertEquals(before, InMemoryDatabase.accounts.value)
    }

    @Test fun missingCardImageStopsExportWithoutChangingData() {
        val id = account(); val account = InMemoryDatabase.accounts.value.single()
        InMemoryDatabase.updateAccount(account.copy(cardImageUri = File(context.filesDir, "missing.png").absolutePath))
        expect(BackupError.MISSING_IMAGE) { backup() }
        assertEquals(id, InMemoryDatabase.accounts.value.single().id)
    }

    @Test fun failedPreferenceCommitRollsBackEarlierStoresAndFinancialSnapshot() {
        account()
        val file = backup()
        val prepared = FullAppBackupEngine.prepare(context, Uri.fromFile(file), null)
        val added = account()
        context.getSharedPreferences("vibe_finance_prefs", 0).edit().putString("theme_mode", "LIGHT").commit()
        var failed = false
        val failingContext = object : ContextWrapper(context) {
            override fun getSharedPreferences(name: String, mode: Int): SharedPreferences {
                val prefs = super.getSharedPreferences(name, mode)
                if (name != "assets_display") return prefs
                return object : SharedPreferences by prefs {
                    override fun edit(): SharedPreferences.Editor {
                        val editor = prefs.edit()
                        return object : SharedPreferences.Editor by editor {
                            override fun clear(): SharedPreferences.Editor { editor.clear(); return this }
                            override fun commit(): Boolean {
                                if (!failed) { failed = true; return false }
                                return editor.commit()
                            }
                        }
                    }
                }
            }
        }
        expect(BackupError.STORAGE) { FullAppBackupEngine.restore(failingContext, prepared) }
        assertEquals(2, InMemoryDatabase.accounts.value.size)
        assertTrue(InMemoryDatabase.accounts.value.any { it.id == added })
        assertEquals("LIGHT", context.getSharedPreferences("vibe_finance_prefs", 0).getString("theme_mode", null))
        assertFalse(File(context.filesDir, "full_restore_journal").exists())
    }

    @Test fun interruptedRestoreIsRolledBackBeforeStoresInitializeAndRecoveryIsIdempotent() {
        account()
        val oldData = InMemoryDatabase.snapshotForBackup().toString()
        val oldPreferences = BackupStateCodec.preferences(context).toString()
        val journal = File(context.filesDir, "full_restore_journal").apply { mkdirs() }
        File(journal, "old_data.json").writeText(oldData)
        File(journal, "old_preferences.json").writeText(oldPreferences)
        val media = File(context.filesDir, "restored_media_123abc").apply { mkdirs(); resolve("0.img").writeText("staged") }
        File(journal, "state.json").writeText(JSONObject().put("committed", false).put("hadData", true).put("mediaDirectory", media.name).toString())
        File(context.filesDir, "vibefinance_data.json").writeText("{broken")
        context.getSharedPreferences("assets_display", 0).edit().putBoolean("compact_mode", true).commit()
        FullAppBackupEngine.recoverInterruptedRestore(context)
        FullAppBackupEngine.recoverInterruptedRestore(context)
        assertEquals(oldData, File(context.filesDir, "vibefinance_data.json").readText())
        assertFalse(context.getSharedPreferences("assets_display", 0).getBoolean("compact_mode", false))
        assertFalse(journal.exists()); assertFalse(media.exists())
        InMemoryDatabase.reloadAfterFullRestore(context)
        assertEquals(1, InMemoryDatabase.accounts.value.size)
    }

    private fun expect(error: BackupError, action: () -> Unit) {
        try { action(); fail("Expected $error") } catch (e: BackupException) { assertEquals(error, e.reason) }
    }
}
