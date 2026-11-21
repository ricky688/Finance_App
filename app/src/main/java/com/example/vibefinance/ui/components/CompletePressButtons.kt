@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Global Material 3 Expressive Button Defaults and CompositionLocal.
 * Default resting shape: CircleShape (50% full pill)
 * Default pressed shape (< 48dp): RoundedCornerShape(8.dp)
 * Large pressed shape (>= 48dp): RoundedCornerShape(12.dp)
 */
object AppButtonDefaults {
    val RestingShape: Shape = CircleShape
    val PressedShape: Shape = RoundedCornerShape(8.dp)
    val LargePressedShape: Shape = RoundedCornerShape(12.dp)

    // Translucent surfaces expose the Android hover shadow's rectangular content cutout.
    @Composable
    fun elevation(): ButtonElevation = ButtonDefaults.buttonElevation(hoveredElevation = 0.dp)

    /** Toggle buttons take their own elevation type since material3 1.5.0. */
    @Composable
    fun toggleElevation(): ToggleButtonElevation = ToggleButtonDefaults.elevation(hoveredElevation = 0.dp)

    @Composable
    fun tonalElevation(): ButtonElevation = ButtonDefaults.filledTonalButtonElevation(hoveredElevation = 0.dp)

    @Composable
    fun shapes(
        shape: Shape = RestingShape,
        pressedShape: Shape = PressedShape
    ): ButtonShapes = ButtonDefaults.shapes(shape = shape, pressedShape = pressedShape)

    @Composable
    fun largeShapes(
        shape: Shape = RestingShape,
        pressedShape: Shape = LargePressedShape
    ): ButtonShapes = ButtonDefaults.shapes(shape = shape, pressedShape = pressedShape)
}

val LocalAppButtonShapes = staticCompositionLocalOf<ButtonShapes?> { null }

@Composable
internal fun defaultButtonShapes(): ButtonShapes {
    return LocalAppButtonShapes.current ?: AppButtonDefaults.shapes()
}

/** Material buttons own the single shape-clipped ripple/hover layer; only shape timing is coordinated. */
@Composable
internal fun CompletePressButton(
    onClick: () -> Unit,
    shapes: ButtonShapes = defaultButtonShapes(),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = AppButtonDefaults.elevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val progress by rememberCompletePressProgress(source, enabled)
    val buttonShape = completePressShape(shapes.shape, shapes.pressedShape, progress)
    val bounceModifier = rememberPostPressIconBounce(
        selected = false,
        restingSelectedScale = 1f,
        interactionSource = source,
    )
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = buttonShape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = source,
    ) {
        CompositionLocalProvider(LocalPostPressIconBounce provides bounceModifier) {
            content()
        }
    }
}

@Composable
internal fun CompletePressFilledTonalButton(
    onClick: () -> Unit,
    shapes: ButtonShapes = defaultButtonShapes(),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.filledTonalButtonColors(),
    elevation: ButtonElevation? = AppButtonDefaults.tonalElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val progress by rememberCompletePressProgress(source, enabled)
    val buttonShape = completePressShape(shapes.shape, shapes.pressedShape, progress)
    val bounceModifier = rememberPostPressIconBounce(
        selected = false,
        restingSelectedScale = 1f,
        interactionSource = source,
    )
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = buttonShape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = source,
    ) {
        CompositionLocalProvider(LocalPostPressIconBounce provides bounceModifier) {
            content()
        }
    }
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
    val bounceModifier = rememberPostPressIconBounce(
        selected = false,
        restingSelectedScale = 1f,
        interactionSource = source,
    )
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = buttonShape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = source,
    ) {
        CompositionLocalProvider(LocalPostPressIconBounce provides bounceModifier) {
            content()
        }
    }
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
    val bounceModifier = rememberPostPressIconBounce(
        selected = false,
        restingSelectedScale = 1f,
        interactionSource = source,
    )
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = buttonShape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = source,
    ) {
        CompositionLocalProvider(LocalPostPressIconBounce provides bounceModifier) {
            content()
        }
    }
}

