package com.example.vibefinance.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.theme.BentoCardShape
import com.example.vibefinance.ui.common.bouncyClickable
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MinMaxSpentCard(
    modifier: Modifier = Modifier,
    isMin: Boolean,
    spends: List<TransactionEntity>,
    isDarkTheme: Boolean = false,
) {
    val minSpent = remember(spends) { spends.minByOrNull { it.amount } }
    val maxSpent = remember(spends) { spends.maxByOrNull { it.amount } }

    val spent = if (isMin) minSpent else maxSpent

    val amountText = spent?.let { String.format(Locale.US, "$%,.2f", it.amount) } ?: "-"
    val label = if (isMin) {
        stringResource(R.string.lowest_spending_day)
    } else {
        stringResource(R.string.highest_spending_day)
    }

    val isDark = isDarkTheme || MaterialTheme.colorScheme.background.luminance() < 0.5f

    val colorMinMain = MaterialTheme.colorScheme.primary
    val colorMaxMain = MaterialTheme.colorScheme.error

    val colors = if (isMin) {
        if (isDark) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.primary
            )
        } else {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    } else {
        if (isDark) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.error
            )
        } else {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }

    StatCard(
        modifier = modifier
            .clip(BentoCardShape)
            .bouncyClickable(shape = BentoCardShape) {},
        value = amountText,
        label = label,
        colors = colors,
        content = {
            Spacer(modifier = Modifier.height(2.dp))
            if (spent != null) {
                val dateLocale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
                val timeStr = remember(spent.timestamp, dateLocale) {
                    val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(spent.timestamp), ZoneId.systemDefault())
                    dt.format(DateTimeFormatter.ofPattern("dd MMM HH:mm", dateLocale))
                }
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (spent.description.isNotEmpty()) {
                    Text(
                        text = spent.description,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = LocalContentColor.current.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 1.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        backdropContent = {
            if (spends.isNotEmpty()) {
                SpendsChart(
                    modifier = Modifier.fillMaxSize(),
                    spends = spends,
                    markedTransaction = spent,
                    chartPadding = PaddingValues(vertical = 12.dp, horizontal = 12.dp),
                    colorMin = colorMinMain,
                    colorMax = colorMaxMain,
                    showBeforeMarked = 4,
                    showAfterMarked = 1,
                )
            }
        }
    )
}
