package com.example.vibefinance.data

import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.data.entity.BudgetEntity
import com.example.vibefinance.data.entity.DiscountShop
import com.example.vibefinance.data.entity.InterceptableApp
import com.example.vibefinance.data.entity.RolloverMode
import com.example.vibefinance.data.entity.ShopDiscountOffer
import com.example.vibefinance.data.entity.SubscriptionEntity
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.util.LocalAppManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import kotlin.math.roundToLong

object InMemoryDatabase {
    val accounts = MutableStateFlow<List<AccountEntity>>(emptyList())
    val transactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val budgets = MutableStateFlow<List<BudgetEntity>>(emptyList())
    val subscriptions = MutableStateFlow<List<SubscriptionEntity>>(emptyList())
    val discountShops = MutableStateFlow<List<DiscountShop>>(emptyList())
    val savedAspects = MutableStateFlow<List<String>>(
        listOf("Coffee & Cafe", "Supermarket", "Gas & Fuel", "Electronics", "Dining", "Retail")
    )
    
    var isNotificationLoggingEnabled = true
    val selectedInterceptApps = MutableStateFlow<Set<String>>(emptySet())

    private var nextAccountId = 1L
    private var nextTransactionId = 1L
    private var nextSubscriptionId = 1L
    private var nextDiscountShopId = 1L

    private var appContext: android.content.Context? = null
    private val diskIoLock = Any()
    private const val DATA_FILE_NAME = "vibefinance_data.json"

    fun saveAspect(aspect: String) {
        val trimmed = aspect.trim()
        if (trimmed.isNotBlank()) {
            savedAspects.update { current ->
                if (current.any { it.equals(trimmed, ignoreCase = true) }) current
                else current + trimmed
            }
            persistToDisk()
        }
    }

    val cashbackRules = MutableStateFlow<Map<String, Double>>(emptyMap())

    private var sharedPrefs: android.content.SharedPreferences? = null

    fun initialize(context: android.content.Context) {
        appContext = context.applicationContext
        sharedPrefs = context.getSharedPreferences("cashback_rules_prefs", android.content.Context.MODE_PRIVATE)
        isNotificationLoggingEnabled = sharedPrefs?.getBoolean("notification_logging_enabled", true) ?: true
        val rulesMap = mutableMapOf<String, Double>()
        sharedPrefs?.all?.forEach { (key, value) ->
            if (key == "selected_intercept_apps") {
                // Handled separately
            } else {
                val doubleVal = when (value) {
                    is String -> value.toDoubleOrNull()
                    is Float -> value.toDouble()
                    is Double -> value
                    is Int -> value.toDouble()
                    is Long -> value.toDouble()
                    else -> null
                }
                if (doubleVal != null) {
                    rulesMap[key] = doubleVal
                }
            }
        }
        cashbackRules.value = rulesMap

        val savedAppSet = sharedPrefs?.getStringSet("selected_intercept_apps", null)
        val initialSelection = savedAppSet ?: InterceptableApp.defaultEnabledApps()
        val installedPackages = LocalAppManager.getInstalledPackageNames(context)
        val selectedPackages = migrateInterceptAppSelections(initialSelection, installedPackages)
        selectedInterceptApps.value = selectedPackages
        if (savedAppSet != selectedPackages) {
            sharedPrefs?.edit()?.putStringSet("selected_intercept_apps", selectedPackages)?.apply()
        }

        loadFromDisk(context)
    }

    internal fun migrateInterceptAppSelections(
        selection: Set<String>,
        installedPackages: Set<String>
    ): Set<String> {
        val installedByName = installedPackages.associateBy { it.lowercase(Locale.US) }
        return selection.flatMapTo(linkedSetOf()) { selected ->
            val knownApp = InterceptableApp.fromId(selected)
            if (knownApp != null) {
                knownApp.packageKeywords.mapNotNull { keyword ->
                    installedByName[keyword.lowercase(Locale.US)]
                }
            } else {
                listOfNotNull(installedByName[selected.lowercase(Locale.US)])
            }
        }
    }

    fun toggleInterceptApp(appId: String) {
        selectedInterceptApps.update { current ->
            val updated = if (current.contains(appId)) {
                current - appId
            } else {
                current + appId
            }
            sharedPrefs?.edit()?.putStringSet("selected_intercept_apps", updated)?.apply()
            updated
        }
    }

    fun setInterceptApps(appIds: Set<String>) {
        selectedInterceptApps.value = appIds
        sharedPrefs?.edit()?.putStringSet("selected_intercept_apps", appIds)?.apply()
    }

    fun updateNotificationLoggingEnabled(enabled: Boolean) {
        isNotificationLoggingEnabled = enabled
        sharedPrefs?.edit()?.putBoolean("notification_logging_enabled", enabled)?.apply()
    }

    fun isAppInterceptEnabled(packageName: String, title: String, text: String): Boolean {
        if (!isNotificationLoggingEnabled) return false
        val enabledSet = selectedInterceptApps.value
        if (enabledSet.isEmpty()) return false
        if (enabledSet.none { packageName.equals(it, ignoreCase = true) }) return false

        // A selected chat app still needs to pass its payment-only notification rules.
        val knownApp = InterceptableApp.values().firstOrNull { app ->
            app.packageKeywords.any { keyword ->
                packageName.equals(keyword, ignoreCase = true) ||
                    packageName.startsWith("$keyword.", ignoreCase = true)
            }
        }
        return knownApp?.matches(packageName, title, text) ?: true
    }

