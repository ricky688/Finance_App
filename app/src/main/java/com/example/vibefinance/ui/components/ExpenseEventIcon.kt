package com.example.vibefinance.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.ExpenseIcon
import com.example.vibefinance.data.entity.ExpenseSymbol
import com.example.vibefinance.ui.home.CategoryIcon

internal fun ExpenseSymbol.vector(): ImageVector = when (this) {
    ExpenseSymbol.FOOD -> Icons.Default.Restaurant
    ExpenseSymbol.COFFEE -> Icons.Default.LocalCafe
    ExpenseSymbol.SHOPPING -> Icons.Default.ShoppingBag
    ExpenseSymbol.TRANSPORT -> Icons.Default.Commute
    ExpenseSymbol.CAR -> Icons.Default.DirectionsCar
    ExpenseSymbol.FLIGHT -> Icons.Default.Flight
    ExpenseSymbol.HOME -> Icons.Default.Home
    ExpenseSymbol.BILLS -> Icons.Default.ReceiptLong
    ExpenseSymbol.HEALTH -> Icons.Default.LocalHospital
    ExpenseSymbol.EDUCATION -> Icons.Default.School
    ExpenseSymbol.FITNESS -> Icons.Default.FitnessCenter
    ExpenseSymbol.GIFT -> Icons.Default.CardGiftcard
    ExpenseSymbol.GAMES -> Icons.Default.SportsEsports
    ExpenseSymbol.MOVIES -> Icons.Default.Movie
    ExpenseSymbol.PETS -> Icons.Default.Pets
    ExpenseSymbol.WORK -> Icons.Default.Work
}

internal fun ExpenseSymbol.label() = when (this) {
    ExpenseSymbol.FOOD -> R.string.expense_icon_food
    ExpenseSymbol.COFFEE -> R.string.expense_icon_coffee
    ExpenseSymbol.SHOPPING -> R.string.expense_icon_shopping
    ExpenseSymbol.TRANSPORT -> R.string.expense_icon_transport
    ExpenseSymbol.CAR -> R.string.expense_icon_car
    ExpenseSymbol.FLIGHT -> R.string.expense_icon_flight
    ExpenseSymbol.HOME -> R.string.expense_icon_home
    ExpenseSymbol.BILLS -> R.string.expense_icon_bills
    ExpenseSymbol.HEALTH -> R.string.expense_icon_health
    ExpenseSymbol.EDUCATION -> R.string.expense_icon_education
    ExpenseSymbol.FITNESS -> R.string.expense_icon_fitness
    ExpenseSymbol.GIFT -> R.string.expense_icon_gift
    ExpenseSymbol.GAMES -> R.string.expense_icon_games
    ExpenseSymbol.MOVIES -> R.string.expense_icon_movies
    ExpenseSymbol.PETS -> R.string.expense_icon_pets
    ExpenseSymbol.WORK -> R.string.expense_icon_work
}

@Composable
fun ExpenseEventIcon(customIcon: String?, category: String, tint: Color, modifier: Modifier = Modifier.size(24.dp)) {
    val icon = ExpenseIcon.normalize(customIcon)
    when {
        icon?.startsWith("symbol:") == true -> {
            val symbol = ExpenseSymbol.valueOf(icon.removePrefix("symbol:"))
            Icon(symbol.vector(), stringResource(symbol.label()), modifier, tint)
        }
        icon?.startsWith("emoji:") == true -> {
            val emoji = icon.removePrefix("emoji:")
            BoxWithConstraints(modifier.semantics { contentDescription = emoji }, contentAlignment = Alignment.Center) {
                // Like vector icons, keep the glyph inside its measured icon bounds at large font sizes.
                val glyphSize = (minOf(maxWidth.value, maxHeight.value) * 0.85f / LocalDensity.current.fontScale).sp
                Text(emoji, fontSize = glyphSize, lineHeight = glyphSize, maxLines = 1)
            }
        }
        else -> CategoryIcon(category, tint, modifier)
    }
}
