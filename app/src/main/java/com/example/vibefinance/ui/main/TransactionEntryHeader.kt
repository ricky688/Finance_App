package com.example.vibefinance.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.R
import com.example.vibefinance.ui.components.SpringHarmonicNumberText
import com.example.vibefinance.util.CurrencyEngine
import kotlinx.coroutines.flow.collectLatest
import com.example.vibefinance.ui.preferences.PrivacyText as Text

@Composable
internal fun TransactionEntryTopBar(
    mode: TransactionMode,
    currency: String,
    onCurrencyChange: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        IconButton(onClick = onDismiss) {
            Icon(Icons.Default.ArrowBack, stringResource(R.string.nav_back))
        }
        AnimatedContent(mode, modifier = Modifier.weight(1f), label = "titleTransition") { value ->
            Text(
                text = stringResource(when (value) {
                    TransactionMode.EXPENSE -> R.string.quick_add_expense
                    TransactionMode.INCOME -> R.string.quick_add_income
                    TransactionMode.TRANSFER -> R.string.quick_transfer
                }),
                modifier = Modifier.testTag("EntryModeTitle"), style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
        Box {
            var expanded by remember { mutableStateOf(false) }
            val current = CurrencyEngine.supportedCurrencies.firstOrNull { it.code == currency }
                ?: CurrencyEngine.supportedCurrencies.first()
            Surface(
                onClick = { expanded = !expanded }, shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.testTag("EntryCurrency"),
            ) {
                Text("${current.flagEmoji} ${current.code}",
                    Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
                    maxLines = 1, softWrap = false)
            }
            DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                CurrencyEngine.supportedCurrencies.forEach { option ->
                    DropdownMenuItem(
                        text = { Text("${option.flagEmoji} ${option.code} (${option.symbol})") },
                        modifier = Modifier.testTag("EntryCurrency_${option.code}"),
                        onClick = { onCurrencyChange(option.code); expanded = false },
                    )
                }
            }
        }
    }
}

@Composable
internal fun TransactionEntryAmount(amount: String, currency: String, preview: String?) {
    val amountScroll = rememberScrollState()
    LaunchedEffect(amount) {
        // Follow animated digit widths as an expression grows, while still allowing manual review.
        snapshotFlow { amountScroll.maxValue }.collectLatest { amountScroll.scrollTo(it) }
    }
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp).testTag("EntryHero"),
        horizontalAlignment = Alignment.CenterHorizontally) {
        // Short amounts are centered; long expressions can scroll without pushing currency or title away.
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            Row(Modifier.horizontalScroll(amountScroll).widthIn(min = maxWidth),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text(
                    text = if (currency == "HKD") "HK$" else CurrencyEngine.supportedCurrencies.first { it.code == currency }.symbol,
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 38.sp, lineHeight = 48.sp),
                    color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black,
                )
                Spacer(Modifier.width(6.dp))
                SpringHarmonicNumberText(
                    text = amount, modifier = Modifier.testTag("EntryAmount").semantics { stateDescription = amount },
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 42.sp, lineHeight = 52.sp),
                    color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black,
                )
            }
        }
        Box(Modifier.height(22.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (preview != null) Text(
                text = "= $preview", modifier = Modifier.testTag("EntryCalculatorResult"),
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