    fun setCashbackRule(accountId: Long, category: String, rate: Double?) {
        val key = "${accountId}_$category"
        cashbackRules.update { current ->
            if (rate == null || rate <= 0.0) {
                sharedPrefs?.edit()?.remove(key)?.apply()
                current - key
            } else {
                sharedPrefs?.edit()?.putString(key, rate.toString())?.apply()
                current + (key to rate)
            }
        }
    }

    fun getCashbackRule(accountId: Long, category: String): Double {
        val catLower = category.lowercase(java.util.Locale.US)
        val matchedGroup = when {
            catLower.contains("food") || catLower.contains("drink") || catLower.contains("grocer") -> "Food"
            catLower.contains("transport") || catLower.contains("transit") || catLower.contains("fuel") || catLower.contains("mtr") -> "Transport"
            catLower.contains("shop") || catLower.contains("retail") || catLower.contains("online") -> "Shopping"
            catLower.contains("entertain") || catLower.contains("movie") || catLower.contains("game") -> "Entertainment"
            else -> "Others"
        }
        return cashbackRules.value["${accountId}_$matchedGroup"] ?: 0.0
    }

    fun clearAllTables() {
        // Account IDs are reused after a reset; never carry old notification routing into them.
        appContext?.let { com.example.vibefinance.service.PendingPaymentStore.clearForDataReplacement(it) }
        synchronized(diskIoLock) {
        accounts.value = emptyList()
        transactions.value = emptyList()
        budgets.value = emptyList()
        subscriptions.value = emptyList()
        discountShops.value = emptyList()
        cashbackRules.value = emptyMap()
        nextAccountId = 1L
        nextTransactionId = 1L
        nextSubscriptionId = 1L
        nextDiscountShopId = 1L
        persistToDisk()
        }
    }

    fun insertDiscountShop(shop: DiscountShop): Long {
        val id = if (shop.id == 0L) nextDiscountShopId++ else shop.id
        val newShop = shop.copy(id = id)
        discountShops.update { it + newShop }
        persistToDisk()
        return id
    }

    fun updateDiscountShop(shop: DiscountShop) {
        discountShops.update { list ->
            list.map { if (it.id == shop.id) shop else it }
        }
        persistToDisk()
    }

    fun deleteDiscountShop(shop: DiscountShop) {
        discountShops.update { list ->
            list.filter { it.id != shop.id }
        }
        persistToDisk()
    }

    fun insertSubscription(sub: SubscriptionEntity): Long {
        val id = nextSubscriptionId++
        val newSub = sub.copy(id = id)
        subscriptions.update { it + newSub }
        persistToDisk()
        return id
    }

    fun updateSubscription(sub: SubscriptionEntity) {
        subscriptions.update { list ->
            list.map { if (it.id == sub.id) sub else it }
        }
        persistToDisk()
    }

    fun deleteSubscription(sub: SubscriptionEntity) {
        subscriptions.update { list ->
            list.filter { it.id != sub.id }
        }
        persistToDisk()
    }

    fun insertAccount(account: AccountEntity): Long = synchronized(diskIoLock) {
        val id = nextAccountId++
        val newAccount = account.copy(id = id)
        accounts.update { it + newAccount }
        persistToDisk()
        id
    }

    fun insertAccounts(accounts: List<AccountEntity>): Map<String, Long> = synchronized(diskIoLock) {
        val created = mutableMapOf<String, Long>()
        val currentAccounts = this.accounts.value.toMutableList()
        val newAccountsToAdd = mutableListOf<AccountEntity>()

        for (acc in accounts) {
            val existing = currentAccounts.find { it.name.equals(acc.name.trim(), ignoreCase = true) }
            if (existing != null) {
                created[acc.name] = existing.id
            } else {
                val id = nextAccountId++
                val newAcc = acc.copy(id = id)
                currentAccounts.add(newAcc)
                newAccountsToAdd.add(newAcc)
                created[acc.name] = id
            }
        }
        if (newAccountsToAdd.isNotEmpty()) {
            this.accounts.update { it + newAccountsToAdd }
        }
        persistToDisk()
        created
    }

    fun updateAccount(account: AccountEntity) = synchronized(diskIoLock) {
        val previous = accounts.value.firstOrNull { it.id == account.id }
            ?: throw IllegalArgumentException("Account ${account.id} no longer exists")
        val updatedAccounts = accounts.value.map { if (it.id == account.id) account else it }
        val updatedTransactions = snapshotLegacyTypeBeforeEdit(previous, account)
        persistToDisk(
            accountsSnapshot = updatedAccounts,
            transactionsSnapshot = updatedTransactions,
            throwOnFailure = true
        )
        accounts.value = updatedAccounts
        transactions.value = updatedTransactions
    }

