package com.example.vibefinance.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.R
import com.example.vibefinance.data.repository.CategoryBudgetProgress
import com.example.vibefinance.data.repository.DailyBudgetInfo
import com.example.vibefinance.ui.common.expressiveCardClickable
import com.example.vibefinance.ui.common.rememberExpressiveCardPress
import com.example.vibefinance.ui.components.label
import com.example.vibefinance.ui.components.vector
import java.util.Locale

@Composable
fun CategoryBudgetsCard(
    budgetInfo: DailyBudgetInfo,
    onOpenBudgetDialog: () -> Unit,
    onSaveCategoryBudgets: (Map<String, Double>) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showEditSheet by rememberSaveable { mutableStateOf(false) }

    val handleOpenSheet = {
        if (budgetInfo.totalMonthlyBudget > 0.0) {
            showEditSheet = true
        } else {
            onOpenBudgetDialog()
        }
    }

    val pressState = rememberExpressiveCardPress(
        restingCorner = 28.dp,
        pressedCorner = 16.dp
    )

    val categoryBudgets = budgetInfo.categoryBudgets
    val totalPeriodBudget = budgetInfo.totalMonthlyBudget
    val totalAllocated = categoryBudgets.sumOf { it.budgetAmount }
    val unallocated = (totalPeriodBudget - totalAllocated).coerceAtLeast(0.0)

    Surface(
        shape = pressState.shape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("CategoryBudgetsCard")
            .expressiveCardClickable(
                pressState = pressState,
                rippleColor = MaterialTheme.colorScheme.primary,
                onClick = handleOpenSheet
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PieChart,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = stringResource(R.string.loc_category_budgets_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (categoryBudgets.isNotEmpty() && totalPeriodBudget > 0.0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(
                                R.string.loc_category_budgets_allocation_summary,
                                String.format(Locale.US, "HK$ %,.0f", totalAllocated),
                                String.format(Locale.US, "HK$ %,.0f", totalPeriodBudget),
                                String.format(Locale.US, "HK$ %,.0f", unallocated)
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                // Edit Button
                IconButton(
                    onClick = handleOpenSheet,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (categoryBudgets.isEmpty()) Icons.Default.Add else Icons.Default.Edit,
                        contentDescription = stringResource(R.string.btn_change),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Body
            if (categoryBudgets.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.loc_category_budgets_empty_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.loc_category_budgets_empty_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                            )
                        }
                        Button(
                            onClick = handleOpenSheet,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.loc_category_budgets_setup),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    categoryBudgets.forEach { progress ->
                        CategoryBudgetRow(progress = progress)
                    }
                }
            }
        }
    }

    if (showEditSheet) {
        EditCategoryBudgetsSheet(
            budgetInfo = budgetInfo,
            onDismiss = { showEditSheet = false },
            onSave = { newMap ->
                onSaveCategoryBudgets(newMap)
                showEditSheet = false
            }
        )
    }
}

@Composable
private fun CategoryBudgetRow(
    progress: CategoryBudgetProgress,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.progressRatio.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "categoryBudgetProgress_${progress.symbol.name}"
    )

    val progressColor = when {
        progress.isOverspent -> MaterialTheme.colorScheme.error
        progress.progressRatio >= 0.8f -> Color(0xFFF59E0B) // Amber
        else -> MaterialTheme.colorScheme.primary
    }

    val iconContainerBg = when {
        progress.isOverspent -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
        progress.progressRatio >= 0.8f -> Color(0xFFFEF3C7) // Light amber
        else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    }

    val iconTint = when {
        progress.isOverspent -> MaterialTheme.colorScheme.error
        progress.progressRatio >= 0.8f -> Color(0xFFB45309) // Deep amber
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Built-in Symbol Icon in Rounded Squircle Container
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconContainerBg,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = progress.symbol.vector(),
                        contentDescription = stringResource(progress.symbol.label()),
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Center details & Progress Bar
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(progress.symbol.label()),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Remaining / Overspent status
                    if (progress.isOverspent) {
                        Text(
                            text = stringResource(
                                R.string.loc_category_budgets_overspent,
                                String.format(Locale.US, "HK$ %,.2f", -progress.remainingAmount)
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text(
                            text = stringResource(
                                R.string.loc_category_budgets_remaining,
                                String.format(Locale.US, "HK$ %,.2f", progress.remainingAmount)
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (progress.progressRatio >= 0.8f) Color(0xFFD97706) else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Smooth Linear Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(progressColor)
                    )
                }

                // Spent / Budget amount subtitle
                Text(
                    text = String.format(
                        Locale.US,
                        "%s HK$ %,.0f / HK$ %,.0f (%.0f%%)",
                        stringResource(R.string.loc_category_budgets_spent_label),
                        progress.spentAmount,
                        progress.budgetAmount,
                        progress.progressRatio * 100f
                    ),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
