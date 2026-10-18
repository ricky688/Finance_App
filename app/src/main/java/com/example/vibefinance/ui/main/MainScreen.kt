@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.example.vibefinance.ui.main

import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.vibefinance.ui.components.AppModalBottomSheet
import com.example.vibefinance.ui.components.ExpressiveConnectedButtonGroup
import com.example.vibefinance.ui.components.RollingNumberText
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.graphics.vector.ImageVector
import android.content.res.Resources
import com.example.vibefinance.theme.LocalIsDarkTheme
import com.example.vibefinance.theme.MotionTokens
import com.example.vibefinance.ui.accounts.AssetEditorHost
import com.example.vibefinance.ui.accounts.assetEditorBackgroundMotion
import com.example.vibefinance.ui.accounts.rememberAssetEditorState
import com.example.vibefinance.ui.recurring.SubscriptionEntryHost
import com.example.vibefinance.ui.recurring.rememberSubscriptionEditorState
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics

import com.example.vibefinance.ui.components.rememberCompletePressProgress
import com.example.vibefinance.ui.components.connectedSpatialMotion
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
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ContainedLoadingIndicator
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ripple
import com.example.vibefinance.ui.components.AppSwitch
import com.example.vibefinance.ui.components.rememberConnectedButtonColorMotion
import com.example.vibefinance.ui.components.ConnectedButtonRipple
import com.example.vibefinance.util.VibeHaptics
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
import com.example.vibefinance.util.rememberVibeHapticFeedback
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalView
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.service.PendingPayment
import com.example.vibefinance.service.PendingPaymentStore
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.ui.home.CategoryIcon
import androidx.compose.foundation.clickable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.AccountBalance
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
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import androidx.compose.ui.res.stringResource
import com.example.vibefinance.R
import com.example.vibefinance.ui.AppLanguage
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
import androidx.compose.runtime.collectAsState
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
import com.example.vibefinance.util.CurrencyEngine
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
import com.example.vibefinance.ui.components.FlyingCashOverlay
import com.example.vibefinance.ui.components.EasterEggTriggerDetector
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
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
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
internal fun ExpressiveCollapsingTopBar(
    selectedTab: TabItem,
    state: FinanceUiState,
    isExpanded: Boolean,
    hazeState: HazeState,
    backgroundObscured: Boolean,
    actions: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    canNavigateBack: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onAppIconEasterEggTrigger: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    val topBlurIntensity = com.example.vibefinance.ui.preferences.LocalExperience.current.blur
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

    val easterEggDetector = remember { EasterEggTriggerDetector(requiredTaps = 5, timeoutMillis = 1800L) }
    val iconScale = remember { Animatable(1f) }
    val iconRotation = remember { Animatable(0f) }
    val iconScope = rememberCoroutineScope()
    val iconFeedbackView = androidx.compose.ui.platform.LocalView.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("MainTopBar")
            .hazeEffect(
                state = hazeState,
                style = SubtleGlass.style(colors.surface, topBlurIntensity),
                block = { blurEnabled = !backgroundObscured && topBlurIntensity > 0f }
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
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = false, radius = 24.dp)
                        ) {
                            val now = android.os.SystemClock.uptimeMillis()
                            val triggered = easterEggDetector.registerTap(now)
                            iconScope.launch {
                                iconScale.animateTo(0.85f, spring(stiffness = Spring.StiffnessHigh))
                                iconScale.animateTo(
                                    1f,
                                    spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                            }
                            if (triggered) {
                                com.example.vibefinance.util.VibeHaptics.successConfirm(iconFeedbackView)
                                iconScope.launch {
                                    iconRotation.snapTo(0f)
                                    iconRotation.animateTo(
                                        360f,
                                        spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                                }
                                onAppIconEasterEggTrigger()
                            } else {
                                com.example.vibefinance.util.VibeHaptics.lightTick(iconFeedbackView)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .graphicsLayer {
                                scaleX = iconScale.value
                                scaleY = iconScale.value
                                rotationZ = iconRotation.value
                            },
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
                    .testTag("MainSecondaryTopBar")
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
    val haptic = rememberVibeHapticFeedback()
    val hapticView = androidx.compose.ui.platform.LocalView.current
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
    val requestedPaymentCategory by viewModel.requestedPaymentCategory.collectAsStateWithLifecycle()
    var editingPaymentCategoryId by rememberSaveable { mutableStateOf<String?>(null) }
    var savingPaymentCategory by remember { mutableStateOf(false) }
    var paymentCategoryError by remember { mutableStateOf(false) }
    val paymentCategoryScope = rememberCoroutineScope()
    LaunchedEffect(requestedPaymentCategory) {
        requestedPaymentCategory?.let {
            editingPaymentCategoryId = it
            paymentCategoryError = false
            viewModel.clearRequestedPaymentCategory()
        }
    }
    val notificationTransactions by com.example.vibefinance.data.InMemoryDatabase.transactions.collectAsStateWithLifecycle()
    val paymentCategoryTransaction = notificationTransactions.firstOrNull {
        it.groupId == "notification:$editingPaymentCategoryId"
    }
    LaunchedEffect(editingPaymentCategoryId, paymentCategoryTransaction, state.isLoading) {
        if (editingPaymentCategoryId != null && !state.isLoading && paymentCategoryTransaction == null) {
            editingPaymentCategoryId = null
            android.widget.Toast.makeText(context, R.string.nf_transaction_unavailable, android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    LaunchedEffect(requestedTab) {
        requestedTab?.let {
            selectedTab = it
            viewModel.clearRequestedTab()
        }
    }
    var budgetSheetDestination by rememberSaveable { mutableStateOf(BudgetSheetDestination.NONE) }
    val showBudgetDialog = budgetSheetDestination.showsSettings
    val budgetConfigurationOnly = budgetSheetDestination == BudgetSheetDestination.EDIT_PERIOD
    val showNewPeriodSheet = budgetSheetDestination == BudgetSheetDestination.NEW_PERIOD
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var showAddRecurringSheet by remember { mutableStateOf(false) }
    var showLoadingIndicator by remember { mutableStateOf(false) }
    var prefilledTransferCardId by rememberSaveable { mutableStateOf<Long?>(null) }
    var activeTransactionMode by rememberSaveable { mutableStateOf(TransactionMode.EXPENSE) }
    val snackbarHostState = remember { SnackbarHostState() }
    val pendingPayments by PendingPaymentStore.pending.collectAsStateWithLifecycle()
    var deferredPaymentIds by remember { mutableStateOf(emptySet<String>()) }
    var savingPaymentId by remember { mutableStateOf<String?>(null) }
    var settlingEditorPaymentId by remember { mutableStateOf<String?>(null) }
    val assetEditorState = rememberAssetEditorState()
    val subscriptionEditorState = rememberSubscriptionEditorState()
    val accountSaveResult by viewModel.accountSaveResult.collectAsStateWithLifecycle()
    val pendingChoiceScope = rememberCoroutineScope()
    LaunchedEffect(context) { PendingPaymentStore.initialize(context) }
    val paymentToChoose = if (assetEditorState.isOpen || editingPaymentCategoryId != null) null
        else pendingPayments.firstOrNull { it.id !in deferredPaymentIds && it.id != settlingEditorPaymentId }

    // Scroll state tracking
    val density = LocalDensity.current
    var measuredBottomBarHeightPx by remember { mutableFloatStateOf(0f) }

    var isFabExpanded by remember { mutableStateOf(true) }
    var isNavBarVisible by remember { mutableStateOf(true) }
    val dailyListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val assetsListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val recurringListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val historyListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val activePageListState = when (selectedTab) {
        TabItem.HOME -> dailyListState
        TabItem.ACCOUNTS -> assetsListState
        TabItem.RECURRING -> recurringListState
        TabItem.HISTORY -> historyListState
    }
    val topBarState = rememberPageTopBarState(activePageListState)
    val hazeState = rememberHazeState()
    val overlayTopBar = LocalConfiguration.current.screenWidthDp < 600

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

    val nestedScrollConnection = remember(showAddDialog, topBarState) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (showAddDialog) {
                    return Offset.Zero
                }
                val delta = available.y
                topBarState.onScroll(delta)
                
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
        }
    }

    // Reset navigation/FAB visibility when changing tabs; the title follows each page's position.
    LaunchedEffect(selectedTab) {
        isFabExpanded = true
        isNavBarVisible = true
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

    // --- PREDICTIVE BACK NAVIGATION HANDLERS ---
    // If user is on a secondary tab (History, Recurring, Assets, Radar), predictive back returns to HOME
    var tabBackProgress by remember { mutableFloatStateOf(0f) }
    val animatedTabBackProgress by animateFloatAsState(
        targetValue = tabBackProgress,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tabBackProgress"
    )

    val isAnyModalActive = assetEditorState.isOpen || subscriptionEditorState.isOpen || editingPaymentCategoryId != null || showBudgetDialog || showAddDialog || showAddRecurringSheet || showRecalcSheet || showNewPeriodSheet || state.importPreview != null
    val isAnySheetOpen = showRecalcSheet || showNewPeriodSheet
    // One bounded clock for depth, read in the layer rather than recomposing the page each frame.
    val sheetDepthProgress = animateFloatAsState(
        targetValue = if (isAnySheetOpen) 1f else 0f,
        animationSpec = MotionTokens.emphasizedEnter(220),
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
        if (hasAutoTriggeredForSession || assetEditorState.isOpen) return
        autoSheetScope.launch {
            val isStartupAnimationActive = state.launchAnimationEnabled &&
                state.motionLevel == com.example.vibefinance.ui.preferences.MotionLevel.FULL
            kotlinx.coroutines.delay(if (isStartupAnimationActive) 800L else 200L) // Responsive delay after app launch UI renders
            if (assetEditorState.isOpen) return@launch
            if (isPeriodEnded) {
                hasAutoTriggeredForSession = true
                budgetSheetDestination = BudgetSheetDestination.NEW_PERIOD
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



    val feedbackView = androidx.compose.ui.platform.LocalView.current
    val batchResult by viewModel.batchResult.collectAsState()
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
                FinanceUiEvent.HapticSuccess -> com.example.vibefinance.util.VibeHaptics.successConfirm(feedbackView)
                FinanceUiEvent.SeedCompleted -> {
                    // Seed completed successfully
                }
            }
        }
    }

    val settingsDrawerState = androidx.compose.material3.rememberDrawerState(androidx.compose.material3.DrawerValue.Closed)
    var selectedEndDateMillis by rememberSaveable(budgetInfo.endDate, showBudgetDialog) {
        mutableStateOf<Long?>(budgetInfo.endDate.takeIf { it > 0 })
    }
    var showDatePickerModal by rememberSaveable(showBudgetDialog) { mutableStateOf(false) }
    LaunchedEffect(showBudgetDialog, budgetConfigurationOnly) {
        // Import/restore and opening the focused budget form can leave Settings directly.
        if (!showBudgetDialog || budgetConfigurationOnly) settingsDrawerState.close()
    }
    var flyingCashTriggerKey by remember { mutableLongStateOf(0L) }
    val assetEditorTransition = androidx.compose.animation.core.updateTransition(
        assetEditorState.isOpen, label = "Asset editor navigation"
    )
    val transactionEditorTransition = androidx.compose.animation.core.updateTransition(
        showAddDialog, label = "Transaction editor navigation"
    )
    val subscriptionEditorTransition = androidx.compose.animation.core.updateTransition(
        subscriptionEditorState.isOpen, label = "Subscription editor navigation"
    )
    val fullScreenEditorTransition = androidx.compose.animation.core.updateTransition(
        assetEditorState.isOpen || showAddDialog || subscriptionEditorState.isOpen, label = "Full-screen editor background"
    )
    val shouldSlideBackground = (showAddDialog || subscriptionEditorState.isOpen) && !assetEditorState.isOpen
    Box(Modifier.fillMaxSize().assetEditorBackgroundMotion(fullScreenEditorTransition, enableSlide = shouldSlideBackground)
        .semantics { if (fullScreenEditorTransition.currentState || fullScreenEditorTransition.targetState) hideFromAccessibility() }) {
    com.example.vibefinance.ui.settings.SettingsDrawerHost(
        drawerState = settingsDrawerState,
        drawerContent = {
            if (showBudgetDialog && !budgetConfigurationOnly) {
                com.example.vibefinance.ui.settings.SettingsSheet(
                    state = state,
                    budgetInfo = budgetInfo,
                    viewModel = viewModel,
                    onDismiss = { budgetSheetDestination = budgetSheetDestination.dismissSettings() },
                    onOpenNewPeriod = { budgetSheetDestination = BudgetSheetDestination.NEW_PERIOD },
                    onOpenDatePicker = { showDatePickerModal = true },
                    selectedEndDateMillis = selectedEndDateMillis,
                    drawerState = settingsDrawerState
                )
            }
        }
    ) {
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
                    isExpanded = topBarState.expanded,
                    hazeState = hazeState,
                    backgroundObscured = isBackgroundObscured,
                    onAppIconEasterEggTrigger = { flyingCashTriggerKey = System.currentTimeMillis() },
                    actions = {
                        IconButton(onClick = { viewModel.dispatch(FinanceIntent.SetHideAmounts(!state.hideAmounts)) },
                            modifier = Modifier.testTag("ToggleAmountPrivacy")) {
                            Icon(if (state.hideAmounts) androidx.compose.material.icons.Icons.Default.VisibilityOff
                                else androidx.compose.material.icons.Icons.Default.Visibility,
                                contentDescription = stringResource(if (state.hideAmounts) R.string.privacy_show_amounts else R.string.privacy_hide_amounts))
                        }
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
                        IconButton(onClick = {
                            VibeHaptics.lightTick(hapticView)
                            budgetSheetDestination = BudgetSheetDestination.SETTINGS
                        }) {
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
                    selectedTab == TabItem.RECURRING && !showAddRecurringSheet && !subscriptionEditorState.isOpen -> TabItem.RECURRING
                    else -> null
                }
                AnimatedContent(
                    targetState = fabTab,
                    contentAlignment = Alignment.BottomEnd,
                    transitionSpec = {
                        val enter = fadeIn(animationSpec = MotionTokens.emphasizedEnter(180)) +
                            scaleIn(
                                initialScale = 0.78f,
                                transformOrigin = TransformOrigin(1f, 1f),
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        val exit = fadeOut(animationSpec = MotionTokens.emphasizedExit(120)) +
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
                            (fadeIn(MotionTokens.standardEnter(120)) togetherWith fadeOut(MotionTokens.standardExit(90))).using(null)
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
                        TabItem.HOME -> ExtendedFloatingActionButton(
                            expanded = isFabExpanded,
                            onClick = {
                                if (selectedTab != TabItem.HOME) return@ExtendedFloatingActionButton
                                com.example.vibefinance.util.VibeHaptics.impactMedium(hapticView)
                                activeTransactionMode = TransactionMode.EXPENSE
                                prefilledTransferCardId = null
                                showAddDialog = true
                            },
                            modifier = Modifier.testTag("DailyAddFab").graphicsLayer {
                                translationY = with(density) { fabOffsetY.toPx() }
                            },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            shape = RoundedCornerShape(16.dp),
                            icon = { Icon(Icons.Default.Add, contentDescription = stringResource(R.string.ui_main_add_transaction), modifier = Modifier.size(24.dp)) },
                            text = { Text(stringResource(R.string.btn_add), maxLines = 1, softWrap = false) }
                        )
                        TabItem.RECURRING -> ExtendedFloatingActionButton(
                            expanded = isFabExpanded,
                            onClick = {
                                if (selectedTab != TabItem.RECURRING) return@ExtendedFloatingActionButton
                                com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                subscriptionEditorState.open()
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
                    val motion = com.example.vibefinance.ui.preferences.LocalExperience.current.motion
                    val isStartupAnimationActive = state.launchAnimationEnabled &&
                        motion == com.example.vibefinance.ui.preferences.MotionLevel.FULL
                    var isNavBarAppEntered by rememberSaveable { mutableStateOf(!isStartupAnimationActive) }
                    LaunchedEffect(isStartupAnimationActive) {
                        if (isStartupAnimationActive && !isNavBarAppEntered) {
                            delay(750L) // Synchronized with Material You Splash Exit
                            isNavBarAppEntered = true
                        } else {
                            isNavBarAppEntered = true
                        }
                    }

                    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
                    NavigationBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .hazeEffect(hazeState, SubtleGlass.style(MaterialTheme.colorScheme.surfaceContainer, state.blurIntensity)) {
                                blurEnabled = !isBackgroundObscured && state.blurIntensity > 0f
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
                            val popProgress = remember { androidx.compose.animation.core.Animatable(if (isNavBarAppEntered && isNavBarVisible) 1f else 0f) }
                            LaunchedEffect(isNavBarVisible, isNavBarAppEntered) {
                                if (isNavBarAppEntered) {
                                    if (isNavBarVisible) {
                                        if (motion == com.example.vibefinance.ui.preferences.MotionLevel.MINIMAL) {
                                            popProgress.snapTo(1f)
                                        } else {
                                            popProgress.snapTo(0f)
                                            val staggerDelay = if (isStartupAnimationActive) {
                                                index * 50L + 20L // Standard splash exit stagger
                                            } else {
                                                index * 35L + 10L // Early snappy pop entrance when startup animation is off
                                            }
                                            delay(staggerDelay)
                                            popProgress.animateTo(
                                                targetValue = 1f,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessMediumLow
                                                )
                                            )
                                        }
                                    } else {
                                        if (motion == com.example.vibefinance.ui.preferences.MotionLevel.MINIMAL) {
                                            popProgress.snapTo(0f)
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
                                    }
                                } else {
                                    popProgress.snapTo(0f)
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
                                        com.example.vibefinance.util.VibeHaptics.selection(hapticView)
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
            // Scaffold reports the animated visible height, including status-bar insets.
            // Reserve that height only; retaining the expanded maximum leaves a blank band.
            val pageTopPadding = (if (overlayTopBar) paddingValues.calculateTopPadding() else 0.dp) + 16.dp
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
                                animationSpec = MotionTokens.emphasizedEnter(210)
                            ) + scaleIn(
                                initialScale = 0.94f,
                                transformOrigin = TransformOrigin.Center,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                            val exitTransition = fadeOut(
                                animationSpec = MotionTokens.emphasizedExit(90)
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
                                    onPayCard = { id -> prefilledTransferCardId = id; activeTransactionMode = TransactionMode.TRANSFER; showAddDialog = true },
                                    onIntent = viewModel::dispatch,
                                    modifier = Modifier.padding(horizontal = if (LocalConfiguration.current.screenWidthDp < 400) 12.dp else 16.dp),
                                    listState = dailyListState,
                                    topContentPadding = pageTopPadding,
                                    topVisibilityInset = if (overlayTopBar) paddingValues.calculateTopPadding() else 0.dp,
                                    bottomVisibilityInsetProvider = { (
                                        if (measuredBottomBarHeightPx > 0f) with(density) { measuredBottomBarHeightPx.toDp() }
                                        else 100.dp
                                    ).minus(navBarOffsetY).coerceAtLeast(navBarInsetsBottom) },
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
                                    onDismissAddDialog = { showAddDialog = false; prefilledTransferCardId = null },
                                    onOpenBudgetDialog = {
                                        budgetSheetDestination = BudgetSheetDestination.fromDaily(budgetInfo.totalMonthlyBudget)
                                    },
                                    onOpenRecalcSheet = {
                                        val isBudgetEnd = budgetInfo.monthlyRemaining <= 0.0 || (budgetInfo.dailyRemaining < 0.0 && budgetInfo.newDailyBudget <= 0.0)
                                        if (isPeriodEnded || isBudgetEnd) {
                                            budgetSheetDestination = BudgetSheetDestination.NEW_PERIOD
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
                                listState = assetsListState,
                                topContentPadding = pageTopPadding,
                                topVisibilityInset = if (overlayTopBar) paddingValues.calculateTopPadding() else 0.dp,
                                bottomVisibilityInsetProvider = { (
                                    if (measuredBottomBarHeightPx > 0f) with(density) { measuredBottomBarHeightPx.toDp() }
                                    else 100.dp
                                ).minus(navBarOffsetY).coerceAtLeast(navBarInsetsBottom) },
                                onViewAccountHistory = { accountId ->
                                    historyAccountFilterId = accountId
                                    historyCategoryFilter = null
                                    selectedTab = TabItem.HISTORY
                                },
                                editorState = assetEditorState,
                                renderEditor = false
                            )
                            TabItem.RECURRING -> RecurringScreen(
                                state = state,
                                onIntent = viewModel::dispatch,
                                modifier = Modifier.fillMaxSize(),
                                listState = recurringListState,
                                topContentPadding = pageTopPadding,
                                topVisibilityInset = if (overlayTopBar) paddingValues.calculateTopPadding() else 0.dp,
                                bottomVisibilityInsetProvider = { (
                                    if (measuredBottomBarHeightPx > 0f) with(density) { measuredBottomBarHeightPx.toDp() }
                                    else 100.dp
                                ).minus(navBarOffsetY).coerceAtLeast(navBarInsetsBottom) },
                                showAddSheet = showAddRecurringSheet,
                                onDismissAddSheet = { showAddRecurringSheet = false },
                                editorState = subscriptionEditorState,
                                renderEditor = false
                            )
                            TabItem.HISTORY -> HistoryScreen(
                                state = state,
                                batchResult = batchResult,
                                onIntent = viewModel::dispatch,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                listState = historyListState,
                                topContentPadding = pageTopPadding,
                                topVisibilityInset = if (overlayTopBar) paddingValues.calculateTopPadding() else 0.dp,
                                bottomVisibilityInsetProvider = { (
                                    if (measuredBottomBarHeightPx > 0f) with(density) { measuredBottomBarHeightPx.toDp() }
                                    else 100.dp
                                ).minus(navBarOffsetY).coerceAtLeast(navBarInsetsBottom) },
                                accountFilterId = historyAccountFilterId,
                                onClearAccountFilter = { historyAccountFilterId = null },
                                categoryFilter = historyCategoryFilter,
                                onClearCategoryFilter = { historyCategoryFilter = null },
                                onAddTransaction = {
                                    activeTransactionMode = TransactionMode.EXPENSE
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

                SubtleGlassTransition(
                    state = hazeState, top = true, obscured = isBackgroundObscured,
                    modifier = Modifier.align(Alignment.TopCenter)
                        .offset(y = paddingValues.calculateTopPadding())
                )
                SubtleGlassTransition(
                    state = hazeState, top = false, obscured = isBackgroundObscured,
                    modifier = Modifier.align(Alignment.BottomCenter).offset {
                        val barHeight = if (measuredBottomBarHeightPx > 0f) measuredBottomBarHeightPx else 100.dp.toPx()
                        IntOffset(0, -(barHeight - navBarOffsetY.toPx()).roundToInt())
                    }
                )

                FlyingCashOverlay(
                    triggerKey = flyingCashTriggerKey,
                    onCompleted = { flyingCashTriggerKey = 0L }
                )
            }
        }
    }

    }

    }

    TransactionEntryHost(
        visible = showAddDialog, state = state, onIntent = viewModel::dispatch,
        onDismiss = { showAddDialog = false; prefilledTransferCardId = null },
        initialMode = activeTransactionMode, categoryFrequency = categoryFrequency,
        accountFrequency = accountFrequency, prefilledDestinationAccountId = prefilledTransferCardId,
        transition = transactionEditorTransition,
    )

    SubscriptionEntryHost(
        visible = subscriptionEditorState.isOpen,
        state = state,
        editorState = subscriptionEditorState,
        onIntent = viewModel::dispatch,
        onDismiss = {
            subscriptionEditorState.close()
            showAddRecurringSheet = false
        },
        transition = subscriptionEditorTransition,
    )

    AssetEditorHost(
        state = state, editorState = assetEditorState, onIntent = viewModel::dispatch,
        transition = assetEditorTransition,
        saveResult = accountSaveResult,
        onAccountCreated = { account ->
            val paymentId = assetEditorState.originPaymentId
            val originTab = assetEditorState.originTab
            if (paymentId != null) {
                settlingEditorPaymentId = paymentId
                pendingChoiceScope.launch {
                    val recorded = runCatching {
                        PendingPaymentStore.accept(context, paymentId, account.id, rememberChoice = true)
                    }.getOrDefault(false)
                    settlingEditorPaymentId = null
                    if (!recorded) deferredPaymentIds = deferredPaymentIds + paymentId
                    snackbarHostState.showSnackbar(if (recorded) context.getString(R.string.pending_payment_quick_created, account.name)
                        else context.getString(R.string.pending_payment_failed))
                }
            }
            originTab?.let { selectedTab = TabItem.valueOf(it) }
        },
        onDismissCreate = {
            assetEditorState.originPaymentId?.let { deferredPaymentIds = deferredPaymentIds + it }
            assetEditorState.originTab?.let { selectedTab = TabItem.valueOf(it) }
        }
    )

    if (!assetEditorState.isOpen) paymentCategoryTransaction?.let { transaction ->
        NotificationCategorySheet(
            transaction = transaction, state = state, saving = savingPaymentCategory,
            error = paymentCategoryError,
            onDismiss = { if (!savingPaymentCategory) editingPaymentCategoryId = null },
            onSave = { category ->
                val paymentId = editingPaymentCategoryId
                if (!savingPaymentCategory && paymentId != null) {
                    savingPaymentCategory = true
                    paymentCategoryError = false
                    paymentCategoryScope.launch {
                        val saved = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                            runCatching { PendingPaymentStore.changeCategory(context, paymentId, category) }.getOrDefault(false)
                        }
                        savingPaymentCategory = false
                        if (saved) {
                            if (editingPaymentCategoryId == paymentId) editingPaymentCategoryId = null
                        } else paymentCategoryError = true
                    }
                }
            }
        )
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
            onQuickCreate = { suggestedAccount ->
                savingPaymentId = payment.id
                pendingChoiceScope.launch {
                    val newAccountId = runCatching {
                        com.example.vibefinance.data.InMemoryDatabase.insertAccount(suggestedAccount)
                    }.getOrDefault(0L)
                    if (newAccountId > 0L) {
                        val recorded = runCatching {
                            PendingPaymentStore.accept(context, payment.id, newAccountId, rememberChoice = true)
                        }.getOrDefault(false)
                        savingPaymentId = null
                        snackbarHostState.showSnackbar(
                            if (recorded) context.getString(R.string.pending_payment_quick_created, suggestedAccount.name)
                            else context.getString(R.string.pending_payment_failed)
                        )
                    } else {
                        savingPaymentId = null
                        snackbarHostState.showSnackbar(context.getString(R.string.pending_payment_failed))
                    }
                }
            },
            onDetailCreate = { prefillAccount ->
                assetEditorState.open(prefill = prefillAccount, paymentId = payment.id, tab = selectedTab.name)
            },
            onLater = { deferredPaymentIds = deferredPaymentIds + payment.id },
            onIgnore = { PendingPaymentStore.ignore(context, payment.id) }
        )
    }

    // Focused budget configuration and its date picker retain their modal treatment.
    if (showBudgetDialog) {
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
                                    com.example.vibefinance.util.VibeHaptics.selection(hapticView)
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

        if (budgetConfigurationOnly) com.example.vibefinance.ui.settings.SettingsSheet(
            state = state,
            budgetInfo = budgetInfo,
            viewModel = viewModel,
            onDismiss = { budgetSheetDestination = budgetSheetDestination.dismissSettings() },
            onOpenNewPeriod = {
                budgetSheetDestination = BudgetSheetDestination.NEW_PERIOD
            },
            onOpenDatePicker = { showDatePickerModal = true },
            selectedEndDateMillis = selectedEndDateMillis,
            budgetConfigurationOnly = budgetConfigurationOnly
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
                budgetSheetDestination = BudgetSheetDestination.NONE
            },
            onDismiss = {
                budgetSheetDestination = budgetSheetDestination.dismissNewPeriod()
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
    hapticsEnabled: Boolean = true,
    corners: ConnectedButtonCorners = ConnectedButtonCorners.Standard,
    spatialMotionEnabled: Boolean = true,
    labelProvider: @Composable (T) -> String
) {
    val groupScroll = rememberScrollState()
    var pressedIndex by remember(items.size) { mutableStateOf<Int?>(null) }
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
            LaunchedEffect(isPressed.value) {
                if (isPressed.value) pressedIndex = index
                else if (pressedIndex == index) pressedIndex = null
            }
            DisposableEffect(interactionSource) {
                onDispose { if (pressedIndex == index) pressedIndex = null }
            }
            val spatialMotion = if (spatialMotionEnabled) {
                connectedSpatialMotion(index, pressedIndex, isPressed.value)
            } else Modifier
            val shapeProgress by rememberCompletePressProgress(interactionSource)

            val restingCorner = corners.selected
            val restingTopStart = if (isScrollable || isSelected) restingCorner else (if (index == 0) corners.outer else corners.inner)
            val restingBottomStart = restingTopStart
            val restingTopEnd = if (isScrollable || isSelected) restingCorner else (if (index == items.size - 1) corners.outer else corners.inner)
            val restingBottomEnd = restingTopEnd

            val pressedCorner = corners.pressed

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
            
            val haptic = rememberVibeHapticFeedback()
    val hapticView = androidx.compose.ui.platform.LocalView.current
            val view = LocalView.current

            Surface(
                color = colorMotion.containerColor,
                contentColor = colorMotion.contentColor,
                shape = shape,
                modifier = itemModifier
                    .then(itemModifierProvider?.invoke(index) ?: Modifier)
                    .then(if (isScrollable) Modifier.bringIntoViewRequester(revealRequester) else Modifier)
                    .then(spatialMotion)
                    .clip(shape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = true, color = colorMotion.rippleColor),
                        onClick = {
                            if (hapticsEnabled) {
                                com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                            }
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            onItemSelected(index)
                        }
                    )
            ) {
                Row(
                    modifier = Modifier
                        .then(colorMotion.contentModifier)
                        // Animate each chip's width, so following siblings move continuously.
                        // A fill-width parent alone cannot animate its children's positions.
                        .then(if (isScrollable) Modifier.animateContentSize(
                            spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow)
                        ) else Modifier)
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
                        softWrap = false,
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
    modifier: Modifier = Modifier,
    restingCorner: Dp = 20.dp,
    pressedCorner: Dp = 8.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val view = LocalView.current
    val haptic = rememberVibeHapticFeedback()
    val hapticView = androidx.compose.ui.platform.LocalView.current
    val shapeProgress by rememberCompletePressProgress(interactionSource)
    val cornerRadius = androidx.compose.ui.unit.lerp(restingCorner, pressedCorner, shapeProgress)
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
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        com.example.vibefinance.util.VibeHaptics.selection(hapticView)
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
                    maxLines = 1,
                    softWrap = false,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ModeSwitcherAnimatedIcon(
    mode: TransactionMode,
    tint: Color,
    isSelected: Boolean,
    clickTrigger: Int,
    modifier: Modifier = Modifier
) {
    val reducedMotion = com.example.vibefinance.ui.preferences.LocalExperience.current.motion != com.example.vibefinance.ui.preferences.MotionLevel.FULL
    val density = LocalDensity.current
    val densityFactor = density.density
    val iconSizePx = with(density) { 18.dp.toPx() }
    val scale = iconSizePx / 24f

    // Kinetic active scale (Material 3 Expressive pop when selected)
    val animScale = remember { Animatable(if (isSelected) 1.08f else 1f) }

    // Stroke draw-in / trim paths progress for Expense and Income (0f -> 1f)
    val drawProgress = remember { Animatable(1f) }

    // Directional slide offsets for Transfer:
    // Upper arrow slides right (positive X offset)
    // Lower arrow slides left (negative X offset)
    val upperSlideX = remember { Animatable(0f) }
    val lowerSlideX = remember { Animatable(0f) }

    // Pre-create and cache vector paths and PathMeasures for 0-allocation 120 FPS rendering
    val expenseLinePath = remember(scale) {
        Path().apply {
            moveTo(2.5f * scale, 7.5f * scale)
            lineTo(9.5f * scale, 14.5f * scale)
            lineTo(13.5f * scale, 10.5f * scale)
            lineTo(21.0f * scale, 18.0f * scale)
        }
    }
    val expenseHead1Path = remember(scale) {
        Path().apply {
            moveTo(21.0f * scale, 18.0f * scale)
            lineTo(15.0f * scale, 18.0f * scale)
        }
    }
    val expenseHead2Path = remember(scale) {
        Path().apply {
            moveTo(21.0f * scale, 18.0f * scale)
            lineTo(21.0f * scale, 12.0f * scale)
        }
    }

    val incomeLinePath = remember(scale) {
        Path().apply {
            moveTo(2.5f * scale, 16.5f * scale)
            lineTo(9.5f * scale, 9.5f * scale)
            lineTo(13.5f * scale, 13.5f * scale)
            lineTo(21.0f * scale, 6.0f * scale)
        }
    }
    val incomeHead1Path = remember(scale) {
        Path().apply {
            moveTo(21.0f * scale, 6.0f * scale)
            lineTo(15.0f * scale, 6.0f * scale)
        }
    }
    val incomeHead2Path = remember(scale) {
        Path().apply {
            moveTo(21.0f * scale, 6.0f * scale)
            lineTo(21.0f * scale, 12.0f * scale)
        }
    }

    val transferUpperPath = remember(scale) {
        Path().apply {
            moveTo(21f * scale, 9f * scale)
            lineTo(17.01f * scale, 5f * scale)
            lineTo(17.01f * scale, 8f * scale)
            lineTo(10f * scale, 8f * scale)
            lineTo(10f * scale, 10f * scale)
            lineTo(17.01f * scale, 10f * scale)
            lineTo(17.01f * scale, 13f * scale)
            close()
        }
    }
    val transferLowerPath = remember(scale) {
        Path().apply {
            moveTo(6.99f * scale, 11f * scale)
            lineTo(3f * scale, 15f * scale)
            lineTo(6.99f * scale, 19f * scale)
            lineTo(6.99f * scale, 16f * scale)
            lineTo(14f * scale, 16f * scale)
            lineTo(14f * scale, 14f * scale)
            lineTo(6.99f * scale, 14f * scale)
            close()
        }
    }

    val expenseLineMeasure = remember(expenseLinePath) {
        PathMeasure().apply { setPath(expenseLinePath, false) }
    }
    val expenseHead1Measure = remember(expenseHead1Path) {
        PathMeasure().apply { setPath(expenseHead1Path, false) }
    }
    val expenseHead2Measure = remember(expenseHead2Path) {
        PathMeasure().apply { setPath(expenseHead2Path, false) }
    }

    val incomeLineMeasure = remember(incomeLinePath) {
        PathMeasure().apply { setPath(incomeLinePath, false) }
    }
    val incomeHead1Measure = remember(incomeHead1Path) {
        PathMeasure().apply { setPath(incomeHead1Path, false) }
    }
    val incomeHead2Measure = remember(incomeHead2Path) {
        PathMeasure().apply { setPath(incomeHead2Path, false) }
    }

    val drawSegment = remember { Path() }
    val strokeWidthPx = with(density) { 2.1.dp.toPx() }
    val strokeStyle = remember(strokeWidthPx) {
        Stroke(
            width = strokeWidthPx,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    }

    LaunchedEffect(isSelected) {
        if (reducedMotion) {
            animScale.snapTo(if (isSelected) 1.08f else 1f)
        } else {
            animScale.animateTo(
                targetValue = if (isSelected) 1.08f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    LaunchedEffect(clickTrigger, isSelected) {
        if (clickTrigger == 0 && !isSelected) {
            drawProgress.snapTo(1f)
            upperSlideX.snapTo(0f)
            lowerSlideX.snapTo(0f)
            return@LaunchedEffect
        }

        if (reducedMotion) {
            drawProgress.snapTo(1f)
            upperSlideX.snapTo(0f)
            lowerSlideX.snapTo(0f)
            return@LaunchedEffect
        }

        when (mode) {
            TransactionMode.EXPENSE, TransactionMode.INCOME -> {
                if (isSelected) {
                    drawProgress.snapTo(0f)
                    drawProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = MotionTokens.emphasized(320)
                    )
                } else {
                    drawProgress.snapTo(1f)
                }
            }
            TransactionMode.TRANSFER -> {
                if (isSelected) {
                    launch {
                        upperSlideX.snapTo(0f)
                        upperSlideX.animateTo(
                            targetValue = 4.5f,
                            animationSpec = tween(durationMillis = 130, easing = FastOutSlowInEasing)
                        )
                        upperSlideX.animateTo(
                            targetValue = 0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                    }
                    launch {
                        lowerSlideX.snapTo(0f)
                        lowerSlideX.animateTo(
                            targetValue = -4.5f,
                            animationSpec = tween(durationMillis = 130, easing = FastOutSlowInEasing)
                        )
                        lowerSlideX.animateTo(
                            targetValue = 0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                    }
                } else {
                    upperSlideX.snapTo(0f)
                    lowerSlideX.snapTo(0f)
                }
            }
        }
    }

    Canvas(
        modifier = modifier
            .size(18.dp)
            .graphicsLayer {
                scaleX = animScale.value
                scaleY = animScale.value
            }
    ) {
        when (mode) {
            TransactionMode.EXPENSE -> {
                val p = drawProgress.value
                val lineProgress = (p / 0.70f).coerceIn(0f, 1f)
                val headProgress = ((p - 0.55f) / 0.45f).coerceIn(0f, 1f)

                if (lineProgress > 0f) {
                    drawSegment.reset()
                    expenseLineMeasure.getSegment(0f, expenseLineMeasure.length * lineProgress, drawSegment, true)
                    drawPath(drawSegment, color = tint, style = strokeStyle)
                }

                if (headProgress > 0f) {
                    drawSegment.reset()
                    expenseHead1Measure.getSegment(0f, expenseHead1Measure.length * headProgress, drawSegment, true)
                    drawPath(drawSegment, color = tint, style = strokeStyle)

                    drawSegment.reset()
                    expenseHead2Measure.getSegment(0f, expenseHead2Measure.length * headProgress, drawSegment, true)
                    drawPath(drawSegment, color = tint, style = strokeStyle)
                }
            }
            TransactionMode.INCOME -> {
                val p = drawProgress.value
                val lineProgress = (p / 0.70f).coerceIn(0f, 1f)
                val headProgress = ((p - 0.55f) / 0.45f).coerceIn(0f, 1f)

                if (lineProgress > 0f) {
                    drawSegment.reset()
                    incomeLineMeasure.getSegment(0f, incomeLineMeasure.length * lineProgress, drawSegment, true)
                    drawPath(drawSegment, color = tint, style = strokeStyle)
                }

                if (headProgress > 0f) {
                    drawSegment.reset()
                    incomeHead1Measure.getSegment(0f, incomeHead1Measure.length * headProgress, drawSegment, true)
                    drawPath(drawSegment, color = tint, style = strokeStyle)

                    drawSegment.reset()
                    incomeHead2Measure.getSegment(0f, incomeHead2Measure.length * headProgress, drawSegment, true)
                    drawPath(drawSegment, color = tint, style = strokeStyle)
                }
            }
            TransactionMode.TRANSFER -> {
                translate(left = upperSlideX.value * densityFactor, top = 0f) {
                    drawPath(transferUpperPath, color = tint)
                }
                translate(left = lowerSlideX.value * densityFactor, top = 0f) {
                    drawPath(transferLowerPath, color = tint)
                }
            }
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
    exchangeRateLoader: ExchangeRateLoader? = null,
    prefilledDestinationAccountId: Long? = null,
    selectorNavigationReady: Boolean = true
) {
    val context = LocalContext.current
    val localFocusManager = LocalFocusManager.current
    val haptic = rememberVibeHapticFeedback()
    val hapticView = androidx.compose.ui.platform.LocalView.current
    val draft = rememberSaveable(saver = AddTransactionDraft.Saver) {
        AddTransactionDraft(initialMode,
            state.accounts.sortedByDescending { accountFrequency[it.id] ?: 0 }
                .firstOrNull { it.id != prefilledDestinationAccountId }?.id,
            prefilledDestinationAccountId)
    }
    var typedAmount by draft.amount
    var calculatorEvaluated by draft.evaluated
    var entrySubmitting by remember { mutableStateOf(false) }
    val calculatorView = androidx.compose.ui.platform.LocalView.current
    val calculatorPress: (String) -> Unit = { key ->
        val next = com.example.vibefinance.util.KeypadCalculatorEngine.press(
            com.example.vibefinance.util.KeypadCalculatorEngine.State(typedAmount, calculatorEvaluated), key)
        typedAmount = next.expression; calculatorEvaluated = next.evaluated
    }
    var descriptionText by draft.description
    var expenseIcon by rememberSaveable { mutableStateOf<String?>(null) }
    var isDailyBudget by rememberSaveable { mutableStateOf(true) }

    val transactionMode by draft.mode
    val isTransfer = transactionMode == TransactionMode.TRANSFER
    var selectedCurrency by draft.currency
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
    val rawAmount = com.example.vibefinance.util.KeypadCalculatorEngine.result(typedAmount)?.toDouble()?.takeIf { it.isFinite() && it >= 0 }
    fun convertToHkd(amount: Double?): Double? = amount?.let {
        if (selectedCurrency == "HKD") it else rateQuote?.let { quote ->
            runCatching { quote.convert(it) }.getOrNull()
        }
    }
    val convertedAmount = convertToHkd(rawAmount)
    // A trailing operator keeps the completed expression's prediction while Apply stays disabled.
    val calculationPreview = com.example.vibefinance.util.KeypadCalculatorEngine.preview(typedAmount)
    val convertedPreviewAmount = convertToHkd(calculationPreview?.toDouble()?.takeIf { it.isFinite() && it >= 0 })
    val parsedAmount = typedAmount.toDoubleOrNull() ?: (rawAmount ?: (calculationPreview?.toDouble() ?: 0.0))
    val liveEffectiveAmount = convertToHkd(parsedAmount) ?: parsedAmount


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
    var expenseCategoryText by rememberSaveable { mutableStateOf(initialExpenseCat) }
    var incomeCategoryText by rememberSaveable { mutableStateOf(initialIncomeCat) }

    var selectedAccountId by draft.sourceAccountId
    var selectedToAccountId by draft.destinationAccountId
    val selectedAccount = state.accounts.firstOrNull { it.id == selectedAccountId }
    val selectedToAccount = state.accounts.firstOrNull { it.id == selectedToAccountId }

    var showInlineAddCategory by remember { mutableStateOf(false) }
    var isCategoryExpanded by rememberSaveable { mutableStateOf(false) }
    var showAccountPickerSheet by rememberSaveable { mutableStateOf(false) }
    var isPickingDestination by rememberSaveable { mutableStateOf(false) }
    var showInlineAddAccountInSheet by remember { mutableStateOf(false) }

    val selectorOverlay = rememberSaveable(saver = EntrySelectorOverlayState.Saver) {
        EntrySelectorOverlayState(null)
    }
    BackHandler(enabled = selectorOverlay.activeTag != null || selectorOverlay.pendingTag != null) { selectorOverlay.close() }
    val scrollState = rememberScrollState()
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

    val expressionPreview = if (typedAmount.drop(1).any { it == '+' || it == '-' }) {
        calculationPreview?.setScale(2)?.toPlainString() ?: "—"
    } else null
    CompositionLocalProvider(LocalEntrySelectorOverlay provides selectorOverlay,
        LocalEntrySelectorNavigationReady provides selectorNavigationReady) {
        BoxWithConstraints(modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface).testTag("EntryForm")) {
            // Short viewports (landscape/IME/large text) keep fields reachable by allowing the
            // hero to scroll with the form. The navigation row and 54dp keypad stay fixed.
            val scrollHero = maxHeight < 520.dp || LocalDensity.current.fontScale > 1.5f
            Column(Modifier.fillMaxSize()) {
                TransactionEntryTopBar(transactionMode, selectedCurrency, { selectedCurrency = it }, onDismissAddDialog)
                ScrollBlurContainer(scrollState, Modifier.weight(1f).fillMaxWidth().testTag("EntryScrollContent"),
                    vertical = true, tagPrefix = "EntryScrollContent", blurActive = selectorOverlay.activeTag == null) { source ->
                    Column(source.fillMaxWidth().verticalScroll(scrollState).padding(horizontal = 16.dp)) {
                        val modeChoices = remember { listOf(TransactionMode.EXPENSE, TransactionMode.INCOME, TransactionMode.TRANSFER) }
                        val isDarkTheme = LocalIsDarkTheme.current ?: (MaterialTheme.colorScheme.background.luminance() < 0.5f)
                        ExpressiveConnectedButtonGroup(
                            items = modeChoices,
                            selectedIndex = modeChoices.indexOf(transactionMode),
                            onItemSelected = { index ->
                                if (!entrySubmitting && draft.switchMode(modeChoices[index])) {
                                    showInlineAddCategory = false
                                    isCategoryExpanded = false
                                    com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 12.dp)
                                .testTag("TransactionModeSwitcher"),
                            inactiveContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            activeContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary,
                            activeContentColor = if (isDarkTheme) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary,
                            focusBackdropColor = MaterialTheme.colorScheme.surface,
                            itemModifierProvider = { index ->
                                Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("TransactionMode_${modeChoices[index].name}")
                            },
                            animatedIconProvider = { mode, tint, isSelected, clickTrigger ->
                                ModeSwitcherAnimatedIcon(
                                    mode = mode,
                                    tint = tint,
                                    isSelected = isSelected,
                                    clickTrigger = clickTrigger
                                )
                            },
                            labelProvider = { mode ->
                                stringResource(
                                    when (mode) {
                                        TransactionMode.EXPENSE -> R.string.filter_expense
                                        TransactionMode.INCOME -> R.string.filter_income
                                        TransactionMode.TRANSFER -> R.string.filter_transfer
                                    }
                                )
                            }
                        )

                        val currencyInfo = remember(selectedCurrency) {
                            CurrencyEngine.supportedCurrencies.firstOrNull { it.code == selectedCurrency }
                                ?: CurrencyEngine.supportedCurrencies.first()
                        }
                        val currencySymbol = if (selectedCurrency == "HKD") "HK$" else currencyInfo.symbol
                        val amountFocusRequester = remember { FocusRequester() }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    amountFocusRequester.requestFocus()
                                },
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$currencySymbol ",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            BasicTextField(
                                value = if (typedAmount == "0") "" else typedAmount,
                                onValueChange = { input ->
                                    if (input.isEmpty()) {
                                        typedAmount = "0"
                                    } else if (input == ".") {
                                        typedAmount = "0."
                                    } else if (input.matches(Regex("""^\d*(?:\.\d{0,2})?$"""))) {
                                        typedAmount = input
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                textStyle = MaterialTheme.typography.displaySmall.copy(
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Start
                                ),
                                visualTransformation = if (com.example.vibefinance.ui.preferences.LocalExperience.current.hideAmounts)
                                    androidx.compose.ui.text.input.PasswordVisualTransformation()
                                else
                                    androidx.compose.ui.text.input.VisualTransformation.None,
                                modifier = Modifier
                                    .width(IntrinsicSize.Min)
                                    .widthIn(min = 32.dp)
                                    .focusRequester(amountFocusRequester)
                                    .testTag("EntryAmount")
                                    .semantics { stateDescription = typedAmount },
                                decorationBox = { innerTextField ->
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        if (typedAmount == "0" || typedAmount.isEmpty()) {
                                            Text(
                                                text = "0",
                                                style = MaterialTheme.typography.displaySmall.copy(
                                                    fontSize = 42.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                                )
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                            if (typedAmount.isNotEmpty() && typedAmount != "0") {
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = {
                                        typedAmount = "0"
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        if (selectedCurrency != "HKD") {
                            CurrencyRateStatus(selectedCurrency,
                                rateResult?.takeIf { it.quote == null || it.quote.currencyCode == selectedCurrency },
                                rateLoading || rateResult == null, convertedPreviewAmount,
                                onRefresh = { rateRefreshRequest++ })
                        }

                        // ==========================================
                        // BENTO CARD 1: Budget & Note (RoundedCornerShape(22.dp))
                        // ==========================================
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AnimatedVisibility(
                                    visible = transactionMode == TransactionMode.EXPENSE,
                            enter = fadeIn(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)) +
                                expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow), expandFrom = Alignment.Top),
                            exit = fadeOut(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)) +
                                shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow), shrinkTowards = Alignment.Top)
                        ) {
                            Column(Modifier.fillMaxWidth().testTag("EntryBudgetSection")) {
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
                                                com.example.vibefinance.util.VibeHaptics.selection(hapticView)
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
                                        AppSwitch(
                                            checked = isDailyBudget,
                                            onCheckedChange = {
                                                isDailyBudget = it
                                                com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                            }
                                        )
                                    }
                                }

                                // 2. Budget Preview Card (Dynamic calculation / Non-daily notice)
                                val budgetInfo = state.budgetInfo
                                if (budgetInfo != null && convertedPreviewAmount != null) {
                                    val inputAmount = convertedPreviewAmount
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
                        }

                        AnimatedVisibility(
                            visible = transactionMode == TransactionMode.TRANSFER,
                            enter = fadeIn(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)) +
                                expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow), expandFrom = Alignment.Top),
                            exit = fadeOut(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)) +
                                shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow), shrinkTowards = Alignment.Top)
                        ) {
                            LiveTransferBalanceFlowCard(
                                sourceAccount = selectedAccount,
                                destAccount = selectedToAccount,
                                amount = liveEffectiveAmount,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            )
                        }

                        AnimatedVisibility(
                            visible = transactionMode == TransactionMode.INCOME,
                            enter = fadeIn(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)) +
                                expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow), expandFrom = Alignment.Top),
                            exit = fadeOut(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)) +
                                shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow), shrinkTowards = Alignment.Top)
                        ) {
                            LiveIncomeAssetBoostCard(
                                receivingAccount = selectedAccount,
                                allAccounts = state.accounts,
                                amount = liveEffectiveAmount,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            )
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
                            placeholder = { Text(stringResource(when (transactionMode) {
                                TransactionMode.EXPENSE -> R.string.ui_main_note_hint_expense
                                TransactionMode.INCOME -> R.string.ui_main_note_hint_income
                                TransactionMode.TRANSFER -> R.string.ui_main_note_hint_transfer
                            }), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f), style = MaterialTheme.typography.bodyMedium) },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = { receiptPickerLauncher.launch("image/*") },
                                    modifier = Modifier.size(48.dp)
                                ) {
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
                            modifier = Modifier.fillMaxWidth().testTag("EntryDescription")
                        )
                    }
                }

                val resources = LocalContext.current.resources
                val isZh = remember(resources) {
                    resources.configuration.locales[0].language.startsWith("zh")
                }

                // ==========================================
                // BENTO CARD 2: Classification & Asset (RoundedCornerShape(22.dp))
                // ==========================================
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
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
                                // Header Row: Label + Expand/Collapse Toggle
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (transactionMode == TransactionMode.INCOME)
                                            stringResource(R.string.ui_main_income_category)
                                        else
                                            stringResource(R.string.sub_category_label),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                    )
                                    val categoryArrowRotation by animateFloatAsState(
                                        targetValue = if (isCategoryExpanded) 180f else 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        ),
                                        label = "EntryCategory_arrowRotation"
                                    )
                                    Surface(
                                        onClick = {
                                            com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                            isCategoryExpanded = !isCategoryExpanded
                                        },
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.testTag("EntryCategory_toggle")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            val toggleText = if (isCategoryExpanded) {
                                                if (isZh) "收合" else stringResource(R.string.settings_section_collapsed)
                                            } else {
                                                if (isZh) "展開全部" else stringResource(R.string.settings_section_expanded)
                                            }
                                            Text(
                                                text = toggleText,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .graphicsLayer { rotationZ = categoryArrowRotation }
                                            )
                                        }
                                    }
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
                                                    com.example.vibefinance.util.VibeHaptics.selection(hapticView)
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

                                androidx.compose.runtime.key(transactionMode) {
                                    val incomeMode = transactionMode == TransactionMode.INCOME
                                    val currentCategories = if (incomeMode) sortedIncomeCategories else sortedExpenseCategories
                                    val currentCategoryText = if (incomeMode) incomeCategoryText else expenseCategoryText
                                    val deduplicatedCategories = remember(currentCategories, resources) {
                                        currentCategories.distinctBy { getCategoryDisplayNameFromResources(resources, it) }
                                    }
                                    val categoryListState = rememberLazyListState()
                                    LaunchedEffect(currentCategoryText, deduplicatedCategories.size) {
                                        val selectedIdx = deduplicatedCategories.indexOfFirst {
                                            getCategoryDisplayNameFromResources(resources, it).equals(getCategoryDisplayNameFromResources(resources, currentCategoryText), ignoreCase = true)
                                        }
                                        if (selectedIdx >= 0) {
                                            categoryListState.animateScrollToItem((selectedIdx - 1).coerceAtLeast(0))
                                        }
                                    }

                                    AnimatedContent(
                                        targetState = isCategoryExpanded,
                                        transitionSpec = {
                                            if (targetState) {
                                                (fadeIn(
                                                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                                                ) + expandVertically(
                                                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
                                                    expandFrom = Alignment.Top
                                                )).togetherWith(
                                                    fadeOut(
                                                        animationSpec = tween(90, easing = LinearEasing)
                                                    ) + shrinkVertically(
                                                        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
                                                        shrinkTowards = Alignment.Top
                                                    )
                                                )
                                            } else {
                                                (fadeIn(
                                                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                                                ) + expandVertically(
                                                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
                                                    expandFrom = Alignment.Top
                                                )).togetherWith(
                                                    fadeOut(
                                                        animationSpec = tween(90, easing = LinearEasing)
                                                    ) + shrinkVertically(
                                                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
                                                        shrinkTowards = Alignment.Top
                                                    )
                                                )
                                            }.using(
                                                SizeTransform(clip = false) { _, _ ->
                                                    spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                                                }
                                            )
                                        },
                                        label = "CategoryExpansionTransition",
                                        modifier = Modifier.fillMaxWidth()
                                    ) { expanded ->
                                        if (!expanded) {
                                            LazyRow(
                                                state = categoryListState,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalFadingEdge(categoryListState, 16.dp)
                                                    .testTag("EntryCategory")
                                            ) {
                                                items(deduplicatedCategories, key = { it }) { cat ->
                                                    val isSelected = getCategoryDisplayNameFromResources(resources, currentCategoryText).equals(getCategoryDisplayNameFromResources(resources, cat), ignoreCase = true)
                                                    FilterChip(
                                                        selected = isSelected,
                                                        onClick = {
                                                            com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                                            if (incomeMode) {
                                                                incomeCategoryText = cat
                                                            } else {
                                                                expenseCategoryText = cat
                                                            }
                                                        },
                                                        leadingIcon = {
                                                            CategoryIcon(
                                                                category = cat,
                                                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        },
                                                        label = {
                                                            Text(
                                                                com.example.vibefinance.ui.home.getCategoryDisplayName(cat),
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                                maxLines = 1,
                                                                softWrap = false,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        },
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier.testTag("EntryCategory_row_$cat")
                                                    )
                                                }
                                                item(key = "__add_category__") {
                                                    FilterChip(
                                                        selected = showInlineAddCategory,
                                                        onClick = {
                                                            com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                                            showInlineAddCategory = !showInlineAddCategory
                                                        },
                                                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                                        label = { Text(stringResource(R.string.btn_add)) },
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier.testTag("EntryCategory_row_add")
                                                    )
                                                }
                                            }
                                        } else {
                                            FlowRow(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("EntryCategory_cloud"),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                deduplicatedCategories.forEach { cat ->
                                                    val isSelected = getCategoryDisplayNameFromResources(resources, currentCategoryText).equals(getCategoryDisplayNameFromResources(resources, cat), ignoreCase = true)
                                                    FilterChip(
                                                        selected = isSelected,
                                                        onClick = {
                                                            com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                                            if (incomeMode) {
                                                                incomeCategoryText = cat
                                                            } else {
                                                                expenseCategoryText = cat
                                                            }
                                                            isCategoryExpanded = false
                                                            scope.launch {
                                                                val idx = deduplicatedCategories.indexOf(cat)
                                                                if (idx >= 0) {
                                                                    categoryListState.animateScrollToItem((idx - 1).coerceAtLeast(0))
                                                                }
                                                            }
                                                        },
                                                        leadingIcon = {
                                                            CategoryIcon(
                                                                category = cat,
                                                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        },
                                                        label = {
                                                            Text(
                                                                com.example.vibefinance.ui.home.getCategoryDisplayName(cat),
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                                maxLines = 1,
                                                                softWrap = false,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        },
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier.testTag("EntryCategory_row_$cat")
                                                    )
                                                }
                                                FilterChip(
                                                    selected = showInlineAddCategory,
                                                    onClick = {
                                                        com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                                        showInlineAddCategory = !showInlineAddCategory
                                                    },
                                                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                                    label = { Text(stringResource(R.string.btn_add)) },
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.testTag("EntryCategory_row_add")
                                                )
                                            }
                                        }
                                    }
                                }
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

                        // 3. Full-Width Selected Asset Card & Destination Card in Transfer Mode
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("EntryAssetSection"),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = if (isTransfer) stringResource(R.string.transfer_from) else stringResource(R.string.ui_main_asset_selection),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                            )

                            SelectedAccountCard(
                                account = selectedAccount,
                                isDestination = false,
                                onClick = {
                                    com.example.vibefinance.util.VibeHaptics.lightTick(hapticView)
                                    isPickingDestination = false
                                    showAccountPickerSheet = true
                                },
                                modifier = Modifier.testTag("EntryAccount_card")
                            )

                            // Destination Account Card in Transfer mode
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
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.ui_main_destination_account),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                    )
                                    SelectedAccountCard(
                                        account = selectedToAccount,
                                        isDestination = true,
                                        onClick = {
                                            com.example.vibefinance.util.VibeHaptics.lightTick(hapticView)
                                            isPickingDestination = true
                                            showAccountPickerSheet = true
                                        },
                                        modifier = Modifier.testTag("EntryDestination_card")
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))
                    }
                }
                val amt = rawAmount
                val isApplyEnabled = !entrySubmitting && amt != null && amt > 0 && convertedAmount != null &&
                    (selectedCurrency == "HKD" || (!rateLoading && rateQuote != null)) &&
                    draft.hasValidAccounts(selectedAccount?.id, selectedToAccount?.id)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("EntryKeypad")
                        .onGloballyPositioned { selectorOverlay.keypadBounds = it.boundsInWindow() },
                    color = Color.Transparent
                ) {
                    Button(
                        onClick = {
                            val accId = selectedAccount?.id
                            if (isApplyEnabled && accId != null && amt != null && !entrySubmitting) {
                                entrySubmitting = true
                                com.example.vibefinance.util.VibeHaptics.successConfirm(calculatorView)
                                val finalHkdAmt = convertedAmount
                                val descWithFx = if (selectedCurrency != "HKD") {
                                    val origStr = com.example.vibefinance.util.CurrencyEngine.formatOriginal(amt, selectedCurrency)
                                    val quote = rateQuote ?: return@Button
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
                        },
                        enabled = isApplyEnabled,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("EntrySubmit")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(
                                when (transactionMode) {
                                    TransactionMode.EXPENSE -> R.string.btn_save_expense
                                    TransactionMode.INCOME -> R.string.btn_save_income
                                    TransactionMode.TRANSFER -> R.string.btn_confirm_transfer
                                }
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

            }
        }
    }

    if (showAccountPickerSheet) {
        val selectableAccounts = remember(sortedAccounts, selectedAccountId, isPickingDestination) {
            if (isPickingDestination) {
                sortedAccounts.filter { it.id != selectedAccountId }
            } else {
                sortedAccounts
            }
        }
        AppModalBottomSheet(
            onDismissRequest = {
                showAccountPickerSheet = false
                showInlineAddAccountInSheet = false
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(if (isPickingDestination) R.string.ui_main_destination_account else R.string.ui_main_asset_selection),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = {
                        showAccountPickerSheet = false
                        showInlineAddAccountInSheet = false
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Account List (2 Columns Grid)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(selectableAccounts, key = { it.id }) { acc ->
                        val isSelected = acc.id == (if (isPickingDestination) selectedToAccountId else selectedAccountId)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                    if (isPickingDestination) {
                                        selectedToAccountId = acc.id
                                    } else {
                                        selectedAccountId = acc.id
                                        if (isTransfer && selectedToAccountId == selectedAccountId) {
                                            selectedToAccountId = null
                                        }
                                    }
                                    showAccountPickerSheet = false
                                    showInlineAddAccountInSheet = false
                                }
                                .testTag(if (isPickingDestination) "EntryDestination_row_${acc.id}" else "EntryAccount_row_${acc.id}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = getAccountIcon(acc.type),
                                                contentDescription = null,
                                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                                        ) {
                                            Text(
                                                text = accountTypeLabel(acc.type),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = acc.nickname?.takeIf { it.isNotBlank() } ?: acc.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val formattedBal = CurrencyEngine.formatSignedCompact(acc.balance, prefix = "HK$")
                                val balanceLabel = if (acc.type == AccountType.CC)
                                    stringResource(R.string.ui_recurring_account_owing, formattedBal)
                                else
                                    stringResource(R.string.ui_recurring_account_balance, formattedBal)
                                Text(
                                    text = balanceLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Inline "+ 新增帳戶" Toggle & Form
                OutlinedButton(
                    onClick = {
                        showInlineAddAccountInSheet = !showInlineAddAccountInSheet
                        com.example.vibefinance.util.VibeHaptics.lightTick(hapticView)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("AccountPicker_AddAccountToggle"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (showInlineAddAccountInSheet) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (showInlineAddAccountInSheet) stringResource(R.string.btn_cancel) else stringResource(R.string.ui_main_add_account))
                }

                AnimatedVisibility(
                    visible = showInlineAddAccountInSheet,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            Text(stringResource(R.string.ui_main_account_type), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.25f))
                            ExpressiveSegmentedButtonGroup(
                                corners = ConnectedButtonCorners.Entry,
                                spatialMotionEnabled = false,
                                items = AccountType.entries,
                                selectedIndex = AccountType.entries.indexOf(selectedType),
                                onItemSelected = { selectedType = AccountType.entries[it] },
                                modifier = Modifier.weight(1f),
                                isScrollable = true,
                                labelProvider = {
                                    when (it) {
                                        AccountType.CASH -> stringResource(R.string.acc_type_cash)
                                        AccountType.BANK -> stringResource(R.string.ui_main_bank_short)
                                        AccountType.DEBIT -> stringResource(R.string.ui_main_debit_short)
                                        AccountType.CC -> stringResource(R.string.ui_main_credit_short)
                                        AccountType.INVESTMENT -> stringResource(R.string.assets_type_investment)
                                    }
                                }
                            )
                        }

                        if (selectedType == AccountType.INVESTMENT) {
                            Text(
                                stringResource(R.string.assets_investment_notice),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                visualTransformation = if (com.example.vibefinance.ui.preferences.LocalExperience.current.hideAmounts)
                                    androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
                                value = initialBalanceText,
                                onValueChange = { initialBalanceText = it },
                                placeholder = { Text(if (selectedType == AccountType.CC) stringResource(R.string.ui_main_credit_limit_hint) else stringResource(R.string.ui_main_initial_balance_hint)) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            val addAccountInteraction = remember { MutableInteractionSource() }
                            CompletePressButton(
                                onClick = {
                                    com.example.vibefinance.util.VibeHaptics.selection(hapticView)
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
                                                AccountType.INVESTMENT -> "investment"
                                            },
                                            creditLimit = if (selectedType == AccountType.CC) bal else null
                                        )
                                        onIntent(com.example.vibefinance.ui.FinanceIntent.SaveAccount(newAccount))
                                    }
                                    newAssetName = ""
                                    initialBalanceText = ""
                                    showInlineAddAccountInSheet = false
                                    showAccountPickerSheet = false
                                },
                                shapes = ButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                                interactionSource = addAccountInteraction,
                                modifier = Modifier
                                    .height(48.dp)
                                    .pressBounce(interactionSource = addAccountInteraction)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.btn_save))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedAccountCard(
    account: AccountEntity?,
    isDestination: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .bouncyClickable(shape = RoundedCornerShape(16.dp), onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getAccountIcon(account?.type),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = account?.let { it.nickname?.takeIf { n -> n.isNotBlank() } ?: it.name }
                            ?: stringResource(if (isDestination) R.string.ui_main_destination_account else R.string.ui_main_asset_selection),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    account?.let { acc ->
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = accountTypeLabel(acc.type),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                account?.let { acc ->
                    val formattedBal = CurrencyEngine.formatSignedCompact(acc.balance, prefix = "HK$")
                    val balanceLabel = if (acc.type == AccountType.CC)
                        stringResource(R.string.ui_recurring_account_owing, formattedBal)
                    else
                        stringResource(R.string.ui_recurring_account_balance, formattedBal)
                    Text(
                        text = balanceLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(R.string.ui_main_asset_selection),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

private fun getAccountIcon(type: AccountType?): ImageVector {
    return when (type) {
        AccountType.CASH -> Icons.Default.Savings
        AccountType.BANK -> Icons.Default.AccountBalance
        AccountType.DEBIT -> Icons.Default.CreditCard
        AccountType.CC -> Icons.Default.ShoppingCart
        AccountType.INVESTMENT -> Icons.AutoMirrored.Filled.TrendingUp
        null -> Icons.Default.AccountBalance
    }
}

private fun getCategoryDisplayNameFromResources(resources: Resources, category: String): String {
    val normalized = category.trim().replaceFirst(Regex("^[^\\p{L}\\p{N}]+"), "").lowercase(Locale.US).trim()
    return when (normalized) {
        "food", "food & drink", "restaurant", "food & dining" -> resources.getString(R.string.cat_food)
        "transport", "transit", "commute", "bus", "taxi" -> resources.getString(R.string.cat_transport)
        "shopping", "clothing" -> resources.getString(R.string.cat_shopping)
        "groceries", "supermarket" -> resources.getString(R.string.cat_groceries)
        "electronics", "gadgets" -> resources.getString(R.string.cat_electronics)
        "entertainment", "movie", "games", "gaming" -> resources.getString(R.string.cat_entertainment)
        "utilities", "electricity", "water", "bills" -> resources.getString(R.string.cat_utilities)
        "housing", "rent" -> resources.getString(R.string.cat_housing)
        "health", "medical", "health & medical" -> resources.getString(R.string.cat_health)
        "software / ai", "software", "ai", "subscription", "recurring" -> resources.getString(R.string.cat_software_ai)
        "education", "tuition" -> resources.getString(R.string.cat_education)
        "salary", "paycheck", "wages", "income" -> resources.getString(R.string.cat_salary)
        "bonus", "reward" -> resources.getString(R.string.cat_bonus)
        "investment", "dividend", "stocks" -> resources.getString(R.string.cat_investment)
        "transfer" -> resources.getString(R.string.filter_transfer)
        "part-time", "freelance", "side hustle" -> resources.getString(R.string.loc_cat_part_time)
        "gift", "redpocket", "allowance" -> resources.getString(R.string.loc_cat_gift)
        "other income" -> resources.getString(R.string.loc_cat_other_income)
        "fitness", "gym" -> resources.getString(R.string.loc_cat_fitness)
        "travel", "holiday" -> resources.getString(R.string.loc_cat_travel)
        "others", "other" -> resources.getString(R.string.cat_others)
        else -> category
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

private fun formatLiveBalance(v: Double): String =
    if (v < 0.0) String.format(Locale.US, "-$%,.2f", kotlin.math.abs(v))
    else String.format(Locale.US, "$%,.2f", v)

@Composable
private fun LiveTransferBalanceFlowCard(
    sourceAccount: AccountEntity?,
    destAccount: AccountEntity?,
    amount: Double,
    modifier: Modifier = Modifier
) {
    val isSourceCc = sourceAccount?.type == AccountType.CC
    val isDestCc = destAccount?.type == AccountType.CC
    val sourceInitial = sourceAccount?.balance ?: 0.0
    val sourceProjected = if (isSourceCc) sourceInitial + amount else sourceInitial - amount
    val isInsufficient = !isSourceCc && amount > 0.0 && sourceInitial < amount
    val destInitial = destAccount?.balance ?: 0.0
    val destProjected = if (isDestCc) destInitial - amount else destInitial + amount
    val isDebtCleared = isDestCc && amount > 0.0 && destProjected <= 0.0

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(
            1.dp,
            if (isInsufficient) MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        modifier = modifier.testTag("LiveTransferBalanceFlowCard")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.live_transfer_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (isInsufficient) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.live_transfer_insufficient),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                } else if (isDebtCleared) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.live_transfer_debt_cleared),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Source account column
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.live_transfer_from),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = sourceAccount?.let { it.nickname ?: it.name } ?: "—",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.live_transfer_current, formatLiveBalance(sourceInitial)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "➔ ",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isInsufficient) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        RollingNumberText(
                            text = formatLiveBalance(sourceProjected),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isInsufficient) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Transfer Arrow & Amount indicator in the middle
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    if (amount > 0.0) {
                        RollingNumberText(
                            text = formatLiveBalance(amount),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Destination account column
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.live_transfer_to),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (destAccount == null) {
                        Text(
                            text = stringResource(R.string.live_transfer_select_dest),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 2
                        )
                    } else {
                        Text(
                            text = destAccount.nickname ?: destAccount.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.live_transfer_current, formatLiveBalance(destInitial)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "➔ ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            RollingNumberText(
                                text = formatLiveBalance(destProjected),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDebtCleared) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveIncomeAssetBoostCard(
    receivingAccount: AccountEntity?,
    allAccounts: List<AccountEntity>,
    amount: Double,
    modifier: Modifier = Modifier
) {
    val isCc = receivingAccount?.type == AccountType.CC
    val isInvestment = receivingAccount?.type == AccountType.INVESTMENT
    val initialBalance = receivingAccount?.balance ?: 0.0
    val projectedBalance = if (isCc) initialBalance - amount else initialBalance + amount
    val liquidAccounts = allAccounts.filter { it.type != AccountType.CC && it.type != AccountType.INVESTMENT }
    val currentLiquidTotal = liquidAccounts.sumOf { it.balance }
    val projectedLiquidTotal = currentLiquidTotal + if (!isCc && !isInvestment) amount else 0.0

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = modifier.testTag("LiveIncomeAssetBoostCard")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.live_income_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (amount > 0.0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                    ) {
                        Text(
                            text = stringResource(R.string.live_income_boost_badge, formatLiveBalance(amount)),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left metric: Receiving Account
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = receivingAccount?.let { it.nickname ?: it.name } ?: stringResource(R.string.live_income_account_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.live_income_current, formatLiveBalance(initialBalance)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "➔ ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            RollingNumberText(
                                text = formatLiveBalance(projectedBalance),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Right metric: Total Liquid Assets
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = stringResource(R.string.live_income_total_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.live_income_current, formatLiveBalance(currentLiquidTotal)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "➔ ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            RollingNumberText(
                                text = formatLiveBalance(projectedLiquidTotal),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
