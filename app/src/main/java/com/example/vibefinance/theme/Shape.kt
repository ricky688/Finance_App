package com.example.vibefinance.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive Shape Scale.
 * Centralized expressive corner tokens for containers, cards, dialogs, and sheets.
 */
val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// Expressive Bento Grid & Container Shapes
val BentoCardShape = RoundedCornerShape(24.dp)
val BentoSubCardShape = RoundedCornerShape(20.dp)
val BentoSmallCardShape = RoundedCornerShape(16.dp)
val HeroCardShape = RoundedCornerShape(28.dp)
val PillShape = CircleShape
val ExpressivePillShape = RoundedCornerShape(50)
val ExpressiveSheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
val FloatingBarShape = RoundedCornerShape(32.dp)

/**
 * Multi-lobed expressive rosette/scallop shape used for Image Toolbox-style
 * icons, flower badges, and theme preview swatches.
 */
class RosetteShape(
    val lobes: Int = 12,
    val depth: Float = 0.10f
) : androidx.compose.ui.graphics.Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): androidx.compose.ui.graphics.Outline {
        val path = androidx.compose.ui.graphics.Path()
        val cx = size.width / 2f
        val cy = size.height / 2f
        val maxR = kotlin.math.min(cx, cy)
        if (maxR <= 0f) return androidx.compose.ui.graphics.Outline.Generic(path)
        val minR = maxR * (1f - depth.coerceIn(0.02f, 0.45f))
        val totalSamples = lobes * 12
        val points = ArrayList<androidx.compose.ui.geometry.Offset>(totalSamples)

        for (i in 0 until totalSamples) {
            val angle = (i * 2.0 * Math.PI / totalSamples).toFloat()
            val wave = ((kotlin.math.cos(lobes * angle.toDouble()) + 1.0) / 2.0).toFloat()
            val r = minR + (maxR - minR) * wave
            val x = cx + r * kotlin.math.cos(angle.toDouble()).toFloat()
            val y = cy + r * kotlin.math.sin(angle.toDouble()).toFloat()
            points.add(androidx.compose.ui.geometry.Offset(x, y))
        }

        path.moveTo(points[0].x, points[0].y)
        for (i in 0 until totalSamples) {
            val curr = points[i]
            val next = points[(i + 1) % totalSamples]
            val midX = (curr.x + next.x) / 2f
            val midY = (curr.y + next.y) / 2f
            path.quadraticTo(curr.x, curr.y, midX, midY)
        }
        path.close()
        return androidx.compose.ui.graphics.Outline.Generic(path)
    }
}

val CloverIconShape = RosetteShape(lobes = 4, depth = 0.16f)
val ScallopBadgeShape = RosetteShape(lobes = 12, depth = 0.10f)
val FlowerIconShape = RosetteShape(lobes = 8, depth = 0.14f)
val StarIconShape = RosetteShape(lobes = 8, depth = 0.28f)
val SquircleIconShape = RoundedCornerShape(32)
val DiamondIconShape = androidx.compose.foundation.shape.CutCornerShape(32)

/**
 * Supported container shape modes for leading card/item icons,
 * inspired by Image Toolbox shape personalization.
 */
enum class IconShapeMode(
    val titleZh: String,
    val subtitleZh: String,
    val titleEn: String,
    val subtitleEn: String
) {
    CLOVER("四葉草", "經典多葉柔和花瓣輪廓", "Clover", "Classic multi-lobed petal contour"),
    SQUIRCLE("平滑方角", "現代超橢圓柔和曲線", "Squircle", "Modern superellipse smooth curve"),
    ROUNDED_SQUARE("圓角矩形", "對稱圓弧邊角容器", "Rounded Square", "Symmetric rounded corner container"),
    CIRCLE("圓形", "完全對稱經典正圓形", "Circle", "Fully symmetric classic circle"),
    SCALLOP("十二瓣花", "細緻十二波浪扇形花邊", "Scallop", "Delicate twelve-wave scalloped edge"),
    FLOWER("八葉花", "八瓣對稱盛開花朵造型", "Flower", "Eight-lobed blossoming flower design"),
    DIAMOND("寶石菱形", "幾何切角晶體多邊形", "Diamond", "Geometric cut crystal polygon"),
    STAR("八角星芒", "璀璨立體幾何星形徽章", "Star", "Radiant geometric star badge");

    val title: String get() = titleZh
    val subtitle: String get() = subtitleZh

    fun localizedTitle(isZh: Boolean = true): String = if (isZh) titleZh else titleEn
    fun localizedSubtitle(isZh: Boolean = true): String = if (isZh) subtitleZh else subtitleEn

    val shape: androidx.compose.ui.graphics.Shape
        get() = when (this) {
            CLOVER -> CloverIconShape
            SQUIRCLE -> SquircleIconShape
            ROUNDED_SQUARE -> RoundedCornerShape(14.dp)
            CIRCLE -> CircleShape
            SCALLOP -> ScallopBadgeShape
            FLOWER -> FlowerIconShape
            DIAMOND -> DiamondIconShape
            STAR -> StarIconShape
        }
}

val LocalIconShape = androidx.compose.runtime.compositionLocalOf<androidx.compose.ui.graphics.Shape> { CloverIconShape }
