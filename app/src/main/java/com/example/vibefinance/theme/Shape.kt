@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon

/** Material corner tokens apply to UI containers, separately from the library's icon contours. */
object MaterialCornerScale {
    val large = RoundedCornerShape(16.dp)
    val largeIncreased = RoundedCornerShape(20.dp)
    val extraLarge = RoundedCornerShape(28.dp)
    val extraLargeIncreased = RoundedCornerShape(32.dp)
    val extraExtraLarge = RoundedCornerShape(48.dp)
    val full = CircleShape
}

val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = MaterialCornerScale.large,
    largeIncreased = MaterialCornerScale.largeIncreased,
    extraLarge = MaterialCornerScale.extraLarge,
    extraLargeIncreased = MaterialCornerScale.extraLargeIncreased,
    extraExtraLarge = MaterialCornerScale.extraExtraLarge
)

val BentoCardShape = MaterialCornerScale.extraLarge
val BentoSubCardShape = MaterialCornerScale.largeIncreased
val BentoSmallCardShape = MaterialCornerScale.large
val HeroCardShape = MaterialCornerScale.extraLarge
val PillShape = MaterialCornerScale.full
val ExpressivePillShape = MaterialCornerScale.full
val ExpressiveSheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
val FloatingBarShape = MaterialCornerScale.extraLargeIncreased

// Palette swatches also use the official shape rather than a hand-drawn rosette.
val ScallopBadgeShape: Shape by lazy { MaterialIconShape(MaterialShapes.Cookie12Sided) }

/**
 * Official Material Shape Library silhouettes, in the reference's order.
 * Burst through Puffy diamond and the two pixel shapes are intentionally excluded.
 * All contour geometry comes from androidx.compose.material3.MaterialShapes.
 */
