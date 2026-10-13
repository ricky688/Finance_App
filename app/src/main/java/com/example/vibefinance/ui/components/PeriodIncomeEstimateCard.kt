package com.example.vibefinance.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.vibefinance.R
import com.example.vibefinance.ui.FinanceIntent
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import com.example.vibefinance.util.CurrencyEngine
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.UUID

internal fun parseIncomeEstimate(text: String): Double? = text.trim().toBigDecimalOrNull()
    ?.takeIf { it.signum() >= 0 && it.stripTrailingZeros().scale() <= 2 }
    ?.toDouble()?.takeIf { it.isFinite() }

@Composable
fun PeriodIncomeEstimateCard(state: FinanceUiState, onIntent: (FinanceIntent) -> Unit,
    modifier: Modifier = Modifier) {
    val period = state.budgetInfo?.takeIf { it.daysLeft > 0 && it.endDate >= it.startDate }
    var editing by rememberSaveable(period?.startDate, period?.endDate) { mutableStateOf(false) }
    var draft by rememberSaveable(period?.startDate, period?.endDate) { mutableStateOf("") }
    var pendingRequest by rememberSaveable { mutableStateOf<String?>(null) }
    var saveFailed by rememberSaveable { mutableStateOf(false) }
    val latestState by rememberUpdatedState(state)
    LaunchedEffect(state.incomeEstimateResult, pendingRequest, state.isSavingIncomeEstimate, state.isLoading) {
        val result = state.incomeEstimateResult?.takeIf { it.requestId == pendingRequest }
        if (result != null) {
            saveFailed = !result.saved
            if (result.saved) editing = false
            pendingRequest = null
        } else if (pendingRequest != null && !state.isSavingIncomeEstimate && !state.isLoading) {
            // A restored draft can outlive the process that owned its save operation.
            withFrameNanos { }
            if (!latestState.isSavingIncomeEstimate && latestState.incomeEstimateResult?.requestId != pendingRequest) {
                saveFailed = true
                pendingRequest = null
            }
        }
    }
    val locale = LocalConfiguration.current.locales[0]
    val formatter = remember(locale) { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale) }
    val dateRange = period?.let {
        val zone = ZoneId.systemDefault()
        "${formatter.format(Instant.ofEpochMilli(it.startDate).atZone(zone))} – ${formatter.format(Instant.ofEpochMilli(it.endDate).atZone(zone))}"
    }
    Card(modifier.fillMaxWidth().testTag("PeriodIncomeEstimateCard"), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TrendingUp, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.period_income_title), style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f))
                if (period != null) IconButton(onClick = {
                    draft = period.estimatedIncome?.let { java.math.BigDecimal.valueOf(it).toPlainString() }.orEmpty()
                    saveFailed = false
                    editing = true
                }, enabled = !state.isSavingIncomeEstimate, modifier = Modifier.testTag("EditPeriodIncomeEstimate")) {
                    Icon(Icons.Default.Edit, stringResource(R.string.period_income_edit))
                }
            }
            if (dateRange != null) Text(dateRange, style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("IncomeEstimatePeriod"))
            Text(period?.estimatedIncome?.let(CurrencyEngine::formatHkd) ?: stringResource(R.string.period_income_not_set),
                style = MaterialTheme.typography.headlineSmall, maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("PeriodIncomeEstimateAmount"))
            Text(stringResource(if (period == null) R.string.period_income_requires_budget else R.string.period_income_reference),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    if (editing && period != null) {
        val parsed = parseIncomeEstimate(draft)
        val invalid = draft.isNotBlank() && parsed == null
        val busy = pendingRequest != null
        AlertDialog(onDismissRequest = { if (!busy) editing = false },
            modifier = Modifier.testTag("PeriodIncomeEstimateDialog"),
            title = { Text(stringResource(R.string.period_income_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(dateRange.orEmpty(), style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(value = draft, onValueChange = { draft = it; saveFailed = false },
                        label = { Text(stringResource(R.string.period_income_amount_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true, enabled = !busy, isError = invalid || saveFailed,
                        supportingText = { Text(stringResource(when {
                            invalid -> R.string.period_income_invalid
                            saveFailed -> R.string.period_income_save_error
                            else -> R.string.period_income_clear_hint
                        })) }, modifier = Modifier.fillMaxWidth().testTag("IncomeEstimateInput"))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (pendingRequest == null && !state.isSavingIncomeEstimate) {
                        val request = UUID.randomUUID().toString()
                        pendingRequest = request
                        onIntent(FinanceIntent.SetPeriodIncomeEstimate(period.startDate, period.endDate, parsed, request))
                    }
                }, enabled = !invalid && !busy && !state.isSavingIncomeEstimate, modifier = Modifier.testTag("SaveIncomeEstimate")) {
                    if (busy) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.btn_save))
                }
            }, dismissButton = {
                TextButton(onClick = { editing = false }, enabled = !busy,
                    modifier = Modifier.testTag("CancelIncomeEstimate")) { Text(stringResource(R.string.btn_cancel)) }
            })
    }
}
