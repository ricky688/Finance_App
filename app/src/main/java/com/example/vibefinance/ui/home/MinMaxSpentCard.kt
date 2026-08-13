package com.example.vibefinance.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.vibefinance.data.entity.TransactionEntity
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
    val label = if (isMin) "最低消費" else "最高消費"

    val colorMinMain = Color(0xFF185ED6)
    val colorMaxMain = Color(0xFFDD1414)

    val colors = if (isMin) {
        if (isDarkTheme) {
            CardDefaults.cardColors(
                containerColor = Color(0xFF132038),
                contentColor = Color(0xFF90CAF9)
            )
        } else {
            CardDefaults.cardColors(
                containerColor = Color(0xFFE3F2FD),
                contentColor = Color(0xFF0D47A1)
            )
        }
    } else {
        if (isDarkTheme) {
            CardDefaults.cardColors(
                containerColor = Color(0xFF331619),
                contentColor = Color(0xFFEF9A9A)
            )
        } else {
            CardDefaults.cardColors(
                containerColor = Color(0xFFFFEBEE),
                contentColor = Color(0xFFC62828)
            )
        }
    }

    StatCard(
        modifier = modifier,
        value = amountText,
        label = label,
        colors = colors,
        content = {
            Spacer(modifier = Modifier.height(6.dp))
            if (spent != null) {
                val timeStr = remember(spent.timestamp) {
                    val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(spent.timestamp), ZoneId.systemDefault())
                    dt.format(DateTimeFormatter.ofPattern("dd MMM hh:mm a", Locale.US))
                }
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.bodyMedium,
                )

                if (spent.description.isNotEmpty()) {
                    Text(
                        text = spent.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalContentColor.current.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 1
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
                    chartPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp),
                    colorMin = colorMinMain,
                    colorMax = colorMaxMain,
                    showBeforeMarked = 4,
                    showAfterMarked = 1,
                )
            }
        }
    )
}
