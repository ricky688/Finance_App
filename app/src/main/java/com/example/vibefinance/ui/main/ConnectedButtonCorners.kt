package com.example.vibefinance.ui.main

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.example.vibefinance.theme.VibeMotion
import com.example.vibefinance.ui.preferences.LocalExperience
import com.example.vibefinance.ui.preferences.MotionLevel

/** Shape-only tokens: row dimensions and touch targets are independent of corner changes. */
@Immutable
data class ConnectedButtonCorners(
    val selected: Dp,
    val outer: Dp,
    val inner: Dp,
    val pressed: Dp
) {
    companion object {
        val Standard = ConnectedButtonCorners(24.dp, 24.dp, 8.dp, 6.dp)
        // Keep the pressed radius tighter than half a natural-height chip, so its outline still morphs.
        val Entry = ConnectedButtonCorners(32.dp, 32.dp, 8.dp, 8.dp)
    }
}

internal object ConnectedListCorners {
    val InnerRest = 8.dp
    val InnerPressed = 4.dp
}

@Immutable
internal data class ConnectedListCornerTarget(
    val topIsFullPill: Boolean,
    val bottomIsFullPill: Boolean,
    val innerDp: Dp,
)

internal fun connectedListRowCornerTarget(
    isFirst: Boolean,
    isLast: Boolean,
    isSelected: Boolean = false,
    isPressed: Boolean = false,
    innerRest: Dp = ConnectedListCorners.InnerRest,
    innerPressed: Dp = ConnectedListCorners.InnerPressed,
): ConnectedListCornerTarget {
    val topPill = isFirst || (isSelected && !isPressed)
    val bottomPill = isLast || (isSelected && !isPressed)
    val innerDp = if (isPressed) innerPressed else innerRest
    return ConnectedListCornerTarget(
        topIsFullPill = topPill,
        bottomIsFullPill = bottomPill,
        innerDp = innerDp,
    )
}

@Immutable
internal data class ConnectedListRowCornerSize(
    val pillFraction: Float,
    val innerDp: Dp,
) : CornerSize {
    override fun toPx(shapeSize: Size, density: Density): Float {
        val pillPx = minOf(shapeSize.width, shapeSize.height) * 0.5f
        val innerPx = with(density) { innerDp.toPx() }.coerceIn(0f, pillPx)
        return lerp(innerPx, pillPx, pillFraction.coerceIn(0f, 1f)).coerceIn(0f, pillPx)
    }
}

internal fun connectedListRowShape(
    topPillFraction: Float,
    bottomPillFraction: Float,
    innerDp: Dp,
): RoundedCornerShape {
    val topCorner = ConnectedListRowCornerSize(pillFraction = topPillFraction, innerDp = innerDp)
    val bottomCorner = ConnectedListRowCornerSize(pillFraction = bottomPillFraction, innerDp = innerDp)
    return RoundedCornerShape(
        topStart = topCorner,
        topEnd = topCorner,
        bottomEnd = bottomCorner,
        bottomStart = bottomCorner,
    )
}

/**
 * Animated list-row shape matching the page's Connected Button Group specification:
 * - Unselected resting: outer group ends (`isFirst` top / `isLast` bottom) = `50%` full pill,
 *   adjacent inner seam corners = `8.dp`.
 * - Pressed: inner seam corners (and all 4 corners of middle rows) tighten to `4.dp`, while
 *   group outer ends (`isFirst` top / `isLast` bottom) stay `50%` pill.
 * - Selected: all four corners expand to `50%` full pill (`CornerFull`).
 */
@Composable
fun rememberConnectedListRowShape(
    isFirst: Boolean,
    isLast: Boolean,
    isSelected: Boolean = false,
    isPressed: Boolean = false,
    innerRest: Dp = ConnectedListCorners.InnerRest,
    innerPressed: Dp = ConnectedListCorners.InnerPressed,
): RoundedCornerShape {
    val minimalMotion = LocalExperience.current.motion == MotionLevel.MINIMAL
    val target = connectedListRowCornerTarget(
        isFirst = isFirst,
        isLast = isLast,
        isSelected = isSelected,
        isPressed = isPressed,
        innerRest = innerRest,
        innerPressed = innerPressed,
    )
    val specFloat = if (minimalMotion) snap<Float>() else VibeMotion.fastSpatial()
    val specDp = if (minimalMotion) snap<Dp>() else VibeMotion.fastSpatial()
    val topPillFraction by animateFloatAsState(
        targetValue = if (target.topIsFullPill) 1f else 0f,
        animationSpec = specFloat,
        label = "ConnectedListRowTopPill"
    )
    val bottomPillFraction by animateFloatAsState(
        targetValue = if (target.bottomIsFullPill) 1f else 0f,
        animationSpec = specFloat,
        label = "ConnectedListRowBottomPill"
    )
    val innerDp by animateDpAsState(
        targetValue = target.innerDp,
        animationSpec = specDp,
        label = "ConnectedListRowInnerDp"
    )
    return remember(topPillFraction, bottomPillFraction, innerDp) {
        connectedListRowShape(
            topPillFraction = topPillFraction,
            bottomPillFraction = bottomPillFraction,
            innerDp = innerDp,
        )
    }
}