    /** Save account details and its balance correction together in one durable snapshot. */
    fun updateAccountWithBalanceAdjustment(account: AccountEntity): Long? = synchronized(diskIoLock) {
        val previous = accounts.value.firstOrNull { it.id == account.id }
            ?: throw IllegalArgumentException("Account ${account.id} no longer exists")
        require(account.balance.isFinite()) { "Balance must be a finite amount" }

        val previousCents = (previous.balance * 100.0).roundToLong()
        val requestedCents = (account.balance * 100.0).roundToLong()
        val deltaCents = requestedCents - previousCents
        val updatedAccount = account.copy(
            balance = if (deltaCents == 0L) previous.balance else requestedCents / 100.0
        )
        val adjustment = if (deltaCents != 0L) {
            val delta = deltaCents / 100.0
            TransactionEntity(
                id = nextTransactionId,
                amount = if (account.type == AccountType.CC) delta else -delta,
                category = "Balance Adjustment",
                timestamp = System.currentTimeMillis(),
                accountId = account.id,
                sourceWasCreditCard = account.type == AccountType.CC,
                isExcludedFromDailyBudget = true,
                isBalanceAdjustment = true,
                balanceAdjustmentDelta = delta
            )
        } else null

        val updatedAccounts = accounts.value.map { if (it.id == account.id) updatedAccount else it }
        val previousTransactions = snapshotLegacyTypeBeforeEdit(previous, account)
        val updatedTransactions = if (adjustment != null) previousTransactions + adjustment else previousTransactions
        val updatedNextTransactionId = nextTransactionId + if (adjustment != null) 1L else 0L
        persistToDisk(
            accountsSnapshot = updatedAccounts,
            transactionsSnapshot = updatedTransactions,
            nextTransactionIdSnapshot = updatedNextTransactionId,
            throwOnFailure = true
        )
        nextTransactionId = updatedNextTransactionId
        accounts.value = updatedAccounts
        transactions.value = updatedTransactions
        adjustment?.id
    }

    /** Old snapshots lack type fields; freeze their original sign before an account is reclassified. */
    private fun snapshotLegacyTypeBeforeEdit(
        previous: AccountEntity,
        updated: AccountEntity
    ): List<TransactionEntity> {
        if (previous.type == updated.type) return transactions.value
        val wasCreditCard = previous.type == AccountType.CC
        return transactions.value.map { transaction ->
            transaction.copy(
                sourceWasCreditCard = if (transaction.accountId == previous.id) {
                    transaction.sourceWasCreditCard ?: wasCreditCard
                } else transaction.sourceWasCreditCard,
                destinationWasCreditCard = if (transaction.toAccountId == previous.id) {
                    transaction.destinationWasCreditCard ?: wasCreditCard
                } else transaction.destinationWasCreditCard
            )
        }
    }

    /** Apply a delta against the latest balance, so concurrent writers cannot overwrite each other. */
    fun adjustAccountBalance(accountId: Long, delta: Double): Boolean = synchronized(diskIoLock) {
        require(delta.isFinite()) { "Balance delta must be finite" }
        val current = accounts.value.firstOrNull { it.id == accountId } ?: return@synchronized false
        val updatedBalance = current.balance + delta
        require(updatedBalance.isFinite()) { "Resulting balance must be finite" }
        val updatedAccounts = accounts.value.map { account ->
            if (account.id == accountId) account.copy(balance = updatedBalance) else account
        }
        persistToDisk(accountsSnapshot = updatedAccounts, throwOnFailure = true)
        accounts.value = updatedAccounts
        true
    }

    fun updateBalance(id: Long, newBalance: Double) = synchronized(diskIoLock) {
        accounts.update { list ->
            list.map { if (it.id == id) it.copy(balance = newBalance) else it }
        }
        persistToDisk()
    }

    fun batchUpdateBalances(deltas: Map<Long, Double>) = synchronized(diskIoLock) {
        accounts.update { list ->
            list.map { acc ->
                val delta = deltas[acc.id]
                if (delta != null) acc.copy(balance = acc.balance + delta) else acc
            }
        }
        persistToDisk()
    }

    fun deleteAccount(account: AccountEntity) {
        val deleted = synchronized(diskIoLock) {
            val currentAccounts = accounts.value.associateBy { it.id }
            if (account.id !in currentAccounts) return@synchronized false
            val linkedTransactions = transactions.value.filter {
                it.accountId == account.id || it.toAccountId == account.id
            }
            val survivingAccountDeltas = mutableMapOf<Long, Double>()
            val now = System.currentTimeMillis()
            linkedTransactions.filter { it.timestamp <= now && it.toAccountId != null }.forEach { tx ->
                val otherId = if (tx.accountId == account.id) tx.toAccountId!! else tx.accountId
                val other = currentAccounts[otherId] ?: return@forEach
                val reversal = if (tx.accountId == account.id) {
                    if (tx.destinationWasCreditCard ?: (other.type == AccountType.CC)) tx.amount else -tx.amount
                } else {
                    if (tx.sourceWasCreditCard ?: (other.type == AccountType.CC)) -tx.amount else tx.amount
                }
                survivingAccountDeltas[otherId] = (survivingAccountDeltas[otherId] ?: 0.0) + reversal
            }
            transactions.value = transactions.value.filterNot {
                it.accountId == account.id || it.toAccountId == account.id
            }
            accounts.value = accounts.value.filter { it.id != account.id }.map { existing ->
                existing.copy(balance = existing.balance + (survivingAccountDeltas[existing.id] ?: 0.0))
            }
            cashbackRules.value = cashbackRules.value.filterKeys { !it.startsWith("${account.id}_") }
            sharedPrefs?.let { prefs ->
                prefs.edit().apply {
                    prefs.all.keys.filter { it.startsWith("${account.id}_") }.forEach { remove(it) }
                }.apply()
            }
            persistToDisk()
            true
        }
        if (deleted) {
            appContext?.let { com.example.vibefinance.service.PendingPaymentStore.clearRememberedChoices(it) }
        }
    }

