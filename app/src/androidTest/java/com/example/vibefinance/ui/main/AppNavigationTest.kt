package com.example.vibefinance.ui.main

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.rule.GrantPermissionRule
import android.Manifest

@RunWith(AndroidJUnit4::class)
class AppNavigationTest {

    @get:Rule
    val permissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testBottomNavigationTabSwitching() {
        // Wait for idle to ensure initial load is done
        composeTestRule.waitForIdle()

        // 1. Initially Home (Daily) should be selected and displayed
        composeTestRule.onNodeWithText("Daily", useUnmergedTree = true).assertIsDisplayed()

        // 2. Switch to Assets tab
        composeTestRule.onNodeWithText("Assets", useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Interactive Assets", useUnmergedTree = true).assertIsDisplayed()

        // 3. Switch to History tab
        composeTestRule.onNodeWithText("History", useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Your atmospheric financial journey.", useUnmergedTree = true).assertIsDisplayed()

        // 4. Switch back to Daily tab
        composeTestRule.onNodeWithText("Daily", useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun testQuickLogBottomSheetFormCoexistence() {
        // Wait for idle to ensure initial load is done
        composeTestRule.waitForIdle()

        // Open the Quick Log Bottom Sheet by clicking the "Add Transaction" FAB
        composeTestRule.onNodeWithContentDescription("Add Transaction").performClick()
        composeTestRule.waitForIdle()

        // Verify the bottom sheet is displayed
        composeTestRule.onNodeWithText("內部轉帳 (Internal Transfer)", useUnmergedTree = true).assertIsDisplayed()

        // Find the internal transfer row (which is clickable to toggle)
        val switchNode = composeTestRule.onNodeWithText("內部轉帳 (Internal Transfer)", useUnmergedTree = true)
        
        // Toggle multiple times rapidly to simulate intense micro-interactions
        for (i in 1..5) {
            switchNode.performClick()
            composeTestRule.waitForIdle()
            
            // Assert that the container boundaries stabilize properly without crashing
            // Specifically, when isTransfer is true, "From Account" should be displayed, otherwise "Asset Selection"
            if (i % 2 != 0) {
                composeTestRule.onNodeWithText("From Account", useUnmergedTree = true).assertIsDisplayed()
            } else {
                composeTestRule.onNodeWithText("Asset Selection", useUnmergedTree = true).assertIsDisplayed()
            }
        }
    }
}
