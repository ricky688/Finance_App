package com.example.vibefinance.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.vibefinance.ui.home.CategoryIcon
import com.example.vibefinance.ui.preferences.LocalExperience
import com.example.vibefinance.ui.preferences.MotionLevel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Material 3 Expressive Animated Category Icon.
 * Features category-tailored physical kinetic choreography (e.g. car engine rumble & launch,
 * knife/fork chop, shopping bag swing, heartbeat pulse, flight takeoff, coin swallow)
 * executed entirely within GPU graphics layers without layout recomposition.
 */
@Composable
fun AnimatedCategoryIcon(
    category: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    trigger: Int = 0,
    tint: Color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
    iconSize: Dp = 16.dp
) {
    val motion = LocalExperience.current.motion
    val density = LocalDensity.current.density

    val scaleX = remember { Animatable(if (isSelected) 1.08f else 1f) }
    val scaleY = remember { Animatable(if (isSelected) 1.08f else 1f) }
    val offsetY = remember { Animatable(0f) }
    val offsetX = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }

    var wasSelected by remember { mutableStateOf(isSelected) }
    var lastTrigger by remember { mutableIntStateOf(trigger) }

    val normalized = remember(category) {
        category.trim().replaceFirst(Regex("^[^\\p{L}\\p{N}]+"), "").lowercase(Locale.US).trim()
    }

    LaunchedEffect(isSelected, trigger, motion) {
        val entering = isSelected && !wasSelected
        val reselected = isSelected && wasSelected && (trigger != lastTrigger)
        wasSelected = isSelected
        lastTrigger = trigger

        if ((entering || reselected) && motion == MotionLevel.FULL) {
            coroutineScope {
                when (normalized) {
                    // 1. Transport: Engine ignition rumble -> rev lean back -> acceleration surge launch
                    "transport", "transit", "commute", "bus", "taxi" -> {
                        launch {
                            // Phase 1: Engine vibration rumble (~100ms)
                            repeat(3) {
                                offsetY.animateTo(-1.5f, tween(16, easing = LinearEasing))
                                offsetY.animateTo(1.5f, tween(16, easing = LinearEasing))
                            }
                            offsetY.animateTo(0f, tween(16))
                        }
                        launch {
                            repeat(3) {
                                rotation.animateTo(2.5f, tween(16, easing = LinearEasing))
                                rotation.animateTo(-2.5f, tween(16, easing = LinearEasing))
                            }
                            rotation.animateTo(0f, tween(16))
                        }
                        launch {
                            // Phase 2: Rev & lean back (~70ms)
                            delay(96)
                            offsetX.animateTo(-2.5f, tween(70, easing = FastOutSlowInEasing))
                            // Phase 3: Acceleration surge forward (~110ms)
                            offsetX.animateTo(7.5f, tween(110, easing = FastOutSlowInEasing))
                            // Spring settle back to center
                            offsetX.animateTo(
                                0f,
                                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
                            )
                        }
                        launch {
                            delay(96)
                            scaleX.animateTo(0.94f, tween(70))
                            scaleX.animateTo(1.20f, tween(110))
                            scaleX.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            delay(96)
                            scaleY.animateTo(0.94f, tween(70))
                            scaleY.animateTo(1.20f, tween(110))
                            scaleY.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                    }

                    // 2. Food & Dining: Knife/fork lift -> sharp downward slice -> clink & settle
                    "food", "food & drink", "restaurant", "food & dining" -> {
                        launch {
                            // Phase 1: Lift & tilt
                            offsetY.animateTo(-3.5f, tween(80, easing = FastOutSlowInEasing))
                            // Phase 2: Sharp chop down
                            offsetY.animateTo(2.5f, tween(60, easing = FastOutSlowInEasing))
                            // Phase 3: Spring bounce back to baseline
                            offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            rotation.animateTo(-14f, tween(80, easing = FastOutSlowInEasing))
                            rotation.animateTo(8f, tween(60, easing = FastOutSlowInEasing))
                            rotation.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            scaleX.animateTo(1.0f, tween(80))
                            scaleX.animateTo(1.15f, tween(60)) // squash on slice
                            scaleX.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            scaleY.animateTo(1.12f, tween(80))
                            scaleY.animateTo(0.90f, tween(60))
                            scaleY.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                    }

                    // 3. Shopping & Groceries: Handle lift -> side-to-side pendulum swing -> weighted settle
                    "shopping", "clothing", "groceries", "supermarket" -> {
                        launch {
                            // Phase 1: Quick upward pull
                            offsetY.animateTo(-4.5f, tween(85, easing = FastOutSlowInEasing))
                            offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            // Phase 2: Pendulum swing
                            rotation.animateTo(18f, tween(80, easing = FastOutSlowInEasing))
                            rotation.animateTo(-14f, tween(85, easing = FastOutSlowInEasing))
                            rotation.animateTo(8f, tween(60, easing = FastOutSlowInEasing))
                            rotation.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            scaleX.animateTo(0.92f, tween(85))
                            scaleX.animateTo(1.14f, tween(100))
                            scaleX.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            scaleY.animateTo(1.15f, tween(85))
                            scaleY.animateTo(0.94f, tween(100))
                            scaleY.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                    }

                    // 4. Entertainment & Subscription: Film reel stutter crank -> high-speed 360° spin
                    "entertainment", "movie", "netflix", "premium", "subscription", "recurring" -> {
                        launch {
                            // Phase 1: 2 quick stutter ticks
                            rotation.animateTo(20f, tween(40, easing = LinearEasing))
                            rotation.animateTo(45f, tween(45, easing = LinearEasing))
                            // Phase 2: Smooth 360° acceleration spin
                            rotation.animateTo(360f, tween(220, easing = FastOutSlowInEasing))
                            rotation.snapTo(0f)
                        }
                        launch {
                            scaleX.animateTo(1.22f, tween(180, easing = FastOutSlowInEasing))
                            scaleX.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            scaleY.animateTo(1.22f, tween(180, easing = FastOutSlowInEasing))
                            scaleY.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                    }

                    // 5. Gaming: Rapid 4-way D-Pad combo twitch -> button press squash -> victory pop
                    "game", "games", "gaming" -> {
                        launch {
                            // D-pad micro movements
                            offsetX.animateTo(-2.5f, tween(35))
                            offsetX.animateTo(2.5f, tween(35))
                            offsetX.animateTo(-1.5f, tween(35))
                            offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            offsetY.animateTo(-2f, tween(35))
                            offsetY.animateTo(2f, tween(35))
                            offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            rotation.animateTo(-12f, tween(50))
                            rotation.animateTo(12f, tween(55))
                            rotation.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            delay(105)
                            // Button smash squash
                            scaleX.animateTo(0.88f, tween(50))
                            scaleX.animateTo(1.24f, tween(80))
                            scaleX.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            delay(105)
                            scaleY.animateTo(0.88f, tween(50))
                            scaleY.animateTo(1.24f, tween(80))
                            scaleY.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                    }

                    // 6. Utilities & Electronics: Charge -> lightning arc zap strobe -> dissipate
                    "utilities", "electricity", "water", "bills", "electronics", "gadgets" -> {
                        launch {
                            // Lightning zig-zag arc angles
                            rotation.animateTo(-18f, tween(40, easing = LinearEasing))
                            rotation.animateTo(18f, tween(45, easing = LinearEasing))
                            rotation.animateTo(-10f, tween(40, easing = LinearEasing))
                            rotation.animateTo(6f, tween(40, easing = LinearEasing))
                            rotation.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            scaleX.animateTo(0.90f, tween(40))
                            scaleX.animateTo(1.36f, tween(45))
                            scaleX.animateTo(1.10f, tween(40))
                            scaleX.animateTo(1.26f, tween(40))
                            scaleX.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            scaleY.animateTo(0.90f, tween(40))
                            scaleY.animateTo(1.36f, tween(45))
                            scaleY.animateTo(1.10f, tween(40))
                            scaleY.animateTo(1.26f, tween(40))
                            scaleY.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                    }

                    // 7. Housing: Roof stretch up -> solid foundation stomp -> lock in place
                    "housing", "rent" -> {
                        launch {
                            // Phase 1: Roof stretch up
                            offsetY.animateTo(-3.5f, tween(90, easing = FastOutSlowInEasing))
                            // Phase 2: Downward foundation stomp
                            offsetY.animateTo(2.5f, tween(75, easing = FastOutSlowInEasing))
                            // Phase 3: Firm settle
                            offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            scaleX.animateTo(0.90f, tween(90))
                            scaleX.animateTo(1.22f, tween(75)) // wide stomp
                            scaleX.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            scaleY.animateTo(1.24f, tween(90))
                            scaleY.animateTo(0.88f, tween(75))
                            scaleY.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                    }

                    // 8. Health & Medical: Cardiac double pulse (Lub-DUB)
                    "health", "medical", "health & medical" -> {
                        launch {
                            // "Lub" (first pulse)
                            scaleX.animateTo(1.18f, tween(60, easing = FastOutSlowInEasing))
                            scaleX.animateTo(1.05f, tween(40, easing = FastOutSlowInEasing))
                            // "DUB" (stronger ventricular pulse)
                            scaleX.animateTo(1.34f, tween(70, easing = FastOutSlowInEasing))
                            scaleX.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            scaleY.animateTo(1.18f, tween(60, easing = FastOutSlowInEasing))
                            scaleY.animateTo(1.05f, tween(40, easing = FastOutSlowInEasing))
                            scaleY.animateTo(1.34f, tween(70, easing = FastOutSlowInEasing))
                            scaleY.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            delay(100)
                            offsetY.animateTo(-2f, tween(70))
                            offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                    }

                    // 9. Fitness: Heavy dumbbell press / bicep curl lift -> hold -> weighted thump
                    "fitness", "gym" -> {
                        launch {
                            // Upward straining lift
                            offsetY.animateTo(-5.5f, tween(110, easing = FastOutSlowInEasing))
                            delay(60) // Peak contraction hold
                            // Weighted descent & thump
                            offsetY.animateTo(1.5f, tween(70, easing = FastOutSlowInEasing))
                            offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            rotation.animateTo(-10f, tween(110))
                            delay(60)
                            rotation.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            scaleX.animateTo(1.18f, tween(110))
                            delay(60)
                            scaleX.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            scaleY.animateTo(1.18f, tween(110))
                            delay(60)
                            scaleY.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                    }

                    // 10. Travel: Runway taxi roll -> nose pitch up -> soaring takeoff climb
                    "travel", "holiday" -> {
                        launch {
                            // Phase 1: Runway roll
                            offsetX.animateTo(2.5f, tween(70, easing = LinearEasing))
                            // Phase 3: Soaring climb
                            offsetX.animateTo(7f, tween(130, easing = FastOutSlowInEasing))
                            offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            // Phase 2: Nose up tilt
                            delay(70)
                            rotation.animateTo(-32f, tween(80, easing = FastOutSlowInEasing))
                            offsetY.animateTo(-5.5f, tween(130, easing = FastOutSlowInEasing))
                            rotation.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                            offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            delay(70)
                            scaleX.animateTo(1.22f, tween(130))
                            scaleX.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            delay(70)
                            scaleY.animateTo(1.22f, tween(130))
                            scaleY.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                    }

                    // 11. Salary / Savings / Income: Coin drop impact -> swallow wobble -> satisfied settle
                    "salary", "paycheck", "wages", "income", "bonus", "reward", "savings" -> {
                        launch {
                            // Coin impact squash
                            offsetY.animateTo(3f, tween(80, easing = FastOutSlowInEasing))
                            offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            scaleX.animateTo(1.24f, tween(80)) // squashes outward
                            scaleX.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            scaleY.animateTo(0.86f, tween(80)) // squashes downward
                            scaleY.animateTo(1.20f, tween(90)) // bounces tall
                            scaleY.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            delay(80)
                            rotation.animateTo(-8f, tween(50))
                            rotation.animateTo(8f, tween(50))
                            rotation.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                    }

                    // 12. Education: Graduation cap toss up -> drifting flutter -> catch
                    "education", "tuition" -> {
                        launch {
                            offsetY.animateTo(-6.5f, tween(110, easing = FastOutSlowInEasing))
                            offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            rotation.animateTo(-16f, tween(110))
                            rotation.animateTo(12f, tween(90))
                            rotation.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            scaleX.animateTo(1.18f, tween(110))
                            scaleX.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            scaleY.animateTo(1.18f, tween(110))
                            scaleY.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                    }

                    // 13. Gift: Excitement jiggle -> lid pop
                    "gift", "redpocket", "allowance", "petty cash" -> {
                        launch {
                            rotation.animateTo(-10f, tween(40))
                            rotation.animateTo(10f, tween(40))
                            rotation.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            delay(80)
                            offsetY.animateTo(-5.5f, tween(80, easing = FastOutSlowInEasing))
                            offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            delay(80)
                            scaleX.animateTo(1.28f, tween(80))
                            scaleX.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            delay(80)
                            scaleY.animateTo(1.28f, tween(80))
                            scaleY.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                    }

                    // 14. Investment / Stocks: Trend surge diagonal leap
                    "investment", "dividend", "stocks" -> {
                        launch {
                            offsetX.animateTo(4.5f, tween(110, easing = FastOutSlowInEasing))
                            offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            offsetY.animateTo(-4.5f, tween(110, easing = FastOutSlowInEasing))
                            offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            scaleX.animateTo(1.22f, tween(110))
                            scaleX.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            scaleY.animateTo(1.22f, tween(110))
                            scaleY.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                    }

                    // 15. Software / AI: Memory clock pulse surge
                    "software / ai", "software", "ai" -> {
                        launch {
                            scaleX.animateTo(1.20f, tween(50))
                            scaleX.animateTo(1.0f, tween(40))
                            scaleX.animateTo(1.30f, tween(60))
                            scaleX.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            scaleY.animateTo(1.20f, tween(50))
                            scaleY.animateTo(1.0f, tween(40))
                            scaleY.animateTo(1.30f, tween(60))
                            scaleY.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                    }

                    // 16. Transfer & Loans: Lateral flow shift
                    "transfer", "修改結餘", "借錢", "還錢" -> {
                        launch {
                            offsetX.animateTo(4.5f, tween(90, easing = FastOutSlowInEasing))
                            offsetX.animateTo(-3f, tween(80, easing = FastOutSlowInEasing))
                            offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            scaleX.animateTo(1.20f, tween(90))
                            scaleX.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            scaleY.animateTo(1.20f, tween(90))
                            scaleY.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                    }

                    // Default / Other categories: Universal M3E spring scale pop and micro-lift
                    else -> {
                        launch {
                            scaleX.snapTo(1f)
                            scaleX.animateTo(1.25f, tween(80, easing = FastOutSlowInEasing))
                            scaleX.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            scaleY.snapTo(1f)
                            scaleY.animateTo(1.25f, tween(80, easing = FastOutSlowInEasing))
                            scaleY.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        launch {
                            offsetY.snapTo(0f)
                            offsetY.animateTo(-2.5f, tween(80, easing = FastOutSlowInEasing))
                            offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                    }
                }
            }
        } else if (isSelected) {
            if (motion == MotionLevel.REDUCED) {
                scaleX.animateTo(1.08f, tween(120))
                scaleY.animateTo(1.08f, tween(120))
            } else {
                scaleX.snapTo(1.08f)
                scaleY.snapTo(1.08f)
            }
            offsetY.snapTo(0f)
            offsetX.snapTo(0f)
            rotation.snapTo(0f)
        } else {
            if (motion == MotionLevel.MINIMAL) {
                scaleX.snapTo(1f)
                scaleY.snapTo(1f)
            } else {
                scaleX.animateTo(1f, spring(stiffness = Spring.StiffnessMediumLow))
                scaleY.animateTo(1f, spring(stiffness = Spring.StiffnessMediumLow))
            }
            offsetY.snapTo(0f)
            offsetX.snapTo(0f)
            rotation.snapTo(0f)
        }
    }

    Box(
        modifier = modifier
            .testTag("animatedCategoryIcon_$category")
            .graphicsLayer {
                this.scaleX = scaleX.value
                this.scaleY = scaleY.value
                this.translationY = offsetY.value * density
                this.translationX = offsetX.value * density
                this.rotationZ = rotation.value
            },
        contentAlignment = Alignment.Center
    ) {
        if (isBespokeCategory(normalized)) {
            BespokeCategoryVisual(
                category = category,
                isSelected = isSelected,
                trigger = trigger,
                tint = tint,
                iconSize = iconSize
            )
        } else {
            CategoryIcon(
                category = category,
                tint = tint,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

/**
 * Animated Add Category icon with 90° spring spin on toggle and micro-bounce on click.
 */
@Composable
fun AnimatedAddCategoryIcon(
    isExpanded: Boolean,
    trigger: Int = 0,
    modifier: Modifier = Modifier,
    iconSize: Dp = 14.dp
) {
    val motion = LocalExperience.current.motion
    val rotation = remember { Animatable(if (isExpanded) 90f else 0f) }
    val scale = remember { Animatable(1f) }
    var lastTrigger by remember { mutableIntStateOf(trigger) }

    LaunchedEffect(isExpanded, trigger, motion) {
        val retriggered = trigger != lastTrigger
        lastTrigger = trigger
        val targetRot = if (isExpanded) 90f else 0f

        if (motion == MotionLevel.FULL) {
            coroutineScope {
                launch {
                    rotation.animateTo(
                        targetValue = targetRot,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                }
                if (retriggered) {
                    launch {
                        scale.snapTo(1f)
                        scale.animateTo(1.25f, tween(70, easing = FastOutSlowInEasing))
                        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                    }
                }
            }
        } else if (motion == MotionLevel.REDUCED) {
            rotation.animateTo(targetRot, tween(120))
            scale.snapTo(1f)
        } else {
            rotation.snapTo(targetRot)
            scale.snapTo(1f)
        }
    }

    Box(
        modifier = modifier
            .testTag("animatedAddCategoryIcon")
            .graphicsLayer {
                rotationZ = rotation.value
                scaleX = scale.value
                scaleY = scale.value
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(iconSize)
        )
    }
}
