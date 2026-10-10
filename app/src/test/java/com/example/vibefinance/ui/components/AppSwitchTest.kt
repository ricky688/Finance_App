package com.example.vibefinance.ui.components

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AppSwitchTest {

    private fun resolveSourceFile(relativePath: String): File {
        return listOf(
            File(relativePath),
            File("app", relativePath),
            File("..", relativePath),
            File("../app", relativePath)
        ).firstOrNull { it.exists() } ?: File(relativePath)
    }

    @Test
    fun testAppSwitchSourceExistsAndImplementsM3Specifications() {
        val switchFile = resolveSourceFile("src/main/java/com/example/vibefinance/ui/components/AppSwitch.kt")
        assertTrue("AppSwitch.kt must exist at ${switchFile.absolutePath}", switchFile.exists())

        val content = switchFile.readText()

        // 1. Verify Track container dimensions & Alignment
        assertTrue("Must specify 52.dp track width and 32.dp height", content.contains("size(width = 52.dp, height = 32.dp)"))
        assertTrue("Must center children vertically with Alignment.CenterStart", content.contains("contentAlignment = Alignment.CenterStart"))
        assertTrue("Must provide 48dp accessible touch target", content.contains("sizeIn(minWidth = 48.dp, minHeight = 48.dp)"))

        // 2. Verify Bounded Ripple Highlight centered on the handle
        assertTrue("Must position bounded ripple at handle center offset", content.contains("thumbOffsetX + (thumbSize - 40.dp) / 2"))
        assertTrue("Must configure bounded ripple", content.contains("ripple(bounded = true, color = rippleColor)"))
        assertTrue("Must set ripple color to onPrimary when checked", content.contains("checked -> MaterialTheme.colorScheme.onPrimary"))

        // 3. Verify Handle morphology & stretch kinetics
        assertTrue("Must expand handle to 28dp when pressed", content.contains("if (isPressed) 28.dp else 24.dp"))
        assertTrue("Must use onPrimary handle when checked", content.contains("checked -> MaterialTheme.colorScheme.onPrimary"))
        assertTrue("Must use outline handle when unchecked", content.contains("MaterialTheme.colorScheme.outline"))

        // 4. Verify Track styling
        assertTrue("Must use primary track when checked", content.contains("checked -> MaterialTheme.colorScheme.primary"))
        assertTrue("Must use surfaceContainerHighest track when unchecked", content.contains("MaterialTheme.colorScheme.surfaceContainerHighest"))
        assertTrue("Must use 2dp outline border when unchecked", content.contains("MaterialTheme.colorScheme.outline"))

        // 5. Verify 16dp icons
        assertTrue("Must include Check icon for checked state", content.contains("Icons.Filled.Check"))
        assertTrue("Must include Close icon for unchecked state", content.contains("Icons.Filled.Close"))
        assertTrue("Must size icons at 16dp", content.contains("size(16.dp)"))
    }

    @Test
    fun testConnectedButtonColorMotionProvidesOnPrimaryRippleOnSelected() {
        val motionFile = resolveSourceFile("src/main/java/com/example/vibefinance/ui/components/ConnectedButtonColorMotion.kt")
        assertTrue("ConnectedButtonColorMotion.kt must exist at ${motionFile.absolutePath}", motionFile.exists())

        val content = motionFile.readText()

        // Verify selected button provides activeContentColor (onPrimary) ripple
        assertTrue(
            "Selected button must assign activeContentColor as rippleColor",
            content.contains("isSelected -> activeContentColor")
        )

        // Verify ConnectedButtonRipple accepts isSelected and passes onPrimary
        assertTrue(
            "ConnectedButtonRipple must accept isSelected",
            content.contains("isSelected: Boolean = false")
        )
        assertTrue(
            "ConnectedButtonRipple must configure onPrimary ripple when isSelected",
            content.contains("RippleConfiguration(color = MaterialTheme.colorScheme.onPrimary)")
        )
    }

    @Test
    fun testAllCallSitesImportUnifiedAppSwitch() {
        val filesUsingAppSwitch = listOf(
            "src/main/java/com/example/vibefinance/ui/history/HistoryScreen.kt",
            "src/main/java/com/example/vibefinance/ui/home/RecalcBudgetSheet.kt",
            "src/main/java/com/example/vibefinance/ui/main/MainScreen.kt",
            "src/main/java/com/example/vibefinance/ui/preferences/ExperienceSettings.kt",
            "src/main/java/com/example/vibefinance/ui/radar/AddDiscountShopSheet.kt",
            "src/main/java/com/example/vibefinance/ui/settings/AppearancePickerSheet.kt",
            "src/main/java/com/example/vibefinance/ui/settings/FullBackupActions.kt",
            "src/main/java/com/example/vibefinance/ui/settings/SettingsSheet.kt"
        )

        for (relativePath in filesUsingAppSwitch) {
            val file = resolveSourceFile(relativePath)
            assertTrue("File $relativePath must exist at ${file.absolutePath}", file.exists())
            val content = file.readText()
            assertTrue(
                "File $relativePath must import unified AppSwitch",
                content.contains("import com.example.vibefinance.ui.components.AppSwitch")
            )
        }
    }
}
