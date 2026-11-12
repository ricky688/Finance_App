package com.example.vibefinance.util

import org.json.JSONArray
import org.json.JSONObject

data class MergeResult(
    val mergedJson: JSONObject,
    val localChanged: Boolean,
    val remoteChanged: Boolean
)

object SyncMergeEngine {

    /**
     * Merges local and remote database snapshots.
     * Unions supported collections while retaining local balances and unmerged metadata.
     */
    fun merge(local: JSONObject, remote: JSONObject): MergeResult {
        // Preserve local migration markers and fields not participating in the union below.
        val merged = JSONObject(local.toString())

        val localTx = local.optJSONArray("transactions") ?: JSONArray()
        val remoteTx = remote.optJSONArray("transactions") ?: JSONArray()
        val (mergedTx, txLocalChanged, txRemoteChanged) = mergeTransactions(localTx, remoteTx)
        merged.put("transactions", mergedTx)

        val localAcc = local.optJSONArray("accounts") ?: JSONArray()
        val remoteAcc = remote.optJSONArray("accounts") ?: JSONArray()
        val (mergedAcc, accLocalChanged, accRemoteChanged) = mergeAccounts(localAcc, remoteAcc)
        merged.put("accounts", mergedAcc)

        val localBudgets = local.optJSONArray("budgets") ?: JSONArray()
        val remoteBudgets = remote.optJSONArray("budgets") ?: JSONArray()
        val (mergedBudgets, budLocalChanged, budRemoteChanged) = mergeBudgets(localBudgets, remoteBudgets)
        merged.put("budgets", mergedBudgets)

        val localSubs = local.optJSONArray("subscriptions") ?: JSONArray()
        val remoteSubs = remote.optJSONArray("subscriptions") ?: JSONArray()
        val (mergedSubs, subsLocalChanged, subsRemoteChanged) = mergeSubscriptions(localSubs, remoteSubs)
        merged.put("subscriptions", mergedSubs)

        val localLimits = local.optJSONObject("categoryLimits") ?: JSONObject()
        val remoteLimits = remote.optJSONObject("categoryLimits") ?: JSONObject()
        val (mergedLimits, limLocalChanged, limRemoteChanged) = mergeLimits(localLimits, remoteLimits)
        merged.put("categoryLimits", mergedLimits)

        // Merge category rules (prefer non-empty)
        if (remote.has("categoryMergeRules") && !local.has("categoryMergeRules")) {
            merged.put("categoryMergeRules", remote.getJSONObject("categoryMergeRules"))
        } else if (local.has("categoryMergeRules")) {
            merged.put("categoryMergeRules", local.getJSONObject("categoryMergeRules"))
        }

        // Keep highest generator IDs
        val maxTxId = findMaxId(mergedTx)
        val maxAccId = findMaxId(mergedAcc)
        val maxSubId = findMaxId(mergedSubs)
        merged.put("nextTransactionId", maxOf(maxTxId + 1, local.optLong("nextTransactionId"), remote.optLong("nextTransactionId")))
        merged.put("nextAccountId", maxOf(maxAccId + 1, local.optLong("nextAccountId"), remote.optLong("nextAccountId")))
        merged.put("nextSubscriptionId", maxOf(maxSubId + 1, local.optLong("nextSubscriptionId"), remote.optLong("nextSubscriptionId")))

        val localChanged = txLocalChanged || accLocalChanged || budLocalChanged || subsLocalChanged || limLocalChanged
        val remoteChanged = txRemoteChanged || accRemoteChanged || budRemoteChanged || subsRemoteChanged || limRemoteChanged

        return MergeResult(
            mergedJson = merged,
            localChanged = localChanged,
            remoteChanged = remoteChanged
        )
    }