    fun insertTransaction(transaction: TransactionEntity): Long = synchronized(diskIoLock) {
        val id = nextTransactionId++
        val newTx = withAccountTypeSnapshot(transaction.copy(id = id))
        transactions.update { it + newTx }
        persistToDisk()
        id
    }

    /** Insert a notification expense and its account balance in the same persisted snapshot. */
    fun insertNotificationExpenseIfAbsent(transaction: TransactionEntity): Boolean = synchronized(diskIoLock) {
        val key = transaction.groupId
        require(key?.startsWith("notification:") == true) { "Missing notification identity" }
        require(transaction.amount.isFinite() && transaction.amount != 0.0) { "Invalid notification amount" }
        if (transactions.value.any { it.groupId == key }) return@synchronized false
        val account = accounts.value.firstOrNull { it.id == transaction.accountId }
            ?: return@synchronized false
        val id = nextTransactionId
        val balanceDelta = if (account.type == AccountType.CC) transaction.amount else -transaction.amount
        val updatedBalance = account.balance + balanceDelta
        require(updatedBalance.isFinite()) { "Resulting balance must be finite" }
        val updatedTransactions = transactions.value + withAccountTypeSnapshot(transaction.copy(id = id))
        val updatedAccounts = accounts.value.map { current ->
            if (current.id == account.id) current.copy(balance = current.balance + balanceDelta)
            else current
        }
        persistToDisk(
            accountsSnapshot = updatedAccounts,
            transactionsSnapshot = updatedTransactions,
            nextTransactionIdSnapshot = id + 1L,
            throwOnFailure = true
        )
        nextTransactionId = id + 1L
        accounts.value = updatedAccounts
        transactions.value = updatedTransactions
        true
    }

    fun insertTransactions(transactions: List<TransactionEntity>) = synchronized(diskIoLock) {
        val newTransactions = transactions.map { tx ->
            val id = if (tx.id == 0L) nextTransactionId++ else tx.id
            if (id >= nextTransactionId) nextTransactionId = id + 1
            withAccountTypeSnapshot(tx.copy(id = id))
        }
        this.transactions.update { it + newTransactions }
        persistToDisk()
    }

    private fun withAccountTypeSnapshot(transaction: TransactionEntity): TransactionEntity {
        val currentAccounts = accounts.value.associateBy { it.id }
        return transaction.copy(
            sourceWasCreditCard = transaction.sourceWasCreditCard
                ?: (currentAccounts[transaction.accountId]?.type == AccountType.CC),
            destinationWasCreditCard = if (transaction.toAccountId == null) null else (
                transaction.destinationWasCreditCard
                    ?: (currentAccounts[transaction.toAccountId]?.type == AccountType.CC)
            )
        )
    }

    data class ImportTransactionDraft(
        val amount: Double,
        val category: String,
        val timestamp: Long,
        val sourceAccountName: String,
        val destinationAccountName: String?,
        val isTransfer: Boolean,
        val isIncome: Boolean,
        val description: String
    )

    /**
     * Validate and stage the entire import, write one AtomicFile snapshot, then publish it.
     * Existing data stays intact if validation, serialization, or disk writing fails.
     */
    fun importFinancialData(
        accountTemplates: List<AccountEntity>,
        drafts: List<ImportTransactionDraft>,
        replaceExisting: Boolean
    ): Int {
        val importedCount = synchronized(diskIoLock) {
        checkNotNull(appContext) { "Storage is not initialized" }
        require(accountTemplates.isNotEmpty() && drafts.isNotEmpty()) { "The file has no importable transactions" }

        var accountIdCursor = nextAccountId
        var transactionIdCursor = nextTransactionId
        val stagedAccounts = (if (replaceExisting) emptyList() else accounts.value).toMutableList()
        val stagedTransactions = (if (replaceExisting) emptyList() else transactions.value).toMutableList()
        val accountsByName = linkedMapOf<String, AccountEntity>()
        stagedAccounts.forEach { existing ->
            val key = existing.name.trim().lowercase(Locale.ROOT)
            require(key !in accountsByName) {
                "Multiple existing accounts are named ${existing.name}; rename one before merging"
            }
            accountsByName[key] = existing
        }

        accountTemplates.forEach { template ->
            val name = template.name.trim()
            require(name.isNotEmpty()) { "An imported account has no name" }
            val key = name.lowercase(Locale.ROOT)
            if (accountsByName[key] == null) {
                val newAccount = template.copy(id = accountIdCursor++, name = name, balance = 0.0)
                stagedAccounts += newAccount
                accountsByName[key] = newAccount
            }
        }

        val stagedById = stagedAccounts.associateBy { it.id }
        val balanceDeltas = mutableMapOf<Long, Double>()
        val now = System.currentTimeMillis()
        drafts.forEach { draft ->
            require(draft.amount.isFinite()) { "An imported amount is invalid" }
            val source = accountsByName[draft.sourceAccountName.trim().lowercase(Locale.ROOT)]
                ?: throw IllegalArgumentException("Unknown source account: ${draft.sourceAccountName}")
            val destination = if (draft.isTransfer) {
                val destinationName = draft.destinationAccountName?.trim().orEmpty()
                require(destinationName.isNotEmpty()) { "A transfer has no destination account" }
                accountsByName[destinationName.lowercase(Locale.ROOT)]
                    ?: throw IllegalArgumentException("Unknown transfer account: $destinationName")
            } else null

            val transaction = TransactionEntity(
                id = transactionIdCursor++,
                amount = draft.amount,
                category = draft.category,
                timestamp = draft.timestamp,
                accountId = source.id,
                toAccountId = destination?.id,
                sourceWasCreditCard = source.type == AccountType.CC,
                destinationWasCreditCard = destination?.let { it.type == AccountType.CC },
                isExcludedFromDailyBudget = draft.isTransfer || draft.isIncome,
                description = draft.description
            )
            stagedTransactions += transaction

            if (transaction.timestamp <= now) {
                val sourceDelta = if (source.type == AccountType.CC) transaction.amount else -transaction.amount
                balanceDeltas[source.id] = (balanceDeltas[source.id] ?: 0.0) + sourceDelta
                if (destination != null) {
                    val destinationType = stagedById.getValue(destination.id).type
                    val destinationDelta = if (destinationType == AccountType.CC) -transaction.amount else transaction.amount
                    balanceDeltas[destination.id] = (balanceDeltas[destination.id] ?: 0.0) + destinationDelta
                }
            }
        }

        val finalAccounts = stagedAccounts.map { account ->
            val balance = account.balance + (balanceDeltas[account.id] ?: 0.0)
            require(balance.isFinite()) { "Imported balance is invalid for ${account.name}" }
            account.copy(balance = balance)
        }
        persistToDisk(
            accountsSnapshot = finalAccounts,
            transactionsSnapshot = stagedTransactions,
            nextAccountIdSnapshot = accountIdCursor,
            nextTransactionIdSnapshot = transactionIdCursor,
            throwOnFailure = true
        )
        nextAccountId = accountIdCursor
        nextTransactionId = transactionIdCursor
        accounts.value = finalAccounts
        transactions.value = stagedTransactions
        drafts.size
        }
        if (replaceExisting) {
            // IDs keep increasing so a crash before preference cleanup cannot route an old
            // remembered notification to a different account with the same ID.
            appContext?.let { context ->
                runCatching {
                    com.example.vibefinance.service.PendingPaymentStore.clearForDataReplacement(context)
                }.onFailure { android.util.Log.e("InMemoryDatabase", "Failed to clear old notification choices", it) }
            }
        }
        return importedCount
    }

