package com.example.vibefinance.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween

/**
 * Material 3 Easing & Duration Tokens as specified in Material Design 3 guidelines:
 * https://m3.material.io/styles/motion/easing-and-duration/applying-easing-and-duration
 *
 * Provides the official Emphasized and Standard easing sets, along with the M3 Expressive
 * micro-overshoot curve and pre-configured animation spec helpers.
 */
object MotionTokens {
    // ==========================================
    // 1. Material 3 Emphasized Easing Set
    // Used for focal, high-impact macro-transitions:
    // Container transforms, Bottom Sheets, Dialogs, and expanding elements.
    // ==========================================
    /** Emphasized: Begin and end on screen (major morphs, container transforms). */
    val Emphasized: Easing = CubicBezierEasing(0.20f, 0.00f, 0.00f, 1.00f)

    /** Emphasized Decelerate: Elements entering the screen (sheets, dialogs, banners). */
    val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.70f, 0.10f, 1.00f)

    /** Emphasized Accelerate: Elements exiting the screen permanently. */
    val EmphasizedAccelerate: Easing = CubicBezierEasing(0.30f, 0.00f, 0.80f, 0.15f)

    // ==========================================
    // 2. Material 3 Standard Easing Set
    // Used for utilitarian, low-impact micro-interactions:
    // Toggle switches, icon state changes, tooltips, and subtle fades.
    // ==========================================
    /** Standard: Minor spatial adjustments begin and end on screen. */
    val Standard: Easing = CubicBezierEasing(0.20f, 0.00f, 0.00f, 1.00f)

    /** Standard Decelerate: Minor utility elements entering the screen. */
    val StandardDecelerate: Easing = CubicBezierEasing(0.00f, 0.00f, 0.20f, 1.00f)

    /** Standard Accelerate: Minor utility elements exiting the screen. */
    val StandardAccelerate: Easing = CubicBezierEasing(0.30f, 0.00f, 1.00f, 1.00f)

    // ==========================================
    // 3. Material 3 Expressive Micro-Overshoot Curve
    // Specified in DESIGN.md for tactile navigation pushes and sheet entrances.
    // ==========================================
    val FancyTransitionEasing: Easing = CubicBezierEasing(0.48f, 0.19f, 0.05f, 1.03f)

    // ==========================================
    // 4. Material 3 Standard Durations (in milliseconds)
    // ==========================================
    const val DurationShort1 = 50
    const val DurationShort2 = 100
    const val DurationShort3 = 150
    const val DurationShort4 = 200

    const val DurationMedium1 = 250
    const val DurationMedium2 = 300
    const val DurationMedium3 = 350
    const val DurationMedium4 = 400

    const val DurationLong1 = 450
    const val DurationLong2 = 500
    const val DurationLong3 = 550
    const val DurationLong4 = 600

    const val DurationExtraLong1 = 700
    const val DurationExtraLong2 = 800
    const val DurationExtraLong3 = 900
    const val DurationExtraLong4 = 1000

    // ==========================================
    // 5. Pre-configured Tween Spec Helpers
    // ==========================================
    /** Emphasized transform spec for elements that begin and end on screen (default 500ms). */
    fun <T> emphasized(durationMillis: Int = DurationLong2): TweenSpec<T> =
        tween(durationMillis, easing = Emphasized)

    /** Emphasized enter spec for elements entering the screen (default 400ms). */
    fun <T> emphasizedEnter(durationMillis: Int = DurationMedium4): TweenSpec<T> =
        tween(durationMillis, easing = EmphasizedDecelerate)

    /** Emphasized exit spec for elements leaving the screen (default 200ms). */
    fun <T> emphasizedExit(durationMillis: Int = DurationShort4): TweenSpec<T> =
        tween(durationMillis, easing = EmphasizedAccelerate)

    /** Standard transform spec for utility components (default 300ms). */
    fun <T> standard(durationMillis: Int = DurationMedium2): TweenSpec<T> =
        tween(durationMillis, easing = Standard)

    /** Standard enter spec for utility components (default 250ms). */
    fun <T> standardEnter(durationMillis: Int = DurationMedium1): TweenSpec<T> =
        tween(durationMillis, easing = StandardDecelerate)

    /** Standard exit spec for utility components (default 200ms). */
    fun <T> standardExit(durationMillis: Int = DurationShort4): TweenSpec<T> =
        tween(durationMillis, easing = StandardAccelerate)

    /** Expressive overshoot spec for playful pushes and dialogs (default 400ms). */
    fun <T> fancy(durationMillis: Int = DurationMedium4): TweenSpec<T> =
        tween(durationMillis, easing = FancyTransitionEasing)
}
