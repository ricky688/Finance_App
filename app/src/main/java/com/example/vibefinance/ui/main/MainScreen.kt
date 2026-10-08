@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.main

import com.example.vibefinance.ui.components.rememberCompletePressProgress
import com.example.vibefinance.ui.components.completePressShape

import com.example.vibefinance.ui.components.CompletePressButton

import com.example.vibefinance.ui.common.ScrollBlurContainer
import com.example.vibefinance.ui.common.horizontalFadingEdge
import androidx.compose.ui.platform.testTag
import com.example.vibefinance.ui.common.verticalFadingEdge
import com.example.vibefinance.ui.components.SubtleGlass
import com.example.vibefinance.ui.components.SubtleGlassTransition
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.mutableIntStateOf
import com.example.vibefinance.ui.home.RecalcBudgetSheet
import android.widget.Toast
import com.example.vibefinance.data.currency.ExchangeRateLoader
import com.example.vibefinance.data.currency.ExchangeRateRepository
import com.example.vibefinance.data.currency.ExchangeRateResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.ToggleFloatingActionButtonDefaults
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.ripple
import com.example.vibefinance.ui.components.ExpressiveSwitch
import com.example.vibefinance.ui.components.rememberConnectedButtonColorMotion
import com.example.vibefinance.ui.components.ConnectedButtonRipple
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.draw.shadow
import com.example.vibefinance.ui.common.bouncyClickable
import com.example.vibefinance.ui.common.pressBounce
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.animation.animateContentSize
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.service.PendingPaymentStore
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.ui.home.CategoryIcon
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material.icons.outlined.AutoMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.example.vibefinance.R
import com.example.vibefinance.ui.AppLanguage
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.layout
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.example.vibefinance.ui.components.KeyboardButton
import com.example.vibefinance.ui.components.KeyboardButtonType
import com.example.vibefinance.ui.components.GlassmorphicCard
import com.example.vibefinance.ui.components.VibeFinanceIcon
import androidx.compose.material3.Surface
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import com.example.vibefinance.ui.home.LocalAmbientMotionEnabled
import androidx.compose.runtime.toMutableStateList
import androidx.compose.foundation.horizontalScroll
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vibefinance.data.repository.DailyBudgetInfo
import com.example.vibefinance.theme.ThemeMode
import com.example.vibefinance.ui.FinanceIntent
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.FinanceUiEvent
import com.example.vibefinance.ui.FinanceViewModel
import com.example.vibefinance.ui.accounts.AccountsScreen
import com.example.vibefinance.ui.components.ObsidianGradientBackground
import com.example.vibefinance.ui.history.HistoryScreen
import com.example.vibefinance.ui.home.HomeScreen
import java.time.Instant
import java.time.ZoneId
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Palette
import androidx.compose.ui.platform.LocalConfiguration
import com.example.vibefinance.ui.components.datepicker.DatePicker
import com.example.vibefinance.ui.components.datepicker.CalendarState
import com.example.vibefinance.ui.components.datepicker.CalendarSelectionMode
import com.example.vibefinance.ui.components.datepicker.selectedDatesFormatted
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawWithContent
import com.example.vibefinance.ui.recurring.RecurringScreen
import androidx.compose.runtime.saveable.rememberSaveable
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

import com.example.vibefinance.ui.radar.RadarScreen
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.outlined.GpsFixed

enum class TabItem { HOME, ACCOUNTS, RECURRING, HISTORY }

enum class TransactionMode {
    EXPENSE,
    INCOME,
    TRANSFER
}

private data class TopBarPageText(val primary: String, val supporting: String)

@Composable
private fun topBarPageText(tab: TabItem, state: FinanceUiState): TopBarPageText {
    val primary = when (tab) {
        TabItem.HOME -> stringResource(R.string.topbar_vibe_overview)
        TabItem.ACCOUNTS -> stringResource(R.string.topbar_assets_cards)
        TabItem.RECURRING -> stringResource(R.string.subscriptions_title)
        TabItem.HISTORY -> stringResource(R.string.topbar_activity_history)
    }
    val translation = when (tab) {
        TabItem.HOME -> stringResource(R.string.topbar_alt_vibe_overview)
        TabItem.ACCOUNTS -> stringResource(R.string.topbar_alt_assets_cards)
        TabItem.RECURRING -> stringResource(R.string.topbar_alt_subscriptions)
        TabItem.HISTORY -> stringResource(R.string.topbar_alt_activity_history)
    }
    val detail = when (tab) {
        TabItem.HOME -> stringResource(R.string.topbar_daily_flow)
        TabItem.ACCOUNTS -> stringResource(R.string.topbar_cards_assets_count, state.accounts.size)
        TabItem.RECURRING -> stringResource(R.string.active_count_format, state.subscriptions.size)
        TabItem.HISTORY -> stringResource(R.string.topbar_transactions_logged, state.transactions.size)
    }
    return TopBarPageText(primary, "$translation · $detail")
}

