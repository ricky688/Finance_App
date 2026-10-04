package com.example.vibefinance.ui.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.awt.Font
import java.awt.font.FontRenderContext
import java.io.File

class HeatmapRefinementVerificationTest {

    @Test
    fun testR8_HeatmapTileShapeLabelsAreShortened() {
        assertEquals("Squircle", HeatmapTileShape.SQUIRCLE.label)
        assertEquals("Pill", HeatmapTileShape.PEBBLE_PILL.label)
        assertEquals("Glow", HeatmapTileShape.SMOOTH_GLOW.label)
    }

    @Test
    fun testR8_TileShapeSelectorRowWidthBudgetOn360dp() {
        // Load true NotoSans font or fallback to system Font.SANS_SERIF
        val notoFile = File("/usr/share/fonts/noto/NotoSans-Bold.ttf")
        val baseFont = if (notoFile.exists()) {
            Font.createFont(Font.TRUETYPE_FONT, notoFile)
        } else {
            Font(Font.SANS_SERIF, Font.BOLD, 12)
        }

        val font10Bold = baseFont.deriveFont(Font.BOLD, 10f)
        val font12Bold = baseFont.deriveFont(Font.BOLD, 12f)
        val font12Plain = baseFont.deriveFont(Font.PLAIN, 12f)
        val frc = FontRenderContext(null, true, true)

        val tileShapeLabelWidth = font10Bold.getStringBounds("TILE SHAPE", frc).width.toFloat()
        val squircleBold = font12Bold.getStringBounds("Squircle", frc).width.toFloat()
        val squirclePlain = font12Plain.getStringBounds("Squircle", frc).width.toFloat()
        val pillBold = font12Bold.getStringBounds("Pill", frc).width.toFloat()
        val pillPlain = font12Plain.getStringBounds("Pill", frc).width.toFloat()
        val glowBold = font12Bold.getStringBounds("Glow", frc).width.toFloat()
        val glowPlain = font12Plain.getStringBounds("Glow", frc).width.toFloat()

        val chipHorizontalPadding = 16f // 8.dp left + 8.dp right
        val checkmarkFootprint = 16f // 12.dp icon + 4.dp space
        val rowSpacing = 6f // spacedBy(6.dp)

        // Case 1: Squircle selected (default), Pill & Glow unselected
        val rowWidthSquircleSelected = tileShapeLabelWidth + rowSpacing +
                (squircleBold + chipHorizontalPadding + checkmarkFootprint) + rowSpacing +
                (pillPlain + chipHorizontalPadding) + rowSpacing +
                (glowPlain + chipHorizontalPadding)

        // Case 2: Pill selected
        val rowWidthPillSelected = tileShapeLabelWidth + rowSpacing +
                (squirclePlain + chipHorizontalPadding) + rowSpacing +
                (pillBold + chipHorizontalPadding + checkmarkFootprint) + rowSpacing +
                (glowPlain + chipHorizontalPadding)

        // Case 3: Glow selected
        val rowWidthGlowSelected = tileShapeLabelWidth + rowSpacing +
                (squirclePlain + chipHorizontalPadding) + rowSpacing +
                (pillPlain + chipHorizontalPadding) + rowSpacing +
                (glowBold + chipHorizontalPadding + checkmarkFootprint)

        // Available on 360dp screen: 360 - 24 (screen padding) - 32 (card padding) = 304dp
        val availableWidth360 = 360f - 24f - 32f

        println("R8 Widths with True Font Metrics:")
        println(" - Case 1 (Squircle selected): $rowWidthSquircleSelected dp (Headroom: ${availableWidth360 - rowWidthSquircleSelected} dp)")
        println(" - Case 2 (Pill selected): $rowWidthPillSelected dp (Headroom: ${availableWidth360 - rowWidthPillSelected} dp)")
        println(" - Case 3 (Glow selected): $rowWidthGlowSelected dp (Headroom: ${availableWidth360 - rowWidthGlowSelected} dp)")

        assertTrue("Squircle selected must fit within 304dp", rowWidthSquircleSelected <= availableWidth360)
        assertTrue("Pill selected must fit within 304dp", rowWidthPillSelected <= availableWidth360)
        assertTrue("Glow selected must fit within 304dp", rowWidthGlowSelected <= availableWidth360)
    }

