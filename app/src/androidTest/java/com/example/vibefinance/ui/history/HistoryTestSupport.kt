package com.example.vibefinance.ui.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.example.vibefinance.ui.main.LocalMainWindowWidth

/**
 * Pins History to a phone-width window (411dp), so tests exercise the phone flow — detail dialog,
 * then Edit — whatever the test device's real width is. Wide windows open the list-detail pane instead.
 */
internal fun ComposeContentTestRule.setPhoneContent(content: @Composable () -> Unit) = setContent {
    CompositionLocalProvider(LocalMainWindowWidth provides 411.dp) { content() }
}

/** Opens a record the way a phone user does: tap the row, then Edit in the detail dialog. */
internal fun ComposeTestRule.openTransactionEditor(rowTag: String) {
    onNodeWithTag(rowTag).performClick()
    waitUntil(5_000) { onAllNodes(androidx.compose.ui.test.hasTestTag("DetailEditButton")).fetchSemanticsNodes().isNotEmpty() }
    onNodeWithTag("DetailEditButton").performClick()
    waitUntil(5_000) { onAllNodes(androidx.compose.ui.test.hasTestTag("EditTransactionAmount")).fetchSemanticsNodes().isNotEmpty() }
}
