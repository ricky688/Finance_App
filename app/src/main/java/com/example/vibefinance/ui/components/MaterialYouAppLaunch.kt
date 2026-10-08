package com.example.vibefinance.ui.components

import com.example.vibefinance.R
import androidx.compose.ui.res.stringResource
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Material You App Launch Animation Overlay.
 *
 * Implements the Material 3 Expressive Container Transform Launch animation pattern:
 * 1. Shows a centered Material You splash badge with spring pulse during initial boot.
 * 2. Morph-scales outward from the center node to reveal the main screen content with spring physics.
 */
@Composable
fun MaterialYouAppLaunchOverlay(
    modifier: Modifier = Modifier,
    splashDurationMillis: Long = 900L,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    // Remember the launch decision once; enabling in Settings must not replay the intro.
    var isLaunching by remember { mutableStateOf(enabled) }
    val showLaunch = enabled && isLaunching

    LaunchedEffect(Unit) {
        if (isLaunching) {
            delay(splashDurationMillis)
            isLaunching = false
        }
    }

    val contentScale by animateFloatAsState(
        targetValue = if (showLaunch) 0.88f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "contentLaunchScale"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (showLaunch) 0.0f else 1.0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "contentLaunchAlpha"
    )

    val cornerRadius by animateFloatAsState(
        targetValue = if (showLaunch) 40f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "contentLaunchCorner"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Main Screen Content with Material You Entrance Scale Morph
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = contentScale
                    scaleY = contentScale
                    alpha = contentAlpha
                    clip = cornerRadius > 0f
                    shape = RoundedCornerShape(cornerRadius.dp)
                }
        ) {
            content()
        }

        // Material You Brand Splash Container Overlay
        AnimatedVisibility(
            visible = showLaunch,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(350)) + scaleOut(
                targetScale = 1.4f,
                transformOrigin = TransformOrigin.Center,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("MaterialYouLaunchAnimation")
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                // Expressive Material 3 Brand Launch Capsule
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 12.dp,
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .padding(24.dp)
                        .graphicsLayer {
                            val pulseScale = if (isLaunching) 1.0f else 1.15f
                            scaleX = pulseScale
                            scaleY = pulseScale
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Savings,
                                    contentDescription = stringResource(R.string.loc_app_icon),
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(18.dp))

                        Column {
                            Text(
                                text = "Vibe Finance",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = stringResource(R.string.loc_material_edition),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
