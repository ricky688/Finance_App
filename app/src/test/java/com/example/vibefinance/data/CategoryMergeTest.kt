package com.example.vibefinance.data

import android.content.Context
import com.example.vibefinance.ai.MerchantRuleEngine
import com.example.vibefinance.data.entity.*
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

/** Settings/source/target/review/cancel path explored with ARTEMIS on Waydroid first.
 * Robolectric storage is isolated from the user's ledger. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CategoryMergeTest {
    private lateinit var context: Context
    private val db get() = InMemoryDatabase
    private fun tx(category: String, amount: Double = 12.6, time: Long = 1000L) =
        TransactionEntity(amount = amount, category = category, timestamp = time, accountId = 1)
    private fun sub(category: String) = SubscriptionEntity(name = "Recurring", amount = 10.0,
        category = category, frequency = "Monthly", nextPaymentDate = 3000L, accountId = 1)
    @Before fun setup() {
        context = RuntimeEnvironment.getApplication()
        db.initialize(context); db.clearAllTables()
        context.getSharedPreferences("vibe_merchant_rules", Context.MODE_PRIVATE).edit().clear().commit()
        db.insertAccount(AccountEntity(name = "Mox", type = AccountType.BANK, balance = 500.0, icon = "bank"))
    }
    @After fun cleanup() {
        File(context.filesDir, "vibefinance_data.json.new").deleteRecursively()
        db.clearAllTables()
    }
    @Test fun multipleSourcesRelabelPastFutureAndRecurringWithoutChangingMoneyOrMetadata() {
        db.insertTransactions(listOf(tx("Food"), tx("Transport", time = Long.MAX_VALUE).copy(
            installmentNumber = 2, totalInstallments = 3, groupId = "installment-test"),
            tx("Shopping"), tx("Food", -300.0), tx("Food").copy(toAccountId = 2),
            tx("Food").copy(isBalanceAdjustment = true, balanceAdjustmentDelta = 12.6)))
        db.insertSubscription(sub("Food")); db.insertSubscription(sub("Transport"))
        val accounts = db.accounts.value; val old = db.transactions.value
        val result = db.mergeCategories(CategoryKind.EXPENSE, setOf("Food", "Transport"), "Other")
        assertEquals(2, result.transactionCount); assertEquals(2, result.subscriptionCount)
        assertEquals(accounts, db.accounts.value)
        old.zip(db.transactions.value).forEachIndexed { index, (before, after) ->
            assertEquals(before.copy(category = if (index < 2) "Other" else before.category), after)
        }
        assertEquals(listOf("Other", "Other"), db.subscriptions.value.map { it.category })
    }
    @Test fun budgetsAreSummedAndOnlyRetiredChoicesDisappear() {
        db.setCategoryLimit("Food", 100.0); db.setCategoryLimit("Transport", 50.0)
        db.setCategoryLimit("Other", 20.0); db.setCategoryLimit("Shopping", 30.0)
        db.mergeCategories(CategoryKind.EXPENSE, setOf("Food", "Transport"), "Other")
        assertEquals(mapOf("Other" to 170.0, "Shopping" to 30.0), db.categoryLimits.value)
        val choices = categoryChoices(CategoryKind.EXPENSE, db.transactions.value, rules = db.categoryMergeRules.value)
        assertFalse("Food" in choices); assertFalse("Transport" in choices); assertTrue("Other" in choices)
        db.setCategoryLimit("Food", 200.0)
        assertEquals(200.0, db.categoryLimits.value.getValue("Other"), 0.0)
    }
    @Test fun incomeAndExpenseWithSameLabelRemainIndependent() {
        db.insertTransactions(listOf(tx("Food", -300.0), tx("Food", 25.0)))
        db.setCategoryLimit("Food", 100.0)
        db.mergeCategories(CategoryKind.INCOME, setOf("Food"), "Salary")
        assertEquals(listOf("Salary", "Food"), db.transactions.value.map { it.category })
        assertEquals(mapOf("Food" to 100.0), db.categoryLimits.value)
        assertEquals("Food", db.categoryMergeRules.value.resolve("Food", CategoryKind.EXPENSE))
        assertEquals("Salary", db.categoryMergeRules.value.resolve("Food", CategoryKind.INCOME))
    }
    @Test fun futureInsertEditBulkAndRecurringCannotReintroduceRetiredCategory() {
        db.mergeCategories(CategoryKind.EXPENSE, setOf("Food"), "Other")
        val id = db.insertTransaction(tx("Food")); db.insertTransactions(listOf(tx("Food")))
        db.updateTransaction(db.transactions.value.first { it.id == id }.copy(category = "Food", description = "Edited"))
        val sid = db.insertSubscription(sub("Food"))
        db.updateSubscription(db.subscriptions.value.first { it.id == sid }.copy(category = "Food"))
        assertTrue(db.transactions.value.all { it.category == "Other" })
        assertEquals("Other", db.subscriptions.value.single().category)
    }
    @Test fun notificationRetainsDeduplicationAndAppliesBalanceOnceAfterMerge() {
        db.mergeCategories(CategoryKind.EXPENSE, setOf("Food"), "Other")
        val notification = tx("Food").copy(groupId = "notification:smart-octopus-regression")
        assertTrue(db.insertNotificationExpenseIfAbsent(notification))
        assertFalse(db.insertNotificationExpenseIfAbsent(notification))
        assertEquals("Other", db.transactions.value.single().category)
        assertEquals(487.4, db.accounts.value.single().balance, 0.001)
    }
    @Test fun importedExpensesUseMergedCategoryAndIncomeNamespaceStaysSeparate() {
        db.mergeCategories(CategoryKind.EXPENSE, setOf("Food"), "Other")
        val drafts = listOf(
            InMemoryDatabase.ImportTransactionDraft(12.6, "Food", 1000L, "Mox", null, false, false, "Imported expense"),
            InMemoryDatabase.ImportTransactionDraft(-300.0, "Food", 1000L, "Mox", null, false, true, "Imported income"))
        assertEquals(2, db.importFinancialData(db.accounts.value, drafts, replaceExisting = false))
        assertEquals(listOf("Other", "Food"), db.transactions.value.map { it.category })
    }
    @Test fun chainedMergesSurviveDiskReloadAndResolveOriginalCategory() {
        db.insertTransaction(tx("Food")); db.insertSubscription(sub("Food"))
        db.mergeCategories(CategoryKind.EXPENSE, setOf("Food"), "Shopping")
        db.mergeCategories(CategoryKind.EXPENSE, setOf("Shopping"), "Other")
        val before = db.transactions.value
        db.categoryMergeRules.value = CategoryMergeRules(); db.transactions.value = emptyList()
        db.initialize(context)
        assertEquals(before, db.transactions.value)
        assertEquals("Other", db.categoryMergeRules.value.resolve("Food", CategoryKind.EXPENSE))
        assertEquals("Other", db.subscriptions.value.single().category)
    }
    @Test fun storageFailureDoesNotPublishPartialMergeOrChangeSavedLedger() {
        db.insertTransaction(tx("Food")); db.setCategoryLimit("Food", 40.0)
        val old = db.transactions.value; val limits = db.categoryLimits.value; val accounts = db.accounts.value
        val file = File(context.filesDir, "vibefinance_data.json"); val bytes = file.readBytes()
        val blocker = File(context.filesDir, "vibefinance_data.json.new")
        assertTrue(blocker.mkdir()); File(blocker, "block").writeText("prevent overwrite")
        try {
            assertThrows(Exception::class.java) { db.mergeCategories(CategoryKind.EXPENSE, setOf("Food"), "Other") }
            assertEquals(old, db.transactions.value); assertEquals(limits, db.categoryLimits.value)
            assertEquals(accounts, db.accounts.value); assertEquals(CategoryMergeRules(), db.categoryMergeRules.value)
            assertArrayEquals(bytes, file.readBytes())
        } finally { blocker.deleteRecursively() }
    }
    @Test fun invalidOrStaleSelectionsAreRejectedWithoutMutation() {
        for ((sources, target) in listOf(setOf("Food") to "Food", emptySet<String>() to "Other",
            setOf("Transfer") to "Other", setOf("Balance Adjustment") to "Other", setOf("Missing") to "Other")) {
            assertThrows(IllegalArgumentException::class.java) { db.mergeCategories(CategoryKind.EXPENSE, sources, target) }
        }
        assertEquals(CategoryMergeRules(), db.categoryMergeRules.value)
    }
    @Test fun customCategoriesFromRecurringAndLimitsAreAvailableWithoutPastRecords() {
        db.insertSubscription(sub("Coffee")); db.setCategoryLimit("Lunch", 50.0)
        db.mergeCategories(CategoryKind.EXPENSE, setOf("Coffee", "Lunch"), "Food")
        assertEquals("Food", db.subscriptions.value.single().category)
        assertEquals(mapOf("Food" to 50.0), db.categoryLimits.value)
    }
    @Test fun legacyAndMalformedRulesAreSafeAndNeverRedirectSystemCategories() {
        assertEquals(CategoryMergeRules(), CategoryMergeRules.fromJson(null))
        val malformed = CategoryMergeRules.fromJson(JSONObject("""{"expense":{"Food":"Shopping","Shopping":"Food","Other":"Transfer"}}"""))
        assertEquals("Food", malformed.resolve("Food", CategoryKind.EXPENSE))
        assertEquals("Other", malformed.resolve("Other", CategoryKind.EXPENSE))
        assertTrue(isSystemCategory(" Transfer "))
        assertEquals("Transfer", CategoryMergeRules(mapOf("Transfer" to "Food")).resolve("Transfer", CategoryKind.EXPENSE))
    }
    @Test fun merchantSuggestionsFollowMergedCategoryForBuiltInAndLearnedRules() {
        context.getSharedPreferences("vibe_merchant_rules", Context.MODE_PRIVATE).edit().putString("local cafe", "Food").commit()
        db.mergeCategories(CategoryKind.EXPENSE, setOf("Food"), "Other")
        assertEquals("Other", MerchantRuleEngine.suggestCategory(context, "Starbucks"))
        assertEquals("Other", MerchantRuleEngine.suggestCategory(context, "Local Cafe"))
    }
    @Test fun oldLedgerWithoutRulesLoadsWithoutChangingExistingTransactions() {
        db.insertTransaction(tx("Food"))
        val file = File(context.filesDir, "vibefinance_data.json")
        val root = JSONObject(file.readText()); root.remove("categoryMergeRules"); file.writeText(root.toString())
        db.categoryMergeRules.value = CategoryMergeRules(mapOf("Food" to "Other"))
        db.initialize(context)
        assertEquals(CategoryMergeRules(), db.categoryMergeRules.value)
        assertEquals("Food", db.transactions.value.single().category)
    }
}
