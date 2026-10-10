@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

/** Native sheets use DefaultSpatial for show/settling, FastEffects for hide,
 * and DefaultEffects for the scrim. Keep their timing and easing coordinated.
 * Tween still respects the coroutine's Android/app MotionDurationScale.
 */
private class SheetMotionScheme(private val parent: MotionScheme) : MotionScheme by parent {
    override fun <T> defaultSpatialSpec(): FiniteAnimationSpec<T> =
        tween(durationMillis = 320, easing = FastOutSlowInEasing)

    override fun <T> defaultEffectsSpec(): FiniteAnimationSpec<T> =
        tween(durationMillis = 320, easing = FastOutSlowInEasing)

    override fun <T> fastEffectsSpec(): FiniteAnimationSpec<T> =
        tween(durationMillis = 320, easing = FastOutSlowInEasing)
}

/** Keep native gestures, insets, accessibility and predictive Back; scope easing to the sheet. */
@Composable
internal fun AppModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    sheetMaxWidth: Dp = BottomSheetDefaults.SheetMaxWidth,
    sheetGesturesEnabled: Boolean = true,
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    containerColor: Color = BottomSheetDefaults.ContainerColor,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = BottomSheetDefaults.Elevation,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    dragHandle: (@Composable () -> Unit)? = { BottomSheetDefaults.DragHandle() },
    contentWindowInsets: @Composable () -> WindowInsets = { BottomSheetDefaults.windowInsets },
    properties: ModalBottomSheetProperties = ModalBottomSheetProperties(),
    content: @Composable ColumnScope.() -> Unit
) {
    val parentMotion = MaterialTheme.motionScheme
    val sheetMotion = remember(parentMotion) { SheetMotionScheme(parentMotion) }
    MaterialTheme(motionScheme = sheetMotion) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest, modifier = modifier, sheetState = sheetState,
            sheetMaxWidth = sheetMaxWidth, sheetGesturesEnabled = sheetGesturesEnabled,
            shape = shape, containerColor = containerColor, contentColor = contentColor,
            tonalElevation = tonalElevation, scrimColor = scrimColor, dragHandle = dragHandle,
            contentWindowInsets = contentWindowInsets, properties = properties
        ) {
            val columnScope = this
            // Buttons, switches and nested content retain the app's own motion scheme.
            MaterialTheme(motionScheme = parentMotion) { content(columnScope) }
        }
    }
}