enum class IconShapeMode(
    val titleZh: String,
    val subtitleZh: String,
    val titleEn: String,
    val subtitleEn: String
) {
    CIRCLE("圓形", "經典正圓形", "Circle", "Classic circle"),
    SQUARE("方形", "圓潤方形輪廓", "Square", "Rounded square contour"),
    SLANTED("斜方形", "圓角傾斜方形", "Slanted", "Rounded slanted square"),
    ARCH("拱形", "圓頂平底輪廓", "Arch", "Rounded top with a flat base"),
    SEMICIRCLE("半圓", "柔和半圓輪廓", "Semicircle", "Soft semicircle contour"),
    OVAL("橢圓", "傾斜橢圓輪廓", "Oval", "Tilted oval contour"),
    PILL("膠囊", "圓潤膠囊輪廓", "Pill", "Rounded capsule contour"),
    TRIANGLE("三角形", "圓角三角形輪廓", "Triangle", "Rounded triangle contour"),
    ARROW("箭頭", "柔和箭頭輪廓", "Arrow", "Soft arrow contour"),
    FAN("扇形", "柔和扇形輪廓", "Fan", "Soft fan contour"),
    DIAMOND("菱形", "圓角菱形輪廓", "Diamond", "Rounded diamond contour"),
    CLAMSHELL("貝殼", "圓潤貝殼輪廓", "Clamshell", "Rounded shell contour"),
    PENTAGON("五邊形", "圓角五邊形輪廓", "Pentagon", "Rounded pentagonal contour"),
    GEM("寶石", "圓潤寶石輪廓", "Gem", "Rounded gem contour"),
    SUNNY("陽光", "柔和陽光輪廓", "Sunny", "Soft sunny contour"),
    VERY_SUNNY("燦爛陽光", "柔和陽光花邊", "Very sunny", "Soft sunny scallops"),
    COOKIE_4("四瓣餅乾", "柔和四瓣花邊", "4-sided cookie", "Soft four-lobed edge"),
    COOKIE_6("六瓣餅乾", "柔和六瓣花邊", "6-sided cookie", "Soft six-lobed edge"),
    COOKIE_7("七瓣餅乾", "柔和七瓣花邊", "7-sided cookie", "Soft seven-lobed edge"),
    COOKIE_9("九瓣餅乾", "柔和九瓣花邊", "9-sided cookie", "Soft nine-lobed edge"),
    COOKIE_12("十二瓣餅乾", "柔和十二瓣花邊", "12-sided cookie", "Soft twelve-lobed edge"),
    CLOVER_4("四葉草", "四片圓潤花瓣", "4-leaf clover", "Four rounded petals"),
    CLOVER_8("八葉草", "八片圓潤花瓣", "8-leaf clover", "Eight rounded petals"),
    GHOST("幽靈", "圓頂波浪底部", "Ghost-ish", "Rounded top with a wavy base"),
    BUN("麵包", "圓潤波浪輪廓", "Bun", "Rounded wavy contour"),
    RANDOM("隨機圓角", "每個圖示使用不同圓潤形狀", "Random rounded", "A different rounded shape for each icon");

    val title: String get() = titleZh
    val subtitle: String get() = subtitleZh
    fun localizedTitle(isZh: Boolean = true): String = if (isZh) titleZh else titleEn
    fun localizedSubtitle(isZh: Boolean = true): String = if (isZh) subtitleZh else subtitleEn

    internal val materialShape: RoundedPolygon by lazy {
        when (this) {
            CIRCLE -> MaterialShapes.Circle
            SQUARE -> MaterialShapes.Square
            SLANTED -> MaterialShapes.Slanted
            ARCH -> MaterialShapes.Arch
            SEMICIRCLE -> MaterialShapes.SemiCircle
            OVAL -> MaterialShapes.Oval
            PILL -> MaterialShapes.Pill
            TRIANGLE -> MaterialShapes.Triangle
            ARROW -> MaterialShapes.Arrow
            FAN -> MaterialShapes.Fan
            DIAMOND -> MaterialShapes.Diamond
            CLAMSHELL -> MaterialShapes.ClamShell
            PENTAGON -> MaterialShapes.Pentagon
            GEM -> MaterialShapes.Gem
            SUNNY -> MaterialShapes.Sunny
            VERY_SUNNY -> MaterialShapes.VerySunny
            COOKIE_4, RANDOM -> MaterialShapes.Cookie4Sided
            COOKIE_6 -> MaterialShapes.Cookie6Sided
            COOKIE_7 -> MaterialShapes.Cookie7Sided
            COOKIE_9 -> MaterialShapes.Cookie9Sided
            COOKIE_12 -> MaterialShapes.Cookie12Sided
            CLOVER_4 -> MaterialShapes.Clover4Leaf
            CLOVER_8 -> MaterialShapes.Clover8Leaf
            GHOST -> MaterialShapes.Ghostish
            BUN -> MaterialShapes.Bun
        }
    }

    val shape: Shape by lazy { MaterialIconShape(materialShape) }

    companion object {
        val roundedModes: List<IconShapeMode> = entries.filter { it != RANDOM }

        /** Keep settings saved by earlier app versions valid without reintroducing excluded shapes. */
        fun fromStoredName(name: String?): IconShapeMode = entries.firstOrNull { it.name == name }
            ?: when (name) {
                "SQUIRCLE", "ROUNDED_SQUARE" -> SQUARE
                "SCALLOP" -> COOKIE_12
                "ROUNDED_HEXAGON" -> CLAMSHELL
                "EGG" -> OVAL
                "SHIELD" -> ARCH
                "HEART" -> COOKIE_4
                else -> COOKIE_4 // Includes legacy Clover and all removed decorative shapes.
            }

        /** Deterministic pseudo-random assignment survives redraws, scrolling and restarts. */
        fun roundedModeFor(key: String): IconShapeMode {
            val hash = key.fold(0x811c9dc5u) { value, char ->
                (value xor char.code.toUInt()) * 0x01000193u
            }
            return roundedModes[(hash % roundedModes.size.toUInt()).toInt()]
        }
    }
}

/** Render the library's unmodified cubic geometry; this adapter defines no custom contours. */
private class MaterialIconShape(polygon: RoundedPolygon) : Shape {
    private val normalizedPath = Path().apply {
        val cubics = polygon.cubics
        moveTo(cubics.first().anchor0X, cubics.first().anchor0Y)
        cubics.forEach { cubic ->
            cubicTo(cubic.control0X, cubic.control0Y, cubic.control1X, cubic.control1Y,
                cubic.anchor1X, cubic.anchor1Y)
        }
        close()
    }

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            addPath(normalizedPath)
            transform(Matrix().apply { scale(size.width, size.height) })
            val bounds = getBounds()
            translate(Offset(size.width / 2f - bounds.center.x, size.height / 2f - bounds.center.y))
        }
        return Outline.Generic(path)
    }
}

val LocalIconShape = compositionLocalOf<Shape> { IconShapeMode.COOKIE_4.shape }
val LocalRandomIconShapes = compositionLocalOf { false }

/** Use a stable icon/item key so a random shape stays attached to the same icon. */
@Composable
fun rememberIconShape(key: String): Shape {
    val fixedShape = LocalIconShape.current
    val random = LocalRandomIconShapes.current
    return remember(fixedShape, random, key) {
        if (random) IconShapeMode.roundedModeFor(key).shape else fixedShape
    }
}
