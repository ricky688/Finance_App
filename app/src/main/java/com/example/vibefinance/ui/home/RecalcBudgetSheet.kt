package com.example.vibefinance.ui.home

import android.graphics.PointF
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.res.stringResource
import com.example.vibefinance.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import com.example.vibefinance.data.entity.RolloverMode
import com.example.vibefinance.data.repository.DailyBudgetInfo
import com.example.vibefinance.ui.common.bouncyClickable
import com.example.vibefinance.ui.components.ExpressiveSwitch
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

// Buckwheat Particle Physics Engine Data Structure
private data class RibbonParticle(
    var posX: Float,
    var posY: Float,
    var velX: Float,
    var velY: Float,
    val color: Color,
    val width: Float,
    val height: Float,
    val windage: Float,
    var angleZ: Float,
    var angleX: Float,
    val maxLifetimeMs: Float,
    var remainingLifetimeMs: Float,
    val shiftXCoeff: Float,
    var alpha: Float = 1f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecalcBudgetSheet(
    budgetInfo: DailyBudgetInfo,
    yesterdayLeftover: Double = 0.0,
    isMandatory: Boolean = true,
    isToday: Boolean = false,
    onSelectMode: (RolloverMode, Boolean) -> Unit,
    onDismiss: () -> Unit = {}
) {
    var rememberChoice by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    // Detect Light vs Dark Theme Mode (Matching Buckwheat Theme)
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f || isSystemInDarkTheme()

    // Buckwheat Palette colors
    val buckwheatColors = remember {
        listOf(
            Color(0xFFD14BE9), // Purple
            Color(0xFF5AC25E), // Green
            Color(0xFFF54580), // Pink
            Color(0xFFCCBE42), // Gold
            Color(0xFF3ABDF8), // Cyan
            Color(0xFFEB5F54)  // Coral
        )
    }

    // Particle state list for physics animation
    val particles = remember { mutableStateListOf<RibbonParticle>() }
    var animTick by remember { mutableLongStateOf(0L) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var hasLaunchedConfetti by remember { mutableStateOf(false) }

    // Launch one celebration after Canvas has a measured size, then stop requesting frames.
    LaunchedEffect(canvasSize.width > 0 && canvasSize.height > 0) {
        if (canvasSize.width <= 0 || canvasSize.height <= 0 || hasLaunchedConfetti) return@LaunchedEffect
        hasLaunchedConfetti = true
        delay(150)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

        val canvasWidth = canvasSize.width.toFloat()
        val canvasHeight = canvasSize.height.toFloat()
        repeat(80) {
            val angleRad = Math.toRadians((Random.nextFloat() * 45f + 35f).toDouble())
            val force = Random.nextFloat() * 14f + 7f
            particles.add(
                RibbonParticle(
                    posX = canvasWidth * 0.1f,
                    posY = canvasHeight * 0.5f,
                    velX = (cos(angleRad) * force).toFloat(),
                    velY = -(sin(angleRad) * force).toFloat(),
                    color = buckwheatColors.random(),
                    width = Random.nextFloat() * 12f + 8f,
                    height = Random.nextFloat() * 18f + 12f,
                    windage = Random.nextFloat() * 0.1f + 0.02f,
                    angleZ = Random.nextFloat() * 360f,
                    angleX = Random.nextFloat() * 360f,
                    maxLifetimeMs = 3500f,
                    remainingLifetimeMs = 3500f,
                    shiftXCoeff = (Random.nextFloat() - 0.5f) * 4f
                )
            )
        }
        repeat(80) {
            val angleRad = Math.toRadians((Random.nextFloat() * 45f + 100f).toDouble())
            val force = Random.nextFloat() * 14f + 7f
            particles.add(
                RibbonParticle(
                    posX = canvasWidth * 0.9f,
                    posY = canvasHeight * 0.5f,
                    velX = (cos(angleRad) * force).toFloat(),
                    velY = -(sin(angleRad) * force).toFloat(),
                    color = buckwheatColors.random(),
                    width = Random.nextFloat() * 12f + 8f,
                    height = Random.nextFloat() * 18f + 12f,
                    windage = Random.nextFloat() * 0.1f + 0.02f,
                    angleZ = Random.nextFloat() * 360f,
                    angleX = Random.nextFloat() * 360f,
                    maxLifetimeMs = 3500f,
                    remainingLifetimeMs = 3500f,
                    shiftXCoeff = (Random.nextFloat() - 0.5f) * 4f
                )
            )
        }

        var lastTime = System.nanoTime()
        while (particles.isNotEmpty()) {
            withFrameNanos { frameTime ->
                val dt = ((frameTime - lastTime) / 1_000_000f).coerceIn(1f, 32f)
                lastTime = frameTime
                animTick = frameTime

                val iterator = particles.iterator()
                while (iterator.hasNext()) {
                    val p = iterator.next()
                    p.remainingLifetimeMs -= dt

                    if (p.remainingLifetimeMs <= 0f) {
                        p.alpha -= dt * 0.002f
                        if (p.alpha <= 0f) {
                            iterator.remove()
                            continue
                        }
                    }

                    // Physics updates: Gravity 0.28f + Windage + Sinusoidal Wobble
                    p.velY += 0.28f * (dt / 16f)
                    p.velX *= (1f - p.windage * 0.02f)
                    p.posX += (p.velX + sin(p.remainingLifetimeMs * 0.008f) * p.shiftXCoeff) * (dt / 16f)
                    p.posY += p.velY * (dt / 16f)

                    p.angleZ += 6f * (dt / 16f)
                    p.angleX += 5f * (dt / 16f)
                }
            }
        }
    }

    // Calculate options matching official Buckwheat algorithm
    val daysLeft = budgetInfo.daysLeft.coerceAtLeast(1)
    // Period remaining before today (excluding today's expenses) for exact rollover preview math
    val todayExpenses = (budgetInfo.dailyAllowance - budgetInfo.dailyRemaining).coerceAtLeast(0.0)
    val periodRemainingBeforeToday = budgetInfo.monthlyRemaining + todayExpenses

    // Option 1: Split by remaining days including today (按剩餘天數分割) -> B_rem_before_today / daysLeft
    val dailyDistribute = if (daysLeft > 0) (periodRemainingBeforeToday / daysLeft).coerceAtLeast(0.0) else 0.0

    // Option 2: Add to today only (加至今天預算) -> Today gets (baseDaily + yesterdayLeftover), Next days get baseDaily
    val startLocal = java.time.Instant.ofEpochMilli(budgetInfo.startDate).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
    val endLocal = java.time.Instant.ofEpochMilli(budgetInfo.endDate).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
    val totalDaysInPeriod = (java.time.temporal.ChronoUnit.DAYS.between(startLocal, endLocal) + 1).coerceAtLeast(1).toDouble()
    val baseDaily = if (totalDaysInPeriod > 0) (budgetInfo.totalMonthlyBudget / totalDaysInPeriod).coerceAtLeast(0.0) else 0.0
    val dailyTodayAdd = (baseDaily + yesterdayLeftover.coerceAtLeast(0.0)).coerceAtLeast(0.0)
    val dailyNextDays = baseDaily

    // Color tokens adaptation for Light & Dark mode
    val sheetContainerColor = if (isDark) Color(0xFF16181D) else Color(0xFFFAFAFD)
    val cardContainerColor = if (isDark) Color(0xFF252830) else Color(0xFFF0F1F5)
    val cardBorder = if (isDark) BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)) else BorderStroke(1.dp, Color.Black.copy(alpha = 0.06f))
    val primaryTextColor = if (isDark) Color.White else Color(0xFF1A1C20)
    val secondaryTextColor = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF626673)

    // Intercept Back button press to prevent mandatory dialog dismissal without selection
    androidx.activity.compose.BackHandler(enabled = isMandatory) {
        // Do nothing on mandatory popup - forces user to pick Option 1 or Option 2
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { targetValue ->
            if (isMandatory) {
                targetValue != SheetValue.Hidden
            } else {
                true
            }
        }
    )

    ModalBottomSheet(
        onDismissRequest = {
            if (!isMandatory) {
                onDismiss()
            }
        },
        sheetState = sheetState,
        containerColor = sheetContainerColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            if (!isMandatory) {
                Surface(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 4.dp)
                        .width(36.dp)
                        .height(4.dp),
                    color = primaryTextColor.copy(alpha = 0.25f),
                    shape = CircleShape
                ) {}
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Close Button Row (Only visible if not mandatory)
                if (!isMandatory) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(primaryTextColor.copy(alpha = 0.08f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.btn_close),
                                tint = primaryTextColor.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Top Header Title (昨日結餘 / 昨日超支)
                val headerTitleText = if (yesterdayLeftover < 0) stringResource(R.string.loc_yesterday_overspent) else stringResource(R.string.loc_yesterday_leftover)

                Text(
                    text = headerTitleText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Animated Hero Number Counter with Spring Physics
                val animatedHeroAmount by animateFloatAsState(
                    targetValue = Math.abs(yesterdayLeftover).toFloat(),
                    animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "heroRecalcAmount"
                )

                // Hero Leftover Amount Display (e.g. HK$116)
                Text(
                    text = String.format(Locale.US, "HK$%,.0f", animatedHeroAmount),
                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 44.sp),
                    fontWeight = FontWeight.Black,
                    color = primaryTextColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Subtitle Link (stringResource(R.string.loc_budget_help))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .bouncyClickable(shape = RoundedCornerShape(12.dp)) { /* Info link */ }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = secondaryTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.loc_budget_help),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = secondaryTextColor
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // stringResource(R.string.loc_remember_choice) (Remember Choice) Card with Spring Physics
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = cardContainerColor,
                    border = cardBorder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .bouncyClickable(shape = RoundedCornerShape(18.dp)) { rememberChoice = !rememberChoice }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.loc_remember_choice),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = primaryTextColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.loc_change_rollover_later),
                                style = MaterialTheme.typography.labelMedium,
                                color = secondaryTextColor
                            )
                        }
                        ExpressiveSwitch(
                            checked = rememberChoice,
                            onCheckedChange = { rememberChoice = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Option 1 Card: "按剩餘天數分割" (Split by remaining days) with Spring Physics
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = cardContainerColor,
                    border = cardBorder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .bouncyClickable(shape = RoundedCornerShape(20.dp)) {
                            onSelectMode(RolloverMode.DISTRIBUTE_EVENLY, rememberChoice)
                            onDismiss()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.split_rest_days_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = primaryTextColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.split_rest_days_description, dailyDistribute),
                                style = MaterialTheme.typography.bodyMedium,
                                color = secondaryTextColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = secondaryTextColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Option 2 Card: "Leave for Today" (保留至今天) with Spring Physics
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = cardContainerColor,
                    border = cardBorder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .bouncyClickable(shape = RoundedCornerShape(20.dp)) {
                            onSelectMode(RolloverMode.ADD_TO_NEXT_DAY, rememberChoice)
                            onDismiss()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.add_current_day_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = primaryTextColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.add_current_day_description, dailyTodayAdd, dailyNextDays),
                                style = MaterialTheme.typography.bodyMedium,
                                color = secondaryTextColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = secondaryTextColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }

            // Confetti Cannon Canvas Overlay (Rendered ON TOP of all cards & content)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp)
                    .onSizeChanged { canvasSize = it }
            ) {
                animTick.let { }

                particles.forEach { p ->
                    rotate(degrees = p.angleZ, pivot = Offset(p.posX, p.posY)) {
                        val ribbonScaleY = abs(cos(Math.toRadians(p.angleX.toDouble()))).toFloat().coerceAtLeast(0.2f)
                        drawRoundRect(
                            color = p.color.copy(alpha = (p.alpha * 0.95f).coerceIn(0f, 1f)),
                            topLeft = Offset(p.posX - p.width / 2, p.posY - (p.height * ribbonScaleY) / 2),
                            size = Size(p.width, p.height * ribbonScaleY),
                            cornerRadius = CornerRadius(3f, 3f)
                        )
                    }
                }
            }
        }
    }
}
