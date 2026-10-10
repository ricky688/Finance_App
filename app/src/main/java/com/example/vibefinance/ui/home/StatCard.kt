package com.example.vibefinance.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import com.example.vibefinance.theme.BentoCardShape

/**
 * Computes auto-scaled font size for stat card values based on character length and compact layout mode.
 */
fun calculateStatCardValueFontSize(
    value: String,
    isCompact: Boolean,
    specifiedFontSize: TextUnit = TextUnit.Unspecified
): TextUnit = if (specifiedFontSize.isSpecified) {
    if (isCompact && value.length >= 10) {
        (specifiedFontSize.value * 0.75f).sp
    } else if (isCompact && value.length >= 7) {
        (specifiedFontSize.value * 0.85f).sp
    } else {
        specifiedFontSize
    }
} else {
    when {
        value.length >= 12 -> 15.sp
        value.length >= 9 -> 17.sp
        value.length >= 7 -> 19.sp
        isCompact -> 20.sp
        else -> 22.sp
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    contentPadding: PaddingValues? = null,
    colors: CardColors = CardDefaults.cardColors(),
    valueFontSize: TextUnit = TextUnit.Unspecified,
    valueFontStyle: TextStyle = MaterialTheme.typography.displayMedium,
    labelFontStyle: TextStyle = MaterialTheme.typography.labelMedium,
    content: @Composable ColumnScope.() -> Unit = {},
    backdropContent: @Composable () -> Unit = {},
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val isCompact = screenWidthDp < 400

    val effectivePadding = contentPadding ?: if (isCompact) {
        PaddingValues(vertical = 12.dp, horizontal = 12.dp)
    } else {
        PaddingValues(vertical = 14.dp, horizontal = 16.dp)
    }

    val effectiveValueFontSize = calculateStatCardValueFontSize(value, isCompact, valueFontSize)

    Card(
        modifier = modifier.clip(BentoCardShape),
        shape = BentoCardShape,
        colors = colors,
    ) {
        val textColor = LocalContentColor.current

        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
            ) {
                backdropContent()
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(effectivePadding)
            ) {
                Text(
                    text = value,
                    style = valueFontStyle,
                    fontSize = effectiveValueFontSize,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                    maxLines = 1,
                    lineHeight = TextUnit(1.2f, TextUnitType.Em)
                )
                Text(
                    text = label,
                    style = labelFontStyle,
                    color = textColor.copy(alpha = 0.65f),
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.height(2.dp))

                CompositionLocalProvider(
                    LocalContentColor provides textColor,
                ) {
                    Column(
                        content = content,
                    )
                }
            }
        }
    }
}
