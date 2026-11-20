package com.example.vibefinance.wear

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalScrollCaptureInProgress
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.material3.onehandedgesture.OneHandedGestureAction
import androidx.wear.compose.material3.onehandedgesture.OneHandedGesturePriority
import androidx.wear.compose.material3.onehandedgesture.oneHandedGesture
import androidx.wear.compose.material3.onehandedgesture.rememberOneHandedGestureConfiguration
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.example.vibefinance.wear.contract.*
import kotlinx.coroutines.*
import java.text.DateFormat
import java.util.Date
import java.util.UUID

class BudgetActivity : ComponentActivity() {
    private var launchRoute = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launchRoute.value = intent?.getStringExtra(BudgetTileService.EXTRA_WEAR_ROUTE)
        setContent {
            val context = LocalContext.current
            val companionChanges by CompanionCache.changes.collectAsState()
            val budgetChanges by BudgetCache.changes.collectAsState()
            val motionMode = remember(companionChanges, budgetChanges) {
                CompanionCache(context).read().snapshot?.motion
                    ?: BudgetCache(context).read().snapshot?.motion
                    ?: "FULL"
            }
            val motionScheme = if (motionMode == "FULL") MotionScheme.expressive() else MotionScheme.standard()
            MaterialTheme(
                colorScheme = dynamicColorScheme(context) ?: ColorScheme(
                    primary = Color(0xffb9f4cb), onPrimary = Color(0xff00391e),
                    primaryContainer = Color(0xff14512e), onPrimaryContainer = Color(0xffb9f4cb)
                ),
                motionScheme = motionScheme
            ) {
                LiveBudgetScreen(
                    initialRouteHint = launchRoute.value,
                    onConsumeRouteHint = { launchRoute.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        launchRoute.value = intent.getStringExtra(BudgetTileService.EXTRA_WEAR_ROUTE)
    }
}

private sealed interface WearRoute {
    data object Home : WearRoute
    data object Pending : WearRoute
    data object QuickAdd : WearRoute
    data class RecentDetail(val tx: WearRecentTransaction, val startInCategoryPicker: Boolean = false) : WearRoute
}

private sealed interface HomeDialogState {
    data object None : HomeDialogState
    data class Success(val text: String) : HomeDialogState
    data class Failure(val text: String) : HomeDialogState
    data object OpenOnPhone : HomeDialogState
}

@Composable
private fun LiveBudgetScreen(
    initialRouteHint: String?,
    onConsumeRouteHint: () -> Unit
) {
    val context = LocalContext.current
    val budgetCache = remember(context) { BudgetCache(context) }
    val companionCache = remember(context) { CompanionCache(context) }
    val budgetChanges by BudgetCache.changes.collectAsState()
    val companionChanges by CompanionCache.changes.collectAsState()

    var budget by remember { mutableStateOf(budgetCache.read()) }
    var companion by remember { mutableStateOf(companionCache.read()) }
    var queuedCount by remember { mutableIntStateOf(companionCache.queuedCommands().size) }
    var refreshing by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    var route by remember { mutableStateOf<WearRoute>(WearRoute.Home) }
    var homeDialog by remember { mutableStateOf<HomeDialogState>(HomeDialogState.None) }

    val scope = rememberCoroutineScope()
    val transport = remember(context) { WearBudgetTransport(context) }
    val doneText = stringResource(R.string.pending_done)
    val failedText = stringResource(R.string.pending_failed)
    val openPhoneCurved = stringResource(R.string.opened_on_phone_curved)

    fun reloadCaches() {
        budget = budgetCache.read()
        companion = companionCache.read()
        queuedCount = companionCache.queuedCommands().size
    }

    // Deep-link from Tile curved EdgeButton straight into Pending Review or Quick Log
    LaunchedEffect(initialRouteHint) {
        when (initialRouteHint) {
            BudgetTileService.ROUTE_PENDING -> {
                if (companion.snapshot?.pendingItems?.isNotEmpty() == true) {
                    route = WearRoute.Pending
                }
                onConsumeRouteHint()
            }
            BudgetTileService.ROUTE_QUICK_ADD -> {
                if (companion.snapshot?.recentEnabled == true && companion.snapshot?.quickEntryAccounts?.isNotEmpty() == true) {
                    route = WearRoute.QuickAdd
                }
                onConsumeRouteHint()
            }
        }
    }

    val refresh: () -> Unit = {
        if (!refreshing) scope.launch {
            refreshing = true
            failed = false
            try {
                transport.flushQueuedCommands()
                transport.refresh()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                failed = true
            } finally {
                refreshing = false
                reloadCaches()
            }
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        reloadCaches()
        refresh()
    }
    LaunchedEffect(budgetChanges, companionChanges) { reloadCaches() }
    LaunchedEffect(budget.snapshot?.revision, companion.snapshot?.revision) {
        val nextExpiry = companion.snapshot?.expiresAt ?: budget.snapshot?.expiresAt ?: return@LaunchedEffect
        delay((nextExpiry - System.currentTimeMillis()).coerceAtLeast(0))
        reloadCaches()
    }

    val effectiveBudget = remember(budget, companion) {
        val compSnap = companion.snapshot
        if (compSnap != null && !companion.expired) {
            CachedBudget(
                snapshot = WearBudgetSnapshot(
                    revision = compSnap.revision,
                    status = compSnap.status,
                    generatedAt = compSnap.generatedAt,
                    expiresAt = compSnap.freshUntil,
                    calculationDate = compSnap.calculationDate,
                    zoneId = compSnap.zoneId,
                    dailyAmount = compSnap.dailyAmount,
                    periodAmount = compSnap.periodAmount,
                    daysLeft = compSnap.daysLeft,
                    motion = compSnap.motion
                ),
                expired = false
            )
        } else {
            budget
        }
    }

    val fullMotion = effectiveBudget.snapshot?.motion == "FULL"

    val sendCommand: suspend (WearCommand) -> WearCommandResult? = { cmd ->
        try {
            transport.sendCommand(cmd)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            null
        } finally {
            reloadCaches()
        }
    }

    when (val current = route) {
        WearRoute.Home -> BudgetScreen(
            budget = effectiveBudget,
            refreshing = refreshing,
            failed = failed,
            refresh = refresh,
            companion = companion,
            queuedCount = queuedCount,
            onOpenPending = { route = WearRoute.Pending },
            onOpenQuickAdd = { route = WearRoute.QuickAdd },
            onOpenRecent = { tx -> route = WearRoute.RecentDetail(tx) },
            onQuickUndoRecent = { tx ->
                val paymentId = tx.groupId.removePrefix("notification:")
                if (paymentId.isNotBlank()) {
                    scope.launch {
                        val res = sendCommand(
                            WearCommand(
                                requestId = UUID.randomUUID().toString(),
                                issuedAt = System.currentTimeMillis(),
                                action = WearCommandAction.UNDO_NOTIFICATION_PAYMENT,
                                paymentId = paymentId
                            )
                        )
                        homeDialog = if (res?.status == WearCommandStatus.SUCCESS) {
                            HomeDialogState.Success(doneText)
                        } else {
                            HomeDialogState.Failure(res?.message?.ifBlank { failedText } ?: failedText)
                        }
                    }
                }
            },
            onQuickCategoryRecent = { tx -> route = WearRoute.RecentDetail(tx, startInCategoryPicker = true) },
            onFlushQueued = { refresh() }
        )

        WearRoute.Pending -> WearPendingScreen(
            items = companion.snapshot?.pendingItems.orEmpty(),
            fullMotion = fullMotion,
            onSendCommand = sendCommand,
            onOpenOnPhone = { target -> transport.openOnPhone(target) },
            onClose = { route = WearRoute.Home }
        )

        WearRoute.QuickAdd -> WearQuickAddScreen(
            categories = companion.snapshot?.quickEntryCategories.orEmpty(),
            accounts = companion.snapshot?.quickEntryAccounts.orEmpty(),
            fullMotion = fullMotion,
            onSendCommand = sendCommand,
            onEnqueueOffline = { cmd ->
                companionCache.enqueueOfflineCommand(cmd)
                reloadCaches()
            },
            onClose = { route = WearRoute.Home }
        )

        is WearRoute.RecentDetail -> RecentTransactionDetailScreen(
            tx = current.tx,
            startInCategoryPicker = current.startInCategoryPicker,
            categories = companion.snapshot?.quickEntryCategories.orEmpty(),
            fullMotion = fullMotion,
            onSendCommand = sendCommand,
            onOpenOnPhone = { target -> transport.openOnPhone(target) },
            onClose = { route = WearRoute.Home }
        )
    }

    val curvedStyle = ConfirmationDialogDefaults.curvedTextStyle
    val phoneCurvedStyle = OpenOnPhoneDialogDefaults.curvedTextStyle
    when (val dlg = homeDialog) {
        is HomeDialogState.Success -> SuccessConfirmationDialog(
            visible = true,
            onDismissRequest = { homeDialog = HomeDialogState.None },
            curvedText = { confirmationDialogCurvedText(dlg.text, curvedStyle) }
        )
        is HomeDialogState.Failure -> FailureConfirmationDialog(
            visible = true,
            onDismissRequest = { homeDialog = HomeDialogState.None },
            curvedText = { confirmationDialogCurvedText(dlg.text, curvedStyle) }
        )
        HomeDialogState.OpenOnPhone -> OpenOnPhoneDialog(
            visible = true,
            onDismissRequest = { homeDialog = HomeDialogState.None },
            curvedText = { openOnPhoneDialogCurvedText(openPhoneCurved, phoneCurvedStyle) }
        )
        HomeDialogState.None -> Unit
    }
}

/** Preserved overload for existing component regression tests (`BudgetScreenTest`). */
@Composable
internal fun BudgetScreen(budget: CachedBudget, refreshing: Boolean, failed: Boolean, refresh: () -> Unit) {
    BudgetScreen(
        budget = budget,
        refreshing = refreshing,
        failed = failed,
        refresh = refresh,
        companion = null,
        queuedCount = 0,
        onOpenPending = {},
        onOpenQuickAdd = {},
        onOpenRecent = {},
        onQuickUndoRecent = {},
        onQuickCategoryRecent = {},
        onFlushQueued = {}
    )
}

@Composable
internal fun BudgetScreen(
    budget: CachedBudget,
    refreshing: Boolean,
    failed: Boolean,
    refresh: () -> Unit,
    companion: CachedCompanion?,
    queuedCount: Int,
    onOpenPending: () -> Unit,
    onOpenQuickAdd: () -> Unit,
    onOpenRecent: (WearRecentTransaction) -> Unit,
    onQuickUndoRecent: (WearRecentTransaction) -> Unit,
    onQuickCategoryRecent: (WearRecentTransaction) -> Unit,
    onFlushQueued: () -> Unit
) {
    val listState = rememberTransformingLazyColumnState()
    val transformation = rememberTransformationSpec()
    val snapshot = budget.snapshot
    val compSnap = companion?.snapshot
    val fullMotion = snapshot?.motion == "FULL"
    val scrollCapture = LocalScrollCaptureInProgress.current
    val isStale = companion?.stale == true

    val pendingCount = compSnap?.pendingItems?.size ?: 0
    val canQuickLog = compSnap?.recentEnabled == true && compSnap.quickEntryAccounts.isNotEmpty()
    val showEdgeAction = snapshot?.status == BudgetStatus.READY && (pendingCount > 0 || canQuickLog)
    val edgeLabel = when {
        pendingCount > 0 -> stringResource(R.string.tile_edge_pending, pendingCount)
        else -> stringResource(R.string.quick_log_button)
    }
    val edgeAction: () -> Unit = if (pendingCount > 0) onOpenPending else onOpenQuickAdd
    val pinchConfig = rememberOneHandedGestureConfiguration(
        action = OneHandedGestureAction.Primary,
        priority = OneHandedGesturePriority.Clickable
    )

    AppScaffold {
        Box(modifier = Modifier.fillMaxSize()) {
            // Subtle M3E segmented/circular bezel progress indicator when companion snapshot is active
            if (compSnap != null && snapshot?.status == BudgetStatus.READY && !scrollCapture) {
                val indicatorColor = when {
                    compSnap.isOverspent -> MaterialTheme.colorScheme.error
                    compSnap.dailyProgress < 0.2f -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                }
                val colors = ProgressIndicatorDefaults.colors(
                    indicatorColor = indicatorColor,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
                val progressVal = if (compSnap.isOverspent) 1f else compSnap.dailyProgress.coerceIn(0f, 1f)
                if (compSnap.daysLeft in 2..14) {
                    SegmentedCircularProgressIndicator(
                        segmentCount = compSnap.daysLeft,
                        progress = { progressVal },
                        startAngle = 225f,
                        endAngle = 315f,
                        colors = colors,
                        strokeWidth = 3.dp,
                        modifier = Modifier.fillMaxSize().padding(2.dp)
                    )
                } else {
                    CircularProgressIndicator(
                        progress = { progressVal },
                        startAngle = 225f,
                        endAngle = 315f,
                        colors = colors,
                        strokeWidth = 3.dp,
                        modifier = Modifier.fillMaxSize().padding(2.dp)
                    )
                }
            }

            ScreenScaffold(
                scrollState = listState,
                scrollIndicator = { if (!scrollCapture) ScrollIndicator(listState) },
                edgeButton = {
                    if (showEdgeAction) {
                        EdgeButton(
                            onClick = edgeAction,
                            colors = if (pendingCount > 0) {
                                ButtonDefaults.buttonColors()
                            } else {
                                ButtonDefaults.filledTonalButtonColors()
                            },
                            modifier = Modifier
                                .testTag(if (pendingCount > 0) "PendingEdgeButton" else "QuickLogEdgeButton")
                                .oneHandedGesture(
                                    gestureConfiguration = pinchConfig,
                                    onGestureLabel = edgeLabel,
                                    onGesture = { edgeAction() }
                                )
                        ) {
                            Text(edgeLabel, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            ) { padding ->
                TransformingLazyColumn(
                    state = listState,
                    contentPadding = padding,
                    modifier = Modifier.fillMaxSize().testTag("BudgetList")
                ) {
                    item {
                        ListHeader(
                            modifier = Modifier
                                .then(if (fullMotion) Modifier.transformedHeight(this, transformation) else Modifier)
                                .minimumVerticalContentPadding(ListHeaderDefaults.minimumTopListContentPadding),
                            transformation = if (fullMotion) SurfaceTransformation(transformation) else null
                        ) {
                            Text(stringResource(if (snapshot?.status == BudgetStatus.READY) R.string.daily_title else R.string.budget_title))
                        }
                    }
                    if (snapshot?.status == BudgetStatus.READY) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val amountColor = when {
                                    snapshot.dailyAmount.startsWith("-") -> MaterialTheme.colorScheme.error
                                    isStale -> MaterialTheme.colorScheme.onSurfaceVariant
                                    else -> MaterialTheme.colorScheme.primary
                                }
                                FullAmount(
                                    snapshot.dailyAmount,
                                    "DailyAmount",
                                    MaterialTheme.typography.numeralLarge,
                                    amountColor
                                )
                            }
                        }

                        if (pendingCount > 0) {
                            item {
                                Button(
                                    onClick = onOpenPending,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("PendingBanner")
                                        .minimumVerticalContentPadding(ButtonDefaults.minimumVerticalListContentPadding)
                                        .then(if (fullMotion) Modifier.transformedHeight(this, transformation) else Modifier),
                                    transformation = if (fullMotion) SurfaceTransformation(transformation) else null
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = stringResource(R.string.pending_banner_title, pendingCount),
                                            style = MaterialTheme.typography.labelMedium,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = stringResource(R.string.pending_banner_subtitle),
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.surfaceContainer,
                                            RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                                        )
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        stringResource(R.string.period_title),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    FullAmount(snapshot.periodAmount, "PeriodAmount", MaterialTheme.typography.numeralSmall)
                                }
                                Text(
                                    stringResource(R.string.days_left, snapshot.daysLeft),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.surfaceContainer,
                                            RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                                        )
                                        .padding(12.dp)
                                )
                            }
                        }

                        if (queuedCount > 0) {
                            item {
                                OutlinedButton(
                                    onClick = onFlushQueued,
                                    enabled = !refreshing,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("RetryQueuedBtn")
                                        .then(if (fullMotion) Modifier.transformedHeight(this, transformation) else Modifier),
                                    transformation = if (fullMotion) SurfaceTransformation(transformation) else null
                                ) {
                                    Text(
                                        text = stringResource(R.string.quick_add_retry_queued, queuedCount),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        val upcoming = compSnap?.upcomingBill
                        if (upcoming != null) {
                            item {
                                TitleCard(
                                    onClick = {},
                                    enabled = false,
                                    title = {
                                        Text(
                                            text = "${upcoming.name} · ${upcoming.amountText}",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    subtitle = {
                                        Text(
                                            text = stringResource(R.string.upcoming_bill_title),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("UpcomingBillCard")
                                        .then(if (fullMotion) Modifier.transformedHeight(this, transformation) else Modifier),
                                    transformation = if (fullMotion) SurfaceTransformation(transformation) else null
                                ) {
                                    Text(
                                        text = if (upcoming.daysUntilDue == 0) {
                                            stringResource(R.string.upcoming_due_today)
                                        } else {
                                            stringResource(R.string.upcoming_due_in, upcoming.daysUntilDue, upcoming.dueDateText)
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        val recents = compSnap?.recentTransactions.orEmpty()
                        if (recents.isNotEmpty()) {
                            item {
                                ListHeader(
                                    modifier = Modifier.then(if (fullMotion) Modifier.transformedHeight(this, transformation) else Modifier),
                                    transformation = if (fullMotion) SurfaceTransformation(transformation) else null
                                ) {
                                    Text(stringResource(R.string.recent_title))
                                }
                            }
                            items(recents.size) { idx ->
                                val tx = recents[idx]
                                val cardContent: @Composable () -> Unit = {
                                    TitleCard(
                                        onClick = { onOpenRecent(tx) },
                                        title = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = tx.merchant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = tx.amountText,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = if (tx.amountText.startsWith("+")) {
                                                        MaterialTheme.colorScheme.primary
                                                    } else {
                                                        MaterialTheme.colorScheme.onSurface
                                                    },
                                                    maxLines = 1
                                                )
                                            }
                                        },
                                        subtitle = {
                                            Text(
                                                text = if (tx.accountName.isNotBlank()) "${tx.category} · ${tx.accountName}" else tx.category,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("RecentTx_$idx")
                                            .then(if (fullMotion) Modifier.transformedHeight(this, transformation) else Modifier),
                                        transformation = if (fullMotion) SurfaceTransformation(transformation) else null
                                    )
                                }

                                if (tx.canQuickUndo) {
                                    val undoLabel = stringResource(R.string.recent_undo)
                                    val catLabel = stringResource(R.string.recent_change_category)
                                    SwipeToReveal(
                                        primaryAction = {
                                            PrimaryActionButton(
                                                onClick = { onQuickUndoRecent(tx) },
                                                icon = { Icon(Icons.Filled.Delete, contentDescription = undoLabel) },
                                                text = { Text(undoLabel) }
                                            )
                                        },
                                        secondaryAction = {
                                            SecondaryActionButton(
                                                onClick = { onQuickCategoryRecent(tx) },
                                                icon = { Icon(Icons.Filled.Edit, contentDescription = catLabel) }
                                            )
                                        },
                                        onSwipePrimaryAction = { onQuickUndoRecent(tx) }
                                    ) {
                                        cardContent()
                                    }
                                } else {
                                    cardContent()
                                }
                            }
                        }
                    } else {
                        item {
                            Text(
                                stringResource(
                                    when {
                                        budget.expired -> R.string.expired
                                        snapshot?.status == BudgetStatus.HIDDEN -> R.string.hidden
                                        snapshot?.status == BudgetStatus.NO_BUDGET -> R.string.no_budget
                                        snapshot?.status == BudgetStatus.ERROR -> R.string.invalid
                                        else -> R.string.waiting
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("BudgetStatus")
                            )
                        }
                    }
                    if (snapshot != null) item {
                        val timeText = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(snapshot.generatedAt))
                        Text(
                            stringResource(if (isStale) R.string.stale_notice else R.string.updated, timeText),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (failed) item {
                        Text(
                            stringResource(if (snapshot?.status == BudgetStatus.READY) R.string.cached_offline else R.string.refresh_failed),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (snapshot?.status == BudgetStatus.READY) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().testTag("RefreshStatus")
                        )
                    }
                    item {
                        FilledTonalButton(
                            onClick = refresh,
                            enabled = !refreshing,
                            modifier = Modifier.fillMaxWidth().testTag("RefreshBudget")
                                .minimumVerticalContentPadding(ButtonDefaults.minimumVerticalListContentPadding)
                                .then(if (fullMotion) Modifier.transformedHeight(this, transformation) else Modifier),
                            transformation = if (fullMotion) SurfaceTransformation(transformation) else null
                        ) {
                            Text(
                                stringResource(if (refreshing) R.string.refreshing else R.string.refresh),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentTransactionDetailScreen(
    tx: WearRecentTransaction,
    startInCategoryPicker: Boolean,
    categories: List<String>,
    fullMotion: Boolean,
    onSendCommand: suspend (WearCommand) -> WearCommandResult?,
    onOpenOnPhone: suspend (String) -> Boolean,
    onClose: () -> Unit
) {
    val listState = rememberTransformingLazyColumnState()
    val transformation = rememberTransformationSpec()
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var pickingCategory by remember(tx.id, startInCategoryPicker) { mutableStateOf(startInCategoryPicker) }
    var dialogState by remember(tx.id) { mutableStateOf<HomeDialogState>(HomeDialogState.None) }

    val paymentId = tx.groupId.removePrefix("notification:")
    val doneText = stringResource(R.string.pending_done)
    val failedText = stringResource(R.string.pending_failed)
    val openPhoneCurved = stringResource(R.string.opened_on_phone_curved)

    AppScaffold {
        ScreenScaffold(
            scrollState = listState,
            scrollIndicator = { ScrollIndicator(listState) },
            edgeButton = {
                EdgeButton(
                    onClick = {
                        if (pickingCategory) pickingCategory = false else onClose()
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    enabled = !busy
                ) {
                    Text(stringResource(R.string.quick_add_back), maxLines = 1)
                }
            }
        ) { padding ->
            TransformingLazyColumn(state = listState, contentPadding = padding, modifier = Modifier.fillMaxSize()) {
                item {
                    ListHeader(
                        modifier = Modifier.then(if (fullMotion) Modifier.transformedHeight(this, transformation) else Modifier),
                        transformation = if (fullMotion) SurfaceTransformation(transformation) else null
                    ) {
                        Text(if (pickingCategory) stringResource(R.string.quick_add_category_title) else tx.category)
                    }
                }
                if (pickingCategory) {
                    val cats = categories.ifEmpty {
                        listOf("Food & Drink", "Transport", "Groceries", "Shopping", "Entertainment", "Utilities", "Other")
                    }
                    items(cats.size) { i ->
                        val cat = cats[i]
                        FilledTonalButton(
                            onClick = {
                                if (busy) return@FilledTonalButton
                                scope.launch {
                                    busy = true
                                    val res = onSendCommand(
                                        WearCommand(
                                            requestId = UUID.randomUUID().toString(),
                                            issuedAt = System.currentTimeMillis(),
                                            action = WearCommandAction.CHANGE_NOTIFICATION_CATEGORY,
                                            paymentId = paymentId,
                                            category = cat
                                        )
                                    )
                                    busy = false
                                    dialogState = if (res?.status == WearCommandStatus.SUCCESS) {
                                        HomeDialogState.Success(doneText)
                                    } else {
                                        pickingCategory = false
                                        HomeDialogState.Failure(res?.message?.ifBlank { failedText } ?: failedText)
                                    }
                                }
                            },
                            enabled = !busy,
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (fullMotion) Modifier.transformedHeight(this, transformation) else Modifier),
                            transformation = if (fullMotion) SurfaceTransformation(transformation) else null
                        ) {
                            Text(cat, maxLines = 1, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        }
                    }
                } else {
                    item {
                        TitleCard(
                            onClick = {},
                            enabled = false,
                            title = { Text(tx.merchant, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                            subtitle = {
                                if (tx.accountName.isNotBlank()) Text(tx.accountName, maxLines = 1)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (fullMotion) Modifier.transformedHeight(this, transformation) else Modifier),
                            transformation = if (fullMotion) SurfaceTransformation(transformation) else null
                        ) {
                            Spacer(Modifier.height(4.dp))
                            FullAmount(tx.amountText, "RecentDetailAmount", MaterialTheme.typography.numeralMedium)
                        }
                    }
                    if (tx.canQuickUndo && paymentId.isNotBlank()) {
                        item {
                            val catInteraction = remember { MutableInteractionSource() }
                            val undoInteraction = remember { MutableInteractionSource() }
                            ButtonGroup(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(if (fullMotion) Modifier.transformedHeight(this, transformation) else Modifier),
                                transformation = if (fullMotion) SurfaceTransformation(transformation) else null
                            ) {
                                FilledTonalButton(
                                    onClick = { pickingCategory = true },
                                    enabled = !busy,
                                    interactionSource = catInteraction,
                                    modifier = Modifier.weight(1f).animateWidth(catInteraction)
                                ) {
                                    Text(
                                        stringResource(R.string.recent_change_category),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        if (busy) return@OutlinedButton
                                        scope.launch {
                                            busy = true
                                            val res = onSendCommand(
                                                WearCommand(
                                                    requestId = UUID.randomUUID().toString(),
                                                    issuedAt = System.currentTimeMillis(),
                                                    action = WearCommandAction.UNDO_NOTIFICATION_PAYMENT,
                                                    paymentId = paymentId
                                                )
                                            )
                                            busy = false
                                            dialogState = if (res?.status == WearCommandStatus.SUCCESS) {
                                                HomeDialogState.Success(doneText)
                                            } else {
                                                HomeDialogState.Failure(res?.message?.ifBlank { failedText } ?: failedText)
                                            }
                                        }
                                    },
                                    enabled = !busy,
                                    interactionSource = undoInteraction,
                                    modifier = Modifier.weight(1f).animateWidth(undoInteraction)
                                ) {
                                    Text(
                                        stringResource(R.string.recent_undo),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Button(
                            onClick = {
                                scope.launch {
                                    dialogState = HomeDialogState.OpenOnPhone
                                    onOpenOnPhone("history")
                                }
                            },
                            enabled = !busy,
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (fullMotion) Modifier.transformedHeight(this, transformation) else Modifier),
                            transformation = if (fullMotion) SurfaceTransformation(transformation) else null
                        ) {
                            Text(stringResource(R.string.pending_open_phone), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }

    val curvedStyle = ConfirmationDialogDefaults.curvedTextStyle
    val phoneCurvedStyle = OpenOnPhoneDialogDefaults.curvedTextStyle
    when (val dlg = dialogState) {
        is HomeDialogState.Success -> SuccessConfirmationDialog(
            visible = true,
            onDismissRequest = {
                dialogState = HomeDialogState.None
                onClose()
            },
            curvedText = { confirmationDialogCurvedText(dlg.text, curvedStyle) }
        )
        is HomeDialogState.Failure -> FailureConfirmationDialog(
            visible = true,
            onDismissRequest = { dialogState = HomeDialogState.None },
            curvedText = { confirmationDialogCurvedText(dlg.text, curvedStyle) }
        )
        HomeDialogState.OpenOnPhone -> OpenOnPhoneDialog(
            visible = true,
            onDismissRequest = { dialogState = HomeDialogState.None },
            curvedText = { openOnPhoneDialogCurvedText(openPhoneCurved, phoneCurvedStyle) }
        )
        HomeDialogState.None -> Unit
    }
}

@Composable
internal fun FullAmount(
    amount: String,
    tag: String,
    baseStyle: TextStyle = MaterialTheme.typography.numeralMedium,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val available = with(density) { maxWidth.roundToPx() }
        val style = remember(amount, available, density.fontScale, baseStyle) {
            var candidate = baseStyle
            // Preserve the full string and a readable floor; extreme amounts remain scrollable.
            while (candidate.fontSize > 14.sp && measurer.measure(amount, candidate, softWrap = false).size.width > available) {
                candidate = candidate.copy(fontSize = (candidate.fontSize.value - 1).coerceAtLeast(14f).sp)
            }
            candidate
        }
        Text(
            amount,
            style = style,
            color = color,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible,
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).testTag(tag)
        )
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
private fun BudgetPreview() {
    MaterialTheme(motionScheme = MotionScheme.expressive()) {
        BudgetScreen(
            CachedBudget(
                WearBudgetSnapshot(
                    1, BudgetStatus.READY, 1, 2,
                    "1970-01-01", "UTC", "-HK$ 1,234,567", "HK$ 2,439.50", 3
                )
            ), false, false, {}
        )
    }
}
