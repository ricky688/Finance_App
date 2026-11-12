package com.example.vibefinance.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.preferences.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Transfer Done → native source sheet explored with ARTEMIS first; all records are memory-only. */
class AccountPickerScrollEdgesTest {
    @get:Rule val rule = createComposeRule()

    private fun showGrid(count: Int = 40, blur: Float = 1f, dark: Boolean = false) {
        rule.setContent {
            CompositionLocalProvider(LocalExperience provides ExperiencePreferences(blur = blur)) {
                MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                    Column(Modifier.width(280.dp).background(MaterialTheme.colorScheme.surface)) {
                        Text("Header", Modifier.testTag("PickerHeaderFixture"))
                        val state = rememberLazyGridState()
                        AccountPickerScrollEdges(state, Modifier.fillMaxWidth().height(220.dp)) { source ->
                            LazyVerticalGrid(GridCells.Fixed(2), state = state,
                                modifier = source.fillMaxSize().testTag("PickerTestGrid")) {
                                items(count) { Box(Modifier.fillMaxWidth().height(80.dp).background(Color.Red)) }
                            }
                        }
                        Text("Actions", Modifier.testTag("PickerActionsFixture"))
                    }
                }
            }
        }
    }

    @Test fun overflowingEdgesFadeAndBlurThenClearAtReachedLimits() {
        showGrid()
        rule.waitUntil(5000) { rule.onAllNodesWithTag("AccountPicker_end_blur").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("AccountPicker_start_fade").assertDoesNotExist()
        rule.onNodeWithTag("AccountPicker_end_fade").assertExists()
        val header = rule.onNodeWithTag("PickerHeaderFixture").getUnclippedBoundsInRoot()
        val actions = rule.onNodeWithTag("PickerActionsFixture").getUnclippedBoundsInRoot()
        val pixels = rule.onNodeWithTag("AccountPickerScrollViewport").captureToImage().toPixelMap()
        val center = pixels[pixels.width / 4, pixels.height / 2]
        val edge = pixels[pixels.width / 4, pixels.height - 1]
        assertTrue("The clipped scroll edge must visibly fade", edge.alpha < center.alpha - 0.2f || edge.green > center.green + 0.2f)
        rule.onNodeWithTag("PickerTestGrid").performScrollToIndex(20)
        rule.onNodeWithTag("AccountPicker_start_blur").assertExists()
        rule.onNodeWithTag("AccountPicker_end_blur").assertExists()
        rule.onNodeWithTag("PickerTestGrid").performScrollToIndex(39)
        rule.onNodeWithTag("AccountPicker_start_fade").assertExists()
        rule.onNodeWithTag("AccountPicker_end_fade").assertDoesNotExist()
        rule.onNodeWithTag("AccountPicker_end_blur").assertDoesNotExist()
        assertEquals(header, rule.onNodeWithTag("PickerHeaderFixture").getUnclippedBoundsInRoot())
        assertEquals(actions, rule.onNodeWithTag("PickerActionsFixture").getUnclippedBoundsInRoot())
        rule.onNodeWithTag("PickerTestGrid").performScrollToIndex(0)
        rule.onNodeWithTag("AccountPicker_start_blur").assertDoesNotExist()
        rule.onNodeWithTag("AccountPicker_start_fade").assertDoesNotExist()
    }

    @Test fun blurOffRetainsOnlyTheFadeInDarkTheme() {
        showGrid(blur = 0f, dark = true)
        rule.onNodeWithTag("AccountPicker_end_fade").assertExists()
        rule.onNodeWithTag("AccountPicker_end_blur").assertDoesNotExist()
        rule.onNodeWithTag("PickerTestGrid").performScrollToIndex(20)
        rule.onNodeWithTag("AccountPicker_start_fade").assertExists()
        rule.onNodeWithTag("AccountPicker_start_blur").assertDoesNotExist()
    }

    @Test fun shortListHasNoFadeOrBlur() {
        showGrid(count = 2)
        listOf("start_fade", "end_fade", "start_blur", "end_blur").forEach {
            rule.onNodeWithTag("AccountPicker_$it").assertDoesNotExist()
        }
    }

    @Test fun allThreeModesAndTransferDestinationUseTheSameBoundedSheetEffects() {
        val accounts = (1L..40L).map {
            AccountEntity(it, "Bank $it", type = AccountType.BANK, balance = 1000.0, icon = "bank")
        }
        rule.setContent {
            MaterialTheme {
                TransactionEntryHost(true, FinanceUiState(isLoading = false, accounts = accounts),
                    onIntent = { error("Fixture must not persist") }, onDismiss = {},
                    initialMode = TransactionMode.EXPENSE, categoryFrequency = emptyMap(), accountFrequency = emptyMap())
            }
        }
        fun checkSheet(card: String, destination: Boolean = false) {
            rule.onNodeWithTag(card).performScrollTo().performClick()
            rule.waitUntil(5000) { rule.onAllNodesWithTag("AccountPicker_end_blur").fetchSemanticsNodes().isNotEmpty() }
            val header = rule.onNodeWithTag("AccountPickerHeader").getUnclippedBoundsInRoot()
            val add = rule.onNodeWithTag("AccountPicker_AddAccountToggle").getUnclippedBoundsInRoot()
            if (destination) rule.onNodeWithTag("EntryDestination_row_1").assertDoesNotExist()
            rule.onNodeWithTag("AccountPickerGrid").performScrollToIndex(20)
            rule.onNodeWithTag("AccountPicker_start_blur").assertExists()
            assertEquals(header, rule.onNodeWithTag("AccountPickerHeader").getUnclippedBoundsInRoot())
            assertEquals(add, rule.onNodeWithTag("AccountPicker_AddAccountToggle").getUnclippedBoundsInRoot())
            rule.onNodeWithContentDescription("Close").performClick()
            rule.waitUntil(5000) { rule.onAllNodesWithTag("AccountPickerGrid").fetchSemanticsNodes().isEmpty() }
        }
        TransactionMode.entries.forEach { mode ->
            rule.onNodeWithTag("TransactionMode_${mode.name}").performScrollTo().performClick()
            checkSheet("EntryAccount_card")
            if (mode == TransactionMode.TRANSFER) checkSheet("EntryDestination_card", destination = true)
        }
    }
}
