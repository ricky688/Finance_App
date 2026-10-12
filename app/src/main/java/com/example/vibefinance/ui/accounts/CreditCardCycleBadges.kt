package com.example.vibefinance.ui.accounts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import com.example.vibefinance.util.CreditCardCycleCalculator
import kotlinx.coroutines.delay
import java.time.temporal.ChronoUnit

@Composable
internal fun rememberFinancialNow(): Long {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { now = System.currentTimeMillis() }
    LaunchedEffect(Unit) { while (true) { delay(60_000 - System.currentTimeMillis() % 60_000); now = System.currentTimeMillis() } }
    return now
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CreditCardCycleBadges(account: AccountEntity, transactions: List<TransactionEntity>, modifier: Modifier = Modifier) {
    val now = rememberFinancialNow()
    val calculator = remember(now) { CreditCardCycleCalculator() }
    val metrics = remember(account, transactions, now) { calculator.metrics(account, transactions) }
    val today = calculator.today
    FlowRow(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
            Row(Modifier.padding(horizontal = 8.dp, vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                metrics.utilization?.let { ratio -> CircularProgressIndicator(progress = { ratio }, modifier = Modifier.size(14.dp), strokeWidth = 2.dp) }
                Text(stringResource(R.string.cycle_spent, metrics.spent), style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
        }
        metrics.remaining?.let { remaining ->
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerHighest) {
                Text(stringResource(R.string.cycle_remaining, remaining), Modifier.padding(8.dp, 5.dp), style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
        }
        account.billingDate?.takeIf { it in 1..31 }?.let {
            val days = ChronoUnit.DAYS.between(today, metrics.cycle.closing).toInt()
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                Text(if (days == 0) stringResource(R.string.cycle_closes_today) else stringResource(R.string.cycle_closes_in, metrics.cycle.closing.dayOfMonth, days), Modifier.padding(8.dp, 5.dp), style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
        }
        calculator.nextDue(account)?.let { due ->
            val days = ChronoUnit.DAYS.between(today, due).toInt()
            val color = when { days <= 3 -> MaterialTheme.colorScheme.error; days <= 7 -> Color(0xFFF57C00); else -> MaterialTheme.colorScheme.primary }
            Surface(shape = RoundedCornerShape(12.dp), color = color.copy(alpha = 0.12f)) {
                Text(if (days == 0) stringResource(R.string.ah_due_today) else stringResource(R.string.cycle_due_in, due.dayOfMonth, days), Modifier.padding(8.dp, 5.dp), color = color, style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
        }
    }
}
