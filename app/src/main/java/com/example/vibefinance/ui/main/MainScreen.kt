package com.example.vibefinance.ui.main

import com.example.vibefinance.ui.home.RecalcBudgetSheet
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
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
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.outlined.AutoMode
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.layout
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.example.vibefinance.ui.components.KeyboardButton
import com.example.vibefinance.ui.components.KeyboardButtonType
import com.example.vibefinance.ui.components.WheelNumberPicker
import com.example.vibefinance.ui.components.GlassmorphicCard
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
import androidx.compose.runtime.mutableStateListOf
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

import com.example.vibefinance.ui.radar.RadarScreen
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.outlined.GpsFixed

enum class TabItem { HOME, ACCOUNTS, RADAR, RECURRING, HISTORY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var selectedTab by rememberSaveable { mutableStateOf(TabItem.HOME) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    val addExpenseSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }

    // Scroll state tracking
    val density = LocalDensity.current
    val topBarHeight = 120.dp
    val bottomBarHeight = 100.dp

    val topBarHeightPx = remember(density) { with(density) { topBarHeight.toPx() } }
    val bottomBarHeightPx = remember(density) { with(density) { bottomBarHeight.toPx() } }

    var topBarOffsetHeightPx by remember { mutableStateOf(0f) }
    var bottomBarOffsetHeightPx by remember { mutableStateOf(0f) }

    var isFabExpanded by remember { mutableStateOf(true) }
    var isNavBarVisible by remember { mutableStateOf(true) }

    val navBarOffsetY by animateDpAsState(
        targetValue = if (isNavBarVisible) 0.dp else 140.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "navBarOffsetY"
    )

    val budgetInfoState = state.budgetInfo
    val spentToday = if (budgetInfoState != null) budgetInfoState.dailyAllowance - budgetInfoState.dailyRemaining else 0.0
    val ratioSpent = if (budgetInfoState != null && budgetInfoState.dailyAllowance > 0) spentToday / budgetInfoState.dailyAllowance else 0.0
    val glowColorAnimated by animateColorAsState(
        targetValue = when {
            ratioSpent <= 0.5 -> MaterialTheme.colorScheme.primary
            ratioSpent <= 1.0 -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.tertiary
        },
        animationSpec = tween(durationMillis = 600),
        label = "glowColor"
    )

