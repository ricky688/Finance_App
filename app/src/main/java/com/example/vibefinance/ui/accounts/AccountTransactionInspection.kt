package com.example.vibefinance.ui.accounts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.data.entity.historyAmountFor
import com.example.vibefinance.data.repository.DailyBudgetInfo
import com.example.vibefinance.theme.rememberIconShape
import com.example.vibefinance.ui.common.PageContentEntrance
import com.example.vibefinance.ui.components.ExpenseEventIcon
import com.example.vibefinance.ui.components.ExpressiveSwipeRow
import com.example.vibefinance.ui.components.HistoryEmptyState
import com.example.vibefinance.ui.home.getCategoryDisplayName
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import com.example.vibefinance.util.VibeHaptics
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private fun AccountEntity.inspectionLabel(): String =
    nickname?.trim()?.takeIf { it.isNotEmpty() } ?: name

/**
 * Right-panel recent transactions for an inspected account on large screens, styled identically
 * to the History screen (pill day headers, daily net/period badges, and grouped swipe rows).
 */
@Composable
internal fun AccountTransactionInspection(
    id: Long,
    transactions: List<TransactionEntity>,
    hideAmounts: Boolean,
    accounts: List<AccountEntity> = emptyList(),
    budgetInfo: DailyBudgetInfo? = null,
    onViewAccountHistory: () -> Unit = {},
    onDeleteTransaction: (TransactionEntity) -> Unit = {},
) {
    val zone = remember { ZoneId.systemDefault() }
    val dateLocale = LocalConfiguration.current.locales[0]
    val isZh = dateLocale.language.startsWith("zh")
    val datePattern = if (isZh) "yyyy年M月d日" else "MMM dd, yyyy"
    val dateHeaderFormatter = remember(datePattern, dateLocale) { DateTimeFormatter.ofPattern(datePattern, dateLocale) }
    val weekdayFormatter = remember(dateLocale) { DateTimeFormatter.ofPattern("EEEE", dateLocale) }
    val timeFormatter = remember(dateLocale) { DateTimeFormatter.ofPattern("HH:mm", dateLocale) }

    val strToday = stringResource(R.string.date_today)
    val strYesterday = stringResource(R.string.date_yesterday)
    val strBalanceAdjustment = stringResource(R.string.balance_adjustment_title)
    val strNonDailyExpense = stringResource(R.string.non_daily_expense_badge)
    val strFallbackCash = stringResource(R.string.acc_type_cash)
    val strFallbackAccount = stringResource(R.string.ah_fallback_account)
    val strDelete = stringResource(R.string.btn_delete)
    val view = LocalView.current

    val accountsMap = remember(accounts) { accounts.associateBy { it.id } }

    val groupedTransactions = remember(id, transactions, zone) {
        val now = System.currentTimeMillis()
        val recent = transactions
            .filter { it.accountId == id || it.toAccountId == id }
            .sortedByDescending { it.timestamp }
            .take(20)
        val presentAndPast = recent.filter { it.timestamp <= now }.sortedByDescending { it.timestamp }
        val future = recent.filter { it.timestamp > now }.sortedBy { it.timestamp }
        (presentAndPast + future).groupBy { tx ->
            LocalDateTime.ofInstant(Instant.ofEpochMilli(tx.timestamp), zone).toLocalDate()
        }
    }

    val activePeriodStart = budgetInfo?.startDate ?: 0L
    val activePeriodEnd = budgetInfo?.endDate ?: 0L
    val hasBudgetPeriod = activePeriodStart > 0L && activePeriodEnd >= activePeriodStart
    val activePeriodEndExclusive = if (hasBudgetPeriod) {
        Instant.ofEpochMilli(activePeriodEnd).atZone(zone).toLocalDate()
            .plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    } else 0L

    val ratioSpent = if (budgetInfo != null && budgetInfo.dailyAllowance > 0) {
        val spentToday = budgetInfo.dailyAllowance - budgetInfo.dailyRemaining
        spentToday / budgetInfo.dailyAllowance
    } else {
        0.0
    }
    val activeVibeColor = when {
        ratioSpent <= 0.5 -> MaterialTheme.colorScheme.primary
        ratioSpent <= 1.0 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.tertiary
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("AccountTransactionInspection")
    ) {
        PageContentEntrance("inspect-history-header", delayMillis = 40) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.adaptive_recent_transactions),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                com.example.vibefinance.ui.components.CompletePressTextButton(
                    onClick = {
                        VibeHaptics.selection(view)
                        onViewAccountHistory()
                    },
                    shapes = com.example.vibefinance.ui.components.AppButtonDefaults.shapes(),
                ) {
                    Text(
                        text = stringResource(R.string.assets_view_history),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        if (groupedTransactions.isEmpty()) {
            PageContentEntrance("inspect-history-empty", delayMillis = 60) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    HistoryEmptyState(
                        message = stringResource(R.string.no_transactions),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            val groupedList = groupedTransactions.toList()
            val today = remember(zone) { LocalDate.now(zone) }
            val nowMillis = remember { System.currentTimeMillis() }

            groupedList.forEachIndexed { groupIndex, (localDate, txsForDate) ->
                val dateLabel = when (localDate) {
                    today -> strToday
                    today.minusDays(1) -> strYesterday
                    else -> localDate.format(dateHeaderFormatter)
                }
                val weekday = localDate.format(weekdayFormatter)
                val headerText = "$dateLabel · $weekday"
                val dailySummaryTransactions = txsForDate.filter { tx -> tx.toAccountId == null }
                val dailyNet = dailySummaryTransactions.sumOf { tx -> tx.historyAmountFor(id) }
                val isInActivePeriod = hasBudgetPeriod && txsForDate.any { tx ->
                    tx.timestamp >= activePeriodStart && tx.timestamp < activePeriodEndExclusive
                }

                PageContentEntrance("inspect-day-$localDate", delayMillis = 50 + (groupIndex * 15).coerceAtMost(60)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
                            .padding(start = 14.dp, end = 10.dp, top = 6.dp, bottom = 6.dp)
                            .testTag("HistoryDayHeader"),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = headerText,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        if (dailySummaryTransactions.isNotEmpty()) {
                            Text(
                                text = if (hideAmounts) {
                                    "••••"
                                } else {
                                    stringResource(
                                        R.string.daily_net_format,
                                        when {
                                            dailyNet > 0.0 -> "+"
                                            dailyNet < 0.0 -> "-"
                                            else -> ""
                                        },
                                        kotlin.math.abs(dailyNet)
                                    )
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = activeVibeColor,
                                maxLines = 1,
                                modifier = Modifier.testTag("HistoryDayNet")
                            )
                        }

                        if (budgetInfo != null && budgetInfo.startDate > 0L && budgetInfo.endDate > 0L) {
                            Surface(
                                shape = CircleShape,
                                color = if (isInActivePeriod) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = if (isInActivePeriod) stringResource(R.string.badge_active_period)
                                    else stringResource(R.string.badge_past_period),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isInActivePeriod) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                txsForDate.forEachIndexed { index, tx ->
                    val sourceAccount = accountsMap[tx.accountId]
                    val destAccount = tx.toAccountId?.let { toId -> accountsMap[toId] }
                    val isFuture = tx.timestamp > nowMillis
                    val isTransfer = tx.toAccountId != null
                    val isInstallment = tx.installmentNumber != null
                    val displayDelta = tx.historyAmountFor(id)
                    val isIncome = displayDelta > 0

                    val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(tx.timestamp), zone)
                    val timeText = dateTime.format(timeFormatter)

                    val transitionState = remember(tx.id) { MutableTransitionState(true) }
                    LaunchedEffect(transitionState.currentState, transitionState.targetState) {
                        if (!transitionState.currentState && !transitionState.targetState) {
                            onDeleteTransaction(tx)
                        }
                    }

                    val isFirst = index == 0
                    val isLast = index == txsForDate.lastIndex
                    val rowInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    val isRowPressed by rowInteractionSource.collectIsPressedAsState()
                    val itemShape = com.example.vibefinance.ui.main.rememberConnectedListRowShape(
                        isFirst = isFirst,
                        isLast = isLast,
                        isSelected = false,
                        isPressed = isRowPressed
                    )

                    AnimatedVisibility(
                        visibleState = transitionState,
                        exit = shrinkVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) + fadeOut(animationSpec = tween(150))
                    ) {
                        PageContentEntrance(
                            id = "inspect-tx:${tx.id}",
                            delayMillis = (65 + index * 20).coerceAtMost(110)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                ExpressiveSwipeRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("InspectTransactionRow_${tx.id}"),
                                    shape = itemShape,
                                    interactionSource = rowInteractionSource,
                                    onEdit = {
                                        VibeHaptics.selection(view)
                                        onViewAccountHistory()
                                    },
                                    onClick = {
                                        VibeHaptics.selection(view)
                                        onViewAccountHistory()
                                    },
                                    onDelete = { transitionState.targetState = false }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(44.dp),
                                            shape = rememberIconShape("history.${tx.id}"),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                            border = BorderStroke(
                                                1.dp,
                                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                            )
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (tx.isBalanceAdjustment) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                } else {
                                                    ExpenseEventIcon(
                                                        customIcon = if (tx.isReimbursement) "icon:reimbursement"
                                                        else tx.customIcon.takeIf { tx.amount > 0 && tx.toAccountId == null },
                                                        category = tx.category,
                                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            val titleText = when {
                                                tx.isBalanceAdjustment -> strBalanceAdjustment
                                                tx.description.isNotBlank() -> tx.description
                                                tx.isReimbursement -> stringResource(R.string.reimbursement_title)
                                                else -> getCategoryDisplayName(tx.category)
                                            }
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = titleText,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                )
                                                if (tx.isExcludedFromDailyBudget && !tx.isBalanceAdjustment && tx.toAccountId == null && !isIncome) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                                                        border = BorderStroke(
                                                            0.5.dp,
                                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                                                        )
                                                    ) {
                                                        Text(
                                                            text = strNonDailyExpense,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp),
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    }
                                                }
                                            }

                                            val cardName = sourceAccount?.inspectionLabel() ?: strFallbackCash
                                            val categoryName = (if (tx.isReimbursement) stringResource(R.string.reimbursement_title) + " · " else "") +
                                                getCategoryDisplayName(tx.category)
                                            val cardSubtitle = when {
                                                tx.isBalanceAdjustment -> cardName
                                                isTransfer -> "${sourceAccount?.inspectionLabel() ?: strFallbackAccount} ➔ ${destAccount?.inspectionLabel() ?: strFallbackAccount}"
                                                isInstallment -> "$categoryName • $cardName (${tx.installmentNumber}/${tx.totalInstallments})"
                                                else -> "$categoryName • $cardName"
                                            }
                                            Text(
                                                text = cardSubtitle,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Column(
                                            horizontalAlignment = Alignment.End,
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            val amtString = if (hideAmounts) {
                                                "••••"
                                            } else {
                                                String.format(
                                                    Locale.US,
                                                    if (isIncome) "+HK$%,.2f" else "-HK$%,.2f",
                                                    kotlin.math.abs(displayDelta)
                                                )
                                            }
                                            val amountColor = when {
                                                isTransfer -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                isIncome -> MaterialTheme.colorScheme.secondary
                                                isFuture -> activeVibeColor.copy(alpha = 0.5f)
                                                else -> activeVibeColor
                                            }

                                            Text(
                                                text = amtString,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = amountColor,
                                                maxLines = 1,
                                                softWrap = false
                                            )

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = timeText,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                )
                                                IconButton(
                                                    onClick = { transitionState.targetState = false },
                                                    modifier = Modifier.size(20.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = strDelete,
                                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                if (!isLast) {
                                    Spacer(modifier = Modifier.height(5.dp))
                                }
                            }
                        }
                    }
                }

                if (groupIndex < groupedList.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}
