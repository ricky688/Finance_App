package com.example.vibefinance.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.vibefinance.R
import com.example.vibefinance.theme.*
import java.util.Locale
import kotlin.math.roundToInt

@Composable
internal fun paletteStyleLabel(style: PaletteStyle): String = stringResource(when (style) {
    PaletteStyle.TONAL_SPOT -> R.string.appearance_palette_style_desc
    PaletteStyle.VIBRANT -> R.string.palette_style_vibrant
    PaletteStyle.EXPRESSIVE -> R.string.palette_style_expressive
    PaletteStyle.NEUTRAL -> R.string.palette_style_neutral
    PaletteStyle.MONOCHROME -> R.string.palette_style_monochrome
    PaletteStyle.FIDELITY -> R.string.palette_style_fidelity
})

@Composable
internal fun PaletteStyleDialog(selected: PaletteStyle, onSelect: (PaletteStyle) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.appearance_palette_style_title)) },
        text = {
            Column(Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                PaletteStyle.entries.forEach { style ->
                    SettingsActionCard(
                        onClick = { onSelect(style) }, shape = RoundedCornerShape(16.dp),
                        color = if (style == selected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth().testTag("PaletteStyle_${style.name}")
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = style == selected, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text(paletteStyleLabel(style), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) } }
    )
}

internal fun parsePaletteHex(hex: String): Int? = hex.removePrefix("#").takeIf {
    it.length == 6 && it.all { c -> c in '0'..'9' || c in 'a'..'f' || c in 'A'..'F' }
}?.toLongOrNull(16)?.toInt()?.or(0xFF000000.toInt())

@Composable
internal fun PaletteVariantDialog(
    palette: AppearancePalette, initialSeed: Int, dark: Boolean, contrast: Int, style: PaletteStyle,
    onSave: (Int) -> Unit, onDismiss: () -> Unit
) {
    fun format(seed: Int) = String.format(Locale.ROOT, "%06X", seed and 0xFFFFFF)
    var hex by rememberSaveable(palette) { mutableStateOf(format(initialSeed)) }
    val seed = parsePaletteHex(hex)
    val previewSeed = seed ?: initialSeed
    val preview = remember(palette, previewSeed, dark, contrast, style) {
        paletteColorScheme(palette, dark, contrast, style, previewSeed)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.palette_edit_variant)) },
        text = {
            Column(Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(paletteLabel(palette), style = MaterialTheme.typography.titleMedium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(preview.primary, preview.secondary, preview.tertiary).forEach { color ->
                        Box(Modifier.weight(1f).height(48.dp).background(color, RoundedCornerShape(16.dp)))
                    }
                }
                OutlinedTextField(
                    value = hex, onValueChange = { hex = it }, singleLine = true,
                    label = { Text(stringResource(R.string.palette_hex_label)) },
                    supportingText = { Text(stringResource(R.string.palette_hex_help)) },
                    isError = seed == null, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    modifier = Modifier.fillMaxWidth().testTag("PaletteHex")
                )
                listOf(16 to R.string.palette_red, 8 to R.string.palette_green, 0 to R.string.palette_blue).forEach { (shift, label) ->
                    val channel = (previewSeed ushr shift) and 255
                    Text("${stringResource(label)}: $channel", style = MaterialTheme.typography.labelLarge)
                    Slider(
                        value = channel.toFloat(), valueRange = 0f..255f,
                        onValueChange = { value ->
                            val mask = 255 shl shift
                            hex = format((previewSeed and mask.inv()) or (value.roundToInt() shl shift))
                        }, modifier = Modifier.testTag("PaletteChannel_$shift")
                    )
                }
                if (palette != AppearancePalette.CUSTOM) {
                    TextButton(onClick = { hex = format(palette.seedArgb) }) { Text(stringResource(R.string.palette_reset_variant)) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { seed?.let(onSave) }, enabled = seed != null, modifier = Modifier.testTag("PaletteVariantSave")) {
                Text(stringResource(R.string.btn_save_changes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("PaletteVariantCancel")) { Text(stringResource(android.R.string.cancel)) }
        }
    )
}
