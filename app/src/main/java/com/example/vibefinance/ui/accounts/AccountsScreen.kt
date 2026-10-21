@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.accounts

import androidx.compose.material.icons.automirrored.filled.TrendingUp
import com.example.vibefinance.ui.common.ContentEntranceViewport
import com.example.vibefinance.ui.common.PageContentEntrance

import com.example.vibefinance.ui.components.CompletePressButton
import com.example.vibefinance.ui.components.CompletePressFilledTonalButton
import com.example.vibefinance.ui.components.CompletePressTextButton
import com.example.vibefinance.ui.components.CompletePressToggleButton

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.snap
import com.example.vibefinance.theme.MotionTokens
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.example.vibefinance.util.rememberVibeHapticFeedback
import androidx.compose.ui.platform.LocalConfiguration
import com.example.vibefinance.R
import com.example.vibefinance.ui.common.bouncyClickable
import com.example.vibefinance.ui.common.pressBounce
import android.widget.Toast
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import com.example.vibefinance.util.LocalAppManager
import com.example.vibefinance.theme.LocalIsDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.tween
import androidx.compose.material3.Surface

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.ui.graphics.luminance
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Crop
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.FilledTonalButton
import com.example.vibefinance.ui.home.DailyBentoCardWrapper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.OutlinedTextField
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.ui.semantics.role
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import com.example.vibefinance.ui.preferences.LocalExperience
import com.example.vibefinance.ui.preferences.MotionLevel
import com.example.vibefinance.util.InstalledAppInfo
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.drawBehind
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import com.example.vibefinance.ui.components.RollingNumberText
import com.example.vibefinance.ui.components.rememberConnectedButtonColorMotion
import com.example.vibefinance.ui.components.ConnectedButtonRipple
import com.example.vibefinance.ui.components.GlassmorphicCard
import com.example.vibefinance.theme.JetBrainsMonoFontFamily
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.platform.LocalContext
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.ui.FinanceIntent

import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.components.MoneySeparationChart
import com.example.vibefinance.ui.components.InteractiveDonutChart
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale

fun getCardThemeColors(themeName: String?, isDark: Boolean): Pair<Color, Color> =
    when (themeName?.lowercase(Locale.US)) {
        "ocean", "ocean breeze" -> Color(0xFF0D47A1) to Color(0xFF1976D2)
        "emerald", "emerald forest" -> Color(0xFF1B5E20) to Color(0xFF388E3C)
        "sunset", "sunset glow" -> Color(0xFFE65100) to Color(0xFFF57C00)
        "purple", "purple neon" -> Color(0xFF4A148C) to Color(0xFF7B1FA2)
        "rose" -> Color(0xFF880E4F) to Color(0xFFC2185B)
        "crimson" -> Color(0xFFB71C1C) to Color(0xFFD32F2F)
        "dark stealth" -> Color(0xFF1A1D20) to Color(0xFF0D0E10)
        else -> Color(0xFF161B22) to Color(0xFF242C37) // Rich Dark Metallic Slate for Default Cards
    }

fun getCardGradient(themeName: String?, isDark: Boolean): Brush {
    val (c1, c2) = getCardThemeColors(themeName, isDark)
    return Brush.verticalGradient(listOf(c1, c2))
}

enum class AssetFilter {
    ALL,
    CASH_BANK,
    DEBIT_CARDS,
    CREDIT_CARDS
}

private fun AccountEntity.displayName(): String = nickname?.trim()?.takeIf { it.isNotEmpty() } ?: name

private fun AccountEntity.realLastFour(): String? =
    cardLast4?.takeIf { it.length == 4 && it.all { digit -> digit in '0'..'9' } }

fun AccountEntity.watermarkText(): String {
    val cleanName = nickname?.trim()?.takeIf { it.isNotEmpty() } ?: name.trim()
    val tokens = cleanName.split(Regex("[\\s_\\-/]+")).filter { it.isNotBlank() }
    val firstToken = tokens.firstOrNull().orEmpty()
    return if (firstToken.length in 2..10 && firstToken.any { it.isLetter() }) {
        firstToken.uppercase(Locale.US)
    } else if (cleanName.length in 2..6) {
        cleanName.uppercase(Locale.getDefault())
    } else {
        when (type) {
            AccountType.CC -> "CREDIT"
            AccountType.INVESTMENT -> "INVESTMENT"
            AccountType.CASH -> "CASH"
            AccountType.BANK -> "BANK"
            AccountType.DEBIT -> "DEBIT"
        }
    }
}


@Composable
internal fun accountTypeDisplayLabel(type: AccountType): String = stringResource(
    when (type) {
        AccountType.CASH -> R.string.acc_type_cash
        AccountType.BANK -> R.string.acc_type_bank
        AccountType.DEBIT -> R.string.ah_type_debit
        AccountType.CC -> R.string.acc_type_credit
        AccountType.INVESTMENT -> R.string.assets_type_investment
    }
)

@Composable
private fun accountBalanceLabel(account: AccountEntity): String = stringResource(
    if (account.type != AccountType.CC) R.string.ah_current_balance
    else if (account.balance < 0.0) R.string.ah_card_credit
    else R.string.ah_outstanding_debt
)

private fun accountBalanceText(account: AccountEntity): String {
    val balance = if (account.type == AccountType.CC) kotlin.math.abs(account.balance) else account.balance
    return com.example.vibefinance.util.CurrencyEngine.formatSignedAmount(balance, prefix = "HK$")
}

@Composable
internal fun cardDesignOptionLabel(key: String): String = when (key) {
    "None" -> stringResource(R.string.ah_none)
    "Cyber Grid" -> stringResource(R.string.ah_pattern_cyber_grid)
    "Neon Waves" -> stringResource(R.string.ah_pattern_neon_waves)
    "Geometric Mesh" -> stringResource(R.string.ah_pattern_geometric_mesh)
    else -> key // Payment-network brands keep their official names.
}

@Composable
internal fun accountCardThemeLabel(key: String): String = stringResource(
    when (key) {
        "ocean" -> R.string.ah_theme_ocean
        "emerald" -> R.string.ah_theme_emerald
        "sunset" -> R.string.ah_theme_sunset
        "purple" -> R.string.ah_theme_purple
        "rose" -> R.string.ah_theme_rose
        "crimson" -> R.string.ah_theme_crimson
        else -> R.string.ah_theme_default
    }
)

@Composable
private fun AccountEntity.localizedWatermarkText(): String {
    val cleanName = nickname?.trim()?.takeIf { it.isNotEmpty() } ?: name.trim()
    val firstToken = cleanName.split(Regex("[\\s_\\-/]+")).firstOrNull { it.isNotBlank() }.orEmpty()
    return if ((firstToken.length in 2..10 && firstToken.any { it.isLetter() }) || cleanName.length in 2..6) {
        watermarkText()
    } else {
        accountTypeDisplayLabel(type).uppercase(LocalConfiguration.current.locales[0])
    }
}

private data class AccountThumbnailSpec(val width: Dp, val height: Dp, val shape: Shape)

private fun accountThumbnailSpec(ratio: String?, compact: Boolean): AccountThumbnailSpec =
    when (ratio) {
        "16:9", "WIDE" -> if (compact) AccountThumbnailSpec(47.dp, 27.dp, RoundedCornerShape(7.dp))
        else AccountThumbnailSpec(56.dp, 32.dp, RoundedCornerShape(7.dp))
        "4:3" -> if (compact) AccountThumbnailSpec(36.dp, 28.dp, RoundedCornerShape(8.dp))
        else AccountThumbnailSpec(44.dp, 33.dp, RoundedCornerShape(9.dp))
        "2.35:1" -> if (compact) AccountThumbnailSpec(48.dp, 22.dp, RoundedCornerShape(6.dp))
        else AccountThumbnailSpec(60.dp, 26.dp, RoundedCornerShape(6.dp))
        "1:1", "SQUARE" -> if (compact) AccountThumbnailSpec(29.dp, 29.dp, RoundedCornerShape(8.dp))
        else AccountThumbnailSpec(40.dp, 40.dp, RoundedCornerShape(10.dp))
        "CIRCLE" -> if (compact) AccountThumbnailSpec(29.dp, 29.dp, CircleShape)
        else AccountThumbnailSpec(40.dp, 40.dp, CircleShape)
        else -> if (compact) AccountThumbnailSpec(44.dp, 28.dp, RoundedCornerShape(7.dp))
        else AccountThumbnailSpec(50.dp, 32.dp, RoundedCornerShape(7.dp))
    }

@Composable
private fun rememberAccountThumbnail(filePath: String?): ImageBitmap? {
    val context = LocalContext.current
    return remember(filePath) {
        if (filePath.isNullOrBlank()) return@remember null
        try {
            val bitmap = if (filePath.equals("sample", ignoreCase = true)) {
                createSampleCardBitmap(context)
            } else {
                val file = File(filePath)
                if (!file.isFile) return@remember null
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(file.absolutePath, bounds)
                if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@remember null
                val sampleSize = (maxOf(bounds.outWidth, bounds.outHeight) / 256).coerceAtLeast(1)
                BitmapFactory.decodeFile(file.absolutePath, BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                })
            }
            bitmap?.asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }
}