@Composable
private fun ExpressiveCollapsingTopBar(
    selectedTab: TabItem,
    state: FinanceUiState,
    isExpanded: Boolean,
    hazeState: HazeState,
    backgroundObscured: Boolean,
    onHeightMeasured: (Float) -> Unit,
    actions: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    canNavigateBack: Boolean = false,
    onNavigateBack: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    val topRowHeight = 64.dp +
        32.dp * (LocalDensity.current.fontScale - 1f).coerceAtLeast(0f)
    val springFloat = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    val specularEdge = Brush.horizontalGradient(
        0f to Color.Transparent,
        0.22f to colors.outlineVariant.copy(alpha = 0.18f),
        0.50f to colors.surfaceTint.copy(alpha = 0.25f),
        0.78f to colors.outlineVariant.copy(alpha = 0.18f),
        1f to Color.Transparent
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { onHeightMeasured(it.size.height.toFloat()) }
            .hazeEffect(
                state = hazeState,
                style = SubtleGlass.style(colors.surface),
                block = { blurEnabled = !backgroundObscured }
            )
            .background(colors.surface.copy(alpha = 0.90f))
            .drawWithContent {
                drawContent()
                drawLine(
                    brush = specularEdge,
                    start = Offset(0f, size.height - 1.dp.toPx()),
                    end = Offset(size.width, size.height - 1.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(topRowHeight)
                .padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (canNavigateBack) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.nav_back))
                }
            } else {
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = colors.primaryContainer,
                        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.3f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            VibeFinanceIcon(
                                size = 22.dp,
                                showBackground = false
                            )
                        }
                    }
                }
            }

            AnimatedContent(
                targetState = if (isExpanded) null else selectedTab,
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart,
                transitionSpec = {
                    val enter = fadeIn(animationSpec = springFloat) + slideInVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) { -it / 2 }
                    val exit = fadeOut(animationSpec = springFloat) + slideOutVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) { it / 2 }
                    (enter togetherWith exit).using(
                        SizeTransform(clip = false) { _, _ ->
                            spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        }
                    )
                },
                label = "topRowBrandToCompactTitle"
            ) { compactTab ->
                if (compactTab == null) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                } else {
                    val page = topBarPageText(compactTab, state)
                    Column {
                        Text(
                            text = page.primary,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.onSurface,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = page.supporting,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
            actions()
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(
                expandFrom = Alignment.Top,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(animationSpec = springFloat) + slideInVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) { -it / 4 },
            exit = shrinkVertically(
                shrinkTowards = Alignment.Top,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeOut(animationSpec = springFloat) + slideOutVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) { -it / 4 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Line 1: Primary Title (Finance Overview) with energetic medium-bouncy vertical spring
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        val enter = fadeIn(animationSpec = springFloat) + slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) { -it / 2 }
                        val exit = fadeOut(animationSpec = springFloat) + slideOutVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ) { it / 2 }
                        enter togetherWith exit
                    },
                    label = "largePagePrimaryTitle"
                ) { tab ->
                    val page = topBarPageText(tab, state)
                    Text(
                        text = page.primary,
                        fontSize = 26.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                // Line 2: Supporting Subtitle (English word / subtext below) with separate, staggered softer spring
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        val enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = 260,
                                delayMillis = 65,
                                easing = FastOutSlowInEasing
                            )
                        ) + slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ) { -it * 3 / 4 }
                        val exit = fadeOut(
                            animationSpec = tween(durationMillis = 140)
                        ) + slideOutVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) { it * 3 / 4 }
                        enter togetherWith exit
                    },
                    label = "largePageSupportingTitle"
                ) { tab ->
                    val page = topBarPageText(tab, state)
                    Text(
                        text = page.supporting,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    // Cache history ranking while the page is loaded, not during every sheet's first frame.
    val categoryFrequency = remember(state.transactions) {
        state.transactions.groupingBy { it.category }.eachCount()
    }
    val accountFrequency = remember(state.transactions) {
        state.transactions.groupingBy { it.accountId }.eachCount()
    }
    var selectedTab by rememberSaveable { mutableStateOf(TabItem.HOME) }
    var historyAccountFilterId by rememberSaveable { mutableStateOf<Long?>(null) }
    var historyCategoryFilter by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(state.categoryMergeRules) {
        historyCategoryFilter = historyCategoryFilter?.let {
            state.categoryMergeRules.resolve(it, com.example.vibefinance.data.entity.CategoryKind.EXPENSE)
        }
    }
    val requestedTab by viewModel.requestedTab.collectAsStateWithLifecycle()
    LaunchedEffect(requestedTab) {
        requestedTab?.let {
            selectedTab = it
            viewModel.clearRequestedTab()
        }
    }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showAddRecurringSheet by remember { mutableStateOf(false) }
    var showLoadingIndicator by remember { mutableStateOf(false) }
    var isFabMenuExpanded by remember { mutableStateOf(false) }
    var activeTransactionMode by remember { mutableStateOf(TransactionMode.EXPENSE) }
    val snackbarHostState = remember { SnackbarHostState() }
    val pendingPayments by PendingPaymentStore.pending.collectAsStateWithLifecycle()
    var deferredPaymentIds by remember { mutableStateOf(emptySet<String>()) }
    var savingPaymentId by remember { mutableStateOf<String?>(null) }
    val pendingChoiceScope = rememberCoroutineScope()
    LaunchedEffect(context) { PendingPaymentStore.initialize(context) }
    val paymentToChoose = pendingPayments.firstOrNull { it.id !in deferredPaymentIds }

    // Scroll state tracking
    val density = LocalDensity.current
    var measuredBottomBarHeightPx by remember { mutableFloatStateOf(0f) }

    var isFabExpanded by remember { mutableStateOf(true) }
    var isNavBarVisible by remember { mutableStateOf(true) }
    var isTopBarExpanded by rememberSaveable { mutableStateOf(true) }
    var topBarScrollDistancePx by remember { mutableFloatStateOf(0f) }
    var expandedTopBarHeightPx by remember(density.fontScale) { mutableFloatStateOf(0f) }
    val topBarCollapseThresholdPx = with(density) { 32.dp.toPx() }
    val hazeState = rememberHazeState()
    val overlayTopBar = LocalConfiguration.current.screenWidthDp < 600
    val pageTopPadding = if (overlayTopBar) {
        val expandedHeight = if (expandedTopBarHeightPx > 0f) {
            with(density) { expandedTopBarHeightPx.toDp() }
        } else {
            WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 152.dp
        }
        expandedHeight + 16.dp
    } else {
        16.dp
    }

    val navBarOffsetY by animateDpAsState(
        targetValue = if (isNavBarVisible) 0.dp else 140.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "navBarOffsetY"
    )

    val navBarInsetsBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val fabMaxTranslation = if (measuredBottomBarHeightPx > 0f) {
        (with(density) { measuredBottomBarHeightPx.toDp() } - navBarInsetsBottom).coerceIn(60.dp, 96.dp)
    } else {
        80.dp
    }

    val fabOffsetY by animateDpAsState(
        targetValue = if (isNavBarVisible) 0.dp else fabMaxTranslation,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "fabOffsetY"
    )

    val nestedScrollConnection = remember(showAddDialog, topBarCollapseThresholdPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (showAddDialog) {
                    return Offset.Zero
                }
                val delta = available.y
                
                // M3 Motion: Scroll down slides Navigation Bar off-screen, scroll up slides on-screen
                if (delta < -8f) {
                    isFabExpanded = false
                    isNavBarVisible = false
                } else if (delta > 8f) {
                    isFabExpanded = true
                    isNavBarVisible = true
                }
 
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (!showAddDialog) {
                    val scrollY = if (consumed.y != 0f) consumed.y else available.y.coerceAtLeast(0f)
                    if (scrollY != 0f) {
                        topBarScrollDistancePx =
                            (topBarScrollDistancePx - scrollY).coerceAtLeast(0f)
                        isTopBarExpanded = topBarScrollDistancePx < topBarCollapseThresholdPx
                    }
                }
                return Offset.Zero
            }
 
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                return super.onPostFling(consumed, available)
            }
        }
    }

    // Reset visibility states when changing tabs
    LaunchedEffect(selectedTab) {
        isFabExpanded = true
        isNavBarVisible = true
        topBarScrollDistancePx = 0f
        isTopBarExpanded = true
    }

    val budgetInfo = state.budgetInfo ?: DailyBudgetInfo(
        totalMonthlyBudget = 0.0,
        totalSpentThisMonth = 0.0,
        monthlyRemaining = 0.0,
        dailyAllowance = 0.0,
        dailyRemaining = 0.0,
        daysLeft = 0,
        startDate = 0L,
        endDate = 0L
    )

    var showRecalcSheet by remember { mutableStateOf(false) }
    var isRecalcSheetMandatory by remember { mutableStateOf(false) }
    var showNewPeriodSheet by remember { mutableStateOf(false) }

    // --- PREDICTIVE BACK NAVIGATION HANDLERS ---
    // 1. If FAB menu is expanded, predictive back collapses it
    androidx.activity.compose.PredictiveBackHandler(enabled = isFabMenuExpanded) { progressFlow ->
        try {
            progressFlow.collect { }
            isFabMenuExpanded = false
        } catch (e: kotlinx.coroutines.CancellationException) {
            // gesture cancelled
        }
    }

    // 2. If user is on a secondary tab (History, Recurring, Assets, Radar), predictive back returns to HOME
    var tabBackProgress by remember { mutableFloatStateOf(0f) }
    val animatedTabBackProgress by animateFloatAsState(
        targetValue = tabBackProgress,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tabBackProgress"
    )

    val isAnyModalActive = showBudgetDialog || showAddDialog || showAddRecurringSheet || showRecalcSheet || showNewPeriodSheet || isFabMenuExpanded || state.importPreview != null
    val isAnySheetOpen = showRecalcSheet || showNewPeriodSheet || showAddDialog || showAddRecurringSheet
    // One bounded clock for depth, read in the layer rather than recomposing the page each frame.
    val sheetDepthProgress = animateFloatAsState(
        targetValue = if (isAnySheetOpen) 1f else 0f,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "sheetDepthProgress"
    )
    val windowFocused = androidx.compose.ui.platform.LocalWindowInfo.current.isWindowFocused
    val isBackgroundObscured by remember(isAnyModalActive, paymentToChoose, windowFocused) {
        derivedStateOf {
            !windowFocused || isAnyModalActive || paymentToChoose != null || sheetDepthProgress.value > 0f
        }
    }
    androidx.activity.compose.PredictiveBackHandler(
        enabled = selectedTab != TabItem.HOME && !isAnyModalActive
    ) { progressFlow ->
        try {
            progressFlow.collect { backEvent ->
                tabBackProgress = backEvent.progress
            }
            selectedTab = TabItem.HOME
        } catch (e: kotlinx.coroutines.CancellationException) {
            // gesture cancelled
        } finally {
            tabBackProgress = 0f
        }
    }

    val periodEndExclusive = remember(budgetInfo.endDate) {
        if (budgetInfo.endDate > 0L) {
            Instant.ofEpochMilli(budgetInfo.endDate).atZone(ZoneId.systemDefault()).toLocalDate()
                .plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } else 0L
    }
    val isPeriodEnded = budgetInfo.totalMonthlyBudget > 0 &&
        (budgetInfo.daysLeft <= 0 || (periodEndExclusive > 0L && System.currentTimeMillis() >= periodEndExclusive))

    // Automatic New-Day Rollover Recalculation & Period Ended Trigger
    // Waits for app launch/loading to complete, then adds an 800ms smooth delay so the splash screen is gone and user sees Daily page first
    val prefs = remember(context) { com.example.vibefinance.util.CoordinatedPreferences.get(context, "vibefinance_prefs") }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val autoSheetScope = rememberCoroutineScope()
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                deferredPaymentIds = emptySet()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var hasAutoTriggeredForSession by remember { mutableStateOf(false) }

    fun computeYesterdayRemainingBudget(): Double {
        if (budgetInfo.totalMonthlyBudget <= 0 || budgetInfo.startDate <= 0 || budgetInfo.endDate <= 0) return 0.0
        val today = LocalDate.now()
        val todayStart = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val yesterdayStart = today.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val startLocal = Instant.ofEpochMilli(budgetInfo.startDate).atZone(ZoneId.systemDefault()).toLocalDate()
        val endLocal = Instant.ofEpochMilli(budgetInfo.endDate).atZone(ZoneId.systemDefault()).toLocalDate()

        if (today.minusDays(1).isBefore(startLocal)) {
            return 0.0
        }

        val totalDaysInPeriod = (java.time.temporal.ChronoUnit.DAYS.between(startLocal, endLocal) + 1).coerceAtLeast(1).toDouble()
        val standardBaseDaily = if (totalDaysInPeriod > 0) budgetInfo.totalMonthlyBudget / totalDaysInPeriod else 0.0

        val yesterdayExpenses = state.transactions.filter {
            it.toAccountId == null &&
            !it.isExcludedFromDailyBudget &&
            it.amount > 0 &&
            it.timestamp >= yesterdayStart &&
            it.timestamp < todayStart
        }.sumOf { it.amount }

        return (standardBaseDaily - yesterdayExpenses).coerceAtLeast(0.0)
    }

    fun checkAndTriggerAutoSheets() {
        if (hasAutoTriggeredForSession) return
        autoSheetScope.launch {
            kotlinx.coroutines.delay(800L) // Smooth 800ms delay after app launch UI renders
            if (isPeriodEnded) {
                hasAutoTriggeredForSession = true
                showNewPeriodSheet = true
            } else {
                val currentDateStr = java.time.LocalDate.now().toString()
                val lastCheckedDate = prefs.getString("last_daily_recalc_date", "")
                if (lastCheckedDate != currentDateStr && budgetInfo.totalMonthlyBudget > 0 && budgetInfo.daysLeft > 0) {
                    val yesterdayLeft = computeYesterdayRemainingBudget()
                    val autoRemember = prefs.getBoolean("auto_apply_recalc_choice", false)
                    if (yesterdayLeft <= 0.001 || autoRemember) {
                        hasAutoTriggeredForSession = true
                        prefs.edit().putString("last_daily_recalc_date", currentDateStr).apply()
                    } else {
                        hasAutoTriggeredForSession = true
                        isRecalcSheetMandatory = true
                        showRecalcSheet = true
                    }
                }
            }
        }
    }

    LaunchedEffect(state.isLoading, isPeriodEnded) {
        if (!state.isLoading) {
            checkAndTriggerAutoSheets()
        }
    }

    // Avoid a loading flash when local data is ready almost immediately.
    LaunchedEffect(state.isLoading) {
        showLoadingIndicator = false
        if (state.isLoading) {
            delay(250)
            showLoadingIndicator = true
        }
    }

    DisposableEffect(lifecycleOwner, budgetInfo.totalMonthlyBudget, isPeriodEnded) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                if (!state.isLoading) {
                    checkAndTriggerAutoSheets()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }



    // Event listener for MVI one-use toasts
    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is FinanceUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is FinanceUiEvent.ShowSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.actionLabel,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed && event.actionIntent != null) {
                        viewModel.dispatch(event.actionIntent)
                    }
                }
                FinanceUiEvent.SeedCompleted -> {
                    // Seed completed successfully
                }
            }
        }
    }

    ObsidianGradientBackground {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection),
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                ExpressiveCollapsingTopBar(
                    selectedTab = selectedTab,
                    state = state,
                    isExpanded = isTopBarExpanded,
                    hazeState = hazeState,
                    backgroundObscured = isBackgroundObscured,
                    onHeightMeasured = { heightPx ->
                        if (isTopBarExpanded && heightPx > expandedTopBarHeightPx) {
                            expandedTopBarHeightPx = heightPx
                        }
                    },
                    actions = {
                        val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
                        val iconRotation by animateFloatAsState(
                            targetValue = if (isDarkTheme) 360f else 0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "themeIconRotation"
                        )
                        val iconTint by animateColorAsState(
                            targetValue = if (isDarkTheme) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "themeIconTint"
                        )
                        IconButton(onClick = {
                            val nextMode = if (isDarkTheme) ThemeMode.LIGHT else ThemeMode.DARK
                            viewModel.dispatch(FinanceIntent.SetThemeMode(nextMode))
                        }) {
                            AnimatedContent(
                                targetState = isDarkTheme,
                                transitionSpec = {
                                    (fadeIn(animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )) + scaleIn(
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )).togetherWith(
                                        fadeOut(animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )) + scaleOut(animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        ))
                                    )
                                },
                                label = "moonSunIconSwitch"
                            ) { isDark ->
                                Icon(
                                    imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = stringResource(R.string.theme_mode_label),
                                    tint = iconTint,
                                    modifier = Modifier.graphicsLayer { rotationZ = iconRotation }
                                )
                            }
                        }
                        IconButton(onClick = { showBudgetDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.settings_title)
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                val fabTab = when {
                    selectedTab == TabItem.HOME && !showAddDialog -> TabItem.HOME
                    selectedTab == TabItem.RECURRING && !showAddRecurringSheet -> TabItem.RECURRING
                    else -> null
                }
                AnimatedContent(
                    targetState = fabTab,
                    contentAlignment = Alignment.BottomEnd,
                    transitionSpec = {
                        val enter = fadeIn(animationSpec = tween(180)) +
                            scaleIn(
                                initialScale = 0.78f,
                                transformOrigin = TransformOrigin(1f, 1f),
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        val exit = fadeOut(animationSpec = tween(120)) +
                            scaleOut(
                                targetScale = 0.78f,
                                transformOrigin = TransformOrigin(1f, 1f),
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        // The modal covers the FAB: avoid a second layout/scale spring while
                        // the sheet is first composed. Keep the expressive tab-to-tab motion.
                        if (targetState == null || initialState == null) {
                            (fadeIn(tween(120)) togetherWith fadeOut(tween(90))).using(null)
                        } else {
                            (enter togetherWith exit).using(
                                SizeTransform(clip = false) { _, _ ->
                                    spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                }
                            )
                        }
                    },
                    label = "dailyRecurringFabTransition"
                ) { targetTab ->
                    when (targetTab) {
                        TabItem.HOME -> FloatingActionButtonMenu(
                            expanded = isFabMenuExpanded,
                            button = {
                                ToggleFloatingActionButton(
                                    checked = isFabMenuExpanded,
                                    onCheckedChange = {
                                        if (selectedTab != TabItem.HOME) return@ToggleFloatingActionButton
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        isFabMenuExpanded = !isFabMenuExpanded
                                    }
                                ) {
                                    val imageVector = if (checkedProgress > 0.5f) Icons.Default.Close else Icons.Default.Add
                                    Icon(
                                        imageVector = imageVector,
                                        contentDescription = if (isFabMenuExpanded) stringResource(R.string.ui_main_close_menu) else stringResource(R.string.ui_main_add_transaction),
                                        modifier = Modifier.animateIcon({ checkedProgress })
                                    )
                                }
                            },
                            modifier = Modifier.graphicsLayer {
                                translationY = with(density) { fabOffsetY.toPx() }
                            }
                        ) {
                            FloatingActionButtonMenuItem(
                                onClick = {
                                    if (selectedTab != TabItem.HOME) return@FloatingActionButtonMenuItem
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    activeTransactionMode = TransactionMode.TRANSFER
                                    isFabMenuExpanded = false
                                    showAddDialog = true
                                },
                                text = { Text(stringResource(R.string.filter_transfer)) },
                                icon = { Icon(Icons.Default.SwapHoriz, contentDescription = stringResource(R.string.filter_transfer)) }
                            )
                            FloatingActionButtonMenuItem(
                                onClick = {
                                    if (selectedTab != TabItem.HOME) return@FloatingActionButtonMenuItem
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    activeTransactionMode = TransactionMode.INCOME
                                    isFabMenuExpanded = false
                                    showAddDialog = true
                                },
                                text = { Text(stringResource(R.string.filter_income)) },
                                icon = { Icon(Icons.Default.TrendingUp, contentDescription = stringResource(R.string.filter_income)) }
                            )
                            FloatingActionButtonMenuItem(
                                onClick = {
                                    if (selectedTab != TabItem.HOME) return@FloatingActionButtonMenuItem
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    activeTransactionMode = TransactionMode.EXPENSE
                                    isFabMenuExpanded = false
                                    showAddDialog = true
                                },
                                text = { Text(stringResource(R.string.filter_expense)) },
                                icon = { Icon(Icons.Default.TrendingDown, contentDescription = stringResource(R.string.filter_expense)) }
                            )
                        }
                        TabItem.RECURRING -> ExtendedFloatingActionButton(
                            expanded = isFabExpanded,
                            onClick = {
                                if (selectedTab != TabItem.RECURRING) return@ExtendedFloatingActionButton
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showAddRecurringSheet = true
                            },
                            icon = { Icon(Icons.Default.Add, contentDescription = stringResource(R.string.btn_add)) },
                            text = { Text(stringResource(R.string.btn_add), fontWeight = FontWeight.Bold) },
                            modifier = Modifier
                                .graphicsLayer {
                                    translationY = with(density) { fabOffsetY.toPx() }
                                }
                                .clip(RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        else -> Unit
                    }
                }
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            if (coordinates.size.height > 0) {
                                measuredBottomBarHeightPx = coordinates.size.height.toFloat()
                            }
                        }
                        .graphicsLayer {
                            translationY = with(density) { navBarOffsetY.toPx() }
                        }
                ) {
                    var isNavBarAppEntered by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(750) // Synchronized with Material You Splash Exit
                        isNavBarAppEntered = true
                    }

                    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
                    NavigationBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .hazeEffect(hazeState, SubtleGlass.style(MaterialTheme.colorScheme.surfaceContainer)) {
                                blurEnabled = !isBackgroundObscured
                            }
                            .drawBehind {
                                drawLine(
                                    color = outlineVariant.copy(alpha = 0.35f),
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, 0f),
                                    strokeWidth = 1f
                                )
                            },
                        containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.92f),
                        tonalElevation = 0.dp
                    ) {
                        val tabs = listOf(
                            TabItem.HOME to Triple(stringResource(R.string.nav_daily), Icons.Filled.Home, Icons.Outlined.Home),
                            TabItem.ACCOUNTS to Triple(stringResource(R.string.nav_assets), Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance),
                            TabItem.RECURRING to Triple(stringResource(R.string.nav_recurring), Icons.Filled.AutoMode, Icons.Outlined.AutoMode),
                            TabItem.HISTORY to Triple(stringResource(R.string.nav_history), Icons.AutoMirrored.Filled.List, Icons.AutoMirrored.Outlined.List)
                        )

                        tabs.forEachIndexed { index, (tab, tripleInfo) ->
                            val (title, filledIcon, outlinedIcon) = tripleInfo
                            val isSelected = selectedTab == tab

                            // Staggered Left-to-Right Pop Entrance & Staggered Right-to-Left Pop Exit Animation
                            val popProgress = remember { androidx.compose.animation.core.Animatable(if (isNavBarVisible) 1f else 0f) }
                            LaunchedEffect(isNavBarVisible, isNavBarAppEntered) {
                                if (isNavBarAppEntered) {
                                    if (isNavBarVisible) {
                                        popProgress.snapTo(0f)
                                        delay(index * 50L + 20L) // Staggered left-to-right pop entrance
                                        popProgress.animateTo(
                                            targetValue = 1f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        )
                                    } else {
                                        delay((3 - index) * 40L) // Staggered right-to-left pop exit
                                        popProgress.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioLowBouncy,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        )
                                    }
                                } else {
                                    popProgress.snapTo(if (isNavBarVisible) 1f else 0f)
                                }
                            }

                            val iconScale by animateFloatAsState(
                                targetValue = if (isSelected) 1.15f else 1.0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                label = "navIconScale"
                            )

                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    if (selectedTab != tab) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        isFabMenuExpanded = false
                                        if (tab == TabItem.HISTORY) {
                                            historyAccountFilterId = null
                                            historyCategoryFilter = null
                                        }
                                        selectedTab = tab
                                    }
                                },
                                icon = {
                                    val currentIcon = if (isSelected) filledIcon else outlinedIcon
                                    Crossfade(
                                        targetState = currentIcon,
                                        animationSpec = tween(180),
                                        label = "navIconFill"
                                    ) { vector ->
                                        Icon(
                                            imageVector = vector,
                                            contentDescription = title,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .graphicsLayer {
                                                    scaleX = iconScale
                                                    scaleY = iconScale
                                                }
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                ),
                                modifier = Modifier.graphicsLayer {
                                    val progress = popProgress.value
                                    translationY = with(density) { ((1f - progress) * 56.dp.toPx()) }
                                    scaleX = progress.coerceAtLeast(0.01f)
                                    scaleY = progress.coerceAtLeast(0.01f)
                                    alpha = progress
                                }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // 1. Screen content (takes up whole screen, padded horizontally and top, bottom extends to screen edge)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = if (overlayTopBar) 0.dp else paddingValues.calculateTopPadding(),
                            start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
                            end = paddingValues.calculateEndPadding(LocalLayoutDirection.current),
                            bottom = 0.dp
                        )
                        .graphicsLayer {
                            val backScale = 1f - (animatedTabBackProgress * 0.08f)
                            val depth = sheetDepthProgress.value
                            val depthScale = 1f - 0.05f * depth
                            scaleX = depthScale * backScale
                            scaleY = depthScale * backScale
                            val backCorner = animatedTabBackProgress * 28.dp.toPx()
                            val totalCorner = (24.dp.toPx() * depth + backCorner).coerceAtLeast(0f)
                            clip = depth > 0f || animatedTabBackProgress > 0f
                            shape = RoundedCornerShape(totalCorner)
                        }
                ) {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            // Official Material 3 Top-Level Destination Motion Spec (Fade + Scale In from 94%)
                            val enterTransition = fadeIn(
                                animationSpec = tween(durationMillis = 210, easing = FastOutSlowInEasing)
                            ) + scaleIn(
                                initialScale = 0.94f,
                                transformOrigin = TransformOrigin.Center,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                            val exitTransition = fadeOut(
                                animationSpec = tween(durationMillis = 90, easing = FastOutSlowInEasing)
                            )
                            enterTransition togetherWith exitTransition
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(state = hazeState),
                        label = "tabChangeTopLevel"
                    ) { targetTab ->
                        when (targetTab) {
                            TabItem.HOME -> CompositionLocalProvider(
                                LocalAmbientMotionEnabled provides !isBackgroundObscured
                            ) {
                                HomeScreen(
                                    state = state,
                                    onIntent = viewModel::dispatch,
                                    modifier = Modifier.padding(horizontal = if (LocalConfiguration.current.screenWidthDp < 400) 12.dp else 16.dp),
                                    topContentPadding = pageTopPadding,
                                    topVisibilityInset = if (overlayTopBar) paddingValues.calculateTopPadding() else 0.dp,
                                    bottomVisibilityInset = (
                                        if (measuredBottomBarHeightPx > 0f) with(density) { measuredBottomBarHeightPx.toDp() }
                                        else 100.dp
                                    ).minus(navBarOffsetY).coerceAtLeast(navBarInsetsBottom),
                                    onViewAllClick = {
                                        historyAccountFilterId = null
                                        historyCategoryFilter = null
                                        selectedTab = TabItem.HISTORY
                                    },
                                    onCategoryClick = { category ->
                                        historyAccountFilterId = null
                                        historyCategoryFilter = category
                                        selectedTab = TabItem.HISTORY
                                    },
                                    showAddDialog = showAddDialog,
                                    onDismissAddDialog = { showAddDialog = false },
                                    onOpenBudgetDialog = {
                                        val isBudgetEnd = budgetInfo.monthlyRemaining <= 0.0 || (budgetInfo.dailyRemaining < 0.0 && budgetInfo.newDailyBudget <= 0.0)
                                        if (isPeriodEnded || isBudgetEnd) {
                                            showNewPeriodSheet = true
                                        } else {
                                            showBudgetDialog = true
                                        }
                                    },
                                    onOpenRecalcSheet = {
                                        val isBudgetEnd = budgetInfo.monthlyRemaining <= 0.0 || (budgetInfo.dailyRemaining < 0.0 && budgetInfo.newDailyBudget <= 0.0)
                                        if (isPeriodEnded || isBudgetEnd) {
                                            showNewPeriodSheet = true
                                        } else {
                                            isRecalcSheetMandatory = false
                                            showRecalcSheet = true
                                        }
                                    }
                                )
                            }
                            TabItem.ACCOUNTS -> AccountsScreen(
                                state = state,
                                onIntent = viewModel::dispatch,
                                modifier = Modifier.fillMaxSize(),
                                topContentPadding = pageTopPadding,
                                topVisibilityInset = if (overlayTopBar) paddingValues.calculateTopPadding() else 0.dp,
                                bottomVisibilityInset = (
                                    if (measuredBottomBarHeightPx > 0f) with(density) { measuredBottomBarHeightPx.toDp() }
                                    else 100.dp
                                ).minus(navBarOffsetY).coerceAtLeast(navBarInsetsBottom),
                                onViewAccountHistory = { accountId ->
                                    historyAccountFilterId = accountId
                                    historyCategoryFilter = null
                                    selectedTab = TabItem.HISTORY
                                }
                            )
                            TabItem.RECURRING -> RecurringScreen(
                                state = state,
                                onIntent = viewModel::dispatch,
                                modifier = Modifier.fillMaxSize(),
                                topContentPadding = pageTopPadding,
                                topVisibilityInset = if (overlayTopBar) paddingValues.calculateTopPadding() else 0.dp,
                                bottomVisibilityInset = (
                                    if (measuredBottomBarHeightPx > 0f) with(density) { measuredBottomBarHeightPx.toDp() }
                                    else 100.dp
                                ).minus(navBarOffsetY).coerceAtLeast(navBarInsetsBottom),
                                showAddSheet = showAddRecurringSheet,
                                onDismissAddSheet = { showAddRecurringSheet = false }
                            )
                            TabItem.HISTORY -> HistoryScreen(
                                state = state,
                                onIntent = viewModel::dispatch,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                topContentPadding = pageTopPadding,
                                topVisibilityInset = if (overlayTopBar) paddingValues.calculateTopPadding() else 0.dp,
                                bottomVisibilityInset = (
                                    if (measuredBottomBarHeightPx > 0f) with(density) { measuredBottomBarHeightPx.toDp() }
                                    else 100.dp
                                ).minus(navBarOffsetY).coerceAtLeast(navBarInsetsBottom),
                                accountFilterId = historyAccountFilterId,
                                onClearAccountFilter = { historyAccountFilterId = null },
                                categoryFilter = historyCategoryFilter,
                                onClearCategoryFilter = { historyCategoryFilter = null },
                                onAddTransaction = {
                                    activeTransactionMode = TransactionMode.EXPENSE
                                    isFabMenuExpanded = false
                                    showAddDialog = true
                                }
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = showLoadingIndicator,
                    modifier = Modifier.align(Alignment.Center),
                    enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                        scaleIn(initialScale = 0.82f, animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )),
                    exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                        scaleOut(targetScale = 0.9f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                ) {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        tonalElevation = 6.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ContainedLoadingIndicator(modifier = Modifier.size(72.dp))
                            Text(
                                text = stringResource(R.string.loading_finances),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // 5. Standard Modal Bottom Sheet for Add Transaction
                if (showAddDialog) {
                    val addExpenseSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ModalBottomSheet(
                        onDismissRequest = { showAddDialog = false },
                        sheetState = addExpenseSheetState,
                        containerColor = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    ) {
                        AddExpenseSheetContent(
                            state = state,
                            onIntent = viewModel::dispatch,
                            onDismissAddDialog = { showAddDialog = false },
                            initialMode = activeTransactionMode,
                            categoryFrequency = categoryFrequency,
                            accountFrequency = accountFrequency,
                            modifier = Modifier.statusBarsPadding()
                        )
                    }
                }

                SubtleGlassTransition(
                    state = hazeState, top = true, obscured = isBackgroundObscured,
                    modifier = Modifier.align(Alignment.TopCenter)
                        .offset(y = paddingValues.calculateTopPadding())
                )
                val visibleBottomBarHeight = (
                    if (measuredBottomBarHeightPx > 0f) with(density) { measuredBottomBarHeightPx.toDp() }
                    else 100.dp
                ).minus(navBarOffsetY)
                SubtleGlassTransition(
                    state = hazeState, top = false, obscured = isBackgroundObscured,
                    modifier = Modifier.align(Alignment.BottomCenter).offset(y = -visibleBottomBarHeight)
                )

                // 6. Backdrop Scrim Overlay when Speed Dial FAB Menu is Open
                AnimatedVisibility(
                    visible = selectedTab == TabItem.HOME && isFabMenuExpanded && !showAddDialog,
                    enter = fadeIn(animationSpec = tween(180)),
                    exit = fadeOut(animationSpec = tween(150))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.48f))
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) {
                                isFabMenuExpanded = false
                            }
                    )
                }


            }
        }
    }

    paymentToChoose?.let { payment ->
        PendingPaymentChoiceDialog(
            payment = payment,
            accounts = state.accounts,
            saving = savingPaymentId == payment.id,
            onRecord = { accountId, rememberChoice ->
                savingPaymentId = payment.id
                pendingChoiceScope.launch {
                    val recorded = runCatching {
                        PendingPaymentStore.accept(context, payment.id, accountId, rememberChoice)
                    }.getOrDefault(false)
                    savingPaymentId = null
                    snackbarHostState.showSnackbar(
                        context.getString(
                            if (recorded) R.string.pending_payment_recorded
                            else R.string.pending_payment_failed
                        )
                    )
                }
            },
            onLater = { deferredPaymentIds = deferredPaymentIds + payment.id },
            onIgnore = { PendingPaymentStore.ignore(context, payment.id) }
        )
    }

    // M3 Settings & Budget Configuration BottomSheet
    if (showBudgetDialog) {
        var selectedEndDateMillis by remember(budgetInfo.endDate) {
            mutableStateOf<Long?>(budgetInfo.endDate.takeIf { it > 0 })
        }
        var showDatePickerModal by remember { mutableStateOf(false) }

        if (showDatePickerModal) {
            val context = LocalContext.current
            val initialDate = selectedEndDateMillis?.let {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            }
            val startDate = if (budgetInfo.startDate > 0) {
                Instant.ofEpochMilli(budgetInfo.startDate).atZone(ZoneId.systemDefault()).toLocalDate()
            } else {
                LocalDate.now()
            }
            val calendarState = remember {
                CalendarState(
                    context = context,
                    selectionMode = CalendarSelectionMode.RANGE,
                    selectDate = initialDate,
                    disableBeforeDate = startDate,
                )
            }

            AlertDialog(
                onDismissRequest = { showDatePickerModal = false },
                confirmButton = {},
                dismissButton = {},
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(450.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(onClick = { showDatePickerModal = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    contentDescription = stringResource(R.string.btn_close)
                                )
                            }

                            val dateConfirmInteraction = remember { MutableInteractionSource() }
                            CompletePressButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val end = calendarState.calendarUiState.value.selectedEndDate
                                    if (end != null) {
                                        selectedEndDateMillis = end.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                    }
                                    showDatePickerModal = false
                                },
                                enabled = calendarState.calendarUiState.value.hasSelectedDates,
                                shapes = ButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                                interactionSource = dateConfirmInteraction,
                                modifier = Modifier.pressBounce(interactionSource = dateConfirmInteraction),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = stringResource(R.string.btn_confirm),
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                                Text(stringResource(R.string.btn_confirm))
                            }
                        }

                        // Selection info text
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            val locale = LocalConfiguration.current.locales[0]
                            val hasSel = calendarState.calendarUiState.value.hasSelectedDates
                            val selectedText = if (!hasSel) {
                                stringResource(R.string.settings_select_end_date)
                            } else {
                                selectedDatesFormatted(calendarState, locale)
                            }
                            Text(
                                text = selectedText,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            val daysCount = if (hasSel) {
                                val sDate = calendarState.calendarUiState.value.selectedStartDate ?: startDate
                                val eDate = calendarState.calendarUiState.value.selectedEndDate ?: sDate
                                (ChronoUnit.DAYS.between(sDate, eDate) + 1).coerceAtLeast(1).toInt()
                            } else {
                                0
                            }
                            if (daysCount > 0) {
                                Text(
                                    text = stringResource(R.string.days_count_format, daysCount),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        )

                        DatePicker(
                            calendarState = calendarState,
                            onDayClicked = { date ->
                                calendarState.setSelectedDay(date)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
            )
        }

        com.example.vibefinance.ui.settings.SettingsSheet(
            state = state,
            budgetInfo = budgetInfo,
            viewModel = viewModel,
            onDismiss = { showBudgetDialog = false },
            onOpenNewPeriod = {
                showBudgetDialog = false
                showNewPeriodSheet = true
            },
            onOpenDatePicker = { showDatePickerModal = true },
            selectedEndDateMillis = selectedEndDateMillis
        )
    }

    if (showRecalcSheet) {
        val yesterdayRemainingBudget = computeYesterdayRemainingBudget()
        val currentDateStr = java.time.LocalDate.now().toString()
        RecalcBudgetSheet(
            budgetInfo = budgetInfo,
            yesterdayLeftover = yesterdayRemainingBudget,
            isMandatory = isRecalcSheetMandatory,
            isToday = false,
            onSelectMode = { selectedMode, rememberChoice ->
                val finalStart = if (budgetInfo.startDate > 0) budgetInfo.startDate else System.currentTimeMillis()
                viewModel.dispatch(FinanceIntent.SetCustomPeriodBudget(budgetInfo.totalMonthlyBudget, finalStart, budgetInfo.endDate, selectedMode))
                prefs.edit().putString("last_daily_recalc_date", currentDateStr).apply()
                if (rememberChoice) {
                    prefs.edit().putBoolean("auto_apply_recalc_choice", true).apply()
                }
                showRecalcSheet = false
            },
            onDismiss = {
                if (!isRecalcSheetMandatory) {
                    showRecalcSheet = false
                }
            }
        )
    }

    if (showNewPeriodSheet) {
        com.example.vibefinance.ui.home.NewPeriodBudgetSheet(
            budgetInfo = budgetInfo,
            onConfirmNewPeriod = { amount, startDate, endDate ->
                viewModel.dispatch(
                    FinanceIntent.SetCustomPeriodBudget(
                        amount = amount,
                        startDate = startDate,
                        endDate = endDate,
                        showToast = true
                    )
                )
                val currentDateStr = java.time.LocalDate.now().toString()
                prefs.edit().putString("last_daily_recalc_date", currentDateStr).apply()
                showNewPeriodSheet = false
            },
            onDismiss = {
                showNewPeriodSheet = false
            }
        )
    }

    val importPreview = state.importPreview
    if (importPreview != null) {
        com.example.vibefinance.ui.settings.ImportDataPreviewSheet(
            preview = importPreview,
            isImporting = state.isImporting,
            onConfirm = { replaceExisting ->
                viewModel.dispatch(FinanceIntent.ConfirmImport(replaceExisting))
            },
            onDismiss = {
                viewModel.dispatch(FinanceIntent.DismissImportPreview)
            }
        )
    }
}

@Composable
fun <T> ExpressiveSegmentedButtonGroup(
    items: List<T>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isScrollable: Boolean = false,
    iconProvider: @Composable ((T, Color) -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    itemModifierProvider: ((Int) -> Modifier)? = null,
    fadeTagPrefix: String? = null,
    selectionRevealRequest: Int = 0,
    preserveContentColors: Boolean = false,
    labelProvider: @Composable (T) -> String
) {
    val groupScroll = rememberScrollState()
    ScrollBlurContainer(groupScroll, modifier.fillMaxWidth(), enabled = isScrollable, tagPrefix = fadeTagPrefix) { sourceModifier ->
    Row(
        modifier = if (isScrollable) {
            sourceModifier.fillMaxWidth().horizontalScroll(groupScroll)
        } else {
            sourceModifier.fillMaxWidth()
        },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = index == selectedIndex
            val revealRequester = remember { BringIntoViewRequester() }
            LaunchedEffect(selectionRevealRequest, isSelected) {
                if (isScrollable && isSelected && selectionRevealRequest > 0) {
                    // Wait for the newly selected label/check layout before locating its bounds.
                    withFrameNanos { }
                    revealRequester.bringIntoView()
                }
            }
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed = interactionSource.collectIsPressedAsState()
            val shapeProgress by rememberCompletePressProgress(interactionSource)

            val restingCorner = 20.dp
            val restingTopStart = if (isScrollable || isSelected) restingCorner else (if (index == 0) 24.dp else 8.dp)
            val restingBottomStart = if (isScrollable || isSelected) restingCorner else (if (index == 0) 24.dp else 8.dp)
            val restingTopEnd = if (isScrollable || isSelected) restingCorner else (if (index == items.size - 1) 24.dp else 8.dp)
            val restingBottomEnd = if (isScrollable || isSelected) restingCorner else (if (index == items.size - 1) 24.dp else 8.dp)

            val pressedCorner = 6.dp

            val topStart by animateDpAsState(
                targetValue = restingTopStart,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "topStart_$index"
            )
            val bottomStart by animateDpAsState(
                targetValue = restingBottomStart,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "bottomStart_$index"
            )
            val topEnd by animateDpAsState(
                targetValue = restingTopEnd,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "topEnd_$index"
            )
            val bottomEnd by animateDpAsState(
                targetValue = restingBottomEnd,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "bottomEnd_$index"
            )
            
            val shape = completePressShape(
                RoundedCornerShape(
                    topStart = topStart, bottomStart = bottomStart,
                    topEnd = topEnd, bottomEnd = bottomEnd
                ),
                RoundedCornerShape(pressedCorner),
                shapeProgress
            )

            val colorMotion = rememberConnectedButtonColorMotion(
                isSelected = isSelected,
                isPressed = isPressed.value,
                preserveContentColors = preserveContentColors
            )
            
            val itemModifier = if (isScrollable) Modifier else Modifier.weight(1f)
            
            val haptic = LocalHapticFeedback.current
            val view = LocalView.current

            Surface(
                color = colorMotion.containerColor,
                contentColor = colorMotion.contentColor,
                shape = shape,
                modifier = itemModifier
                    .then(itemModifierProvider?.invoke(index) ?: Modifier)
                    .then(if (isScrollable) Modifier.bringIntoViewRequester(revealRequester) else Modifier)
                    .clip(shape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = true, color = colorMotion.rippleColor),
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onItemSelected(index)
                        }
                    )
            ) {
                Row(
                    modifier = Modifier
                        .then(colorMotion.contentModifier)
                        .padding(vertical = 8.dp, horizontal = if (isScrollable) 12.dp else 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedVisibility(
                        visible = isSelected,
                        enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + expandHorizontally(
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
                        ),
                        exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + shrinkHorizontally(
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = colorMotion.contentColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }
                    
                    if (iconProvider != null) {
                        iconProvider(item, colorMotion.contentColor)
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    
                    Text(
                        text = labelProvider(item),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorMotion.contentColor,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
        trailingContent?.invoke()
    }
}
}

@Composable
fun ExpressiveAddButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current
    val shapeProgress by rememberCompletePressProgress(interactionSource)
    val cornerRadius = androidx.compose.ui.unit.lerp(20.dp, 8.dp, shapeProgress)
    val shape = RoundedCornerShape(cornerRadius)

    ConnectedButtonRipple {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            contentColor = MaterialTheme.colorScheme.primary,
            shape = shape,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
            modifier = modifier
                .clip(shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true),
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                    }
                )
        ) {
            Row(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun FabSpeedDialItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.12f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
fun AddExpenseSheetContent(
    state: com.example.vibefinance.ui.FinanceUiState,
    onIntent: (com.example.vibefinance.ui.FinanceIntent) -> Unit,
    onDismissAddDialog: () -> Unit,
    initialMode: TransactionMode = TransactionMode.EXPENSE,
    categoryFrequency: Map<String, Int>,
    accountFrequency: Map<Long, Int>,
    modifier: Modifier = Modifier,
    exchangeRateLoader: ExchangeRateLoader? = null
) {
    val context = LocalContext.current
    val localFocusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current
    var typedAmount by remember { mutableStateOf("0.00") }
    var descriptionText by remember { mutableStateOf("") }
    var expenseIcon by remember { mutableStateOf<String?>(null) }
    var isDailyBudget by remember { mutableStateOf(true) }
    
    val transactionMode = initialMode
    val isTransfer = initialMode == TransactionMode.TRANSFER
    var selectedCurrency by remember { mutableStateOf("HKD") }
    val defaultRateLoader = remember(context.applicationContext) {
        ExchangeRateRepository(java.io.File(context.applicationContext.cacheDir, "exchange_rates"))
    }
    val rateLoader = exchangeRateLoader ?: defaultRateLoader
    var rateResult by remember { mutableStateOf<ExchangeRateResult?>(null) }
    var rateLoading by remember { mutableStateOf(false) }
    var rateRefreshRequest by remember { mutableIntStateOf(0) }
    var lastRequestedCurrency by remember { mutableStateOf<String?>(null) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { rateRefreshRequest++ }
    LaunchedEffect(selectedCurrency, rateRefreshRequest, rateLoader) {
        val forceRefresh = lastRequestedCurrency == selectedCurrency
        lastRequestedCurrency = selectedCurrency
        rateResult = null
        rateLoading = selectedCurrency != "HKD"
        if (selectedCurrency != "HKD") {
            rateResult = rateLoader.load(selectedCurrency, forceRefresh)
            rateLoading = false
        }
    }
    // A result from the previous currency cannot be used between selection and its effect.
    val rateQuote = rateResult?.quote?.takeIf { it.currencyCode == selectedCurrency }
    val rawAmount = typedAmount.toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0 }
    val convertedAmount = rawAmount?.let { amount ->
        if (selectedCurrency == "HKD") amount else rateQuote?.let { quote ->
            runCatching { quote.convert(amount) }.getOrNull()
        }
    }

 
    val expenseCategories = remember(state.categoryMergeRules) {
        com.example.vibefinance.data.entity.categoryChoices(com.example.vibefinance.data.entity.CategoryKind.EXPENSE,
            state.transactions, state.subscriptions, state.categoryLimits, state.categoryMergeRules).toMutableStateList()
    }
    val incomeCategories = remember(state.categoryMergeRules) {
        com.example.vibefinance.data.entity.categoryChoices(com.example.vibefinance.data.entity.CategoryKind.INCOME,
            state.transactions, state.subscriptions, state.categoryLimits, state.categoryMergeRules).toMutableStateList()
    }

    val sortedExpenseCategories = remember(expenseCategories.toList(), categoryFrequency) {
        expenseCategories.sortedWith(
            compareByDescending<String> { categoryFrequency[it] ?: 0 }
        )
    }
    val sortedIncomeCategories = remember(incomeCategories.toList(), categoryFrequency) {
        incomeCategories.sortedWith(
            compareByDescending<String> { categoryFrequency[it] ?: 0 }
        )
    }
    val sortedAccounts = remember(state.accounts, accountFrequency) {
        state.accounts.sortedWith(
            compareByDescending { accountFrequency[it.id] ?: 0 }
        )
    }

    val initialExpenseCat = remember { sortedExpenseCategories.firstOrNull() ?: "Food" }
    val initialIncomeCat = remember { sortedIncomeCategories.firstOrNull() ?: "Salary" }
    var expenseCategoryText by remember { mutableStateOf(initialExpenseCat) }
    var incomeCategoryText by remember { mutableStateOf(initialIncomeCat) }

    val initialAccount = remember { sortedAccounts.firstOrNull() ?: state.accounts.firstOrNull() }
    var selectedAccount by remember { mutableStateOf(initialAccount) }
    var selectedToAccount by remember { mutableStateOf<AccountEntity?>(null) }

    var showInlineAddCategory by remember { mutableStateOf(false) }
    var showInlineAddAsset by remember { mutableStateOf(false) }

    val selectorOverlay = remember { EntrySelectorOverlayState() }
    BackHandler(enabled = selectorOverlay.activeTag != null) { selectorOverlay.activeTag = null }
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    var headerHeightPx by remember { mutableIntStateOf(0) }
    val maxScrollPx = with(density) { 150.dp.toPx() }
    val targetFraction = if (scrollState.value > with(density) { 12.dp.toPx() }) 1f else 0f
    val fraction by androidx.compose.animation.core.animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )

    val containerColor = androidx.compose.ui.graphics.lerp(
        androidx.compose.ui.graphics.Color.Transparent,
        MaterialTheme.colorScheme.primaryContainer,
        fraction
    )
    val labelColor = androidx.compose.ui.graphics.lerp(
        MaterialTheme.colorScheme.onSurface,
        MaterialTheme.colorScheme.onPrimaryContainer,
        fraction
    )
    val dollarColor = androidx.compose.ui.graphics.lerp(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.primary,
        fraction
    )
    
    val verticalPadding = (6 * fraction).dp + (4 * (1f - fraction)).dp
    val horizontalPadding = (14 * fraction).dp + (8 * (1f - fraction)).dp

    val scope = rememberCoroutineScope()
    var isScanningReceipt by remember { mutableStateOf(false) }

    val receiptPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            isScanningReceipt = true
            scope.launch {
                val scanned = com.example.vibefinance.ai.ReceiptScannerEngine.scanReceipt(context, uri)
                if (scanned != null) {
                    if (scanned.totalAmount > 0.0) {
                        typedAmount = String.format(Locale.US, "%.2f", scanned.totalAmount)
                    }
                    descriptionText = scanned.merchantName
                    if (expenseCategories.contains(scanned.category)) {
                        expenseCategoryText = scanned.category
                    } else if (incomeCategories.contains(scanned.category)) {
                        incomeCategoryText = scanned.category
                    }
                    Toast.makeText(
                        context,
                        context.getString(R.string.ui_main_receipt_success, scanned.totalAmount.toString(), scanned.merchantName),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(context, context.getString(R.string.ui_main_receipt_failed), Toast.LENGTH_SHORT).show()
                }
                isScanningReceipt = false
            }
        }
    }

    CompositionLocalProvider(LocalEntrySelectorOverlay provides selectorOverlay) {
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // The sheet already animates its position. A second size spring here
                // repeatedly moves its anchors when the measured header spacer updates.
                .verticalFadingEdge(topFadeHeight = 0.dp, bottomFadeHeight = 40.dp)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            val headerHeightDp = with(density) { if (headerHeightPx > 0) headerHeightPx.toDp() else 68.dp }
            Spacer(modifier = Modifier.height(headerHeightDp + 10.dp))

            if (selectedCurrency != "HKD") {
                CurrencyRateStatus(selectedCurrency,
                    rateResult?.takeIf { it.quote == null || it.quote.currencyCode == selectedCurrency },
                    rateLoading || rateResult == null, convertedAmount,
                    onRefresh = { rateRefreshRequest++ })
            }

            if (transactionMode == TransactionMode.EXPENSE) {
                // 1. Count in Daily Budget Switch Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isDailyBudget = !isDailyBudget
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.daily_budget_toggle_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isDailyBudget) stringResource(R.string.daily_budget_toggle_desc_on) else stringResource(R.string.daily_budget_toggle_desc_off),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isDailyBudget,
                            onCheckedChange = {
                                isDailyBudget = it
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                            thumbContent = if (isDailyBudget) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                }
                            } else null
                        )
                    }
                }

                // 2. Budget Preview Card (Dynamic calculation / Non-daily notice)
                val budgetInfo = state.budgetInfo
                if (budgetInfo != null && convertedAmount != null) {
                    val inputAmount = convertedAmount
                    val currentDailyRem = budgetInfo.dailyRemaining
                    val simDailyRem = currentDailyRem - inputAmount
                    val isOverBudget = simDailyRem < 0

                    val futureDays = (budgetInfo.daysLeft - 1).coerceAtLeast(1)
                    val newMonthlyRem = (budgetInfo.monthlyRemaining - inputAmount).coerceAtLeast(0.0)
                    val newDailyForRest = newMonthlyRem / futureDays

                    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

                    AnimatedContent(
                        targetState = isDailyBudget,
                        transitionSpec = {
                            (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                             expandVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)))
                                .togetherWith(fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                 shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)))
                        },
                        label = "budgetPreviewTransition"
                    ) { isDaily ->
                        if (isDaily) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isOverBudget) {
                                    if (isDarkTheme) Color(0xFF3B1414) else Color(0xFFFFF0F0)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                },
                                border = BorderStroke(
                                    1.dp,
                                    if (isOverBudget) {
                                        if (isDarkTheme) Color(0xFFFF5252).copy(alpha = 0.5f) else Color(0xFFE57373)
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isOverBudget) Color(0xFFFF5252).copy(alpha = 0.18f) else Color(0xFF00E676).copy(alpha = 0.18f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (isOverBudget) Icons.Default.Warning else Icons.Default.TrendingDown,
                                                contentDescription = null,
                                                tint = if (isOverBudget) Color(0xFFFF5252) else Color(0xFF00E676),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = if (isOverBudget) stringResource(R.string.ui_main_budget_warning) else stringResource(R.string.ui_main_budget_preview),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isOverBudget) (if (isDarkTheme) Color(0xFFFF8A80) else Color(0xFFB71C1C)) else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isOverBudget) {
                                                stringResource(R.string.ui_main_budget_preview_over, String.format(Locale.US, "%.0f", -simDailyRem), String.format(Locale.US, "%.0f", newDailyForRest))
                                            } else {
                                                stringResource(R.string.ui_main_budget_preview_remaining, String.format(Locale.US, "%.0f", simDailyRem))
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = if (isDarkTheme) 0.35f else 0.5f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.ShoppingCart,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = stringResource(R.string.non_daily_preview_title),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Text(
                                            text = stringResource(R.string.non_daily_preview_desc),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

        OutlinedTextField(
            value = descriptionText,
            onValueChange = { input ->
                descriptionText = input
                val suggested = com.example.vibefinance.ai.MerchantRuleEngine.suggestCategory(context, input)
                if (transactionMode == TransactionMode.INCOME) {
                    if (suggested != null && incomeCategories.contains(suggested)) {
                        incomeCategoryText = suggested
                    }
                } else {
                    if (suggested != null && expenseCategories.contains(suggested)) {
                        expenseCategoryText = suggested
                    }
                }
            },
            placeholder = { Text(stringResource(R.string.ui_main_note_hint), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f), style = MaterialTheme.typography.bodyMedium) },
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { receiptPickerLauncher.launch("image/*") }) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = stringResource(R.string.quick_scan_receipt),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        AnimatedVisibility(
            visible = !isTransfer,
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + expandVertically(
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
                expandFrom = Alignment.Top
            ),
            exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + shrinkVertically(
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
                shrinkTowards = Alignment.Top
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth().clipToBounds()) {
                Text(
                    text = if (transactionMode == TransactionMode.INCOME) stringResource(R.string.ui_main_income_category) else stringResource(R.string.sub_category_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                AnimatedVisibility(
                    visible = showInlineAddCategory && !isTransfer,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        var newCatName by remember { mutableStateOf("") }
                        Text(stringResource(R.string.ui_main_add_category), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newCatName,
                                onValueChange = { newCatName = it },
                                placeholder = { Text(stringResource(R.string.ui_main_category_hint)) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                textStyle = MaterialTheme.typography.bodyMedium
                            )
                            val addCatInteraction = remember { MutableInteractionSource() }
                            CompletePressButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val name = newCatName.trim()
                                    if (transactionMode == TransactionMode.INCOME) {
                                        if (name.isNotEmpty() && !incomeCategories.contains(name)) {
                                            incomeCategories.add(0, name)
                                            incomeCategoryText = name
                                        }
                                    } else {
                                        if (name.isNotEmpty() && !expenseCategories.contains(name)) {
                                            expenseCategories.add(0, name)
                                            expenseCategoryText = name
                                        }
                                    }
                                    newCatName = ""
                                    showInlineAddCategory = false
                                },
                                shapes = ButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                                interactionSource = addCatInteraction,
                                modifier = Modifier.pressBounce(interactionSource = addCatInteraction)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.btn_add))
                            }
                        }
                    }
                }
                
                val currentCategories = if (transactionMode == TransactionMode.INCOME) sortedIncomeCategories else sortedExpenseCategories
                val currentCategoryText = if (transactionMode == TransactionMode.INCOME) incomeCategoryText else expenseCategoryText

                ExpandableEntrySelector(
                    items = currentCategories,
                    title = if (transactionMode == TransactionMode.INCOME) stringResource(R.string.ui_main_income_category) else stringResource(R.string.sub_category_label),
                    tagPrefix = "EntryCategory",
                    compactChoices = true,
                    keyProvider = { it },
                    subtitleProvider = { it },
                    selectedIndex = currentCategories.indexOf(currentCategoryText),
                    onItemSelected = { index ->
                        if (transactionMode == TransactionMode.INCOME) {
                            incomeCategoryText = currentCategories[index]
                        } else {
                            expenseCategoryText = currentCategories[index]
                        }
                    },
                    onAdd = { showInlineAddCategory = !showInlineAddCategory },
                    labelProvider = { cat -> com.example.vibefinance.ui.home.getCategoryDisplayName(cat) }
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (transactionMode == TransactionMode.EXPENSE) {
            com.example.vibefinance.ui.components.ExpenseIconChoice(
                value = expenseIcon, category = expenseCategoryText, onValueChange = { expenseIcon = it }
            )
            Spacer(Modifier.height(8.dp))
        }

        // 3. Asset Selection Horizontal Grid
        Column(modifier = Modifier.fillMaxWidth().testTag("EntryAssetSection")) {
            Text(
                text = if (isTransfer) stringResource(R.string.transfer_from) else stringResource(R.string.ui_main_asset_selection),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            AnimatedVisibility(
                visible = showInlineAddAsset,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    var newAssetName by remember { mutableStateOf("") }
                    var selectedType by remember { mutableStateOf(AccountType.CASH) }
                    var initialBalanceText by remember { mutableStateOf("") }
                    
                    Text(stringResource(R.string.ui_main_add_account), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    
                    OutlinedTextField(
                        value = newAssetName,
                        onValueChange = { newAssetName = it },
                        placeholder = { Text(stringResource(R.string.ui_main_account_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.ui_main_account_type), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.2f))
                        ExpressiveSegmentedButtonGroup(
                            items = listOf(AccountType.CASH, AccountType.BANK, AccountType.DEBIT, AccountType.CC),
                            selectedIndex = listOf(AccountType.CASH, AccountType.BANK, AccountType.DEBIT, AccountType.CC).indexOf(selectedType),
                            onItemSelected = { selectedType = listOf(AccountType.CASH, AccountType.BANK, AccountType.DEBIT, AccountType.CC)[it] },
                            modifier = Modifier.weight(1f),
                            isScrollable = true,
                            labelProvider = {
                                when (it) {
                                    AccountType.CASH -> stringResource(R.string.acc_type_cash)
                                    AccountType.BANK -> stringResource(R.string.ui_main_bank_short)
                                    AccountType.DEBIT -> stringResource(R.string.ui_main_debit_short)
                                    AccountType.CC -> stringResource(R.string.ui_main_credit_short)
                                }
                            }
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = initialBalanceText,
                            onValueChange = { initialBalanceText = it },
                            placeholder = { Text(if (selectedType == AccountType.CC) stringResource(R.string.ui_main_credit_limit_hint) else stringResource(R.string.ui_main_initial_balance_hint)) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        val addAccountInteraction = remember { MutableInteractionSource() }
                        CompletePressButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                val name = newAssetName.trim()
                                val bal = initialBalanceText.toDoubleOrNull() ?: 0.0
                                if (name.isNotEmpty()) {
                                    val newAccount = AccountEntity(
                                        name = name,
                                        type = selectedType,
                                        balance = if (selectedType == AccountType.CC) 0.0 else bal,
                                        icon = when (selectedType) {
                                            AccountType.CASH -> "wallet"
                                            AccountType.BANK -> "bank"
                                            AccountType.DEBIT -> "debit_card"
                                            AccountType.CC -> "credit_card"
                                        },
                                        creditLimit = if (selectedType == AccountType.CC) bal else null
                                    )
                                    onIntent(com.example.vibefinance.ui.FinanceIntent.SaveAccount(newAccount))
                                }
                                newAssetName = ""
                                initialBalanceText = ""
                                showInlineAddAsset = false
                            },
                            shapes = ButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                            interactionSource = addAccountInteraction,
                            modifier = Modifier.pressBounce(interactionSource = addAccountInteraction)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.btn_save))
                        }
                    }
                }
            }
            
            ExpandableEntrySelector(
                items = sortedAccounts,
                title = if (isTransfer) stringResource(R.string.transfer_from) else stringResource(R.string.ui_main_asset_selection),
                tagPrefix = "EntryAccount",
                keyProvider = { it.id.toString() },
                subtitleProvider = { accountTypeLabel(it.type) },
                selectedIndex = sortedAccounts.indexOfFirst { it.id == selectedAccount?.id },
                onItemSelected = { index ->
                    selectedAccount = sortedAccounts[index]
                    if (isTransfer && selectedToAccount?.id == selectedAccount?.id) selectedToAccount = null
                },
                iconProvider = { acc, tintColor ->
                    val accIcon = when (acc.type) {
                        AccountType.CASH -> Icons.Default.Savings
                        AccountType.BANK -> Icons.Default.AccountBalance
                        AccountType.DEBIT -> Icons.Default.CreditCard
                        AccountType.CC -> Icons.Default.ShoppingCart
                    }
                    Icon(
                        imageVector = accIcon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = tintColor
                    )
                },
                onAdd = { showInlineAddAsset = !showInlineAddAsset },
                labelProvider = { acc -> acc.nickname?.takeIf { it.isNotBlank() } ?: acc.name }
            )
        }

        // 4. Destination Account for internal transfer
        AnimatedVisibility(
            visible = isTransfer,
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + expandVertically(
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
                expandFrom = Alignment.Top
            ),
            exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + shrinkVertically(
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
                shrinkTowards = Alignment.Top
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth().clipToBounds()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.ui_main_destination_account),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val destAccounts = remember(sortedAccounts, selectedAccount) {
                    sortedAccounts.filter { it.id != selectedAccount?.id }
                }
                ExpandableEntrySelector(
                    items = destAccounts,
                    title = stringResource(R.string.ui_main_destination_account),
                    tagPrefix = "EntryDestination",
                    keyProvider = { it.id.toString() },
                    subtitleProvider = { accountTypeLabel(it.type) },
                    selectedIndex = destAccounts.indexOfFirst { it.id == selectedToAccount?.id },
                    onItemSelected = { index -> selectedToAccount = destAccounts[index] },
                    iconProvider = { acc, tintColor ->
                        val accIcon = when (acc.type) {
                            AccountType.CASH -> Icons.Default.Savings
                            AccountType.BANK -> Icons.Default.AccountBalance
                            AccountType.DEBIT -> Icons.Default.CreditCard
                            AccountType.CC -> Icons.Default.ShoppingCart
                        }
                        Icon(
                            imageVector = accIcon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = tintColor
                        )
                    },
                    labelProvider = { acc -> acc.nickname?.takeIf { it.isNotBlank() } ?: acc.name }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 5. Buckwheat-Style Custom Numeric Keypad
        val buttonHeight = 50.dp
        val gap = 6.dp

        Column(
            verticalArrangement = Arrangement.spacedBy(gap),
            modifier = Modifier.fillMaxWidth().testTag("EntryKeypad").onGloballyPositioned {
                selectorOverlay.keypadBounds = it.boundsInWindow()
            }
        ) {
            // Row 1: 7, 8, 9, Backspace
            Row(
                horizontalArrangement = Arrangement.spacedBy(gap),
                modifier = Modifier.fillMaxWidth().height(buttonHeight)
            ) {
                listOf("7", "8", "9").forEach { num ->
                    KeyboardButton(
                        modifier = Modifier.weight(1f),
                        type = KeyboardButtonType.DEFAULT,
                        text = num,
                        onClick = {
                            if (typedAmount == "0" || typedAmount == "0.00" || typedAmount == "0.0") {
                                typedAmount = num
                            } else {
                                if (typedAmount.contains(".")) {
                                    val parts = typedAmount.split(".")
                                    if (parts.size > 1 && parts[1].length >= 2) return@KeyboardButton
                                }
                                typedAmount += num
                            }
                        }
                    )
                }
                KeyboardButton(
                    modifier = Modifier.weight(1f),
                    type = KeyboardButtonType.SECONDARY,
                    icon = rememberVectorPainter(Icons.Default.Backspace),
                    onClick = {
                        if (typedAmount.length > 1) {
                            typedAmount = typedAmount.dropLast(1)
                            if (typedAmount.endsWith(".") && typedAmount.length == 1) {
                                typedAmount = "0"
                            }
                        } else {
                            typedAmount = "0"
                        }
                    },
                    onLongClick = {
                        typedAmount = "0"
                    }
                )
            }

            // Rows 2-4: Left area (4-6, 1-3, 0-dot) and Right area (Apply Check)
            Row(
                horizontalArrangement = Arrangement.spacedBy(gap),
                modifier = Modifier.fillMaxWidth().height(buttonHeight * 3 + gap * 2)
            ) {
                // Left Column (4-6, 1-3, 0-dot)
                Column(
                    verticalArrangement = Arrangement.spacedBy(gap),
                    modifier = Modifier.weight(3f).fillMaxHeight()
                ) {
                    // Row 2: 4, 5, 6
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(gap),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        listOf("4", "5", "6").forEach { num ->
                            KeyboardButton(
                                modifier = Modifier.weight(1f),
                                type = KeyboardButtonType.DEFAULT,
                                text = num,
                                onClick = {
                                    if (typedAmount == "0" || typedAmount == "0.00" || typedAmount == "0.0") {
                                        typedAmount = num
                                    } else {
                                        if (typedAmount.contains(".")) {
                                            val parts = typedAmount.split(".")
                                            if (parts.size > 1 && parts[1].length >= 2) return@KeyboardButton
                                        }
                                        typedAmount += num
                                    }
                                }
                            )
                        }
                    }

                    // Row 3: 1, 2, 3
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(gap),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        listOf("1", "2", "3").forEach { num ->
                            KeyboardButton(
                                modifier = Modifier.weight(1f),
                                type = KeyboardButtonType.DEFAULT,
                                text = num,
                                onClick = {
                                    if (typedAmount == "0" || typedAmount == "0.00" || typedAmount == "0.0") {
                                        typedAmount = num
                                    } else {
                                        if (typedAmount.contains(".")) {
                                            val parts = typedAmount.split(".")
                                            if (parts.size > 1 && parts[1].length >= 2) return@KeyboardButton
                                        }
                                        typedAmount += num
                                    }
                                }
                            )
                        }
                    }

                    // Row 4: 0, .
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(gap),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        KeyboardButton(
                            modifier = Modifier.weight(2f),
                            type = KeyboardButtonType.DEFAULT,
                            text = "0",
                            onClick = {
                                if (typedAmount != "0") {
                                    if (typedAmount.contains(".")) {
                                        val parts = typedAmount.split(".")
                                        if (parts.size > 1 && parts[1].length >= 2) return@KeyboardButton
                                    }
                                    typedAmount += "0"
                                }
                            }
                        )
                        KeyboardButton(
                            modifier = Modifier.weight(1f),
                            type = KeyboardButtonType.DEFAULT,
                            text = ".",
                            onClick = {
                                if (!typedAmount.contains(".")) {
                                    typedAmount += "."
                                }
                            }
                        )
                    }
                }

                // Right Column (Tall Apply/Check Button)
                val amt = typedAmount.toDoubleOrNull()
                val isApplyEnabled = amt != null && amt.isFinite() && amt > 0.0 && convertedAmount != null &&
                    (selectedCurrency == "HKD" || (!rateLoading && rateQuote != null)) && selectedAccount?.id != null &&
                    (!isTransfer || (selectedToAccount != null && selectedToAccount?.id != selectedAccount?.id))
                KeyboardButton(
                    modifier = Modifier.weight(1f).fillMaxHeight().alpha(if (isApplyEnabled) 1f else 0.5f).testTag("EntrySubmit"),
                    type = KeyboardButtonType.PRIMARY,
                    icon = rememberVectorPainter(Icons.Default.Check),
                    enabled = isApplyEnabled,
                    onClick = {
                        val accId = selectedAccount?.id
                        if (isApplyEnabled && accId != null && amt != null) {
                            val finalHkdAmt = convertedAmount
                            val descWithFx = if (selectedCurrency != "HKD") {
                                val origStr = com.example.vibefinance.util.CurrencyEngine.formatOriginal(amt, selectedCurrency)
                                val quote = rateQuote ?: return@KeyboardButton
                                val detail = "$origStr; 1 ${quote.currencyCode} = ${quote.rateText()} HKD; ${quote.source} ${quote.rateDate}"
                                "${descriptionText.ifEmpty { if (isTransfer) "Transfer" else if (transactionMode == TransactionMode.INCOME) "Income" else "Purchase" }} ($detail)"
                            } else {
                                descriptionText.ifEmpty { "Purchase" }
                            }

                            if (transactionMode == TransactionMode.TRANSFER && selectedToAccount != null) {
                                onIntent(
                                    com.example.vibefinance.ui.FinanceIntent.AddTransaction(
                                        amount = finalHkdAmt,
                                        category = "Transfer",
                                        accountId = accId,
                                        toAccountId = selectedToAccount?.id,
                                        isExcludedFromDailyBudget = true,
                                        description = if (selectedCurrency != "HKD") descWithFx else descriptionText.ifEmpty { "CC Payment Transfer" }
                                    )
                                )
                            } else if (transactionMode == TransactionMode.INCOME) {
                                onIntent(
                                    com.example.vibefinance.ui.FinanceIntent.AddTransaction(
                                        amount = -finalHkdAmt,
                                        category = incomeCategoryText.ifEmpty { "Income" },
                                        accountId = accId,
                                        description = descWithFx
                                    )
                                )
                            } else {
                                onIntent(
                                    com.example.vibefinance.ui.FinanceIntent.AddTransaction(
                                        amount = finalHkdAmt,
                                        category = expenseCategoryText,
                                        accountId = accId,
                                        description = descWithFx,
                                        isExcludedFromDailyBudget = !isDailyBudget,
                                        customIcon = expenseIcon
                                    )
                                )
                            }
                            onDismissAddDialog()
                        }
                    }
                )
            }
        }

            Spacer(modifier = Modifier.height(24.dp))
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(10f)
                .background(MaterialTheme.colorScheme.surface)
                .onGloballyPositioned { headerHeightPx = it.size.height }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismissAddDialog) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.nav_back),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                AnimatedContent(
                    targetState = transactionMode,
                    transitionSpec = {
                        (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + 
                         slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { -it / 2 })
                            .togetherWith(fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)))
                    },
                    label = "titleTransition"
                ) { mode ->
                    Text(
                        text = when (mode) {
                            TransactionMode.INCOME -> stringResource(R.string.quick_add_income)
                            TransactionMode.TRANSFER -> stringResource(R.string.quick_transfer)
                            else -> stringResource(R.string.quick_add_expense)
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                val dynamicFontSize = (28 * (1f - 0.3f * fraction)).sp
                Surface(
                    color = containerColor,
                    contentColor = labelColor,
                    shape = RoundedCornerShape(percent = 50),
                    tonalElevation = (4 * fraction).dp,
                    shadowElevation = (4 * fraction).dp,
                    modifier = Modifier
                        .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 4.dp
                        )
                    ) {
                        AnimatedVisibility(
                            visible = fraction > 0.5f,
                            enter = fadeIn() + scaleIn(initialScale = 0.5f),
                            exit = fadeOut() + scaleOut(targetScale = 0.5f)
                        ) {
                            Row {
                                Icon(
                                    imageVector = Icons.Default.Paid,
                                    contentDescription = stringResource(R.string.ui_main_amount_icon),
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                        }
                        
                        Text(
                            text = if (selectedCurrency == "HKD") "$" else
                                com.example.vibefinance.util.CurrencyEngine.supportedCurrencies.first { it.code == selectedCurrency }.symbol,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = dynamicFontSize),
                            color = dollarColor,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        com.example.vibefinance.ui.components.SpringHarmonicNumberText(
                            text = typedAmount,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = dynamicFontSize),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Box(modifier = Modifier.padding(start = 6.dp)) {
                    var showCurrencyMenu by remember { mutableStateOf(false) }
                    val currInfo = com.example.vibefinance.util.CurrencyEngine.supportedCurrencies.find { it.code == selectedCurrency }
                        ?: com.example.vibefinance.util.CurrencyEngine.supportedCurrencies.first()

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        onClick = { showCurrencyMenu = !showCurrencyMenu },
                        modifier = Modifier.testTag("EntryCurrency")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${currInfo.flagEmoji} ${currInfo.code}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showCurrencyMenu,
                        onDismissRequest = { showCurrencyMenu = false }
                    ) {
                        com.example.vibefinance.util.CurrencyEngine.supportedCurrencies.forEach { curr ->
                            DropdownMenuItem(
                                text = { Text("${curr.flagEmoji} ${curr.code} (${curr.symbol})") },
                                modifier = Modifier.testTag("EntryCurrency_${curr.code}"),
                                onClick = {
                                    selectedCurrency = curr.code
                                    showCurrencyMenu = false
                                }
                            )
                        }

                    }
                }
            }
        }
    }
}
}

