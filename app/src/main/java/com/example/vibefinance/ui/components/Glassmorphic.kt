package com.example.vibefinance.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import androidx.compose.ui.graphics.luminance

@Composable
fun ObsidianGradientBackground(
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val topBgColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isDark) Color(0xFF131314) else Color(0xFFF8F9FA),
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 600,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "bgTopColor"
    )

    val bottomBgColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isDark) Color(0xFF1E1F20) else Color(0xFFEEF2F6),
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 600,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "bgBottomColor"
    )

    val brush = remember(topBgColor, bottomBgColor) {
        Brush.verticalGradient(colors = listOf(topBgColor, bottomBgColor))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush)
    ) {
        content()
    }
}

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 32.dp,
    borderWidth: Dp = 1.dp,
    borderColor: Color = Color.White,
    containerColor: Color = Color.Unspecified,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val finalBorderColor = if (borderColor == Color.White && !isDark) {
        MaterialTheme.colorScheme.outlineVariant
    } else {
        borderColor
    }

    val resolvedContainerColor = if (containerColor != Color.Unspecified) {
        containerColor
    } else {
        if (isDark) Color.Transparent else MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius)),
        colors = CardDefaults.cardColors(
            containerColor = resolvedContainerColor
        ),
        border = BorderStroke(
            width = borderWidth,
            brush = Brush.linearGradient(
                colors = listOf(
                    finalBorderColor.copy(alpha = if (isDark) 0.25f else 0.5f),
                    finalBorderColor.copy(alpha = if (isDark) 0.10f else 0.2f)
                )
            )
        ),
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (isDark) {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.05f),
                                Color.White.copy(alpha = 0.02f)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent
                            )
                        )
                    }
                )
                .padding(20.dp)
        ) {
            content()
        }
    }
}