    val nestedScrollConnection = remember(showAddDialog) {
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
 
                topBarOffsetHeightPx = 0f
                bottomBarOffsetHeightPx = 0f
 
                return Offset.Zero
            }
 
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                topBarOffsetHeightPx = 0f
                bottomBarOffsetHeightPx = 0f
                return super.onPostFling(consumed, available)
            }
        }
    }

    // Reset visibility states when changing tabs
    LaunchedEffect(selectedTab) {
        topBarOffsetHeightPx = 0f
        bottomBarOffsetHeightPx = 0f
        isFabExpanded = true
    }

    val budgetInfo = state.budgetInfo ?: DailyBudgetInfo(
        totalMonthlyBudget = 1500.0,
        totalSpentThisMonth = 0.0,
        monthlyRemaining = 1500.0,
        dailyAllowance = 50.0,
        dailyRemaining = 50.0,
        daysLeft = 30,
        startDate = System.currentTimeMillis(),
        endDate = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
    )

    var showRecalcSheet by remember { mutableStateOf(false) }
    var isRecalcSheetMandatory by remember { mutableStateOf(false) }
    var showNewPeriodSheet by remember { mutableStateOf(false) }

    val isPeriodEnded = budgetInfo.totalMonthlyBudget > 0 && (budgetInfo.daysLeft <= 0 || (budgetInfo.endDate > 0 && System.currentTimeMillis() >= budgetInfo.endDate))

    // Automatic New-Day Rollover Recalculation & Period Ended Trigger
    // Waits for app launch/loading to complete, then adds an 800ms smooth delay so the splash screen is gone and user sees Daily page first
    val prefs = remember(context) { context.getSharedPreferences("vibefinance_prefs", android.content.Context.MODE_PRIVATE) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val autoSheetScope = rememberCoroutineScope()

    var hasAutoTriggeredForSession by remember { mutableStateOf(false) }

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
                    hasAutoTriggeredForSession = true
                    isRecalcSheetMandatory = true
                    showRecalcSheet = true
                }
            }
        }
    }

    LaunchedEffect(state.isLoading, isPeriodEnded) {
        if (!state.isLoading) {
            checkAndTriggerAutoSheets()
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
            floatingActionButton = {
                AnimatedVisibility(
                    visible = selectedTab == TabItem.HOME && !showAddDialog,
                    enter = fadeIn(animationSpec = tween(durationMillis = 250)) +
                            scaleIn(
                                initialScale = 0.4f,
                                transformOrigin = TransformOrigin(1.0f, 1.0f),
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
                            ) +
                            slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            ),
                    exit = fadeOut(animationSpec = tween(durationMillis = 200)) +
                           scaleOut(
                               targetScale = 0.4f,
                               transformOrigin = TransformOrigin(1.0f, 1.0f),
                               animationSpec = tween(durationMillis = 200)
                           ) +
                           slideOutVertically(
                               targetOffsetY = { it / 2 },
                               animationSpec = tween(durationMillis = 200)
                           )
                ) {
                    val contentColor = if (glowColorAnimated == MaterialTheme.colorScheme.primary) {
                        MaterialTheme.colorScheme.onPrimary
                    } else if (glowColorAnimated == MaterialTheme.colorScheme.secondary) {
                        MaterialTheme.colorScheme.onSecondary
                    } else {
                        MaterialTheme.colorScheme.onTertiary
                    }

                    ExtendedFloatingActionButton(
                        onClick = { showAddDialog = true },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Transaction",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        text = {
                            Text(
                                text = "Add",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        expanded = isFabExpanded,
                        containerColor = glowColorAnimated,
                        contentColor = contentColor,
                        shape = RoundedCornerShape(16.dp),
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                        modifier = Modifier.graphicsLayer {
                            translationY = with(density) { navBarOffsetY.toPx() }
                        }
                    )
                }
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            translationY = with(density) { navBarOffsetY.toPx() }
                        }
                ) {
                    var isNavBarAppEntered by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(750) // Synchronized with Material You Splash Exit
                        isNavBarAppEntered = true
                    }

                    NavigationBar(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.96f),
                        tonalElevation = 6.dp
                    ) {
                        val tabs = listOf(
                            TabItem.HOME to Triple("Daily", Icons.Filled.Home, Icons.Outlined.Home),
                            TabItem.ACCOUNTS to Triple("Assets", Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance),
                            TabItem.RECURRING to Triple("Recurring", Icons.Filled.AutoMode, Icons.Outlined.AutoMode),
                            TabItem.HISTORY to Triple("History", Icons.AutoMirrored.Filled.List, Icons.AutoMirrored.Outlined.List)
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
                val isAnySheetOpen = showRecalcSheet || showNewPeriodSheet || showAddDialog
                val sheetDepthScale by animateFloatAsState(
                    targetValue = if (isAnySheetOpen) 0.95f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    label = "sheetDepthScale"
                )
                val sheetDepthCorner by animateDpAsState(
                    targetValue = if (isAnySheetOpen) 24.dp else 0.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
                    label = "sheetDepthCorner"
                )

                // 1. Screen content (takes up whole screen, padded horizontally)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .graphicsLayer {
                            scaleX = sheetDepthScale
                            scaleY = sheetDepthScale
                            clip = isAnySheetOpen || sheetDepthCorner > 0.dp
                            shape = RoundedCornerShape(sheetDepthCorner.coerceAtLeast(0.dp))
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
                        modifier = Modifier.fillMaxSize().verticalFadingEdge(),
                        label = "tabChangeTopLevel"
                    ) { targetTab ->
                        when (targetTab) {
                            TabItem.HOME -> HomeScreen(
                                state = state,
                                onIntent = viewModel::dispatch,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                onViewAllClick = { selectedTab = TabItem.HISTORY },
                                showAddDialog = showAddDialog,
                                onDismissAddDialog = { showAddDialog = false },
                                onOpenBudgetDialog = {
                                    if (isPeriodEnded) {
                                        showNewPeriodSheet = true
                                    } else {
                                        showBudgetDialog = true
                                    }
                                },
                                onOpenRecalcSheet = {
                                    if (isPeriodEnded) {
                                        showNewPeriodSheet = true
                                    } else {
                                        isRecalcSheetMandatory = false
                                        showRecalcSheet = true
                                    }
                                }
                            )
                            TabItem.ACCOUNTS -> AccountsScreen(
                                state = state,
                                onIntent = viewModel::dispatch,
                                modifier = Modifier.fillMaxSize()
                            )
                            TabItem.RADAR -> RadarScreen(
                                state = state,
                                onIntent = viewModel::dispatch,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                onLogSpendClick = { shopName, aspect, cardId ->
                                    showAddDialog = true
                                }
                            )
                            TabItem.RECURRING -> RecurringScreen(
                                state = state,
                                onIntent = viewModel::dispatch,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            TabItem.HISTORY -> HistoryScreen(
                                state = state,
                                onIntent = viewModel::dispatch,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }

                // 1.5. Floating Pill Top Bar (Permanently Pinned on Screen)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .zIndex(10f)
                        .padding(start = 20.dp, end = 20.dp, top = 8.dp)
                        .statusBarsPadding()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.96f),
                        tonalElevation = 10.dp,
                        shadowElevation = 12.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Brand & Tab Title Pill Section
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Vibe Finance Glowing Brand Badge
                                Surface(
                                    modifier = Modifier.size(36.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Savings,
                                            contentDescription = "Vibe Finance Logo",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Column {
                                    AnimatedContent(
                                        targetState = selectedTab,
                                        transitionSpec = {
                                            (fadeIn(animationSpec = tween(220)) + slideInVertically { -it / 2 })
                                                .togetherWith(fadeOut(animationSpec = tween(220)) + slideOutVertically { it / 2 })
                                        },
                                        label = "topBarTitle"
                                    ) { currentTab ->
                                        val titleText = when (currentTab) {
                                            TabItem.HOME -> "Vibe Overview"
                                            TabItem.ACCOUNTS -> "Assets & Cards"
                                            TabItem.RADAR -> "Discount Radar"
                                            TabItem.RECURRING -> "Subscriptions"
                                            TabItem.HISTORY -> "Activity History"
                                        }
                                        Text(
                                            text = titleText,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                    }
                                    
                                    val subtitleText = when (selectedTab) {
                                        TabItem.HOME -> "Daily Flow"
                                        TabItem.ACCOUNTS -> "${state.accounts.size} Cards & Assets"
                                        TabItem.RADAR -> "${state.discountShops.size} Nearby Partners"
                                        TabItem.RECURRING -> "${state.subscriptions.size} Active"
                                        TabItem.HISTORY -> "${state.transactions.size} Logged"
                                    }
                                    Text(
                                        text = subtitleText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                        maxLines = 1
                                    )
                                }
                            }

                            // Right Action Icons Section (Theme Toggle + Settings Action)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {

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
                                    targetValue = if (isDarkTheme) Color(0xFFFFD54F) else Color(0xFF5C6BC0),
                                    animationSpec = tween(500),
                                    label = "themeIconTint"
                                )

                                val buttonBgColor by animateColorAsState(
                                    targetValue = if (isDarkTheme) Color(0xFF2A2B2D).copy(alpha = 0.9f) else Color(0xFFE2E8F0).copy(alpha = 0.9f),
                                    animationSpec = tween(500),
                                    label = "themeButtonBg"
                                )

                                // Animated Light / Dark Mode Toggle Icon Button with Moon-to-Sun rotation & morph
                                Surface(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            val nextMode = if (isDarkTheme) ThemeMode.LIGHT else ThemeMode.DARK
                                            viewModel.dispatch(FinanceIntent.SetThemeMode(nextMode))
                                        },
                                    shape = CircleShape,
                                    color = buttonBgColor,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AnimatedContent(
                                            targetState = isDarkTheme,
                                            transitionSpec = {
                                                (fadeIn(animationSpec = tween(300)) + scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)))
                                                    .togetherWith(fadeOut(animationSpec = tween(200)) + scaleOut(animationSpec = tween(200)))
                                            },
                                            label = "moonSunIconSwitch"
                                        ) { isDark ->
                                            Icon(
                                                imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                                contentDescription = "Theme Toggle",
                                                tint = iconTint,
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .graphicsLayer {
                                                        rotationZ = iconRotation
                                                    }
                                            )
                                        }
                                    }
                                }

                                // Settings & Budget Dialog Launcher
                                Surface(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            showBudgetDialog = true
                                        },
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Settings & Budget",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Official Material 3 Navigation Bar with M3 Motion: Slides Off and On Screen during a scroll






                // 5. Standard Modal Bottom Sheet for Add Expense
                if (showAddDialog) {
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
                            modifier = Modifier.statusBarsPadding()
                        )
                    }
                }


            }
        }
    }

    // M3 DatePicker BUDGET SELECTOR DIALOG
    if (showBudgetDialog) {
        var budgetAmountText by remember {
            val currentAmt = budgetInfo.totalMonthlyBudget
            mutableStateOf(if (currentAmt > 0) String.format(Locale.US, "%.2f", currentAmt) else "")
        }
        var selectedEndDateMillis by remember {
            mutableStateOf<Long?>(budgetInfo.endDate.takeIf { it > 0 })
        }
        var selectedRolloverMode by remember {
            mutableStateOf(budgetInfo.rolloverMode)
        }
        var showDatePickerModal by remember { mutableStateOf(false) }

        // Category Limits mutable text states
        var foodLimitText by remember { mutableStateOf(state.categoryLimits["Food"]?.let { String.format(Locale.US, "%.2f", it) } ?: "") }
        var transportLimitText by remember { mutableStateOf(state.categoryLimits["Transport"]?.let { String.format(Locale.US, "%.2f", it) } ?: "") }
        var shoppingLimitText by remember { mutableStateOf(state.categoryLimits["Shopping"]?.let { String.format(Locale.US, "%.2f", it) } ?: "") }
        var utilitiesLimitText by remember { mutableStateOf(state.categoryLimits["Utilities"]?.let { String.format(Locale.US, "%.2f", it) } ?: "") }
        var otherLimitText by remember { mutableStateOf(state.categoryLimits["Other"]?.let { String.format(Locale.US, "%.2f", it) } ?: "") }

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
                        // Custom Top Bar styled like Buckwheat
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
                                    contentDescription = "Close"
                                )
                            }
                            
                            Button(
                                onClick = {
                                    val end = calendarState.calendarUiState.value.selectedEndDate
                                    if (end != null) {
                                        selectedEndDateMillis = end.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                    }
                                    showDatePickerModal = false
                                },
                                enabled = calendarState.calendarUiState.value.hasSelectedDates,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Apply",
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                                Text("Apply")
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
                                "Select Finish Date"
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
                                    text = "$daysCount days total",
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

        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Settings",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showBudgetDialog = false }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close settings"
                        )
                    }
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // --- SECTION 1: APPEARANCE ---
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "APPEARANCE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "Theme Mode",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        
                        val themeOptions = listOf("System", "Light", "Dark")
                        val currentThemeIndex = when (state.themeMode) {
                            ThemeMode.SYSTEM -> 0
                            ThemeMode.LIGHT -> 1
                            ThemeMode.DARK -> 2
                        }
                        
                        ExpressiveSegmentedButtonGroup(
                            items = themeOptions,
                            selectedIndex = currentThemeIndex,
                            onItemSelected = { index ->
                                val selectedMode = when (index) {
                                    0 -> ThemeMode.SYSTEM
                                    1 -> ThemeMode.LIGHT
                                    else -> ThemeMode.DARK
                                }
                                viewModel.dispatch(FinanceIntent.SetThemeMode(selectedMode))
                            },
                            labelProvider = { it }
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        // Dynamic Color Switch Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .clickable {
                                    viewModel.dispatch(FinanceIntent.SetDynamicColorEnabled(!state.dynamicColorEnabled))
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Palette,
                                    contentDescription = "Dynamic Color",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Dynamic Colors (Material You)",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "System coloring (Android 12+)",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            Switch(
                                checked = state.dynamicColorEnabled,
                                onCheckedChange = { enabled ->
                                    viewModel.dispatch(FinanceIntent.SetDynamicColorEnabled(enabled))
                                },
                                thumbContent = if (state.dynamicColorEnabled) {
                                    {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize)
                                        )
                                    }
                                } else {
                                    null
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Notification Logging Switch Row
                        var autoLogEnabled by remember {
                            mutableStateOf(
                                InMemoryDatabase.isNotificationLoggingEnabled &&
                                androidx.core.app.NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
                            )
                        }

                        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
                        DisposableEffect(lifecycleOwner) {
                            val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                                if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                                    val isGranted = androidx.core.app.NotificationManagerCompat.getEnabledListenerPackages(context)
                                        .contains(context.packageName)
                                    autoLogEnabled = InMemoryDatabase.isNotificationLoggingEnabled && isGranted
                                }
                            }
                            lifecycleOwner.lifecycle.addObserver(observer)
                            onDispose {
                                lifecycleOwner.lifecycle.removeObserver(observer)
                            }
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .clickable {
                                    val isGranted = androidx.core.app.NotificationManagerCompat.getEnabledListenerPackages(context)
                                        .contains(context.packageName)
                                    if (!isGranted) {
                                        try {
                                            context.startActivity(android.content.Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").apply {
                                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                            })
                                        } catch (e: Exception) {
                                            // Fallback
                                        }
                                    } else {
                                        val target = !autoLogEnabled
                                        InMemoryDatabase.isNotificationLoggingEnabled = target
                                        autoLogEnabled = target
                                    }
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.NotificationsActive,
                                    contentDescription = "Notification Logging",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Automatic Transaction Logging",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Log from Google & Samsung Wallet notifications",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            Switch(
                                checked = autoLogEnabled,
                                onCheckedChange = { checked ->
                                    val isGranted = androidx.core.app.NotificationManagerCompat.getEnabledListenerPackages(context)
                                        .contains(context.packageName)
                                    if (!isGranted) {
                                        try {
                                            context.startActivity(android.content.Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").apply {
                                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                            })
                                        } catch (e: Exception) {
                                            // Fallback
                                        }
                                    } else {
                                        InMemoryDatabase.isNotificationLoggingEnabled = checked
                                        autoLogEnabled = checked
                                    }
                                },
                                thumbContent = if (autoLogEnabled) {
                                    {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize)
                                        )
                                    }
                                } else {
                                    null
                                }
                            )
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )
                    
                    // --- SECTION 2: BUDGET LIMITS ---
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "BUDGET PERIOD CONFIGURATION",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        
                        OutlinedTextField(
                            value = budgetAmountText,
                            onValueChange = { budgetAmountText = it },
                            textStyle = MaterialTheme.typography.headlineMedium.copy(
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            ),
                            prefix = { Text("$", style = MaterialTheme.typography.headlineMedium) },
                            label = { Text("Budget Limit") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        val today = LocalDate.now()
                        val endLocalDate = selectedEndDateMillis?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                        val days = if (endLocalDate != null) {
                            (ChronoUnit.DAYS.between(today, endLocalDate) + 1).coerceAtLeast(1).toInt()
                        } else {
                            0
                        }
                        val endTextFormatted = endLocalDate?.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)) ?: "Select End Date"
                        val buttonText = if (days > 0) {
                            "Ends on $endTextFormatted ($days days)"
                        } else {
                            endTextFormatted
                        }
                        
                        Button(
                            onClick = { showDatePickerModal = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select End Date",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = buttonText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )

                    // --- BUCKWHEAT ROLLOVER MODE SELECTOR ---
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "BUDGET RECALCULATION MODE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "Choose how unused budget or overspending from previous days is recalculated.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        
                        // Option 1: Distribute Evenly
                        val isDistribute = selectedRolloverMode == com.example.vibefinance.data.entity.RolloverMode.DISTRIBUTE_EVENLY
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isDistribute) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = BorderStroke(if (isDistribute) 1.5.dp else 1.dp, if (isDistribute) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedRolloverMode = com.example.vibefinance.data.entity.RolloverMode.DISTRIBUTE_EVENLY }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isDistribute,
                                    onClick = { selectedRolloverMode = com.example.vibefinance.data.entity.RolloverMode.DISTRIBUTE_EVENLY }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Distribute Over Remaining Days (Default)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Recalculates and spreads past leftover or overspend evenly over all remaining days in period.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                        
                        // Option 2: Add to Next Day
                        val isAddNext = selectedRolloverMode == com.example.vibefinance.data.entity.RolloverMode.ADD_TO_NEXT_DAY
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isAddNext) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = BorderStroke(if (isAddNext) 1.5.dp else 1.dp, if (isAddNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedRolloverMode = com.example.vibefinance.data.entity.RolloverMode.ADD_TO_NEXT_DAY }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isAddNext,
                                    onClick = { selectedRolloverMode = com.example.vibefinance.data.entity.RolloverMode.ADD_TO_NEXT_DAY }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Add to Tomorrow's Daily Budget",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Adds yesterday's unused leftover directly into today's daily budget allowance.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )
                    
                    // --- CATEGORY SPENDING CAPS ---
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "CATEGORY SPENDING CAPS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "Set monthly spending limits for transaction categories (leave blank to disable)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        
                        val categoriesList = listOf(
                            Triple("Food", foodLimitText, { text: String -> foodLimitText = text }),
                            Triple("Transport", transportLimitText, { text: String -> transportLimitText = text }),
                            Triple("Shopping", shoppingLimitText, { text: String -> shoppingLimitText = text }),
                            Triple("Utilities", utilitiesLimitText, { text: String -> utilitiesLimitText = text }),
                            Triple("Other", otherLimitText, { text: String -> otherLimitText = text })
                        )
                        
                        categoriesList.forEach { (catName, limitText, setLimitText) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = catName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = limitText,
                                    onValueChange = setLimitText,
                                    placeholder = { Text("No limit", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))) },
                                    prefix = { Text("$", style = MaterialTheme.typography.bodyMedium) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.width(130.dp),
                                    textStyle = MaterialTheme.typography.bodyMedium,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                )
                            }
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )
                    
                    // --- SECTION: ROLLING PERIOD COMPARISON ---
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "BUDGET PERIOD PACING COMPARISON",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "Visualizes cumulative expenses against target pacing and previous month performance.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        
                        // Calculations
                        val budgetStart = budgetInfo.startDate.takeIf { it > 0 } ?: LocalDate.now().minusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        val budgetEnd = budgetInfo.endDate.takeIf { it > 0 } ?: LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        
                        val startLocalDate = Instant.ofEpochMilli(budgetStart).atZone(ZoneId.systemDefault()).toLocalDate()
                        val endLocalDate = Instant.ofEpochMilli(budgetEnd).atZone(ZoneId.systemDefault()).toLocalDate()
                        val totalDays = (ChronoUnit.DAYS.between(startLocalDate, endLocalDate) + 1).coerceAtLeast(1).toInt()
                        
                        // Group active transactions by day index
                        val activeCumulativeList = remember(state.transactions, budgetStart, budgetEnd, totalDays) {
                            val dailySum = DoubleArray(totalDays)
                            state.transactions.forEach { tx ->
                                if (tx.toAccountId == null && !tx.isExcludedFromDailyBudget && tx.amount > 0) {
                                    if (tx.timestamp in budgetStart..budgetEnd) {
                                        val txDate = Instant.ofEpochMilli(tx.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
                                        val dayIdx = ChronoUnit.DAYS.between(startLocalDate, txDate).toInt().coerceIn(0, totalDays - 1)
                                        dailySum[dayIdx] += tx.amount
                                    }
                                }
                            }
                            val cumList = mutableListOf<Double>()
                            var sum = 0.0
                            for (i in 0 until totalDays) {
                                sum += dailySum[i]
                                cumList.add(sum)
                            }
                            cumList
                        }
                        
                        // Reference previous period list (e.g. June, seeded with smooth pacing target ending at 85% of monthly budget)
                        val limitAmt = budgetInfo.totalMonthlyBudget.takeIf { it > 0.0 } ?: 1000.0
                        val previousCumulativeList = remember(totalDays, limitAmt) {
                            val list = mutableListOf<Double>()
                            for (i in 0 until totalDays) {
                                val progress = i.toDouble() / (totalDays - 1).coerceAtLeast(1)
                                val baseVal = limitAmt * 0.85 * progress
                                val variation = limitAmt * 0.04 * kotlin.math.sin(progress * Math.PI * 4)
                                list.add((baseVal + variation).coerceAtLeast(0.0))
                            }
                            list
                        }
                        
                        val maxVal = maxOf(
                            activeCumulativeList.maxOrNull() ?: 0.0,
                            previousCumulativeList.maxOrNull() ?: 0.0,
                            limitAmt
                        ) * 1.15
                        
                        val chartPrimaryColor = MaterialTheme.colorScheme.primary
                        val chartSecondaryColor = MaterialTheme.colorScheme.secondary
                        val chartGridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                
                                val stepX = canvasWidth / (totalDays - 1).coerceAtLeast(1)
                                
                                // 1. Draw horizontal grid lines
                                val gridCount = 4
                                for (i in 0 until gridCount) {
                                    val gridY = (canvasHeight / (gridCount - 1)) * i
                                    drawLine(
                                        color = chartGridColor,
                                        start = Offset(0f, gridY),
                                        end = Offset(canvasWidth, gridY),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                }
                                
                                // 2. Draw Target Diagonal Pacing Line (Dotted, Secondary)
                                drawLine(
                                    color = chartSecondaryColor.copy(alpha = 0.5f),
                                    start = Offset(0f, canvasHeight),
                                    end = Offset(canvasWidth, canvasHeight - ((limitAmt / maxVal) * canvasHeight).toFloat()),
                                    strokeWidth = 2.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                )
                                
                                // 3. Draw Previous Month line (Light Grey/Solid)
                                val prevPath = Path()
                                previousCumulativeList.forEachIndexed { idx, valAmt ->
                                    val x = idx * stepX
                                    val y = canvasHeight - ((valAmt / maxVal) * canvasHeight).toFloat()
                                    if (idx == 0) {
                                        prevPath.moveTo(x, y)
                                    } else {
                                        prevPath.lineTo(x, y)
                                    }
                                }
                                drawPath(
                                    path = prevPath,
                                    color = Color.Gray.copy(alpha = 0.6f),
                                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                )
                                
                                // 4. Draw Active Month line (Bold Primary)
                                val activePath = Path()
                                activeCumulativeList.forEachIndexed { idx, valAmt ->
                                    val x = idx * stepX
                                    val y = canvasHeight - ((valAmt / maxVal) * canvasHeight).toFloat()
                                    if (idx == 0) {
                                        activePath.moveTo(x, y)
                                    } else {
                                        activePath.lineTo(x, y)
                                    }
                                }
                                drawPath(
                                    path = activePath,
                                    color = chartPrimaryColor,
                                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                        }
                        
                        // Legend
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(chartPrimaryColor))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Active", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Gray))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Prev Month", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(chartSecondaryColor))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ideal Pacing", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )
                    
                    // --- SECTION 3: SYSTEM ACTIONS ---
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "DATABASE ACTIONS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Button(
                            onClick = {
                                viewModel.dispatch(FinanceIntent.SeedMockData)
                                showBudgetDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Reset",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset & Seed Mock Data", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amt = budgetAmountText.toDoubleOrNull()
                        val end = selectedEndDateMillis
                        if (amt != null && end != null) {
                            val finalStart = if (budgetInfo.startDate > 0) {
                                budgetInfo.startDate
                            } else {
                                LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                            }
                            viewModel.dispatch(FinanceIntent.SetCustomPeriodBudget(amt, finalStart, end, selectedRolloverMode))
                            
                            // Save Category Limits
                            viewModel.dispatch(FinanceIntent.SetCategoryLimit("Food", foodLimitText.toDoubleOrNull()))
                            viewModel.dispatch(FinanceIntent.SetCategoryLimit("Transport", transportLimitText.toDoubleOrNull()))
                            viewModel.dispatch(FinanceIntent.SetCategoryLimit("Shopping", shoppingLimitText.toDoubleOrNull()))
                            viewModel.dispatch(FinanceIntent.SetCategoryLimit("Utilities", utilitiesLimitText.toDoubleOrNull()))
                            viewModel.dispatch(FinanceIntent.SetCategoryLimit("Other", otherLimitText.toDoubleOrNull()))
                            
                            showBudgetDialog = false
                        }
                    }
                ) {
                    Text("Save Changes", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            showBudgetDialog = false
                            showNewPeriodSheet = true
                        }
                    ) {
                        Text("開啟新週期", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { showBudgetDialog = false }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(32.dp))
        )
    }

    if (showRecalcSheet) {
        val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val yesterdayStart = LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val yesterdayExpenses = state.transactions.filter {
            it.toAccountId == null &&
            !it.isExcludedFromDailyBudget &&
            it.amount > 0 &&
            it.timestamp >= yesterdayStart &&
            it.timestamp < todayStart
        }.sumOf { it.amount }

        val todayExpenses = state.transactions.filter {
            it.toAccountId == null &&
            !it.isExcludedFromDailyBudget &&
            it.amount > 0 &&
            it.timestamp >= todayStart &&
            it.timestamp < tomorrowStart
        }.sumOf { it.amount }

        // Standard base daily allowance for single day in custom period
        val startLocal = java.time.Instant.ofEpochMilli(budgetInfo.startDate).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        val endLocal = java.time.Instant.ofEpochMilli(budgetInfo.endDate).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        val totalDaysInPeriod = (java.time.temporal.ChronoUnit.DAYS.between(startLocal, endLocal) + 1).coerceAtLeast(1).toDouble()
        val standardBaseDaily = if (totalDaysInPeriod > 0) budgetInfo.totalMonthlyBudget / totalDaysInPeriod else 0.0

        // Yesterday's actual remaining daily budget = standard base daily - yesterday's expenses
        val yesterdayRemainingBudget = (standardBaseDaily - yesterdayExpenses).coerceAtLeast(0.0)

        val currentDateStr = java.time.LocalDate.now().toString()
        RecalcBudgetSheet(
            budgetInfo = budgetInfo,
            yesterdayLeftover = yesterdayRemainingBudget,
            isMandatory = isRecalcSheetMandatory,
            isToday = false,
            onSelectMode = { selectedMode, _ ->
                val finalStart = if (budgetInfo.startDate > 0) budgetInfo.startDate else System.currentTimeMillis()
                viewModel.dispatch(FinanceIntent.SetCustomPeriodBudget(budgetInfo.totalMonthlyBudget, finalStart, budgetInfo.endDate, selectedMode))
                prefs.edit().putString("last_daily_recalc_date", currentDateStr).apply()
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
                        endDate = endDate
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
}

@Composable
fun <T> ExpressiveSegmentedButtonGroup(
    items: List<T>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isScrollable: Boolean = false,
    iconProvider: @Composable ((T, Color) -> Unit)? = null,
    labelProvider: @Composable (T) -> String
) {
    Row(
        modifier = if (isScrollable) {
            modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
        } else {
            modifier.fillMaxWidth()
        },
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = index == selectedIndex
            
            val topStart by animateDpAsState(
                targetValue = if (isSelected) 24.dp else (if (index == 0) 24.dp else 8.dp),
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            )
            val bottomStart by animateDpAsState(
                targetValue = if (isSelected) 24.dp else (if (index == 0) 24.dp else 8.dp),
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            )
            val topEnd by animateDpAsState(
                targetValue = if (isSelected) 24.dp else (if (index == items.size - 1) 24.dp else 8.dp),
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            )
            val bottomEnd by animateDpAsState(
                targetValue = if (isSelected) 24.dp else (if (index == items.size - 1) 24.dp else 8.dp),
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            )
            
            val shape = RoundedCornerShape(
                topStart = topStart,
                bottomStart = bottomStart,
                topEnd = topEnd,
                bottomEnd = bottomEnd
            )
            
            val containerColor by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            )
            val contentColor by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            )
            
            Surface(
                color = containerColor,
                contentColor = contentColor,
                shape = shape,
                modifier = Modifier
                    .then(
                        if (isScrollable) {
                            Modifier
                        } else {
                            Modifier.weight(1f)
                        }
                    )
                    .clickable { onItemSelected(index) }
            ) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 12.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = contentColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    } else if (iconProvider != null) {
                        iconProvider(item, contentColor)
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    
                    Text(
                        text = labelProvider(item),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

enum class TransactionMode {
    EXPENSE,
    INCOME,
    TRANSFER
}

@Composable
fun AddExpenseSheetContent(
    state: com.example.vibefinance.ui.FinanceUiState,
    onIntent: (com.example.vibefinance.ui.FinanceIntent) -> Unit,
    onDismissAddDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localFocusManager = LocalFocusManager.current
    var typedAmount by remember { mutableStateOf("0.00") }
    var descriptionText by remember { mutableStateOf("") }
    var categoryText by remember { mutableStateOf("Food") }
    var selectedAccount by remember { mutableStateOf<AccountEntity?>(state.accounts.firstOrNull()) }
    var selectedToAccount by remember { mutableStateOf<AccountEntity?>(null) }
    
    var isInstallment by remember { mutableStateOf(false) }
    var installmentCountText by remember { mutableStateOf("12") }
    
    var transactionMode by remember { mutableStateOf(TransactionMode.EXPENSE) }
    var isTransfer by remember { mutableStateOf(false) }
    var selectedCurrency by remember { mutableStateOf("HKD") }
 
    val expenseCategories = remember { mutableStateListOf("Food", "Transport", "Shopping", "Utilities", "Other") }
    val incomeCategories = remember { mutableStateListOf("Salary", "Bonus", "Investment", "Part-Time", "Gift", "Other Income") }
    var expenseCategoryText by remember { mutableStateOf("Food") }
    var incomeCategoryText by remember { mutableStateOf("Salary") }

    var showInlineAddCategory by remember { mutableStateOf(false) }
    var showInlineAddAsset by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val density = LocalDensity.current
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
                        "📷 Receipt Scanned! Auto-filled HK$ ${scanned.totalAmount} for ${scanned.merchantName}",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(context, "Could not parse receipt text", Toast.LENGTH_SHORT).show()
                }
                isScanningReceipt = false
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                .verticalFadingEdge(topFadeHeight = 0.dp, bottomFadeHeight = 40.dp)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(68.dp))

            // --- DYNAMIC BUCKWHEAT BUDGET PREVIEW CARD ---
            val budgetInfo = state.budgetInfo
            if (budgetInfo != null) {
                val inputAmount = typedAmount.toDoubleOrNull() ?: 0.0
                val currentDailyRem = budgetInfo.dailyRemaining
                val simDailyRem = currentDailyRem - inputAmount
                val isOverBudget = simDailyRem < 0

                val futureDays = (budgetInfo.daysLeft - 1).coerceAtLeast(1)
                val newMonthlyRem = (budgetInfo.monthlyRemaining - inputAmount).coerceAtLeast(0.0)
                val newDailyForRest = newMonthlyRem / futureDays

                val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = if (isOverBudget) {
                        if (isDarkTheme) Color(0xFF3B1414) else Color(0xFFFFF0F0)
                    } else {
                        if (isDarkTheme) Color(0xFF072A1A) else Color(0xFFE8F5E9)
                    },
                    border = BorderStroke(
                        1.dp,
                        if (isOverBudget) {
                            if (isDarkTheme) Color(0xFFFF5252).copy(alpha = 0.5f) else Color(0xFFE57373)
                        } else {
                            if (isDarkTheme) Color(0xFF00E676).copy(alpha = 0.5f) else Color(0xFF81C784)
                        }
                    ),
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isOverBudget) Color(0xFFFF5252).copy(alpha = 0.2f) else Color(0xFF00E676).copy(alpha = 0.2f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isOverBudget) Icons.Default.Warning else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (isOverBudget) Color(0xFFFF5252) else Color(0xFF00E676),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isOverBudget) "超支！其餘天數新每日預算 (Rest of Days)" else "今日預算剩餘 (Today Remaining)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOverBudget) {
                                        if (isDarkTheme) Color(0xFFFF8A80) else Color(0xFFB71C1C)
                                    } else {
                                        if (isDarkTheme) Color(0xFF81C784) else Color(0xFF1B5E20)
                                    }
                                )
                                Text(
                                    text = if (isOverBudget) {
                                        "超出今日預算 \$${String.format(Locale.US, "%.0f", -simDailyRem)}，未來 ${futureDays} 天每日為:"
                                    } else {
                                        "扣除本筆消費後今日尚有:"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Amount Display with Smooth Animated Transition
                        AnimatedContent(
                            targetState = if (isOverBudget) newDailyForRest to true else simDailyRem to false,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(150)) + scaleIn(initialScale = 0.8f)) togetherWith (fadeOut(animationSpec = tween(100)) + scaleOut(targetScale = 1.1f))
                            },
                            label = "DynamicBudgetAnim"
                        ) { (amount, over) ->
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = String.format(Locale.US, "HK$ %,.0f", Math.abs(amount)),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = if (over) {
                                        if (isDarkTheme) Color(0xFFFF5252) else Color(0xFFC62828)
                                    } else {
                                        if (isDarkTheme) Color(0xFF00E676) else Color(0xFF2E7D32)
                                    }
                                )
                                if (over) {
                                    Text(
                                        text = "/ 天 (per day)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isDarkTheme) Color(0xFFFF8A80) else Color(0xFFB71C1C),
                                        fontWeight = FontWeight.Bold
                                    )
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
            placeholder = { Text("Note / Merchant Description (e.g. Starbucks)", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)) },
            trailingIcon = {
                IconButton(onClick = { receiptPickerLauncher.launch("image/*") }) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Scan Receipt",
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

        Spacer(modifier = Modifier.height(16.dp))

        // --- 1. MATERIAL 3 CONNECTED BUTTON GROUP (MODE SELECTOR) ---
        Text(
            text = "Transaction Type",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 6.dp)
        )

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            SegmentedButton(
                selected = transactionMode == TransactionMode.EXPENSE,
                onClick = {
                    localFocusManager.clearFocus()
                    transactionMode = TransactionMode.EXPENSE
                    isTransfer = false
                    selectedToAccount = null
                },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                icon = {
                    Icon(
                        imageVector = Icons.Default.TrendingDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            ) {
                Text(
                    text = "Expense",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            SegmentedButton(
                selected = transactionMode == TransactionMode.INCOME,
                onClick = {
                    localFocusManager.clearFocus()
                    transactionMode = TransactionMode.INCOME
                    isTransfer = false
                    selectedToAccount = null
                },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                icon = {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            ) {
                Text(
                    text = "Income",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            SegmentedButton(
                selected = transactionMode == TransactionMode.TRANSFER,
                onClick = {
                    localFocusManager.clearFocus()
                    transactionMode = TransactionMode.TRANSFER
                    isTransfer = true
                },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                icon = {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            ) {
                Text(
                    text = "Transfer",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

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
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "+ Add Category",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { showInlineAddCategory = !showInlineAddCategory }
                    )
                }
                
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
                        Text("Add Custom Category", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newCatName,
                                onValueChange = { newCatName = it },
                                placeholder = { Text("Category Name (e.g. Health)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                textStyle = MaterialTheme.typography.bodyMedium
                            )
                            Button(
                                onClick = {
                                    val name = newCatName.trim()
                                    if (transactionMode == TransactionMode.INCOME) {
                                        if (name.isNotEmpty() && !incomeCategories.contains(name)) {
                                            incomeCategories.add(name)
                                            incomeCategoryText = name
                                        }
                                    } else {
                                        if (name.isNotEmpty() && !expenseCategories.contains(name)) {
                                            expenseCategories.add(name)
                                            expenseCategoryText = name
                                        }
                                    }
                                    newCatName = ""
                                    showInlineAddCategory = false
                                }
                            ) {
                                Text("Add")
                            }
                        }
                    }
                }
                
                val currentCategories = if (transactionMode == TransactionMode.INCOME) incomeCategories else expenseCategories
                val currentCategoryText = if (transactionMode == TransactionMode.INCOME) incomeCategoryText else expenseCategoryText

                ExpressiveSegmentedButtonGroup(
                    items = currentCategories,
                    selectedIndex = currentCategories.indexOf(currentCategoryText),
                    onItemSelected = { index ->
                        if (transactionMode == TransactionMode.INCOME) {
                            incomeCategoryText = incomeCategories[index]
                        } else {
                            expenseCategoryText = expenseCategories[index]
                        }
                    },
                    isScrollable = true,
                    iconProvider = { cat, tintColor ->
                        CategoryIcon(category = cat, tint = tintColor, modifier = Modifier.size(16.dp))
                    },
                    labelProvider = { cat -> cat }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Asset Selection Horizontal Grid
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTransfer) "From Account" else "Asset Selection",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Text(
                    text = "+ Add Card / Account",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { showInlineAddAsset = !showInlineAddAsset }
                )
            }
            
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
                    
                    Text("Add Custom Card / Account", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    
                    OutlinedTextField(
                        value = newAssetName,
                        onValueChange = { newAssetName = it },
                        placeholder = { Text("Account Name (e.g. Citi Bank)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Type:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.2f))
                        ExpressiveSegmentedButtonGroup(
                            items = listOf(AccountType.CASH, AccountType.BANK, AccountType.CC),
                            selectedIndex = listOf(AccountType.CASH, AccountType.BANK, AccountType.CC).indexOf(selectedType),
                            onItemSelected = { selectedType = listOf(AccountType.CASH, AccountType.BANK, AccountType.CC)[it] },
                            modifier = Modifier.weight(1f),
                            labelProvider = {
                                when (it) {
                                    AccountType.CASH -> "Cash"
                                    AccountType.BANK -> "Bank"
                                    AccountType.CC -> "Card"
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
                            placeholder = { Text(if (selectedType == AccountType.CC) "Credit Limit ($)" else "Initial Balance ($)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                val name = newAssetName.trim()
                                val bal = initialBalanceText.toDoubleOrNull() ?: 0.0
                                if (name.isNotEmpty()) {
                                    val newAccount = AccountEntity(
                                        name = name,
                                        type = selectedType,
                                        balance = if (selectedType == AccountType.CASH) bal else 0.0,
                                        icon = when (selectedType) {
                                            AccountType.CASH -> "wallet"
                                            AccountType.BANK -> "bank"
                                            AccountType.CC -> "credit_card"
                                        },
                                        creditLimit = if (selectedType == AccountType.CC) bal else null
                                    )
                                    onIntent(com.example.vibefinance.ui.FinanceIntent.SaveAccount(newAccount))
                                }
                                newAssetName = ""
                                initialBalanceText = ""
                                showInlineAddAsset = false
                            }
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
            
            ExpressiveSegmentedButtonGroup(
                items = state.accounts,
                selectedIndex = state.accounts.indexOfFirst { it.id == selectedAccount?.id },
                onItemSelected = { index -> selectedAccount = state.accounts[index] },
                isScrollable = true,
                iconProvider = { acc, tintColor ->
                    val accIcon = when (acc.type) {
                        AccountType.CASH -> Icons.Default.Savings
                        AccountType.BANK -> Icons.Default.AccountBalance
                        AccountType.CC -> Icons.Default.ShoppingCart
                    }
                    Icon(
                        imageVector = accIcon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = tintColor
                    )
                },
                labelProvider = { acc -> acc.name }
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
                    text = "To Account (Destination)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val destAccounts = remember(state.accounts, selectedAccount) {
                    state.accounts.filter { it.id != selectedAccount?.id }
                }
                ExpressiveSegmentedButtonGroup(
                    items = destAccounts,
                    selectedIndex = destAccounts.indexOfFirst { it.id == selectedToAccount?.id },
                    onItemSelected = { index -> selectedToAccount = destAccounts[index] },
                    isScrollable = true,
                    iconProvider = { acc, tintColor ->
                        val accIcon = when (acc.type) {
                            AccountType.CASH -> Icons.Default.Savings
                            AccountType.BANK -> Icons.Default.AccountBalance
                            AccountType.CC -> Icons.Default.ShoppingCart
                        }
                        Icon(
                            imageVector = accIcon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = tintColor
                        )
                    },
                    labelProvider = { acc -> acc.name }
                )
            }
        }
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
                // Installments option (available for all accounts)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { isInstallment = !isInstallment }
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = if (isInstallment) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "分期付費 (Split into Installments)",
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "將金額分攤到多個月份 (Divide amount over months)",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Switch(
                        checked = isInstallment,
                        onCheckedChange = { isInstallment = it },
                        thumbContent = if (isInstallment) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                )
                            }
                        } else {
                            null
                        }
                    )
                }

                if (isInstallment) {
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp,
                        borderWidth = 1.dp,
                        borderColor = MaterialTheme.colorScheme.outlineVariant
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "分期期數 (Installment Months)",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                WheelNumberPicker(
                                    selectedValue = installmentCountText.toIntOrNull() ?: 12,
                                    onValueSelected = { installmentCountText = it.toString() },
                                    range = 2..36,
                                    itemHeight = 64.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "期 (Months)",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val view = LocalView.current
        
        // --- ⚡ ONE-TAP QUICK EXPENSE PRESET CHIPS ---
        val quickPresets = remember {
            listOf(
                Triple("☕", "Coffee", "24"),
                Triple("🚇", "MTR", "10"),
                Triple("🍱", "Lunch", "65"),
                Triple("🥤", "Drink", "18"),
                Triple("🛒", "Groceries", "120")
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚡ Quick Presets (一鍵快捷)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            items(quickPresets) { (emoji, label, amtStr) ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        typedAmount = amtStr
                        descriptionText = "$emoji $label"
                        val suggestedCat = if (label == "MTR") "Transport" else if (label == "Groceries") "Shopping" else "Food"
                        if (expenseCategories.contains(suggestedCat)) {
                            expenseCategoryText = suggestedCat
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = emoji, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "HK$ $amtStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 5. Buckwheat-Style Custom Numeric Keypad
        val buttonHeight = 60.dp
        val gap = 6.dp

        Column(
            verticalArrangement = Arrangement.spacedBy(gap),
            modifier = Modifier.fillMaxWidth()
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
                val isApplyEnabled = amt != null && amt > 0.0 && selectedAccount?.id != null
                KeyboardButton(
                    modifier = Modifier.weight(1f).fillMaxHeight().alpha(if (isApplyEnabled) 1f else 0.5f),
                    type = KeyboardButtonType.PRIMARY,
                    icon = rememberVectorPainter(Icons.Default.Check),
                    onClick = {
                        val accId = selectedAccount?.id
                        if (isApplyEnabled && accId != null && amt != null) {
                            val finalHkdAmt = com.example.vibefinance.util.CurrencyEngine.convertToHkd(amt, selectedCurrency)
                            val descWithFx = if (selectedCurrency != "HKD") {
                                val origStr = com.example.vibefinance.util.CurrencyEngine.formatOriginal(amt, selectedCurrency)
                                if (descriptionText.isNotEmpty()) "$descriptionText ($origStr)" else "Foreign Expense ($origStr)"
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
                                        description = descriptionText.ifEmpty { "CC Payment Transfer" }
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
                            } else if (isInstallment) {
                                val insCount = installmentCountText.toIntOrNull() ?: 12
                                onIntent(
                                    com.example.vibefinance.ui.FinanceIntent.AddInstallmentTransaction(
                                        amount = finalHkdAmt,
                                        category = expenseCategoryText,
                                        accountId = accId,
                                        description = descWithFx,
                                        installments = insCount
                                    )
                                )
                            } else {
                                onIntent(
                                    com.example.vibefinance.ui.FinanceIntent.AddTransaction(
                                        amount = finalHkdAmt,
                                        category = expenseCategoryText,
                                        accountId = accId,
                                        description = descWithFx
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismissAddDialog) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
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
                            TransactionMode.INCOME -> "Add Income"
                            TransactionMode.TRANSFER -> "Transfer"
                            else -> "Add Expense"
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
                                    contentDescription = "Amount Icon",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                        }
                        
                        Text(
                            text = "$",
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
                        onClick = { showCurrencyMenu = !showCurrencyMenu }
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

fun Modifier.verticalFadingEdge(
    topFadeHeight: Dp = 100.dp,
    bottomFadeHeight: Dp = 110.dp
): Modifier = this.graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        
        val topFadePx = topFadeHeight.toPx()
        val bottomFadePx = bottomFadeHeight.toPx()
        
        // Top fade
        if (topFadePx > 0f) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black),
                    startY = 0f,
                    endY = topFadePx
                ),
                blendMode = BlendMode.DstIn
            )
        }
        
        // Bottom fade
        if (bottomFadePx > 0f) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Black, Color.Transparent),
                    startY = size.height - bottomFadePx,
                    endY = size.height
                ),
                blendMode = BlendMode.DstIn
            )
        }
    }

