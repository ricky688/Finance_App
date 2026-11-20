package com.example.vibefinance.screenshot

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.data.entity.SubscriptionEntity
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.data.repository.DailyBudgetInfo
import com.example.vibefinance.theme.VibeFinanceTheme
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.history.HistoryScreen
import com.example.vibefinance.ui.main.AdaptiveMainFrame
import com.example.vibefinance.ui.main.TabItem

@Preview(name = "Phone", device = Devices.PHONE, showBackground = true)
@Preview(name = "Phone landscape", device = "spec:width=891dp,height=411dp", showBackground = true)
@Preview(name = "Foldable", device = Devices.FOLDABLE, showBackground = true)
@Preview(name = "Tablet", device = Devices.TABLET, showBackground = true)
@Preview(name = "Desktop", device = Devices.DESKTOP, showBackground = true)
annotation class FormFactorPreviews

/**
 * Screens hosting a Nav3 list-detail scene. Layoutlib's CompositionDataTree throws an NPE for
 * those scenes above ~700dp, so they are captured at phone/foldable sizes only; wider layouts are
 * checked on Waydroid with scrcpy flex-display.
 */
@Preview(name = "Phone", device = Devices.PHONE, showBackground = true)
@Preview(name = "Foldable", device = Devices.FOLDABLE, showBackground = true)
annotation class ListDetailPreviews

// Home, Accounts and Recurring wrap their content in entrance animations that start at alpha 0,
// so a static layoutlib frame is blank; they are verified on Waydroid instead.

/** Fixed fixture so references stay stable across runs. */
private object Fixture {
    private const val DAY = 86_400_000L
    private const val NOW = 1_763_510_400_000L // 2025-11-19 00:00 UTC

    val accounts = listOf(
        AccountEntity(id = 1, name = "Cash", type = AccountType.CASH, balance = 1_280.50, icon = "wallet"),
        AccountEntity(id = 2, name = "HSBC Savings", type = AccountType.BANK, balance = 48_210.75, icon = "bank"),
        AccountEntity(id = 3, name = "BOC Debit", type = AccountType.DEBIT, balance = 9_604.00, icon = "card"),
        AccountEntity(
            id = 4, name = "Mox Credit", type = AccountType.CC, balance = 3_420.30, icon = "card",
            creditLimit = 30_000.0, billingDate = 5, paymentDate = 25, cardLast4 = "4242"
        ),
        AccountEntity(id = 5, name = "Index Fund", type = AccountType.INVESTMENT, balance = 120_000.0, icon = "chart"),
    )

    val transactions = listOf(
        "Food" to 68.0, "Transport" to 12.5, "Shopping" to 420.0, "Food" to 135.0,
        "Entertainment" to 88.0, "Bills" to 560.0, "Food" to 42.0, "Transport" to 31.0,
    ).mapIndexed { i, (category, amount) ->
        TransactionEntity(
            id = i + 1L, amount = amount, category = category, timestamp = NOW - i * DAY / 2,
            accountId = (i % 4) + 1L, description = "$category #${i + 1}"
        )
    }

    val subscriptions = listOf(
        SubscriptionEntity(id = 1, name = "Netflix", amount = 93.0, category = "Entertainment", frequency = "Monthly", nextPaymentDate = NOW + 3 * DAY, accountId = 4),
        SubscriptionEntity(id = 2, name = "Spotify", amount = 68.0, category = "Entertainment", frequency = "Monthly", nextPaymentDate = NOW + 9 * DAY, accountId = 4),
        SubscriptionEntity(id = 3, name = "Gym", amount = 450.0, category = "Health", frequency = "Monthly", nextPaymentDate = NOW + 14 * DAY, accountId = 3),
    )

    val state = FinanceUiState(
        isLoading = false,
        accounts = accounts,
        transactions = transactions,
        subscriptions = subscriptions,
        budgetInfo = DailyBudgetInfo(
            totalMonthlyBudget = 12_000.0, totalSpentThisMonth = 1_356.5, monthlyRemaining = 10_643.5,
            dailyAllowance = 400.0, dailyRemaining = 264.0, daysLeft = 11,
            startDate = NOW - 19 * DAY, endDate = NOW + 11 * DAY
        ),
    )
}

@Composable
private fun PreviewFrame(content: @Composable () -> Unit) {
    VibeFinanceTheme(darkTheme = false, dynamicColorEnabled = false) {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) { content() }
    }
}

@PreviewTest
@FormFactorPreviews
@Composable
fun MainFrameScreenshot() = PreviewFrame {
    AdaptiveMainFrame(selectedTab = TabItem.HOME, onSelect = {}) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Content") }
    }
}



@PreviewTest
@ListDetailPreviews
@Composable
fun HistoryScreenshot() = PreviewFrame { HistoryScreen(state = Fixture.state, onIntent = {}) }

