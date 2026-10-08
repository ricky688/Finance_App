package com.example.vibefinance.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vibefinance.R
import com.example.vibefinance.data.currency.ExchangeRateResult
import com.example.vibefinance.util.CurrencyEngine

@Composable
internal fun CurrencyRateStatus(
    currencyCode: String,
    result: ExchangeRateResult?,
    loading: Boolean,
    convertedAmount: Double?,
    onRefresh: () -> Unit
) {
    val quote = result?.quote
    Surface(Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("EntryExchangeRate"),
        shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Row(Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                when {
                    loading -> {
                        Text(stringResource(R.string.currency_rate_loading, currencyCode), style = MaterialTheme.typography.bodyMedium)
                        LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 6.dp))
                    }
                    quote == null -> Text(stringResource(R.string.currency_rate_unavailable),
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("EntryExchangeRate_error"))
                    else -> {
                        Text(stringResource(R.string.currency_rate_value, currencyCode, quote.rateText()),
                            style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        convertedAmount?.let {
                            Text(stringResource(R.string.currency_converted_amount, CurrencyEngine.formatHkd(it)),
                                style = MaterialTheme.typography.bodyMedium, modifier = Modifier.testTag("EntryConvertedAmount"))
                        }
                        Text(stringResource(R.string.currency_rate_date, quote.source, quote.rateDate),
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (result.offline) Text(stringResource(R.string.currency_rate_cached),
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.testTag("EntryExchangeRate_cached"))
                    }
                }
            }
            IconButton(onClick = onRefresh, enabled = !loading, modifier = Modifier.testTag("EntryExchangeRate_refresh")) {
                Icon(Icons.Default.Refresh, stringResource(R.string.currency_rate_refresh))
            }
        }
    }
}