    @Test
    fun testR6_HeaderTitleWidthBudgetsAcrossDeviceSizes() {
        val notoFile = File("/usr/share/fonts/noto/NotoSans-Bold.ttf")
        val baseFont = if (notoFile.exists()) {
            Font.createFont(Font.TRUETYPE_FONT, notoFile)
        } else {
            Font(Font.SANS_SERIF, Font.BOLD, 14)
        }

        val font13Bold = baseFont.deriveFont(Font.BOLD, 13f)
        val font14_5Bold = baseFont.deriveFont(Font.BOLD, 14.5f)
        val font10_5Bold = baseFont.deriveFont(Font.BOLD, 10.5f)
        val font10_5Plain = baseFont.deriveFont(Font.PLAIN, 10.5f)
        val frc = FontRenderContext(null, true, true)

        val financialRhythm13Width = font13Bold.getStringBounds("Financial Rhythm", frc).width.toFloat()
        val spendingCalendar13Width = font13Bold.getStringBounds("Spending Calendar", frc).width.toFloat()
        val financialRhythm14_5Width = font14_5Bold.getStringBounds("Financial Rhythm", frc).width.toFloat()
        val spendingCalendar14_5Width = font14_5Bold.getStringBounds("Spending Calendar", frc).width.toFloat()

        val habitBold = font10_5Bold.getStringBounds("Habit", frc).width.toFloat()
        val habitPlain = font10_5Plain.getStringBounds("Habit", frc).width.toFloat()
        val monthBold = font10_5Bold.getStringBounds("Month", frc).width.toFloat()
        val monthPlain = font10_5Plain.getStringBounds("Month", frc).width.toFloat()

        println("\n=== R6 REFINED HEADER TITLE BUDGET ANALYSIS ===")
        println("Title Text Requirements (13sp Bold for <380dp):")
        println(" - 'Financial Rhythm': $financialRhythm13Width dp")
        println(" - 'Spending Calendar': $spendingCalendar13Width dp")
        println("Title Text Requirements (14.5sp Bold for >=380dp):")
        println(" - 'Financial Rhythm': $financialRhythm14_5Width dp")
        println(" - 'Spending Calendar': $spendingCalendar14_5Width dp")

        // Refined ExpressiveDualViewSwitcher Footprint:
        // Outer Surface padding: 2.dp * 2 = 4.dp
        // Inner Row padding: 2.dp * 2 = 4.dp
        // Button spacing: 2.dp
        // Button horizontal padding: 6.dp * 2 = 12.dp per button
        // Mode icon: 13.dp, spacer: 3.dp -> 16.dp
        // Selected mode checkmark: 13.dp icon + 2.dp spacer = 15.dp

        // Heatmap mode: Habit selected (bold + checkmark), Month unselected (plain)
        val habitSelectedBtn = 12f + 15f + 16f + habitBold
        val monthUnselectedBtn = 12f + 0f + 16f + monthPlain
        val switcherWidthHeatmap = 4f + 4f + 2f + habitSelectedBtn + monthUnselectedBtn

        // Calendar mode: Habit unselected (plain), Month selected (bold + checkmark)
        val habitUnselectedBtn = 12f + 0f + 16f + habitPlain
        val monthSelectedBtn = 12f + 15f + 16f + monthBold
        val switcherWidthCalendar = 4f + 4f + 2f + habitUnselectedBtn + monthSelectedBtn

        val titleLeadingFootprint = 20f // Icon 16.dp + Spacer 4.dp
        val titleToSwitcherSpacing = 4f // Spacer 4.dp

        println("Refined Switcher footprint:")
        println(" - Heatmap mode (Habit selected): $switcherWidthHeatmap dp")
        println(" - Calendar mode (Month selected): $switcherWidthCalendar dp")

        val screenBudgets = listOf(360, 392, 412, 480)
        for (sw in screenBudgets) {
            val screenPadding = if (sw < 400) 24f else 32f
            val cardPadding = 32f // 16.dp * 2
            val cardInnerWidth = sw - screenPadding - cardPadding

            val availTextHeatmap = cardInnerWidth - switcherWidthHeatmap - titleToSwitcherSpacing - titleLeadingFootprint
            val availTextCalendar = cardInnerWidth - switcherWidthCalendar - titleToSwitcherSpacing - titleLeadingFootprint

            val reqFinancialRhythm = if (sw < 380) financialRhythm13Width else financialRhythm14_5Width
            val reqSpendingCalendar = if (sw < 380) spendingCalendar13Width else spendingCalendar14_5Width

            val marginHeatmap = availTextHeatmap - reqFinancialRhythm
            val marginCalendar = availTextCalendar - reqSpendingCalendar

            println("\nScreen ${sw}dp Budget:")
            println(" - Card Inner Width: $cardInnerWidth dp")
            println(" - Heatmap Mode: Avail = $availTextHeatmap dp, Required = $reqFinancialRhythm dp, Margin = $marginHeatmap dp")
            println(" - Calendar Mode: Avail = $availTextCalendar dp, Required = $reqSpendingCalendar dp, Margin = $marginCalendar dp")

            // Strict empirical assertion: EVERY screen size must have strictly positive headroom (zero truncation)
            assertTrue("Financial Rhythm must fit on ${sw}dp with positive headroom (margin: $marginHeatmap dp)", marginHeatmap > 0f)
            assertTrue("Spending Calendar must fit on ${sw}dp with positive headroom (margin: $marginCalendar dp)", marginCalendar > 0f)
        }
    }

