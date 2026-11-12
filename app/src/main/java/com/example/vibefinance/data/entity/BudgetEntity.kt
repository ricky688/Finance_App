package com.example.vibefinance.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RolloverMode {
    DISTRIBUTE_EVENLY, // Distribute past leftover/overspend evenly over remaining days
    ADD_TO_NEXT_DAY    // Add yesterday's leftover directly to today's daily budget
}

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val id: String = "ACTIVE_PERIOD", // Key for the active budgeting period
    val totalBudgetAmount: Double,
    val startDate: Long, // Start date of period in epoch milliseconds
    val endDate: Long,   // End date of period in epoch milliseconds
    val rolloverMode: RolloverMode = RolloverMode.DISTRIBUTE_EVENLY,
    val estimatedIncome: Double? = null, // Legacy manual value retained for backup compatibility; forecasts are derived.
    val categoryBudgetsJson: String = "{}" // JSON mapping ExpenseSymbol.name -> allocated budget amount
) {
    fun categoryBudgetsMap(): Map<String, Double> {
        if (categoryBudgetsJson.isBlank() || categoryBudgetsJson == "{}") return emptyMap()
        return try {
            val json = org.json.JSONObject(categoryBudgetsJson)
            val map = mutableMapOf<String, Double>()
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = json.optDouble(key, 0.0)
                if (value > 0.0) {
                    map[key] = value
                }
            }
            map
        } catch (_: Exception) {
            emptyMap()
        }
    }

    companion object {
        fun encodeCategoryBudgets(map: Map<String, Double>): String {
            if (map.isEmpty()) return "{}"
            val json = org.json.JSONObject()
            map.forEach { (key, value) ->
                if (value > 0.0) {
                    json.put(key, value)
                }
            }
            return json.toString()
        }
    }
}
