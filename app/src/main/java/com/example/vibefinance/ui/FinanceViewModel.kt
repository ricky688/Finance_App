package com.example.vibefinance.ui

import com.example.vibefinance.R
import com.example.vibefinance.data.entity.CategoryKind
import com.example.vibefinance.data.entity.CategoryMergeRules
import com.example.vibefinance.util.appString
import com.example.vibefinance.util.resolveAppLocale
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
import com.example.vibefinance.theme.AppearancePalette
import com.example.vibefinance.theme.IconShapeMode
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
import java.util.Locale
import javax.inject.Inject

import com.example.vibefinance.data.entity.DiscountShop
import com.example.vibefinance.util.FinancialDataImportEngine
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class AppLanguage {
    SYSTEM,
    ENGLISH,
    TRADITIONAL_CHINESE
}

// MVI State
data class FinanceUiState(
    val isLoading: Boolean = true,
    val accounts: List<AccountEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val budgetInfo: DailyBudgetInfo? = null,
    val activeMonth: YearMonth = YearMonth.now(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColorEnabled: Boolean = true,
    val appearancePalette: AppearancePalette = AppearancePalette.ORIGINAL,
    val appearanceContrast: Int = 0,
    val pureBlackDarkMode: Boolean = false,
    val launchAnimationEnabled: Boolean = true,
    val iconShape: IconShapeMode = IconShapeMode.COOKIE_4,
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,
    val subscriptions: List<SubscriptionEntity> = emptyList(),
    val categoryLimits: Map<String, Double> = emptyMap(),
    val categoryMergeRules: CategoryMergeRules = CategoryMergeRules(),
    val isMergingCategories: Boolean = false,
    val discountShops: List<DiscountShop> = emptyList(),
    val importPreview: FinancialDataImportEngine.ImportDataPreview? = null,
    val isImporting: Boolean = false,
    val error: String? = null
)

// MVI Intents
sealed interface FinanceIntent {
    data object SeedMockData : FinanceIntent
    data class SetCustomPeriodBudget(
        val amount: Double,
        val startDate: Long,
        val endDate: Long,
        val rolloverMode: com.example.vibefinance.data.entity.RolloverMode = com.example.vibefinance.data.entity.RolloverMode.DISTRIBUTE_EVENLY,
        val showToast: Boolean = false
    ) : FinanceIntent
    data class AddTransaction(
        val amount: Double,
        val category: String,
        val accountId: Long,
        val description: String,
        val toAccountId: Long? = null,
        val isExcludedFromDailyBudget: Boolean = false,
        val customIcon: String? = null
    ) : FinanceIntent
    data class AddInstallmentTransaction(
        val amount: Double,
        val category: String,
        val accountId: Long,
        val description: String,
        val installments: Int,
        val firstDueDate: Long = System.currentTimeMillis(),
        val customIcon: String? = null
    ) : FinanceIntent
    data class DeleteInstallmentGroup(val groupId: String, val accountId: Long) : FinanceIntent
    data class DeleteTransaction(val tx: TransactionEntity) : FinanceIntent
    data class EditTransaction(val oldTx: TransactionEntity, val newTx: TransactionEntity) : FinanceIntent
    data class RestoreTransaction(val tx: TransactionEntity) : FinanceIntent
    data class SaveAccount(
        val account: AccountEntity,
        val originalBalance: Double? = null
    ) : FinanceIntent
    data class DeleteAccount(val account: AccountEntity) : FinanceIntent
    data class SetThemeMode(val themeMode: ThemeMode) : FinanceIntent
    data class SetDynamicColorEnabled(val enabled: Boolean) : FinanceIntent
    data class SetAppearancePalette(val palette: AppearancePalette) : FinanceIntent
    data class SetAppearanceContrast(val level: Int) : FinanceIntent
    data class SetPureBlackDarkMode(val enabled: Boolean) : FinanceIntent
    data class SetLaunchAnimationEnabled(val enabled: Boolean) : FinanceIntent
    data class SetIconShape(val shape: IconShapeMode) : FinanceIntent
    data class SetAppLanguage(val language: AppLanguage) : FinanceIntent
    data class SaveSubscription(val sub: SubscriptionEntity) : FinanceIntent
    data class DeleteSubscription(val sub: SubscriptionEntity, val deletePastTransactions: Boolean = false) : FinanceIntent
    data class MergeCategories(val kind: CategoryKind, val sources: Set<String>, val target: String) : FinanceIntent
    data class SetCategoryLimit(val category: String, val limit: Double?) : FinanceIntent
    data class SaveDiscountShop(val shop: DiscountShop) : FinanceIntent
    data class DeleteDiscountShop(val shop: DiscountShop) : FinanceIntent
    data class AnalyzeImportFile(val uri: Uri, val filename: String) : FinanceIntent
    data class ConfirmImport(val replaceExisting: Boolean = false) : FinanceIntent
    data object DismissImportPreview : FinanceIntent
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

    // Read before the first composition so a disabled intro cannot flash during loading.
    private val _uiState = MutableStateFlow(FinanceUiState(
        launchAnimationEnabled = com.example.vibefinance.util.CoordinatedPreferences
            .get(application, "vibe_finance_prefs").getBoolean("launch_animation_enabled", true)
    ))
    val uiState: StateFlow<FinanceUiState> = _uiState.asStateFlow()

    private val _requestedTab = MutableStateFlow<com.example.vibefinance.ui.main.TabItem?>(null)
    val requestedTab: StateFlow<com.example.vibefinance.ui.main.TabItem?> = _requestedTab.asStateFlow()

    fun requestTab(tab: com.example.vibefinance.ui.main.TabItem) {
        _requestedTab.value = tab
    }

    fun clearRequestedTab() {
        _requestedTab.value = null
    }

    private val _uiEvents = MutableSharedFlow<FinanceUiEvent>()
    val uiEvents: SharedFlow<FinanceUiEvent> = _uiEvents.asSharedFlow()

    init {
        loadData()
    }

    fun refreshAppearanceAfterRestore() {
        // Restore saved preferences
        val prefs = com.example.vibefinance.util.CoordinatedPreferences.get(application, "vibe_finance_prefs")
        val savedLangName = prefs.getString("app_language", AppLanguage.SYSTEM.name) ?: AppLanguage.SYSTEM.name
        val savedLang = try { AppLanguage.valueOf(savedLangName) } catch (e: Exception) { AppLanguage.SYSTEM }

        val savedThemeName = prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        val savedTheme = try { ThemeMode.valueOf(savedThemeName) } catch (e: Exception) { ThemeMode.SYSTEM }

        val savedDynamicColor = prefs.getBoolean("dynamic_color_enabled", false) // default false or loaded
        val savedPalette = runCatching {
            AppearancePalette.valueOf(prefs.getString("appearance_palette", AppearancePalette.ORIGINAL.name)!!)
        }.getOrDefault(AppearancePalette.ORIGINAL)
        val savedContrast = prefs.getInt("appearance_contrast", 0).coerceIn(-1, 1)
        val savedPureBlack = prefs.getBoolean("pure_black_dark_mode", false)
        val savedLaunchAnimation = prefs.getBoolean("launch_animation_enabled", true)
        val storedIconShape = prefs.getString("icon_shape", IconShapeMode.COOKIE_4.name)
        val savedIconShape = IconShapeMode.fromStoredName(storedIconShape)
        if (storedIconShape != savedIconShape.name) {
            prefs.edit().putString("icon_shape", savedIconShape.name).apply()
        }

        _uiState.update {
            it.copy(
                appLanguage = savedLang,
                themeMode = savedTheme,
                dynamicColorEnabled = savedDynamicColor,
                appearancePalette = savedPalette,
                appearanceContrast = savedContrast,
                pureBlackDarkMode = savedPureBlack,
                launchAnimationEnabled = savedLaunchAnimation,
                iconShape = savedIconShape
            )
        }

    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                refreshAppearanceAfterRestore()

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

                viewModelScope.launch {
                    com.example.vibefinance.data.InMemoryDatabase.categoryMergeRules.collect { rules ->
                        _uiState.update { it.copy(categoryMergeRules = rules) }
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
                        _uiState.update { it.copy(isLoading = false) }
                        _uiEvents.emit(FinanceUiEvent.SeedCompleted)
                        _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_database_reset)))
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
                        if (intent.showToast) {
                            _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_budget_set)))
                        }
                    } catch (e: Exception) {
                        _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_error, e.localizedMessage ?: e.javaClass.simpleName)))
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
                            description = intent.description,
                            customIcon = com.example.vibefinance.data.entity.ExpenseIcon.normalize(intent.customIcon)
                        )
                        transactionRepository.insertTransaction(tx)
                        
                        // Learn Merchant Categorization Rule
                        if (intent.description.isNotBlank()) {
                            com.example.vibefinance.ai.MerchantRuleEngine.learnRule(application, intent.description, intent.category)
                        }
                        
                        _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_transaction_recorded)))
                    } catch (e: Exception) {
                        _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_error, e.localizedMessage ?: e.javaClass.simpleName)))
                    }
                }
                is FinanceIntent.AddInstallmentTransaction -> {
                    try {
                        val baseTx = TransactionEntity(
                            amount = intent.amount,
                            category = intent.category,
                            timestamp = intent.firstDueDate,
                            accountId = intent.accountId,
                            description = intent.description,
                            customIcon = com.example.vibefinance.data.entity.ExpenseIcon.normalize(intent.customIcon)
                        )
                        transactionRepository.insertInstallmentTransaction(baseTx, intent.installments)
                        _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_installment_created, intent.installments)))
                    } catch (e: Exception) {
                        _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_error, e.localizedMessage ?: e.javaClass.simpleName)))
                    }
                }
                is FinanceIntent.DeleteInstallmentGroup -> {
                    try {
                        transactionRepository.deleteInstallmentGroup(intent.groupId, intent.accountId)
                        _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_installment_deleted)))
                    } catch (e: Exception) {
                        _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_error, e.localizedMessage ?: e.javaClass.simpleName)))
                    }
                }
                is FinanceIntent.DeleteTransaction -> {
                    try {
                        transactionRepository.deleteTransaction(intent.tx)
                        _uiEvents.emit(FinanceUiEvent.ShowSnackbar("Transaction deleted", "Undo", FinanceIntent.RestoreTransaction(intent.tx)))
                    } catch (e: Exception) {
                        _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_error, e.localizedMessage ?: e.javaClass.simpleName)))
                    }
                }
                is FinanceIntent.RestoreTransaction -> {
                    try {
                        transactionRepository.insertTransaction(intent.tx)
                        // Optionally emit a toast to confirm restoration
                        _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_transaction_restored)))
                    } catch (e: Exception) {
                        _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_error, e.localizedMessage ?: e.javaClass.simpleName)))
                    }
                }
                is FinanceIntent.EditTransaction -> {
                    try {
                        transactionRepository.updateTransaction(intent.newTx, intent.oldTx)
                        _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_transaction_updated)))
                    } catch (e: Exception) {
                        _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_error, e.localizedMessage ?: e.javaClass.simpleName)))
                    }
                }
                is FinanceIntent.SaveAccount -> {
                    try {
                        require(intent.account.balance.isFinite()) { "Balance must be a finite amount" }
                        if (intent.account.id == 0L) {
                            accountRepository.insertAccount(intent.account)
                        } else {
                            val current = accountRepository.getAccountByIdSync(intent.account.id)
                                ?: throw IllegalArgumentException("Account ${intent.account.id} no longer exists")
                            val accountToSave = if (intent.originalBalance != null &&
                                intent.account.balance == intent.originalBalance
                            ) {
                                intent.account.copy(balance = current.balance)
                            } else {
                                intent.account
                            }
                            accountRepository.updateAccountWithBalanceAdjustment(accountToSave)
                        }

                        if (intent.account.id == 0L) {
                            _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_account_created)))
                        } else {
                            _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_account_updated)))
                        }
                    } catch (e: Exception) {
                        _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_error, e.localizedMessage ?: e.javaClass.simpleName)))
                    }
                }
                is FinanceIntent.DeleteAccount -> {
                    try {
                        accountRepository.deleteAccount(intent.account)
                        _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_account_deleted)))
                    } catch (e: Exception) {
                        _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_error, e.localizedMessage ?: e.javaClass.simpleName)))
                    }
                }
                is FinanceIntent.SetThemeMode -> {
                    _uiState.update { it.copy(themeMode = intent.themeMode) }
                    val prefs = com.example.vibefinance.util.CoordinatedPreferences.get(application, "vibe_finance_prefs")
                    prefs.edit().putString("theme_mode", intent.themeMode.name).apply()
                    val message = when (intent.themeMode) {
                        ThemeMode.SYSTEM -> application.appString(R.string.feedback_theme_system)
                        ThemeMode.LIGHT -> application.appString(R.string.feedback_theme_light)
                        ThemeMode.DARK -> application.appString(R.string.feedback_theme_dark)
                    }
                    _uiEvents.emit(FinanceUiEvent.ShowToast(message))
                }
                is FinanceIntent.SetDynamicColorEnabled -> {
                    _uiState.update { it.copy(dynamicColorEnabled = intent.enabled) }
                    val prefs = com.example.vibefinance.util.CoordinatedPreferences.get(application, "vibe_finance_prefs")
                    prefs.edit().putBoolean("dynamic_color_enabled", intent.enabled).apply()
                    val message = if (intent.enabled) application.appString(R.string.feedback_dynamic_on) else application.appString(R.string.feedback_dynamic_off)
                    _uiEvents.emit(FinanceUiEvent.ShowToast(message))
                }
                is FinanceIntent.SetAppearancePalette -> {
                    _uiState.update { it.copy(appearancePalette = intent.palette, dynamicColorEnabled = false) }
                    com.example.vibefinance.util.CoordinatedPreferences.get(application, "vibe_finance_prefs")
                        .edit().putString("appearance_palette", intent.palette.name)
                        .putBoolean("dynamic_color_enabled", false).apply()
                }
                is FinanceIntent.SetAppearanceContrast -> {
                    val level = intent.level.coerceIn(-1, 1)
                    _uiState.update { it.copy(appearanceContrast = level, dynamicColorEnabled = false) }
                    com.example.vibefinance.util.CoordinatedPreferences.get(application, "vibe_finance_prefs")
                        .edit().putInt("appearance_contrast", level)
                        .putBoolean("dynamic_color_enabled", false).apply()
                }
                is FinanceIntent.SetPureBlackDarkMode -> {
                    _uiState.update { it.copy(pureBlackDarkMode = intent.enabled) }
                    com.example.vibefinance.util.CoordinatedPreferences.get(application, "vibe_finance_prefs")
                        .edit().putBoolean("pure_black_dark_mode", intent.enabled).apply()
                }
                is FinanceIntent.SetLaunchAnimationEnabled -> {
                    com.example.vibefinance.util.CoordinatedPreferences.get(application, "vibe_finance_prefs")
                        .edit().putBoolean("launch_animation_enabled", intent.enabled).apply()
                    _uiState.update { it.copy(launchAnimationEnabled = intent.enabled) }
                }
                is FinanceIntent.SetIconShape -> {
                    _uiState.update { it.copy(iconShape = intent.shape) }
                    com.example.vibefinance.util.CoordinatedPreferences.get(application, "vibe_finance_prefs")
                        .edit().putString("icon_shape", intent.shape.name).apply()
                    val message = application.appString(R.string.feedback_icon_shape, intent.shape.localizedTitle(resolveAppLocale(_uiState.value.appLanguage).language == "zh"))
                    _uiEvents.emit(FinanceUiEvent.ShowToast(message))
                }
                is FinanceIntent.SetAppLanguage -> {
                    _uiState.update { it.copy(appLanguage = intent.language) }
                    val prefs = com.example.vibefinance.util.CoordinatedPreferences.get(application, "vibe_finance_prefs")
                    prefs.edit().putString("app_language", intent.language.name).apply()
                    val message = when (intent.language) {
                        AppLanguage.SYSTEM -> application.appString(R.string.feedback_language_system)
                        AppLanguage.ENGLISH -> application.appString(R.string.feedback_language_english)
                        AppLanguage.TRADITIONAL_CHINESE -> application.appString(R.string.feedback_language_chinese)
                    }
                    _uiEvents.emit(FinanceUiEvent.ShowToast(message))
                }
                is FinanceIntent.SaveSubscription -> {
                    try {
                        if (intent.sub.id == 0L) {
                            subscriptionRepository.insertSubscription(intent.sub)
                            _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_subscription_added)))
                        } else {
                            subscriptionRepository.updateSubscription(intent.sub)
                            _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_subscription_updated)))
                        }
                        // Re-trigger charges checking immediately in case due date is in the past
                        subscriptionRepository.checkAndTriggerAutoCharges(application)
                    } catch (e: Exception) {
                        _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_error, e.localizedMessage ?: e.javaClass.simpleName)))
                    }
                }
                is FinanceIntent.DeleteSubscription -> {
                    try {
                        val pastTransactions = com.example.vibefinance.ui.recurring.findMatchingTransactionsForSubscription(
                            intent.sub,
                            _uiState.value.transactions
                        )

                        subscriptionRepository.deleteSubscription(intent.sub)

                        if (intent.deletePastTransactions && pastTransactions.isNotEmpty()) {
                            for (tx in pastTransactions) {
                                transactionRepository.deleteTransaction(tx)
                            }
                            val count = pastTransactions.size
                            val sum = pastTransactions.sumOf { it.amount }
                            val sumFormatted = String.format(Locale.getDefault(), "HK$ %,.2f", sum)
                            _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_subscription_payments_deleted, count, sumFormatted)))
                        } else {
                            if (pastTransactions.isNotEmpty()) {
                                _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_subscription_kept, pastTransactions.size)))
                            } else {
                                _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_subscription_deleted)))
                            }
                        }
                    } catch (e: Exception) {
                        _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_error, e.localizedMessage ?: e.javaClass.simpleName)))
                    }
                }
                is FinanceIntent.MergeCategories -> {
                    if (_uiState.value.isMergingCategories) return@launch
                    _uiState.update { it.copy(isMergingCategories = true) }
                    try {
                        withContext(Dispatchers.IO) {
                            com.example.vibefinance.data.InMemoryDatabase.mergeCategories(intent.kind, intent.sources, intent.target)
                        }
                        _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.category_merge_success)))
                    } catch (e: Exception) {
                        _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.category_merge_failed)))
                    } finally {
                        _uiState.update { it.copy(isMergingCategories = false) }
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
                        _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_error, e.localizedMessage ?: e.javaClass.simpleName)))
                    }
                }
                is FinanceIntent.SaveDiscountShop -> {
                    try {
                        if (intent.shop.id == 0L) {
                            com.example.vibefinance.data.InMemoryDatabase.insertDiscountShop(intent.shop)
                        } else {
                            com.example.vibefinance.data.InMemoryDatabase.updateDiscountShop(intent.shop)
                        }
                        _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_shop_saved)))
                    } catch (e: Exception) {
                        _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_error, e.localizedMessage ?: e.javaClass.simpleName)))
                    }
                }
                is FinanceIntent.DeleteDiscountShop -> {
                    try {
                        com.example.vibefinance.data.InMemoryDatabase.deleteDiscountShop(intent.shop)
                        _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_shop_removed)))
                    } catch (e: Exception) {
                        _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_error, e.localizedMessage ?: e.javaClass.simpleName)))
                    }
                }
                is FinanceIntent.AnalyzeImportFile -> {
                    _uiState.update { it.copy(isImporting = true) }
                    try {
                        val preview = withContext(Dispatchers.IO) {
                            FinancialDataImportEngine.parseUri(application, intent.uri)
                        }
                        _uiState.update { it.copy(importPreview = preview, isImporting = false) }
                        if (preview.error != null) {
                            _uiEvents.emit(FinanceUiEvent.ShowToast(preview.error))
                        }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(isImporting = false) }
                        _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_parse_failed, e.localizedMessage ?: e.javaClass.simpleName)))
                    }
                }
                is FinanceIntent.ConfirmImport -> {
                    if (_uiState.value.isImporting) return@launch
                    val preview = _uiState.value.importPreview ?: return@launch
                    _uiState.update { it.copy(isImporting = true) }
                    try {
                        val count = withContext(Dispatchers.IO) {
                            check(preview.error == null) { preview.error ?: "Invalid import preview" }
                            val accountEntities = preview.detectedAccounts.map { detected ->
                                AccountEntity(
                                    name = detected.name,
                                    type = detected.detectedType,
                                    balance = 0.0,
                                    icon = FinancialDataImportEngine.detectAccountIcon(detected.detectedType)
                                )
                            }
                            val drafts = preview.rawTransactions.map { raw ->
                                com.example.vibefinance.data.InMemoryDatabase.ImportTransactionDraft(
                                    amount = raw.amount,
                                    category = raw.category,
                                    timestamp = raw.timestamp,
                                    sourceAccountName = raw.sourceAccountName,
                                    destinationAccountName = raw.destinationAccountName,
                                    isTransfer = raw.isTransfer,
                                    isIncome = raw.isIncome,
                                    description = raw.description
                                )
                            }
                            com.example.vibefinance.data.InMemoryDatabase.importFinancialData(
                                accountTemplates = accountEntities,
                                drafts = drafts,
                                replaceExisting = intent.replaceExisting
                            )
                        }
                        val successMessage = if (intent.replaceExisting) application.appString(R.string.feedback_import_replaced, count) else application.appString(R.string.feedback_import_merged, count)
                        _uiState.update { it.copy(importPreview = null, isImporting = false) }
                        _uiEvents.emit(FinanceUiEvent.ShowToast(successMessage))
                    } catch (e: Exception) {
                        _uiState.update { it.copy(isImporting = false) }
                        _uiEvents.emit(FinanceUiEvent.ShowToast(application.appString(R.string.feedback_import_failed, e.localizedMessage ?: e.javaClass.simpleName)))
                    }
                }
                FinanceIntent.DismissImportPreview -> {
                    _uiState.update { it.copy(importPreview = null) }
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