private fun Modifier.layoutSize(
    widthState: () -> androidx.compose.ui.unit.Dp,
    heightState: () -> androidx.compose.ui.unit.Dp
) = this.layout { measurable, constraints ->
    val w = widthState().roundToPx()
    val h = heightState().roundToPx()
    val placeable = measurable.measure(androidx.compose.ui.unit.Constraints.fixed(w, h))
    layout(w, h) {
        placeable.place(0, 0)
    }
}

@Composable
fun AllowedInterceptAppsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val selectedApps by InMemoryDatabase.selectedInterceptApps.collectAsStateWithLifecycle()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    var searchQuery by remember { mutableStateOf("") }
    val installedApps = remember(context) { com.example.vibefinance.util.LocalAppManager.getInstalledApps(context) }

    val filteredApps = remember(searchQuery, installedApps) {
        if (searchQuery.isBlank()) {
            installedApps
        } else {
            installedApps.filter {
                it.appName.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.example.vibefinance.R.string.allowed_apps_dialog_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(com.example.vibefinance.R.string.allowed_apps_dialog_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(androidx.compose.ui.res.stringResource(com.example.vibefinance.R.string.allowed_apps_search_hint), fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.ui_main_search),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.ui_main_clear),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filteredApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.example.vibefinance.R.string.allowed_apps_no_matches),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    filteredApps.forEach { appInfo ->
                        val isChecked = selectedApps.contains(appInfo.packageName)

                        Surface(
                            onClick = { InMemoryDatabase.toggleInterceptApp(appInfo.packageName) },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                            border = BorderStroke(
                                1.dp,
                                if (isChecked) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (appInfo.iconBitmap != null) {
                                        Image(
                                            bitmap = appInfo.iconBitmap.asImageBitmap(),
                                            contentDescription = appInfo.appName,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(9.dp))
                                                .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(9.dp))
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = appInfo.appName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = appInfo.packageName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Switch(
                                    checked = isChecked,
                                    onCheckedChange = { InMemoryDatabase.toggleInterceptApp(appInfo.packageName) },
                                    thumbContent = if (isChecked) {
                                        {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(SwitchDefaults.IconSize)
                                            )
                                        }
                                    } else null
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val confirmInteraction = remember { MutableInteractionSource() }
            val haptic = LocalHapticFeedback.current
            CompletePressButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onDismiss()
                },
                shapes = ButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                interactionSource = confirmInteraction,
                modifier = Modifier.pressBounce(interactionSource = confirmInteraction)
            ) {
                Text(androidx.compose.ui.res.stringResource(com.example.vibefinance.R.string.btn_confirm))
            }
        }
    )
}
