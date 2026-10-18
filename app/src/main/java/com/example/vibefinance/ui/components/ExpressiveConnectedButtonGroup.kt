@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.example.vibefinance.util.rememberVibeHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** Native connected shapes with Light-only focus fill and a uniform Dark crossfade. */
@Composable
fun <T> ExpressiveConnectedButtonGroup(
    items: List<T>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    lightModeFocusMotion: Boolean = true,
    compact: Boolean = false,
    focusBackdropColor: Color = Color.Unspecified,
    inactiveContainerColor: Color = Color.Unspecified,
    inactiveContentColor: Color = Color.Unspecified,
    activeContainerColor: Color = Color.Unspecified,
    activeContentColor: Color = Color.Unspecified,
    iconProvider: @Composable ((T, Color) -> Unit)? = null,
    animatedIconProvider: @Composable ((item: T, tint: Color, isSelected: Boolean, clickTrigger: Int) -> Unit)? = null,
    itemModifierProvider: ((Int) -> Modifier)? = null,
    labelProvider: @Composable (T) -> String
) {
    val colors = MaterialTheme.colorScheme
    val haptic = rememberVibeHapticFeedback()
    val clickTriggers = remember(items.size) { androidx.compose.runtime.mutableStateListOf(*Array(items.size) { 0 }) }

    Row(
        modifier = modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = index == selectedIndex
            val interactionSource = remember(item) { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val colorMotion = rememberConnectedButtonColorMotion(
                isSelected = isSelected,
                isPressed = isPressed,
                inactiveContainerColor = if (inactiveContainerColor != Color.Unspecified) inactiveContainerColor else colors.primaryContainer.copy(alpha = 0.4f),
                inactiveContentColor = if (inactiveContentColor != Color.Unspecified) inactiveContentColor else colors.onPrimaryContainer,
                activeContainerColor = if (activeContainerColor != Color.Unspecified) activeContainerColor else colors.primary,
                activeContentColor = if (activeContentColor != Color.Unspecified) activeContentColor else colors.onPrimary,
                backdropColor = if (focusBackdropColor == Color.Unspecified) colors.surface else focusBackdropColor,
                enabled = lightModeFocusMotion
            )

            // Compact controls retain a 48dp row allocation around the 40dp visual button.
            Box(
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                ConnectedButtonRipple(isSelected = isSelected) {
                    CompletePressToggleButton(
                        checked = isSelected,
                        onCheckedChange = {
                            if (index < clickTriggers.size) {
                                clickTriggers[index] = clickTriggers[index] + 1
                            }
                            onItemSelected(index)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        interactionSource = interactionSource,
                        shapes = when {
                            items.size == 1 -> ToggleButtonDefaults.shapes()
                            index == 0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            index == items.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                        },
                        colors = ToggleButtonDefaults.toggleButtonColors(
                            containerColor = colorMotion.containerColor,
                            checkedContainerColor = colorMotion.containerColor,
                            contentColor = colorMotion.contentColor,
                            checkedContentColor = colorMotion.contentColor
                        ),
                        elevation = null,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.fillMaxWidth()
                            .then(itemModifierProvider?.invoke(index) ?: Modifier)
                            .heightIn(min = if (compact) 40.dp else 48.dp)
                            .semantics {
                                selected = isSelected
                                role = Role.RadioButton
                            }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .heightIn(min = if (compact) 40.dp else 48.dp)
                                .then(colorMotion.contentModifier),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = if (compact) 4.dp else 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (animatedIconProvider != null) {
                                    val trigger = if (index < clickTriggers.size) clickTriggers[index] else 0
                                    animatedIconProvider(item, colorMotion.contentColor, isSelected, trigger)
                                    Spacer(Modifier.width(6.dp))
                                } else if (iconProvider != null) {
                                    val iconScale by animateFloatAsState(
                                        targetValue = if (isSelected) 1.08f else 1f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        ),
                                        label = "btnGroupIconScale_$index"
                                    )
                                    Box(
                                        modifier = Modifier.graphicsLayer {
                                            scaleX = iconScale
                                            scaleY = iconScale
                                        },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        iconProvider(item, colorMotion.contentColor)
                                    }
                                    Spacer(Modifier.width(6.dp))
                                } else {
                                    AnimatedVisibility(
                                        visible = isSelected,
                                        enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) + expandHorizontally(
                                            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
                                        ),
                                        exit = fadeOut(spring(stiffness = Spring.StiffnessMediumLow)) + shrinkHorizontally(
                                            spring(stiffness = Spring.StiffnessMediumLow)
                                        )
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                        }
                                    }
                                }
                                Text(
                                    text = labelProvider(item),
                                    style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    maxLines = 1,
                                    softWrap = false,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
