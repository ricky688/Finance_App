package com.example.vibefinance.ui.recurring

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AdaptiveRecurringSceneTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun wideScreenDisplaysBothListAndPlaceholderPanes() {
        rule.setContent {
            MaterialTheme {
                Box(Modifier.requiredSize(900.dp, 600.dp).testTag("Window")) {
                    AdaptiveRecurringScene(
                        selectedId = null,
                        isEditing = false,
                        onClose = {},
                        listContent = {
                            Text("List Pane Content", Modifier.testTag("TestListPane"))
                        },
                        placeholderContent = {
                            Text("Timeline Placeholder", Modifier.testTag("TestPlaceholder"))
                        },
                        detailContent = {
                            Text("Detail Content", Modifier.testTag("TestDetail"))
                        }
                    )
                }
            }
        }

        rule.waitForIdle()
        rule.onNodeWithTag("TestListPane").assertIsDisplayed()
        rule.onNodeWithTag("TestPlaceholder").assertIsDisplayed()
        rule.onNodeWithTag("TestDetail").assertDoesNotExist()

        val listBounds = rule.onNodeWithTag("TestListPane").fetchSemanticsNode().boundsInRoot
        val placeholderBounds = rule.onNodeWithTag("TestPlaceholder").fetchSemanticsNode().boundsInRoot
        assertTrue("List pane should sit on the left of placeholder", listBounds.right <= placeholderBounds.left + 50f)
    }

    @Test
    fun wideScreenEditingRendersDetailPaneSimultaneously() {
        var closed = false
        rule.setContent {
            MaterialTheme {
                Box(Modifier.requiredSize(900.dp, 600.dp).testTag("Window")) {
                    AdaptiveRecurringScene(
                        selectedId = 42L,
                        isEditing = true,
                        onClose = { closed = true },
                        listContent = {
                            Text("List Pane Content", Modifier.testTag("TestListPane"))
                        },
                        placeholderContent = {
                            Text("Timeline Placeholder", Modifier.testTag("TestPlaceholder"))
                        },
                        detailContent = { id ->
                            Text("Editing Item $id", Modifier.testTag("TestDetail"))
                        }
                    )
                }
            }
        }

        rule.waitForIdle()
        rule.onNodeWithTag("TestListPane").assertIsDisplayed()
        rule.onNodeWithTag("TestDetail").assertIsDisplayed()
        rule.onNodeWithText("Editing Item 42").assertIsDisplayed()
        rule.onNodeWithTag("TestPlaceholder").assertDoesNotExist()
    }
}
