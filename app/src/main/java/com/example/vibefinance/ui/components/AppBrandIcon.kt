package com.example.vibefinance.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.data.entity.InterceptableApp

@Composable
fun AppBrandIcon(
    packageOrAppId: String,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    shapeRadius: Dp = 8.dp
) {
    val context = LocalContext.current
    val knownApp = remember(packageOrAppId) {
        InterceptableApp.fromId(packageOrAppId) ?: InterceptableApp.values().find { it.packageKeywords.contains(packageOrAppId) }
    }

    val installedBitmap = remember(packageOrAppId) {
        val direct = com.example.vibefinance.util.LocalAppManager.getAppIcon(context, packageOrAppId)
        if (direct != null) direct
        else if (knownApp != null) {
            knownApp.packageKeywords.firstNotNullOfOrNull { pkg ->
                com.example.vibefinance.util.LocalAppManager.getAppIcon(context, pkg)
            }
        } else null
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(shapeRadius)),
        contentAlignment = Alignment.Center
    ) {
        if (installedBitmap != null) {
            Image(
                bitmap = installedBitmap.asImageBitmap(),
                contentDescription = knownApp?.displayName ?: packageOrAppId,
                modifier = Modifier.fillMaxSize()
            )
        } else if (knownApp != null) {
            BrandVectorCanvas(app = knownApp)
        } else {
            // Generic App Icon Fallback with Initial
            val label = com.example.vibefinance.util.LocalAppManager.getAppLabel(context, packageOrAppId)
            val initial = label.firstOrNull()?.uppercaseChar()?.toString() ?: "A"
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun AppBrandIcon(
    app: InterceptableApp,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    shapeRadius: Dp = 8.dp
) {
    AppBrandIcon(
        packageOrAppId = app.id,
        modifier = modifier,
        size = size,
        shapeRadius = shapeRadius
    )
}

@Composable
private fun BrandVectorCanvas(app: InterceptableApp) {
    when (app) {
        InterceptableApp.GOOGLE_PAY -> GooglePayLogo()
        InterceptableApp.SAMSUNG_PAY -> SamsungPayLogo()
        InterceptableApp.LINE_PAY -> LinePayLogo()
        InterceptableApp.OCTOPUS -> OctopusCardLogo()
        InterceptableApp.PAYME -> PayMeLogo()
        InterceptableApp.ALIPAY -> AlipayLogo()
        InterceptableApp.WECHAT_PAY -> WeChatPayLogo()
        InterceptableApp.HSBC -> BankBadgeLogo(Color(0xFFDB0011), "HSBC")
        InterceptableApp.HANG_SENG -> BankBadgeLogo(Color(0xFF008559), "恒生")
        InterceptableApp.BOCHK -> BankBadgeLogo(Color(0xFFB71C1C), "中銀")
        InterceptableApp.MOX_BANK -> BankBadgeLogo(Color(0xFF111111), "MOX")
        InterceptableApp.CITIBANK -> BankBadgeLogo(Color(0xFF003B70), "citi")
    }
}

@Composable
private fun BankBadgeLogo(bgColor: Color, label: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp
        )
    }
}

@Composable
private fun GooglePayLogo() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize(0.72f)) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val r = w * 0.44f

            // Google 4-Color 'G' Arc
            val strokeWidth = w * 0.20f

            // Blue: Right top arc to horizontal bar
            drawArc(
                color = Color(0xFF4285F4),
                startAngle = -45f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(cx - r, cy - r),
                size = Size(r * 2, r * 2),
                style = Stroke(width = strokeWidth)
            )

            // Green: Bottom arc
            drawArc(
                color = Color(0xFF34A853),
                startAngle = 45f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(cx - r, cy - r),
                size = Size(r * 2, r * 2),
                style = Stroke(width = strokeWidth)
            )

            // Yellow: Bottom left arc
            drawArc(
                color = Color(0xFFFBBC05),
                startAngle = 135f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(cx - r, cy - r),
                size = Size(r * 2, r * 2),
                style = Stroke(width = strokeWidth)
            )

            // Red: Top left arc
            drawArc(
                color = Color(0xFFEA4335),
                startAngle = 225f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(cx - r, cy - r),
                size = Size(r * 2, r * 2),
                style = Stroke(width = strokeWidth)
            )

            // Blue center bar
            drawRect(
                color = Color(0xFF4285F4),
                topLeft = Offset(cx, cy - strokeWidth / 2f),
                size = Size(r * 0.95f, strokeWidth)
            )
        }
    }
}

