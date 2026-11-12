package com.example.vibefinance.ui.main

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.R
import com.example.vibefinance.theme.MotionTokens
import com.example.vibefinance.ui.preferences.LocalExperience
import com.example.vibefinance.ui.preferences.MotionLevel
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import com.example.vibefinance.util.CurrencyEngine
import kotlin.math.roundToInt

@Composable
internal fun TransactionEntryTopBar(
    mode: TransactionMode,
    currency: String,
    onCurrencyChange: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        IconButton(onClick = onDismiss) {
            Icon(Icons.Default.ArrowBack, stringResource(R.string.nav_back))
        }
        AnimatedContent(mode, modifier = Modifier.weight(1f), label = "titleTransition") { value ->
            Text(
                text = stringResource(when (value) {
                    TransactionMode.EXPENSE -> R.string.quick_add_expense
                    TransactionMode.INCOME -> R.string.quick_add_income
                    TransactionMode.TRANSFER -> R.string.quick_transfer
                }),
                modifier = Modifier.testTag("EntryModeTitle"), style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
        Box {
            var expanded by remember { mutableStateOf(false) }
            val current = CurrencyEngine.supportedCurrencies.firstOrNull { it.code == currency }
                ?: CurrencyEngine.supportedCurrencies.first()
            Surface(
                onClick = { expanded = !expanded }, shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.testTag("EntryCurrency"),
            ) {
                Text("${current.flagEmoji} ${current.code}",
                    Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
                    maxLines = 1, softWrap = false)
            }
            DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                CurrencyEngine.supportedCurrencies.forEach { option ->
                    DropdownMenuItem(
                        text = { Text("${option.flagEmoji} ${option.code} (${option.symbol})") },
                        modifier = Modifier.testTag("EntryCurrency_${option.code}"),
                        onClick = { onCurrencyChange(option.code); expanded = false },
                    )
                }
            }
        }
    }
}

/** The editable glyphs morph; their advances, line box and native selection stay stable. */
@Composable
internal fun TransactionEntryAmount(
    amount: String,
    onAmountChange: (String) -> Unit,
    onDone: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    val motion = LocalExperience.current.motion
    val minimal = motion == MotionLevel.MINIMAL || Build.VERSION.SDK_INT < 26
    val emphasis = animateFloatAsState(
        targetValue = if (focused) 1f else 0f,
        animationSpec = if (minimal) snap() else MotionTokens.emphasized(
            if (motion == MotionLevel.REDUCED) 100 else 200),
        label = "Amount figure emphasis"
    )
    val frame by remember { derivedStateOf { (emphasis.value * 16).roundToInt().coerceIn(0, 16) } }
    val family = AmountNumberFonts.family(if (minimal) { if (focused) 16 else 0 } else frame)
    val measuringStyle = MaterialTheme.typography.displayLarge.copy(
        fontFamily = AmountNumberFonts.family(16), fontWeight = FontWeight.Bold,
        fontSynthesis = FontSynthesis.None,
        fontFeatureSettings = "tnum", fontSize = 80.sp, lineHeight = 96.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Start
    )
    val height = with(LocalDensity.current) { 96.sp.toDp() }
    val measurer = rememberTextMeasurer(cacheSize = 16)
    val hidden = LocalExperience.current.hideAmounts
    val figures = amount.ifEmpty { "0" }
    val measuredText = if (hidden) "•".repeat(figures.length) else figures
    BoxWithConstraints(modifier) {
        val caretSpace = with(LocalDensity.current) { 4.dp.toPx() }
        val size = remember(measuredText, measurer, measuringStyle, constraints.maxWidth, caretSpace) {
            val available = (constraints.maxWidth - caretSpace).coerceAtLeast(1f)
            fun fits(size: Float) = measurer.measure(AnnotatedString(measuredText),
                style = measuringStyle.copy(fontSize = size.sp), softWrap = false,
                maxLines = 1, overflow = TextOverflow.Visible).size.width <= available
            if (fits(80f)) 80f else {
                var low = 16f
                var high = 80f
                // Measure candidate sizes: Android glyph rounding is not exactly proportional.
                repeat(8) {
                    val middle = (low + high) / 2f
                    if (fits(middle)) low = middle else high = middle
                }
                low
            }
        }
        val style = measuringStyle.copy(fontFamily = family, fontSize = size.sp)
        BasicTextField(
            value = if (amount == "0") "" else amount,
            onValueChange = { input ->
                if (input.isEmpty()) onAmountChange("0")
                else if (input == ".") onAmountChange("0.")
                else if (input.matches(Regex("""^\d*(?:\.\d{0,2})?$"""))) onAmountChange(input)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            textStyle = style,
            visualTransformation = if (hidden)
                PasswordVisualTransformation()
            else VisualTransformation.None,
            modifier = Modifier.width(IntrinsicSize.Min).widthIn(min = 32.dp).height(height)
                .focusRequester(focusRequester).onFocusChanged { focused = it.isFocused }
                .testTag("EntryAmount").semantics { stateDescription = amount },
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (amount == "0" || amount.isEmpty()) Text("0", style = style.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)))
                    innerTextField()
                }
            }
        )
    }
}

/** Only these 17 families enter the resolver cache, rather than one per animation tick. */
@OptIn(ExperimentalTextApi::class)
private object AmountNumberFonts {
    private val fallback = FontFamily(
        Font(R.font.roboto_flex_numbers_static, weight = FontWeight.Bold))
    private val variations by lazy {
        List(17) { frame ->
            val fraction = frame / 16f
            FontFamily(Font(
                R.font.roboto_flex_numbers, weight = FontWeight.Bold,
                variationSettings = FontVariation.Settings(
                    FontVariation.weight(700),
                    FontVariation.Setting("opsz", 42f),
                    FontVariation.Setting("YTFI", 680f + 108f * fraction),
                    FontVariation.Setting("GRAD", 25f * fraction)
                )))
        }
    }
    fun family(frame: Int): FontFamily =
        if (Build.VERSION.SDK_INT >= 26) variations[frame] else fallback
}
