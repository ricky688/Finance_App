@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package com.example.vibefinance.ui.history

import com.example.vibefinance.ui.components.postPressIconBounce
import com.example.vibefinance.ui.motion.paneEntrance
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import com.example.vibefinance.ui.motion.sharedTransitionBounds
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.SizeTransform
import androidx.compose.foundation.background
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.debounce
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.animateContentSize
import com.example.vibefinance.theme.MotionTokens
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.AssignmentReturn
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.ui.components.AppButtonDefaults
import com.example.vibefinance.ui.components.CompletePressButton
import com.example.vibefinance.ui.components.CompletePressFilledTonalButton
import com.example.vibefinance.ui.components.CompletePressOutlinedButton
import com.example.vibefinance.ui.components.CompletePressTextButton
import com.example.vibefinance.ui.main.ModeSwitcherAnimatedIcon
import com.example.vibefinance.ui.main.TransactionMode
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import com.example.vibefinance.util.VibeHaptics
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.filled.Layers
import androidx.compose.ui.platform.LocalView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

/**
 * The phone transaction dialog frame. Normally a regular [AlertDialog]; with [inline] it lays out
 * the same title, text and buttons without its own window or surface, so [MorphingTransactionDialog]
 * can morph between the detail and edit dialogs inside one container.
 */
@Composable
internal fun TransactionDialogFrame(
    inline: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(32.dp),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    title: @Composable () -> Unit,
    text: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null,
) {
    if (!inline) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            shape = shape,
            containerColor = containerColor,
            title = title,
            text = text,
            confirmButton = confirmButton,
            dismissButton = dismissButton
        )
        return
    }
    // Mirrors AlertDialog's content layout: 24dp padding, title, scrollable text, end-aligned buttons.
    Column(Modifier.padding(24.dp)) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
                Box(Modifier.padding(bottom = 16.dp)) { title() }
            }
        }
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
            ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                Box(Modifier.weight(1f, fill = false).padding(bottom = 24.dp)) { text() }
            }
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            dismissButton?.invoke()
            confirmButton()
        }
    }
}

/**
 * Phone transaction dialog that keeps one window while switching between the detail view and the
 * edit form: the shared 32dp card resizes with a slow spatial spring while the content cross-fades
 * and settles in from 96% scale, so pressing Edit (or cancelling back) morphs instead of
 * closing one dialog and opening another.
 */
/**
 * Phone transaction dialog that keeps one window while switching between the detail view and the
 * edit form, using a **shared element transition**: the location card (with the very same map
 * view), amount, category, date/time, account and description move and reshape from their place
 * on one page to their place on the other, while the card resizes. Elements that exist on only one
 * page slide a short way and fade, so nothing pops in. All motion uses the slow spatial spring.
 */
/** Phone (compact) stage inside [MorphingTransactionDialog]. */
private enum class PhoneTransactionDialogStage { DETAIL, EDIT, FULL_SCREEN_MAP }

/**
 * Phone transaction dialog that keeps one window while switching between the detail view, edit form,
 * and full-screen interactive OSM map: the single [SharedMapHost] moves smoothly between the card
 * and full screen without reloading tiles.
 */
@OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)
@Composable
internal fun MorphingTransactionDialog(
    editing: Boolean,
    fullScreenMapOpen: Boolean = false,
    onDismissRequest: () -> Unit,
    detail: @Composable () -> Unit,
    edit: @Composable () -> Unit,
    fullScreenMap: (@Composable () -> Unit)? = null,
) {
    val currentDismiss by rememberUpdatedState(onDismissRequest)
    val mapHost = remember { SharedMapHost() }
    val stage = when {
        fullScreenMapOpen && fullScreenMap != null -> PhoneTransactionDialogStage.FULL_SCREEN_MAP
        editing -> PhoneTransactionDialogStage.EDIT
        else -> PhoneTransactionDialogStage.DETAIL
    }
    androidx.compose.material3.BasicAlertDialog(
        onDismissRequest = {
            if (stage != PhoneTransactionDialogStage.FULL_SCREEN_MAP) currentDismiss()
        },
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val dialogView = LocalView.current
        SideEffect {
            val window = (dialogView.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window
            if (window != null) {
                androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
                @Suppress("DEPRECATION")
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                @Suppress("DEPRECATION")
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
            }
        }
        androidx.compose.animation.SharedTransitionLayout {
            CompositionLocalProvider(
                com.example.vibefinance.ui.motion.LocalSharedTransitionScope provides this,
                LocalSharedMapHost provides mapHost,
                LocalCompactTransactionForm provides true
            ) {
                AnimatedContent(
                    targetState = stage,
                    contentAlignment = Alignment.Center,
                    transitionSpec = {
                        if (targetState == PhoneTransactionDialogStage.FULL_SCREEN_MAP) {
                            (fadeIn(com.example.vibefinance.theme.VibeMotion.defaultEffects()) +
                                scaleIn(com.example.vibefinance.theme.VibeMotion.slowSpatial(), initialScale = 0.94f)) togetherWith
                                fadeOut(com.example.vibefinance.theme.VibeMotion.fastEffects()) using
                                SizeTransform(clip = false) { _, _ -> com.example.vibefinance.theme.VibeMotion.slowSpatial() }
                        } else if (initialState == PhoneTransactionDialogStage.FULL_SCREEN_MAP) {
                            fadeIn(com.example.vibefinance.theme.VibeMotion.defaultEffects()) togetherWith
                                (fadeOut(com.example.vibefinance.theme.VibeMotion.fastEffects()) +
                                    scaleOut(com.example.vibefinance.theme.VibeMotion.defaultSpatial(), targetScale = 0.96f)) using
                                SizeTransform(clip = false) { _, _ -> com.example.vibefinance.theme.VibeMotion.defaultSpatial() }
                        } else {
                            val toEdit = targetState == PhoneTransactionDialogStage.EDIT
                            val enter = slideInVertically(com.example.vibefinance.theme.VibeMotion.slowSpatial()) { h -> if (toEdit) h / 8 else -h / 8 } +
                                fadeIn(com.example.vibefinance.theme.VibeMotion.slowEffects())
                            val exit = slideOutVertically(com.example.vibefinance.theme.VibeMotion.slowSpatial()) { h -> if (toEdit) -h / 8 else h / 8 } +
                                fadeOut(com.example.vibefinance.theme.VibeMotion.defaultEffects())
                            (enter togetherWith exit).using(
                                SizeTransform(clip = false) { _, _ -> com.example.vibefinance.theme.VibeMotion.slowSpatial() }
                            )
                        }
                    },
                    label = "transactionDetailToEditOrMap"
                ) { targetStage ->
                    CompositionLocalProvider(
                        com.example.vibefinance.ui.motion.LocalSharedContentScope provides this,
                        com.example.vibefinance.ui.motion.LocalIsSharedTargetPage provides (targetStage == stage)
                    ) {
                        if (targetStage == PhoneTransactionDialogStage.FULL_SCREEN_MAP && fullScreenMap != null) {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) { detectTapGestures { } }
                            ) {
                                fullScreenMap()
                            }
                        } else {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout))
                                    // Scrim tap dismisses; gesture-only so no extra accessibility node (back dismisses too).
                                    .pointerInput(Unit) { detectTapGestures { currentDismiss() } },
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth(0.95f)
                                        .widthIn(max = 560.dp)
                                        .padding(vertical = 24.dp)
                                        .sharedTransitionBounds("tx-morph-dialog-card", remeasure = true)
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(32.dp))
                                        .testTag("TransactionMorphDialog"),
                                    shape = RoundedCornerShape(32.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 0.dp
                                ) {
                                    Box(
                                        // Swallow taps on the card so only the scrim dismisses.
                                        Modifier.pointerInput(Unit) { detectTapGestures { } }
                                    ) {
                                        if (targetStage == PhoneTransactionDialogStage.EDIT) edit() else detail()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Edit form plus save state, produced by History and rendered inside [TransactionDetailPage]. */
internal class TransactionEditUi(
    val form: @Composable () -> Unit,
    val canSave: Boolean,
    val busy: Boolean,
    val save: () -> Unit,
)

/**
 * One page for viewing and editing a transaction (M3 Expressive redesign, item 2).
 * Phones show it full screen through [TransactionDetailPageHost]; wide windows show it in the
 * list-detail pane ([embedded] = true, so no back arrow while viewing). Edit mode swaps the body
 * for the shared edit form in place and shows Cancel / Save in a floating toolbar.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun TransactionDetailPage(
    transaction: TransactionEntity,
    accounts: List<AccountEntity>,
    embedded: Boolean,
    editing: Boolean,
    editUi: TransactionEditUi?,
    onClose: () -> Unit,
    onEdit: (TransactionEntity) -> Unit,
    onCancelEdit: () -> Unit,
    onDelete: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier,
    onUpdateTransaction: ((TransactionEntity) -> Unit)? = null,
    onMoneyBack: (() -> Unit)? = null,
    onUnlink: (() -> Unit)? = null,
    onRecordReimbursement: ((Double, String) -> Unit)? = null,
    onOpenFullScreenMap: (() -> Unit)? = null,
) {
    val hideAmounts = com.example.vibefinance.ui.preferences.LocalExperience.current.hideAmounts
    val context = LocalContext.current
    var currentTx by remember(transaction) { mutableStateOf(transaction) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showPresetSheet by remember { mutableStateOf(false) }
    var showSplitSheet by remember(transaction.id) { mutableStateOf(false) }

    val view = LocalView.current
    val isDark = isSystemInDarkTheme()

    val sourceAccount = remember(currentTx.accountId, accounts) {
        accounts.firstOrNull { it.id == currentTx.accountId }
    }
    val destinationAccount = remember(currentTx.toAccountId, accounts) {
        accounts.firstOrNull { it.id == currentTx.toAccountId }
    }

    val isTransfer = currentTx.toAccountId != null
    val isIncome = currentTx.amount < 0 && !isTransfer
    val isExpense = currentTx.amount >= 0 && !isTransfer
    val isReimbursement = currentTx.isReimbursement

    val displayAmount = abs(currentTx.amount)
    val amountPrefix = when {
        isTransfer -> ""
        isIncome -> "+"
        else -> "-"
    }
    val amountColor = when {
        isTransfer -> MaterialTheme.colorScheme.primary
        isIncome -> Color(0xFF2E7D32)
        else -> MaterialTheme.colorScheme.error
    }


    Box(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .then(if (embedded) Modifier else Modifier.windowInsetsPadding(WindowInsets.safeDrawing))
            .testTag("TransactionDetailPage")
    ) {
        val contentHorizontalPadding = if (embedded) 0.dp else 20.dp
        Column(Modifier.fillMaxSize()) {
            // Top app bar: back (or cancel while editing), title, edit and delete actions.
            Row(
                modifier = Modifier.paneEntrance(0).fillMaxWidth().padding(
                    start = if (embedded) 0.dp else 4.dp,
                    end = if (embedded) 0.dp else 8.dp,
                    top = if (embedded) 0.dp else 8.dp,
                    bottom = 4.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!embedded || editing) {
                    IconButton(
                        onClick = {
                            com.example.vibefinance.util.VibeHaptics.selection(view)
                            if (editing) onCancelEdit() else onClose()
                        },
                        modifier = Modifier.testTag("DetailCloseButton")
                    ) {
                        Icon(
                            if (editing) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(if (editing) R.string.btn_cancel else R.string.btn_close)
                        )
                    }
                } else {
                    Spacer(Modifier.width(4.dp))
                }
                Box(Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            var iconClickTrigger by remember { mutableStateOf(1) }
                            Surface(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        iconClickTrigger++
                                    },
                                shape = CircleShape,
                                color = when {
                                    isReimbursement -> MaterialTheme.colorScheme.secondaryContainer
                                    isTransfer -> MaterialTheme.colorScheme.primaryContainer
                                    isIncome -> Color(0xFFE8F5E9)
                                    else -> MaterialTheme.colorScheme.errorContainer
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isReimbursement) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                                            contentDescription = stringResource(R.string.reimbursement_title),
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    } else {
                                        val mode = when {
                                            isTransfer -> TransactionMode.TRANSFER
                                            isIncome -> TransactionMode.INCOME
                                            else -> TransactionMode.EXPENSE
                                        }
                                        val iconTint = when {
                                            isTransfer -> MaterialTheme.colorScheme.onPrimaryContainer
                                            isIncome -> Color(0xFF1B5E20)
                                            else -> MaterialTheme.colorScheme.onErrorContainer
                                        }
                                        ModeSwitcherAnimatedIcon(
                                            mode = mode,
                                            tint = iconTint,
                                            isSelected = true,
                                            clickTrigger = iconClickTrigger,
                                            iconSize = 20.dp
                                        )
                                    }
                                }
                            }

                            Column {
                                Text(
                                    text = when {
                                        isReimbursement -> stringResource(R.string.reimbursement_title)
                                        isTransfer -> stringResource(R.string.filter_transfer)
                                        isIncome -> stringResource(R.string.filter_income)
                                        else -> com.example.vibefinance.ui.home.getCategoryDisplayName(currentTx.category)
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(currentTx.timestamp)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                AnimatedVisibility(
                    visible = !editing,
                    enter = fadeIn(com.example.vibefinance.theme.VibeMotion.defaultEffects()) +
                        scaleIn(com.example.vibefinance.theme.VibeMotion.fastSpatial(), initialScale = 0.6f),
                    exit = fadeOut(com.example.vibefinance.theme.VibeMotion.fastEffects()) +
                        scaleOut(com.example.vibefinance.theme.VibeMotion.fastSpatial(), targetScale = 0.6f)
                ) {
                    Row {
                        IconButton(
                            onClick = { com.example.vibefinance.util.VibeHaptics.selection(view); onEdit(currentTx) },
                            modifier = Modifier.testTag("DetailEditButton")
                        ) { Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.btn_edit)) }
                        IconButton(
                            onClick = { com.example.vibefinance.util.VibeHaptics.selection(view); showDeleteConfirm = true },
                            modifier = Modifier.testTag("DetailDeleteButton")
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = stringResource(R.string.btn_delete),
                                tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            AnimatedContent(
                targetState = editing && editUi != null,
                transitionSpec = {
                    (fadeIn(com.example.vibefinance.theme.VibeMotion.defaultEffects()) +
                        slideInVertically(com.example.vibefinance.theme.VibeMotion.defaultSpatial()) { it / 12 })
                        .togetherWith(fadeOut(com.example.vibefinance.theme.VibeMotion.fastEffects()))
                },
                modifier = Modifier.weight(1f),
                label = "transactionPageMode"
            ) { isEditing ->
                if (isEditing && editUi != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = contentHorizontalPadding)
                            .padding(top = 8.dp, bottom = 112.dp)
                            .testTag("TransactionEditForm"),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) { editUi.form() }
                } else {
                    val heroSection: @Composable () -> Unit = {
                        // 1. Hero Amount Display
                        Surface(
                            modifier = Modifier.paneEntrance(1).fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (hideAmounts) "••••" else String.format(Locale.US, "%sHK$%,.2f", amountPrefix, displayAmount),
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = amountColor,
                                    maxLines = 1
                                )

                                if (currentTx.description.isNotBlank()) {
                                    Text(
                                        text = currentTx.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }


                    }
                    val locationSection: @Composable () -> Unit = {
                        // 2. OpenStreetMap (OSM) Location Card (Graceful display when coordinates exist)
                        val location = rememberTransactionLocation(currentTx)
                        if (location != null) {
                            val lat = location.latitude
                            val lng = location.longitude
                            val locLabel = location.label
                            val startStationName = location.startStationName
                            val endStationName = location.endStationName
                            val effectiveStartLat = location.startLatitude
                            val effectiveStartLng = location.startLongitude

                            Surface(
                                modifier = Modifier
                                    .paneEntrance(2)
                                    .fillMaxWidth()
                                    .testTag("OsmLocationCard"),
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.align(Alignment.CenterVertically)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = stringResource(R.string.tx_detail_location),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            CompletePressTextButton(
                                                onClick = {
                                                    val geoUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(currentTx.description.ifBlank { currentTx.category })})")
                                                    val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                    }
                                                    runCatching { context.startActivity(mapIntent) }
                                                },
                                                shapes = AppButtonDefaults.shapes(),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp).postPressIconBounce())
                                                Spacer(Modifier.width(4.dp))
                                                Text(stringResource(R.string.tx_detail_open_in_map), style = MaterialTheme.typography.labelSmall)
                                            }

                                            if (onOpenFullScreenMap != null) {
                                                CompletePressTextButton(
                                                    onClick = onOpenFullScreenMap,
                                                    shapes = AppButtonDefaults.shapes(),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(28.dp).testTag("OpenFullScreenOsmButton")
                                                ) {
                                                    Icon(Icons.Default.OpenInFull, contentDescription = null, modifier = Modifier.size(14.dp).postPressIconBounce())
                                                    Spacer(Modifier.width(4.dp))
                                                    Text(stringResource(R.string.tx_detail_fullscreen_map), style = MaterialTheme.typography.labelSmall)
                                                }
                                            }

                                            if (onUpdateTransaction != null) {
                                                CompletePressTextButton(
                                                    onClick = { showPresetSheet = true },
                                                    shapes = AppButtonDefaults.shapes(),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(28.dp)
                                                ) {
                                                    Icon(Icons.Default.EditLocation, contentDescription = null, modifier = Modifier.size(14.dp).postPressIconBounce())
                                                    Spacer(Modifier.width(2.dp))
                                                    Text(stringResource(R.string.tx_detail_retag_location), style = MaterialTheme.typography.labelSmall)
                                                }
                                            }
                                        }
                                    }

                                    // Transit Route Bento Card
                                    if (startStationName != null && endStationName != null) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(androidx.compose.ui.graphics.Color(0xFF00C853)))
                                                    Text(
                                                        text = startStationName,
                                                        style = MaterialTheme.typography.labelLarge,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Icon(
                                                        imageVector = androidx.compose.material.icons.Icons.Default.DirectionsSubway,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(androidx.compose.ui.graphics.Color(0xFFD50000)))
                                                    Text(
                                                        text = endStationName,
                                                        style = MaterialTheme.typography.labelLarge,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                                Text(
                                                    text = "港鐵出行",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }

                                    SharedTransactionMap(
                                        location = location,
                                        modifier = Modifier.fillMaxWidth().height(180.dp),
                                        interactive = false,
                                        onPreviewClick = onOpenFullScreenMap
                                    )

                                    // Location Address / Street description
                                    Text(
                                        text = locLabel,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        } else if (onUpdateTransaction != null) {
                            var isTagging by remember { mutableStateOf(false) }
                            val scope = rememberCoroutineScope()
                            Surface(
                                modifier = Modifier
                                    .paneEntrance(2)
                                    .fillMaxWidth()
                                    .testTag("OsmNoLocationCard"),
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(38.dp),
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.surfaceContainerHighest
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.LocationOff,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = stringResource(R.string.tx_detail_no_location_title),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = stringResource(R.string.tx_detail_no_location_hint),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CompletePressButton(
                                            onClick = {
                                                if (!isTagging) {
                                                    isTagging = true
                                                    scope.launch {
                                                        val freshLoc = com.example.vibefinance.util.GnssLocationManager.resolveFreshLocation(context)
                                                            ?: com.example.vibefinance.util.GnssLocationManager.getInstantLocation(context)
                                                        isTagging = false
                                                        if (freshLoc != null) {
                                                            val updated = currentTx.copy(
                                                                latitude = freshLoc.latitude,
                                                                longitude = freshLoc.longitude,
                                                                locationName = freshLoc.locationName
                                                            )
                                                            currentTx = updated
                                                            onUpdateTransaction(updated)
                                                            android.widget.Toast.makeText(
                                                                context,
                                                                context.getString(R.string.tx_detail_location_success),
                                                                android.widget.Toast.LENGTH_SHORT
                                                            ).show()
                                                        } else {
                                                            android.widget.Toast.makeText(
                                                                context,
                                                                context.getString(R.string.tx_detail_location_failed),
                                                                android.widget.Toast.LENGTH_LONG
                                                            ).show()
                                                            showPresetSheet = true
                                                        }
                                                    }
                                                }
                                            },
                                            shapes = AppButtonDefaults.shapes(),
                                            enabled = !isTagging,
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(36.dp)
                                        ) {
                                            if (isTagging) {
                                                LoadingIndicator(
                                                    modifier = Modifier.size(14.dp),
                                                    color = MaterialTheme.colorScheme.onPrimary
                                                )
                                            } else {
                                                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(15.dp).postPressIconBounce())
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = stringResource(R.string.tx_detail_tag_location),
                                                    style = MaterialTheme.typography.labelMedium
                                                )
                                            }
                                        }

                                        CompletePressOutlinedButton(
                                            onClick = { showPresetSheet = true },
                                            shapes = AppButtonDefaults.shapes(),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(15.dp).postPressIconBounce())
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = stringResource(R.string.tx_detail_tag_preset),
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }
                                    }
                                    if (onOpenFullScreenMap != null) {
                                        CompletePressOutlinedButton(
                                            onClick = onOpenFullScreenMap,
                                            shapes = AppButtonDefaults.shapes(),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(36.dp)
                                                .testTag("PickOnFullScreenOsmButton")
                                        ) {
                                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(15.dp).postPressIconBounce())
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = stringResource(R.string.tx_detail_pick_on_map),
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }


                    }
                    val infoSection: @Composable () -> Unit = {
                        // 3. Detail Info Grid
                        Surface(
                            modifier = Modifier.paneEntrance(3).fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Account row
                                DetailRow(
                                    icon = Icons.Default.AccountBalanceWallet,
                                    label = stringResource(R.string.label_account),
                                    value = when {
                                        isTransfer -> "${sourceAccount?.name ?: "#${currentTx.accountId}"} → ${destinationAccount?.name ?: "#${currentTx.toAccountId}"}"
                                        else -> sourceAccount?.name ?: "#${currentTx.accountId}"
                                    }
                                )

                                // Category row
                                if (!isTransfer) {
                                    DetailRow(
                                        icon = Icons.Default.Category,
                                        label = stringResource(R.string.cat_others),
                                        value = com.example.vibefinance.ui.home.getCategoryDisplayName(currentTx.category)
                                    )
                                }

                                // Daily budget status
                                if (isExpense) {
                                    DetailRow(
                                        icon = if (currentTx.isExcludedFromDailyBudget) Icons.Outlined.EventBusy else Icons.Outlined.EventAvailable,
                                        label = stringResource(R.string.daily_budget_toggle_title),
                                        value = if (currentTx.isExcludedFromDailyBudget) {
                                            stringResource(R.string.daily_budget_toggle_desc_off)
                                        } else {
                                            stringResource(R.string.daily_budget_toggle_desc_on)
                                        }
                                    )
                                }

                                // Reimbursement status
                                if (currentTx.isReimbursement) {
                                    DetailRow(
                                        icon = Icons.AutoMirrored.Outlined.AssignmentReturn,
                                        label = stringResource(R.string.reimbursement_title),
                                        value = if (currentTx.reimbursementOf != null) {
                                            "#${currentTx.reimbursementOf}"
                                        } else {
                                            stringResource(R.string.reimbursement_unlink)
                                        }
                                    )
                                }
                            }
                        }

                        NotificationSourceCard(
                            transaction = currentTx,
                            modifier = Modifier.paneEntrance(3).fillMaxWidth()
                        )

                        if (isExpense && !isReimbursement && !currentTx.isBalanceAdjustment && currentTx.timestamp <= System.currentTimeMillis()) {
                            CompletePressFilledTonalButton(
                                onClick = {
                                    VibeHaptics.selection(view)
                                    showSplitSheet = true
                                },
                                shapes = AppButtonDefaults.shapes(),
                                modifier = Modifier.paneEntrance(4).fillMaxWidth().testTag("TransactionSplitBillButton")
                            ) {
                                Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(18.dp).postPressIconBounce())
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.split_bill_action), fontWeight = FontWeight.SemiBold)
                            }
                        }

                        if (onMoneyBack != null && currentTx.amount < 0 && currentTx.toAccountId == null && !currentTx.isBalanceAdjustment) {
                            Row(Modifier.paneEntrance(5), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CompletePressTextButton(
                                    onClick = onMoneyBack,
                                    shapes = AppButtonDefaults.shapes()
                                ) { Text(stringResource(R.string.reimbursement_choose_expense)) }
                                if (currentTx.reimbursementOf != null && onUnlink != null) CompletePressTextButton(
                                    onClick = onUnlink,
                                    shapes = AppButtonDefaults.shapes()
                                ) { Text(stringResource(R.string.reimbursement_unlink)) }
                            }
                        }
                    }
                    BoxWithConstraints(Modifier.fillMaxSize()) {
                        if (maxWidth >= 720.dp) {
                            // Wide pane: amount and map on the left, the record's fields on the right.
                            Row(
                                Modifier.fillMaxSize().padding(horizontal = contentHorizontalPadding),
                                horizontalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                Column(
                                    Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())
                                        .padding(top = 8.dp, bottom = 24.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) { heroSection(); locationSection() }
                                Column(
                                    Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())
                                        .padding(top = 8.dp, bottom = 24.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) { infoSection() }
                            }
                        } else {
                            Column(
                                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                                    .padding(horizontal = contentHorizontalPadding).padding(top = 8.dp, bottom = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) { heroSection(); locationSection(); infoSection() }
                        }
                    }
                }
            }
        }

        // Edit mode: M3 Expressive floating toolbar with Cancel / Save.
        AnimatedVisibility(
            visible = editing && editUi != null,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
            enter = fadeIn(com.example.vibefinance.theme.VibeMotion.defaultEffects()) +
                slideInVertically(com.example.vibefinance.theme.VibeMotion.defaultSpatial()) { it },
            exit = fadeOut(com.example.vibefinance.theme.VibeMotion.fastEffects()) +
                slideOutVertically(com.example.vibefinance.theme.VibeMotion.defaultSpatial()) { it }
        ) {
            val ui = editUi
            HorizontalFloatingToolbar(expanded = true, modifier = Modifier.testTag("TransactionEditToolbar")) {
                CompletePressTextButton(
                    enabled = ui?.busy != true,
                    onClick = { com.example.vibefinance.util.VibeHaptics.selection(view); onCancelEdit() },
                    shapes = AppButtonDefaults.shapes()
                ) { Text(stringResource(R.string.btn_cancel)) }
                Spacer(Modifier.width(8.dp))
                CompletePressButton(
                    enabled = ui?.canSave == true,
                    onClick = { com.example.vibefinance.util.VibeHaptics.selection(view); ui?.save?.invoke() },
                    shapes = AppButtonDefaults.shapes(),
                    modifier = Modifier.testTag("EditTransactionSave")
                ) {
                    if (ui?.busy == true) LoadingIndicator(Modifier.size(24.dp))
                    else Text(stringResource(R.string.btn_save))
                }
            }
        }
    }

    // District / Location Preset Sheet Dialog
    if (showPresetSheet && onUpdateTransaction != null) {
        AlertDialog(
            onDismissRequest = { showPresetSheet = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = stringResource(R.string.tx_detail_select_district_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    val presets = com.example.vibefinance.util.GnssLocationManager.PRESETS
                    presets.forEachIndexed { index, preset ->
                        val isSelectedPreset = currentTx.locationName == preset.title
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val rowShape = com.example.vibefinance.ui.main.rememberConnectedListRowShape(
                            isFirst = index == 0,
                            isLast = index == presets.lastIndex,
                            isSelected = isSelectedPreset,
                            isPressed = isPressed
                        )
                        Surface(
                            onClick = {
                                showPresetSheet = false
                                val updated = currentTx.copy(
                                    latitude = preset.latitude,
                                    longitude = preset.longitude,
                                    locationName = preset.title
                                )
                                currentTx = updated
                                onUpdateTransaction(updated)
                                android.widget.Toast.makeText(
                                    context,
                                    context.getString(R.string.tx_detail_location_success) + ": " + preset.title,
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            },
                            interactionSource = interactionSource,
                            shape = rowShape,
                            color = if (isSelectedPreset) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = preset.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Clear Location option if location currently exists
                    if (currentTx.latitude != null) {
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            onClick = {
                                showPresetSheet = false
                                val updated = currentTx.copy(
                                    latitude = null,
                                    longitude = null,
                                    locationName = null
                                )
                                currentTx = updated
                                onUpdateTransaction(updated)
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = stringResource(R.string.tx_detail_remove_location),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                CompletePressTextButton(
                    onClick = { showPresetSheet = false },
                    shapes = AppButtonDefaults.shapes(),
                ) {
                    Text(stringResource(R.string.btn_close))
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.tx_detail_delete_confirm_title)) },
            text = { Text(stringResource(R.string.tx_detail_delete_confirm_desc)) },
            confirmButton = {
                CompletePressButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(currentTx)
                    },
                    shapes = AppButtonDefaults.shapes(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.btn_delete))
                }
            },
            dismissButton = {
                CompletePressTextButton(
                    onClick = { showDeleteConfirm = false },
                    shapes = AppButtonDefaults.shapes()
                ) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    if (showSplitSheet) {
        SplitAmountSheet(
            transaction = currentTx,
            onDismiss = { showSplitSheet = false },
            onRecordReimbursement = onRecordReimbursement
        )
    }
}

/** Phone (compact) viewer: the original detail dialog, kept by user preference (2026-11-20). */
@Composable
fun TransactionDetailDialog(
    transaction: TransactionEntity,
    accounts: List<AccountEntity>,
    onDismissRequest: () -> Unit,
    onEdit: (TransactionEntity) -> Unit,
    onDelete: (TransactionEntity) -> Unit,
    onUpdateTransaction: ((TransactionEntity) -> Unit)? = null,
    onRecordReimbursement: ((Double, String) -> Unit)? = null,
    inline: Boolean = false,
    onOpenFullScreenMap: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    var currentTx by remember(transaction.id, transaction.latitude, transaction.longitude, transaction.locationName) { mutableStateOf(transaction) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showPresetSheet by remember { mutableStateOf(false) }
    var showSplitSheet by remember(transaction.id) { mutableStateOf(false) }

    val view = LocalView.current
    val isDark = isSystemInDarkTheme()

    val sourceAccount = remember(currentTx.accountId, accounts) {
        accounts.firstOrNull { it.id == currentTx.accountId }
    }
    val destinationAccount = remember(currentTx.toAccountId, accounts) {
        accounts.firstOrNull { it.id == currentTx.toAccountId }
    }

    val isTransfer = currentTx.toAccountId != null
    val isIncome = currentTx.amount < 0 && !isTransfer
    val isExpense = currentTx.amount >= 0 && !isTransfer
    val isReimbursement = currentTx.isReimbursement

    val displayAmount = abs(currentTx.amount)
    val amountPrefix = when {
        isTransfer -> ""
        isIncome -> "+"
        else -> "-"
    }
    val amountColor = when {
        isTransfer -> MaterialTheme.colorScheme.primary
        isIncome -> Color(0xFF2E7D32)
        else -> MaterialTheme.colorScheme.error
    }

    val dialogScale by animateFloatAsState(
        targetValue = 1.0f,
        animationSpec = com.example.vibefinance.theme.VibeMotion.slowSpatial(),
        label = "detailDialogContainerTransform"
    )

    TransactionDialogFrame(
        inline = inline,
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .graphicsLayer {
                scaleX = dialogScale
                scaleY = dialogScale
            }
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(32.dp)
            )
            .testTag("TransactionDetailDialog"),
        shape = RoundedCornerShape(32.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    var iconClickTrigger by remember { mutableStateOf(1) }
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                iconClickTrigger++
                            },
                        shape = CircleShape,
                        color = when {
                            isReimbursement -> MaterialTheme.colorScheme.secondaryContainer
                            isTransfer -> MaterialTheme.colorScheme.primaryContainer
                            isIncome -> Color(0xFFE8F5E9)
                            else -> MaterialTheme.colorScheme.errorContainer
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isReimbursement) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                                    contentDescription = stringResource(R.string.reimbursement_title),
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            } else {
                                val mode = when {
                                    isTransfer -> TransactionMode.TRANSFER
                                    isIncome -> TransactionMode.INCOME
                                    else -> TransactionMode.EXPENSE
                                }
                                val iconTint = when {
                                    isTransfer -> MaterialTheme.colorScheme.onPrimaryContainer
                                    isIncome -> Color(0xFF1B5E20)
                                    else -> MaterialTheme.colorScheme.onErrorContainer
                                }
                                ModeSwitcherAnimatedIcon(
                                    mode = mode,
                                    tint = iconTint,
                                    isSelected = true,
                                    clickTrigger = iconClickTrigger,
                                    iconSize = 20.dp
                                )
                            }
                        }
                    }

                    Column {
                        Text(
                            text = when {
                                isReimbursement -> stringResource(R.string.reimbursement_title)
                                isTransfer -> stringResource(R.string.filter_transfer)
                                isIncome -> stringResource(R.string.filter_income)
                                else -> com.example.vibefinance.ui.home.getCategoryDisplayName(currentTx.category)
                            },
                            modifier = Modifier.sharedTransitionBounds(SharedTxKeys.Category),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(currentTx.timestamp)),
                            modifier = Modifier.sharedTransitionBounds(SharedTxKeys.DateTime),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        text = {
            // Edges fade only while there is more to scroll in that direction.
            val detailScroll = rememberScrollState()
            com.example.vibefinance.ui.common.ScrollBlurContainer(scrollState = detailScroll, vertical = true) { blurModifier ->
            Column(
                modifier = blurModifier
                    .fillMaxWidth()
                    .verticalScroll(detailScroll),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Hero Amount Display
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%sHK$%,.2f", amountPrefix, displayAmount),
                            modifier = Modifier.sharedTransitionBounds(SharedTxKeys.Amount),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = amountColor,
                            maxLines = 1
                        )

                        if (currentTx.description.isNotBlank()) {
                            Text(
                                text = currentTx.description,
                                modifier = Modifier.sharedTransitionBounds(SharedTxKeys.Description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // 2. OpenStreetMap (OSM) Location Card (Graceful display when coordinates exist)
                val location = rememberTransactionLocation(currentTx)
                if (location != null) {
                    val lat = location.latitude
                    val lng = location.longitude
                    val locLabel = location.label
                    val startStationName = location.startStationName
                    val endStationName = location.endStationName
                    val effectiveStartLat = location.startLatitude
                    val effectiveStartLng = location.startLongitude

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .sharedTransitionBounds(SharedTxKeys.LocationCard, remeasure = true)
                            .testTag("OsmLocationCard"),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.tx_detail_location),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    CompletePressTextButton(
                                        onClick = {
                                            val geoUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(currentTx.description.ifBlank { currentTx.category })})")
                                            val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            runCatching { context.startActivity(mapIntent) }
                                        },
                                        shapes = AppButtonDefaults.shapes(),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp).postPressIconBounce())
                                        Spacer(Modifier.width(4.dp))
                                        Text(stringResource(R.string.tx_detail_open_in_map), style = MaterialTheme.typography.labelSmall)
                                    }

                                    if (onOpenFullScreenMap != null) {
                                        CompletePressTextButton(
                                            onClick = onOpenFullScreenMap,
                                            shapes = AppButtonDefaults.shapes(),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp).testTag("OpenFullScreenOsmButton")
                                        ) {
                                            Icon(Icons.Default.OpenInFull, contentDescription = null, modifier = Modifier.size(14.dp).postPressIconBounce())
                                            Spacer(Modifier.width(4.dp))
                                            Text(stringResource(R.string.tx_detail_fullscreen_map), style = MaterialTheme.typography.labelSmall)
                                        }
                                    }

                                    if (onUpdateTransaction != null) {
                                        CompletePressTextButton(
                                            onClick = { showPresetSheet = true },
                                            shapes = AppButtonDefaults.shapes(),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Icon(Icons.Default.EditLocation, contentDescription = null, modifier = Modifier.size(14.dp).postPressIconBounce())
                                            Spacer(Modifier.width(2.dp))
                                            Text(stringResource(R.string.tx_detail_retag_location), style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }

                            // Transit Route Bento Card
                            if (startStationName != null && endStationName != null) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(androidx.compose.ui.graphics.Color(0xFF00C853)))
                                            Text(
                                                text = startStationName,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Icon(
                                                imageVector = androidx.compose.material.icons.Icons.Default.DirectionsSubway,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(androidx.compose.ui.graphics.Color(0xFFD50000)))
                                            Text(
                                                text = endStationName,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Text(
                                            text = "港鐵出行",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            SharedTransactionMap(
                                location = location,
                                modifier = Modifier.fillMaxWidth().height(180.dp),
                                interactive = false,
                                onPreviewClick = onOpenFullScreenMap
                            )

                            // Location Address / Street description
                            Text(
                                text = locLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else if (onUpdateTransaction != null) {
                    var isTagging by remember { mutableStateOf(false) }
                    val scope = rememberCoroutineScope()
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("OsmNoLocationCard"),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(38.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOff,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.tx_detail_no_location_title),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = stringResource(R.string.tx_detail_no_location_hint),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CompletePressButton(
                                    onClick = {
                                        if (!isTagging) {
                                            isTagging = true
                                            scope.launch {
                                                val freshLoc = com.example.vibefinance.util.GnssLocationManager.resolveFreshLocation(context)
                                                    ?: com.example.vibefinance.util.GnssLocationManager.getInstantLocation(context)
                                                isTagging = false
                                                if (freshLoc != null) {
                                                    val updated = currentTx.copy(
                                                        latitude = freshLoc.latitude,
                                                        longitude = freshLoc.longitude,
                                                        locationName = freshLoc.locationName
                                                    )
                                                    currentTx = updated
                                                    onUpdateTransaction(updated)
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        context.getString(R.string.tx_detail_location_success),
                                                        android.widget.Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        context.getString(R.string.tx_detail_location_failed),
                                                        android.widget.Toast.LENGTH_LONG
                                                    ).show()
                                                    showPresetSheet = true
                                                }
                                            }
                                        }
                                    },
                                    shapes = AppButtonDefaults.shapes(),
                                    enabled = !isTagging,
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                ) {
                                    if (isTagging) {
                                        LoadingIndicator(
                                            modifier = Modifier.size(14.dp),
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    } else {
                                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(15.dp).postPressIconBounce())
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = stringResource(R.string.tx_detail_tag_location),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }

                                CompletePressOutlinedButton(
                                    onClick = { showPresetSheet = true },
                                    shapes = AppButtonDefaults.shapes(),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(15.dp).postPressIconBounce())
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.tx_detail_tag_preset),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                            if (onOpenFullScreenMap != null) {
                                CompletePressOutlinedButton(
                                    onClick = onOpenFullScreenMap,
                                    shapes = AppButtonDefaults.shapes(),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(36.dp)
                                        .testTag("PickOnFullScreenOsmButton")
                                ) {
                                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(15.dp).postPressIconBounce())
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.tx_detail_pick_on_map),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Detail Info Grid
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Account row
                        DetailRow(
                            modifier = Modifier.sharedTransitionBounds(SharedTxKeys.Account),
                            icon = Icons.Default.AccountBalanceWallet,
                            label = stringResource(R.string.label_account),
                            value = when {
                                isTransfer -> "${sourceAccount?.name ?: "#${currentTx.accountId}"} → ${destinationAccount?.name ?: "#${currentTx.toAccountId}"}"
                                else -> sourceAccount?.name ?: "#${currentTx.accountId}"
                            }
                        )

                        // Category row
                        if (!isTransfer) {
                            DetailRow(
                                icon = Icons.Default.Category,
                                label = stringResource(R.string.cat_others),
                                value = com.example.vibefinance.ui.home.getCategoryDisplayName(currentTx.category)
                            )
                        }

                        // Daily budget status
                        if (isExpense) {
                            DetailRow(
                                icon = if (currentTx.isExcludedFromDailyBudget) Icons.Outlined.EventBusy else Icons.Outlined.EventAvailable,
                                label = stringResource(R.string.daily_budget_toggle_title),
                                value = if (currentTx.isExcludedFromDailyBudget) {
                                    stringResource(R.string.daily_budget_toggle_desc_off)
                                } else {
                                    stringResource(R.string.daily_budget_toggle_desc_on)
                                }
                            )
                        }

                        // Reimbursement status
                        if (currentTx.isReimbursement) {
                            DetailRow(
                                icon = Icons.AutoMirrored.Outlined.AssignmentReturn,
                                label = stringResource(R.string.reimbursement_title),
                                value = if (currentTx.reimbursementOf != null) {
                                    "#${currentTx.reimbursementOf}"
                                } else {
                                    stringResource(R.string.reimbursement_unlink)
                                }
                            )
                        }
                    }
                }
                NotificationSourceCard(
                    transaction = currentTx,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isExpense && !isReimbursement && !currentTx.isBalanceAdjustment && currentTx.timestamp <= System.currentTimeMillis()) {
                    CompletePressFilledTonalButton(
                        onClick = {
                            VibeHaptics.selection(view)
                            showSplitSheet = true
                        },
                        shapes = AppButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth().testTag("TransactionSplitBillButton")
                    ) {
                        Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(18.dp).postPressIconBounce())
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.split_bill_action), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete button (Error tone)
                CompletePressTextButton(
                    onClick = { showDeleteConfirm = true },
                    shapes = AppButtonDefaults.shapes(),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("DetailDeleteButton")
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp).postPressIconBounce())
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.btn_delete))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Close button
                    CompletePressOutlinedButton(
                        onClick = onDismissRequest,
                        shapes = AppButtonDefaults.shapes(),
                        modifier = Modifier.testTag("DetailCloseButton")
                    ) {
                        Text(stringResource(R.string.btn_close))
                    }

                    // Edit button
                    CompletePressButton(
                        onClick = {
                            // In the morphing phone dialog the detail stays open behind the edit form.
                            if (!inline) onDismissRequest()
                            onEdit(currentTx)
                        },
                        shapes = AppButtonDefaults.shapes(),
                        modifier = Modifier.testTag("DetailEditButton")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp).postPressIconBounce())
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.btn_edit))
                    }
                }
            }
        }
    )

    // District / Location Preset Sheet Dialog
    if (showPresetSheet && onUpdateTransaction != null) {
        AlertDialog(
            onDismissRequest = { showPresetSheet = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = stringResource(R.string.tx_detail_select_district_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    val presets = com.example.vibefinance.util.GnssLocationManager.PRESETS
                    presets.forEachIndexed { index, preset ->
                        val isSelectedPreset = currentTx.locationName == preset.title
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val rowShape = com.example.vibefinance.ui.main.rememberConnectedListRowShape(
                            isFirst = index == 0,
                            isLast = index == presets.lastIndex,
                            isSelected = isSelectedPreset,
                            isPressed = isPressed
                        )
                        Surface(
                            onClick = {
                                showPresetSheet = false
                                val updated = currentTx.copy(
                                    latitude = preset.latitude,
                                    longitude = preset.longitude,
                                    locationName = preset.title
                                )
                                currentTx = updated
                                onUpdateTransaction(updated)
                                android.widget.Toast.makeText(
                                    context,
                                    context.getString(R.string.tx_detail_location_success) + ": " + preset.title,
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            },
                            interactionSource = interactionSource,
                            shape = rowShape,
                            color = if (isSelectedPreset) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = preset.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Clear Location option if location currently exists
                    if (currentTx.latitude != null) {
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            onClick = {
                                showPresetSheet = false
                                val updated = currentTx.copy(
                                    latitude = null,
                                    longitude = null,
                                    locationName = null
                                )
                                currentTx = updated
                                onUpdateTransaction(updated)
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = stringResource(R.string.tx_detail_remove_location),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                CompletePressTextButton(
                    onClick = { showPresetSheet = false },
                    shapes = AppButtonDefaults.shapes(),
                ) {
                    Text(stringResource(R.string.btn_close))
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.tx_detail_delete_confirm_title)) },
            text = { Text(stringResource(R.string.tx_detail_delete_confirm_desc)) },
            confirmButton = {
                CompletePressButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDismissRequest()
                        onDelete(currentTx)
                    },
                    shapes = AppButtonDefaults.shapes(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.btn_delete))
                }
            },
            dismissButton = {
                CompletePressTextButton(
                    onClick = { showDeleteConfirm = false },
                    shapes = AppButtonDefaults.shapes()
                ) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    if (showSplitSheet) {
        SplitAmountSheet(
            transaction = currentTx,
            onDismiss = { showSplitSheet = false },
            onRecordReimbursement = onRecordReimbursement
        )
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        Spacer(Modifier.width(16.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

@Composable
private fun NotificationSourceCard(
    transaction: com.example.vibefinance.data.entity.TransactionEntity,
    modifier: Modifier = Modifier
) {
    val pkg = transaction.notificationPackage?.takeIf { it.isNotBlank() }
    val title = transaction.notificationTitle?.takeIf { it.isNotBlank() }
    val body = transaction.notificationText?.takeIf { it.isNotBlank() }
    if (pkg == null && title == null && body == null) return

    val context = LocalContext.current
    val appLabel = remember(pkg) {
        if (pkg == null) "" else com.example.vibefinance.ui.settings.resolveAppLabel(context, pkg)
    }

    Surface(
        modifier = modifier.testTag("TransactionNotificationSourceCard"),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(
                        R.string.tx_source_notification_title,
                        appLabel.ifBlank { "Notification" }
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (body != null) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

