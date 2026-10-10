package com.example.vibefinance.ui.main

import org.junit.Assert.*
import org.junit.Test

class BudgetSheetDestinationTest {
    @Test fun missingBudgetStartsNewPeriod_andExistingBudgetOpensEditor() {
        assertEquals(BudgetSheetDestination.NEW_PERIOD, BudgetSheetDestination.fromDaily(0.0))
        assertEquals(BudgetSheetDestination.EDIT_PERIOD, BudgetSheetDestination.fromDaily(1500.0))
    }

    @Test fun setupNeverHasASettingsSheetUnderneath_andClosingReturnsToDaily() {
        val setup = BudgetSheetDestination.fromDaily(0.0)
        assertFalse(setup.showsSettings)
        val closed = setup.dismissNewPeriod()
        assertEquals(BudgetSheetDestination.NONE, closed)
        assertFalse(closed.showsSettings)
    }

    @Test fun delayedSettingsDismissalCannotDismissTheNewPeriodSheet() {
        assertEquals(BudgetSheetDestination.NEW_PERIOD, BudgetSheetDestination.NEW_PERIOD.dismissSettings())
        assertEquals(BudgetSheetDestination.NONE, BudgetSheetDestination.SETTINGS.dismissSettings())
        assertEquals(BudgetSheetDestination.NONE, BudgetSheetDestination.EDIT_PERIOD.dismissSettings())
    }

    @Test fun delayedNewPeriodDismissalCannotCloseAnEditorOrSettings() {
        assertEquals(BudgetSheetDestination.EDIT_PERIOD, BudgetSheetDestination.EDIT_PERIOD.dismissNewPeriod())
        assertEquals(BudgetSheetDestination.SETTINGS, BudgetSheetDestination.SETTINGS.dismissNewPeriod())
        assertEquals(BudgetSheetDestination.NONE, BudgetSheetDestination.NONE.dismissNewPeriod())
    }
}