    private fun findMaxId(arr: JSONArray): Long {
        var max = 0L
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val id = obj.optLong("id", 0L)
            if (id > max) max = id
        }
        return max
    }

    private fun mergeTransactions(local: JSONArray, remote: JSONArray): Triple<JSONArray, Boolean, Boolean> {
        val mergedList = mutableListOf<JSONObject>()
        val seenSignatures = mutableSetOf<String>()
        var localChanged = false
        var remoteChanged = false

        fun signature(obj: JSONObject): String {
            val ts = obj.optLong("timestamp")
            val amt = obj.optDouble("amount")
            val acc = obj.optLong("accountId")
            val desc = obj.optString("description")
            return "${ts}_${amt}_${acc}_${desc}"
        }

        // Add all local transactions
        for (i in 0 until local.length()) {
            val item = local.getJSONObject(i)
            seenSignatures.add(signature(item))
            mergedList.add(item)
        }

        var nextAssignedId = findMaxId(local) + 1

        // Merge remote transactions
        for (i in 0 until remote.length()) {
            val item = remote.getJSONObject(i)
            val sig = signature(item)
            if (sig !in seenSignatures) {
                // New transaction from remote!
                seenSignatures.add(sig)
                // Check if ID collides with existing local ID
                val rawId = item.optLong("id", 0L)
                val idCollides = mergedList.any { it.optLong("id") == rawId }
                if (idCollides) {
                    val cloned = JSONObject(item.toString())
                    cloned.put("id", nextAssignedId++)
                    mergedList.add(cloned)
                } else {
                    mergedList.add(item)
                }
                localChanged = true
            }
        }

        // Check if there are local transactions that were not in remote
        val remoteSignatures = (0 until remote.length()).map { signature(remote.getJSONObject(it)) }.toSet()
        if (mergedList.any { signature(it) !in remoteSignatures }) {
            remoteChanged = true
        }

        // Sort by timestamp descending
        mergedList.sortByDescending { it.optLong("timestamp") }

        val resArray = JSONArray()
        mergedList.forEach { resArray.put(it) }
        return Triple(resArray, localChanged, remoteChanged)
    }

    private fun mergeAccounts(local: JSONArray, remote: JSONArray): Triple<JSONArray, Boolean, Boolean> {
        val mergedMap = mutableMapOf<Long, JSONObject>()
        var localChanged = false
        var remoteChanged = false

        for (i in 0 until local.length()) {
            val item = local.getJSONObject(i)
            val id = item.optLong("id")
            mergedMap[id] = item
        }

        for (i in 0 until remote.length()) {
            val item = remote.getJSONObject(i)
            val id = item.optLong("id")
            if (!mergedMap.containsKey(id)) {
                mergedMap[id] = item
                localChanged = true
            }
        }

        val remoteIds = (0 until remote.length()).map { remote.getJSONObject(it).optLong("id") }.toSet()
        if (mergedMap.keys.any { it !in remoteIds }) {
            remoteChanged = true
        }

        val res = JSONArray()
        mergedMap.values.forEach { res.put(it) }
        return Triple(res, localChanged, remoteChanged)
    }

    private fun mergeBudgets(local: JSONArray, remote: JSONArray): Triple<JSONArray, Boolean, Boolean> {
        val mergedMap = mutableMapOf<String, JSONObject>()
        var localChanged = false
        var remoteChanged = false

        for (i in 0 until local.length()) {
            val b = local.getJSONObject(i)
            mergedMap[b.optString("id")] = b
        }

        for (i in 0 until remote.length()) {
            val b = remote.getJSONObject(i)
            val id = b.optString("id")
            val existing = mergedMap[id]
            if (existing == null) {
                mergedMap[id] = b
                localChanged = true
            } else {
                // If remote budget has later start date, prefer remote
                val localStart = existing.optLong("startDate")
                val remoteStart = b.optLong("startDate")
                if (remoteStart > localStart) {
                    mergedMap[id] = b
                    localChanged = true
                }
            }
        }

        val remoteIds = (0 until remote.length()).map { remote.getJSONObject(it).optString("id") }.toSet()
        if (mergedMap.keys.any { it !in remoteIds }) {
            remoteChanged = true
        }

        val res = JSONArray()
        mergedMap.values.forEach { res.put(it) }
        return Triple(res, localChanged, remoteChanged)
    }

    private fun mergeSubscriptions(local: JSONArray, remote: JSONArray): Triple<JSONArray, Boolean, Boolean> {
        val mergedMap = mutableMapOf<Long, JSONObject>()
        var localChanged = false
        var remoteChanged = false

        for (i in 0 until local.length()) {
            val s = local.getJSONObject(i)
            mergedMap[s.optLong("id")] = s
        }

        for (i in 0 until remote.length()) {
            val s = remote.getJSONObject(i)
            val id = s.optLong("id")
            if (!mergedMap.containsKey(id)) {
                mergedMap[id] = s
                localChanged = true
            }
        }

        val remoteIds = (0 until remote.length()).map { remote.getJSONObject(it).optLong("id") }.toSet()
        if (mergedMap.keys.any { it !in remoteIds }) {
            remoteChanged = true
        }

        val res = JSONArray()
        mergedMap.values.forEach { res.put(it) }
        return Triple(res, localChanged, remoteChanged)
    }

    private fun mergeLimits(local: JSONObject, remote: JSONObject): Triple<JSONObject, Boolean, Boolean> {
        val merged = JSONObject()
        var localChanged = false
        var remoteChanged = false

        val localKeys = local.keys() ?: java.util.Collections.emptyIterator()
        while (localKeys.hasNext()) {
            val k = localKeys.next()
            merged.put(k, local.getDouble(k))
        }

        val remoteKeys = remote.keys() ?: java.util.Collections.emptyIterator()
        while (remoteKeys.hasNext()) {
            val k = remoteKeys.next()
            if (!merged.has(k)) {
                merged.put(k, remote.getDouble(k))
                localChanged = true
            }
        }

        val checkKeys = local.keys() ?: java.util.Collections.emptyIterator()
        while (checkKeys.hasNext()) {
            val k = checkKeys.next()
            if (!remote.has(k)) {
                remoteChanged = true
            }
        }

        return Triple(merged, localChanged, remoteChanged)
    }
}
