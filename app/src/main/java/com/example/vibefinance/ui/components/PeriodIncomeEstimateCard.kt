package com.example.vibefinance.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.vibefinance.R
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import com.example.vibefinance.util.CurrencyEngine
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun PeriodIncomeEstimateCard(state: FinanceUiState, modifier: Modifier = Modifier) {
    val period = state.budgetInfo?.takeIf { it.daysLeft > 0 && it.endDate >= it.startDate }
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
                Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.period_income_title), style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f))
            }
            if (dateRange != null) Text(dateRange, style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("IncomeEstimatePeriod"))
            Text(period?.estimatedIncome?.let(CurrencyEngine::formatHkd) ?: stringResource(R.string.period_income_not_set),
                style = MaterialTheme.typography.headlineSmall, maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("PeriodIncomeEstimateAmount"))
            if (period != null && period.incomeEstimatePeriods > 0) {
                Text(pluralStringResource(R.plurals.period_income_learned, period.incomeEstimatePeriods, period.incomeEstimatePeriods),
                    style = MaterialTheme.typography.bodySmall, modifier = Modifier.testTag("IncomeEstimateBasis"))
                if (period.incomeEstimatePartialHistory) Text(stringResource(R.string.period_income_partial),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(stringResource(if (period == null) R.string.period_income_requires_budget else R.string.period_income_reference),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
