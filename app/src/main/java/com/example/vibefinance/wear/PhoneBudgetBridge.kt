package com.example.vibefinance.wear

import android.content.Context
import android.content.SharedPreferences
import androidx.work.*
import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.data.repository.BudgetRepository
import com.example.vibefinance.service.PendingPaymentStore
import com.example.vibefinance.util.CoordinatedPreferences
import com.example.vibefinance.wear.contract.*
import com.google.android.gms.wearable.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

data class WearNodeStatus(
    val id: String,
    val displayName: String,
    val isNearby: Boolean
)

object PhoneBudgetBridge {
    const val SHARE_KEY = "wear_budget_sharing"
    const val PENDING_DECISIONS_KEY = "wear_pending_decisions"
    const val RECENT_AND_QUICK_KEY = "wear_recent_and_quick_add"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private var observations: Job? = null
    private var started = false
    private var listener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    private val _lastSyncedAt = MutableStateFlow(0L)
    val lastSyncedAt = _lastSyncedAt.asStateFlow()

    fun start(context: Context) {
        if (started) return
        started = true
        val app = context.applicationContext
        _lastSyncedAt.value = bridgePrefs(app).getLong("last_synced_at", 0L)
        listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key in setOf(
                    SHARE_KEY, PENDING_DECISIONS_KEY, RECENT_AND_QUICK_KEY,
                    "hide_amounts", "app_lock_enabled", "motion_level"
                )
            ) {
                configure(app)
            }
        }
        prefs(app).registerOnSharedPreferenceChangeListener(listener)
        configure(app)
    }

    fun prefs(context: Context): SharedPreferences = CoordinatedPreferences.get(context, "vibe_finance_prefs")

    internal fun currentRevision(context: Context): Long = bridgePrefs(context).getLong("revision", 0L)

    suspend fun queryConnectedWatches(context: Context): List<WearNodeStatus> = try {
        Wearable.getNodeClient(context.applicationContext).connectedNodes.await().map { node ->
            WearNodeStatus(node.id, node.displayName, node.isNearby)
        }
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        emptyList()
    }

    private fun configure(context: Context) {
        val enabled = prefs(context).getBoolean(SHARE_KEY, false)
        if (!enabled && !bridgePrefs(context).getBoolean("published", false)) return
        val manager = WorkManager.getInstance(context)
        if (enabled) {
            manager.enqueueUniquePeriodicWork(
                "wear-budget-refresh", ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<PhoneBudgetWorker>(15, TimeUnit.MINUTES).build()
            )
            if (observations?.isActive != true) observations = scope.launch {
                try {
                    InMemoryDatabase.ensureInitialized(context)
                    PendingPaymentStore.initialize(context)
                    combine(
                        InMemoryDatabase.budgets,
                        InMemoryDatabase.transactions,
                        InMemoryDatabase.accounts,
                        InMemoryDatabase.subscriptions,
                        PendingPaymentStore.pending
                    ) { _, _, _, _, _ -> Unit }
                        .collect { publishSafely(context) }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    android.util.Log.w("WearBudget", "Budget observation unavailable")
                }
            }
            scope.launch { publishSafely(context) }
        } else {
            observations?.cancel()
            observations = null
            manager.cancelUniqueWork("wear-budget-refresh")
            // A persisted publication flag lets an offline watch receive a tombstone later.
            if (bridgePrefs(context).getBoolean("published", false)) scope.launch { publishSafely(context) }
        }
    }

    private fun bridgePrefs(context: Context) = context.getSharedPreferences("wear_budget_bridge", Context.MODE_PRIVATE)

    suspend fun publishSafely(context: Context): Boolean = try {
        publish(context)
        true
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        android.util.Log.w("WearBudget", "Budget publication unavailable")
        false
    }

    private suspend fun publish(context: Context) = mutex.withLock {
        val prefs = prefs(context)
        val allowed = prefs.getBoolean(SHARE_KEY, false) &&
            !prefs.getBoolean("hide_amounts", false) &&
            !prefs.getBoolean("app_lock_enabled", false)
        val pendingAllowed = allowed && prefs.getBoolean(PENDING_DECISIONS_KEY, false)
        val recentAllowed = allowed && prefs.getBoolean(RECENT_AND_QUICK_KEY, false)
        val zone = ZoneId.systemDefault()
        var calculationDate = LocalDate.now(zone)
        val info = if (allowed) {
            InMemoryDatabase.ensureInitialized(context)
            PendingPaymentStore.initialize(context)
            var result = BudgetRepository().getDailyAllowanceFlow().first()
            if (LocalDate.now(zone) != calculationDate) {
                calculationDate = LocalDate.now(zone)
                result = BudgetRepository().getDailyAllowanceFlow().first()
            }
            result
        } else null
        val now = System.currentTimeMillis()
        val bridge = bridgePrefs(context)
        val revision = maxOf(now, bridge.getLong("revision", 0) + 1)
        check(bridge.edit().putLong("revision", revision).putBoolean("published", true).commit())

        // Recheck after IO so a simultaneous privacy change never renews a visible lease.
        val stillAllowed = allowed &&
            prefs.getBoolean(SHARE_KEY, false) &&
            !prefs.getBoolean("hide_amounts", false) &&
            !prefs.getBoolean("app_lock_enabled", false)
        val stillPending = stillAllowed && pendingAllowed && prefs.getBoolean(PENDING_DECISIONS_KEY, false)
        val stillRecent = stillAllowed && recentAllowed && prefs.getBoolean(RECENT_AND_QUICK_KEY, false)
        val motion = prefs.getString("motion_level", "MINIMAL").orEmpty()

        val budgetSnapshot = snapshotForWear(info, revision, now, zone, stillAllowed, motion)
        val companionSnapshot = companionSnapshotForWear(
            info = info,
            revision = revision,
            now = now,
            zone = zone,
            budgetAllowed = stillAllowed,
            pendingAllowed = stillPending,
            recentAllowed = stillRecent,
            motion = motion,
            accounts = if (stillPending || stillRecent) InMemoryDatabase.accounts.value else emptyList(),
            pendingPayments = if (stillPending) PendingPaymentStore.pending.value else emptyList(),
            transactions = if (stillRecent) InMemoryDatabase.transactions.value else emptyList(),
            subscriptions = if (stillRecent) InMemoryDatabase.subscriptions.value else emptyList()
        )

        val dataClient = Wearable.getDataClient(context)
        val budgetRequest = PutDataMapRequest.create(WearBudgetSnapshot.DATA_PATH).apply {
            dataMap.putByteArray(WearBudgetSnapshot.PAYLOAD_KEY, budgetSnapshot.encode())
        }.asPutDataRequest().setUrgent()
        val companionRequest = PutDataMapRequest.create(WearCompanionSnapshot.DATA_PATH).apply {
            dataMap.putByteArray(WearCompanionSnapshot.PAYLOAD_KEY, companionSnapshot.encode())
        }.asPutDataRequest().setUrgent()

        dataClient.putDataItem(budgetRequest).await()
        dataClient.putDataItem(companionRequest).await()

        bridge.edit().putLong("last_synced_at", now).apply()
        _lastSyncedAt.value = now
    }
}

class PhoneBudgetWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = if (PhoneBudgetBridge.publishSafely(applicationContext))
        Result.success() else Result.retry()
}

class PhoneBudgetService : WearableListenerService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(event: MessageEvent) {
        val app = applicationContext
        when (event.path) {
            WearBudgetSnapshot.REFRESH_PATH -> {
                if (event.data.isNotEmpty()) return
                // Publish immediately for low-latency watch refresh; fall back to WorkManager if needed.
                serviceScope.launch {
                    val ok = PhoneBudgetBridge.publishSafely(app)
                    if (!ok) {
                        WorkManager.getInstance(app).enqueueUniqueWork(
                            "wear-budget-request", ExistingWorkPolicy.KEEP,
                            OneTimeWorkRequestBuilder<PhoneBudgetWorker>().build()
                        )
                    }
                }
            }
            WearCommand.COMMAND_PATH -> {
                val payload = event.data
                val sourceNode = event.sourceNodeId
                if (payload.isEmpty() || sourceNode.isBlank()) return
                serviceScope.launch {
                    val result = try {
                        val command = WearCommand.decode(payload)
                        WearCommandHandler.handle(app, command)
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        WearCommandResult("unknown", WearCommandStatus.INVALID_REQUEST, "Invalid command")
                    }
                    runCatching {
                        Wearable.getMessageClient(app)
                            .sendMessage(sourceNode, WearCommandResult.RESULT_PATH, result.encode())
                            .await()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
