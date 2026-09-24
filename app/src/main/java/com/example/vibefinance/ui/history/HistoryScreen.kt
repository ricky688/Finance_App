package com.example.vibefinance.ui.history

import androidx.compose.ui.res.stringResource
import com.example.vibefinance.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.LocalDensity
import com.example.vibefinance.ui.components.ExpressiveSwipeRow
import com.example.vibefinance.ui.home.CategoryIcon
import androidx.compose.ui.unit.min
import androidx.compose.runtime.derivedStateOf
import kotlin.math.abs
import kotlin.math.absoluteValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import android.widget.Toast
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.theme.LocalIconShape
import com.example.vibefinance.ui.FinanceIntent
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.components.GlassmorphicCard
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    state: FinanceUiState,
    onIntent: (FinanceIntent) -> Unit,
    modifier: Modifier = Modifier,
    topContentPadding: Dp = 16.dp
) {
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var periodFilterMode by remember { mutableStateOf(com.example.vibefinance.ui.components.PeriodFilterMode.ALL) }

    val strToday = stringResource(R.string.date_today)
    val strYesterday = stringResource(R.string.date_yesterday)

    val budgetInfo = state.budgetInfo

    val periodFilteredTransactions = remember(state.transactions, periodFilterMode, budgetInfo) {
        val bInfo = budgetInfo
        if (bInfo != null && bInfo.startDate > 0L && bInfo.endDate > 0L) {
            when (periodFilterMode) {
                com.example.vibefinance.ui.components.PeriodFilterMode.ALL -> state.transactions
                com.example.vibefinance.ui.components.PeriodFilterMode.ACTIVE_PERIOD -> state.transactions.filter { 
                    it.timestamp >= bInfo.startDate && it.timestamp <= bInfo.endDate 
                }
                com.example.vibefinance.ui.components.PeriodFilterMode.OTHER_PERIODS -> state.transactions.filter { 
                    it.timestamp < bInfo.startDate || it.timestamp > bInfo.endDate 
                }
            }
        } else {
            state.transactions
        }
    }

    val filteredTransactions = remember(periodFilteredTransactions, selectedCategoryFilter) {
        val catFilter = selectedCategoryFilter
        if (!catFilter.isNullOrBlank()) {
            periodFilteredTransactions.filter { 
                it.category.equals(catFilter, ignoreCase = true) 
            }
        } else {
            periodFilteredTransactions
        }
    }

    val transactionsList = filteredTransactions

    // Group transactions by date: present & past transactions sorted descending (newest logged first at top), followed by future scheduled transactions
    val groupedTransactions = remember(transactionsList) {
        val now = System.currentTimeMillis()
        val presentAndPast = transactionsList.filter { it.timestamp <= now }.sortedByDescending { it.timestamp }
        val future = transactionsList.filter { it.timestamp > now }.sortedBy { it.timestamp }
        (presentAndPast + future).groupBy { tx ->
            LocalDateTime.ofInstant(Instant.ofEpochMilli(tx.timestamp), ZoneId.systemDefault()).toLocalDate()
        }
    }

    // Extract current period's spent vibe color dynamically based on budget spent ratio
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

    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var editAmountText by remember { mutableStateOf("") }
    var editCategoryText by remember { mutableStateOf("") }
    var editDescriptionText by remember { mutableStateOf("") }

    // Predictive back for editing transaction modal
    androidx.activity.compose.PredictiveBackHandler(enabled = editingTransaction != null) { progressFlow ->
        try {
            progressFlow.collect { }
            editingTransaction = null
        } catch (e: kotlinx.coroutines.CancellationException) {
        }
    }

    // Predictive back for active category filter
    androidx.activity.compose.PredictiveBackHandler(enabled = selectedCategoryFilter != null && editingTransaction == null) { progressFlow ->
        try {
            progressFlow.collect { }
            selectedCategoryFilter = null
        } catch (e: kotlinx.coroutines.CancellationException) {
        }
    }


    AnimatedContent(
        targetState = state.isLoading,
        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
        label = "historySkeletonCrossfade"
    ) { isLoading ->
        if (isLoading) {
            com.example.vibefinance.ui.components.HistoryScreenSkeleton(modifier = modifier)
        } else {
            LazyColumn(
                modifier = modifier.fillMaxSize().testTag("HistoryList"),
                contentPadding = PaddingValues(top = topContentPadding),
                verticalArrangement = Arrangement.Top
            ) {
        // --- 📅 ACTIVE BUDGET PERIOD INDICATOR & FILTER CARD ---
        item {
            com.example.vibefinance.ui.components.BudgetPeriodIndicatorCard(
                budgetInfo = state.budgetInfo,
                selectedFilter = periodFilterMode,
                onSelectFilter = { mode -> periodFilterMode = mode },
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // --- 📊 CATEGORY BREAKDOWN & CSV EXPORT CARD ---
        item {
            val context = LocalContext.current
            com.example.vibefinance.ui.components.CategoryBreakdownCard(
                transactions = periodFilteredTransactions,
                onExportCsv = {
                    val csvContent = com.example.vibefinance.util.CsvExportEngine.generateCsvContent(
                        transactions = state.transactions,
                        accounts = state.accounts
                    )
                    com.example.vibefinance.util.CsvExportEngine.shareCsvFile(context, csvContent)
                },
                selectedCategory = selectedCategoryFilter,
                onSelectCategory = { cat -> selectedCategoryFilter = cat },
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }



        if (transactionsList.isEmpty()) {
            item {
                Spacer(modifier = Modifier.height(40.dp))
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 32.dp,
                    borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                ) {
                    Text(
                        text = stringResource(R.string.no_transactions),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            val groupedList = groupedTransactions.toList()
            groupedList.forEachIndexed { groupIndex, (localDate, txsForDate) ->
                // 1. Date Header Group (Muted, Clean Typography)
                val today = LocalDate.now()
                val isZh = Locale.getDefault().language.startsWith("zh")
                val datePattern = if (isZh) "yyyy年M月d日" else "MMM dd, yyyy"
                val headerText = when (localDate) {
                    today -> strToday
                    today.minusDays(1) -> strYesterday
                    else -> localDate.format(DateTimeFormatter.ofPattern(datePattern, Locale.getDefault()))
                }

                item {
                    val bInfo = state.budgetInfo
                    val sampleTxMilli = txsForDate.firstOrNull()?.timestamp ?: 0L
                    val isInActivePeriod = bInfo != null && bInfo.startDate > 0L && bInfo.endDate > 0L &&
                            sampleTxMilli >= bInfo.startDate && sampleTxMilli <= bInfo.endDate

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = headerText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                            fontWeight = FontWeight.Bold
                        )

                        if (bInfo != null && bInfo.startDate > 0L && bInfo.endDate > 0L) {
                            Surface(
                                shape = CircleShape,
                                color = if (isInActivePeriod) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = if (isInActivePeriod) stringResource(R.string.badge_active_period) else stringResource(R.string.badge_past_period),
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

                // 2. Transaction Item Flat list (M3 grouped list items)
                itemsIndexed(items = txsForDate, key = { _, tx -> tx.id }) { index, tx ->
                    val sourceAccount = state.accounts.find { it.id == tx.accountId }
                    val destAccount = tx.toAccountId?.let { toId -> state.accounts.find { it.id == toId } }
                    val isFuture = tx.timestamp > System.currentTimeMillis()
                    val isTransfer = tx.toAccountId != null
                    val isInstallment = tx.installmentNumber != null
                    val isIncome = tx.amount < 0

                    val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(tx.timestamp), ZoneId.systemDefault())
                    val timeText = date.format(DateTimeFormatter.ofPattern("HH:mm", Locale.US))

                    val transitionState = remember { MutableTransitionState(true) }

                    LaunchedEffect(transitionState.currentState, transitionState.targetState) {
                        if (!transitionState.currentState && !transitionState.targetState) {
                            onIntent(FinanceIntent.DeleteTransaction(tx))
                        }
                    }

                    val isFirst = index == 0
                    val isLast = index == txsForDate.size - 1

                    AnimatedVisibility(
                        visibleState = transitionState,
                        exit = shrinkVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) + fadeOut(animationSpec = tween(150)),
                        modifier = Modifier.animateItem(
                            placementSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            val itemShape = RoundedCornerShape(
                                topStart = if (isFirst) 26.dp else 8.dp,
                                topEnd = if (isFirst) 26.dp else 8.dp,
                                bottomStart = if (isLast) 26.dp else 8.dp,
                                bottomEnd = if (isLast) 26.dp else 8.dp
                            )
                            ExpressiveSwipeRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("TransactionRow_${tx.id}"),
                                shape = itemShape,
                                onEdit = {
                                    editingTransaction = tx
                                    editAmountText = String.format(Locale.US, "%.2f", Math.abs(tx.amount))
                                    editCategoryText = tx.category
                                    editDescriptionText = tx.description
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
                                        // 1. Leading Avatar Visual Container (M3 Expressive)
                                        Surface(
                                            modifier = Modifier.size(44.dp),
                                            shape = LocalIconShape.current,
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CategoryIcon(
                                                    category = tx.category,
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }

                                        // 2. Center Content: Headline (Title) & Supporting Metadata (Subtitles)
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            val titleText = if (tx.description.isNotBlank()) tx.description else tx.category
                                            Text(
                                                text = titleText,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            val cardName = sourceAccount?.name ?: "Cash"
                                            val categoryName = com.example.vibefinance.ui.home.getCategoryDisplayName(tx.category)
                                            val cardSubtitle = when {
                                                isTransfer -> "${sourceAccount?.name ?: "Account"} ➔ ${destAccount?.name ?: "Account"}"
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

                                            // Cashback Earned Pill (if applicable)
                                            val rate = com.example.vibefinance.data.InMemoryDatabase.getCashbackRule(tx.accountId, tx.category)
                                            if (rate > 0.0 && tx.amount > 0 && !isTransfer) {
                                                val cbAmount = tx.amount * (rate / 100.0)
                                                Box(
                                                    modifier = Modifier
                                                        .padding(top = 2.dp)
                                                        .clip(RoundedCornerShape(99.dp))
                                                        .background(Color(0xFF81C784).copy(alpha = 0.15f))
                                                        .border(1.dp, Color(0xFF81C784).copy(alpha = 0.3f), RoundedCornerShape(99.dp))
                                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = stringResource(R.string.cashback_pill_format, cbAmount, String.format(Locale.US, "%.1f", rate)),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color(0xFF2E7D32),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        // 3. Trailing Content: Metric (Amount) & Secondary Metadata (Timestamp/Delete)
                                        Column(
                                            horizontalAlignment = Alignment.End,
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            val amtString = String.format(Locale.US, if (isIncome) "+HK$%,.2f" else "-HK$%,.2f", Math.abs(tx.amount))
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
                                                color = amountColor
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
                                                        contentDescription = "Delete",
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

                // 3. Daily summary follows the signed amounts shown in the rows, including transfers.
                val dailyNet = txsForDate.sumOf { -it.amount }
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = stringResource(
                                R.string.daily_net_format,
                                if (dailyNet > 0.0) "+" else "",
                                kotlin.math.abs(dailyNet)
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = activeVibeColor.copy(alpha = 0.8f),
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }
                }

                // 4. Day Divider (if this is not the last group/day)
                if (groupIndex < groupedList.size - 1) {
                    item {
                        androidx.compose.material3.HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            thickness = 1.dp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
    }
    }

    // Real Transaction Edit Dialog with Container Transform Morphing
    editingTransaction?.let { tx ->
        val dialogScale by animateFloatAsState(
            targetValue = 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "dialogContainerTransform"
        )

        AlertDialog(
            onDismissRequest = { editingTransaction = null },
            title = {
                Text(
                    text = stringResource(R.string.edit_transaction_title),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = editAmountText,
                        onValueChange = { editAmountText = it },
                        label = { Text(stringResource(R.string.edit_amount_label) + " (HK$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editCategoryText,
                        onValueChange = { editCategoryText = it },
                        label = { Text(stringResource(R.string.edit_category_label)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editDescriptionText,
                        onValueChange = { editDescriptionText = it },
                        label = { Text(stringResource(R.string.edit_description_label)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = editAmountText.toDoubleOrNull()
                        if (amount != null) {
                            val originalIsIncome = tx.amount < 0
                            val targetSign = if (originalIsIncome) -1.0 else 1.0
                            val signedAmount = Math.abs(amount) * targetSign

                            val newTx = tx.copy(
                                amount = signedAmount,
                                category = editCategoryText,
                                description = editDescriptionText
                            )
                            onIntent(FinanceIntent.EditTransaction(tx, newTx))
                            editingTransaction = null
                        }
                    }
                ) {
                    Text(stringResource(R.string.btn_save), color = MaterialTheme.colorScheme.secondary)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingTransaction = null }) {
                    Text(stringResource(R.string.btn_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier
                .graphicsLayer {
                    scaleX = dialogScale
                    scaleY = dialogScale
                }
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(32.dp)
                )
        )
    }
}

private fun combineColors(color1: Color, color2: Color, weight: Float): Color {
    return Color(
        red = color1.red * weight + color2.red * (1f - weight),
        green = color1.green * weight + color2.green * (1f - weight),
        blue = color1.blue * weight + color2.blue * (1f - weight),
        alpha = color1.alpha * weight + color2.alpha * (1f - weight)
    )
}
