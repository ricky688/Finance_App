package com.example.vibefinance.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modifier that applies a smooth, subtle pulsing skeleton shimmer animation
 * to placeholder UI elements while content is loading.
 */
fun Modifier.pulsingSkeleton(
    shape: Shape = RoundedCornerShape(16.dp),
    durationMillis: Int = 1000
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "skeletonPulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.38f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonAlpha"
    )

    val baseColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)
    val pulseColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    val animatedColor = lerp(baseColor, pulseColor, alpha)

    this
        .clip(shape)
        .background(animatedColor)
}

/**
 * Reusable Pulsing Skeleton Placeholder Box
 */
@Composable
fun PulsingSkeletonBox(
    modifier: Modifier = Modifier,
    height: Dp = 24.dp,
    width: Dp? = null,
    shape: Shape = RoundedCornerShape(12.dp)
) {
    val baseModifier = if (width != null) modifier.width(width).height(height) else modifier.fillMaxWidth().height(height)
    Box(modifier = baseModifier.pulsingSkeleton(shape = shape))
}

/**
 * Skeleton Loader for the Home / Daily Budget Screen
 */
@Composable
fun HomeScreenSkeleton(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = false
    ) {
        // Hero Budget Card Skeleton
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .pulsingSkeleton(shape = RoundedCornerShape(28.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PulsingSkeletonBox(width = 90.dp, height = 28.dp, shape = RoundedCornerShape(14.dp))
                        PulsingSkeletonBox(width = 80.dp, height = 24.dp, shape = RoundedCornerShape(12.dp))
                    }
                    PulsingSkeletonBox(width = 160.dp, height = 48.dp, shape = RoundedCornerShape(12.dp))
                    PulsingSkeletonBox(height = 42.dp, shape = RoundedCornerShape(20.dp))
                }
            }
        }

        // Whole Budget Card Skeleton
        item {
            PulsingSkeletonBox(height = 110.dp, shape = RoundedCornerShape(24.dp))
        }

        // Bento Matrix Skeleton
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PulsingSkeletonBox(modifier = Modifier.weight(1.2f), height = 160.dp, shape = RoundedCornerShape(24.dp))
                    PulsingSkeletonBox(modifier = Modifier.weight(0.8f), height = 160.dp, shape = RoundedCornerShape(24.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PulsingSkeletonBox(modifier = Modifier.weight(1f), height = 128.dp, shape = RoundedCornerShape(24.dp))
                    PulsingSkeletonBox(modifier = Modifier.weight(1f), height = 128.dp, shape = RoundedCornerShape(24.dp))
                }
            }
        }

        // Total Expenses Row Skeleton
        item {
            PulsingSkeletonBox(height = 84.dp, shape = RoundedCornerShape(24.dp))
        }

        // Chart Skeleton
        item {
            PulsingSkeletonBox(height = 200.dp, shape = RoundedCornerShape(24.dp))
        }
    }
}

/**
 * Skeleton Loader for the Accounts / Assets Screen
 */
@Composable
fun AccountsScreenSkeleton(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = false
    ) {
        // Net Asset Value Header Skeleton
        item {
            PulsingSkeletonBox(height = 160.dp, shape = RoundedCornerShape(24.dp))
        }

        // Subtitle & Action Skeleton
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PulsingSkeletonBox(width = 160.dp, height = 28.dp, shape = RoundedCornerShape(8.dp))
                PulsingSkeletonBox(width = 100.dp, height = 36.dp, shape = RoundedCornerShape(12.dp))
            }
        }

        // Account List Item Skeletons
        items(4) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .pulsingSkeleton(shape = RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.size(44.dp).pulsingSkeleton(shape = CircleShape))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        PulsingSkeletonBox(width = 120.dp, height = 18.dp, shape = RoundedCornerShape(6.dp))
                        PulsingSkeletonBox(width = 80.dp, height = 14.dp, shape = RoundedCornerShape(6.dp))
                    }
                }
                PulsingSkeletonBox(width = 70.dp, height = 22.dp, shape = RoundedCornerShape(6.dp))
            }
        }
    }
}

/**
 * Skeleton Loader for the History Screen
 */
@Composable
fun HistoryScreenSkeleton(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        userScrollEnabled = false
    ) {
        // Search Bar Skeleton
        item {
            PulsingSkeletonBox(height = 56.dp, shape = RoundedCornerShape(28.dp))
        }

        // Filter Chips Row Skeleton
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(4) {
                    PulsingSkeletonBox(width = 76.dp, height = 34.dp, shape = RoundedCornerShape(17.dp))
                }
            }
        }

        // Transaction Group Skeleton Header
        item {
            PulsingSkeletonBox(width = 110.dp, height = 20.dp, shape = RoundedCornerShape(6.dp))
        }

        // Transaction Item Skeletons
        items(6) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .pulsingSkeleton(shape = RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.size(40.dp).pulsingSkeleton(shape = CircleShape))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        PulsingSkeletonBox(width = 100.dp, height = 16.dp, shape = RoundedCornerShape(6.dp))
                        PulsingSkeletonBox(width = 60.dp, height = 12.dp, shape = RoundedCornerShape(6.dp))
                    }
                }
                PulsingSkeletonBox(width = 64.dp, height = 20.dp, shape = RoundedCornerShape(6.dp))
            }
        }
    }
}
