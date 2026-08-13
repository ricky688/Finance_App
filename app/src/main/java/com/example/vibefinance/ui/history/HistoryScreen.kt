package com.example.vibefinance.ui.history

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
import androidx.compose.animation.core.CubicBezierEasing
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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.example.vibefinance.ui.components.SwipeActions
import com.example.vibefinance.ui.components.SwipeActionsConfig
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
import androidx.compose.ui.unit.sp
import com.example.vibefinance.data.entity.TransactionEntity
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
    modifier: Modifier = Modifier
) {
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var periodFilterMode by remember { mutableStateOf(com.example.vibefinance.ui.components.PeriodFilterMode.ALL) }

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

    var activeSwipeId by remember { mutableStateOf<Long?>(null) }
    var activeSwipeOffset by remember { mutableStateOf(0f) }


    AnimatedContent(
        targetState = state.isLoading,
        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
        label = "historySkeletonCrossfade"
    ) { isLoading ->
        if (isLoading) {
            com.example.vibefinance.ui.components.HistoryScreenSkeleton(modifier = modifier)
        } else {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top
            ) {
        item {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(80.dp)
            )
        }

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
                        text = "No transactions found.",
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
                val headerText = when (localDate) {
                    today -> "Today"
                    today.minusDays(1) -> "Yesterday"
                    else -> localDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.US))
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
                            text = headerText.lowercase(Locale.US),
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
                                    text = if (isInActivePeriod) "Active Period" else "Past / Other Period",
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
                            animationSpec = tween(
                                durationMillis = 400,
                                easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
                            )
                        ) + fadeOut(animationSpec = tween(200)),
                        modifier = Modifier.animateItem()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            SwipeActions(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("TransactionRow_${tx.id}"),
                                startActionsConfig = SwipeActionsConfig(
                                    threshold = 0.4f,
                                    background = MaterialTheme.colorScheme.tertiaryContainer,
                                    backgroundActive = MaterialTheme.colorScheme.tertiary,
                                    iconTint = MaterialTheme.colorScheme.onTertiary,
                                    icon = rememberVectorPainter(Icons.Default.Edit),
                                    stayDismissed = false,
                                    onDismiss = {
                                        editingTransaction = tx
                                        editAmountText = String.format(Locale.US, "%.2f", Math.abs(tx.amount))
                                        editCategoryText = tx.category
                                        editDescriptionText = tx.description
                                    }
                                ),
                                endActionsConfig = SwipeActionsConfig(
                                    threshold = 0.4f,
                                    background = MaterialTheme.colorScheme.errorContainer,
                                    backgroundActive = MaterialTheme.colorScheme.error,
                                    iconTint = MaterialTheme.colorScheme.onError,
                                    icon = rememberVectorPainter(Icons.Default.Delete),
                                    stayDismissed = true,
                                    onDismiss = {
                                        transitionState.targetState = false
                                    }
                                )
                            ) { dismissState ->
                                val offsetVal = runCatching { dismissState.requireOffset() }.getOrDefault(0f)

                                LaunchedEffect(offsetVal) {
                                    if (abs(offsetVal) > 2f) {
                                        activeSwipeId = tx.id
                                        activeSwipeOffset = offsetVal
                                    } else if (activeSwipeId == tx.id) {
                                        activeSwipeId = null
                                        activeSwipeOffset = 0f
                                    }
                                }

                                val isSelfSwiping = activeSwipeId == tx.id
                                val activeIndex = if (activeSwipeId != null) txsForDate.indexOfFirst { it.id == activeSwipeId } else -1
                                val distFromActive = if (activeIndex != -1 && !isSelfSwiping) abs(index - activeIndex) else 0

                                val neighborDragOffset = if (!isSelfSwiping && activeIndex != -1) {
                                    when (distFromActive) {
                                        1 -> activeSwipeOffset * 0.15f
                                        2 -> activeSwipeOffset * 0.05f
                                        else -> 0f
                                    }
                                } else 0f

                                // Android 16 Corner Morphing Physics: strictly applies to the swiped cell AND its immediate top/bottom neighbor cells (distFromActive == 1)
                                val shouldMorphCorners = isSelfSwiping || distFromActive == 1
                                val effectiveOffsetForMorphing = if (isSelfSwiping) offsetVal else if (distFromActive == 1) activeSwipeOffset else 0f
                                val swipeProgress = if (shouldMorphCorners) (abs(effectiveOffsetForMorphing) / 250f).coerceIn(0f, 1f) else 0f

                                val baseTopStart = if (isFirst) 20.dp else 4.dp
                                val baseBottomStart = if (isLast) 20.dp else 4.dp
                                val baseTopEnd = if (isFirst) 20.dp else 4.dp
                                val baseBottomEnd = if (isLast) 20.dp else 4.dp

                                val morphedTopStart = if (shouldMorphCorners && effectiveOffsetForMorphing > 0) baseTopStart + (28.dp - baseTopStart) * swipeProgress else baseTopStart
                                val morphedBottomStart = if (shouldMorphCorners && effectiveOffsetForMorphing > 0) baseBottomStart + (28.dp - baseBottomStart) * swipeProgress else baseBottomStart
                                val morphedTopEnd = if (shouldMorphCorners && effectiveOffsetForMorphing < 0) baseTopEnd + (28.dp - baseTopEnd) * swipeProgress else baseTopEnd
                                val morphedBottomEnd = if (shouldMorphCorners && effectiveOffsetForMorphing < 0) baseBottomEnd + (28.dp - baseBottomEnd) * swipeProgress else baseBottomEnd

                                val dynamicItemShape = RoundedCornerShape(
                                    topStart = morphedTopStart,
                                    topEnd = morphedTopEnd,
                                    bottomStart = morphedBottomStart,
                                    bottomEnd = morphedBottomEnd
                                )



                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            translationX = neighborDragOffset
                                        }
                                        .clickable {
                                            editingTransaction = tx
                                            editAmountText = String.format(Locale.US, "%.2f", Math.abs(tx.amount))
                                            editCategoryText = tx.category
                                            editDescriptionText = tx.description
                                        },
                                    shape = dynamicItemShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
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
                                            shape = RoundedCornerShape(14.dp),
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
                                            val categoryName = tx.category
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
                                                        text = String.format(Locale.US, "+$%.2f cashback (%s%%)", cbAmount, String.format(Locale.US, "%.1f", rate)),
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
                                            val amtString = String.format(Locale.US, if (isIncome) "+$%.2f" else "-$%.2f", Math.abs(tx.amount))
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
                            }
                            if (!isLast) {
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                        }
                    }
                }

                // 3. Daily Summary Row (Right-Aligned dynamic total spending)
                val dailyTotal = txsForDate.filter { it.amount > 0 && it.toAccountId == null }.sumOf { it.amount }
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = String.format("Daily Total: $%.2f", dailyTotal),
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
            Spacer(modifier = Modifier.height(80.dp))
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
                    text = "Edit Transaction",
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
                        label = { Text("Amount ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editCategoryText,
                        onValueChange = { editCategoryText = it },
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editDescriptionText,
                        onValueChange = { editDescriptionText = it },
                        label = { Text("Description") },
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
                    Text("Save", color = MaterialTheme.colorScheme.secondary)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingTransaction = null }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
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

