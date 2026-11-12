package com.example.vibefinance.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsCardsFamilyTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun rolloverOptionCard_singleClickSelects_andGuaranteesAccessibleTouchTarget() {
        var selected by mutableStateOf(false)
        var clicks = 0

        rule.setContent {
            MaterialTheme {
                Box(Modifier.padding(16.dp)) {
                    RolloverOptionCard(
                        title = "平攤至下週期每日預算",
                        desc = "將剩餘額度平均分配至下個週期的每一天",
                        isSelected = selected,
                        onClick = {
                            clicks++
                            selected = true
                        }
                    )
                }
            }
        }

        val card = rule.onNode(hasText("平攤至下週期每日預算"))
        card.assertExists()
        // Must guarantee at least 48dp touch target
        card.assertHeightIsAtLeast(SettingsTokens.MinTouchTarget)

        // Must declare RadioButton semantic role
        rule.onNode(hasAnyAncestor(hasText("平攤至下週期每日預算")) or hasText("平攤至下週期每日預算"))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))

        // Before click
        assertEquals(0, clicks)
        assertFalse(selected)

        // Click card
        card.performClick()
        rule.waitForIdle()

        // Single click executed
        assertEquals(1, clicks)
        assertTrue(selected)

        // Selected state verified
        rule.onNode(hasText("平攤至下週期每日預算")).assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, true))
    }

    @Test
    fun themeOptionCard_hasMinimumTouchTargetAndToggles() {
        var clicks = 0
        var isSelected by mutableStateOf(false)

        rule.setContent {
            MaterialTheme {
                Box(Modifier.padding(16.dp)) {
                    ThemeOptionCard(
                        title = "淺色模式",
                        icon = Icons.Default.LightMode,
                        isSelected = isSelected,
                        onClick = {
                            clicks++
                            isSelected = !isSelected
                        }
                    )
                }
            }
        }

        val card = rule.onNode(hasText("淺色模式"))
        card.assertExists()
        card.assertHeightIsAtLeast(SettingsTokens.MinTouchTarget)

        card.performClick()
        rule.waitForIdle()

        assertEquals(1, clicks)
        assertTrue(isSelected)
    }

    @Test
    fun languageOptionCard_hasMinimumTouchTarget() {
        var clicks = 0

        rule.setContent {
            MaterialTheme {
                Box(Modifier.padding(16.dp)) {
                    LanguageOptionCard(
                        title = "繁體中文（香港特別行政區）",
                        subtitle = "ZH-HK",
                        isSelected = true,
                        onClick = { clicks++ }
                    )
                }
            }
        }

        val card = rule.onNode(hasText("繁體中文（香港特別行政區）"))
        card.assertExists()
        card.assertHeightIsAtLeast(SettingsTokens.MinTouchTarget)

        card.performClick()
        rule.waitForIdle()

        assertEquals(1, clicks)
    }

    @Test
    fun settingsActionCard_rendersWithDefaultTokensAndFiresSingleClick() {
        var clicks = 0

        rule.setContent {
            MaterialTheme {
                Box(Modifier.padding(16.dp)) {
                    SettingsActionCard(
                        onClick = { clicks++ },
                        modifier = Modifier.fillMaxWidth().testTag("ActionCard")
                    ) {
                        Text("次級設定選項", modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }

        val card = rule.onNodeWithTag("ActionCard")
        card.assertExists()
        card.assertHeightIsAtLeast(SettingsTokens.MinTouchTarget)

        card.performClick()
        rule.waitForIdle()

        assertEquals(1, clicks)
    }
}
