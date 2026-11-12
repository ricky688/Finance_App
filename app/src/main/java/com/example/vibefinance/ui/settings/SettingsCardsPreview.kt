package com.example.vibefinance.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vibefinance.theme.VibeFinanceTheme

@Composable
private fun SettingsCardsPreviewContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "主題選項卡 (ThemeOptionCard)",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemeOptionCard(
                title = "淺色模式",
                icon = Icons.Default.LightMode,
                isSelected = false,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
            ThemeOptionCard(
                title = "深色模式",
                icon = Icons.Default.DarkMode,
                isSelected = true,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = "語言選項卡 (LanguageOptionCard)",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LanguageOptionCard(
                title = "繁體中文（香港特別行政區）",
                subtitle = "ZH-HK",
                isSelected = true,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
            LanguageOptionCard(
                title = "English (United States)",
                subtitle = "EN-US",
                isSelected = false,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = "預算滾動選項列 (RolloverOptionCard)",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        RolloverOptionCard(
            title = "平攤至下週期每日預算",
            desc = "將剩餘額度平均分配至下個週期的每一天，維持平穩花費節奏",
            isSelected = true,
            onClick = {}
        )
        RolloverOptionCard(
            title = "直接全額計入總額度",
            desc = "剩餘額度直接全額合併至下週期預算池中，不改變每日基底",
            isSelected = false,
            onClick = {}
        )

        Text(
            text = "次級設定動作卡 (SettingsActionCard)",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        SettingsActionCard(
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SettingsTokens.CardPaddingHorizontal, vertical = SettingsTokens.CardPaddingVertical),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(SettingsTokens.HorizontalIconToTextGap))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "色彩方案與自訂調色盤",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(SettingsTokens.TitleToDescriptionGap))
                    Text(
                        text = "動態取色 (Dynamic Monet) • 原創自然綠",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(name = "Fixed Palette Light", showBackground = true, widthDp = 380)
@Composable
fun SettingsCardsPreviewLightFixed() {
    VibeFinanceTheme(darkTheme = false, dynamicColorEnabled = false) {
        SettingsCardsPreviewContent()
    }
}

@Preview(name = "Fixed Palette Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 380)
@Composable
fun SettingsCardsPreviewDarkFixed() {
    VibeFinanceTheme(darkTheme = true, dynamicColorEnabled = false) {
        SettingsCardsPreviewContent()
    }
}

@Preview(name = "Dynamic Color Light", showBackground = true, widthDp = 380)
@Composable
fun SettingsCardsPreviewDynamicLight() {
    VibeFinanceTheme(darkTheme = false, dynamicColorEnabled = true) {
        SettingsCardsPreviewContent()
    }
}

@Preview(name = "Large Font Scale & Narrow Width (320dp)", fontScale = 1.3f, showBackground = true, widthDp = 320)
@Composable
fun SettingsCardsPreviewLargeFontNarrow() {
    VibeFinanceTheme(darkTheme = false, dynamicColorEnabled = false) {
        SettingsCardsPreviewContent()
    }
}
