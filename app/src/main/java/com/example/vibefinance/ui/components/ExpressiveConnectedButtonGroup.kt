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
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
    labelProvider: @Composable (T) -> String
) {
    val colors = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current

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
                backdropColor = if (focusBackdropColor == Color.Unspecified) colors.surface else focusBackdropColor,
                enabled = lightModeFocusMotion
            )

            // Compact controls retain a 48dp row allocation around the 40dp visual button.
            Box(
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                ConnectedButtonRipple {
                    CompletePressToggleButton(
                        checked = isSelected,
                        onCheckedChange = {
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
                                Text(
                                    text = labelProvider(item),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
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
