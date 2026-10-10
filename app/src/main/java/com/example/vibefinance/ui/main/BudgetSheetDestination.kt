package com.example.vibefinance.ui.main

/** One destination prevents a dismissed setup sheet from revealing another budget window. */
internal enum class BudgetSheetDestination {
    NONE, SETTINGS, EDIT_PERIOD, NEW_PERIOD;

    val showsSettings: Boolean get() = this == SETTINGS || this == EDIT_PERIOD

    fun dismissSettings(): BudgetSheetDestination = if (showsSettings) NONE else this

    fun dismissNewPeriod(): BudgetSheetDestination = if (this == NEW_PERIOD) NONE else this

    companion object {
        fun fromDaily(totalBudget: Double): BudgetSheetDestination =
            if (totalBudget > 0.0) EDIT_PERIOD else NEW_PERIOD
    }
}
