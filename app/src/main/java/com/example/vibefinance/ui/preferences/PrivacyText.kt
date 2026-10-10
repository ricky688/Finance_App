package com.example.vibefinance.ui.preferences

import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.TextUnit
import androidx.compose.foundation.text.InlineTextContent

private val currencyAmount = Regex("(?:HK\\$|US\\$|NT\\$|A\\$|C\\$|[\\p{Sc}]|\\b[A-Z]{3}\\s+)[+−-]?\\s*[0-9][0-9,]*(?:\\.[0-9]+)?")
private val embeddedDecimal = Regex("(?<![\\p{L}\\d.])[-+−]?[0-9][0-9,]*\\.[0-9]{2}(?![0-9.%x])")
private val plainAmount = Regex("^[+−-]?[0-9][0-9,]*\\.[0-9]{1,2}$")
fun redactAmounts(text: String): String = if (plainAmount.matches(text.trim())) "••••"
    else embeddedDecimal.replace(currencyAmount.replace(text, "••••"), "••••")

/** Replaces the displayed text AND semantics; original amounts are not left in accessibility nodes. */
@Composable
fun PrivacyText(
    text: String, modifier: Modifier = Modifier, color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified, fontStyle: FontStyle? = null, fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null, letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null, textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified, overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true, maxLines: Int = Int.MAX_VALUE, minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null, style: TextStyle = LocalTextStyle.current
) {
    androidx.compose.material3.Text(text = if (LocalExperience.current.hideAmounts) redactAmounts(text) else text,
        modifier = modifier, color = color, fontSize = fontSize, fontStyle = fontStyle, fontWeight = fontWeight,
        fontFamily = fontFamily, letterSpacing = letterSpacing, textDecoration = textDecoration,
        textAlign = textAlign, lineHeight = lineHeight, overflow = overflow, softWrap = softWrap,
        maxLines = maxLines, minLines = minLines, onTextLayout = onTextLayout, style = style)
}

@Composable
fun PrivacyText(
    text: AnnotatedString, modifier: Modifier = Modifier, color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified, fontStyle: FontStyle? = null, fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null, letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null, textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified, overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true, maxLines: Int = Int.MAX_VALUE, minLines: Int = 1,
    inlineContent: Map<String, InlineTextContent> = mapOf(), onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    val redacted = if (LocalExperience.current.hideAmounts) redactAmounts(text.text) else text.text
    val display = if (redacted == text.text) text else AnnotatedString(redacted)
    androidx.compose.material3.Text(text = display, modifier = modifier, color = color, fontSize = fontSize,
        fontStyle = fontStyle, fontWeight = fontWeight, fontFamily = fontFamily, letterSpacing = letterSpacing,
        textDecoration = textDecoration, textAlign = textAlign, lineHeight = lineHeight, overflow = overflow,
        softWrap = softWrap, maxLines = maxLines, minLines = minLines, inlineContent = inlineContent,
        onTextLayout = onTextLayout, style = style)
}