    @Test
    fun testR2_LeftAlignedStartAndNoAutoScrollInCode() {
        val heatmapFile = File("src/main/java/com/example/vibefinance/ui/home/M3ExpressiveHeatmap.kt")
        val content = heatmapFile.readText()

        // Verify removal of auto-scroll
        assertFalse(
            "scrollTo(scrollState.maxValue) must NOT exist in M3ExpressiveHeatmap.kt",
            content.contains("scrollTo(scrollState.maxValue)")
        )
        assertFalse(
            "animateScrollTo must NOT exist in M3ExpressiveHeatmap.kt",
            content.contains("animateScrollTo")
        )

        // Verify initial scroll state is rememberScrollState() without non-zero offset
        assertTrue(
            "rememberScrollState() must be present",
            content.contains("val scrollState = rememberScrollState()")
        )

        // Verify left-edge gradient fade is conditional on scrollState.value > 0
        assertTrue(
            "drawWithContent must check scrollState.value > 0 before drawing left fade",
            content.contains("if (scrollState.value > 0)")
        )
    }

    @Test
    fun testR5_NoBouncySpringPhysicsInViewSwitcher() {
        val calendarFile = File("src/main/java/com/example/vibefinance/ui/home/SpendsCalendar.kt")
        val content = calendarFile.readText()

        // Extract ExpressiveDualViewSwitcher section
        val switcherStart = content.indexOf("fun ExpressiveDualViewSwitcher")
        val switcherEnd = content.indexOf("fun SpendsCalendar")
        val switcherCode = content.substring(switcherStart, switcherEnd)

        assertFalse(
            "ExpressiveDualViewSwitcher must NOT contain DampingRatioMediumBouncy",
            switcherCode.contains("Spring.DampingRatioMediumBouncy")
        )

        // Extract AnimatedContent transitionSpec in SpendsCalendar
        val animContentStart = content.indexOf("AnimatedContent(")
        val animContentEnd = content.indexOf("CalendarDisplayMode.HEATMAP ->")
        val animContentCode = content.substring(animContentStart, animContentEnd)

        assertFalse(
            "AnimatedContent transitionSpec must NOT contain DampingRatioMediumBouncy",
            animContentCode.contains("Spring.DampingRatioMediumBouncy")
        )
        assertTrue(
            "AnimatedContent transitionSpec must use Spring.DampingRatioNoBouncy",
            animContentCode.contains("Spring.DampingRatioNoBouncy")
        )
    }

    @Test
    fun testR6_SpendsCalendarCodeInspection() {
        val calendarFile = File("src/main/java/com/example/vibefinance/ui/home/SpendsCalendar.kt")
        val content = calendarFile.readText()

        // 1. Verify responsive font scaling
        assertTrue(
            "LocalConfiguration screenWidthDp must be accessed",
            content.contains("LocalConfiguration.current.screenWidthDp")
        )
        assertTrue(
            "Responsive titleFontSize scaling condition must check < 380",
            content.contains("if (screenWidthDp < 380) 13.sp else 14.5.sp")
        )
        assertTrue(
            "Title text style must use titleFontSize",
            content.contains("style = MaterialTheme.typography.titleMedium.copy(fontSize = titleFontSize)")
        )

        // 2. Verify compacted ExpressiveDualViewSwitcher padding (6dp), spacers (2dp/3dp), icon (13dp), font (10.5sp)
        val switcherStart = content.indexOf("fun ExpressiveDualViewSwitcher")
        val switcherEnd = content.indexOf("fun SpendsCalendar")
        val switcherCode = content.substring(switcherStart, switcherEnd)

        assertTrue(
            "Switcher button padding must be horizontal = 6.dp",
            switcherCode.contains(".padding(horizontal = 6.dp, vertical = 5.dp)")
        )
        assertTrue(
            "Checkmark icon size must be 13.dp",
            switcherCode.contains("Icons.Filled.Check") && switcherCode.contains("modifier = Modifier.size(13.dp)")
        )
        assertTrue(
            "Checkmark trailing spacer must be 2.dp",
            switcherCode.contains("Spacer(modifier = Modifier.width(2.dp))")
        )
        assertTrue(
            "Mode icon trailing spacer must be 3.dp",
            switcherCode.contains("Spacer(modifier = Modifier.width(3.dp))")
        )
        assertTrue(
            "Switcher button font size must be 10.5.sp",
            switcherCode.contains("fontSize = 10.5.sp")
        )

        // 3. Verify title icon (16dp) and spacers (4dp)
        val headerStart = content.indexOf("fun SpendsCalendar")
        val headerEnd = content.indexOf("AnimatedContent(")
        val headerCode = content.substring(headerStart, headerEnd)

        assertTrue(
            "Header title icon size must be 16.dp",
            headerCode.contains("modifier = Modifier.size(16.dp)")
        )
        assertTrue(
            "Header icon spacer must be 4.dp",
            headerCode.contains("Spacer(modifier = Modifier.width(4.dp))")
        )
        assertTrue(
            "Spacer between Title and Switcher must be 4.dp",
            headerCode.contains("Spacer(modifier = Modifier.width(4.dp))")
        )
    }
}