@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = AppButtonDefaults.elevation(),
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
    elevation: ButtonElevation? = AppButtonDefaults.elevation(),
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
    elevation: ButtonElevation? = AppButtonDefaults.tonalElevation(),
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
    elevation: ButtonElevation? = AppButtonDefaults.tonalElevation(),
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
    colors: ToggleButtonColors = ToggleButtonDefaults.colors(),
    elevation: ToggleButtonElevation? = AppButtonDefaults.toggleElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ToggleButtonDefaults.contentPaddingFor(ButtonDefaults.MinHeight),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val progress by rememberCompletePressProgress(source, enabled)
    val minimalMotion = com.example.vibefinance.ui.preferences.LocalExperience.current.motion ==
        com.example.vibefinance.ui.preferences.MotionLevel.MINIMAL
    val selection by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = if (minimalMotion) androidx.compose.animation.core.snap() else tween(ReleaseShapeDurationMillis),
        label = "connectedRestingShape"
    )
    val resting = interpolateRoundedShape(shapes.shape, shapes.checkedShape, selection)
    val shape = completePressShape(resting, shapes.pressedShape, progress)
    val bounceModifier = rememberPostPressIconBounce(
        selected = checked,
        restingSelectedScale = 1.08f,
        interactionSource = source,
    )
    ToggleButton(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        shapes = ToggleButtonShapes(shape, shape, shape),
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = source,
    ) {
        CompositionLocalProvider(LocalPostPressIconBounce provides bounceModifier) {
            content()
        }
    }
}

private val SplitOuterCorner = CornerSize(50)
private val SplitInnerCorner = CornerSize(4.dp)
private val SplitPillShape = RoundedCornerShape(SplitOuterCorner)
private val SplitLeadingRestShape = RoundedCornerShape(SplitOuterCorner, SplitInnerCorner, SplitInnerCorner, SplitOuterCorner)
private val SplitTrailingRestShape = RoundedCornerShape(SplitInnerCorner, SplitOuterCorner, SplitOuterCorner, SplitInnerCorner)

/** SplitButton leading half: 50% outer / 4dp inner at rest, morphing to 50% full pill on press (180ms/220ms). */
@Composable
internal fun CompletePressSplitLeadingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tonal: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val progress by rememberCompletePressProgress(source, enabled)
    val shape = completePressShape(SplitLeadingRestShape, SplitPillShape, progress)
    val shapes = SplitButtonShapes(shape = shape, pressedShape = shape, checkedShape = null)
    val bounceModifier = rememberPostPressIconBounce(
        selected = false,
        restingSelectedScale = 1f,
        interactionSource = source,
    )
    if (tonal) {
        SplitButtonDefaults.TonalLeadingButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shapes = shapes,
            interactionSource = source,
        ) {
            CompositionLocalProvider(LocalPostPressIconBounce provides bounceModifier) {
                content()
            }
        }
    } else {
        SplitButtonDefaults.LeadingButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shapes = shapes,
            interactionSource = source,
        ) {
            CompositionLocalProvider(LocalPostPressIconBounce provides bounceModifier) {
                content()
            }
        }
    }
}

/** SplitButton trailing half: 4dp inner / 50% outer at rest, morphing to 50% full pill when pressed or checked. */
@Composable
internal fun CompletePressSplitTrailingButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tonal: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val progress by rememberCompletePressProgress(source, enabled)
    val minimalMotion = com.example.vibefinance.ui.preferences.LocalExperience.current.motion ==
        com.example.vibefinance.ui.preferences.MotionLevel.MINIMAL
    val checkedProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = if (minimalMotion) androidx.compose.animation.core.snap() else tween(ReleaseShapeDurationMillis),
        label = "splitTrailingCheckedShape"
    )
    val resting = interpolateRoundedShape(SplitTrailingRestShape, SplitPillShape, checkedProgress)
    val shape = completePressShape(resting, SplitPillShape, progress)
    val shapes = SplitButtonShapes(shape = shape, pressedShape = shape, checkedShape = shape)
    val bounceModifier = rememberPostPressIconBounce(
        selected = checked,
        restingSelectedScale = 1.08f,
        interactionSource = source,
    )
    if (tonal) {
        SplitButtonDefaults.TonalTrailingButton(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled,
            shapes = shapes,
            interactionSource = source,
        ) {
            CompositionLocalProvider(LocalPostPressIconBounce provides bounceModifier) {
                content()
            }
        }
    } else {
        SplitButtonDefaults.TrailingButton(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled,
            shapes = shapes,
            interactionSource = source,
        ) {
            CompositionLocalProvider(LocalPostPressIconBounce provides bounceModifier) {
                content()
            }
        }
    }
}

@Composable
internal fun CompletePressTonalSplitLeadingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) = CompletePressSplitLeadingButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    tonal = true,
    interactionSource = interactionSource,
    content = content
)

@Composable
internal fun CompletePressTonalSplitTrailingButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) = CompletePressSplitTrailingButton(
    checked = checked,
    onCheckedChange = onCheckedChange,
    modifier = modifier,
    enabled = enabled,
    tonal = true,
    interactionSource = interactionSource,
    content = content
)
