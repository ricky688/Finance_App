package com.example.vibefinance

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.vibefinance.data.DataSeeder
import com.example.vibefinance.data.repository.AccountRepository
import com.example.vibefinance.data.repository.BudgetRepository
import com.example.vibefinance.data.repository.TransactionRepository
import com.example.vibefinance.service.DeadlineCheckWorker
import com.example.vibefinance.theme.VibeFinanceTheme
import com.example.vibefinance.theme.ThemeMode
import com.example.vibefinance.ui.FinanceViewModel
import com.example.vibefinance.ui.main.MainScreen
import java.util.concurrent.TimeUnit
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.LocalActivityResultRegistryOwner
import com.example.vibefinance.ui.AppLanguage
import java.util.Locale

class MainActivity : ComponentActivity() {
    
    // Clean manual dependency injection bypassing local annotation processor issues
    private val viewModel: FinanceViewModel by lazy {
        val accountRepository = AccountRepository()
        val transactionRepository = TransactionRepository()
        val budgetRepository = BudgetRepository()
        val subscriptionRepository = com.example.vibefinance.data.repository.SubscriptionRepository(transactionRepository)
        val dataSeeder = DataSeeder(
            accountRepository = accountRepository,
            transactionRepository = transactionRepository,
            budgetRepository = budgetRepository,
            subscriptionRepository = subscriptionRepository
        )
        FinanceViewModel(
            accountRepository = accountRepository,
            transactionRepository = transactionRepository,
            budgetRepository = budgetRepository,
            subscriptionRepository = subscriptionRepository,
            dataSeeder = dataSeeder,
            application = application
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.vibefinance.data.InMemoryDatabase.initialize(this)

        // Request POST_NOTIFICATIONS runtime permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // Schedule CC payment deadlines background checks every 24 hours
        try {
            val checkRequest = PeriodicWorkRequestBuilder<DeadlineCheckWorker>(24, TimeUnit.HOURS)
                .build()
            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                "cc_deadline_check_work",
                ExistingPeriodicWorkPolicy.KEEP,
                checkRequest
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        handleIntent(intent)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val isDark = when (state.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            val context = LocalContext.current
            val deviceConfiguration = LocalConfiguration.current
            val systemLocales = android.content.res.Resources.getSystem().configuration.locales
            val currentLocale = remember(state.appLanguage, systemLocales, deviceConfiguration) {
                com.example.vibefinance.util.resolveAppLocale(state.appLanguage)
            }

            val baseConfig = LocalConfiguration.current
            val localizedConfig = remember(state.appLanguage, baseConfig, currentLocale) {
                android.content.res.Configuration(baseConfig).apply {
                    setLocale(currentLocale)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        setLocales(android.os.LocaleList(currentLocale, Locale.ENGLISH))
                    }
                    setLayoutDirection(currentLocale)
                }
            }

            val localizedContext = remember(context, localizedConfig) {
                object : android.content.ContextWrapper(this@MainActivity) {
                    private val configContext = this@MainActivity.createConfigurationContext(localizedConfig)
                    override fun getResources(): android.content.res.Resources = configContext.resources
                    override fun getAssets(): android.content.res.AssetManager = configContext.assets
                }
            }

            // Legacy formatters follow the chosen locale; System is resolved from Resources.getSystem().
            LaunchedEffect(currentLocale) {
                Locale.setDefault(currentLocale)
            }

            CompositionLocalProvider(
                LocalConfiguration provides localizedConfig,
                LocalContext provides localizedContext,
                androidx.compose.ui.platform.LocalResources provides localizedContext.resources,
                LocalActivityResultRegistryOwner provides this@MainActivity
            ) {
                androidx.compose.animation.Crossfade(
                    targetState = isDark,
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 450,
                        easing = androidx.compose.animation.core.FastOutSlowInEasing
                    ),
                    label = "screenThemeCrossfade"
                ) { targetIsDark ->
                    VibeFinanceTheme(
                        darkTheme = targetIsDark,
                        dynamicColorEnabled = state.dynamicColorEnabled,
                        appearancePalette = state.appearancePalette,
                        appearanceContrast = state.appearanceContrast,
                        pureBlackDarkMode = state.pureBlackDarkMode,
                        iconShape = state.iconShape.shape,
                        randomIconShapes = state.iconShape == com.example.vibefinance.theme.IconShapeMode.RANDOM
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            com.example.vibefinance.ui.components.MaterialYouAppLaunchOverlay {
                                MainScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: android.content.Intent?) {
        val targetTabStr = intent?.getStringExtra("navigate_tab")
        if (targetTabStr != null) {
            val targetTab = when (targetTabStr.lowercase(Locale.US)) {
                "history" -> com.example.vibefinance.ui.main.TabItem.HISTORY
                "recurring" -> com.example.vibefinance.ui.main.TabItem.RECURRING
                "assets", "accounts" -> com.example.vibefinance.ui.main.TabItem.ACCOUNTS
                "home", "daily" -> com.example.vibefinance.ui.main.TabItem.HOME
                else -> null
            }
            if (targetTab != null) {
                viewModel.requestTab(targetTab)
            }
        }

        val testTitle = intent?.getStringExtra("test_notif_title")
        val testText = intent?.getStringExtra("test_notif_text")
        val testPkg = intent?.getStringExtra("test_notif_pkg")
        if (!testTitle.isNullOrBlank() && !testText.isNullOrBlank() && !testPkg.isNullOrBlank()) {
            lifecycleScope.launch {
                com.example.vibefinance.service.PaymentNotificationListener.processNotificationForTest(
                    this@MainActivity,
                    testTitle,
                    testText,
                    testPkg
                )
            }
        }
    }
}
