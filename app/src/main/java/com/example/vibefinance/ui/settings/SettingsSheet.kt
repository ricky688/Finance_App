@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.settings

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.vibefinance.ui.common.pressBounce
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.FilledTonalButton
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import com.example.vibefinance.theme.IconShapeMode
import com.example.vibefinance.theme.LocalIconShape
import com.example.vibefinance.theme.ScallopBadgeShape
import com.example.vibefinance.theme.paletteColorScheme
import com.example.vibefinance.ui.components.ExpressiveSwitch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vibefinance.R
import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.data.entity.RolloverMode
import com.example.vibefinance.data.repository.DailyBudgetInfo
import com.example.vibefinance.theme.ThemeMode
import com.example.vibefinance.ui.AppLanguage
import com.example.vibefinance.ui.FinanceIntent
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.FinanceViewModel
import com.example.vibefinance.util.LocalAppManager
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private enum class SettingsGroup {
    APPEARANCE, LANGUAGE, SMART_LOGGING, BUDGET, PACING, PRIVACY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    state: FinanceUiState,
    budgetInfo: DailyBudgetInfo,
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit,
    onOpenNewPeriod: () -> Unit,
    onOpenDatePicker: () -> Unit,
    selectedEndDateMillis: Long?
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onDismiss()
            val fileName = com.example.vibefinance.util.FinancialDataImportEngine.getFileNameFromUri(context, uri)
            viewModel.dispatch(FinanceIntent.AnalyzeImportFile(uri, fileName))
        }
    }

    // Local mutable state for editing budget limits and category caps
    var budgetAmountText by remember(budgetInfo.totalMonthlyBudget) {
        mutableStateOf(
            if (budgetInfo.totalMonthlyBudget > 0.0) {
                if (budgetInfo.totalMonthlyBudget % 1.0 == 0.0) {
                    budgetInfo.totalMonthlyBudget.toInt().toString()
                } else {
                    String.format(Locale.US, "%.2f", budgetInfo.totalMonthlyBudget)
                }
            } else ""
        )
    }

    var selectedRolloverMode by remember(budgetInfo.rolloverMode) {
        mutableStateOf(budgetInfo.rolloverMode)
    }

    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showAppSelectionDialog by remember { mutableStateOf(false) }
    var showPaletteSheet by remember { mutableStateOf(false) }
    var showShapePickerDialog by rememberSaveable { mutableStateOf(false) }
    var expandedSection by rememberSaveable { mutableStateOf<String?>(SettingsGroup.APPEARANCE.name) }

    // Intercepted apps flow
    val selectedApps by InMemoryDatabase.selectedInterceptApps.collectAsStateWithLifecycle()

    // Notification Permission State Sync
    var autoLogEnabled by remember {
        mutableStateOf(
            InMemoryDatabase.isNotificationLoggingEnabled &&
                    androidx.core.app.NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
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

    // Reset Database Confirmation Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.settings_reset_confirm_title),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.settings_reset_confirm_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                val confirmInteraction = remember { MutableInteractionSource() }
                val haptic = LocalHapticFeedback.current
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.dispatch(FinanceIntent.SeedMockData)
                        showResetConfirmDialog = false
                        onDismiss()
                    },
                    shapes = ButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                    interactionSource = confirmInteraction,
                    modifier = Modifier.pressBounce(interactionSource = confirmInteraction),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.btn_confirm), color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                val dismissInteraction = remember { MutableInteractionSource() }
                val haptic = LocalHapticFeedback.current
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showResetConfirmDialog = false
                    },
                    shapes = ButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                    interactionSource = dismissInteraction,
                    modifier = Modifier.pressBounce(interactionSource = dismissInteraction)
                ) {
                    Text(stringResource(R.string.btn_cancel))
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showAppSelectionDialog) {
        com.example.vibefinance.ui.main.AllowedInterceptAppsDialog(onDismiss = { showAppSelectionDialog = false })
    }

    if (showShapePickerDialog) {
        IconShapePickerDialog(
            currentShape = state.iconShape,
            onSelectShape = { mode ->
                viewModel.dispatch(FinanceIntent.SetIconShape(mode))
            },
            onDismiss = { showShapePickerDialog = false }
        )
    }

    // Predictive back for nested dialogs and sheets
    androidx.activity.compose.PredictiveBackHandler(enabled = showPaletteSheet) { progressFlow ->
        try {
            progressFlow.collect { }
            showPaletteSheet = false
        } catch (e: kotlinx.coroutines.CancellationException) {
        }
    }

    androidx.activity.compose.PredictiveBackHandler(enabled = showResetConfirmDialog && !showPaletteSheet) { progressFlow ->
        try {
            progressFlow.collect { }
            showResetConfirmDialog = false
        } catch (e: kotlinx.coroutines.CancellationException) {
        }
    }

    androidx.activity.compose.PredictiveBackHandler(enabled = showAppSelectionDialog && !showPaletteSheet && !showResetConfirmDialog) { progressFlow ->
        try {
            progressFlow.collect { }
            showAppSelectionDialog = false
        } catch (e: kotlinx.coroutines.CancellationException) {
        }
    }

    androidx.activity.compose.PredictiveBackHandler(enabled = showShapePickerDialog && !showPaletteSheet && !showResetConfirmDialog && !showAppSelectionDialog) { progressFlow ->
        try {
            progressFlow.collect { }
            showShapePickerDialog = false
        } catch (e: kotlinx.coroutines.CancellationException) {
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            saveChanges(
                budgetAmountText,
                selectedEndDateMillis,
                budgetInfo,
                selectedRolloverMode,
                viewModel
            )
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // Expressive headline stays visible while the settings groups scroll.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(17.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(26.dp)
                        )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.settings_sheet_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.settings_sheet_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = {
                        saveChanges(
                            budgetAmountText,
                            selectedEndDateMillis,
                            budgetInfo,
                            selectedRolloverMode,
                            viewModel
                        )
                        onDismiss()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.btn_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ==========================================
                // ==========================================
                // SECTION 1: CUSTOMIZATION (自訂)
                // ==========================================
                val isDark = when (state.themeMode) {
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                }

                SettingsSectionContainer(
                    title = stringResource(R.string.settings_section_appearance),
                    icon = Icons.Filled.Palette,
                    expanded = expandedSection == SettingsGroup.APPEARANCE.name,
                    onToggle = {
                        expandedSection = if (expandedSection == SettingsGroup.APPEARANCE.name) null
                        else SettingsGroup.APPEARANCE.name
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Item 1: 色彩方案 (Color scheme) -> Opens AppearancePickerSheet
                        Surface(
                            onClick = { showPaletteSheet = true },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, LocalIconShape.current),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Palette,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.appearance_palette_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = stringResource(R.string.appearance_palette_desc),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                // Scallop Rosette badge with live preview & edit pencil
                                val currentPreview = remember(state.appearancePalette, isDark, state.appearanceContrast) {
                                    paletteColorScheme(state.appearancePalette, isDark, state.appearanceContrast)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.65f),
                                            ScallopBadgeShape
                                        )
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                            ScallopBadgeShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Circular color disc
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                    ) {
                                        Column(Modifier.fillMaxSize()) {
                                            Box(
                                                Modifier
                                                    .fillMaxWidth()
                                                    .weight(1f)
                                                    .background(currentPreview.primary)
                                            )
                                            Row(Modifier.fillMaxWidth().weight(1f)) {
                                                Box(
                                                    Modifier
                                                        .fillMaxHeight()
                                                        .weight(1f)
                                                        .background(currentPreview.tertiary)
                                                )
                                                Box(
                                                    Modifier
                                                        .fillMaxHeight()
                                                        .weight(1f)
                                                        .background(currentPreview.secondary)
                                                )
                                            }
                                        }
                                    }
                                    // Center edit badge
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .background(currentPreview.primary, CircleShape)
                                            .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = null,
                                            tint = currentPreview.onPrimary,
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Item 2: 動態色彩 (Dynamic color)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    viewModel.dispatch(FinanceIntent.SetDynamicColorEnabled(!state.dynamicColorEnabled))
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, LocalIconShape.current),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Colorize,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.dynamic_color_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = stringResource(R.string.dynamic_color_desc),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                ExpressiveSwitch(
                                    checked = state.dynamicColorEnabled,
                                    onCheckedChange = { enabled ->
                                        viewModel.dispatch(FinanceIntent.SetDynamicColorEnabled(enabled))
                                    }
                                )
                            }
                        }

                        // Item 3: 純黑深色模式 (Pure black dark mode)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, LocalIconShape.current),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DarkMode,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.appearance_pure_black_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = stringResource(R.string.appearance_pure_black_desc),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                ExpressiveSwitch(
                                    checked = state.pureBlackDarkMode,
                                    onCheckedChange = { viewModel.dispatch(FinanceIntent.SetPureBlackDarkMode(it)) }
                                )
                            }
                        }

                        // Item 4: 圖示形狀 (Icon shape)
                        Surface(
                            onClick = { showShapePickerDialog = true },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, LocalIconShape.current),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.appearance_icon_shape_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    val isZh = androidx.compose.ui.platform.LocalConfiguration.current.locales[0].language.startsWith("zh")
                                    Text(
                                        text = "${state.iconShape.localizedTitle(isZh)} · ${stringResource(R.string.appearance_icon_shape_desc)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.65f),
                                            state.iconShape.shape
                                        )
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                            state.iconShape.shape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .border(
                                                2.dp,
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                state.iconShape.shape
                                            )
                                    )
                                }
                            }
                        }

                        // Theme Mode Selector (夜間模式)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.theme_mode_label),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val currentTheme = state.themeMode
                            ThemeOptionCard(
                                title = stringResource(R.string.theme_system),
                                icon = Icons.Filled.BrightnessAuto,
                                isSelected = currentTheme == ThemeMode.SYSTEM,
                                onClick = { viewModel.dispatch(FinanceIntent.SetThemeMode(ThemeMode.SYSTEM)) },
                                modifier = Modifier.weight(1f)
                            )
                            ThemeOptionCard(
                                title = stringResource(R.string.theme_light),
                                icon = Icons.Filled.LightMode,
                                isSelected = currentTheme == ThemeMode.LIGHT,
                                onClick = { viewModel.dispatch(FinanceIntent.SetThemeMode(ThemeMode.LIGHT)) },
                                modifier = Modifier.weight(1f)
                            )
                            ThemeOptionCard(
                                title = stringResource(R.string.theme_dark),
                                icon = Icons.Filled.DarkMode,
                                isSelected = currentTheme == ThemeMode.DARK,
                                onClick = { viewModel.dispatch(FinanceIntent.SetThemeMode(ThemeMode.DARK)) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // ==========================================
                // SECTION 2: LANGUAGE & REGION
                // ==========================================
                SettingsSectionContainer(
                    title = stringResource(R.string.settings_section_language),
                    icon = Icons.Filled.Language,
                    expanded = expandedSection == SettingsGroup.LANGUAGE.name,
                    onToggle = {
                        expandedSection = if (expandedSection == SettingsGroup.LANGUAGE.name) null
                        else SettingsGroup.LANGUAGE.name
                    }
                ) {
                    Text(
                        text = stringResource(R.string.language_label),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val currentLang = state.appLanguage
                        LanguageOptionCard(
                            title = stringResource(R.string.lang_system),
                            subtitle = stringResource(R.string.loc_automatic),
                            isSelected = currentLang == AppLanguage.SYSTEM,
                            onClick = { viewModel.dispatch(FinanceIntent.SetAppLanguage(AppLanguage.SYSTEM)) },
                            modifier = Modifier.weight(1f)
                        )
                        LanguageOptionCard(
                            title = stringResource(R.string.lang_en),
                            subtitle = "EN",
                            isSelected = currentLang == AppLanguage.ENGLISH,
                            onClick = { viewModel.dispatch(FinanceIntent.SetAppLanguage(AppLanguage.ENGLISH)) },
                            modifier = Modifier.weight(1f)
                        )
                        LanguageOptionCard(
                            title = stringResource(R.string.lang_zh_hant),
                            subtitle = "中文",
                            isSelected = currentLang == AppLanguage.TRADITIONAL_CHINESE,
                            onClick = { viewModel.dispatch(FinanceIntent.SetAppLanguage(AppLanguage.TRADITIONAL_CHINESE)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // ==========================================
                // SECTION 3: SMART NOTIFICATIONS & AUTO-LOGGING
                // ==========================================
                SettingsSectionContainer(
                    title = stringResource(R.string.settings_section_smart_logging),
                    icon = Icons.Filled.NotificationsActive,
                    expanded = expandedSection == SettingsGroup.SMART_LOGGING.name,
                    onToggle = {
                        expandedSection = if (expandedSection == SettingsGroup.SMART_LOGGING.name) null
                        else SettingsGroup.SMART_LOGGING.name
                    }
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        val isGranted = androidx.core.app.NotificationManagerCompat.getEnabledListenerPackages(context)
                                            .contains(context.packageName)
                                        if (!isGranted) {
                                            try {
                                                context.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").apply {
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                })
                                            } catch (e: Exception) {
                                                // Fallback
                                            }
                                        } else {
                                            val target = !autoLogEnabled
                                            InMemoryDatabase.updateNotificationLoggingEnabled(target)
                                            autoLogEnabled = target
                                        }
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.NotificationsActive,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = stringResource(R.string.settings_auto_logging_title),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = stringResource(R.string.settings_auto_logging_desc),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                                ExpressiveSwitch(
                                    checked = autoLogEnabled,
                                    onCheckedChange = { checked ->
                                        val isGranted = androidx.core.app.NotificationManagerCompat.getEnabledListenerPackages(context)
                                            .contains(context.packageName)
                                        if (!isGranted) {
                                            try {
                                                context.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").apply {
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                })
                                            } catch (e: Exception) {
                                                // Fallback
                                            }
                                        } else {
                                            InMemoryDatabase.updateNotificationLoggingEnabled(checked)
                                            autoLogEnabled = checked
                                        }
                                    }
                                )
                            }

                            // App Selection Chips Preview
                            if (autoLogEnabled) {
                                val installedPackages = remember(context, selectedApps) {
                                    LocalAppManager.getInstalledPackageNames(context)
                                }
                                val selectedInstalledPackages = remember(installedPackages, selectedApps) {
                                    selectedApps.filter { it in installedPackages }.sorted()
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { showAppSelectionDialog = true }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(R.string.settings_manage_apps_label),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = if (selectedInstalledPackages.size == 1) {
                                                stringResource(R.string.settings_apps_enabled_one)
                                            } else if (selectedInstalledPackages.isNotEmpty()) {
                                                stringResource(R.string.settings_apps_enabled_count, selectedInstalledPackages.size)
                                            } else {
                                                stringResource(R.string.settings_apps_none_enabled)
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.horizontalScroll(rememberScrollState())
                                    ) {
                                        selectedInstalledPackages.take(5).forEach { packageName ->
                                            val bitmap = remember(context, packageName) {
                                                LocalAppManager.getAppIcon(context, packageName)
                                            }
                                            bitmap?.let {
                                                Image(
                                                    bitmap = it.asImageBitmap(),
                                                    contentDescription = remember(context, packageName) {
                                                        LocalAppManager.getAppLabel(context, packageName)
                                                    },
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(RoundedCornerShape(7.dp))
                                                        .border(
                                                            0.8.dp,
                                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                                                            RoundedCornerShape(7.dp)
                                                        )
                                                )
                                            }
                                        }

                                        Surface(
                                            modifier = Modifier.size(28.dp),
                                            shape = RoundedCornerShape(7.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // SECTION 4: BUDGET & PERIOD CONFIGURATION
                // ==========================================
                SettingsSectionContainer(
                    title = stringResource(R.string.settings_section_budget_mgmt),
                    icon = Icons.Filled.AccountBalanceWallet,
                    expanded = expandedSection == SettingsGroup.BUDGET.name,
                    onToggle = {
                        expandedSection = if (expandedSection == SettingsGroup.BUDGET.name) null
                        else SettingsGroup.BUDGET.name
                    }
                ) {
                    // Budget Limit Text Field
                    OutlinedTextField(
                        value = budgetAmountText,
                        onValueChange = { budgetAmountText = it },
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        prefix = {
                            Text(
                                "HK$ ",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        },
                        label = { Text(stringResource(R.string.settings_budget_limit_label)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // End Date Selector Button
                    val today = LocalDate.now()
                    val endLocalDate = selectedEndDateMillis?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    val days = if (endLocalDate != null) {
                        (ChronoUnit.DAYS.between(today, endLocalDate) + 1).coerceAtLeast(1).toInt()
                    } else 0

                    val endTextFormatted = endLocalDate?.let {
                        val pattern = if (state.appLanguage == AppLanguage.TRADITIONAL_CHINESE ||
                            (state.appLanguage == AppLanguage.SYSTEM && Locale.getDefault().language == "zh")
                        ) "yyyy年M月d日" else "dd MMM yyyy"
                        it.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
                    } ?: stringResource(R.string.settings_select_end_date)

                    val buttonText = if (days > 0 && endLocalDate != null) {
                        stringResource(R.string.settings_period_ends_format, endTextFormatted, days)
                    } else endTextFormatted

                    Button(
                        onClick = onOpenDatePicker,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = buttonText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Rollover Mode Selector
                    Text(
                        text = stringResource(R.string.settings_rollover_section_title),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.settings_rollover_section_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Rollover Option 1
                    val isDistribute = selectedRolloverMode == RolloverMode.DISTRIBUTE_EVENLY
                    RolloverOptionCard(
                        title = stringResource(R.string.settings_rollover_distribute_title),
                        desc = stringResource(R.string.settings_rollover_distribute_desc),
                        isSelected = isDistribute,
                        onClick = { selectedRolloverMode = RolloverMode.DISTRIBUTE_EVENLY }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Rollover Option 2
                    val isAddNext = selectedRolloverMode == RolloverMode.ADD_TO_NEXT_DAY
                    RolloverOptionCard(
                        title = stringResource(R.string.settings_rollover_next_day_title),
                        desc = stringResource(R.string.settings_rollover_next_day_desc),
                        isSelected = isAddNext,
                        onClick = { selectedRolloverMode = RolloverMode.ADD_TO_NEXT_DAY }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Start New Period Action Button
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenNewPeriod()
                        },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.settings_btn_start_new_period),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // ==========================================
                // SECTION 5: PACING COMPARISON CHART
                // ==========================================
                SettingsSectionContainer(
                    title = stringResource(R.string.settings_section_pacing),
                    icon = Icons.AutoMirrored.Filled.ShowChart,
                    expanded = expandedSection == SettingsGroup.PACING.name,
                    onToggle = {
                        expandedSection = if (expandedSection == SettingsGroup.PACING.name) null
                        else SettingsGroup.PACING.name
                    }
                ) {
                    Text(
                        text = stringResource(R.string.settings_pacing_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val budgetStart = if (budgetInfo.startDate > 0L) budgetInfo.startDate else LocalDate.now().minusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val budgetEnd = if (budgetInfo.endDate > 0L) budgetInfo.endDate else LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                    val startLocalDate = Instant.ofEpochMilli(budgetStart).atZone(ZoneId.systemDefault()).toLocalDate()
                    val endLocalDate = Instant.ofEpochMilli(budgetEnd).atZone(ZoneId.systemDefault()).toLocalDate()
                    val endExclusive = endLocalDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val totalDays = (ChronoUnit.DAYS.between(startLocalDate, endLocalDate) + 1).coerceAtLeast(1).toInt()

                    val activeCumulativeList = remember(state.transactions, budgetStart, endExclusive, totalDays) {
                        val dailySum = DoubleArray(totalDays)
                        state.transactions.forEach { tx ->
                            if (tx.toAccountId == null && !tx.isExcludedFromDailyBudget && tx.amount > 0.0) {
                                if (tx.timestamp >= budgetStart && tx.timestamp < endExclusive) {
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

                    val limitAmt = if (budgetInfo.totalMonthlyBudget > 0.0) budgetInfo.totalMonthlyBudget else 1000.0
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
                    val chartGridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            val stepX = canvasWidth / (totalDays - 1).coerceAtLeast(1)

                            // Horizontal Grid Lines
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

                            // Target Diagonal Pacing Line
                            drawLine(
                                color = chartSecondaryColor.copy(alpha = 0.5f),
                                start = Offset(0f, canvasHeight),
                                end = Offset(canvasWidth, canvasHeight - ((limitAmt / maxVal) * canvasHeight).toFloat()),
                                strokeWidth = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )

                            // Previous Month Line
                            val prevPath = Path()
                            previousCumulativeList.forEachIndexed { idx, valAmt ->
                                val x = idx * stepX
                                val y = canvasHeight - ((valAmt / maxVal) * canvasHeight).toFloat()
                                if (idx == 0) prevPath.moveTo(x, y) else prevPath.lineTo(x, y)
                            }
                            drawPath(
                                path = prevPath,
                                color = Color.Gray.copy(alpha = 0.6f),
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Active Month Line
                            val activePath = Path()
                            activeCumulativeList.forEachIndexed { idx, valAmt ->
                                val x = idx * stepX
                                val y = canvasHeight - ((valAmt / maxVal) * canvasHeight).toFloat()
                                if (idx == 0) activePath.moveTo(x, y) else activePath.lineTo(x, y)
                            }
                            drawPath(
                                path = activePath,
                                color = chartPrimaryColor,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Legend
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(chartPrimaryColor))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.settings_pacing_active), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Gray))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.settings_pacing_prev), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(chartSecondaryColor))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.settings_pacing_ideal), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // ==========================================
                // SECTION 7: DATA & PRIVACY
                // ==========================================
                SettingsSectionContainer(
                    title = stringResource(R.string.settings_section_data_privacy),
                    icon = Icons.Filled.Security,
                    expanded = expandedSection == SettingsGroup.PRIVACY.name,
                    onToggle = {
                        expandedSection = if (expandedSection == SettingsGroup.PRIVACY.name) null
                        else SettingsGroup.PRIVACY.name
                    }
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.settings_privacy_badge),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val importInteraction = remember { MutableInteractionSource() }
                    FilledTonalButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            filePickerLauncher.launch("*/*")
                        },
                        shapes = ButtonDefaults.shapes(shape = RoundedCornerShape(16.dp), pressedShape = RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        interactionSource = importInteraction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .pressBounce(interactionSource = importInteraction)
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.import_btn_select_file),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { showResetConfirmDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.settings_btn_reset_mock),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // App Info & Version Footer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.settings_about_app_info),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                    )
                }
            }
        }
    }

    if (showPaletteSheet) {
        AppearancePickerSheet(
            state = state,
            viewModel = viewModel,
            onDismiss = { showPaletteSheet = false }
        )
    }
}

private fun saveChanges(
    budgetAmountText: String,
    selectedEndDateMillis: Long?,
    budgetInfo: DailyBudgetInfo,
    selectedRolloverMode: RolloverMode,
    viewModel: FinanceViewModel
) {
    val amt = budgetAmountText.toDoubleOrNull()
    val end = selectedEndDateMillis
    if (amt != null && end != null) {
        val isAmountChanged = kotlin.math.abs(amt - budgetInfo.totalMonthlyBudget) > 0.001
        val isEndDateChanged = end != budgetInfo.endDate
        val isRolloverChanged = selectedRolloverMode != budgetInfo.rolloverMode

        if (isAmountChanged || isEndDateChanged || isRolloverChanged) {
            val finalStart = if (budgetInfo.startDate > 0L) {
                budgetInfo.startDate
            } else {
                LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }
            viewModel.dispatch(
                FinanceIntent.SetCustomPeriodBudget(
                    amount = amt,
                    startDate = finalStart,
                    endDate = end,
                    rolloverMode = selectedRolloverMode,
                    showToast = false
                )
            )
        }
    }
}

@Composable
private fun SettingsSectionContainer(
    title: String,
    icon: ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    val corner by animateDpAsState(
        targetValue = if (expanded) 28.dp else 24.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "settingsSectionCorner"
    )
    val headerBottomCorner by animateDpAsState(
        targetValue = if (expanded) 0.dp else 24.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "settingsHeaderBottomCorner"
    )
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "settingsSectionArrow"
    )
    val sectionColor by animateColorAsState(
        targetValue = if (expanded) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "settingsSectionColor"
    )
    val stateLabel = stringResource(
        if (expanded) R.string.settings_section_expanded else R.string.settings_section_collapsed
    )
    val headerShape = RoundedCornerShape(
        topStart = corner,
        topEnd = corner,
        bottomStart = headerBottomCorner,
        bottomEnd = headerBottomCorner
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(corner),
        color = sectionColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(headerShape)
                    .semantics { stateDescription = stateLabel }
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = LocalIconShape.current,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(26.dp).rotate(arrowRotation)
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                exit = shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                ) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
            ) {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val corner by animateDpAsState(
        targetValue = if (isSelected) 24.dp else 18.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "themeOptionCorner"
    )
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(corner),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LanguageOptionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val corner by animateDpAsState(
        targetValue = if (isSelected) 24.dp else 18.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "languageOptionCorner"
    )
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(corner),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RolloverOptionCard(
    title: String,
    desc: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        border = BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
