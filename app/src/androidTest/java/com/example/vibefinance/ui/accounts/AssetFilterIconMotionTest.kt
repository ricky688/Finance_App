package com.example.vibefinance.ui.accounts

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.ui.preferences.ExperiencePreferences
import com.example.vibefinance.ui.preferences.LocalExperience
import com.example.vibefinance.ui.preferences.MotionLevel
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger

/** Category filter icons explored with ARTEMIS and ADB before authoring this regression. */
@RunWith(AndroidJUnit4::class)
class AssetFilterIconMotionTest {
    @get:Rule val rule = createComposeRule()

    private var selectedFilter by mutableStateOf(AssetFilter.ALL)
    private var motionLevel by mutableStateOf(MotionLevel.FULL)
    private val parentCompositions = AtomicInteger()

    private val fixtureAccounts = listOf(
        AccountEntity(id = 1, name = "Bank 1", type = AccountType.BANK, balance = 1000.0, icon = "bank"),
        AccountEntity(id = 2, name = "Debit 1", type = AccountType.DEBIT, balance = 200.0, icon = "card"),
        AccountEntity(id = 3, name = "Credit 1", type = AccountType.CC, balance = 50.0, icon = "cc")
    )

    private fun show() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            CompositionLocalProvider(
                LocalExperience provides ExperiencePreferences(motion = motionLevel)
            ) {
                MaterialTheme(colorScheme = lightColorScheme()) {
                    SideEffect { parentCompositions.incrementAndGet() }
                    AssetGroupSelector(
                        accounts = fixtureAccounts,
                        selectedFilter = selectedFilter,
                        onSelectFilter = { selectedFilter = it },
                        modifier = Modifier.testTag("AssetGroupSelector")
                    )
                }
            }
        }
        advance(64)
    }

    private fun advance(ms: Long) {
        rule.mainClock.advanceTimeBy(ms)
        rule.waitForIdle()
    }

    @Test
    fun selection_triggersMicroLiftAndSettlesAtSelectedScale() {
        show()
        val initialCompositions = parentCompositions.get()

        // Click on Debit Cards filter to trigger selection transition
        rule.onNodeWithTag("assetFilterIcon_DEBIT_CARDS", useUnmergedTree = true)
            .assertExists()
            .performClick()

        // Advance 40ms into the micro-bounce animation
        advance(40)

        // Exactly one recomposition occurred for the selection state update
        assertEquals(initialCompositions + 1, parentCompositions.get())
        val compositionsDuringMotion = parentCompositions.get()

        // Advance further into the animation
        advance(120)

        // Verify zero parent recompositions during frame animation (graphicsLayer optimization)
        assertEquals(compositionsDuringMotion, parentCompositions.get())

        // Advance past completion
        advance(500)

        // Selected filter state updated and icon remains visible and healthy
        assertEquals(AssetFilter.DEBIT_CARDS, selectedFilter)
        rule.onNodeWithTag("assetFilterIcon_DEBIT_CARDS", useUnmergedTree = true)
            .assertIsDisplayed()
        rule.onNodeWithTag("assetFilterIcon_ALL", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun minimalMotion_respectsPreference() {
        motionLevel = MotionLevel.MINIMAL
        show()

        rule.onNodeWithTag("assetFilterIcon_CREDIT_CARDS", useUnmergedTree = true)
            .performClick()
        advance(16)

        assertEquals(AssetFilter.CREDIT_CARDS, selectedFilter)
        rule.onNodeWithTag("assetFilterIcon_CREDIT_CARDS", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun reselectingAlreadySelectedFilter_replaysMicroLift() {
        show()
        // ALL is already selected initially
        assertEquals(AssetFilter.ALL, selectedFilter)
        val initialCompositions = parentCompositions.get()

        // Click already selected filter
        rule.onNodeWithTag("assetFilterIcon_ALL", useUnmergedTree = true)
            .assertExists()
            .performClick()

        advance(40)
        // Zero parent recomposition during replay
        assertEquals(initialCompositions, parentCompositions.get())

        advance(500)
        assertEquals(AssetFilter.ALL, selectedFilter)
        rule.onNodeWithTag("assetFilterIcon_ALL", useUnmergedTree = true)
            .assertIsDisplayed()
    }
}
