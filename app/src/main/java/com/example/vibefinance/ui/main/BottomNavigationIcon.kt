package com.example.vibefinance.ui.main

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.vibefinance.ui.preferences.LocalExperience
import com.example.vibefinance.ui.preferences.MotionLevel
import kotlinx.coroutines.delay
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** Only the vector layer moves; the navigation indicator, label and 24dp slot stay fixed. */
@Composable
internal fun BottomNavigationIcon(
    tab: TabItem,
    selected: Boolean,
    title: String,
    filledIcon: ImageVector,
    outlinedIcon: ImageVector,
    trigger: Int = 0,
) {
    val motion = LocalExperience.current.motion
    val density = LocalDensity.current.density
    val scale = remember { Animatable(1f) }
    val historyX = remember { List(3) { Animatable(0f) } }
    val columnsY = remember { List(3) { Animatable(0f) } }
    val roofY = remember { Animatable(0f) }
    val baseY = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }
    var wasSelected by remember { mutableStateOf(selected) }
    var lastTrigger by remember { mutableIntStateOf(trigger) }
    LaunchedEffect(selected, trigger, motion) {
        val entering = selected && !wasSelected
        val reselected = selected && wasSelected && (trigger != lastTrigger)
        wasSelected = selected
        lastTrigger = trigger
        scale.snapTo(1f); rotation.snapTo(0f)
        columnsY.forEach { it.snapTo(0f) }
        roofY.snapTo(0f); baseY.snapTo(0f)
        historyX.forEach { it.snapTo(0f) }
        if ((entering || reselected) && motion == MotionLevel.FULL) {
            val settle = spring<Float>(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow)
            when (tab) {
                TabItem.HOME -> {
                    scale.animateTo(0.92f, tween(70))
                    scale.animateTo(1.15f, tween(100))
                    scale.animateTo(1f, settle)
                }
                TabItem.ACCOUNTS -> coroutineScope {
                    listOf(roofY, baseY).forEachIndexed { index, offset ->
                        launch {
                            delay(index * 250L)
                            offset.animateTo(-0.75f, tween(120, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)))
                            offset.animateTo(0f, spring(dampingRatio = 1f, stiffness = Spring.StiffnessMediumLow))
                        }
                    }
                    columnsY.forEachIndexed { column, offset ->
                        launch {
                            delay(column * 100L)
                            offset.animateTo(-1f, tween(120, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)))
                            offset.animateTo(0f, spring(dampingRatio = 1f, stiffness = Spring.StiffnessMediumLow))
                        }
                    }
                }
                TabItem.RECURRING -> {
                    rotation.animateTo(360f, tween(600, easing = CubicBezierEasing(0.48f, 0.19f, 0.05f, 1.03f)))
                    rotation.snapTo(0f)
                }
                TabItem.HISTORY -> coroutineScope {
                    historyX.forEachIndexed { row, offset ->
                        launch {
                            delay(row * 100L)
                            val easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
                            offset.animateTo(24f, tween(180, easing = easing))
                            offset.snapTo(-24f)
                            offset.animateTo(0f, tween(240, easing = easing))
                        }
                    }
                }
            }
        }
    }
    Box(Modifier.size(24.dp).testTag("BottomNavIcon_${tab.name}")) {
        Crossfade(
            targetState = if (selected) filledIcon else outlinedIcon,
            animationSpec = tween(if (motion == MotionLevel.FULL) 180 else 0),
            label = "Bottom navigation icon fill",
        ) { vector ->
            if (tab == TabItem.RECURRING) {
                val fixed = remember(vector) { autoModeLayer(vector, arrows = false) }
                val arrows = remember(vector) { autoModeLayer(vector, arrows = true) }
                Icon(fixed, contentDescription = title, modifier = Modifier.size(24.dp))
                Icon(arrows, contentDescription = null, modifier = Modifier.size(24.dp).graphicsLayer {
                    rotationZ = rotation.value
                })
            } else if (tab == TabItem.ACCOUNTS) {
                Box(Modifier.size(24.dp).semantics { contentDescription = title }) {
                    listOf(roofY, baseY).forEachIndexed { index, offset ->
                        Icon(vector, contentDescription = null, modifier = Modifier.size(24.dp)
                            .graphicsLayer { translationY = offset.value * density }
                            .drawWithContent {
                                val top = if (index == 0) 0f else 18f / 24f * size.height
                                val bottom = if (index == 0) 9f / 24f * size.height else size.height
                                clipRect(top = top, bottom = bottom) { this@drawWithContent.drawContent() }
                            })
                    }
                    columnsY.forEachIndexed { column, offset ->
                        Icon(vector, contentDescription = null, modifier = Modifier.size(24.dp)
                            .graphicsLayer { translationY = offset.value * density }
                            .drawWithContent {
                                // Clip before translating so the original roof/base never enter the moving layer.
                                clipRect(left = column * size.width / 3f, right = (column + 1) * size.width / 3f,
                                    top = 9f / 24f * size.height, bottom = 18f / 24f * size.height) {
                                    this@drawWithContent.drawContent()
                                }
                            })
                    }
                }
            } else if (tab == TabItem.HISTORY) {
                Box(Modifier.size(24.dp).semantics { contentDescription = title }) {
                    // Slice the original vector between rows, without redrawing its bullets or lines.
                    historyX.forEachIndexed { row, offset ->
                        Box(Modifier.matchParentSize().drawWithContent {
                            val top = if (row == 0) 0f else (6f + row * 4f) / 24f * size.height
                            val bottom = if (row == 2) size.height else (10f + row * 4f) / 24f * size.height
                            clipRect(top = top, bottom = bottom) { this@drawWithContent.drawContent() }
                        }) {
                            Icon(vector, contentDescription = null,
                                modifier = Modifier.size(24.dp).graphicsLayer {
                                    translationX = offset.value * density
                                })
                        }
                    }
                }
            } else {
                Icon(vector, contentDescription = title,
                    modifier = Modifier.size(24.dp).graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                    })
            }
        }
    }
}

/** AutoMode's final path is its circular arrow; retain the original library paths verbatim. */
internal fun autoModeLayer(vector: ImageVector, arrows: Boolean): ImageVector {
    val builder = ImageVector.Builder(
        name = "${vector.name}_${if (arrows) "arrows" else "fixed"}",
        defaultWidth = vector.defaultWidth, defaultHeight = vector.defaultHeight,
        viewportWidth = vector.viewportWidth, viewportHeight = vector.viewportHeight,
        autoMirror = vector.autoMirror,
    )
    for (index in 0 until vector.root.size) {
        if ((index == vector.root.size - 1) != arrows) continue
        val path = vector.root[index] as VectorPath
        builder.addPath(
            pathData = path.pathData, pathFillType = path.pathFillType, name = path.name,
            fill = path.fill, fillAlpha = path.fillAlpha, stroke = path.stroke,
            strokeAlpha = path.strokeAlpha, strokeLineWidth = path.strokeLineWidth,
            strokeLineCap = path.strokeLineCap, strokeLineJoin = path.strokeLineJoin,
            strokeLineMiter = path.strokeLineMiter, trimPathStart = path.trimPathStart,
            trimPathEnd = path.trimPathEnd, trimPathOffset = path.trimPathOffset,
        )
    }
    return builder.build()
}
