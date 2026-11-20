package com.example.vibefinance.wear

import android.content.Context
import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.data.entity.BudgetEntity
import com.example.vibefinance.data.entity.SubscriptionEntity
import com.example.vibefinance.data.repository.BudgetRepository
import com.example.vibefinance.service.PendingPaymentStore
import com.example.vibefinance.wear.contract.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File
import java.time.LocalDate
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WearCommandHandlerTest {
    private val context: Context
        get() = RuntimeEnvironment.getApplication()

    @Before
    fun before() {
        InMemoryDatabase.initialize(context)
        InMemoryDatabase.clearAllTables()
        context.getSharedPreferences("pending_payment_choices", 0).edit().clear().commit()
        PendingPaymentStore.reloadAfterFullRestore(context)
        WearCommandHandler.clearProcessedForTest(context)
        PhoneBudgetBridge.prefs(context).edit().clear().commit()
    }

    @After
    fun after() {
        File(context.filesDir, "vibefinance_data.json.new").deleteRecursively()
        InMemoryDatabase.clearAllTables()
        WearCommandHandler.clearProcessedForTest(context)
        PhoneBudgetBridge.prefs(context).edit().clear().commit()
    }

    private fun enableAllWearPrefs() {
        PhoneBudgetBridge.prefs(context).edit()
            .putBoolean(PhoneBudgetBridge.SHARE_KEY, true)
            .putBoolean(PhoneBudgetBridge.PENDING_DECISIONS_KEY, true)
            .putBoolean(PhoneBudgetBridge.RECENT_AND_QUICK_KEY, true)
            .putBoolean("hide_amounts", false)
            .putBoolean("app_lock_enabled", false)
            .commit()
    }

    @Test
    fun privacyAndAppLockBlockAllWearWrites() = runTest {
        val cash = AccountEntity(id = 1L, name = "Wallet (Cash)", type = AccountType.CASH, balance = 500.0, icon = "wallet")
        InMemoryDatabase.insertAccount(cash)

        // Default off -> blocked
        val cmd = WearCommand("req-priv-1", System.currentTimeMillis(), WearCommandAction.QUICK_ADD_EXPENSE, accountId = 1L, category = "Food & Drink", amount = 50.0)
        assertEquals(WearCommandStatus.PRIVACY_BLOCKED, WearCommandHandler.handle(context, cmd).status)

        // Enable sharing + recent, but turn on App Lock -> blocked
        enableAllWearPrefs()
        PhoneBudgetBridge.prefs(context).edit().putBoolean("app_lock_enabled", true).commit()
        assertEquals(WearCommandStatus.PRIVACY_BLOCKED, WearCommandHandler.handle(context, cmd).status)
        assertTrue(InMemoryDatabase.transactions.value.isEmpty())

        // Disable App Lock, turn on Hide Amounts -> blocked
        PhoneBudgetBridge.prefs(context).edit()
            .putBoolean("app_lock_enabled", false)
            .putBoolean("hide_amounts", true)
            .commit()
        assertEquals(WearCommandStatus.PRIVACY_BLOCKED, WearCommandHandler.handle(context, cmd).status)
        assertTrue(InMemoryDatabase.transactions.value.isEmpty())
    }

    @Test
    fun acceptPendingIsIdempotentAndBlocksCreditCardForIncome() = runTest {
        enableAllWearPrefs()
        val bankId = InMemoryDatabase.insertAccount(
            AccountEntity(name = "Mox", type = AccountType.BANK, balance = 1000.0, icon = "bank")
        )
        val ccId = InMemoryDatabase.insertAccount(
            AccountEntity(name = "MOX card", type = AccountType.CC, balance = 0.0, icon = "credit_card")
        )

        val queued = PendingPaymentStore.enqueue(
            context = context,
            sourcePackage = "com.mox.app",
            assetHint = "Mox Bank",
            merchant = "Salary Corp",
            amount = 300.0,
            detectedAt = System.currentTimeMillis(),
            transactionType = "INCOME",
            rawTitle = "收到款項",
            rawText = "Salary Corp 已向你付款 HKD300.00"
        )!!

        // Attempting to book income into CC must be rejected
        val badCmd = WearCommand("req-cc", System.currentTimeMillis(), WearCommandAction.ACCEPT_PENDING, paymentId = queued.id, accountId = ccId)
        assertEquals(WearCommandStatus.INVALID_REQUEST, WearCommandHandler.handle(context, badCmd).status)
        assertEquals(1, PendingPaymentStore.pending.value.size)

        // Confirming into bank account succeeds once
        val goodCmd = WearCommand("req-ok", System.currentTimeMillis(), WearCommandAction.ACCEPT_PENDING, paymentId = queued.id, accountId = bankId)
        assertEquals(WearCommandStatus.SUCCESS, WearCommandHandler.handle(context, goodCmd).status)
        assertEquals(0, PendingPaymentStore.pending.value.size)
        assertEquals(1, InMemoryDatabase.transactions.value.size)

        // Replaying the exact same requestId returns ALREADY_HANDLED and never duplicates
        assertEquals(WearCommandStatus.ALREADY_HANDLED, WearCommandHandler.handle(context, goodCmd).status)
        assertEquals(1, InMemoryDatabase.transactions.value.size)

        // Another requestId for the already-accepted payment also returns ALREADY_HANDLED
        val dupCmd = goodCmd.copy(requestId = "req-ok-2")
        assertEquals(WearCommandStatus.ALREADY_HANDLED, WearCommandHandler.handle(context, dupCmd).status)
        assertEquals(1, InMemoryDatabase.transactions.value.size)
    }

    @Test
    fun quickAddChangeCategoryAndUndoLifecycle() = runTest {
        enableAllWearPrefs()
        val cashId = InMemoryDatabase.insertAccount(
            AccountEntity(name = "Wallet (Cash)", type = AccountType.CASH, balance = 200.0, icon = "wallet")
        )

        val addCmd = WearCommand(
            requestId = "quick-1",
            issuedAt = System.currentTimeMillis(),
            action = WearCommandAction.QUICK_ADD_EXPENSE,
            accountId = cashId,
            category = "Food & Drink",
            amount = 42.50,
            note = "Ramen"
        )
        assertEquals(WearCommandStatus.SUCCESS, WearCommandHandler.handle(context, addCmd).status)
        assertEquals(1, InMemoryDatabase.transactions.value.size)
        assertEquals(157.50, InMemoryDatabase.accounts.value.first { it.id == cashId }.balance, 0.01)

        // Replay must not double-deduct
        assertEquals(WearCommandStatus.ALREADY_HANDLED, WearCommandHandler.handle(context, addCmd).status)
        assertEquals(1, InMemoryDatabase.transactions.value.size)
        assertEquals(157.50, InMemoryDatabase.accounts.value.first { it.id == cashId }.balance, 0.01)

        // Change category
        val catCmd = WearCommand(
            requestId = "cat-1",
            issuedAt = System.currentTimeMillis(),
            action = WearCommandAction.CHANGE_NOTIFICATION_CATEGORY,
            paymentId = "wear-quick-1",
            category = "Transport"
        )
        assertEquals(WearCommandStatus.SUCCESS, WearCommandHandler.handle(context, catCmd).status)
        assertEquals("Transport", InMemoryDatabase.transactions.value.first().category)

        // Undo restores balance and removes transaction
        val undoCmd = WearCommand(
            requestId = "undo-1",
            issuedAt = System.currentTimeMillis(),
            action = WearCommandAction.UNDO_NOTIFICATION_PAYMENT,
            paymentId = "wear-quick-1"
        )
        assertEquals(WearCommandStatus.SUCCESS, WearCommandHandler.handle(context, undoCmd).status)
        assertTrue(InMemoryDatabase.transactions.value.isEmpty())
        assertEquals(200.0, InMemoryDatabase.accounts.value.first { it.id == cashId }.balance, 0.01)
    }

    @Test
    fun companionSnapshotRespectsGranularPrivacyGatesAndIncludesUpcomingBill() = runTest {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val start = today.minusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = today.plusDays(4).atStartOfDay(zone).toInstant().toEpochMilli()
        val now = System.currentTimeMillis()

        val cashId = InMemoryDatabase.insertAccount(
            AccountEntity(name = "Wallet (Cash)", type = AccountType.CASH, balance = 300.0, icon = "wallet")
        )
        val cash = InMemoryDatabase.accounts.value.first { it.id == cashId }
        InMemoryDatabase.budgets.value = listOf(BudgetEntity("ACTIVE_PERIOD", 1000.0, start, end))
        val sub = SubscriptionEntity(
            id = 1L, name = "Spotify", amount = 68.0, category = "Entertainment",
            frequency = "Monthly", nextPaymentDate = today.plusDays(3).atStartOfDay(zone).toInstant().toEpochMilli(),
            accountId = 1L
        )
        val info = BudgetRepository().getDailyAllowanceFlow().first()

        val budgetOnly = companionSnapshotForWear(
            info = info, revision = 1L, now = now, zone = zone,
            budgetAllowed = true, pendingAllowed = false, recentAllowed = false,
            motion = "FULL", accounts = listOf(cash), pendingPayments = emptyList(),
            transactions = emptyList(), subscriptions = listOf(sub)
        )
        assertEquals(BudgetStatus.READY, budgetOnly.status)
        assertNull(budgetOnly.upcomingBill)
        assertTrue(budgetOnly.quickEntryAccounts.isEmpty())
        assertEquals(budgetOnly, WearCompanionSnapshot.decode(budgetOnly.encode()))

        val full = companionSnapshotForWear(
            info = info, revision = 2L, now = now, zone = zone,
            budgetAllowed = true, pendingAllowed = true, recentAllowed = true,
            motion = "FULL", accounts = listOf(cash), pendingPayments = emptyList(),
            transactions = emptyList(), subscriptions = listOf(sub)
        )
        assertNotNull(full.upcomingBill)
        assertEquals("Spotify", full.upcomingBill!!.name)
        assertEquals(3, full.upcomingBill!!.daysUntilDue)
        assertEquals(1, full.quickEntryAccounts.size)
        assertTrue(full.dailyProgress in 0f..1f)
        assertEquals(full, WearCompanionSnapshot.decode(full.encode()))
    }
}
