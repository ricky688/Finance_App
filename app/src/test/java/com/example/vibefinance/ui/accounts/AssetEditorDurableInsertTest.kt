package com.example.vibefinance.ui.accounts

import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.data.entity.*
import java.io.File
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AssetEditorDurableInsertTest {
    private val context
        get() = RuntimeEnvironment.getApplication()

    @Before
    fun before() {
        InMemoryDatabase.initialize(context)
        InMemoryDatabase.clearAllTables()
    }

    @After
    fun after() {
        File(context.filesDir, "vibefinance_data.json.new").deleteRecursively()
        InMemoryDatabase.clearAllTables()
    }

    @Test
    fun failedInsertionPreservesRecordsDiskAndNextId_thenRetrySurvivesReload() {
        val account =
            AccountEntity(name = "銀行", type = AccountType.BANK, balance = 13.5, icon = "bank")
        val first = InMemoryDatabase.insertAccount(account, requireDurable = true)
        val before = InMemoryDatabase.accounts.value
        val disk = File(context.filesDir, "vibefinance_data.json")
        val bytes = disk.readText()
        val blocked = File(context.filesDir, "vibefinance_data.json.new")
        blocked.mkdir()
        try {
            InMemoryDatabase.insertAccount(account.copy(name = "Retry"), requireDurable = true)
            fail("Storage failure must be returned to the editor")
        } catch (_: java.io.IOException) {}
        assertEquals(before, InMemoryDatabase.accounts.value)
        assertEquals(bytes, disk.readText())
        blocked.deleteRecursively()
        val retry =
            InMemoryDatabase.insertAccount(account.copy(name = "Retry"), requireDurable = true)
        assertEquals(first + 1, retry)
        InMemoryDatabase.initialize(context)
        assertEquals(listOf(first, retry), InMemoryDatabase.accounts.value.map { it.id })
    }
}
