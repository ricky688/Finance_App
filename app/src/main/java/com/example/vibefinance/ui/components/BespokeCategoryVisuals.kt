package com.example.vibefinance.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.vibefinance.ui.preferences.LocalExperience
import com.example.vibefinance.ui.preferences.MotionLevel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Checks if a normalized category key maps to one of the 15 bespoke animated categories.
 */
fun isBespokeCategory(normalized: String): Boolean = when (normalized) {
    "food", "food & drink", "restaurant", "food & dining" -> true
    "coffee", "cafe" -> true
    "shopping", "clothing", "groceries", "supermarket" -> true
    "transport", "transit", "commute", "bus", "taxi", "car", "driving", "fuel", "gas" -> true
    "travel", "holiday", "flight" -> true
    "housing", "rent", "home" -> true
    "bills", "utilities", "electricity", "water" -> true
    "health", "medical", "health & medical" -> true
    "education", "tuition", "school" -> true
    "fitness", "gym" -> true
    "gift", "redpocket", "allowance", "petty cash" -> true
    "game", "games", "gaming" -> true
    "entertainment", "movie", "movies", "netflix", "premium", "subscription", "recurring" -> true
    "pets", "pet" -> true
    "work", "part-time", "freelance", "side hustle" -> true
    else -> false
}

/**
 * Renders bespoke animated category visuals where internal icon components animate realistically.
 */
@Composable
fun BespokeCategoryVisual(
    category: String,
    isSelected: Boolean,
    trigger: Int,
    tint: Color,
    iconSize: Dp,
    modifier: Modifier = Modifier
) {
    val normalized = remember(category) {
        category.trim().replaceFirst(Regex("^[^\\p{L}\\p{N}]+"), "").lowercase().trim()
    }

    Box(modifier = modifier.size(iconSize), contentAlignment = Alignment.Center) {
        when (normalized) {
            "food", "food & drink", "restaurant", "food & dining" ->
                FoodVisual(isSelected, trigger, tint)
            "coffee", "cafe" ->
                CoffeeVisual(isSelected, trigger, tint)
            "shopping", "clothing", "groceries", "supermarket" ->
                ShoppingVisual(isSelected, trigger, tint)
            "transport", "transit", "commute", "bus", "taxi", "car", "driving", "fuel", "gas" ->
                TransitCarVisual(isSelected, trigger, tint)
            "travel", "holiday", "flight" ->
                FlightVisual(isSelected, trigger, tint)
            "housing", "rent", "home" ->
                HomeVisual(isSelected, trigger, tint)
            "bills", "utilities", "electricity", "water" ->
                BillsVisual(isSelected, trigger, tint)
            "health", "medical", "health & medical" ->
                HealthVisual(isSelected, trigger, tint)
            "education", "tuition", "school" ->
                SchoolVisual(isSelected, trigger, tint)
            "fitness", "gym" ->
                FitnessVisual(isSelected, trigger, tint)
            "gift", "redpocket", "allowance", "petty cash" ->
                GiftVisual(isSelected, trigger, tint)
            "game", "games", "gaming" ->
                GamesVisual(isSelected, trigger, tint)
            "entertainment", "movie", "movies", "netflix", "premium", "subscription", "recurring" ->
                MoviesVisual(isSelected, trigger, tint)
            "pets", "pet" ->
                PetsVisual(isSelected, trigger, tint)
            "work", "part-time", "freelance", "side hustle" ->
                WorkVisual(isSelected, trigger, tint)
        }
    }
}
