package com.example.vibefinance.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.ExpenseIcon
import com.example.vibefinance.data.entity.ExpenseSymbol

@Composable
fun ExpenseIconChoice(value: String?, category: String, onValueChange: (String?) -> Unit, modifier: Modifier = Modifier) {
    var choosing by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { choosing = true }, modifier = modifier.fillMaxWidth().testTag("ExpenseIconChoose")) {
        ExpenseEventIcon(value, category, MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Text(stringResource(R.string.expense_icon_title))
        Spacer(Modifier.weight(1f))
        Text(stringResource(if (value == null) R.string.expense_icon_default_short else R.string.expense_icon_custom_short),
            style = MaterialTheme.typography.labelMedium)
    }
    if (choosing) ExpenseIconPicker(value, category,
        onConfirm = { onValueChange(it); choosing = false }, onDismiss = { choosing = false })
}

@Composable
private fun ExpenseIconPicker(value: String?, category: String, onConfirm: (String?) -> Unit, onDismiss: () -> Unit) {
    var selected by remember { mutableStateOf(value) }
    var typedEmoji by remember { mutableStateOf(value?.takeIf { it.startsWith("emoji:") }?.removePrefix("emoji:") ?: "") }
    var typing by remember { mutableStateOf(false) }
    val emojiValue = ExpenseIcon.emoji(typedEmoji)
    val valid = !typing || emojiValue != null
    val symbolColumns = when {
        LocalDensity.current.fontScale > 1.6f -> 2
        LocalDensity.current.fontScale > 1.15f -> 3
        else -> 4
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.expense_icon_title)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.expense_icon_hint))
                OutlinedButton(onClick = { selected = null; typedEmoji = ""; typing = false }, Modifier.fillMaxWidth().testTag("ExpenseIconDefault")) {
                    ExpenseEventIcon(null, category, MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.expense_icon_default))
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    ExpenseEventIcon(if (typing) emojiValue else selected, category, MaterialTheme.colorScheme.primary, Modifier.size(40.dp))
                }
                Text(stringResource(R.string.expense_icon_symbols), style = MaterialTheme.typography.titleSmall)
                ExpenseSymbol.entries.chunked(symbolColumns).forEach { symbols ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        symbols.forEach { symbol ->
                            val token = ExpenseIcon.symbol(symbol)
                            Surface(onClick = { selected = token; typedEmoji = ""; typing = false },
                                modifier = Modifier.weight(1f).height(68.dp).testTag("ExpenseIconSymbol_${symbol.name}"),
                                shape = RoundedCornerShape(16.dp),
                                color = if (!typing && selected == token) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                                Column(Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Icon(symbol.vector(), null, Modifier.size(24.dp))
                                    Text(stringResource(symbol.label()), style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                        repeat(symbolColumns - symbols.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
                Text(stringResource(R.string.expense_icon_emoji), style = MaterialTheme.typography.titleSmall)
                listOf("🍜", "☕", "🛍️", "🚕", "🎮", "🐾", "🎁", "⭐").chunked(4).forEach { emojis ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        emojis.forEach { emoji ->
                            val token = ExpenseIcon.emoji(emoji)
                            Surface(onClick = { selected = token; typedEmoji = emoji; typing = false },
                                modifier = Modifier.weight(1f).height(48.dp).testTag("ExpenseIconEmoji_$emoji"),
                                shape = RoundedCornerShape(16.dp),
                                color = if (!typing && selected == token) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow) {
                                Box(contentAlignment = Alignment.Center) { Text(emoji) }
                            }
                        }
                    }
                }
                OutlinedTextField(typedEmoji, { if (it.length <= 64) { typedEmoji = it; typing = true } },
                    label = { Text(stringResource(R.string.expense_icon_enter_emoji)) }, singleLine = true,
                    isError = typing && emojiValue == null,
                    supportingText = { Text(stringResource(R.string.expense_icon_one_emoji)) },
                    modifier = Modifier.fillMaxWidth().testTag("ExpenseIconEmojiInput"))
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(if (typing) emojiValue else selected) }, enabled = valid,
            modifier = Modifier.testTag("ExpenseIconApply")) { Text(stringResource(R.string.btn_save)) } },
        dismissButton = { TextButton(onClick = onDismiss, modifier = Modifier.testTag("ExpenseIconCancel")) { Text(stringResource(R.string.btn_cancel)) } }
    )
}
