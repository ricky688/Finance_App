package com.example.vibefinance.ui

import androidx.lifecycle.ViewModelStore
import com.example.vibefinance.data.DataSeeder
import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.data.repository.*
import com.example.vibefinance.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HapticStrengthPreferenceTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private val prefs get() = context.getSharedPreferences("vibe_finance_prefs", 0)
    private val store = ViewModelStore()
    @Before fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        InMemoryDatabase.initialize(context)
        InMemoryDatabase.clearAllTables()
        prefs.edit().clear().commit()
    }
    @After fun cleanup() {
        store.clear()
        InMemoryDatabase.clearAllTables()
        prefs.edit().clear().commit()
        VibeHaptics.enabled = true
        VibeHaptics.strength = HapticStrength.STANDARD
        Dispatchers.resetMain()
    }
    private fun viewModel(): FinanceViewModel {
        val accounts = AccountRepository(); val transactions = TransactionRepository()
        val budgets = BudgetRepository(); val subscriptions = SubscriptionRepository(transactions)
        return FinanceViewModel(accounts, transactions, budgets, subscriptions,
            DataSeeder(accounts, transactions, budgets, subscriptions), context).also { store.put("fixture", it) }
    }
    @Test fun persistsAcrossReloadAndRetainsStrengthWhileDisabled() {
        val vm = viewModel()
        assertEquals(HapticStrength.STANDARD, vm.uiState.value.hapticStrength)
        vm.dispatch(FinanceIntent.SetHapticStrength(HapticStrength.STRONG))
        vm.dispatch(FinanceIntent.SetHapticFeedbackEnabled(false))
        assertEquals("STRONG", prefs.getString("haptic_strength", null))
        assertEquals(HapticStrength.STRONG, VibeHaptics.strength)
        assertFalse(VibeHaptics.enabled)
        val reopened = viewModel()
        assertEquals(HapticStrength.STRONG, reopened.uiState.value.hapticStrength)
        assertFalse(reopened.uiState.value.hapticFeedbackEnabled)
        prefs.edit().putString("haptic_strength", "GENTLE").commit()
        reopened.refreshAppearanceAfterRestore()
        assertEquals(HapticStrength.GENTLE, reopened.uiState.value.hapticStrength)
        assertEquals(HapticStrength.GENTLE, VibeHaptics.strength)
    }
    @Test fun backupAcceptsOlderPreferencesAndRejectsWrongTypesAndUnknownStrength() {
        val root = BackupStateCodec.preferences(context)
        BackupStateCodec.validatePreferences(root)
        val appearance = root.getJSONObject("vibe_finance_prefs")
        for (invalid in listOf(
            JSONObject().put("type", "string").put("value", "IMPOSSIBLE"),
            JSONObject().put("type", "int").put("value", 2))) {
            appearance.put("haptic_strength", invalid)
            assertThrows(IllegalArgumentException::class.java) { BackupStateCodec.validatePreferences(root) }
        }
        appearance.put("haptic_strength", JSONObject().put("type", "string").put("value", "GENTLE"))
        BackupStateCodec.validatePreferences(root)
        appearance.put("haptic_feedback_enabled", JSONObject().put("type", "string").put("value", "true"))
        assertThrows(IllegalArgumentException::class.java) { BackupStateCodec.validatePreferences(root) }
    }
}