@Composable
private fun AccountCardThumbnail(
    account: AccountEntity,
    bitmap: ImageBitmap,
    compact: Boolean
) {
    val spec = remember(account.customImageAspectRatio, compact) {
        accountThumbnailSpec(account.customImageAspectRatio, compact)
    }
    Surface(
        modifier = Modifier.size(width = spec.width, height = spec.height),
        shape = spec.shape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = if (compact) 0.dp else 2.dp
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

internal data class AccountAccent(val background: Color, val foreground: Color)

@Composable
internal fun accountAccent(type: AccountType, key: String?): AccountAccent {
    val colors = MaterialTheme.colorScheme
    return when (key) {
        "primary" -> AccountAccent(colors.primaryContainer, colors.onPrimaryContainer)
        "secondary" -> AccountAccent(colors.secondaryContainer, colors.onSecondaryContainer)
        "tertiary" -> AccountAccent(colors.tertiaryContainer, colors.onTertiaryContainer)
        else -> when (type) {
            AccountType.CASH -> AccountAccent(colors.secondaryContainer, colors.onSecondaryContainer)
            AccountType.BANK -> AccountAccent(colors.primaryContainer, colors.onPrimaryContainer)
            AccountType.DEBIT -> AccountAccent(colors.tertiaryContainer, colors.onTertiaryContainer)
            AccountType.CC -> AccountAccent(colors.inverseSurface, colors.inverseOnSurface)
            AccountType.INVESTMENT -> AccountAccent(colors.tertiaryContainer, colors.onTertiaryContainer)
        }
    }
}

@Composable
fun AssetCardItem(
    account: AccountEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val customImageBitmap: ImageBitmap? = remember(account.cardImageUri) {
        account.cardImageUri?.let { uriStr ->
            try {
                if (uriStr.equals("sample", ignoreCase = true)) {
                    createSampleCardBitmap(context)?.asImageBitmap()
                } else {
                    val file = File(uriStr)
                    if (file.exists()) {
                        BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                    } else null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    val hasCustomImage = customImageBitmap != null
    val last4 = account.realLastFour()
    val displayName = account.displayName()

    val cardTypeName = when (account.type) {
        AccountType.CC -> stringResource(com.example.vibefinance.R.string.assets_type_credit)
        AccountType.INVESTMENT -> stringResource(com.example.vibefinance.R.string.assets_type_investment)
        AccountType.CASH -> stringResource(com.example.vibefinance.R.string.assets_type_cash)
        AccountType.BANK -> stringResource(com.example.vibefinance.R.string.assets_type_bank)
        AccountType.DEBIT -> stringResource(com.example.vibefinance.R.string.assets_type_debit)
    }.uppercase(LocalConfiguration.current.locales[0])

    Card(
        modifier = modifier
            .width(248.dp)
            .height(152.dp)
            .clip(RoundedCornerShape(20.dp))
            .bouncyClickable(shape = RoundedCornerShape(20.dp)) { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .drawCardBackground(
                    bitmap = customImageBitmap,
                    themeName = if (hasCustomImage) null else (account.cardTheme ?: "default"),
                    patternName = account.cardPattern ?: "cyber grid",
                    isDark = isDark,
                    offsetX = account.cardBgOffsetX,
                    offsetY = account.cardBgOffsetY,
                    scale = account.cardBgScale
                )
        ) {
            if (!hasCustomImage) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = account.localizedWatermarkText(),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 58.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-2).sp,
                            fontFamily = JetBrainsMonoFontFamily
                        ),
                        color = Color.White.copy(alpha = 0.12f),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier.graphicsLayer {
                            translationX = 14.dp.toPx()
                            translationY = 6.dp.toPx()
                        }
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Row: Card Type Word Badge & Protocol/Issuer logo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.32f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = cardTypeName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    if (account.cardProtocol != null) {
                        CardProtocolLogo(protocol = account.cardProtocol)
                    } else {
                        Icon(
                            imageVector = when (account.type) {
                                AccountType.CC -> Icons.Default.CreditCard
                                AccountType.INVESTMENT -> Icons.AutoMirrored.Filled.TrendingUp
                                AccountType.CASH -> Icons.Default.Savings
                                AccountType.BANK -> Icons.Default.AccountBalance
                                AccountType.DEBIT -> Icons.Default.CreditCard
                            },
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Middle Word Display: Account Name
                Column {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val supportingIdentity = listOfNotNull(
                        last4?.let { "•••• $it" },
                        account.name.takeIf { displayName != account.name }
                    ).joinToString(" · ")
                    if (supportingIdentity.isNotEmpty()) {
                        Text(
                            text = supportingIdentity,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Footer: Balance Amount Display & M3 Circular Progress Indicator for Credit Utilization
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = accountBalanceLabel(account),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.65f),
                            fontWeight = FontWeight.Bold
                        )
                        com.example.vibefinance.ui.components.RollingNumberText(
                            text = accountBalanceText(account),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    val limit = account.creditLimit
                    if (account.type == AccountType.CC && limit != null && limit > 0.0) {
                        val ratio = (account.balance / limit).coerceIn(0.0, 1.0).toFloat()
                        val percent = (ratio * 100).toInt()
                        val indicatorColor = when {
                            ratio > 0.70f -> Color(0xFFFF6B6B)
                            ratio > 0.30f -> Color(0xFFFFB74D)
                            else -> Color(0xFF81C784)
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(36.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier.fillMaxSize(),
                                color = indicatorColor,
                                trackColor = Color.White.copy(alpha = 0.25f),
                                strokeWidth = 3.dp,
                                strokeCap = StrokeCap.Round
                            )
                            Text(
                                text = "$percent%",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

data class NetWorthBreakdown(
    val totalAssets: Double,
    val totalDebt: Double,
    val netWorth: Double
)

fun calculateNetWorth(accounts: List<AccountEntity>): NetWorthBreakdown {
    val totalAssets = accounts.filter { it.type != AccountType.CC && it.type != AccountType.INVESTMENT }.sumOf { it.balance }
    val totalDebt = accounts.filter { it.type == AccountType.CC }.sumOf { it.balance }
    return NetWorthBreakdown(
        totalAssets = totalAssets,
        totalDebt = totalDebt,
        netWorth = totalAssets - totalDebt
    )
}

fun filterAccounts(accounts: List<AccountEntity>, filter: AssetFilter): List<AccountEntity> {
    return when (filter) {
        AssetFilter.ALL -> accounts
        AssetFilter.CASH_BANK -> accounts.filter { it.type != AccountType.CC && it.type != AccountType.INVESTMENT }
        AssetFilter.DEBIT_CARDS -> accounts.filter { it.type == AccountType.DEBIT }
        AssetFilter.CREDIT_CARDS -> accounts.filter { it.type == AccountType.CC }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun AssetGroupSelector(
    accounts: List<AccountEntity>,
    selectedFilter: AssetFilter,
    onSelectFilter: (AssetFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val isDarkTheme = LocalIsDarkTheme.current ?: (colors.background.luminance() < 0.5f)
    val spacing = ButtonGroupDefaults.ConnectedSpaceBetween

    Row(
        modifier = modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AssetFilter.entries.forEachIndexed { index, filter ->
            val selected = filter == selectedFilter
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val colorMotion = rememberConnectedButtonColorMotion(
                isSelected = selected,
                isPressed = isPressed,
                inactiveContainerColor = colors.surfaceContainerHigh,
                inactiveContentColor = colors.onSurfaceVariant,
                activeContainerColor = if (isDarkTheme) colors.primaryContainer else colors.primary,
                activeContentColor = if (isDarkTheme) colors.onPrimaryContainer else colors.onPrimary,
                backdropColor = colors.background
            )
            val label = when (filter) {
                AssetFilter.ALL -> stringResource(com.example.vibefinance.R.string.asset_filter_all)
                AssetFilter.CASH_BANK -> stringResource(com.example.vibefinance.R.string.asset_filter_cash_bank)
                AssetFilter.DEBIT_CARDS -> stringResource(com.example.vibefinance.R.string.asset_filter_debit_cards)
                AssetFilter.CREDIT_CARDS -> stringResource(com.example.vibefinance.R.string.asset_filter_credit_cards)
            }
            val count = when (filter) {
                AssetFilter.ALL -> accounts.size
                AssetFilter.CASH_BANK -> accounts.count { it.type != AccountType.CC && it.type != AccountType.INVESTMENT }
                AssetFilter.DEBIT_CARDS -> accounts.count { it.type == AccountType.DEBIT }
                AssetFilter.CREDIT_CARDS -> accounts.count { it.type == AccountType.CC }
            }
            val icon = when (filter) {
                AssetFilter.ALL -> Icons.Default.Savings
                AssetFilter.CASH_BANK -> Icons.Default.AccountBalance
                AssetFilter.DEBIT_CARDS -> Icons.Default.AccountBalanceWallet
                AssetFilter.CREDIT_CARDS -> Icons.Default.CreditCard
            }
            val motion = LocalExperience.current.motion
            val density = LocalDensity.current.density
            val iconBounceScale = remember { Animatable(if (selected) 1.08f else 1f) }
            val iconBounceOffsetY = remember { Animatable(0f) }
            var wasSelected by remember { mutableStateOf(selected) }
            var clickTrigger by remember { mutableIntStateOf(0) }
            var lastTrigger by remember { mutableIntStateOf(0) }

            LaunchedEffect(selected, clickTrigger, motion) {
                val entering = selected && !wasSelected
                val reselected = selected && wasSelected && (clickTrigger != lastTrigger)
                wasSelected = selected
                lastTrigger = clickTrigger

                if ((entering || reselected) && motion == MotionLevel.FULL) {
                    coroutineScope {
                        launch {
                            iconBounceScale.snapTo(1f)
                            iconBounceScale.animateTo(
                                targetValue = 1.20f,
                                animationSpec = tween(durationMillis = 80, easing = FastOutSlowInEasing)
                            )
                            iconBounceScale.animateTo(
                                targetValue = 1.08f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        }
                        launch {
                            iconBounceOffsetY.snapTo(0f)
                            iconBounceOffsetY.animateTo(
                                targetValue = -2.5f,
                                animationSpec = tween(durationMillis = 80, easing = FastOutSlowInEasing)
                            )
                            iconBounceOffsetY.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        }
                    }
                } else if (selected) {
                    if (motion == MotionLevel.REDUCED) {
                        iconBounceScale.animateTo(1.08f, tween(120))
                    } else {
                        iconBounceScale.snapTo(1.08f)
                    }
                    iconBounceOffsetY.snapTo(0f)
                } else {
                    if (motion == MotionLevel.MINIMAL) {
                        iconBounceScale.snapTo(1f)
                    } else {
                        iconBounceScale.animateTo(1f, spring(stiffness = Spring.StiffnessMediumLow))
                    }
                    iconBounceOffsetY.snapTo(0f)
                }
            }

            ConnectedButtonRipple(isSelected = selected) {
                CompletePressToggleButton(
                    checked = selected,
                    onCheckedChange = {
                        clickTrigger++
                        onSelectFilter(filter)
                    },
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 58.dp)
                        .semantics { role = Role.RadioButton },
                    shapes = when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        AssetFilter.entries.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                    colors = ToggleButtonDefaults.toggleButtonColors(
                        containerColor = colorMotion.containerColor,
                        contentColor = colorMotion.contentColor,
                        checkedContainerColor = colorMotion.containerColor,
                        checkedContentColor = colorMotion.contentColor
                    ),
                    elevation = null,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 58.dp)
                            .then(colorMotion.contentModifier)
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier
                                    .testTag("assetFilterIcon_${filter.name}")
                                    .graphicsLayer {
                                        scaleX = iconBounceScale.value
                                        scaleY = iconBounceScale.value
                                        translationY = iconBounceOffsetY.value * density
                                    }
                                    .size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = count.toString(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AccountsScreen(
    state: FinanceUiState,
    onIntent: (FinanceIntent) -> Unit,
    modifier: Modifier = Modifier,
    topContentPadding: Dp = 16.dp,
    topVisibilityInset: Dp = 0.dp,
    bottomVisibilityInset: Dp = 0.dp,
    onViewAccountHistory: (Long) -> Unit = {},
    initialPrefillAccount: AccountEntity? = null,
    onAccountCreated: ((AccountEntity) -> Unit)? = null,
    onDismissCreate: (() -> Unit)? = null,
    editorState: AssetEditorState = rememberAssetEditorState(),
    renderEditor: Boolean = true,
    saveResult: com.example.vibefinance.ui.AccountSaveResult? = null,
    listState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    bottomVisibilityInsetProvider: () -> Dp = { bottomVisibilityInset }
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val presentationLocale = LocalConfiguration.current.locales[0]
    val haptic = rememberVibeHapticFeedback()
    val hapticView = androidx.compose.ui.platform.LocalView.current
    val breakdown = remember(state.accounts) { calculateNetWorth(state.accounts) }
    val totalAssets = breakdown.totalAssets
    val totalDebt = breakdown.totalDebt
    val netWorth = breakdown.netWorth

    androidx.compose.runtime.LaunchedEffect(initialPrefillAccount) {
        if (initialPrefillAccount != null && !editorState.isOpen) editorState.open(prefill = initialPrefillAccount)
    }
    var balanceAccount by remember { mutableStateOf<AccountEntity?>(null) }
    var accountForAppPicker by remember { mutableStateOf<AccountEntity?>(null) }
    var selectedAssetFilter by remember { mutableStateOf(AssetFilter.ALL) }
    val context = LocalContext.current
    val displayPreferences = remember(context) {
        com.example.vibefinance.util.CoordinatedPreferences.get(context, "assets_display")
    }
    var isCompactMode by remember(displayPreferences) {
        mutableStateOf(displayPreferences.getBoolean("compact_mode", false))
    }
    var isAccountReorderMode by rememberSaveable { mutableStateOf(false) }

    val moveAccount: (AccountEntity, AccountEntity) -> Unit = { fromAcc, toAcc ->
        val fullOrder = if (state.assetAccountOrder.isNotEmpty()) {
            state.assetAccountOrder.toMutableList()
        } else {
            state.accounts.sortedWith(
                compareBy<AccountEntity> { it.type.ordinal }
                    .thenBy { it.name.lowercase(Locale.getDefault()) }
            ).map { it.id }.toMutableList()
        }
        state.accounts.forEach { acc ->
            if (!fullOrder.contains(acc.id)) fullOrder.add(acc.id)
        }
        val idxFrom = fullOrder.indexOf(fromAcc.id)
        val idxTo = fullOrder.indexOf(toAcc.id)
        if (idxFrom != -1 && idxTo != -1) {
            fullOrder[idxFrom] = toAcc.id
            fullOrder[idxTo] = fromAcc.id
            com.example.vibefinance.util.VibeHaptics.lightTick(hapticView)
            onIntent(FinanceIntent.SetAssetAccountOrder(fullOrder))
        }
    }

    val filteredAccounts = remember(state.accounts, selectedAssetFilter, state.assetAccountOrder) {
        val base = filterAccounts(state.accounts, selectedAssetFilter)
        if (state.assetAccountOrder.isEmpty()) {
            base.sortedWith(
                compareBy<AccountEntity> { it.type.ordinal }
                    .thenBy { it.name.lowercase(Locale.getDefault()) }
            )
        } else {
            val orderMap = state.assetAccountOrder.withIndex().associate { it.value to it.index }
            base.sortedWith(
                compareBy<AccountEntity> { it.type.ordinal }
                    .thenBy { orderMap[it.id] ?: (Int.MAX_VALUE - 1000 + it.type.ordinal) }
                    .thenBy { it.name.lowercase(Locale.getDefault()) }
            )
        }
    }

    ContentEntranceViewport(
        modifier = modifier.fillMaxSize(),
        topVisibilityInset = topVisibilityInset,
        bottomVisibilityInset = bottomVisibilityInset,
        bottomVisibilityInsetProvider = bottomVisibilityInsetProvider,
        tagPrefix = "AssetsArrival"
    ) {
        AnimatedContent(
            targetState = state.isLoading,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
            label = "accountsSkeletonCrossfade"
        ) { isLoading ->
            if (isLoading) {
                com.example.vibefinance.ui.components.AccountsScreenSkeleton()
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().testTag("AssetsList"),
                    contentPadding = PaddingValues(top = topContentPadding),
                    verticalArrangement = Arrangement.Top
                ) {
            // 2. Premium Net Asset Value Header Card with Rolling Number Text
            item(key = "assets-summary") {
                PageContentEntrance("summary") {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        val isDark = MaterialTheme.colorScheme.background != Color(0xFFF8F9FA)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                // Top Row: Net Asset Value text stats on left, Interactive Donut Chart on right
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(R.string.ah_net_asset_value),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        val netWorthFontSize = when {
                                            netWorth >= 1000000.0 -> 24.sp
                                            netWorth >= 100000.0 -> 26.sp
                                            else -> 30.sp
                                        }
                                         com.example.vibefinance.ui.components.RollingNumberText(
                                            text = com.example.vibefinance.util.CurrencyEngine.formatSignedAmount(netWorth, prefix = "HK$"),
                                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = netWorthFontSize),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }

                                    InteractiveDonutChart(
                                        accounts = state.accounts,
                                        size = 84f
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                    ) {
                                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                            Text(
                                                text = stringResource(com.example.vibefinance.R.string.assets_total_assets),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            com.example.vibefinance.ui.components.RollingNumberText(
                                                text = com.example.vibefinance.util.CurrencyEngine.formatSignedAmount(totalAssets, prefix = "HK$"),
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            Text(
                                                text = stringResource(R.string.ah_total_card_debt),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            com.example.vibefinance.ui.components.RollingNumberText(
                                                text = com.example.vibefinance.util.CurrencyEngine.formatSignedAmount(totalDebt, prefix = "HK$"),
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = if (totalDebt > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(16.dp))
                                com.example.vibefinance.ui.components.PeriodIncomeEstimateCard(state)

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                    thickness = 1.dp
                                )

                                // Money Separation Chart & Proportional Allocation Bar
                                PageContentEntrance("allocation-chart") {
                                    MoneySeparationChart(
                                        accounts = state.accounts
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                }
            }





            // 2.8. Single-choice expressive account group selector
            item(key = "assets-filter") {
                PageContentEntrance("filter") {
                    AssetGroupSelector(
                        accounts = state.accounts,
                        selectedFilter = selectedAssetFilter,
                        onSelectFilter = { selectedAssetFilter = it },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                }
            }

            // 3. Expressive Header & Add Asset Button
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.ah_assets_cards),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        val addAssetInteractionSource = remember { MutableInteractionSource() }
                        var addAssetBounds by remember { mutableStateOf<Rect?>(null) }
                        CompletePressButton(
                            onClick = {
                                com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                editorState.open(sourceBounds = addAssetBounds)
                            },
                            interactionSource = addAssetInteractionSource,
                            modifier = Modifier
                                .onGloballyPositioned { coords ->
                                    if (coords.isAttached) {
                                        addAssetBounds = coords.boundsInRoot()
                                    }
                                }
                                .pressBounce(interactionSource = addAssetInteractionSource)
                                .height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 1.dp,
                                pressedElevation = 0.dp
                            ),
                            shapes = ButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.btn_add_account),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.ah_add_asset),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 4. Edge-to-Edge Full-Bleed Accounts List Items or Friendly Empty State
            item(key = "assets-display-mode") {
                PageContentEntrance("display-mode") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val reorderInteraction = remember { MutableInteractionSource() }
                        CompletePressFilledTonalButton(
                            onClick = {
                                com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                isAccountReorderMode = !isAccountReorderMode
                            },
                            interactionSource = reorderInteraction,
                            shapes = ButtonDefaults.shapes(
                                shape = CircleShape,
                                pressedShape = RoundedCornerShape(percent = 32)
                            ),
                            colors = if (isAccountReorderMode) {
                                ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            } else {
                                ButtonDefaults.filledTonalButtonColors()
                            },
                            modifier = Modifier.pressBounce(interactionSource = reorderInteraction),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isAccountReorderMode) stringResource(R.string.loc_done)
                                else stringResource(R.string.loc_reorder_assets)
                            )
                        }

                        val modeInteraction = remember { MutableInteractionSource() }
                        val experience = com.example.vibefinance.ui.preferences.LocalExperience.current
                        val isReducedMotion = experience.motion == com.example.vibefinance.ui.preferences.MotionLevel.MINIMAL

                        val iconScale = remember { Animatable(if (isCompactMode) 0.90f else 1.0f) }
                        var displayedCompactMode by remember { mutableStateOf(isCompactMode) }
                        var isFirstRun by remember { mutableStateOf(true) }

                        LaunchedEffect(isCompactMode) {
                            if (isFirstRun) {
                                isFirstRun = false
                                iconScale.snapTo(if (isCompactMode) 0.90f else 1.0f)
                                displayedCompactMode = isCompactMode
                                return@LaunchedEffect
                            }
                            if (isReducedMotion) {
                                iconScale.snapTo(if (isCompactMode) 0.90f else 1.0f)
                                displayedCompactMode = isCompactMode
                            } else {
                                if (isCompactMode) {
                                    // Detailed -> Compact:
                                    // Phase 1: Focus burst up to 1.25x
                                    iconScale.animateTo(
                                        targetValue = 1.25f,
                                        animationSpec = tween(
                                            durationMillis = MotionTokens.DurationShort3,
                                            easing = MotionTokens.EmphasizedDecelerate
                                        )
                                    )
                                    // Turnaround: swap glyph at peak
                                    displayedCompactMode = true
                                    // Phase 2: Settle down to 0.90x compact resting scale
                                    iconScale.animateTo(
                                        targetValue = 0.90f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                                } else {
                                    // Compact -> Detailed:
                                    // Phase 1: Squash dip down to 0.75x trough
                                    iconScale.animateTo(
                                        targetValue = 0.75f,
                                        animationSpec = tween(
                                            durationMillis = MotionTokens.DurationShort3,
                                            easing = MotionTokens.EmphasizedAccelerate
                                        )
                                    )
                                    // Turnaround: swap glyph at trough
                                    displayedCompactMode = false
                                    // Phase 2: Bloom up to 1.0f detailed resting scale
                                    iconScale.animateTo(
                                        targetValue = 1.0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                                }
                            }
                        }

                        CompletePressFilledTonalButton(
                            onClick = {
                                com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                isCompactMode = !isCompactMode
                                displayPreferences.edit().putBoolean("compact_mode", isCompactMode).apply()
                            },
                            interactionSource = modeInteraction,
                            shapes = ButtonDefaults.shapes(
                                shape = CircleShape,
                                pressedShape = RoundedCornerShape(percent = 32)
                            ),
                            modifier = Modifier.pressBounce(interactionSource = modeInteraction),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .graphicsLayer {
                                        scaleX = iconScale.value
                                        scaleY = iconScale.value
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                AnimatedContent(
                                    targetState = displayedCompactMode,
                                    transitionSpec = {
                                        if (isReducedMotion) {
                                            EnterTransition.None togetherWith ExitTransition.None
                                        } else {
                                            fadeIn(animationSpec = tween(100)) togetherWith fadeOut(animationSpec = tween(100))
                                        }
                                    },
                                    label = "compact_mode_icon_crossfade"
                                ) { compact ->
                                    Icon(
                                        imageVector = if (compact) Icons.Default.ViewAgenda else Icons.Default.ViewStream,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(7.dp))
                            AnimatedContent(
                                targetState = isCompactMode,
                                transitionSpec = {
                                    if (isReducedMotion) {
                                        EnterTransition.None togetherWith ExitTransition.None
                                    } else if (targetState) {
                                        // Detailed -> Compact: directional slide down
                                        (slideInVertically(animationSpec = tween(200, easing = MotionTokens.EmphasizedDecelerate)) { -it / 2 } +
                                            fadeIn(animationSpec = tween(150)))
                                            .togetherWith(
                                                slideOutVertically(animationSpec = tween(200, easing = MotionTokens.EmphasizedAccelerate)) { it / 2 } +
                                                    fadeOut(animationSpec = tween(150))
                                            )
                                    } else {
                                        // Compact -> Detailed: directional slide up
                                        (slideInVertically(animationSpec = tween(200, easing = MotionTokens.EmphasizedDecelerate)) { it / 2 } +
                                            fadeIn(animationSpec = tween(150)))
                                            .togetherWith(
                                                slideOutVertically(animationSpec = tween(200, easing = MotionTokens.EmphasizedAccelerate)) { -it / 2 } +
                                                    fadeOut(animationSpec = tween(150))
                                            )
                                    }
                                },
                                label = "compact_mode_label_transition"
                            ) { compact ->
                                Text(
                                    text = stringResource(
                                        if (compact) com.example.vibefinance.R.string.assets_detailed_mode
                                        else com.example.vibefinance.R.string.assets_compact_mode
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (isAccountReorderMode) {
                item(key = "assets-reorder-banner") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapVert,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = stringResource(R.string.loc_reorder_assets),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(
                                    onClick = {
                                        com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                        onIntent(com.example.vibefinance.ui.FinanceIntent.ResetAssetAccountOrder)
                                    }
                                ) {
                                    Text(
                                        text = stringResource(R.string.loc_reset),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                FilledTonalButton(
                                    onClick = {
                                        com.example.vibefinance.util.VibeHaptics.successConfirm(hapticView)
                                        isAccountReorderMode = false
                                    },
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.loc_done),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (filteredAccounts.isEmpty()) {
                item {
                    PageContentEntrance("empty") {
                        GlassmorphicCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            cornerRadius = 24.dp,
                            borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = androidx.compose.ui.res.stringResource(com.example.vibefinance.R.string.no_assets_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = androidx.compose.ui.res.stringResource(com.example.vibefinance.R.string.no_assets_description),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                val emptyAddInteraction = remember { MutableInteractionSource() }
                                var emptyAddBounds by remember { mutableStateOf<Rect?>(null) }
                                CompletePressButton(
                                    onClick = {
                                        com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                        editorState.open(sourceBounds = emptyAddBounds)
                                    },
                                    shapes = ButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                                    interactionSource = emptyAddInteraction,
                                    modifier = Modifier
                                        .onGloballyPositioned { coords ->
                                            if (coords.isAttached) {
                                                emptyAddBounds = coords.boundsInRoot()
                                            }
                                        }
                                        .pressBounce(interactionSource = emptyAddInteraction)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = androidx.compose.ui.res.stringResource(com.example.vibefinance.R.string.btn_add_account),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                AccountType.entries.forEach { type ->
                    val groupAccounts = filteredAccounts.filter { it.type == type }
                    if (groupAccounts.isNotEmpty()) {
                        item(key = "asset-group-${type.name}") {
                            val groupIcon = when (type) {
                                AccountType.CASH -> Icons.Default.Savings
                                AccountType.BANK -> Icons.Default.AccountBalance
                                AccountType.DEBIT -> Icons.Default.CreditCard
                                AccountType.CC -> Icons.Default.CreditCard
                                AccountType.INVESTMENT -> Icons.AutoMirrored.Filled.TrendingUp
                            }
                            PageContentEntrance("group:${type.name}") {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 5.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerLow
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                                    ) {
                                        Icon(
                                            imageVector = groupIcon,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = stringResource(
                                                when (type) {
                                                    AccountType.CASH -> com.example.vibefinance.R.string.assets_type_cash
                                                    AccountType.BANK -> com.example.vibefinance.R.string.assets_type_bank
                                                    AccountType.DEBIT -> com.example.vibefinance.R.string.assets_type_debit
                                                    AccountType.CC -> com.example.vibefinance.R.string.assets_type_credit
                                                    AccountType.INVESTMENT -> com.example.vibefinance.R.string.assets_type_investment
                                                }
                                            ),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.secondaryContainer
                                        ) {
                                            Text(
                                                text = groupAccounts.size.toString(),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        itemsIndexed(groupAccounts, key = { _, account -> "account-${account.id}" }) { index, account ->
                            DailyBentoCardWrapper(
                                title = account.displayName(),
                                index = index,
                                totalCount = groupAccounts.size,
                                isEditMode = isAccountReorderMode,
                                onLongPress = {
                                    isAccountReorderMode = true
                                },
                                onMoveUp = {
                                    if (index > 0) {
                                        moveAccount(account, groupAccounts[index - 1])
                                    }
                                },
                                onMoveDown = {
                                    if (index < groupAccounts.lastIndex) {
                                        moveAccount(account, groupAccounts[index + 1])
                                    }
                                },
                                modifier = Modifier.animateItem(
                                    fadeInSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    placementSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    fadeOutSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                )
                            ) {
                                AnimatedContent(
                                    targetState = isCompactMode,
                                    transitionSpec = {
                                        (fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                                            slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it / 8 })
                                            .togetherWith(
                                                fadeOut(spring(stiffness = Spring.StiffnessMediumLow)) +
                                                    slideOutVertically(spring(stiffness = Spring.StiffnessMediumLow)) { -it / 8 }
                                            )
                                            .using(SizeTransform(clip = true) { _, _ ->
                                                spring(stiffness = Spring.StiffnessMediumLow)
                                            })
                                    },
                                    label = "accountViewMode"
                                ) { compact ->
                                    PageContentEntrance("account:${account.id}") {
                                        val isCardHidden = (editorState.isOpen || editorState.isTransitionActive) && editorState.accountId == account.id
                                        val cardItemModifier = Modifier.graphicsLayer { alpha = if (isCardHidden) 0f else 1f }

                                        if (compact) {
                                            CompactAccountRow(
                                                account = account,
                                                transactions = state.transactions,
                                                isFirstInGroup = index == 0,
                                                isLastInGroup = index == groupAccounts.lastIndex,
                                                onEditClick = {
                                                    editorState.open(accountId = account.id)
                                                },
                                                onEditWithBounds = { bounds ->
                                                    editorState.open(accountId = account.id, sourceBounds = bounds)
                                                },
                                                onBalanceClick = { balanceAccount = account },
                                                onHistoryClick = { onViewAccountHistory(account.id) },
                                                onPickApp = { accountForAppPicker = it },
                                                modifier = cardItemModifier
                                            )
                                        } else {
                                            ExpressiveAccountListItem(
                                                account = account,
                                                transactions = state.transactions,
                                                onEditClick = {
                                                    editorState.open(accountId = account.id)
                                                },
                                                onEditWithBounds = { bounds ->
                                                    editorState.open(accountId = account.id, sourceBounds = bounds)
                                                },
                                                onBalanceClick = { balanceAccount = account },
                                                onHistoryClick = { onViewAccountHistory(account.id) },
                                                onPickApp = { accountForAppPicker = it },
                                                modifier = cardItemModifier
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Generous Bottom Spacer to allow scrolling further down
            item {
                Spacer(modifier = Modifier.height(140.dp))
            }
        }
    }
    }

    accountForAppPicker?.let { targetAccount ->
        AppPickerDialog(
            currentPackage = targetAccount.linkedAppPackage,
            onAppSelected = { newPkg ->
                onIntent(FinanceIntent.SaveAccount(targetAccount.copy(linkedAppPackage = newPkg)))
                accountForAppPicker = null
            },
            onDismissRequest = { accountForAppPicker = null }
        )
    }

    balanceAccount?.let { selectedAccount ->
        val liveAccount = state.accounts.firstOrNull { it.id == selectedAccount.id } ?: selectedAccount
        var targetBalanceText by remember(selectedAccount.id) {
            mutableStateOf(String.format(Locale.US, "%.2f", selectedAccount.balance))
        }
        val targetBalance = targetBalanceText.replace(",", "").toDoubleOrNull()?.takeIf { it.isFinite() }
        val delta = targetBalance?.minus(liveAccount.balance)
        AlertDialog(
            onDismissRequest = { balanceAccount = null },
            icon = { Icon(Icons.Default.Edit, contentDescription = null) },
            title = { Text(stringResource(com.example.vibefinance.R.string.assets_edit_balance)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = liveAccount.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(
                            R.string.assets_labeled_balance,
                            accountBalanceLabel(liveAccount), accountBalanceText(liveAccount)
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        visualTransformation = if (com.example.vibefinance.ui.preferences.LocalExperience.current.hideAmounts)
                            androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,

                        value = targetBalanceText,
                        onValueChange = { targetBalanceText = it },
                        label = { Text(stringResource(com.example.vibefinance.R.string.assets_new_balance)) },
                        supportingText = if (liveAccount.type == AccountType.CC) {
                            { Text(stringResource(R.string.ah_credit_balance_help)) }
                        } else null,
                        prefix = { Text("$") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        isError = targetBalanceText.isNotBlank() && targetBalance == null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (delta != null) {
                        Text(
                            text = stringResource(
                                com.example.vibefinance.R.string.assets_balance_change,
                                String.format(Locale.US, "%+,.2f", delta)
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = stringResource(com.example.vibefinance.R.string.assets_balance_history_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                val saveInteraction = remember { MutableInteractionSource() }
                CompletePressButton(
                    onClick = {
                        val value = targetBalance ?: return@CompletePressButton
                        onIntent(FinanceIntent.SaveAccount(
                            account = liveAccount.copy(balance = value),
                            originalBalance = selectedAccount.balance
                        ))
                        balanceAccount = null
                    },
                    enabled = targetBalance != null &&
                        kotlin.math.abs(targetBalance - selectedAccount.balance) >= 0.005,
                    interactionSource = saveInteraction,
                    shapes = ButtonDefaults.shapes(
                        shape = CircleShape,
                        pressedShape = RoundedCornerShape(percent = 32)
                    ),
                    modifier = Modifier.pressBounce(interactionSource = saveInteraction)
                ) {
                    Text(stringResource(com.example.vibefinance.R.string.assets_save_balance))
                }
            },
            dismissButton = {
                TextButton(onClick = { balanceAccount = null }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }

    if (renderEditor) {
        AssetEditorHost(state, editorState, onIntent, saveResult, onAccountCreated, onDismissCreate)
    }
}

}

@Composable
fun CardProtocolLogo(protocol: String?, modifier: Modifier = Modifier) {
    if (protocol == null || protocol.lowercase(Locale.US) == "none") return

    Box(modifier = modifier.height(24.dp).width(38.dp), contentAlignment = Alignment.Center) {
        when (protocol.lowercase(Locale.US)) {
            "visa" -> {
                Text(
                    text = "VISA",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }
            "mastercard" -> {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.height * 0.45f
                    val centerY = size.height / 2f
                    drawCircle(
                        color = Color(0xFFEB001B),
                        radius = radius,
                        center = Offset(size.width * 0.35f, centerY)
                    )
                    drawCircle(
                        color = Color(0xFFF79E1B).copy(alpha = 0.8f),
                        radius = radius,
                        center = Offset(size.width * 0.65f, centerY)
                    )
                }
            }
            "unionpay" -> {
                Box(
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(3.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        listOf(Color(0xFFE21836), Color(0xFF00447C), Color(0xFF007B84)).forEach { color ->
                            Box(Modifier.weight(1f).fillMaxHeight().background(color))
                        }
                    }
                    Text(
                        text = "UnionPay",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontSize = 8.sp,
                        letterSpacing = 0.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
            "jcb" -> {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    listOf("J" to Color(0xFF0062AE), "C" to Color(0xFFD71920), "B" to Color(0xFF009639)).forEach { (letter, color) ->
                        Box(
                            modifier = Modifier.weight(1f).fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp)).background(color),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = letter,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardIssuerLogo(issuer: String?, modifier: Modifier = Modifier) {
    if (issuer == null || issuer.lowercase(Locale.US) == "none") return

    Box(modifier = modifier.height(28.dp).width(45.dp), contentAlignment = Alignment.Center) {
        when (issuer.lowercase(Locale.US)) {
            "boc" -> {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.height * 0.4f
                    val center = Offset(size.width / 2f, size.height / 2f)
                    drawCircle(color = Color(0xFFB31F24), radius = radius, center = center)
                    val squareSize = radius * 0.4f
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(center.x - squareSize/2, center.y - squareSize/2),
                        size = androidx.compose.ui.geometry.Size(squareSize, squareSize)
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(center.x, center.y - radius * 0.8f),
                        end = Offset(center.x, center.y + radius * 0.8f),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
            "hsbc" -> {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val w = size.width * 0.7f
                    val h = size.height * 0.6f

                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(cx - w/2, cy - h/2)
                        lineTo(cx - w/6, cy)
                        lineTo(cx - w/2, cy + h/2)
                        close()
                        moveTo(cx + w/2, cy - h/2)
                        lineTo(cx + w/6, cy)
                        lineTo(cx + w/2, cy + h/2)
                        close()
                    }
                    drawPath(path, color = Color(0xFFDB0011))

                    val centerPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(cx - w/6, cy)
                        lineTo(cx, cy - h/2)
                        lineTo(cx + w/6, cy)
                        lineTo(cx, cy + h/2)
                        close()
                    }
                    drawPath(centerPath, color = Color.White)
                }
            }
            "chase" -> {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val r = size.height * 0.45f

                    for (i in 0..3) {
                        drawContext.canvas.save()
                        drawContext.transform.rotate(degrees = i * 90f, pivot = Offset(cx, cy))
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(cx - r * 0.15f, cy - r * 0.85f)
                            lineTo(cx + r * 0.85f, cy - r * 0.85f)
                            lineTo(cx + r * 0.85f, cy - r * 0.15f)
                            lineTo(cx + r * 0.15f, cy - r * 0.15f)
                            close()
                        }
                        drawPath(path, color = Color(0xFF117ACA))
                        drawContext.canvas.restore()
                    }
                }
            }
            "citi" -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height * 0.45f
                        val rx = size.width * 0.35f
                        val ry = size.height * 0.2f
                        drawArc(
                            color = Color(0xFFEB001B),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(cx - rx, cy - ry),
                            size = androidx.compose.ui.geometry.Size(rx * 2f, ry * 2f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                        )
                    }
                    Text(
                        text = "citi",
                        color = Color(0xFF002D62),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

fun Modifier.rotate3DOnTouch(): Modifier = this.composed {
    var rotationX by remember { mutableStateOf(0f) }
    var rotationY by remember { mutableStateOf(0f) }
    var shineOffsetX by remember { mutableStateOf(0f) }

    val animatedRotationX by animateFloatAsState(
        targetValue = rotationX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )
    val animatedRotationY by animateFloatAsState(
        targetValue = rotationY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )
    val animatedShineOffsetX by animateFloatAsState(
        targetValue = shineOffsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )

    this
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    val changes = event.changes
                    val isAnyPressed = changes.any { it.pressed }
                    if (isAnyPressed) {
                        val change = changes.first()
                        val width = size.width
                        val height = size.height
                        if (width > 0 && height > 0) {
                            val touchX = change.position.x
                            val touchY = change.position.y
                            val maxRotation = 10f
                            val normX = ((touchX / width.toFloat()) - 0.5f).coerceIn(-0.5f, 0.5f) * 2f
                            val normY = ((touchY / height.toFloat()) - 0.5f).coerceIn(-0.5f, 0.5f) * 2f
                            rotationX = -normY * maxRotation
                            rotationY = normX * maxRotation
                            shineOffsetX = normX * width.toFloat()
                        }
                    } else {
                        rotationX = 0f
                        rotationY = 0f
                        shineOffsetX = 0f
                    }
                }
            }
        }
        .graphicsLayer {
            this.rotationX = animatedRotationX
            this.rotationY = animatedRotationY
            cameraDistance = 16f * density
        }
        .drawWithContent {
            drawContent()
            if (animatedRotationX != 0f || animatedRotationY != 0f) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.0f),
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.0f)
                        ),
                        start = Offset(animatedShineOffsetX - size.width * 0.4f, 0f),
                        end = Offset(animatedShineOffsetX + size.width * 0.4f, size.height)
                    ),
                    blendMode = BlendMode.SrcOver
                )
            }
        }
}

@Composable
fun rememberCardImagePainter(filePath: String?): ImageBitmap? {
    if (filePath.isNullOrEmpty()) return null
    return remember(filePath) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

/** Rebuild crop pixels from a private file after recreation; never save bitmaps in a Bundle. */
internal fun loadCropBitmap(path: String): android.graphics.Bitmap? = try {
    BitmapFactory.decodeFile(path) ?: if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
        android.graphics.ImageDecoder.decodeBitmap(android.graphics.ImageDecoder.createSource(File(path)))
    } else null
} catch (_: Exception) {
    null
}

fun saveAndResizeImage(inputStream: java.io.InputStream, targetFile: java.io.File): Boolean {
    return try {
        val bytes = inputStream.readBytes()
        if (bytes.isEmpty()) return false

        val options = android.graphics.BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

        val maxW = 800
        val maxH = 500
        var inSampleSize = 1
        val srcWidth = options.outWidth
        val srcHeight = options.outHeight
        if (srcWidth > maxW || srcHeight > maxH) {
            val halfWidth = srcWidth / 2
            val halfHeight = srcHeight / 2
            while ((halfWidth / inSampleSize) >= maxW && (halfHeight / inSampleSize) >= maxH) {
                inSampleSize *= 2
            }
        }

        val decodeOptions = android.graphics.BitmapFactory.Options().apply {
            inSampleSize = this@apply.inSampleSize
        }
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions) ?: return false

        val finalBitmap = if (bitmap.width > maxW || bitmap.height > maxH) {
            val scaled = android.graphics.Bitmap.createScaledBitmap(bitmap, maxW, maxH, true)
            if (scaled != bitmap) {
                bitmap.recycle()
            }
            scaled
        } else {
            bitmap
        }

        java.io.FileOutputStream(targetFile).use { out ->
            finalBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, out)
        }
        finalBitmap.recycle()
        true
    } catch (e: Throwable) {
        e.printStackTrace()
        false
    }
}

fun saveUriToInternalStorage(context: android.content.Context, uri: Uri, fileName: String): String? {
    android.util.Log.d("VIBE_DEBUG", "saveUriToInternalStorage started for URI: $uri")
    val targetDir = File(context.filesDir, "card_backgrounds").apply { mkdirs() }
    val targetFile = File(targetDir, fileName)
    try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            if (saveAndResizeImage(input, targetFile)) {
                android.util.Log.d("VIBE_DEBUG", "Successfully copied and resized via contentResolver stream to ${targetFile.absolutePath}")
                return targetFile.absolutePath
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("VIBE_DEBUG", "Failed to open resolver stream, trying fallbacks...", e)

        // Fallback 1: Query MediaStore _data column
        try {
            var targetUri = uri
            val pathStr = uri.toString()
            if (pathStr.contains("com.android.providers.media.documents/document/image")) {
                val id = pathStr.substringAfter("image%3A").substringAfter("image:")
                targetUri = Uri.parse("content://media/external/images/media/$id")
                android.util.Log.d("VIBE_DEBUG", "Fallback 1: Translated document URI to MediaStore URI: $targetUri")
            }
            val projection = arrayOf("_data")
            context.contentResolver.query(targetUri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex("_data")
                    if (columnIndex != -1) {
                        val filePath = cursor.getString(columnIndex)
                        android.util.Log.d("VIBE_DEBUG", "Fallback 1: Resolved _data path: $filePath")
                        if (!filePath.isNullOrEmpty()) {
                            val possibleFile = File(filePath)
                            if (possibleFile.exists()) {
                                possibleFile.inputStream().use { input ->
                                    if (saveAndResizeImage(input, targetFile)) {
                                        android.util.Log.d("VIBE_DEBUG", "Fallback 1: Successfully copied and resized to ${targetFile.absolutePath}")
                                        return targetFile.absolutePath
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (cursorEx: Exception) {
            android.util.Log.e("VIBE_DEBUG", "Fallback 1 failed", cursorEx)
        }

        // Fallback 2: Handle primary: / primary%3A emulator document URIs
        try {
            val pathStr = uri.toString()
            android.util.Log.d("VIBE_DEBUG", "Fallback 2: Checking pathStr: $pathStr")
            if (pathStr.contains("primary%3A") || pathStr.contains("primary:")) {
                val relativePath = Uri.decode(pathStr.substringAfter("primary%3A").substringAfter("primary:"))
                val possibleFile = File("/storage/emulated/0/$relativePath")
                android.util.Log.d("VIBE_DEBUG", "Fallback 2: Checking file: ${possibleFile.absolutePath} exists = ${possibleFile.exists()}")
                if (possibleFile.exists()) {
                    possibleFile.inputStream().use { input ->
                        if (saveAndResizeImage(input, targetFile)) {
                            android.util.Log.d("VIBE_DEBUG", "Fallback 2: Successfully copied and resized to ${targetFile.absolutePath}")
                            return targetFile.absolutePath
                        }
                    }
                }
            }
        } catch (fallbackEx: Exception) {
            android.util.Log.e("VIBE_DEBUG", "Fallback 2 failed", fallbackEx)
        }

        // Fallback 3: Query display name and check standard emulator folders
        try {
            val projection = arrayOf("_display_name")
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex("_display_name")
                    if (nameIndex != -1) {
                        val displayName = cursor.getString(nameIndex)
                        android.util.Log.d("VIBE_DEBUG", "Fallback 3: Resolved _display_name: $displayName")
                        if (!displayName.isNullOrEmpty()) {
                            // Try Fallback 4: Query MediaStore by display name for a valid content URI
                            try {
                                val mediaProjection = arrayOf("_id")
                                val selection = "_display_name = ?"
                                val selectionArgs = arrayOf(displayName)
                                context.contentResolver.query(
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    mediaProjection,
                                    selection,
                                    selectionArgs,
                                    null
                                )?.use { mediaCursor ->
                                    if (mediaCursor.moveToFirst()) {
                                        val idIndex = mediaCursor.getColumnIndex("_id")
                                        if (idIndex != -1) {
                                            val id = mediaCursor.getLong(idIndex)
                                            val mediaUri = Uri.parse("content://media/external/images/media/$id")
                                            android.util.Log.d("VIBE_DEBUG", "Fallback 4: Found MediaStore URI for name: $mediaUri")
                                            context.contentResolver.openInputStream(mediaUri)?.use { input ->
                                                if (saveAndResizeImage(input, targetFile)) {
                                                    android.util.Log.d("VIBE_DEBUG", "Fallback 4: Successfully copied and resized to ${targetFile.absolutePath}")
                                                    return targetFile.absolutePath
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (mediaEx: Exception) {
                                android.util.Log.e("VIBE_DEBUG", "Fallback 4 failed", mediaEx)
                            }

                            // Fallback 3 filesystem check (fails under scoped storage but kept as raw legacy)
                            val possiblePaths = arrayOf(
                                "/storage/emulated/0/Download/$displayName",
                                "/storage/emulated/0/Pictures/$displayName",
                                "/storage/emulated/0/DCIM/$displayName",
                                "/storage/emulated/0/$displayName"
                            )
                            for (p in possiblePaths) {
                                val possibleFile = File(p)
                                android.util.Log.d("VIBE_DEBUG", "Fallback 3: Checking path: $p exists = ${possibleFile.exists()}")
                                if (possibleFile.exists()) {
                                    possibleFile.inputStream().use { input ->
                                        if (saveAndResizeImage(input, targetFile)) {
                                            android.util.Log.d("VIBE_DEBUG", "Fallback 3: Successfully copied and resized to ${targetFile.absolutePath}")
                                            return targetFile.absolutePath
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (nameEx: Exception) {
            android.util.Log.e("VIBE_DEBUG", "Fallback 3 failed", nameEx)
        }
    }
    return null
}

fun cropAndSaveCardBackground(
    sourceBitmap: android.graphics.Bitmap,
    rotation: Int,
    zoom: Float,
    offsetX: Float,
    offsetY: Float,
    targetFile: java.io.File
): Boolean {
    return try {
        val matrix = android.graphics.Matrix().apply {
            if (rotation != 0) {
                postRotate(rotation.toFloat())
            }
        }
        val rotated = android.graphics.Bitmap.createBitmap(
            sourceBitmap, 0, 0, sourceBitmap.width, sourceBitmap.height, matrix, true
        )

        val W = rotated.width.toFloat()
        val H = rotated.height.toFloat()
        val targetAspect = 1.6f

        val cropW: Float
        val cropH: Float
        if (W / H > targetAspect) {
            cropH = H / zoom
            cropW = cropH * targetAspect
        } else {
            cropW = W / zoom
            cropH = cropW / targetAspect
        }

        val maxShiftX = (W - cropW) / 2f
        val maxShiftY = (H - cropH) / 2f

        val centerX = W / 2f + offsetX * maxShiftX
        val centerY = H / 2f + offsetY * maxShiftY

        val startX = (centerX - cropW / 2f).coerceIn(0f, W - cropW)
        val startY = (centerY - cropH / 2f).coerceIn(0f, H - cropH)

        val cropped = android.graphics.Bitmap.createBitmap(
            rotated,
            startX.toInt(),
            startY.toInt(),
            cropW.toInt(),
            cropH.toInt()
        )

        val scaled = android.graphics.Bitmap.createScaledBitmap(cropped, 800, 500, true)

        java.io.FileOutputStream(targetFile).use { out ->
            scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, out)
        }

        if (rotated != sourceBitmap) rotated.recycle()
        cropped.recycle()
        scaled.recycle()
        true
    } catch (e: Throwable) {
        e.printStackTrace()
        false
    }
}

fun Modifier.drawCardBackground(
    bitmap: ImageBitmap?,
    themeName: String?,
    patternName: String?,
    isDark: Boolean,
    offsetX: Float = 0f,
    offsetY: Float = 0f,
    scale: Float = 1f,
    customBrush: Brush? = null
): Modifier = this.drawBehind {
    // 1. Draw theme gradient
    val gradient = customBrush ?: getCardGradient(themeName, isDark)
    drawRect(brush = gradient)

    // 2. Draw custom background image with crop, scale, and offset
    if (bitmap != null) {
        val srcW = bitmap.width.toFloat()
        val srcH = bitmap.height.toFloat()
        val dstW = size.width
        val dstH = size.height

        if (srcW > 0f && srcH > 0f && dstW > 0f && dstH > 0f) {
            val shiftX = offsetX * (dstW * 0.4f)
            val shiftY = offsetY * (dstH * 0.4f)

            val reqW = dstW + 2f * kotlin.math.abs(shiftX)
            val reqH = dstH + 2f * kotlin.math.abs(shiftY)
            val coverScale = maxOf(reqW / srcW, reqH / srcH)
            val finalScale = coverScale * scale.coerceIn(0.5f, 3.0f)

            val scaledW = srcW * finalScale
            val scaledH = srcH * finalScale

            val transX = (dstW - scaledW) / 2f + shiftX
            val transY = (dstH - scaledH) / 2f + shiftY

            drawImage(
                image = bitmap,
                dstOffset = IntOffset(transX.roundToInt(), transY.roundToInt()),
                dstSize = IntSize(scaledW.roundToInt(), scaledH.roundToInt())
            )
        }
    }

    // 3. Draw pattern overlays
    if (patternName != null && patternName.lowercase(Locale.US) != "none") {
        when (patternName.lowercase(Locale.US)) {
            "cyber grid" -> {
                val step = 12.dp.toPx()
                val strokeWidth = 0.8f.dp.toPx()
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeWidth
                    )
                    y += step
                }
                var x = 0f
                while (x < size.width) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = strokeWidth
                    )
                    x += step
                }
            }
            "neon waves" -> {
                val wavePath1 = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, size.height * 0.7f)
                    cubicTo(
                        size.width * 0.25f, size.height * 0.5f,
                        size.width * 0.75f, size.height * 0.9f,
                        size.width, size.height * 0.6f
                    )
                }
                drawPath(
                    path = wavePath1,
                    color = Color.White.copy(alpha = 0.12f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                )

                val wavePath2 = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, size.height * 0.4f)
                    cubicTo(
                        size.width * 0.3f, size.height * 0.7f,
                        size.width * 0.7f, size.height * 0.3f,
                        size.width, size.height * 0.5f
                    )
                }
                drawPath(
                    path = wavePath2,
                    color = Color.White.copy(alpha = 0.1f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f.dp.toPx())
                )
            }
            "geometric mesh" -> {
                val w = size.width
                val h = size.height
                val poly1 = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, 0f)
                    lineTo(w * 0.6f, 0f)
                    lineTo(w * 0.3f, h * 0.8f)
                    close()
                }
                drawPath(path = poly1, color = Color.White.copy(alpha = 0.05f))

                val poly2 = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w, h)
                    lineTo(w * 0.4f, h)
                    lineTo(w * 0.7f, h * 0.2f)
                    close()
                }
                drawPath(path = poly2, color = Color.White.copy(alpha = 0.06f))

                val poly3 = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w, 0f)
                    lineTo(w * 0.5f, h * 0.5f)
                    lineTo(w, h * 0.6f)
                    close()
                }
                drawPath(path = poly3, color = Color.White.copy(alpha = 0.04f))
            }
        }
    }
}

fun createTestBgIfNeeded(context: android.content.Context) {
    val testFile = File(context.filesDir, "card_backgrounds/test_bg.png")
    if (!testFile.exists()) {
        try {
            val targetDir = File(context.filesDir, "card_backgrounds").apply { mkdirs() }
            val bitmap = android.graphics.Bitmap.createBitmap(800, 500, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            val paint = android.graphics.Paint()
            val gradient = android.graphics.LinearGradient(
                0f, 0f, 800f, 500f,
                android.graphics.Color.parseColor("#FF0F7B"),
                android.graphics.Color.parseColor("#F89B29"),
                android.graphics.Shader.TileMode.CLAMP
            )
            paint.shader = gradient
            canvas.drawRect(0f, 0f, 800f, 500f, paint)
            java.io.FileOutputStream(testFile).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}


fun createSampleCardBitmap(context: android.content.Context): android.graphics.Bitmap {
    val sampleFile = File(context.filesDir, "card_backgrounds/sample_card_bg.png")
    if (sampleFile.exists()) {
        val bmp = BitmapFactory.decodeFile(sampleFile.absolutePath)
        if (bmp != null) return bmp
    }
    createTestBgIfNeeded(context)
    val fallbackFile = File(context.filesDir, "card_backgrounds/sample_card_bg.png")
    if (fallbackFile.exists()) {
        val bmp = BitmapFactory.decodeFile(fallbackFile.absolutePath)
        if (bmp != null) return bmp
    }
    val bitmap = android.graphics.Bitmap.createBitmap(800, 500, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint().apply {
        shader = android.graphics.LinearGradient(
            0f, 0f, 800f, 500f,
            android.graphics.Color.parseColor("#1A2980"),
            android.graphics.Color.parseColor("#26D0CE"),
            android.graphics.Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, 800f, 500f, paint)
    return bitmap
}

@Composable
fun AccountAppRedirectButton(
    packageName: String,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 26.dp,
    shapeRadius: Dp = 7.dp,
    onLongClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val haptic = rememberVibeHapticFeedback()
    val hapticView = androidx.compose.ui.platform.LocalView.current
    val appIcon = remember(packageName, context) { LocalAppManager.getAppIcon(context, packageName) }
    val shape = RoundedCornerShape(shapeRadius)
    val notInstalledMsg = stringResource(com.example.vibefinance.R.string.assets_linked_app_not_installed)

    if (appIcon == null) return

    Surface(
        modifier = modifier
            .size(sizeDp)
            .bouncyClickable(
                shape = shape,
                onClick = {
                    com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                    val launched = LocalAppManager.launchApp(context, packageName)
                    if (!launched) {
                        Toast.makeText(context, notInstalledMsg, Toast.LENGTH_SHORT).show()
                    }
                },
                onLongClick = onLongClick?.let { action ->
                    {
                        com.example.vibefinance.util.VibeHaptics.heavyLongPress(hapticView)
                        action()
                    }
                }
            ),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f),
        border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Image(
            bitmap = appIcon.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().padding(1.5.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun AppPickerDialog(
    currentPackage: String?,
    onAppSelected: (String?) -> Unit,
    onDismissRequest: () -> Unit,
    loadApps: suspend (Context) -> List<InstalledAppInfo> = LocalAppManager::loadInstalledApps,
    loadIcon: suspend (Context, String) -> Bitmap? = ::loadPickerIcon
) {
    val context = LocalContext.current
    val appLocale = LocalConfiguration.current.locales[0]
    var retryGeneration by remember { mutableStateOf(0) }
    // Show the dialog first. PackageManager scanning and icon decoding must not block composition.
    val catalogResult by produceState<Result<List<InstalledAppInfo>>?>(
        initialValue = null, context, retryGeneration
    ) {
        value = null
        value = try {
            Result.success(loadApps(context))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            Result.failure(failure)
        }
    }
    val installedApps = catalogResult?.getOrNull().orEmpty()
    var searchQuery by remember { mutableStateOf("") }

    val filteredApps = remember(searchQuery, installedApps, appLocale) {
        val q = searchQuery.trim().lowercase(appLocale)
        if (q.isBlank()) {
            installedApps
        } else {
            installedApps.filter {
                it.appName.lowercase(appLocale).contains(q) ||
                it.packageName.lowercase(Locale.US).contains(q)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(26.dp),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(com.example.vibefinance.R.string.assets_linked_app_dialog_title),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = stringResource(com.example.vibefinance.R.string.assets_linked_app_long_press_tip),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(com.example.vibefinance.R.string.assets_linked_app_search_placeholder)) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.ah_clear_search), modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("QuickLaunchAppSearch")
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
                        .testTag("QuickLaunchAppList"),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (catalogResult?.isSuccess == true && !currentPackage.isNullOrBlank() &&
                        !currentPackage.equals("none", ignoreCase = true) && installedApps.none { it.packageName == currentPackage }) {
                        item(key = "unavailable-saved-app") {
                            Text(
                                text = stringResource(R.string.backup_saved_app_unavailable, currentPackage),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(8.dp).testTag("QuickLaunchUnavailableApp")
                            )
                        }
                    }
                    if (searchQuery.isBlank()) {
                        // Option: None / Disable ("none")
                        item {
                            val isNoneSelected = currentPackage == null || currentPackage.equals("none", ignoreCase = true)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bouncyClickable(
                                        shape = RoundedCornerShape(12.dp),
                                        onClick = {
                                            onAppSelected("none")
                                            onDismissRequest()
                                        }
                                    ),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isNoneSelected) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Block, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(com.example.vibefinance.R.string.assets_linked_app_none),
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = stringResource(com.example.vibefinance.R.string.assets_linked_app_none_desc),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isNoneSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = stringResource(com.example.vibefinance.R.string.assets_linked_app_installed_list),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                    }

                    if (catalogResult == null) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                LoadingIndicator(
                                    modifier = Modifier.size(48.dp).testTag("QuickLaunchAppLoading")
                                )
                            }
                        }
                    } else if (catalogResult?.isFailure == true) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(stringResource(com.example.vibefinance.R.string.assets_linked_app_load_failed))
                                TextButton(onClick = { retryGeneration++ }) {
                                    Text(stringResource(com.example.vibefinance.R.string.assets_linked_app_retry))
                                }
                            }
                        }
                    } else if (filteredApps.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(com.example.vibefinance.R.string.assets_linked_app_empty),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(filteredApps, key = { it.packageName }) { app ->
                            // LazyColumn requests only composed rows; icons arrive independently of labels.
                            val iconState = produceState<Bitmap?>(app.iconBitmap, context, app.packageName) {
                                value = loadIcon(context, app.packageName)
                            }
                            val iconBitmap = iconState.value
                            val isSelected = currentPackage == app.packageName
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bouncyClickable(
                                        shape = RoundedCornerShape(12.dp),
                                        onClick = {
                                            onAppSelected(app.packageName)
                                            onDismissRequest()
                                        }
                                    ),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (iconBitmap != null) {
                                        Image(
                                            bitmap = iconBitmap.asImageBitmap(),
                                            contentDescription = app.appName,
                                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Fit
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceContainerHighest),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = app.appName,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = app.packageName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}

private suspend fun loadPickerIcon(context: Context, packageName: String): Bitmap? =
    withContext(Dispatchers.IO) { LocalAppManager.getAppIcon(context, packageName) }

@Composable
private fun CompactAccountRow(
    account: AccountEntity,
    transactions: List<com.example.vibefinance.data.entity.TransactionEntity> = emptyList(),
    isFirstInGroup: Boolean,
    isLastInGroup: Boolean,
    onEditClick: () -> Unit,
    onBalanceClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onPickApp: (AccountEntity) -> Unit = {},
    modifier: Modifier = Modifier,
    onEditWithBounds: ((Rect?) -> Unit)? = null,
) {
    var itemBounds by remember { mutableStateOf<Rect?>(null) }
    val rowShape = RoundedCornerShape(
        topStart = if (isFirstInGroup) 26.dp else 8.dp,
        topEnd = if (isFirstInGroup) 26.dp else 8.dp,
        bottomStart = if (isLastInGroup) 26.dp else 8.dp,
        bottomEnd = if (isLastInGroup) 26.dp else 8.dp
    )
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val displayName = account.displayName()
    val last4 = account.realLastFour()
    val customImage = rememberAccountThumbnail(account.cardImageUri)
    val issuerIcon = account.cardIssuer?.takeIf { it.isNotBlank() && !it.equals("none", ignoreCase = true) }
    val accent = accountAccent(account.type, account.accentColorKey)
    val context = LocalContext.current
    val targetPackage = remember(account.linkedAppPackage, account.name) {
        LocalAppManager.resolveTargetAppPackage(context, account.linkedAppPackage, account.name)
    }
    val closingDay = if (account.type == AccountType.CC) {
        account.billingDate?.takeIf { it in 1..31 }?.let {
            stringResource(com.example.vibefinance.R.string.assets_compact_closing_day, it)
        } ?: stringResource(com.example.vibefinance.R.string.assets_compact_closing_unset)
    } else null

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.5.dp)
            .onGloballyPositioned { coords ->
                if (coords.isAttached) {
                    itemBounds = coords.boundsInRoot()
                }
            }
            .bouncyClickable(shape = rowShape, onClick = {
                onEditWithBounds?.invoke(itemBounds) ?: onEditClick()
            }),
        shape = rowShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.50f else 0.65f),
        border = BorderStroke(
            0.8.dp,
            if (account.accentColorKey == null) MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.25f else 0.40f)
            else accent.foreground.copy(alpha = 0.38f)
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (customImage == null) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(rowShape),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = account.localizedWatermarkText(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1.5).sp,
                            fontFamily = JetBrainsMonoFontFamily
                        ),
                        color = if (isDark) {
                            Color.White.copy(alpha = 0.05f)
                        } else {
                            accent.foreground.copy(alpha = 0.07f)
                        },
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier.graphicsLayer {
                            translationX = 10.dp.toPx()
                        }
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp)
                    .padding(start = 12.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
            // Material 3 Expressive Accent Indicator Strip (Left side)
            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height(28.dp)
                    .clip(CircleShape)
                    .background(accent.foreground.copy(alpha = if (isDark) 0.85f else 0.95f))
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Header row: strictly [name , card image , custom app icon]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        text = displayName,
                        modifier = Modifier.weight(1f, fill = false),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.5.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (customImage != null) {
                        AccountCardThumbnail(account = account, bitmap = customImage, compact = true)
                    } else if (issuerIcon != null) {
                        CardIssuerLogo(issuer = issuerIcon, modifier = Modifier.size(30.dp))
                    }
                    if (targetPackage != null) {
                        AccountAppRedirectButton(
                            packageName = targetPackage,
                            sizeDp = 24.dp,
                            shapeRadius = 6.dp,
                            onLongClick = { onPickApp(account) }
                        )
                    }
                }

                // Subtitle: Micro-badge for last4 and closing day
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (last4 != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = if (isDark) 0.85f else 0.95f),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "•••• $last4",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.5.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (closingDay != null) {
                        Text(
                            text = closingDay,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (displayName != account.name) {
                        Text(
                            text = "· ${account.name}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (account.type == AccountType.CC) CreditCardCycleBadges(account, transactions)
            }

            Column(horizontalAlignment = Alignment.End, modifier = Modifier.testTag("AccountBalance_${account.id}")) {
                Text(
                    text = accountBalanceLabel(account),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    text = accountBalanceText(account),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.5.sp,
                        fontFamily = JetBrainsMonoFontFamily,
                        letterSpacing = (-0.3).sp
                    ),
                    fontWeight = FontWeight.ExtraBold,
                    color = if (account.type == AccountType.CC && account.balance > 0) {
                        if (isDark) Color(0xFFFFB3AD) else Color(0xFFBA1A1A)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1
                )
            }
            CompactAccountAction(
                icon = Icons.Default.Edit,
                contentDescription = stringResource(com.example.vibefinance.R.string.assets_edit_balance),
                onClick = onBalanceClick
            )
            CompactAccountAction(
                icon = Icons.Default.History,
                contentDescription = stringResource(com.example.vibefinance.R.string.assets_view_history),
                onClick = onHistoryClick
            )
        }
    }
}
}

@Composable
private fun CompactAccountAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Surface(
        modifier = Modifier.size(48.dp).bouncyClickable(shape = shape, onClick = onClick),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.72f)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(19.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpressiveAccountListItem(
    account: AccountEntity,
    transactions: List<com.example.vibefinance.data.entity.TransactionEntity> = emptyList(),
    onEditClick: () -> Unit,
    onBalanceClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onPickApp: (AccountEntity) -> Unit = {},
    modifier: Modifier = Modifier,
    onEditWithBounds: ((Rect?) -> Unit)? = null,
) {
    var itemBounds by remember { mutableStateOf<Rect?>(null) }
    val isCC = account.type == AccountType.CC
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val haptic = rememberVibeHapticFeedback()
    val hapticView = androidx.compose.ui.platform.LocalView.current

    val accent = accountAccent(account.type, account.accentColorKey)
    val displayName = account.displayName()
    val last4 = account.realLastFour()
    val customImage = rememberAccountThumbnail(account.cardImageUri)
    val textShadow = if (customImage != null) {
        Shadow(
            color = Color.Black.copy(alpha = if (isDark) 0.55f else 0.22f),
            offset = Offset(0f, 1.5f),
            blurRadius = 4f
        )
    } else null
    val cardTypeName = when (account.type) {
        AccountType.CC -> stringResource(com.example.vibefinance.R.string.assets_type_credit)
        AccountType.INVESTMENT -> stringResource(com.example.vibefinance.R.string.assets_type_investment)
        AccountType.CASH -> stringResource(com.example.vibefinance.R.string.assets_type_cash)
        AccountType.BANK -> stringResource(com.example.vibefinance.R.string.assets_type_bank)
        AccountType.DEBIT -> stringResource(com.example.vibefinance.R.string.assets_type_debit)
    }.uppercase(LocalConfiguration.current.locales[0])
    val context = LocalContext.current
    val targetPackage = remember(account.linkedAppPackage, account.name) {
        LocalAppManager.resolveTargetAppPackage(context, account.linkedAppPackage, account.name)
    }

    // Dedicated Bento Card Container (Subtle, refined M3 surface)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .onGloballyPositioned { coords ->
                if (coords.isAttached) {
                    itemBounds = coords.boundsInRoot()
                }
            }
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow))
            .bouncyClickable(shape = RoundedCornerShape(20.dp), onClick = {
                onEditWithBounds?.invoke(itemBounds) ?: onEditClick()
            }),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(
            alpha = if (customImage != null) (if (isDark) 0.22f else 0.30f) else (if (isDark) 0.60f else 0.85f)
        ),
        border = BorderStroke(
            0.8.dp,
            if (account.accentColorKey == null) {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.25f else 0.35f)
            } else {
                accent.foreground.copy(alpha = 0.40f)
            }
        ),
        shadowElevation = 0.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (customImage != null) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(20.dp))
                        .drawBehind {
                            val srcW = customImage.width.toFloat()
                            val srcH = customImage.height.toFloat()
                            val dstW = size.width
                            val dstH = size.height

                            if (srcW > 0f && srcH > 0f && dstW > 0f && dstH > 0f) {
                                val shiftX = account.cardBgOffsetX * (dstW * 0.4f)
                                val shiftY = account.cardBgOffsetY * (dstH * 0.4f)

                                val reqW = dstW + 2f * kotlin.math.abs(shiftX)
                                val reqH = dstH + 2f * kotlin.math.abs(shiftY)
                                val coverScale = maxOf(reqW / srcW, reqH / srcH)
                                val finalScale = coverScale * account.cardBgScale.coerceIn(0.5f, 3.0f)

                                val scaledW = srcW * finalScale
                                val scaledH = srcH * finalScale

                                val transX = (dstW - scaledW) / 2f + shiftX
                                val transY = (dstH - scaledH) / 2f + shiftY

                                drawImage(
                                    image = customImage,
                                    dstOffset = IntOffset(transX.roundToInt(), transY.roundToInt()),
                                    dstSize = IntSize(scaledW.roundToInt(), scaledH.roundToInt()),
                                    alpha = if (isDark) 0.70f else 0.64f
                                )
                            }
                        }
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.22f else 0.28f),
                                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = if (isDark) 0.45f else 0.52f)
                                )
                            )
                        )
                )
            } else {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = account.localizedWatermarkText(),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-2).sp,
                            fontFamily = JetBrainsMonoFontFamily
                        ),
                        color = if (isDark) {
                            Color.White.copy(alpha = 0.08f)
                        } else {
                            accent.foreground.copy(alpha = 0.09f)
                        },
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier.graphicsLayer {
                            translationX = 18.dp.toPx()
                            translationY = 6.dp.toPx()
                        }
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tier 1: Top Main Row (Name, Card Image, Custom App Icon on Left | Balance & Edit on Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Account Name, Card Image, Custom App Icon & Protocol
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Text(
                                text = displayName,
                                modifier = Modifier.weight(1f, fill = false),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 18.5.sp,
                                    letterSpacing = (-0.3).sp,
                                    shadow = textShadow
                                ),
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (customImage != null) {
                                AccountCardThumbnail(account = account, bitmap = customImage, compact = false)
                            } else if (account.cardIssuer != null && account.cardIssuer != "none") {
                                val avatarBg = accent.background
                                val avatarTint = accent.foreground

                                Surface(
                                    modifier = Modifier.size(34.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = avatarBg,
                                    border = BorderStroke(1.dp, avatarTint.copy(alpha = 0.25f))
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CardIssuerLogo(
                                            issuer = account.cardIssuer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                            if (targetPackage != null) {
                                AccountAppRedirectButton(
                                    packageName = targetPackage,
                                    sizeDp = 28.dp,
                                    shapeRadius = 8.dp,
                                    onLongClick = { onPickApp(account) }
                                )
                            }
                            if (account.cardProtocol != null && account.cardProtocol != "none") {
                                CardProtocolLogo(
                                    protocol = account.cardProtocol,
                                    modifier = Modifier.height(13.dp)
                                )
                            }
                        }

                        if (displayName != account.name) {
                            Text(
                                text = account.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Micro-pill Badges for Card Type & Last4
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = accent.background.copy(alpha = if (isDark) 0.65f else 0.90f),
                                border = BorderStroke(0.5.dp, accent.foreground.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = cardTypeName,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = accent.foreground
                                )
                            }
                            if (last4 != null) {
                                Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.60f else 0.80f),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "•••• $last4",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.5.sp,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Trailing Balance & Edit Action Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.testTag("AccountBalance_${account.id}")) {
                        Text(
                            text = accountBalanceLabel(account),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Text(
                            text = accountBalanceText(account),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 22.sp,
                                fontFamily = JetBrainsMonoFontFamily,
                                letterSpacing = (-0.4).sp,
                                shadow = textShadow
                            ),
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isCC && account.balance > 0) (if (isDark) Color(0xFFFFB3AD) else Color(0xFFBA1A1A)) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDark) 0.08f else 0.05f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.btn_edit_account),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            if (isCC) CreditCardCycleBadges(account, transactions)


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val balanceInteraction = remember { MutableInteractionSource() }
                CompletePressFilledTonalButton(
                    onClick = onBalanceClick,
                    interactionSource = balanceInteraction,
                    shapes = ButtonDefaults.shapes(
                        shape = CircleShape,
                        pressedShape = RoundedCornerShape(percent = 32)
                    ),
                    modifier = Modifier.pressBounce(interactionSource = balanceInteraction)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(stringResource(com.example.vibefinance.R.string.assets_edit_balance))
                }
                val historyInteraction = remember { MutableInteractionSource() }
                CompletePressTextButton(
                    onClick = onHistoryClick,
                    interactionSource = historyInteraction,
                    shapes = ButtonDefaults.shapes(
                        shape = CircleShape,
                        pressedShape = RoundedCornerShape(percent = 32)
                    ),
                    modifier = Modifier.pressBounce(interactionSource = historyInteraction)
                ) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(stringResource(com.example.vibefinance.R.string.assets_view_history))
                }
            }
        }
    }
}
}

@Composable
fun InteractiveImageCropDialog(
    bitmap: android.graphics.Bitmap,
    initialAspectRatio: String,
    onDismiss: () -> Unit,
    onConfirm: (android.graphics.Bitmap, String) -> Unit
) {
    val haptic = rememberVibeHapticFeedback()
    val hapticView = androidx.compose.ui.platform.LocalView.current
    var selectedRatio by rememberSaveable { mutableStateOf(initialAspectRatio) }
    var scale by rememberSaveable { mutableStateOf(1.0f) }
    var offset by rememberSaveable(stateSaver = Saver<Offset, List<Float>>(
        save = { listOf(it.x, it.y) }, restore = { Offset(it[0], it[1]) }
    )) { mutableStateOf(Offset.Zero) }

    val ratioOptions = listOf(
        "1.586:1" to R.string.ah_ratio_card,
        "16:9"    to R.string.ah_ratio_wide,
        "4:3"     to R.string.ah_ratio_photo,
        "1:1"     to R.string.ah_ratio_square,
        "2.35:1"  to R.string.ah_ratio_banner
    )

    val aspectRatioFloat = remember(selectedRatio) {
        when (selectedRatio) {
            "1.586:1" -> 1.586f
            "16:9"    -> 16f / 9f
            "4:3"     -> 4f / 3f
            "2.35:1"  -> 2.35f
            "1:1"     -> 1.0f
            else      -> 1.586f
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.ah_image_position_scale),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.ah_image_crop_tip),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Interactive Crop Viewport Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(Color.Black, RoundedCornerShape(12.dp))
                        .clipToBounds(),
                    contentAlignment = Alignment.Center
                ) {
                    // Frame Box with dynamic aspect ratio
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .aspectRatio(aspectRatioFloat)
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(0.5f, 5.0f)
                                    offset = Offset(offset.x + pan.x, offset.y + pan.y)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = stringResource(R.string.ah_preview),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offset.x,
                                    translationY = offset.y
                                )
                        )
                    }
                }

                // Micro Pan & Reset Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                offset = Offset(offset.x - 20f, offset.y)
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = stringResource(R.string.ah_move_left), modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = {
                                com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                offset = Offset(offset.x + 20f, offset.y)
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = stringResource(R.string.ah_move_right), modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = {
                                com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                offset = Offset(offset.x, offset.y - 20f)
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = stringResource(R.string.ah_move_up), modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = {
                                com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                offset = Offset(offset.x, offset.y + 20f)
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = stringResource(R.string.ah_move_down), modifier = Modifier.size(18.dp))
                        }
                    }
                    if (offset != Offset.Zero || scale != 1.0f) {
                        TextButton(
                            onClick = {
                                com.example.vibefinance.util.VibeHaptics.heavyLongPress(hapticView)
                                offset = Offset.Zero
                                scale = 1.0f
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.ah_reset), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                // Ratio Selector Chips
                Text(
                    text = stringResource(R.string.ah_aspect_ratio),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ratioOptions) { (key, label) ->
                        val isSelected = selectedRatio == key
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                selectedRatio = key
                            },
                            label = {
                                Text(
                                    text = stringResource(label),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            val confirmInteraction = remember { MutableInteractionSource() }
            CompletePressButton(
                onClick = {
                    com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                    val cropped = createCroppedBitmap(bitmap, scale, offset, aspectRatioFloat)
                    onConfirm(cropped, selectedRatio)
                },
                shapes = ButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                interactionSource = confirmInteraction,
                modifier = Modifier.pressBounce(interactionSource = confirmInteraction)
            ) {
                Text(stringResource(R.string.ah_confirm_use))
            }
        },
        dismissButton = {
            val dismissInteraction = remember { MutableInteractionSource() }
            CompletePressTextButton(
                onClick = {
                    com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                    onDismiss()
                },
                shapes = ButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                interactionSource = dismissInteraction,
                modifier = Modifier.pressBounce(interactionSource = dismissInteraction)
            ) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}

fun createCroppedBitmap(
    source: android.graphics.Bitmap,
    scale: Float,
    offset: Offset,
    aspectRatio: Float
): android.graphics.Bitmap {
    val targetWidth = 600
    val targetHeight = (targetWidth / aspectRatio).toInt().coerceAtLeast(1)
    val result = android.graphics.Bitmap.createBitmap(targetWidth, targetHeight, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(result)

    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG)
    val matrix = android.graphics.Matrix()

    val reqW = targetWidth + 2f * kotlin.math.abs(offset.x)
    val reqH = targetHeight + 2f * kotlin.math.abs(offset.y)
    val scaleX = reqW / source.width.toFloat()
    val scaleY = reqH / source.height.toFloat()
    val baseScale = maxOf(scaleX, scaleY)

    val finalScale = baseScale * scale
    val dx = (targetWidth - source.width * finalScale) / 2f + offset.x
    val dy = (targetHeight - source.height * finalScale) / 2f + offset.y

    matrix.setScale(finalScale, finalScale)
    matrix.postTranslate(dx, dy)

    canvas.drawBitmap(source, matrix, paint)
    return result
}
