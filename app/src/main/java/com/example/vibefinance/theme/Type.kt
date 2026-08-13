package com.example.vibefinance.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Google Fonts with system fallback configurations to ensure robust compilation
val Inter = FontFamily.SansSerif
val JetBrainsMono = FontFamily.Monospace

val Typography = Typography(
    // display-lg (42sp, JetBrains Mono, bold)
    displayLarge = TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Bold,
        fontSize = 42.sp,
        lineHeight = 52.sp,
        letterSpacing = (-0.02).sp
    ),
    // display-lg-mobile (32sp, JetBrains Mono, bold)
    displayMedium = TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.02).sp
    ),
    // headline-md (24sp, Inter, SemiBold)
    headlineMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    // title-lg (20sp, Inter, Medium)
    titleLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    // body-lg (16sp, Inter, Normal)
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    // body-md (14sp, Inter, Normal)
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    // label-mono (12sp, JetBrains Mono, Medium)
    labelSmall = TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.05.sp
    )
)

