package com.example.vibefinance.ui.home

import com.example.vibefinance.R
import androidx.compose.ui.res.stringResource
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.ai.ForecastStatus
import com.example.vibefinance.ai.VibeForecastEngine
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.data.repository.DailyBudgetInfo
import com.example.vibefinance.ui.common.bouncyClickable
import java.util.Locale

@Composable
fun VibeForecastCard(
    budgetInfo: DailyBudgetInfo,
    transactions: List<TransactionEntity>,
    modifier: Modifier = Modifier
) {
    val forecast = remember(budgetInfo, transactions) {
        VibeForecastEngine.calculateForecast(budgetInfo, transactions)
    }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val accentColor = when (forecast.status) {
        ForecastStatus.EXCELLENT_SAVINGS -> Color(0xFF00E676) // Emerald Green
        ForecastStatus.STEADY_PACE -> Color(0xFF29B6F6)       // Cyan Blue
        ForecastStatus.OVERSPEND_RISK -> Color(0xFFFF5252)    // Crimson Red
    }

    val animatedAccent by animateColorAsState(
        targetValue = accentColor,
        animationSpec = tween(durationMillis = 500),
        label = "forecastAccent"
    )

    val cardBg = if (isDark) Color(0xFF1E222B) else Color(0xFFF4F6FB)
    val cardBorder = if (isDark) BorderStroke(1.dp, animatedAccent.copy(alpha = 0.3f)) else BorderStroke(1.dp, animatedAccent.copy(alpha = 0.25f))

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .bouncyClickable(shape = RoundedCornerShape(24.dp)) { /* Info tap */ }
            .border(cardBorder.width, cardBorder.brush, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = cardBg
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Ambient subtle glow gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                animatedAccent.copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            radius = 400f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(animatedAccent.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = animatedAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.loc_forecast),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    // Velocity badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = animatedAccent.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = stringResource(R.string.loc_daily_velocity, forecast.actualDailyVelocity),
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.5.sp),
                            fontWeight = FontWeight.Bold,
                            color = animatedAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Forecast Main Display
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = if (forecast.projectedEndBalance >= 0) stringResource(R.string.loc_projected_savings) else stringResource(R.string.loc_overspend_risk),
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = String.format(Locale.US, "HK$ %,.0f", Math.abs(forecast.projectedEndBalance)),
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
                            fontWeight = FontWeight.Black,
                            color = animatedAccent
                        )
                    }

                    // Trend Indicator Icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (forecast.projectedEndBalance >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = animatedAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = if (forecast.status == ForecastStatus.EXCELLENT_SAVINGS) stringResource(R.string.loc_excellent_savings) else if (forecast.status == ForecastStatus.STEADY_PACE) stringResource(R.string.loc_steady_pace) else stringResource(R.string.loc_overspend_warning),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = animatedAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Detailed message banner
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                ) {
                    Text(
                        text = forecast.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}
