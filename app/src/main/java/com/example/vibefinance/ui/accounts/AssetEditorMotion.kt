/*
 * Motion adapted from ImageToolbox's Animations.kt and Easing.kt.
 * Copyright (c) 2026 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.vibefinance.ui.accounts

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

// Match toolboxPredictiveBackAnimation's normal page navigation, not its 600ms
// internal pane transition. Compose retains the app/system MotionDurationScale.
internal fun <T> assetEditorSlideSpec() =
    tween<T>(400, easing = CubicBezierEasing(0.48f, 0.19f, 0.05f, 1.03f))

internal fun <T> assetEditorFadeSpec() =
    tween<T>(300, easing = CubicBezierEasing(0.4f, 0.4f, 0.17f, 0.9f))

internal fun <T> assetEditorScaleSpec() =
    tween<T>(500, easing = CubicBezierEasing(0.55f, 0.55f, 0f, 1f))

@Composable
internal fun Modifier.assetEditorBackgroundMotion(transition: Transition<Boolean>): Modifier {
    val slide =
        transition.animateFloat(
            transitionSpec = { assetEditorSlideSpec() },
            label = "Editor background slide",
        ) { open ->
            if (open) -1f else 0f
        }
    val opacity =
        transition.animateFloat(
            transitionSpec = { assetEditorFadeSpec() },
            label = "Editor background fade",
        ) { open ->
            if (open) 0f else 1f
        }
    val scale =
        transition.animateFloat(
            transitionSpec = { assetEditorScaleSpec() },
            label = "Editor background scale",
        ) { open ->
            if (open) 0.95f else 1f
        }
    return graphicsLayer {
        translationX = size.width * slide.value
        alpha = opacity.value
        scaleX = scale.value
        scaleY = scale.value
    }
}
