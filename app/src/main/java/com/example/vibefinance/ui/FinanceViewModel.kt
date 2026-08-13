package com.example.vibefinance.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibefinance.data.DataSeeder
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.BudgetEntity
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.data.repository.AccountRepository
import com.example.vibefinance.data.repository.BudgetRepository
import com.example.vibefinance.data.repository.DailyBudgetInfo
import com.example.vibefinance.data.repository.TransactionRepository
import com.example.vibefinance.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.app.Application
import com.example.vibefinance.data.entity.SubscriptionEntity
import com.example.vibefinance.data.repository.SubscriptionRepository
import java.time.YearMonth
import javax.inject.Inject

import com.example.vibefinance.data.entity.DiscountShop

// MVI State
data class FinanceUiState(
    val isLoading: Boolean = true,
    val accounts: List<AccountEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val budgetInfo: DailyBudgetInfo? = null,
    val activeMonth: YearMonth = YearMonth.now(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColorEnabled: Boolean = true,
    val subscriptions: List<SubscriptionEntity> = emptyList(),
    val categoryLimits: Map<String, Double> = emptyMap(),
    val discountShops: List<DiscountShop> = emptyList(),
    val error: String? = null
)

// MVI Intents
sealed interface FinanceIntent {
    data object SeedMockData : FinanceIntent
    data class SetCustomPeriodBudget(
        val amount: Double,
        val startDate: Long,
        val endDate: Long,
        val rolloverMode: com.example.vibefinance.data.entity.RolloverMode = com.example.vibefinance.data.entity.RolloverMode.DISTRIBUTE_EVENLY
    ) : FinanceIntent
    data class AddTransaction(
        val amount: Double,
        val category: String,
        val accountId: Long,
        val description: String,
        val toAccountId: Long? = null,
        val isExcludedFromDailyBudget: Boolean = false
    ) : FinanceIntent
    data class AddInstallmentTransaction(
        val amount: Double,
        val category: String,
        val accountId: Long,
        val description: String,
        val installments: Int
    ) : FinanceIntent
    data class DeleteTransaction(val tx: TransactionEntity) : FinanceIntent
    data class EditTransaction(val oldTx: TransactionEntity, val newTx: TransactionEntity) : FinanceIntent
    data class RestoreTransaction(val tx: TransactionEntity) : FinanceIntent
    data class SaveAccount(val account: AccountEntity, val cashbackRates: Map<String, Double> = emptyMap()) : FinanceIntent
    data class DeleteAccount(val account: AccountEntity) : FinanceIntent
    data class SetThemeMode(val themeMode: ThemeMode) : FinanceIntent
    data class SetDynamicColorEnabled(val enabled: Boolean) : FinanceIntent
    data class SaveSubscription(val sub: SubscriptionEntity) : FinanceIntent
    data class DeleteSubscription(val sub: SubscriptionEntity) : FinanceIntent
    data class SetCategoryLimit(val category: String, val limit: Double?) : FinanceIntent
    data class SaveDiscountShop(val shop: DiscountShop) : FinanceIntent
    data class DeleteDiscountShop(val shop: DiscountShop) : FinanceIntent
}

// MVI Single-use Events (for toasts/actions)
sealed interface FinanceUiEvent {
    data class ShowToast(val message: String) : FinanceUiEvent
    data class ShowSnackbar(val message: String, val actionLabel: String? = null, val actionIntent: FinanceIntent? = null) : FinanceUiEvent
    data object SeedCompleted : FinanceUiEvent
}

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val dataSeeder: DataSeeder,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinanceUiState())
    val uiState: StateFlow<FinanceUiState> = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<FinanceUiEvent>()
    val uiEvents: SharedFlow<FinanceUiEvent> = _uiEvents.asSharedFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                // Proactively auto-seed if there are no accounts in the database
                val existingAccounts = accountRepository.allAccountsFlow.first()
                if (existingAccounts.isEmpty()) {
                    dataSeeder.seedDatabase()
                    _uiEvents.emit(FinanceUiEvent.ShowToast("Database auto-seeded with custom period budget!"))
                }

                // Reactive subscription to accounts
                viewModelScope.launch {
                    accountRepository.allAccountsFlow.collect { accountsList ->
                        _uiState.update { it.copy(accounts = accountsList, isLoading = false) }
                    }
                }

                // Reactive subscription to transactions
                viewModelScope.launch {
                    transactionRepository.allTransactionsFlow.collect { transactionsList ->
                        _uiState.update { it.copy(transactions = transactionsList) }
                    }
                }

                // Reactive subscription to daily allowance math engine (custom period based)
                viewModelScope.launch {
                    budgetRepository.getDailyAllowanceFlow().collect { allowanceInfo ->
                        _uiState.update { it.copy(budgetInfo = allowanceInfo) }
                    }
                }

                // Reactive subscription to subscriptions list flow
                viewModelScope.launch {
                    subscriptionRepository.allSubscriptionsFlow.collect { subsList ->
                        _uiState.update { it.copy(subscriptions = subsList) }
                    }
                }

                // Reactive subscription to category limits list flow
                viewModelScope.launch {
                    com.example.vibefinance.data.InMemoryDatabase.categoryLimits.collect { limitsMap ->
                        _uiState.update { it.copy(categoryLimits = limitsMap) }
                    }
                }

                // Reactive subscription to discount shops list flow
                viewModelScope.launch {
                    com.example.vibefinance.data.InMemoryDatabase.discountShops.collect { shopsList ->
                        _uiState.update { it.copy(discountShops = shopsList) }
                    }
                }

                // Run auto-charge trigger
                viewModelScope.launch {
                    subscriptionRepository.checkAndTriggerAutoCharges(application)
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage, isLoading = false) }
            }
        }
    }

    fun dispatch(intent: FinanceIntent) {
        viewModelScope.launch {
            when (intent) {
                FinanceIntent.SeedMockData -> {
                    _uiState.update { it.copy(isLoading = true) }
                    try {
                        dataSeeder.seedDatabase()
                        _uiEvents.emit(FinanceUiEvent.SeedCompleted)
                        _uiEvents.emit(FinanceUiEvent.ShowToast("Database successfully re-seeded!"))
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.localizedMessage, isLoading = false) }
                    }
                }
                is FinanceIntent.SetCustomPeriodBudget -> {
                    try {
                        budgetRepository.insertBudget(
                            BudgetEntity(
                                id = "ACTIVE_PERIOD",
                                totalBudgetAmount = intent.amount,
                                startDate = intent.startDate,
                                endDate = intent.endDate,
                                rolloverMode = intent.rolloverMode
                            )
                        )
                        _uiEvents.emit(FinanceUiEvent.ShowToast("Custom period budget set successfully!"))
                    } catch (e: Exception) {
                        _uiEvents.emit(FinanceUiEvent.ShowToast("Error: ${e.localizedMessage}"))
                    }
                }
                is FinanceIntent.AddTransaction -> {
                    try {
                        val tx = TransactionEntity(
                            amount = intent.amount,
                            category = intent.category,
                            timestamp = System.currentTimeMillis(),
                            accountId = intent.accountId,
                            toAccountId = intent.toAccountId,
                            isExcludedFromDailyBudget = intent.isExcludedFromDailyBudget,
                            description = intent.description
                        )
                        transactionRepository.insertTransaction(tx)
                        
                        // Learn Merchant Categorization Rule
                        if (intent.description.isNotBlank()) {
                            com.example.vibefinance.ai.MerchantRuleEngine.learnRule(application, intent.description, intent.category)
                        }
                        
                        _uiEvents.emit(FinanceUiEvent.ShowToast("Transaction recorded!"))
                    } catch (e: Exception) {
                        _uiEvents.emit(FinanceUiEvent.ShowToast("Error: ${e.localizedMessage}"))
                    }
                }
                is FinanceIntent.AddInstallmentTransaction -> {
                    try {
                        val baseTx = TransactionEntity(
                            amount = intent.amount,
                            category = intent.category,
                            timestamp = System.currentTimeMillis(),
                            accountId = intent.accountId,
                            description = intent.description
                        )
                        transactionRepository.insertInstallmentTransaction(baseTx, intent.installments)
                        _uiEvents.emit(FinanceUiEvent.ShowToast("Installment plan created with ${intent.installments} entries!"))
                    } catch (e: Exception) {
                        _uiEvents.emit(FinanceUiEvent.ShowToast("Error: ${e.localizedMessage}"))
                    }
                }
                is FinanceIntent.DeleteTransaction -> {
                    try {
                        transactionRepository.deleteTransaction(intent.tx)
                        _uiEvents.emit(FinanceUiEvent.ShowSnackbar("Transaction deleted", "Undo", FinanceIntent.RestoreTransaction(intent.tx)))
                    } catch (e: Exception) {
                        _uiEvents.emit(FinanceUiEvent.ShowToast("Error: ${e.localizedMessage}"))
                    }
                }
                is FinanceIntent.RestoreTransaction -> {
                    try {
                        transactionRepository.insertTransaction(intent.tx)
                        // Optionally emit a toast to confirm restoration
                        _uiEvents.emit(FinanceUiEvent.ShowToast("Transaction restored"))
                    } catch (e: Exception) {
                        _uiEvents.emit(FinanceUiEvent.ShowToast("Error: ${e.localizedMessage}"))
                    }
                }
                is FinanceIntent.EditTransaction -> {
                    try {
                        transactionRepository.updateTransaction(intent.newTx, intent.oldTx)
                        _uiEvents.emit(FinanceUiEvent.ShowToast("Transaction updated!"))
                    } catch (e: Exception) {
                        _uiEvents.emit(FinanceUiEvent.ShowToast("Error: ${e.localizedMessage}"))
                    }
                }
                is FinanceIntent.SaveAccount -> {
                    try {
                        val savedId = if (intent.account.id == 0L) {
                            accountRepository.insertAccount(intent.account)
                        } else {
                            accountRepository.updateAccount(intent.account)
                            intent.account.id
                        }

                        intent.cashbackRates.forEach { (category, rate) ->
                            com.example.vibefinance.data.InMemoryDatabase.setCashbackRule(savedId, category, rate)
                        }

                        if (intent.account.id == 0L) {
                            _uiEvents.emit(FinanceUiEvent.ShowToast("Account created successfully!"))
                        } else {
                            _uiEvents.emit(FinanceUiEvent.ShowToast("Account updated successfully!"))
                        }
                    } catch (e: Exception) {
                        _uiEvents.emit(FinanceUiEvent.ShowToast("Error: ${e.localizedMessage}"))
                    }
                }
                is FinanceIntent.DeleteAccount -> {
                    try {
                        accountRepository.deleteAccount(intent.account)
                        _uiEvents.emit(FinanceUiEvent.ShowToast("Account deleted!"))
                    } catch (e: Exception) {
                        _uiEvents.emit(FinanceUiEvent.ShowToast("Error: ${e.localizedMessage}"))
                    }
                }
                is FinanceIntent.SetThemeMode -> {
                    _uiState.update { it.copy(themeMode = intent.themeMode) }
                    val message = when (intent.themeMode) {
                        ThemeMode.SYSTEM -> "Theme: System Default"
                        ThemeMode.LIGHT -> "Theme: Light Mode"
                        ThemeMode.DARK -> "Theme: Dark Mode"
                    }
                    _uiEvents.emit(FinanceUiEvent.ShowToast(message))
                }
                is FinanceIntent.SetDynamicColorEnabled -> {
                    _uiState.update { it.copy(dynamicColorEnabled = intent.enabled) }
                    val message = if (intent.enabled) "Dynamic Colors Enabled" else "Dynamic Colors Disabled"
                    _uiEvents.emit(FinanceUiEvent.ShowToast(message))
                }
                is FinanceIntent.SaveSubscription -> {
                    try {
                        if (intent.sub.id == 0L) {
                            subscriptionRepository.insertSubscription(intent.sub)
                            _uiEvents.emit(FinanceUiEvent.ShowToast("Subscription added!"))
                        } else {
                            subscriptionRepository.updateSubscription(intent.sub)
                            _uiEvents.emit(FinanceUiEvent.ShowToast("Subscription updated!"))
                        }
                        // Re-trigger charges checking immediately in case due date is in the past
                        subscriptionRepository.checkAndTriggerAutoCharges(application)
                    } catch (e: Exception) {
                        _uiEvents.emit(FinanceUiEvent.ShowToast("Error: ${e.localizedMessage}"))
                    }
                }
                is FinanceIntent.DeleteSubscription -> {
                    try {
                        subscriptionRepository.deleteSubscription(intent.sub)
                        _uiEvents.emit(FinanceUiEvent.ShowToast("Subscription deleted!"))
                    } catch (e: Exception) {
                        _uiEvents.emit(FinanceUiEvent.ShowToast("Error: ${e.localizedMessage}"))
                    }
                }
                is FinanceIntent.SetCategoryLimit -> {
                    try {
                        com.example.vibefinance.data.InMemoryDatabase.setCategoryLimit(intent.category, intent.limit)
                        val message = if (intent.limit == null || intent.limit <= 0.0) {
                            "Limit removed for ${intent.category}!"
                        } else {
                            "Limit set for ${intent.category}!"
                        }
                        _uiEvents.emit(FinanceUiEvent.ShowToast(message))
                    } catch (e: Exception) {
                        _uiEvents.emit(FinanceUiEvent.ShowToast("Error: ${e.localizedMessage}"))
                    }
                }
                is FinanceIntent.SaveDiscountShop -> {
                    try {
                        if (intent.shop.id == 0L) {
                            com.example.vibefinance.data.InMemoryDatabase.insertDiscountShop(intent.shop)
                        } else {
                            com.example.vibefinance.data.InMemoryDatabase.updateDiscountShop(intent.shop)
                        }
                        _uiEvents.emit(FinanceUiEvent.ShowToast("Discount shop saved!"))
                    } catch (e: Exception) {
                        _uiEvents.emit(FinanceUiEvent.ShowToast("Error: ${e.localizedMessage}"))
                    }
                }
                is FinanceIntent.DeleteDiscountShop -> {
                    try {
                        com.example.vibefinance.data.InMemoryDatabase.deleteDiscountShop(intent.shop)
                        _uiEvents.emit(FinanceUiEvent.ShowToast("Discount shop removed!"))
                    } catch (e: Exception) {
                        _uiEvents.emit(FinanceUiEvent.ShowToast("Error: ${e.localizedMessage}"))
                    }
                }
            }
        }
    }

    fun updateRolloverMode(mode: com.example.vibefinance.data.entity.RolloverMode) {
        val currentInfo = _uiState.value.budgetInfo ?: return
        dispatch(
            FinanceIntent.SetCustomPeriodBudget(
                amount = currentInfo.totalMonthlyBudget,
                startDate = currentInfo.startDate,
                endDate = currentInfo.endDate,
                rolloverMode = mode
            )
        )
    }
}
