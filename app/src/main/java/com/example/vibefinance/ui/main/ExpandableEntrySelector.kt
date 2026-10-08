package com.example.vibefinance.ui.main

import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.animateFloat
import com.example.vibefinance.ui.common.ScrollBlurContainer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.unit.*
import kotlin.math.roundToInt
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.vibefinance.R
import com.example.vibefinance.ui.components.*
import com.example.vibefinance.data.entity.AccountType

@Composable
internal fun accountTypeLabel(type: AccountType): String = stringResource(when (type) {
    AccountType.CASH -> R.string.acc_type_cash
    AccountType.BANK -> R.string.ui_main_bank_short
    AccountType.DEBIT -> R.string.ui_main_debit_short
    AccountType.CC -> R.string.ui_main_credit_short
})

/** The menu is hosted over the measured keypad, independent of the form's layout. */
internal class EntrySelectorOverlayState {
    var activeTag by mutableStateOf<String?>(null)
    var assetBounds by mutableStateOf(androidx.compose.ui.geometry.Rect.Zero)
    var keypadBounds by mutableStateOf(androidx.compose.ui.geometry.Rect.Zero)
}
internal val LocalEntrySelectorOverlay = staticCompositionLocalOf<EntrySelectorOverlayState> {
    error("Entry selector requires its keypad overlay host")
}

