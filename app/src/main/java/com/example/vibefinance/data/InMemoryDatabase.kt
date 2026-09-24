package com.example.vibefinance.data

import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.BudgetEntity
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.data.entity.SubscriptionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

import com.example.vibefinance.data.entity.DiscountShop
import com.example.vibefinance.data.entity.InterceptableApp
import java.util.Locale

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
    val selectedInterceptApps = MutableStateFlow<Set<String>>(InterceptableApp.defaultEnabledApps())

    private var nextAccountId = 1L
    private var nextTransactionId = 1L
    private var nextSubscriptionId = 1L
    private var nextDiscountShopId = 1L

    fun saveAspect(aspect: String) {
        val trimmed = aspect.trim()
        if (trimmed.isNotBlank()) {
            savedAspects.update { current ->
                if (current.any { it.equals(trimmed, ignoreCase = true) }) current
                else current + trimmed
            }
        }
    }

    val cashbackRules = MutableStateFlow<Map<String, Double>>(emptyMap())

    private var sharedPrefs: android.content.SharedPreferences? = null

    fun initialize(context: android.content.Context) {
        sharedPrefs = context.getSharedPreferences("cashback_rules_prefs", android.content.Context.MODE_PRIVATE)
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
        if (savedAppSet != null && savedAppSet.isNotEmpty()) {
            selectedInterceptApps.value = savedAppSet
        } else {
            selectedInterceptApps.value = InterceptableApp.defaultEnabledApps()
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

    fun isAppInterceptEnabled(packageName: String, title: String, text: String): Boolean {
        if (!isNotificationLoggingEnabled) return false
        val enabledSet = selectedInterceptApps.value
        if (enabledSet.isEmpty()) return false

        for (appId in enabledSet) {
            val app = InterceptableApp.fromId(appId)
            if (app != null) {
                if (app.matches(packageName, title, text)) {
                    return true
                }
            } else {
                if (packageName.equals(appId, ignoreCase = true)) {
                    return true
                }
            }
        }
        return false
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
            catLower.contains("transport") || catLower.contains("taxi") || catLower.contains("bus") || catLower.contains("travel") || catLower.contains("car") || catLower.contains("subway") -> "Transport"
            catLower.contains("shop") || catLower.contains("buy") || catLower.contains("clothing") || catLower.contains("electronic") || catLower.contains("gadget") || catLower.contains("mall") -> "Shopping"
            catLower.contains("utility") || catLower.contains("bill") || catLower.contains("rent") || catLower.contains("phone") || catLower.contains("water") || catLower.contains("power") || catLower.contains("internet") -> "Utilities"
            else -> "Other"
        }
        return cashbackRules.value["${accountId}_$matchedGroup"] ?: 0.0
    }

    fun clearAllTables() {
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
    }

    fun insertDiscountShop(shop: DiscountShop): Long {
        val id = if (shop.id == 0L) nextDiscountShopId++ else shop.id
        val newShop = shop.copy(id = id)
        discountShops.update { it + newShop }
        return id
    }

    fun updateDiscountShop(shop: DiscountShop) {
        discountShops.update { list ->
            list.map { if (it.id == shop.id) shop else it }
        }
    }

    fun deleteDiscountShop(shop: DiscountShop) {
        discountShops.update { list ->
            list.filter { it.id != shop.id }
        }
    }

    fun insertSubscription(sub: SubscriptionEntity): Long {
        val id = nextSubscriptionId++
        val newSub = sub.copy(id = id)
        subscriptions.update { it + newSub }
        return id
    }

    fun updateSubscription(sub: SubscriptionEntity) {
        subscriptions.update { list ->
            list.map { if (it.id == sub.id) sub else it }
        }
    }

    fun deleteSubscription(sub: SubscriptionEntity) {
        subscriptions.update { list ->
            list.filter { it.id != sub.id }
        }
    }

    fun insertAccount(account: AccountEntity): Long {
        val id = nextAccountId++
        val newAccount = account.copy(id = id)
        accounts.update { it + newAccount }
        return id
    }

    fun updateAccount(account: AccountEntity) {
        accounts.update { list ->
            list.map { if (it.id == account.id) account else it }
        }
    }

    fun updateBalance(id: Long, newBalance: Double) {
        accounts.update { list ->
            list.map { if (it.id == id) it.copy(balance = newBalance) else it }
        }
    }

    fun deleteAccount(account: AccountEntity) {
        accounts.update { list ->
            list.filter { it.id != account.id }
        }
    }

    fun insertTransaction(transaction: TransactionEntity): Long {
        val id = nextTransactionId++
        val newTx = transaction.copy(id = id)
        transactions.update { it + newTx }
        return id
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        transactions.update { list ->
            list.filter { it.id != transaction.id }
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        transactions.update { list ->
            list.map { if (it.id == transaction.id) transaction else it }
        }
    }

    fun deleteTransactionsByGroupId(groupId: String) {
        transactions.update { list ->
            list.filter { it.groupId != groupId }
        }
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
    }
}