    fun clearTransactionsAndAccounts() {
        appContext?.let { com.example.vibefinance.service.PendingPaymentStore.clearForDataReplacement(it) }
        synchronized(diskIoLock) {
        accounts.value = emptyList()
        transactions.value = emptyList()
        nextAccountId = 1L
        nextTransactionId = 1L
        persistToDisk()
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) = synchronized(diskIoLock) {
        transactions.update { list ->
            list.filter { it.id != transaction.id }
        }
        persistToDisk()
    }

    fun updateTransaction(transaction: TransactionEntity) = synchronized(diskIoLock) {
        transactions.update { list ->
            list.map { if (it.id == transaction.id) transaction else it }
        }
        persistToDisk()
    }

    fun deleteTransactionsByGroupId(groupId: String) = synchronized(diskIoLock) {
        transactions.update { list ->
            list.filter { it.groupId != groupId }
        }
        persistToDisk()
    }

    fun insertBudget(budget: BudgetEntity) {
        budgets.update { list ->
            val existing = list.find { it.id == budget.id }
            if (existing != null) {
                list.map { if (it.id == budget.id) budget else it }
            } else {
                list + budget
            }
        }
        persistToDisk()
    }

    val categoryLimits = MutableStateFlow<Map<String, Double>>(emptyMap())

    fun setCategoryLimit(category: String, limit: Double?) {
        categoryLimits.update { current ->
            if (limit == null || limit <= 0.0) {
                current - category
            } else {
                current + (category to limit)
            }
        }
        persistToDisk()
    }

    private fun persistToDisk(
        accountsSnapshot: List<AccountEntity>? = null,
        transactionsSnapshot: List<TransactionEntity>? = null,
        nextAccountIdSnapshot: Long? = null,
        nextTransactionIdSnapshot: Long? = null,
        throwOnFailure: Boolean = false
    ) = synchronized(diskIoLock) {
        val ctx = appContext ?: if (throwOnFailure) {
            throw IllegalStateException("Storage is not initialized")
        } else return@synchronized
        try {
            val root = JSONObject()
            root.put("nextAccountId", nextAccountIdSnapshot ?: nextAccountId)
            root.put("nextTransactionId", nextTransactionIdSnapshot ?: nextTransactionId)
            root.put("nextSubscriptionId", nextSubscriptionId)
            root.put("nextDiscountShopId", nextDiscountShopId)
            root.put("creditCardSourceTransfersReconciled", true)

            // accounts
            val accountsArr = JSONArray()
            for (acc in accountsSnapshot ?: accounts.value) {
                val obj = JSONObject()
                obj.put("id", acc.id)
                obj.put("name", acc.name)
                acc.nickname?.let { obj.put("nickname", it) }
                obj.put("type", acc.type.name)
                obj.put("balance", acc.balance)
                obj.put("icon", acc.icon)
                acc.creditLimit?.let { obj.put("creditLimit", it) }
                acc.billingDate?.let { obj.put("billingDate", it) }
                acc.paymentDate?.let { obj.put("paymentDate", it) }
                acc.paymentDeadline?.let { obj.put("paymentDeadline", it) }
                acc.cardTheme?.let { obj.put("cardTheme", it) }
                acc.accentColorKey?.let { obj.put("accentColorKey", it) }
                acc.cardLast4?.let { obj.put("cardLast4", it) }
                acc.cardProtocol?.let { obj.put("cardProtocol", it) }
                acc.cardIssuer?.let { obj.put("cardIssuer", it) }
                acc.cardPattern?.let { obj.put("cardPattern", it) }
                acc.cardImageUri?.let { obj.put("cardImageUri", it) }
                acc.customImageAspectRatio?.let { obj.put("customImageAspectRatio", it) }
                obj.put("cardBgOffsetX", acc.cardBgOffsetX.toDouble())
                obj.put("cardBgOffsetY", acc.cardBgOffsetY.toDouble())
                obj.put("cardBgScale", acc.cardBgScale.toDouble())
                acc.minSpendThreshold?.let { obj.put("minSpendThreshold", it) }
                acc.linkedAppPackage?.let { obj.put("linkedAppPackage", it) }
                acc.notificationAliases?.let { obj.put("notificationAliases", it) }
                accountsArr.put(obj)
            }
            root.put("accounts", accountsArr)

            // transactions
            val txArr = JSONArray()
            for (tx in transactionsSnapshot ?: transactions.value) {
                val obj = JSONObject()
                obj.put("id", tx.id)
                obj.put("amount", tx.amount)
                obj.put("category", tx.category)
                obj.put("timestamp", tx.timestamp)
                obj.put("accountId", tx.accountId)
                tx.toAccountId?.let { obj.put("toAccountId", it) }
                tx.sourceWasCreditCard?.let { obj.put("sourceWasCreditCard", it) }
                tx.destinationWasCreditCard?.let { obj.put("destinationWasCreditCard", it) }
                obj.put("isExcludedFromDailyBudget", tx.isExcludedFromDailyBudget)
                obj.put("isBalanceAdjustment", tx.isBalanceAdjustment)
                tx.balanceAdjustmentDelta?.let { obj.put("balanceAdjustmentDelta", it) }
                obj.put("description", tx.description)
                tx.installmentNumber?.let { obj.put("installmentNumber", it) }
                tx.totalInstallments?.let { obj.put("totalInstallments", it) }
                tx.groupId?.let { obj.put("groupId", it) }
                txArr.put(obj)
            }
            root.put("transactions", txArr)

            // budgets
            val budgetsArr = JSONArray()
            for (b in budgets.value) {
                val obj = JSONObject()
                obj.put("id", b.id)
                obj.put("totalBudgetAmount", b.totalBudgetAmount)
                obj.put("startDate", b.startDate)
                obj.put("endDate", b.endDate)
                obj.put("rolloverMode", b.rolloverMode.name)
                budgetsArr.put(obj)
            }
            root.put("budgets", budgetsArr)

            // subscriptions
            val subsArr = JSONArray()
            for (s in subscriptions.value) {
                val obj = JSONObject()
                obj.put("id", s.id)
                obj.put("name", s.name)
                obj.put("amount", s.amount)
                obj.put("category", s.category)
                obj.put("frequency", s.frequency)
                obj.put("nextPaymentDate", s.nextPaymentDate)
                obj.put("accountId", s.accountId)
                subsArr.put(obj)
            }
            root.put("subscriptions", subsArr)

            // categoryLimits
            val catLimitsObj = JSONObject()
            for ((cat, limit) in categoryLimits.value) {
                catLimitsObj.put(cat, limit)
            }
            root.put("categoryLimits", catLimitsObj)

            // savedAspects
            val aspectsArr = JSONArray()
            for (asp in savedAspects.value) {
                aspectsArr.put(asp)
            }
            root.put("savedAspects", aspectsArr)

            // discountShops
            val shopsArr = JSONArray()
            for (shop in discountShops.value) {
                val obj = JSONObject()
                obj.put("id", shop.id)
                obj.put("name", shop.name)
                obj.put("aspect", shop.aspect)
                obj.put("latitude", shop.latitude)
                obj.put("longitude", shop.longitude)
                obj.put("address", shop.address)
                obj.put("isUserCreated", shop.isUserCreated)
                obj.put("iconCategory", shop.iconCategory)
                val offersArr = JSONArray()
                for (off in shop.offers) {
                    val o = JSONObject()
                    o.put("accountId", off.accountId)
                    o.put("discountRate", off.discountRate)
                    o.put("promoDescription", off.promoDescription)
                    off.validUntil?.let { o.put("validUntil", it) }
                    offersArr.put(o)
                }
                obj.put("offers", offersArr)
                shopsArr.put(obj)
            }
            root.put("discountShops", shopsArr)

            val jsonString = root.toString()
            val file = File(ctx.filesDir, DATA_FILE_NAME)
            val atomicFile = android.util.AtomicFile(file)
            var fos: FileOutputStream? = null
            try {
                fos = atomicFile.startWrite()
                fos.write(jsonString.toByteArray(Charsets.UTF_8))
                atomicFile.finishWrite(fos)
            } catch (e: Exception) {
                if (fos != null) {
                    atomicFile.failWrite(fos)
                }
                android.util.Log.e("InMemoryDatabase", "Failed to write data to disk", e)
                if (throwOnFailure) throw e
            }
        } catch (e: Exception) {
            android.util.Log.e("InMemoryDatabase", "Failed to serialize data for disk", e)
            if (throwOnFailure) throw e
        }
    }

    private fun loadFromDisk(context: android.content.Context) {
        val file = File(context.filesDir, DATA_FILE_NAME)
        if (!file.exists()) return
        try {
            val atomicFile = android.util.AtomicFile(file)
            val bytes = atomicFile.readFully()
            val root = JSONObject(String(bytes, Charsets.UTF_8))

            nextAccountId = root.optLong("nextAccountId", nextAccountId)
            nextTransactionId = root.optLong("nextTransactionId", nextTransactionId)
            nextSubscriptionId = root.optLong("nextSubscriptionId", nextSubscriptionId)
            nextDiscountShopId = root.optLong("nextDiscountShopId", nextDiscountShopId)

            // accounts
            if (root.has("accounts")) {
                val arr = root.getJSONArray("accounts")
                val list = mutableListOf<AccountEntity>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val typeName = obj.optString("type", AccountType.CASH.name)
                    val type = runCatching { AccountType.valueOf(typeName) }.getOrDefault(AccountType.CASH)
                    list.add(
                        AccountEntity(
                            id = obj.getLong("id"),
                            name = obj.getString("name"),
                            nickname = if (obj.has("nickname") && !obj.isNull("nickname"))
                                obj.optString("nickname").takeIf { it.isNotBlank() } else null,
                            type = type,
                            balance = obj.getDouble("balance"),
                            icon = obj.optString("icon", "wallet"),
                            creditLimit = if (obj.has("creditLimit") && !obj.isNull("creditLimit")) obj.getDouble("creditLimit") else null,
                            billingDate = if (obj.has("billingDate") && !obj.isNull("billingDate")) obj.getInt("billingDate") else null,
                            paymentDate = if (obj.has("paymentDate") && !obj.isNull("paymentDate")) obj.getInt("paymentDate") else null,
                            paymentDeadline = if (obj.has("paymentDeadline") && !obj.isNull("paymentDeadline")) obj.getInt("paymentDeadline") else null,
                            cardTheme = if (obj.has("cardTheme") && !obj.isNull("cardTheme")) obj.getString("cardTheme") else null,
                            accentColorKey = if (obj.has("accentColorKey") && !obj.isNull("accentColorKey"))
                                obj.optString("accentColorKey").takeIf { it.isNotBlank() } else null,
                            cardLast4 = if (obj.has("cardLast4") && !obj.isNull("cardLast4")) obj.getString("cardLast4") else null,
                            cardProtocol = if (obj.has("cardProtocol") && !obj.isNull("cardProtocol")) obj.getString("cardProtocol") else null,
                            cardIssuer = if (obj.has("cardIssuer") && !obj.isNull("cardIssuer")) obj.getString("cardIssuer") else null,
                            cardPattern = if (obj.has("cardPattern") && !obj.isNull("cardPattern")) obj.getString("cardPattern") else null,
                            cardImageUri = if (obj.has("cardImageUri") && !obj.isNull("cardImageUri")) obj.getString("cardImageUri") else null,
                            customImageAspectRatio = obj.optString("customImageAspectRatio", "CARD"),
                            cardBgOffsetX = if (obj.has("cardBgOffsetX") && !obj.isNull("cardBgOffsetX")) obj.getDouble("cardBgOffsetX").toFloat() else 0f,
                            cardBgOffsetY = if (obj.has("cardBgOffsetY") && !obj.isNull("cardBgOffsetY")) obj.getDouble("cardBgOffsetY").toFloat() else 0f,
                            cardBgScale = if (obj.has("cardBgScale") && !obj.isNull("cardBgScale")) obj.getDouble("cardBgScale").toFloat() else 1f,
                            minSpendThreshold = if (obj.has("minSpendThreshold") && !obj.isNull("minSpendThreshold")) obj.getDouble("minSpendThreshold") else null,
                            linkedAppPackage = if (obj.has("linkedAppPackage") && !obj.isNull("linkedAppPackage")) obj.getString("linkedAppPackage") else null,
                            notificationAliases = if (obj.has("notificationAliases") && !obj.isNull("notificationAliases"))
                                obj.optString("notificationAliases").takeIf { it.isNotBlank() } else null
                        )
                    )
                }
                accounts.value = list
                if (list.isNotEmpty()) {
                    val maxId = list.maxOf { it.id }
                    if (maxId >= nextAccountId) nextAccountId = maxId + 1
                }
            }

            // transactions
            if (root.has("transactions")) {
                val arr = root.getJSONArray("transactions")
                val list = mutableListOf<TransactionEntity>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        TransactionEntity(
                            id = obj.getLong("id"),
                            amount = obj.getDouble("amount"),
                            category = obj.getString("category"),
                            timestamp = obj.getLong("timestamp"),
                            accountId = obj.getLong("accountId"),
                            toAccountId = if (obj.has("toAccountId") && !obj.isNull("toAccountId")) obj.getLong("toAccountId") else null,
                            sourceWasCreditCard = if (obj.has("sourceWasCreditCard") && !obj.isNull("sourceWasCreditCard")) obj.getBoolean("sourceWasCreditCard") else null,
                            destinationWasCreditCard = if (obj.has("destinationWasCreditCard") && !obj.isNull("destinationWasCreditCard")) obj.getBoolean("destinationWasCreditCard") else null,
                            isExcludedFromDailyBudget = obj.optBoolean("isExcludedFromDailyBudget", false),
                            isBalanceAdjustment = obj.optBoolean("isBalanceAdjustment", false),
                            balanceAdjustmentDelta = if (obj.has("balanceAdjustmentDelta") && !obj.isNull("balanceAdjustmentDelta")) obj.getDouble("balanceAdjustmentDelta") else null,
                            description = obj.optString("description", ""),
                            installmentNumber = if (obj.has("installmentNumber") && !obj.isNull("installmentNumber")) obj.getInt("installmentNumber") else null,
                            totalInstallments = if (obj.has("totalInstallments") && !obj.isNull("totalInstallments")) obj.getInt("totalInstallments") else null,
                            groupId = if (obj.has("groupId") && !obj.isNull("groupId")) obj.getString("groupId") else null
                        )
                    )
                }
                transactions.value = list
                if (list.isNotEmpty()) {
                    val maxId = list.maxOf { it.id }
                    if (maxId >= nextTransactionId) nextTransactionId = maxId + 1
                }
            }

