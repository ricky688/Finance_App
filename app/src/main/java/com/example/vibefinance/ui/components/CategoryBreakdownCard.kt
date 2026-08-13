package com.example.vibefinance.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vibefinance.data.entity.TransactionEntity
import java.util.Locale

@Composable
fun CategoryBreakdownCard(
    transactions: List<TransactionEntity>,
    onExportCsv: () -> Unit,
    modifier: Modifier = Modifier,
    selectedCategory: String? = null,
    onSelectCategory: ((String?) -> Unit)? = null
) {
    val totalExpense = transactions.filter { it.toAccountId == null && it.amount > 0 }.sumOf { it.amount }
    
    // Group transactions by category
    val categoryTotals = transactions
        .filter { it.toAccountId == null && it.amount > 0 }
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .entries
        .sortedByDescending { it.value }

    val categoryColors = listOf(
        Color(0xFF4CAF50), // Emerald Green
        Color(0xFF2196F3), // Ocean Blue
        Color(0xFFFF9800), // Amber Sunset
        Color(0xFF9C27B0), // Royal Purple
        Color(0xFFE91E63), // Rose Pink
        Color(0xFF00BCD4)  // Cyan
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Category Analytics (分類分析)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            AnimatedVisibility(
                                visible = selectedCategory != null,
                                enter = fadeIn(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)) +
                                        scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)),
                                exit = fadeOut(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)) +
                                       scaleOut(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy))
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Focused",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clickable { onSelectCategory?.invoke(null) }
                                        )
                                    }
                                }
                            }
                        }
                        Text(
                            text = if (selectedCategory != null) "Tap category to clear focus filter" else "Tap any category below to filter transactions",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (selectedCategory != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onExportCsv,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Export CSV",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CSV",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (categoryTotals.isEmpty() || totalExpense == 0.0) {
                Text(
                    text = "No expenses logged yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                // Stacked Multi-Segment Progress Bar with Separated Pills & Spring Height Expansion
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    categoryTotals.forEachIndexed { index, entry ->
                        val targetFraction = (entry.value / totalExpense).toFloat().coerceIn(0.01f, 1f)
                        val animatedFraction by animateFloatAsState(
                            targetValue = targetFraction,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "segment_spring"
                        )
                        val color = categoryColors[index % categoryColors.size]
                        val isSegmentSelected = selectedCategory != null && entry.key.equals(selectedCategory, ignoreCase = true)
                        val isSegmentDimmed = selectedCategory != null && !isSegmentSelected

                        // Dynamic spring height expansion when selected (like Daily Spending Chart)
                        val segmentHeight by animateDpAsState(
                            targetValue = when {
                                isSegmentSelected -> 24.dp // Significantly bigger height on selection!
                                isSegmentDimmed -> 10.dp    // Slightly smaller when dimmed
                                else -> 14.dp               // Standard default height
                            },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "segment_height_spring"
                        )

                        val segmentAlpha by animateFloatAsState(
                            targetValue = if (isSegmentDimmed) 0.35f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "segment_alpha_spring"
                        )

                        val segmentScale by animateFloatAsState(
                            targetValue = if (isSegmentSelected) 1.05f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "segment_scale_spring"
                        )

                        Box(
                            modifier = Modifier
                                .weight(animatedFraction)
                                .height(segmentHeight)
                                .graphicsLayer {
                                    scaleX = segmentScale
                                    scaleY = segmentScale
                                }
                                .clip(CircleShape) // Individually rounded pill for each bar segment!
                                .background(color.copy(alpha = segmentAlpha))
                                .clickable {
                                    onSelectCategory?.invoke(if (isSegmentSelected) null else entry.key)
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Detailed Interactive Legend Grid with Spring Highlight Physics
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    categoryTotals.take(5).forEachIndexed { index, entry ->
                        val color = categoryColors[index % categoryColors.size]
                        val percentage = if (totalExpense > 0) (entry.value / totalExpense * 100) else 0.0
                        val isSelected = selectedCategory != null && entry.key.equals(selectedCategory, ignoreCase = true)
                        val isDimmed = selectedCategory != null && !isSelected

                        val rowBgColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            else Color.Transparent,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "row_bg_spring"
                        )

                        val rowAlpha by animateFloatAsState(
                            targetValue = if (isDimmed) 0.35f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "row_alpha_spring"
                        )

                        val dotSize by animateDpAsState(
                            targetValue = if (isSelected) 14.dp else 10.dp,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "dot_size_spring"
                        )

                        val horizontalPadding by animateDpAsState(
                            targetValue = if (isSelected) 12.dp else 8.dp,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "row_h_padding_spring"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(rowBgColor)
                                .clickable {
                                    onSelectCategory?.invoke(if (isSelected) null else entry.key)
                                }
                                .padding(horizontal = horizontalPadding, vertical = 6.dp)
                                .alpha(rowAlpha),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(dotSize)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = entry.key,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = String.format(Locale.US, "%.1f%%", percentage),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = String.format(Locale.US, "HK$ %,.0f", entry.value),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
