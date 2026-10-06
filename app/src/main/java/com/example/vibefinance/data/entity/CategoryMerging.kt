package com.example.vibefinance.data.entity

import org.json.JSONObject

enum class CategoryKind { EXPENSE, INCOME }

/** Separate namespaces keep an income called Food independent of expense Food. */
data class CategoryMergeRules(
    val expense: Map<String, String> = emptyMap(),
    val income: Map<String, String> = emptyMap(),
) {
    fun resolve(category: String, kind: CategoryKind): String {
        if (isSystemCategory(category)) return category
        val rules = if (kind == CategoryKind.INCOME) income else expense
        var current = category.trim()
        val visited = mutableSetOf<String>()
        while (visited.add(current)) {
            val next = rules[current] ?: return current
            if (next.isBlank() || isSystemCategory(next)) return category.trim()
            current = next
        }
        return category.trim() // Ignore malformed cyclic rules instead of looping.
    }

    fun normalize(tx: TransactionEntity): TransactionEntity =
        if (tx.toAccountId != null || tx.isBalanceAdjustment) tx
        else tx.copy(category = resolve(tx.category, tx.categoryKind))

    fun normalize(sub: SubscriptionEntity): SubscriptionEntity =
        sub.copy(category = resolve(sub.category, sub.categoryKind))

    fun merging(kind: CategoryKind, sources: Set<String>, target: String): CategoryMergeRules {
        require(target.isNotBlank() && !isSystemCategory(target)) { "Invalid target category" }
        val destination = resolve(target, kind)
        val canonicalSources = sources.map { resolve(it, kind) }.toSet()
        require(canonicalSources.isNotEmpty() && destination !in canonicalSources) { "Select different source and target categories" }
        require(canonicalSources.none { it.isBlank() || isSystemCategory(it) }) { "Invalid source category" }
        val existing = if (kind == CategoryKind.INCOME) income else expense
        val merged = existing.mapValues { (_, value) ->
            val canonical = resolve(value, kind)
            if (canonical in canonicalSources) destination else canonical
        } + canonicalSources.associateWith { destination }
        return if (kind == CategoryKind.INCOME) copy(income = merged) else copy(expense = merged)
    }

    fun toJson(): JSONObject = JSONObject().apply {
        put("expense", JSONObject(expense)); put("income", JSONObject(income))
    }

    companion object {
        fun fromJson(json: JSONObject?): CategoryMergeRules {
            fun read(kind: String): Map<String, String> {
                val obj = json?.optJSONObject(kind) ?: return emptyMap()
                return obj.keys().asSequence().mapNotNull { key ->
                    (obj.opt(key) as? String)?.takeIf { it.isNotBlank() && !isSystemCategory(it) }
                        ?.let { key to it }
                }.toMap()
            }
            return CategoryMergeRules(read("expense"), read("income"))
        }
    }
}

val TransactionEntity.categoryKind: CategoryKind
    get() = if (amount < 0.0) CategoryKind.INCOME else CategoryKind.EXPENSE
val SubscriptionEntity.categoryKind: CategoryKind
    get() = if (amount < 0.0) CategoryKind.INCOME else CategoryKind.EXPENSE

fun isSystemCategory(category: String): Boolean =
    category.trim().equals("Transfer", true) || category.trim().equals("Balance Adjustment", true)

fun categoryChoices(
    kind: CategoryKind,
    transactions: List<TransactionEntity>,
    subscriptions: List<SubscriptionEntity> = emptyList(),
    limits: Map<String, Double> = emptyMap(),
    rules: CategoryMergeRules = CategoryMergeRules(),
): List<String> = buildList {
    addAll(if (kind == CategoryKind.INCOME) DefaultIncomeCategories else DefaultExpenseCategories)
    addAll(transactions.filter { it.toAccountId == null && !it.isBalanceAdjustment && it.categoryKind == kind }.map { it.category })
    addAll(subscriptions.filter { it.categoryKind == kind }.map { it.category })
    if (kind == CategoryKind.EXPENSE) addAll(limits.keys)
    addAll((if (kind == CategoryKind.INCOME) rules.income else rules.expense).values)
}.filter { it.isNotBlank() && !isSystemCategory(it) }.map { rules.resolve(it, kind) }.distinct()

data class CategoryMergePlan(
    val rules: CategoryMergeRules,
    val transactions: List<TransactionEntity>,
    val subscriptions: List<SubscriptionEntity>,
    val limits: Map<String, Double>,
    val transactionCount: Int,
    val subscriptionCount: Int,
)

/** Relabel records without replaying any account-balance or notification side effects. */
fun planCategoryMerge(
    kind: CategoryKind, sources: Set<String>, target: String,
    transactions: List<TransactionEntity>, subscriptions: List<SubscriptionEntity>,
    limits: Map<String, Double>, rules: CategoryMergeRules,
): CategoryMergePlan {
    val available = categoryChoices(kind, transactions, subscriptions, limits, rules)
    require(target in available && sources.all { it in available }) { "Category no longer exists" }
    val mergedRules = rules.merging(kind, sources, target)
    val updatedTransactions = transactions.map { if (it.categoryKind == kind) mergedRules.normalize(it) else it }
    val updatedSubscriptions = subscriptions.map { if (it.categoryKind == kind) mergedRules.normalize(it) else it }
    val updatedLimits = if (kind == CategoryKind.INCOME) limits else buildMap {
        for ((category, amount) in limits) {
            val canonical = mergedRules.resolve(category, CategoryKind.EXPENSE)
            val total = (get(canonical) ?: 0.0) + amount
            require(total.isFinite() && total >= 0.0) { "Invalid merged budget limit" }
            put(canonical, total)
        }
    }
    return CategoryMergePlan(mergedRules, updatedTransactions, updatedSubscriptions, updatedLimits,
        transactions.zip(updatedTransactions).count { (old, new) -> old.category != new.category },
        subscriptions.zip(updatedSubscriptions).count { (old, new) -> old.category != new.category })
}