@Composable
internal fun <T> ExpandableEntrySelector(
    items: List<T>, selectedIndex: Int, onItemSelected: (Int) -> Unit,
    title: String, tagPrefix: String, keyProvider: (T) -> String,
    labelProvider: @Composable (T) -> String, modifier: Modifier = Modifier,
    overlayIncludesAssets: Boolean = false,
    compactChoices: Boolean = false,
    subtitleProvider: (@Composable (T) -> String)? = null,
    iconProvider: (@Composable (T, Color) -> Unit)? = null,
    onAdd: (() -> Unit)? = null
) {
    val host = LocalEntrySelectorOverlay.current
    val expanded = host.activeTag == tagPrefix
    val density = LocalDensity.current
    var rowHeight by remember { mutableIntStateOf(0) }
    var selectionRevealRequest by remember { mutableIntStateOf(0) }
    val expandLabel = stringResource(if (expanded) R.string.entry_selector_collapse else R.string.entry_selector_expand, title)
    fun add() { host.activeTag = null; onAdd?.invoke() }
    Row(modifier.fillMaxWidth().testTag(tagPrefix), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        // Reuse the original connected group, including its natural height and motion.
        ExpressiveSegmentedButtonGroup(items, selectedIndex, onItemSelected,
            modifier = Modifier.weight(1f).onSizeChanged { rowHeight = it.height }.testTag("${tagPrefix}_row"),
            isScrollable = true, iconProvider = iconProvider, fadeTagPrefix = "${tagPrefix}_row",
            selectionRevealRequest = selectionRevealRequest,
            itemModifierProvider = { index -> Modifier.testTag("${tagPrefix}_row_${keyProvider(items[index])}") },
            trailingContent = if (onAdd != null) { { ExpressiveAddButton(text = stringResource(R.string.btn_add), onClick = ::add) } } else null,
            labelProvider = labelProvider)
        Surface(
            onClick = { host.activeTag = if (expanded) null else tagPrefix },
            shape = RoundedCornerShape(16.dp),
            color = if (expanded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = if (expanded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            border = BorderStroke(1.dp, if (expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.width(56.dp).then(if (rowHeight > 0) Modifier.height(with(density) { rowHeight.toDp() }) else Modifier)
                .testTag("${tagPrefix}_toggle").semantics { stateDescription = expandLabel }
        ) {
            Row(Modifier.padding(horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Icon(Icons.Default.GridView, expandLabel, Modifier.size(18.dp))
                Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, Modifier.size(18.dp))
            }
        }
    }
    val keypad = host.keypadBounds
    val assets = host.assetBounds
    val bounds = if (overlayIncludesAssets && assets.height > 0) {
        keypad.copy(top = minOf(assets.top, keypad.top))
    } else keypad
    val visibility = remember { MutableTransitionState(false) }
    visibility.targetState = expanded
    val panelTransition = rememberTransition(visibility, label = "${tagPrefix}_panel")
    val panelOpacity by panelTransition.animateFloat(
        transitionSpec = { spring(stiffness = Spring.StiffnessMediumLow) },
        label = "panelOpacity"
    ) { visible -> if (visible) 1f else 0f }
    val panelShape = RoundedCornerShape(16.dp)
    val shadowPadding = 8.dp
    val shadowPaddingPx = with(density) { shadowPadding.roundToPx() }
    if ((visibility.currentState || visibility.targetState) && bounds.width > 0 && bounds.height > 0) {
        Popup(
            popupPositionProvider = remember(bounds, shadowPaddingPx) { object : PopupPositionProvider {
                override fun calculatePosition(anchorBounds: IntRect, windowSize: IntSize,
                    layoutDirection: LayoutDirection, popupContentSize: IntSize): IntOffset =
                    IntOffset(bounds.left.roundToInt() - shadowPaddingPx, bounds.top.roundToInt() - shadowPaddingPx)
            } },
            onDismissRequest = { host.activeTag = null },
            // A modal popup owns the whole outside-touch gesture. Otherwise dismissal on
            // DOWN lets UP click the underlying toggle and immediately reopen the menu.
            properties = PopupProperties(focusable = true)
        ) {
            // Keep room for the shadow throughout the transition without changing the covered area.
            Box(Modifier.size(with(density) { bounds.width.toDp() } + shadowPadding * 2,
                with(density) { bounds.height.toDp() } + shadowPadding * 2).padding(shadowPadding)) {
                panelTransition.AnimatedVisibility(
                    visible = { it },
                    modifier = Modifier.graphicsLayer {
                        // This layer sees the animated size, so its outline/shadow follow the moving edge.
                        shape = panelShape
                        clip = true
                        alpha = panelOpacity
                        shadowElevation = 4.dp.toPx() * panelOpacity
                    },
                    enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow), expandFrom = Alignment.Top),
                    exit = shrinkVertically(spring(stiffness = Spring.StiffnessMediumLow), shrinkTowards = Alignment.Top)
                ) {
                    Surface(Modifier.fillMaxSize().testTag("${tagPrefix}_expanded"), shape = panelShape,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shadowElevation = 0.dp) {
                        // Consume blank-space touches so none reach the amount buttons below.
                        Column(Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures {} }.padding(8.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                items.getOrNull(selectedIndex)?.let { item ->
                                    Text(labelProvider(item), style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 120.dp))
                                }
                                IconButton(onClick = { host.activeTag = null }, modifier = Modifier.testTag("${tagPrefix}_collapse")) {
                                    Icon(Icons.Default.KeyboardArrowUp, stringResource(R.string.entry_selector_collapse, title))
                                }
                            }
                            val gridScroll = rememberScrollState()
                            ScrollBlurContainer(gridScroll, Modifier.weight(1f).fillMaxWidth(), vertical = true, tagPrefix = "${tagPrefix}_grid") { sourceModifier ->
                                BoxWithConstraints(sourceModifier.fillMaxSize().verticalScroll(gridScroll)
                                    .selectableGroup().testTag("${tagPrefix}_grid")) {
                                    val columns = (maxWidth / (120f * density.fontScale.coerceAtLeast(1f)).dp).toInt().coerceIn(1, 5)
                                    val count = items.size + if (onAdd != null) 1 else 0
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        (0 until count).toList().chunked(columns).forEach { indices ->
                                            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                indices.forEach { index ->
                                                    if (index < items.size) {
                                                        val item = items[index]
                                                        EntryChoiceTile(labelProvider(item), subtitleProvider?.invoke(item), index == selectedIndex,
                                                            { onItemSelected(index); selectionRevealRequest++ }, Modifier.weight(1f).fillMaxHeight().testTag("${tagPrefix}_grid_${keyProvider(item)}"),
                                                            compact = compactChoices,
                                                            icon = iconProvider?.let { provider -> { tint -> provider(item, tint) } })
                                                    } else EntryChoiceTile(stringResource(R.string.btn_add), selected = false, onClick = ::add,
                                                        modifier = Modifier.weight(1f).fillMaxHeight().testTag("${tagPrefix}_grid_add"), role = Role.Button,
                                                        compact = compactChoices,
                                                        icon = { Icon(Icons.Default.Add, null, Modifier.size(18.dp), tint = it) })
                                                }
                                                repeat(columns - indices.size) { Spacer(Modifier.weight(1f)) }
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
    }
}

@Composable
private fun EntryChoiceTile(
    label: String,
    subtitle: String? = null,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    role: Role = Role.RadioButton,
    compact: Boolean = false,
    icon: (@Composable (Color) -> Unit)? = null
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val progress by rememberCompletePressProgress(source)
    val shape = completePressShape(RoundedCornerShape(16.dp), RoundedCornerShape(16.dp), progress)
    val colors = rememberConnectedButtonColorMotion(selected, pressed,
        inactiveContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        inactiveContentColor = MaterialTheme.colorScheme.onSurface)
    Surface(shape = shape, color = colors.containerColor, contentColor = colors.contentColor,
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.heightIn(min = if (compact) 56.dp else 72.dp).clip(shape)
            .selectable(selected, role = role, interactionSource = source, indication = ripple(color = colors.rippleColor), onClick = onClick)
    ) {
        Column(Modifier.then(colors.contentModifier).padding(horizontal = 10.dp, vertical = if (compact) 6.dp else 10.dp),
            verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                icon?.invoke(colors.contentColor)
                Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f))
            }
            if (!subtitle.isNullOrBlank() && subtitle != label) {
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.labelSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
