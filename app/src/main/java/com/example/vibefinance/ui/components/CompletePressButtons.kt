@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.vibefinance.ui.common.expressiveHoverLayer

/**
 * Global Material 3 Expressive Button Defaults and CompositionLocal.
 * Default resting shape: CircleShape (pill / stadium)
 * Default pressed shape: RoundedCornerShape(percent = 32)
 */
object AppButtonDefaults {
    val RestingShape: Shape = CircleShape
    val PressedShape: Shape = RoundedCornerShape(percent = 32)

    @Composable
    fun shapes(
        shape: Shape = RestingShape,
        pressedShape: Shape = PressedShape
    ): ButtonShapes = ButtonDefaults.shapes(shape = shape, pressedShape = pressedShape)
}

val LocalAppButtonShapes = staticCompositionLocalOf<ButtonShapes?> { null }

@Composable
internal fun defaultButtonShapes(): ButtonShapes {
    return LocalAppButtonShapes.current ?: AppButtonDefaults.shapes()
}

/** Material buttons retain their actual interaction source; only shape timing is coordinated. */
@Composable
internal fun CompletePressButton(
    onClick: () -> Unit,
    shapes: ButtonShapes = defaultButtonShapes(),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val progress by rememberCompletePressProgress(source, enabled)
    val buttonShape = completePressShape(shapes.shape, shapes.pressedShape, progress)
    Button(
        onClick = onClick,
        modifier = modifier.expressiveHoverLayer(
            interactionSource = source,
            enabled = enabled,
            shape = buttonShape,
            liftDp = 1.dp,
            highlightRim = false
        ),
        enabled = enabled,
        shape = buttonShape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = source,
        content = content
    )
}

@Composable
internal fun CompletePressFilledTonalButton(
    onClick: () -> Unit,
    shapes: ButtonShapes = defaultButtonShapes(),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.filledTonalButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.filledTonalButtonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val progress by rememberCompletePressProgress(source, enabled)
    val buttonShape = completePressShape(shapes.shape, shapes.pressedShape, progress)
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.expressiveHoverLayer(
            interactionSource = source,
            enabled = enabled,
            shape = buttonShape,
            liftDp = 1.dp,
            highlightRim = false
        ),
        enabled = enabled,
        shape = buttonShape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = source,
        content = content
    )
}

@Composable
internal fun CompletePressOutlinedButton(
    onClick: () -> Unit,
    shapes: ButtonShapes = defaultButtonShapes(),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = ButtonDefaults.outlinedButtonBorder(enabled),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val progress by rememberCompletePressProgress(source, enabled)
    val buttonShape = completePressShape(shapes.shape, shapes.pressedShape, progress)
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.expressiveHoverLayer(
            interactionSource = source,
            enabled = enabled,
            shape = buttonShape,
            liftDp = 1.dp,
            highlightRim = false
        ),
        enabled = enabled,
        shape = buttonShape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = source,
        content = content
    )
}

@Composable
internal fun CompletePressTextButton(
    onClick: () -> Unit,
    shapes: ButtonShapes = defaultButtonShapes(),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val progress by rememberCompletePressProgress(source, enabled)
    val buttonShape = completePressShape(shapes.shape, shapes.pressedShape, progress)
    TextButton(
        onClick = onClick,
        modifier = modifier.expressiveHoverLayer(
            interactionSource = source,
            enabled = enabled,
            shape = buttonShape,
            liftDp = 1.dp,
            highlightRim = false
        ),
        enabled = enabled,
        shape = buttonShape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = source,
        content = content
    )
}

@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) = CompletePressButton(
    onClick = onClick,
    shapes = defaultButtonShapes(),
    modifier = modifier,
    enabled = enabled,
    colors = colors,
    elevation = elevation,
    border = border,
    contentPadding = contentPadding,
    interactionSource = interactionSource,
    content = content
)

@ExperimentalMaterial3ExpressiveApi
@Composable
fun AppButton(
    onClick: () -> Unit,
    shapes: ButtonShapes,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) = CompletePressButton(
    onClick = onClick,
    shapes = shapes,
    modifier = modifier,
    enabled = enabled,
    colors = colors,
    elevation = elevation,
    border = border,
    contentPadding = contentPadding,
    interactionSource = interactionSource,
    content = content
)

@Composable
fun AppFilledTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.filledTonalButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.filledTonalButtonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) = CompletePressFilledTonalButton(
    onClick = onClick,
    shapes = defaultButtonShapes(),
    modifier = modifier,
    enabled = enabled,
    colors = colors,
    elevation = elevation,
    border = border,
    contentPadding = contentPadding,
    interactionSource = interactionSource,
    content = content
)

@ExperimentalMaterial3ExpressiveApi
@Composable
fun AppFilledTonalButton(
    onClick: () -> Unit,
    shapes: ButtonShapes,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.filledTonalButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.filledTonalButtonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) = CompletePressFilledTonalButton(
    onClick = onClick,
    shapes = shapes,
    modifier = modifier,
    enabled = enabled,
    colors = colors,
    elevation = elevation,
    border = border,
    contentPadding = contentPadding,
    interactionSource = interactionSource,
    content = content
)

@Composable
fun AppOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = ButtonDefaults.outlinedButtonBorder(enabled),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) = CompletePressOutlinedButton(
    onClick = onClick,
    shapes = defaultButtonShapes(),
    modifier = modifier,
    enabled = enabled,
    colors = colors,
    elevation = elevation,
    border = border,
    contentPadding = contentPadding,
    interactionSource = interactionSource,
    content = content
)

@ExperimentalMaterial3ExpressiveApi
@Composable
fun AppOutlinedButton(
    onClick: () -> Unit,
    shapes: ButtonShapes,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = ButtonDefaults.outlinedButtonBorder(enabled),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) = CompletePressOutlinedButton(
    onClick = onClick,
    shapes = shapes,
    modifier = modifier,
    enabled = enabled,
    colors = colors,
    elevation = elevation,
    border = border,
    contentPadding = contentPadding,
    interactionSource = interactionSource,
    content = content
)

@Composable
fun AppTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) = CompletePressTextButton(
    onClick = onClick,
    shapes = defaultButtonShapes(),
    modifier = modifier,
    enabled = enabled,
    colors = colors,
    elevation = elevation,
    border = border,
    contentPadding = contentPadding,
    interactionSource = interactionSource,
    content = content
)

@ExperimentalMaterial3ExpressiveApi
@Composable
fun AppTextButton(
    onClick: () -> Unit,
    shapes: ButtonShapes,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) = CompletePressTextButton(
    onClick = onClick,
    shapes = shapes,
    modifier = modifier,
    enabled = enabled,
    colors = colors,
    elevation = elevation,
    border = border,
    contentPadding = contentPadding,
    interactionSource = interactionSource,
    content = content
)

@Composable
internal fun CompletePressToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    shapes: ToggleButtonShapes,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ToggleButtonColors = ToggleButtonDefaults.toggleButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ToggleButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val progress by rememberCompletePressProgress(source, enabled)
    val selection by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(ReleaseShapeDurationMillis),
        label = "connectedRestingShape"
    )
    val resting = interpolateRoundedShape(shapes.shape, shapes.checkedShape, selection)
    val shape = completePressShape(resting, shapes.pressedShape, progress)
    ToggleButton(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.expressiveHoverLayer(
            interactionSource = source,
            enabled = enabled,
            shape = shape,
            liftDp = 1.dp,
            highlightRim = false
        ),
        enabled = enabled,
        shapes = ToggleButtonShapes(shape, shape, shape),
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = source,
        content = content
    )
}
