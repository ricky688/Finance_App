package com.example.vibefinance.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalScrollCaptureInProgress
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.rememberTextMeasurer
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
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.example.vibefinance.wear.contract.*
import kotlinx.coroutines.*
import java.text.DateFormat
import java.util.Date

class BudgetActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = dynamicColorScheme(LocalContext.current) ?: ColorScheme(
                primary = Color(0xffb9f4cb), onPrimary = Color(0xff00391e),
                primaryContainer = Color(0xff14512e), onPrimaryContainer = Color(0xffb9f4cb)
            )) { LiveBudgetScreen() }
        }
    }
}

@Composable
private fun LiveBudgetScreen() {
    val context = LocalContext.current
    val cache = remember(context) { BudgetCache(context) }
    val changes by BudgetCache.changes.collectAsState()
    var budget by remember { mutableStateOf(cache.read()) }
    var refreshing by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val refresh: () -> Unit = {
        if (!refreshing) scope.launch {
            refreshing = true; failed = false
            try { WearBudgetTransport(context).refresh() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { failed = true }
            finally { refreshing = false; budget = cache.read() }
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { budget = cache.read(); refresh() }
    LaunchedEffect(changes) { budget = cache.read() }
    LaunchedEffect(budget.snapshot?.revision) {
        val snapshot = budget.snapshot ?: return@LaunchedEffect
        delay((snapshot.expiresAt - System.currentTimeMillis()).coerceAtLeast(0))
        budget = cache.read()
    }
    BudgetScreen(budget, refreshing, failed, refresh)
}

@Composable
internal fun BudgetScreen(budget: CachedBudget, refreshing: Boolean, failed: Boolean, refresh: () -> Unit) {
    val listState = rememberTransformingLazyColumnState()
    val transformation = rememberTransformationSpec()
    val snapshot = budget.snapshot
    val fullMotion = snapshot?.motion == "FULL"
    val scrollCapture = LocalScrollCaptureInProgress.current
    AppScaffold {
        ScreenScaffold(scrollState = listState,
            scrollIndicator = { if (!scrollCapture) ScrollIndicator(listState) }) { padding ->
            TransformingLazyColumn(state = listState, contentPadding = padding, modifier = Modifier.fillMaxSize().testTag("BudgetList")) {
                item {
                    ListHeader(modifier = Modifier.then(if (fullMotion) Modifier.transformedHeight(this, transformation) else Modifier)
                        .minimumVerticalContentPadding(ListHeaderDefaults.minimumTopListContentPadding),
                        transformation = if (fullMotion) SurfaceTransformation(transformation) else null) {
                        Text(stringResource(if (snapshot?.status == BudgetStatus.READY) R.string.daily_title else R.string.budget_title))
                    }
                }
                if (snapshot?.status == BudgetStatus.READY) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            FullAmount(snapshot.dailyAmount, "DailyAmount", MaterialTheme.typography.numeralLarge,
                                if (snapshot.dailyAmount.startsWith("-")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                        }
                    }
                    item {
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Column(modifier = Modifier.fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceContainer,
                                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                .padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(stringResource(R.string.period_title), style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                FullAmount(snapshot.periodAmount, "PeriodAmount", MaterialTheme.typography.numeralSmall)
                            }
                            Text(stringResource(R.string.days_left, snapshot.daysLeft),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceContainer,
                                        RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp))
                                    .padding(12.dp))
                        }
                    }
                } else {
                    item {
                        Text(stringResource(when {
                            budget.expired -> R.string.expired
                            snapshot?.status == BudgetStatus.HIDDEN -> R.string.hidden
                            snapshot?.status == BudgetStatus.NO_BUDGET -> R.string.no_budget
                            snapshot?.status == BudgetStatus.ERROR -> R.string.invalid
                            else -> R.string.waiting
                        }), modifier = Modifier.fillMaxWidth().testTag("BudgetStatus"))
                    }
                }
                if (snapshot != null) item {
                    Text(stringResource(R.string.updated, DateFormat.getTimeInstance(DateFormat.SHORT)
                        .format(Date(snapshot.generatedAt))), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth())
                }
                if (failed) item {
                    Text(stringResource(if (snapshot?.status == BudgetStatus.READY) R.string.cached_offline else R.string.refresh_failed),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (snapshot?.status == BudgetStatus.READY) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().testTag("RefreshStatus"))
                }
                item {
                    FilledTonalButton(onClick = refresh, enabled = !refreshing,
                        modifier = Modifier.fillMaxWidth().testTag("RefreshBudget")
                            .minimumVerticalContentPadding(ButtonDefaults.minimumVerticalListContentPadding)
                            .then(if (fullMotion) Modifier.transformedHeight(this, transformation) else Modifier),
                        transformation = if (fullMotion) SurfaceTransformation(transformation) else null) {
                        Text(stringResource(if (refreshing) R.string.refreshing else R.string.refresh),
                            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

@Composable
internal fun FullAmount(amount: String, tag: String,
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
        Text(amount, style = style, color = color, textAlign = TextAlign.Center, maxLines = 1, softWrap = false, overflow = TextOverflow.Visible,
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).testTag(tag))
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
private fun BudgetPreview() {
    MaterialTheme {
        BudgetScreen(CachedBudget(WearBudgetSnapshot(1, BudgetStatus.READY, 1, 2,
            "1970-01-01", "UTC", "-HK$ 1,234,567", "HK$ 2,439.50", 3)), false, false, {})
    }
}