@Composable
private fun SamsungPayLogo() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1428A0)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Pay",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 12.sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
            letterSpacing = (-0.5).sp
        )
    }
}

@Composable
private fun LinePayLogo() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF00C300)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "LINE",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 9.sp,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun OctopusCardLogo() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFF7A00)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "八達通",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 8.5.sp,
            letterSpacing = (-0.5).sp
        )
    }
}

@Composable
private fun PayMeLogo() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE60028)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "PayMe",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 8.sp,
            letterSpacing = (-0.3).sp
        )
    }
}

@Composable
private fun AlipayLogo() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1677FF)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "支",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun WeChatPayLogo() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07C160)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "微信",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

private fun drawableToBitmap(drawable: Drawable, width: Int, height: Int): Bitmap? {
    return try {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            drawable.bitmap
        } else {
            val bitmap = Bitmap.createBitmap(
                if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else width,
                if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = AndroidCanvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * Pure Jetpack Compose rendering of the VibeFinance brand emblem.
 * Translates the XML launcher vector (ic_launcher_foreground.xml)
 * into a resolution-independent, hardware-accelerated Compose Canvas drawing.
 */
@Composable
fun VibeFinanceIcon(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    showBackground: Boolean = false,
    shapeRadius: Dp = 8.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .then(
                if (showBackground) {
                    Modifier
                        .clip(RoundedCornerShape(shapeRadius))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF0F172A), Color(0xFF090D16)),
                                start = Offset.Zero,
                                end = Offset.Infinite
                            )
                        )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sx = this.size.width / 108f
            val sy = this.size.height / 108f

            if (showBackground) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x2610B981), Color(0x00042F24)),
                        center = Offset(54f * sx, 54f * sy),
                        radius = 36f * sx
                    ),
                    radius = 36f * sx,
                    center = Offset(54f * sx, 54f * sy)
                )
            }

            // 1. Ambient Shadow under emblem
            val shadowPath = Path().apply {
                moveTo(33f * sx, 37f * sy)
                lineTo(43f * sx, 37f * sy)
                lineTo(54f * sx, 67f * sy)
                lineTo(65f * sx, 37f * sy)
                lineTo(75f * sx, 37f * sy)
                lineTo(54f * sx, 79f * sy)
                close()
            }
            drawPath(path = shadowPath, color = Color(0x33000000))

            // 2. Stylized Geometric 'V' Lettermark
            val vPath = Path().apply {
                moveTo(32f * sx, 35f * sy)
                lineTo(41f * sx, 35f * sy)
                lineTo(54f * sx, 65f * sy)
                lineTo(67f * sx, 35f * sy)
                lineTo(76f * sx, 35f * sy)
                lineTo(54f * sx, 77f * sy)
                close()
            }
            drawPath(
                path = vPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF34D399), Color(0xFF059669)),
                    start = Offset(32f * sx, 35f * sy),
                    end = Offset(76f * sx, 77f * sy)
                )
            )

            // 3. Upward Growth Sparkline Overlay
            val sparklinePath = Path().apply {
                moveTo(30f * sx, 59f * sy)
                lineTo(43f * sx, 45f * sy)
                lineTo(51f * sx, 53f * sy)
                lineTo(65f * sx, 37f * sy)
            }
            drawPath(
                path = sparklinePath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF6EE7B7), Color(0xFF10B981)),
                    start = Offset(30f * sx, 59f * sy),
                    end = Offset(65f * sx, 37f * sy)
                ),
                style = Stroke(
                    width = 5f * sx,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // 4. Glowing Coin / Capital Node
            val coinCenter = Offset(69f * sx, 33f * sy)
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFA7F3D0), Color(0xFF10B981)),
                    start = Offset(61.5f * sx, 25.5f * sy),
                    end = Offset(76.5f * sx, 40.5f * sy)
                ),
                radius = 7.5f * sx,
                center = coinCenter
            )
            drawCircle(
                color = Color(0xFF064E3B),
                radius = 3.5f * sx,
                center = coinCenter
            )
        }
    }
}