            // budgets
            if (root.has("budgets")) {
                val arr = root.getJSONArray("budgets")
                val list = mutableListOf<BudgetEntity>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val rolloverStr = obj.optString("rolloverMode", RolloverMode.DISTRIBUTE_EVENLY.name)
                    val rollover = runCatching { RolloverMode.valueOf(rolloverStr) }.getOrDefault(RolloverMode.DISTRIBUTE_EVENLY)
                    list.add(
                        BudgetEntity(
                            id = obj.getString("id"),
                            totalBudgetAmount = obj.getDouble("totalBudgetAmount"),
                            startDate = obj.getLong("startDate"),
                            endDate = obj.getLong("endDate"),
                            rolloverMode = rollover
                        )
                    )
                }
                budgets.value = list
            }

            // subscriptions
            if (root.has("subscriptions")) {
                val arr = root.getJSONArray("subscriptions")
                val list = mutableListOf<SubscriptionEntity>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        SubscriptionEntity(
                            id = obj.getLong("id"),
                            name = obj.getString("name"),
                            amount = obj.getDouble("amount"),
                            category = obj.getString("category"),
                            frequency = obj.optString("frequency", "Monthly"),
                            nextPaymentDate = obj.getLong("nextPaymentDate"),
                            accountId = obj.getLong("accountId")
                        )
                    )
                }
                subscriptions.value = list
                if (list.isNotEmpty()) {
                    val maxId = list.maxOf { it.id }
                    if (maxId >= nextSubscriptionId) nextSubscriptionId = maxId + 1
                }
            }

            // categoryLimits
            if (root.has("categoryLimits")) {
                val obj = root.getJSONObject("categoryLimits")
                val map = mutableMapOf<String, Double>()
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    map[k] = obj.getDouble(k)
                }
                categoryLimits.value = map
            }

            // savedAspects
            if (root.has("savedAspects")) {
                val arr = root.getJSONArray("savedAspects")
                val list = mutableListOf<String>()
                for (i in 0 until arr.length()) {
                    list.add(arr.getString(i))
                }
                if (list.isNotEmpty()) {
                    savedAspects.value = list
                }
            }

            // discountShops
            if (root.has("discountShops")) {
                val arr = root.getJSONArray("discountShops")
                val list = mutableListOf<DiscountShop>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val offersList = mutableListOf<ShopDiscountOffer>()
                    if (obj.has("offers")) {
                        val offArr = obj.getJSONArray("offers")
                        for (j in 0 until offArr.length()) {
                            val o = offArr.getJSONObject(j)
                            offersList.add(
                                ShopDiscountOffer(
                                    accountId = o.getLong("accountId"),
                                    discountRate = o.getDouble("discountRate"),
                                    promoDescription = o.optString("promoDescription", ""),
                                    validUntil = if (o.has("validUntil") && !o.isNull("validUntil")) o.getString("validUntil") else null
                                )
                            )
                        }
                    }
                    list.add(
                        DiscountShop(
                            id = obj.getLong("id"),
                            name = obj.getString("name"),
                            aspect = obj.getString("aspect"),
                            latitude = obj.getDouble("latitude"),
                            longitude = obj.getDouble("longitude"),
                            address = obj.optString("address", ""),
                            offers = offersList,
                            isUserCreated = obj.optBoolean("isUserCreated", false),
                            iconCategory = obj.optString("iconCategory", obj.optString("aspect", ""))
                        )
                    )
                }
                discountShops.value = list
                if (list.isNotEmpty()) {
                    val maxId = list.maxOf { it.id }
                    if (maxId >= nextDiscountShopId) nextDiscountShopId = maxId + 1
                }
            }

            if (!root.optBoolean("creditCardSourceTransfersReconciled", false)) {
                val sourceAccounts = accounts.value.associateBy { it.id }
                val corrections = transactions.value
                    .asSequence()
                    .filter { it.toAccountId != null && it.timestamp <= System.currentTimeMillis() }
                    .filter { sourceAccounts[it.accountId]?.type == AccountType.CC }
                    .groupBy { it.accountId }
                    .mapValues { (_, transfers) -> transfers.sumOf { 2.0 * it.amount } }
                if (corrections.isNotEmpty()) {
                    accounts.update { list ->
                        list.map { account ->
                            val correction = corrections[account.id]
                            if (correction != null) account.copy(balance = account.balance + correction) else account
                        }
                    }
                }
                // Writing the marker also makes this correction idempotent on future launches.
                persistToDisk()
            }
        } catch (e: Exception) {
            android.util.Log.e("InMemoryDatabase", "Failed to load data from disk", e)
        }
    }
}
