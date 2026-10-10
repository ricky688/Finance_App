package com.example.vibefinance.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vibefinance.R
import com.example.vibefinance.theme.IconShapeMode
import com.example.vibefinance.theme.MaterialCornerScale
import com.example.vibefinance.theme.rememberIconShape

@Composable
fun IconShapePickerDialog(
    currentShape: IconShapeMode,
    onSelectShape: (IconShapeMode) -> Unit,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isZh = LocalConfiguration.current.locales[0].language.startsWith("zh")
    val select: (IconShapeMode) -> Unit = { mode ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onSelectShape(mode)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(
                        MaterialTheme.colorScheme.primaryContainer,
                        rememberIconShape("shape-picker.title")
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.AutoAwesome, null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.appearance_icon_shape_title),
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().selectableGroup()) {
                val random = IconShapeMode.RANDOM
                val randomSelected = currentShape == random
                Surface(
                    shape = MaterialCornerScale.largeIncreased,
                    color = if (randomSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerHighest,
                    border = BorderStroke(if (randomSelected) 2.dp else 1.dp,
                        if (randomSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth().clip(MaterialCornerScale.largeIncreased)
                        .selectable(randomSelected, role = Role.RadioButton, onClick = { select(random) })
                ) {
                    Row(modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Shuffle, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(random.localizedTitle(isZh), fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall)
                            Text(random.localizedSubtitle(isZh),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 8.dp)) {
                                listOf(IconShapeMode.COOKIE_4, IconShapeMode.CIRCLE, IconShapeMode.ARCH,
                                    IconShapeMode.SUNNY).forEach { mode ->
                                    Box(Modifier.size(20.dp).background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.65f), mode.shape))
                                }
                            }
                        }
                        if (randomSelected) Icon(Icons.Filled.Check, null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                }
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(64.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp)
                ) {
                    items(IconShapeMode.roundedModes, key = { it.name }) { mode ->
                        val selected = mode == currentShape
                        val label = mode.localizedTitle(isZh)
                        Surface(
                            shape = MaterialCornerScale.largeIncreased,
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                            border = BorderStroke(if (selected) 2.dp else 1.dp,
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().size(64.dp)
                                .clip(MaterialCornerScale.largeIncreased)
                                .semantics { contentDescription = label }
                                .selectable(selected, role = Role.RadioButton, onClick = { select(mode) })
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Box(Modifier.size(38.dp)
                                    .background(if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceContainerLow, mode.shape)
                                    .border(2.dp, if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant, mode.shape),
                                    contentAlignment = Alignment.Center) {
                                    if (selected) Icon(Icons.Filled.Check, null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
                Column {
                    Text(currentShape.localizedTitle(isZh), fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleSmall)
                    Text(currentShape.localizedSubtitle(isZh),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_confirm), fontWeight = FontWeight.Bold)
            }
        },
        shape = MaterialCornerScale.extraLarge,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}
