@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.accounts

import com.example.vibefinance.ui.components.CompletePressButton
import com.example.vibefinance.ui.components.CompletePressFilledTonalButton
import com.example.vibefinance.ui.components.CompletePressOutlinedButton
import com.example.vibefinance.ui.components.CompletePressTextButton
import com.example.vibefinance.ui.components.CompletePressToggleButton

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.material3.Slider
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalConfiguration
import com.example.vibefinance.R
import com.example.vibefinance.ui.common.bouncyClickable
import com.example.vibefinance.ui.common.pressBounce
import android.widget.Toast
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import com.example.vibefinance.util.LocalAppManager
import com.example.vibefinance.ui.common.horizontalFadingEdge
import com.example.vibefinance.theme.BentoCardShape
import com.example.vibefinance.theme.BentoSubCardShape
import com.example.vibefinance.theme.BentoSmallCardShape
import com.example.vibefinance.theme.LocalIsDarkTheme
import androidx.compose.ui.unit.TextUnit
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.tween
import androidx.compose.material3.Surface

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.ui.graphics.luminance
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.filled.Crop
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.material.icons.filled.CalendarMonth
import com.example.vibefinance.ui.main.ExpressiveSegmentedButtonGroup
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.ui.semantics.role
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.testTag
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
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.ui.FinanceIntent

import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.components.MoneySeparationChart
import com.example.vibefinance.ui.components.InteractiveDonutChart
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

fun getCardGradient(themeName: String?, isDark: Boolean): Brush {
    val colors = when (themeName?.lowercase(Locale.US)) {
        "ocean", "ocean breeze" -> listOf(Color(0xFF0D47A1), Color(0xFF1976D2))
        "emerald", "emerald forest" -> listOf(Color(0xFF1B5E20), Color(0xFF388E3C))
        "sunset", "sunset glow" -> listOf(Color(0xFFE65100), Color(0xFFF57C00))
        "purple", "purple neon" -> listOf(Color(0xFF4A148C), Color(0xFF7B1FA2))
        "rose" -> listOf(Color(0xFF880E4F), Color(0xFFC2185B))
        "crimson" -> listOf(Color(0xFFB71C1C), Color(0xFFD32F2F))
        "dark stealth" -> listOf(Color(0xFF1A1D20), Color(0xFF0D0E10))
        else -> listOf(Color(0xFF161B22), Color(0xFF242C37)) // Rich Dark Metallic Slate for Default Cards
    }
    return Brush.verticalGradient(colors)
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
            AccountType.CASH -> "CASH"
            AccountType.BANK -> "BANK"
            AccountType.DEBIT -> "DEBIT"
        }
    }
}


@Composable
private fun accountTypeDisplayLabel(type: AccountType): String = stringResource(
    when (type) {
        AccountType.CASH -> R.string.acc_type_cash
        AccountType.BANK -> R.string.acc_type_bank
        AccountType.DEBIT -> R.string.ah_type_debit
        AccountType.CC -> R.string.acc_type_credit
    }
)

@Composable
private fun accountEditorTabLabel(key: String): String = stringResource(
    when (key) {
        "Billing & Due" -> R.string.ah_tab_billing
        "Card Design" -> R.string.ah_tab_design
        else -> R.string.ah_tab_details
    }
)

@Composable
private fun cardDesignOptionLabel(key: String): String = when (key) {
    "None" -> stringResource(R.string.ah_none)
    "Cyber Grid" -> stringResource(R.string.ah_pattern_cyber_grid)
    "Neon Waves" -> stringResource(R.string.ah_pattern_neon_waves)
    "Geometric Mesh" -> stringResource(R.string.ah_pattern_geometric_mesh)
    else -> key // Payment-network brands keep their official names.
}

@Composable
private fun accountCardThemeLabel(key: String): String = stringResource(
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

private data class AccountAccent(val background: Color, val foreground: Color)

@Composable
private fun accountAccent(type: AccountType, key: String?): AccountAccent {
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
                            text = if (account.type == AccountType.CC) stringResource(R.string.ah_outstanding_debt) else stringResource(R.string.ah_current_balance),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.65f),
                            fontWeight = FontWeight.Bold
                        )
                        com.example.vibefinance.ui.components.RollingNumberText(
                            text = String.format(Locale.US, "$%,.2f", account.balance),
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
    val totalAssets = accounts.filter { it.type != AccountType.CC }.sumOf { it.balance }
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
        AssetFilter.CASH_BANK -> accounts.filter { it.type != AccountType.CC }
        AssetFilter.DEBIT_CARDS -> accounts.filter { it.type == AccountType.DEBIT }
        AssetFilter.CREDIT_CARDS -> accounts.filter { it.type == AccountType.CC }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AssetGroupSelector(
    accounts: List<AccountEntity>,
    selectedFilter: AssetFilter,
    onSelectFilter: (AssetFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val isDarkTheme = LocalIsDarkTheme.current ?: (colors.background.luminance() < 0.5f)
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val spacing = ButtonGroupDefaults.ConnectedSpaceBetween
        val segmentWidth = (maxWidth - spacing * (AssetFilter.entries.size - 1)) / AssetFilter.entries.size
        val indicatorOffset by animateDpAsState(
            targetValue = (segmentWidth + spacing) * selectedFilter.ordinal + (segmentWidth - 32.dp) / 2,
            animationSpec = tween(durationMillis = 220),
            label = "assetGroupIndicator"
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().selectableGroup(),
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
                        AssetFilter.CASH_BANK -> accounts.count { it.type != AccountType.CC }
                        AssetFilter.DEBIT_CARDS -> accounts.count { it.type == AccountType.DEBIT }
                        AssetFilter.CREDIT_CARDS -> accounts.count { it.type == AccountType.CC }
                    }
                    val icon = when (filter) {
                        AssetFilter.ALL -> Icons.Default.Savings
                        AssetFilter.CASH_BANK -> Icons.Default.AccountBalance
                        AssetFilter.DEBIT_CARDS -> Icons.Default.AccountBalanceWallet
                        AssetFilter.CREDIT_CARDS -> Icons.Default.CreditCard
                    }
                    val iconScale = animateFloatAsState(
                        targetValue = if (selected) 1.08f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "assetGroupIcon_$index"
                    )

                    ConnectedButtonRipple {
                        CompletePressToggleButton(
                            checked = selected,
                            onCheckedChange = { onSelectFilter(filter) },
                            interactionSource = interactionSource,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 64.dp)
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
                                    .heightIn(min = 64.dp)
                                    .then(colorMotion.contentModifier)
                                    .padding(horizontal = 4.dp, vertical = 7.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    modifier = Modifier.graphicsLayer {
                                        scaleX = iconScale.value
                                        scaleY = iconScale.value
                                    },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(17.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = count.toString(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .size(width = 32.dp, height = 3.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    state: FinanceUiState,
    onIntent: (FinanceIntent) -> Unit,
    modifier: Modifier = Modifier,
    topContentPadding: Dp = 16.dp,
    onViewAccountHistory: (Long) -> Unit = {}
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val presentationLocale = LocalConfiguration.current.locales[0]
    val haptic = LocalHapticFeedback.current
    val breakdown = remember(state.accounts) { calculateNetWorth(state.accounts) }
    val totalAssets = breakdown.totalAssets
    val totalDebt = breakdown.totalDebt
    val netWorth = breakdown.netWorth

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<AccountEntity?>(null) }
    var balanceAccount by remember { mutableStateOf<AccountEntity?>(null) }
    var accountForAppPicker by remember { mutableStateOf<AccountEntity?>(null) }
    var selectedAssetFilter by remember { mutableStateOf(AssetFilter.ALL) }
    val context = LocalContext.current
    val displayPreferences = remember(context) {
        context.getSharedPreferences("assets_display", android.content.Context.MODE_PRIVATE)
    }
    var isCompactMode by remember(displayPreferences) {
        mutableStateOf(displayPreferences.getBoolean("compact_mode", false))
    }

    val filteredAccounts = remember(state.accounts, selectedAssetFilter) {
        filterAccounts(state.accounts, selectedAssetFilter).sortedWith(
            compareBy<AccountEntity> { it.type.ordinal }
                .thenBy { it.name.lowercase(Locale.getDefault()) }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = state.isLoading,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
            label = "accountsSkeletonCrossfade"
        ) { isLoading ->
            if (isLoading) {
                com.example.vibefinance.ui.components.AccountsScreenSkeleton()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = topContentPadding),
                    verticalArrangement = Arrangement.Top
                ) {
            // 2. Premium Net Asset Value Header Card with Rolling Number Text
            item(key = "assets-summary") {
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
                                        text = String.format(Locale.US, "$%,.2f", netWorth),
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
                                            text = String.format(Locale.US, "$%,.2f", totalAssets),
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
                                            text = String.format(Locale.US, "$%,.2f", totalDebt),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = if (totalDebt > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                thickness = 1.dp
                            )

                            // Money Separation Chart & Proportional Allocation Bar
                            MoneySeparationChart(
                                accounts = state.accounts
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }





            // 2.8. Single-choice expressive account group selector
            item(key = "assets-filter") {
                AssetGroupSelector(
                    accounts = state.accounts,
                    selectedFilter = selectedAssetFilter,
                    onSelectFilter = { selectedAssetFilter = it },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
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
                        CompletePressButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                editingAccount = null
                                showAddEditDialog = true
                            },
                            interactionSource = addAssetInteractionSource,
                            modifier = Modifier
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    val modeInteraction = remember { MutableInteractionSource() }
                    CompletePressFilledTonalButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                        Icon(
                            imageVector = if (isCompactMode) Icons.Default.ViewAgenda else Icons.Default.ViewStream,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(
                            text = stringResource(
                                if (isCompactMode) com.example.vibefinance.R.string.assets_detailed_mode
                                else com.example.vibefinance.R.string.assets_compact_mode
                            )
                        )
                    }
                }
            }
            if (filteredAccounts.isEmpty()) {
                item {
                    GlassmorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
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
                            CompletePressButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    editingAccount = null
                                    showAddEditDialog = true
                                },
                                shapes = ButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                                interactionSource = emptyAddInteraction,
                                modifier = Modifier.pressBounce(interactionSource = emptyAddInteraction)
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
                            }
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
                        itemsIndexed(groupAccounts, key = { _, account -> "account-${account.id}" }) { index, account ->
                            AnimatedContent(
                                targetState = isCompactMode,
                                modifier = Modifier.animateItem(
                                    fadeInSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    placementSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    fadeOutSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                ),
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
                                if (compact) {
                                    CompactAccountRow(
                                        account = account,
                                        isFirstInGroup = index == 0,
                                        isLastInGroup = index == groupAccounts.lastIndex,
                                        onEditClick = {
                                            editingAccount = account
                                            showAddEditDialog = true
                                        },
                                        onBalanceClick = { balanceAccount = account },
                                        onHistoryClick = { onViewAccountHistory(account.id) },
                                        onPickApp = { accountForAppPicker = it }
                                    )
                                } else {
                                    ExpressiveAccountListItem(
                                        account = account,
                                        onEditClick = {
                                            editingAccount = account
                                            showAddEditDialog = true
                                        },
                                        onBalanceClick = { balanceAccount = account },
                                        onHistoryClick = { onViewAccountHistory(account.id) },
                                        onPickApp = { accountForAppPicker = it }
                                    )
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
                            com.example.vibefinance.R.string.assets_current_balance,
                            String.format(Locale.US, "$%,.2f", liveAccount.balance)
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = targetBalanceText,
                        onValueChange = { targetBalanceText = it },
                        label = { Text(stringResource(com.example.vibefinance.R.string.assets_new_balance)) },
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

    // 6. Interactive full CRUD Add / Edit Bottom Sheet
    if (showAddEditDialog) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val coroutineScope = rememberCoroutineScope()
        var showDeleteConfirmDialog by remember { mutableStateOf(false) }

        val context = LocalContext.current
        remember {
            createTestBgIfNeeded(context)
        }
        var nameText by remember { mutableStateOf(editingAccount?.name ?: "") }
        var nicknameText by remember { mutableStateOf(editingAccount?.nickname.orEmpty()) }
        var typeState by remember { mutableStateOf(editingAccount?.type ?: AccountType.BANK) }
        var balanceText by remember { mutableStateOf(editingAccount?.balance?.toString() ?: "0.0") }
        var quickAdjustAmountText by remember { mutableStateOf("") }
        
        var creditLimitText by remember { mutableStateOf(editingAccount?.creditLimit?.toString() ?: "5000") }
        var billingDateText by remember { mutableStateOf(editingAccount?.billingDate?.toString().orEmpty()) }
        var paymentDateText by remember { mutableStateOf(editingAccount?.paymentDate?.toString().orEmpty()) }
        var paymentDeadlineText by remember { mutableStateOf(editingAccount?.paymentDeadline?.toString().orEmpty()) }
        
        var selectedCardTheme by remember { mutableStateOf(editingAccount?.cardTheme ?: "default") }
        var selectedAccentColorKey by remember {
            mutableStateOf(editingAccount?.accentColorKey?.takeIf { it in setOf("primary", "secondary", "tertiary") })
        }
        var selectedLinkedAppPackage by remember { mutableStateOf(editingAccount?.linkedAppPackage) }
        var showAppPickerDialog by remember { mutableStateOf(false) }
        var cardLast4Text by remember { mutableStateOf(editingAccount?.cardLast4 ?: "") }
        var notificationAliasesList by remember(editingAccount?.id) {
            mutableStateOf(editingAccount?.getNotificationAliasList() ?: emptyList())
        }
        var newAliasInput by remember { mutableStateOf("") }
        val defaultAccountName = stringResource(R.string.ah_new_account)
        val invalidLastFour = cardLast4Text.isNotEmpty() &&
            (cardLast4Text.length != 4 || cardLast4Text.any { it !in '0'..'9' })
        var selectedCardProtocol by remember { mutableStateOf(
            when (editingAccount?.cardProtocol?.lowercase(Locale.US)) {
                "visa" -> "Visa"
                "mastercard" -> "Mastercard"
                "unionpay" -> "UnionPay"
                "jcb" -> "JCB"
                else -> "None"
            }
        ) }
        var selectedCardPattern by remember { mutableStateOf(
            when (editingAccount?.cardPattern?.lowercase(Locale.US)) {
                "cyber grid" -> "Cyber Grid"
                "neon waves" -> "Neon Waves"
                "geometric mesh" -> "Geometric Mesh"
                else -> "None"
            }
        ) }
        var selectedCardImageUri by remember { mutableStateOf(editingAccount?.cardImageUri ?: "") }
        var selectedAspectRatio by remember { mutableStateOf(editingAccount?.customImageAspectRatio ?: "1.586:1") }
        var selectedCardBgOffsetX by remember { mutableStateOf(editingAccount?.cardBgOffsetX ?: 0f) }
        var selectedCardBgOffsetY by remember { mutableStateOf(editingAccount?.cardBgOffsetY ?: 0f) }
        var selectedCardBgScale by remember { mutableStateOf(editingAccount?.cardBgScale ?: 1f) }
        var tempBitmapToCrop by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
        var showCropDialog by remember { mutableStateOf(false) }

        val galleryLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                try {
                    val imagesDir = File(context.filesDir, "account_images")
                    if (!imagesDir.exists()) imagesDir.mkdirs()
                    val userPickedFile = File(imagesDir, "raw_user_${System.currentTimeMillis()}.png")

                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(userPickedFile).use { output ->
                            input.copyTo(output)
                        }
                    }

                    if (userPickedFile.exists() && userPickedFile.length() > 0) {
                        var bmap = BitmapFactory.decodeFile(userPickedFile.absolutePath)
                        if (bmap == null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                            try {
                                val source = android.graphics.ImageDecoder.createSource(userPickedFile)
                                bmap = android.graphics.ImageDecoder.decodeBitmap(source)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        if (bmap != null) {
                            tempBitmapToCrop = bmap
                            showCropDialog = true
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        if (showCropDialog && tempBitmapToCrop != null) {
            InteractiveImageCropDialog(
                bitmap = tempBitmapToCrop!!,
                initialAspectRatio = selectedAspectRatio,
                onDismiss = { showCropDialog = false },
                onConfirm = { croppedBitmap, ratio ->
                    try {
                        val imagesDir = File(context.filesDir, "account_images")
                        if (!imagesDir.exists()) imagesDir.mkdirs()
                        val targetFile = File(imagesDir, "cropped_${System.currentTimeMillis()}.png")
                        FileOutputStream(targetFile).use { out ->
                            croppedBitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                        }
                        if (targetFile.exists() && targetFile.length() > 0) {
                            selectedCardImageUri = targetFile.absolutePath
                            selectedAspectRatio = ratio
                            selectedCardBgOffsetX = 0f
                            selectedCardBgOffsetY = 0f
                            selectedCardBgScale = 1f
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    showCropDialog = false
                }
            )
        }
        val previewCardImage = rememberCardImagePainter(selectedCardImageUri)

        // Credit cards expose their billing schedule; other accounts skip that tab.
        val tabs = remember(typeState) {
            if (typeState == AccountType.CC) {
                listOf("Details", "Billing & Due", "Card Design")
            } else {
                listOf("Details", "Card Design")
            }
        }
        var selectedDialogTab by remember { mutableStateOf(0) }
        if (selectedDialogTab >= tabs.size) {
            selectedDialogTab = 0
        }

        var billingDayDropdownExpanded by remember { mutableStateOf(false) }
        var paymentDayDropdownExpanded by remember { mutableStateOf(false) }
        var paymentDeadlineDayDropdownExpanded by remember { mutableStateOf(false) }
        var protocolDropdownExpanded by remember { mutableStateOf(false) }
        var patternDropdownExpanded by remember { mutableStateOf(false) }

        if (showDeleteConfirmDialog && editingAccount != null) {
            val deleteConfirmInteractionSource = remember { MutableInteractionSource() }
            val deleteCancelInteractionSource = remember { MutableInteractionSource() }
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text(stringResource(R.string.ah_delete_account_title), fontWeight = FontWeight.Bold) },
                text = { Text(stringResource(R.string.ah_delete_account_message, editingAccount?.name.orEmpty())) },
                confirmButton = {
                    CompletePressButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showDeleteConfirmDialog = false
                            onIntent(FinanceIntent.DeleteAccount(editingAccount!!))
                            showAddEditDialog = false
                        },
                        interactionSource = deleteConfirmInteractionSource,
                        modifier = Modifier.pressBounce(interactionSource = deleteConfirmInteractionSource),
                        shapes = ButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text(stringResource(R.string.btn_delete), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    CompletePressTextButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showDeleteConfirmDialog = false
                        },
                        interactionSource = deleteCancelInteractionSource,
                        modifier = Modifier.pressBounce(interactionSource = deleteCancelInteractionSource),
                        shapes = ButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32))
                    ) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                }
            )
        }

        if (showAppPickerDialog) {
            AppPickerDialog(
                currentPackage = selectedLinkedAppPackage,
                onAppSelected = { selectedLinkedAppPackage = it },
                onDismissRequest = { showAppPickerDialog = false }
            )
        }

        ModalBottomSheet(
            onDismissRequest = { showAddEditDialog = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                )
            }
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 880.dp)
                    .align(Alignment.CenterHorizontally)
                    .fillMaxHeight(0.92f)
                    .imePadding()
            ) {
                val isWide = maxWidth >= 600.dp

                val previewCard: @Composable () -> Unit = {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(138.dp)
                            .then(
                                if (selectedCardImageUri.isEmpty()) {
                                    Modifier.rotate3DOnTouch()
                                } else {
                                    Modifier.pointerInput(selectedCardImageUri) {
                                        detectTransformGestures { _, pan, zoom, _ ->
                                            val spanX = size.width * 0.4f
                                            val spanY = size.height * 0.4f
                                            if (spanX > 0f && spanY > 0f) {
                                                selectedCardBgOffsetX = ((selectedCardBgOffsetX + pan.x / spanX).coerceIn(-1.0f, 1.0f) * 100).roundToInt() / 100f
                                                selectedCardBgOffsetY = ((selectedCardBgOffsetY + pan.y / spanY).coerceIn(-1.0f, 1.0f) * 100).roundToInt() / 100f
                                            }
                                            if (zoom != 1f) {
                                                selectedCardBgScale = ((selectedCardBgScale * zoom).coerceIn(0.5f, 2.5f) * 20).roundToInt() / 20f
                                            }
                                        }
                                    }
                                }
                            ),
                        shape = RoundedCornerShape(22.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(22.dp))
                                .drawCardBackground(
                                    bitmap = previewCardImage,
                                    themeName = selectedCardTheme,
                                    patternName = selectedCardPattern,
                                    isDark = isDark,
                                    offsetX = selectedCardBgOffsetX,
                                    offsetY = selectedCardBgOffsetY,
                                    scale = selectedCardBgScale
                                )
                        ) {
                            if (selectedCardImageUri.isEmpty()) {
                                val previewFallback = accountTypeDisplayLabel(typeState)
                                val previewWatermark = remember(nameText, nicknameText, previewFallback, presentationLocale) {
                                    val cleanName = nicknameText.trim().ifEmpty { nameText.trim().ifEmpty { previewFallback } }
                                    val tokens = cleanName.split(Regex("[\\s_\\-/]+")).filter { it.isNotBlank() }
                                    val firstToken = tokens.firstOrNull().orEmpty()
                                    if (firstToken.length in 2..10 && firstToken.any { it.isLetter() }) {
                                        firstToken.uppercase(Locale.US)
                                    } else if (cleanName.length in 2..6) {
                                        cleanName.uppercase(presentationLocale)
                                    } else {
                                        previewFallback
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clip(RoundedCornerShape(22.dp)),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Text(
                                        text = previewWatermark,
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
                                verticalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        val previewAccent = accountAccent(typeState, selectedAccentColorKey)
                                        Box(
                                            modifier = Modifier
                                                .padding(end = 8.dp)
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(previewAccent.background)
                                                .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                                        )
                                        Text(
                                            text = nicknameText.trim().ifEmpty { nameText.ifEmpty { stringResource(R.string.ah_card_preview) } },
                                            color = Color.White,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (selectedCardProtocol != "None") {
                                        CardProtocolLogo(protocol = selectedCardProtocol)
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text(
                                            text = when (typeState) {
                                                AccountType.BANK -> accountTypeDisplayLabel(AccountType.BANK)
                                                AccountType.CASH -> accountTypeDisplayLabel(AccountType.CASH)
                                                AccountType.DEBIT -> accountTypeDisplayLabel(AccountType.DEBIT)
                                                AccountType.CC -> accountTypeDisplayLabel(AccountType.CC)
                                            },
                                            color = Color.White.copy(alpha = 0.7f),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 1.sp
                                        )
                                        if (cardLast4Text.length == 4 && typeState != AccountType.CASH) {
                                            Text(
                                                text = "•••• $cardLast4Text",
                                                color = Color.White.copy(alpha = 0.85f),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium
                                            )
                                        } else if (nicknameText.isNotBlank() && nicknameText.trim() != nameText.trim()) {
                                            Text(
                                                text = nameText,
                                                color = Color.White.copy(alpha = 0.85f),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    val formattedBalance = String.format(Locale.US, "$%,.2f", balanceText.toDoubleOrNull() ?: 0.0)
                                    RollingNumberText(
                                        text = formattedBalance,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                val tabSelector: @Composable () -> Unit = {
                    ExpressiveSegmentedButtonGroup(
                        items = tabs,
                        selectedIndex = selectedDialogTab,
                        onItemSelected = { selectedDialogTab = it },
                        iconProvider = { title, tintColor ->
                            val icon = when (title) {
                                "Details" -> Icons.Default.Tune
                                "Billing & Due" -> Icons.Default.CalendarMonth
                                "Card Design" -> Icons.Default.Palette
                                else -> Icons.Default.Info
                            }
                            Icon(imageVector = icon, contentDescription = null, tint = tintColor, modifier = Modifier.size(16.dp))
                        },
                        labelProvider = { accountEditorTabLabel(it) }
                    )
                }

                val tabContent: @Composable () -> Unit = {
                    when (tabs.getOrNull(selectedDialogTab)) {
                        "Details" -> {
                            // Bento 1: Account Identity
                            Surface(
                                shape = BentoCardShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Tune,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Text(
                                            text = stringResource(R.string.ah_account_identity),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            letterSpacing = 1.2.sp
                                        )
                                    }

                                    OutlinedTextField(
                                        value = nameText,
                                        onValueChange = { nameText = it },
                                        label = { Text(stringResource(R.string.ah_account_card_name)) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = when (typeState) {
                                                    AccountType.BANK -> Icons.Default.AccountBalance
                                                    AccountType.CASH -> Icons.Default.Savings
                                                    AccountType.DEBIT -> Icons.Default.CreditCard
                                                    AccountType.CC -> Icons.Default.CreditCard
                                                },
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            focusedBorderColor = MaterialTheme.colorScheme.primary
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = nicknameText,
                                        onValueChange = { nicknameText = it },
                                        label = { Text(stringResource(com.example.vibefinance.R.string.assets_identity_nickname)) },
                                        supportingText = {
                                            Text(stringResource(com.example.vibefinance.R.string.assets_identity_nickname_hint))
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = stringResource(R.string.ah_account_type),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        FlowRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            FilterChip(
                                                selected = typeState == AccountType.BANK,
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    typeState = AccountType.BANK
                                                },
                                                label = { Text(accountTypeDisplayLabel(AccountType.BANK)) },
                                                leadingIcon = {
                                                    Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                                                },
                                                modifier = Modifier.widthIn(min = 100.dp)
                                            )
                                            FilterChip(
                                                selected = typeState == AccountType.CASH,
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    typeState = AccountType.CASH
                                                },
                                                label = { Text(accountTypeDisplayLabel(AccountType.CASH)) },
                                                leadingIcon = {
                                                    Icon(Icons.Default.Savings, contentDescription = null, modifier = Modifier.size(16.dp))
                                                },
                                                modifier = Modifier.widthIn(min = 100.dp)
                                            )
                                            FilterChip(
                                                selected = typeState == AccountType.DEBIT,
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    typeState = AccountType.DEBIT
                                                },
                                                label = { Text(stringResource(com.example.vibefinance.R.string.assets_type_debit)) },
                                                leadingIcon = {
                                                    Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
                                                },
                                                modifier = Modifier.widthIn(min = 100.dp)
                                            )
                                            FilterChip(
                                                selected = typeState == AccountType.CC,
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    typeState = AccountType.CC
                                                },
                                                label = { Text(accountTypeDisplayLabel(AccountType.CC)) },
                                                leadingIcon = {
                                                    Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
                                                },
                                                modifier = Modifier.widthIn(min = 100.dp)
                                            )
                                        }
                                    }

                                    OutlinedTextField(
                                        value = cardLast4Text,
                                        onValueChange = { if (it.length <= 4 && it.all { char -> char in '0'..'9' }) cardLast4Text = it },
                                        label = { Text(stringResource(com.example.vibefinance.R.string.assets_identity_last_four)) },
                                        placeholder = { Text(stringResource(R.string.ah_last_four_example)) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        isError = invalidLastFour,
                                        supportingText = if (invalidLastFour) {
                                            { Text(stringResource(com.example.vibefinance.R.string.assets_identity_last_four_error)) }
                                        } else null,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            focusedBorderColor = MaterialTheme.colorScheme.primary
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Text(
                                        text = stringResource(com.example.vibefinance.R.string.assets_identity_accent),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        listOf(
                                            null to com.example.vibefinance.R.string.assets_identity_accent_default,
                                            "primary" to com.example.vibefinance.R.string.assets_identity_accent_primary,
                                            "secondary" to com.example.vibefinance.R.string.assets_identity_accent_secondary,
                                            "tertiary" to com.example.vibefinance.R.string.assets_identity_accent_tertiary
                                        ).forEach { (key, labelRes) ->
                                            val accent = accountAccent(typeState, key)
                                            FilterChip(
                                                selected = selectedAccentColorKey == key,
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    selectedAccentColorKey = key
                                                },
                                                label = { Text(stringResource(labelRes)) },
                                                leadingIcon = {
                                                    Box(
                                                        Modifier
                                                            .size(16.dp)
                                                            .clip(CircleShape)
                                                            .background(accent.background)
                                                            .border(1.dp, accent.foreground.copy(alpha = 0.45f), CircleShape)
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // Bento: Linked App for Quick Redirect
                            Surface(
                                shape = BentoCardShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.OpenInNew,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = stringResource(com.example.vibefinance.R.string.assets_linked_app_title),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                letterSpacing = 1.2.sp
                                            )
                                            Text(
                                                text = stringResource(com.example.vibefinance.R.string.assets_linked_app_desc),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    val linkedAppShape = RoundedCornerShape(14.dp)
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .bouncyClickable(shape = linkedAppShape, onClick = { showAppPickerDialog = true }),
                                        shape = linkedAppShape,
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            val resolvedAppPkg = LocalAppManager.resolveTargetAppPackage(
                                                context = context,
                                                linkedAppPackage = selectedLinkedAppPackage
                                            )
                                            val resolvedAppIcon = remember(resolvedAppPkg) {
                                                resolvedAppPkg?.let { LocalAppManager.getAppIcon(context, it) }
                                            }
                                            val resolvedAppLabel = remember(resolvedAppPkg) {
                                                resolvedAppPkg?.let { LocalAppManager.getAppLabel(context, it) }
                                            }

                                            if (resolvedAppIcon != null) {
                                                Image(
                                                    bitmap = resolvedAppIcon.asImageBitmap(),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)),
                                                    contentScale = ContentScale.Fit
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Block,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }

                                            Column(modifier = Modifier.weight(1f)) {
                                                val statusText = if (resolvedAppPkg != null && resolvedAppLabel != null) {
                                                    resolvedAppLabel
                                                } else {
                                                    stringResource(com.example.vibefinance.R.string.assets_linked_app_none)
                                                }
                                                Text(
                                                    text = statusText,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }

                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Bento: Notification Auto-Log Identifiers / Aliases
                            Surface(
                                shape = BentoCardShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.NotificationsActive,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = stringResource(com.example.vibefinance.R.string.assets_notification_aliases_title),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = stringResource(com.example.vibefinance.R.string.assets_notification_aliases_desc),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Active alias chips
                                    if (notificationAliasesList.isNotEmpty()) {
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            notificationAliasesList.forEach { alias ->
                                                InputChip(
                                                    selected = true,
                                                    onClick = {
                                                        notificationAliasesList = notificationAliasesList.filterNot { it.equals(alias, ignoreCase = true) }
                                                    },
                                                    label = { Text(alias) },
                                                    trailingIcon = {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = stringResource(com.example.vibefinance.R.string.assets_notification_aliases_empty),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }

                                    // Quick suggestion if cardLast4Text is filled and not in list
                                    if (cardLast4Text.length == 4 && notificationAliasesList.none { it.equals(cardLast4Text, ignoreCase = true) }) {
                                        AssistChip(
                                            onClick = {
                                                notificationAliasesList = notificationAliasesList + cardLast4Text
                                            },
                                            label = { Text(stringResource(com.example.vibefinance.R.string.assets_notification_aliases_suggest_last4, cardLast4Text)) },
                                            leadingIcon = {
                                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            }
                                        )
                                    }

                                    // Input to add a new alias
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = newAliasInput,
                                            onValueChange = { newAliasInput = it },
                                            placeholder = { Text(stringResource(com.example.vibefinance.R.string.assets_notification_aliases_add_placeholder)) },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                focusedBorderColor = MaterialTheme.colorScheme.primary
                                            ),
                                            keyboardActions = KeyboardActions(onDone = {
                                                val trimmed = newAliasInput.trim()
                                                if (trimmed.isNotEmpty() && notificationAliasesList.none { it.equals(trimmed, ignoreCase = true) }) {
                                                    notificationAliasesList = notificationAliasesList + trimmed
                                                    newAliasInput = ""
                                                }
                                            })
                                        )
                                        FilledTonalButton(
                                            onClick = {
                                                val trimmed = newAliasInput.trim()
                                                if (trimmed.isNotEmpty() && notificationAliasesList.none { it.equals(trimmed, ignoreCase = true) }) {
                                                    notificationAliasesList = notificationAliasesList + trimmed
                                                    newAliasInput = ""
                                                }
                                            },
                                            enabled = newAliasInput.trim().isNotEmpty(),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(stringResource(com.example.vibefinance.R.string.assets_notification_aliases_add_btn))
                                        }
                                    }
                                }
                            }

                            // Bento 2: Balance & Quick Adjust
                            Surface(
                                shape = BentoCardShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Savings,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Text(
                                            text = if (typeState == AccountType.CC) stringResource(R.string.ah_current_balance_debt) else stringResource(R.string.ah_current_balance),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            letterSpacing = 1.2.sp
                                        )
                                    }

                                    OutlinedTextField(
                                        value = balanceText,
                                        onValueChange = { balanceText = it },
                                        label = { Text(if (typeState == AccountType.CC) stringResource(R.string.ah_current_debt_amount) else stringResource(R.string.ah_current_balance_amount)) },
                                        prefix = { Text("$ ", fontWeight = FontWeight.Bold) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            focusedBorderColor = MaterialTheme.colorScheme.primary
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = stringResource(R.string.ah_quick_adjust),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalFadingEdge(16.dp, 16.dp)
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            listOf(50.0, 100.0, 500.0, 1000.0).forEach { delta ->
                                                SuggestionChip(
                                                    onClick = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        val curr = balanceText.toDoubleOrNull() ?: 0.0
                                                        balanceText = String.format(Locale.US, "%.2f", curr + delta)
                                                    },
                                                    label = { Text("+$${delta.toInt()}", fontWeight = FontWeight.SemiBold) },
                                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                                                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                    ),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                            }
                                            listOf(50.0, 100.0).forEach { delta ->
                                                SuggestionChip(
                                                    onClick = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        val curr = balanceText.toDoubleOrNull() ?: 0.0
                                                        balanceText = String.format(Locale.US, "%.2f", (curr - delta).coerceAtLeast(0.0))
                                                    },
                                                    label = { Text("-$${delta.toInt()}", fontWeight = FontWeight.SemiBold) },
                                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                                        labelColor = MaterialTheme.colorScheme.onErrorContainer
                                                    ),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = quickAdjustAmountText,
                                            onValueChange = { quickAdjustAmountText = it },
                                            label = { Text(stringResource(R.string.ah_custom_adjust)) },
                                            placeholder = { Text(stringResource(R.string.ah_adjust_example)) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                focusedBorderColor = MaterialTheme.colorScheme.primary
                                            ),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.weight(1f)
                                        )
                                        val plusInteraction = remember { MutableInteractionSource() }
                                        CompletePressFilledTonalButton(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                val currentVal = balanceText.toDoubleOrNull() ?: 0.0
                                                val delta = quickAdjustAmountText.toDoubleOrNull() ?: 0.0
                                                balanceText = String.format(Locale.US, "%.2f", currentVal + delta)
                                            },
                                            shapes = ButtonDefaults.shapes(shape = RoundedCornerShape(16.dp), pressedShape = RoundedCornerShape(12.dp)),
                                            interactionSource = plusInteraction,
                                            modifier = Modifier
                                                .height(56.dp)
                                                .pressBounce(interactionSource = plusInteraction)
                                        ) {
                                            Text("+", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        }
                                        val minusInteraction = remember { MutableInteractionSource() }
                                        CompletePressFilledTonalButton(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                val currentVal = balanceText.toDoubleOrNull() ?: 0.0
                                                val delta = quickAdjustAmountText.toDoubleOrNull() ?: 0.0
                                                balanceText = String.format(Locale.US, "%.2f", (currentVal - delta).coerceAtLeast(0.0))
                                            },
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                                            ),
                                            shapes = ButtonDefaults.shapes(shape = RoundedCornerShape(16.dp), pressedShape = RoundedCornerShape(12.dp)),
                                            interactionSource = minusInteraction,
                                            modifier = Modifier
                                                .height(56.dp)
                                                .pressBounce(interactionSource = minusInteraction)
                                        ) {
                                            Text("-", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Bento 3: CC Limits (if CC)
                            if (typeState == AccountType.CC) {
                                Surface(
                                    shape = BentoCardShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CreditCard,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Text(
                                                text = stringResource(R.string.ah_limit_utilization),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                letterSpacing = 1.2.sp
                                            )
                                        }

                                        OutlinedTextField(
                                            value = creditLimitText,
                                            onValueChange = { creditLimitText = it },
                                            label = { Text(stringResource(R.string.ah_credit_limit_amount)) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                focusedBorderColor = MaterialTheme.colorScheme.primary
                                            ),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        val limitVal = creditLimitText.toDoubleOrNull() ?: 1.0
                                        val debtVal = balanceText.toDoubleOrNull() ?: 0.0
                                        val util = if (limitVal > 0) (debtVal / limitVal).coerceIn(0.0, 1.0).toFloat() else 0f
                                        val utilPercent = (util * 100).toInt()

                                        val animatedUtil by animateFloatAsState(
                                            targetValue = util,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow
                                            ),
                                            label = "utilProgressSpring"
                                        )
                                        val targetColor = when {
                                            utilPercent < 30 -> MaterialTheme.colorScheme.primary
                                            utilPercent < 60 -> Color(0xFFFFA000)
                                            else -> MaterialTheme.colorScheme.error
                                        }
                                        val animatedBarColor by animateColorAsState(
                                            targetValue = targetColor,
                                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                                            label = "utilColorSpring"
                                        )

                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.ah_credit_utilization),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "$utilPercent%",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = animatedBarColor
                                                )
                                            }
                                            LinearProgressIndicator(
                                                progress = { animatedUtil },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(8.dp)
                                                    .clip(CircleShape),
                                                color = animatedBarColor,
                                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        "Billing & Due" -> {
                            Surface(
                                shape = BentoCardShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarMonth,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Text(
                                            text = stringResource(R.string.ah_billing_schedule),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            letterSpacing = 1.2.sp
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        ExposedDropdownMenuBox(
                                            expanded = billingDayDropdownExpanded,
                                            onExpandedChange = { billingDayDropdownExpanded = !billingDayDropdownExpanded },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            OutlinedTextField(
                                                value = billingDateText,
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text(stringResource(com.example.vibefinance.R.string.acc_statement_date)) },
                                                placeholder = { Text(stringResource(com.example.vibefinance.R.string.acc_statement_day_select)) },
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = billingDayDropdownExpanded) },
                                                shape = RoundedCornerShape(14.dp),
                                                modifier = Modifier.menuAnchor().fillMaxWidth()
                                            )
                                            ExposedDropdownMenu(
                                                expanded = billingDayDropdownExpanded,
                                                onDismissRequest = { billingDayDropdownExpanded = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text(stringResource(com.example.vibefinance.R.string.acc_statement_day_clear)) },
                                                    onClick = { billingDateText = ""; billingDayDropdownExpanded = false }
                                                )
                                                (1..31).forEach { day ->
                                                    DropdownMenuItem(text = { Text(day.toString()) }, onClick = { billingDateText = day.toString(); billingDayDropdownExpanded = false })
                                                }
                                            }
                                        }

                                        ExposedDropdownMenuBox(
                                            expanded = paymentDayDropdownExpanded,
                                            onExpandedChange = { paymentDayDropdownExpanded = !paymentDayDropdownExpanded },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            OutlinedTextField(
                                                value = paymentDateText,
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text(stringResource(R.string.ah_payment_due)) },
                                                placeholder = { Text(stringResource(com.example.vibefinance.R.string.acc_statement_day_select)) },
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentDayDropdownExpanded) },
                                                shape = RoundedCornerShape(14.dp),
                                                modifier = Modifier.menuAnchor().fillMaxWidth()
                                            )
                                            ExposedDropdownMenu(
                                                expanded = paymentDayDropdownExpanded,
                                                onDismissRequest = { paymentDayDropdownExpanded = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text(stringResource(com.example.vibefinance.R.string.acc_statement_day_clear)) },
                                                    onClick = { paymentDateText = ""; paymentDayDropdownExpanded = false }
                                                )
                                                (1..31).forEach { day ->
                                                    DropdownMenuItem(text = { Text(day.toString()) }, onClick = { paymentDateText = day.toString(); paymentDayDropdownExpanded = false })
                                                }
                                            }
                                        }
                                    }

                                    ExposedDropdownMenuBox(
                                        expanded = paymentDeadlineDayDropdownExpanded,
                                        onExpandedChange = { paymentDeadlineDayDropdownExpanded = !paymentDeadlineDayDropdownExpanded },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = paymentDeadlineText,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text(stringResource(R.string.ah_payment_deadline_day)) },
                                            placeholder = { Text(stringResource(com.example.vibefinance.R.string.acc_statement_day_select)) },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentDeadlineDayDropdownExpanded) },
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.menuAnchor().fillMaxWidth()
                                        )
                                        ExposedDropdownMenu(
                                            expanded = paymentDeadlineDayDropdownExpanded,
                                            onDismissRequest = { paymentDeadlineDayDropdownExpanded = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text(stringResource(com.example.vibefinance.R.string.acc_statement_day_clear)) },
                                                onClick = { paymentDeadlineText = ""; paymentDeadlineDayDropdownExpanded = false }
                                            )
                                            (1..31).forEach { day ->
                                                DropdownMenuItem(text = { Text(day.toString()) }, onClick = { paymentDeadlineText = day.toString(); paymentDeadlineDayDropdownExpanded = false })
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "Card Design" -> {
                            Surface(
                                shape = BentoCardShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Palette,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Text(
                                            text = stringResource(R.string.ah_gradient_palette),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            letterSpacing = 1.2.sp
                                        )
                                    }

                                    val themes = listOf(
                                        "default" to Pair(Color(0xFF161B22), Color(0xFF242C37)),
                                        "ocean" to Pair(Color(0xFF0D47A1), Color(0xFF1976D2)),
                                        "emerald" to Pair(Color(0xFF1B5E20), Color(0xFF388E3C)),
                                        "sunset" to Pair(Color(0xFFE65100), Color(0xFFF57C00)),
                                        "purple" to Pair(Color(0xFF4A148C), Color(0xFF7B1FA2)),
                                        "rose" to Pair(Color(0xFF880E4F), Color(0xFFC2185B)),
                                        "crimson" to Pair(Color(0xFFB71C1C), Color(0xFFD32F2F))
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalFadingEdge(16.dp, 16.dp)
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        themes.forEach { (themeKey, colors) ->
                                            val isSelected = selectedCardTheme == themeKey
                                            val themeLabel = accountCardThemeLabel(themeKey)
                                            val animatedScale by animateFloatAsState(
                                                targetValue = if (isSelected) 1.15f else 1.0f,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessMediumLow
                                                ),
                                                label = "swatchScale_$themeKey"
                                            )
                                            val animatedBorderWidth by animateDpAsState(
                                                targetValue = if (isSelected) 3.dp else 1.dp,
                                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                                label = "swatchBorder_$themeKey"
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .padding(vertical = 4.dp)
                                                    .size(42.dp)
                                                    .graphicsLayer {
                                                        scaleX = animatedScale
                                                        scaleY = animatedScale
                                                    }
                                                    .clip(CircleShape)
                                                    .background(Brush.linearGradient(listOf(colors.first, colors.second)))
                                                    .border(
                                                        width = animatedBorderWidth,
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.35f),
                                                        shape = CircleShape
                                                    )
                                                    .clickable { selectedCardTheme = themeKey }
                                                    .semantics {
                                                        contentDescription = themeLabel
                                                        selected = isSelected
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = stringResource(R.string.ah_selected),
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                }
                            }

                            Surface(
                                shape = BentoCardShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CreditCard,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Text(
                                            text = stringResource(R.string.ah_protocol),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            letterSpacing = 1.2.sp
                                        )
                                    }

                                    ExposedDropdownMenuBox(
                                        expanded = protocolDropdownExpanded,
                                        onExpandedChange = { protocolDropdownExpanded = !protocolDropdownExpanded },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = cardDesignOptionLabel(selectedCardProtocol),
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text(stringResource(R.string.ah_protocol)) },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = protocolDropdownExpanded) },
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.menuAnchor().fillMaxWidth()
                                        )
                                        ExposedDropdownMenu(
                                            expanded = protocolDropdownExpanded,
                                            onDismissRequest = { protocolDropdownExpanded = false }
                                        ) {
                                            listOf("None", "Visa", "Mastercard", "UnionPay", "JCB").forEach { p ->
                                                DropdownMenuItem(
                                                    text = { Text(cardDesignOptionLabel(p)) },
                                                    onClick = {
                                                        selectedCardProtocol = p
                                                        protocolDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Surface(
                                shape = BentoCardShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Image,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Text(
                                            text = stringResource(R.string.ah_texture_art),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            letterSpacing = 1.2.sp
                                        )
                                    }

                                    ExposedDropdownMenuBox(
                                        expanded = patternDropdownExpanded,
                                        onExpandedChange = { patternDropdownExpanded = !patternDropdownExpanded },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = cardDesignOptionLabel(selectedCardPattern),
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text(stringResource(R.string.ah_pattern_overlay)) },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = patternDropdownExpanded) },
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.menuAnchor().fillMaxWidth()
                                        )
                                        ExposedDropdownMenu(
                                            expanded = patternDropdownExpanded,
                                            onDismissRequest = { patternDropdownExpanded = false }
                                        ) {
                                            listOf("None", "Cyber Grid", "Neon Waves", "Geometric Mesh").forEach { pat ->
                                                DropdownMenuItem(
                                                    text = { Text(cardDesignOptionLabel(pat)) },
                                                    onClick = {
                                                        selectedCardPattern = pat
                                                        patternDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val uploadInteraction = remember { MutableInteractionSource() }
                                        CompletePressOutlinedButton(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                galleryLauncher.launch("image/*")
                                            },
                                            shapes = ButtonDefaults.shapes(shape = RoundedCornerShape(16.dp), pressedShape = RoundedCornerShape(12.dp)),
                                            interactionSource = uploadInteraction,
                                            modifier = Modifier
                                                .weight(1f)
                                                .pressBounce(interactionSource = uploadInteraction)
                                        ) {
                                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (selectedCardImageUri.isEmpty()) stringResource(R.string.ah_upload_image) else stringResource(R.string.ah_change_image))
                                        }
                                        if (selectedCardImageUri.isNotEmpty()) {
                                            val cropInteraction = remember { MutableInteractionSource() }
                                            CompletePressOutlinedButton(
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    try {
                                                        val file = File(selectedCardImageUri)
                                                        if (file.exists()) {
                                                            val bmap = BitmapFactory.decodeFile(file.absolutePath)
                                                            if (bmap != null) {
                                                                tempBitmapToCrop = bmap
                                                                showCropDialog = true
                                                            }
                                                        }
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                    }
                                                },
                                                shapes = ButtonDefaults.shapes(shape = RoundedCornerShape(16.dp), pressedShape = RoundedCornerShape(12.dp)),
                                                interactionSource = cropInteraction,
                                                modifier = Modifier.pressBounce(interactionSource = cropInteraction)
                                            ) {
                                                Icon(Icons.Default.Crop, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(stringResource(R.string.ah_crop))
                                            }

                                            val removeInteraction = remember { MutableInteractionSource() }
                                            CompletePressTextButton(
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    selectedCardImageUri = ""
                                                    selectedCardBgOffsetX = 0f
                                                    selectedCardBgOffsetY = 0f
                                                    selectedCardBgScale = 1f
                                                },
                                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                                shapes = ButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                                                interactionSource = removeInteraction,
                                                modifier = Modifier.pressBounce(interactionSource = removeInteraction)
                                            ) {
                                                Text(stringResource(R.string.ah_remove))
                                            }
                                        }
                                    }

                                    if (selectedCardImageUri.isNotEmpty()) {
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.40f else 0.65f),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(14.dp),
                                                verticalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                // Header Row
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(26.dp)
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Tune,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(15.dp)
                                                            )
                                                        }
                                                        Text(
                                                            text = stringResource(R.string.ah_position_axes),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            letterSpacing = 1.1.sp
                                                        )
                                                    }
                                                    if (selectedCardBgOffsetX != 0f || selectedCardBgOffsetY != 0f || selectedCardBgScale != 1f) {
                                                        TextButton(
                                                            onClick = {
                                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                selectedCardBgOffsetX = 0f
                                                                selectedCardBgOffsetY = 0f
                                                                selectedCardBgScale = 1f
                                                            },
                                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(13.dp))
                                                            Spacer(modifier = Modifier.width(3.dp))
                                                            Text(stringResource(R.string.ah_reset), style = MaterialTheme.typography.labelSmall)
                                                        }
                                                    }
                                                }

                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 10.dp, vertical = 7.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Tune,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Text(
                                                            text = stringResource(R.string.ah_gesture_tip),
                                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                }

                                                // X Axis Controller
                                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = stringResource(R.string.ah_horizontal_axis),
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                        val xPercent = (selectedCardBgOffsetX * 100).roundToInt()
                                                        val xDesc = when {
                                                            xPercent < 0 -> stringResource(R.string.ah_offset_left, -xPercent)
                                                            xPercent > 0 -> stringResource(R.string.ah_offset_right, xPercent)
                                                            else -> stringResource(R.string.ah_center_zero)
                                                        }
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                                        ) {
                                                            Text(
                                                                text = xDesc,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        IconButton(
                                                            onClick = {
                                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                                selectedCardBgOffsetX = ((selectedCardBgOffsetX - 0.05f).coerceIn(-1.0f, 1.0f) * 100).roundToInt() / 100f
                                                            },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(Icons.Default.ChevronLeft, contentDescription = stringResource(R.string.ah_shift_left), modifier = Modifier.size(18.dp))
                                                        }
                                                        Slider(
                                                            value = selectedCardBgOffsetX,
                                                            onValueChange = {
                                                                selectedCardBgOffsetX = (it * 100).roundToInt() / 100f
                                                            },
                                                            valueRange = -1.0f..1.0f,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        IconButton(
                                                            onClick = {
                                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                                selectedCardBgOffsetX = ((selectedCardBgOffsetX + 0.05f).coerceIn(-1.0f, 1.0f) * 100).roundToInt() / 100f
                                                            },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(Icons.Default.ChevronRight, contentDescription = stringResource(R.string.ah_shift_right), modifier = Modifier.size(18.dp))
                                                        }
                                                    }
                                                }

                                                // Y Axis Controller
                                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = stringResource(R.string.ah_vertical_axis),
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                        val yPercent = (selectedCardBgOffsetY * 100).roundToInt()
                                                        val yDesc = when {
                                                            yPercent < 0 -> stringResource(R.string.ah_offset_up, -yPercent)
                                                            yPercent > 0 -> stringResource(R.string.ah_offset_down, yPercent)
                                                            else -> stringResource(R.string.ah_center_zero)
                                                        }
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                                        ) {
                                                            Text(
                                                                text = yDesc,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        IconButton(
                                                            onClick = {
                                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                                selectedCardBgOffsetY = ((selectedCardBgOffsetY - 0.05f).coerceIn(-1.0f, 1.0f) * 100).roundToInt() / 100f
                                                            },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = stringResource(R.string.ah_shift_up), modifier = Modifier.size(18.dp))
                                                        }
                                                        Slider(
                                                            value = selectedCardBgOffsetY,
                                                            onValueChange = {
                                                                selectedCardBgOffsetY = (it * 100).roundToInt() / 100f
                                                            },
                                                            valueRange = -1.0f..1.0f,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        IconButton(
                                                            onClick = {
                                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                                selectedCardBgOffsetY = ((selectedCardBgOffsetY + 0.05f).coerceIn(-1.0f, 1.0f) * 100).roundToInt() / 100f
                                                            },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = stringResource(R.string.ah_shift_down), modifier = Modifier.size(18.dp))
                                                        }
                                                    }
                                                }

                                                // Scale / Zoom Controller
                                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = stringResource(R.string.ah_zoom_scale),
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                                                        ) {
                                                            Text(
                                                                text = String.format(Locale.US, "%.2fx", selectedCardBgScale),
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        IconButton(
                                                            onClick = {
                                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                                selectedCardBgScale = ((selectedCardBgScale - 0.1f).coerceIn(0.5f, 2.5f) * 20).roundToInt() / 20f
                                                            },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(Icons.Default.ZoomOut, contentDescription = stringResource(R.string.ah_zoom_out), modifier = Modifier.size(18.dp))
                                                        }
                                                        Slider(
                                                            value = selectedCardBgScale,
                                                            onValueChange = {
                                                                selectedCardBgScale = (it * 20).roundToInt() / 20f
                                                            },
                                                            valueRange = 0.5f..2.5f,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        IconButton(
                                                            onClick = {
                                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                                selectedCardBgScale = ((selectedCardBgScale + 0.1f).coerceIn(0.5f, 2.5f) * 20).roundToInt() / 20f
                                                            },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(Icons.Default.ZoomIn, contentDescription = stringResource(R.string.ah_zoom_in), modifier = Modifier.size(18.dp))
                                                        }
                                                    }
                                                }

                                                // Quick Preset Alignment Chips
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val presets = listOf(
                                                        R.string.ah_align_center to Pair(0f, 0f),
                                                        R.string.ah_align_top to Pair(0f, -0.30f),
                                                        R.string.ah_align_bottom to Pair(0f, 0.30f),
                                                        R.string.ah_align_left to Pair(-0.30f, 0f),
                                                        R.string.ah_align_right to Pair(0.30f, 0f)
                                                    )
                                                    presets.forEach { (name, pos) ->
                                                        val isCurrent = kotlin.math.abs(selectedCardBgOffsetX - pos.first) < 0.05f &&
                                                                        kotlin.math.abs(selectedCardBgOffsetY - pos.second) < 0.05f
                                                        FilterChip(
                                                            selected = isCurrent,
                                                            onClick = {
                                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                                selectedCardBgOffsetX = pos.first
                                                                selectedCardBgOffsetY = pos.second
                                                            },
                                                            label = { Text(stringResource(name), style = MaterialTheme.typography.labelSmall) }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (editingAccount == null) stringResource(R.string.ah_add_asset_card) else stringResource(R.string.ah_modify_account, editingAccount?.name.orEmpty()),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when (typeState) {
                                    AccountType.BANK -> stringResource(R.string.ah_bank_settings)
                                    AccountType.CASH -> stringResource(R.string.ah_cash_settings)
                                    AccountType.DEBIT -> stringResource(R.string.ah_debit_settings)
                                    AccountType.CC -> stringResource(R.string.ah_credit_settings)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = {
                                coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                                    showAddEditDialog = false
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.btn_close),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isWide) {
                        // Responsive Wide Screen: 2-Pane Side-by-Side Bento Layout
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Left Pane: Sticky Overview (weight 0.44f)
                            Column(
                                modifier = Modifier
                                    .weight(0.44f)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                previewCard()

                                // Live Summary Bento Card
                                Surface(
                                    shape = BentoCardShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = stringResource(R.string.ah_live_overview),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                letterSpacing = 1.2.sp
                                            )
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                            ) {
                                                Text(
                                                    text = when (typeState) {
                                                        AccountType.BANK -> accountTypeDisplayLabel(AccountType.BANK)
                                                        AccountType.CASH -> accountTypeDisplayLabel(AccountType.CASH)
                                                        AccountType.DEBIT -> accountTypeDisplayLabel(AccountType.DEBIT)
                                                        AccountType.CC -> accountTypeDisplayLabel(AccountType.CC)
                                                    },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }

                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = if (typeState == AccountType.CC) stringResource(R.string.ah_debt_title) else stringResource(R.string.ah_available_balance),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            val formattedBal = String.format(Locale.US, "$%,.2f", balanceText.toDoubleOrNull() ?: 0.0)
                                            RollingNumberText(
                                                text = formattedBal,
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        if (typeState == AccountType.CC) {
                                            val limitVal = creditLimitText.toDoubleOrNull() ?: 1.0
                                            val debtVal = balanceText.toDoubleOrNull() ?: 0.0
                                            val util = if (limitVal > 0) (debtVal / limitVal).coerceIn(0.0, 1.0).toFloat() else 0f
                                            val utilPercent = (util * 100).toInt()
                                            val animatedUtil by animateFloatAsState(
                                                targetValue = util,
                                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                                label = "wideUtilProgress"
                                            )
                                            val targetColor = when {
                                                utilPercent < 30 -> MaterialTheme.colorScheme.primary
                                                utilPercent < 60 -> Color(0xFFFFA000)
                                                else -> MaterialTheme.colorScheme.error
                                            }
                                            val animatedBarColor by animateColorAsState(targetValue = targetColor, label = "wideUtilColor")

                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text(stringResource(R.string.ah_utilization), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    Text("$utilPercent%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = animatedBarColor)
                                                }
                                                LinearProgressIndicator(
                                                    progress = { animatedUtil },
                                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                                    color = animatedBarColor,
                                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Right Pane: Tabs & Scrollable Configuration (weight 0.56f)
                            Column(
                                modifier = Modifier
                                    .weight(0.56f)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                tabSelector()

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    tabContent()
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    } else {
                        // Compact Portrait Flow with Pinned Sticky Preview Card & Tab Selector
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            previewCard()
                            tabSelector()
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                tabContent()
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }

                    // Anchored Bottom Sticky Action Bar
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (editingAccount != null) {
                                val deleteInteraction = remember { MutableInteractionSource() }
                                CompletePressOutlinedButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showDeleteConfirmDialog = true
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                                    shapes = ButtonDefaults.shapes(shape = RoundedCornerShape(26.dp), pressedShape = RoundedCornerShape(16.dp)),
                                    interactionSource = deleteInteraction,
                                    modifier = Modifier
                                        .height(52.dp)
                                        .pressBounce(interactionSource = deleteInteraction)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.btn_delete))
                                }
                            }

                            val saveInteraction = remember { MutableInteractionSource() }
                            CompletePressButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    if (invalidLastFour) return@CompletePressButton
                                    val name = nameText.trim().ifEmpty { defaultAccountName }
                                    val balance = balanceText.toDoubleOrNull() ?: 0.0
                                    val limit = creditLimitText.toDoubleOrNull() ?: 0.0
                                    val billing = billingDateText.toIntOrNull()?.takeIf { it in 1..31 }
                                    val payment = paymentDateText.toIntOrNull()?.takeIf { it in 1..31 }
                                    val deadline = paymentDeadlineText.toIntOrNull()?.takeIf { it in 1..31 }

                                    val acc = AccountEntity(
                                        id = editingAccount?.id ?: 0L,
                                        name = name,
                                        type = typeState,
                                        balance = balance,
                                        icon = when (typeState) {
                                            AccountType.CASH -> "wallet"
                                            AccountType.BANK -> "bank"
                                            AccountType.DEBIT -> "debit_card"
                                            AccountType.CC -> "credit_card"
                                        },
                                        creditLimit = if (typeState == AccountType.CC) limit else null,
                                        billingDate = if (typeState == AccountType.CC) billing else null,
                                        paymentDate = if (typeState == AccountType.CC) payment else null,
                                        paymentDeadline = if (typeState == AccountType.CC) deadline else null,
                                        cardTheme = selectedCardTheme,
                                        cardLast4 = cardLast4Text.takeIf { it.length == 4 },
                                        nickname = nicknameText.trim().takeIf { it.isNotEmpty() },
                                        accentColorKey = selectedAccentColorKey,
                                        cardProtocol = if (selectedCardProtocol == "None") null else selectedCardProtocol.lowercase(Locale.US),
                                        // Preserve legacy issuer data after retiring its editor.
                                        cardIssuer = editingAccount?.cardIssuer,
                                        cardPattern = if (selectedCardPattern == "None") null else selectedCardPattern.lowercase(Locale.US),
                                        cardImageUri = if (selectedCardImageUri.isEmpty()) null else selectedCardImageUri,
                                        customImageAspectRatio = selectedAspectRatio,
                                        cardBgOffsetX = selectedCardBgOffsetX,
                                        cardBgOffsetY = selectedCardBgOffsetY,
                                        cardBgScale = selectedCardBgScale,
                                        minSpendThreshold = editingAccount?.minSpendThreshold,
                                        linkedAppPackage = selectedLinkedAppPackage,
                                        notificationAliases = notificationAliasesList.joinToString(", ").takeIf { it.isNotBlank() }
                                    )
                                    onIntent(FinanceIntent.SaveAccount(
                                        account = acc,
                                        originalBalance = editingAccount?.balance
                                    ))
                                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                                        showAddEditDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                enabled = !invalidLastFour,
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 1.dp),
                                shapes = ButtonDefaults.shapes(shape = RoundedCornerShape(26.dp), pressedShape = RoundedCornerShape(16.dp)),
                                interactionSource = saveInteraction,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .pressBounce(interactionSource = saveInteraction)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (editingAccount != null) stringResource(R.string.btn_save_changes) else stringResource(R.string.ah_create_asset), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
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
    scale: Float = 1f
): Modifier = this.drawBehind {
    // 1. Draw theme gradient
    val gradient = getCardGradient(themeName, isDark)
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
    val haptic = LocalHapticFeedback.current
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
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val launched = LocalAppManager.launchApp(context, packageName)
                    if (!launched) {
                        Toast.makeText(context, notInstalledMsg, Toast.LENGTH_SHORT).show()
                    }
                },
                onLongClick = onLongClick?.let { action ->
                    {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
    isFirstInGroup: Boolean,
    isLastInGroup: Boolean,
    onEditClick: () -> Unit,
    onBalanceClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onPickApp: (AccountEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
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
            .bouncyClickable(shape = rowShape, onClick = onEditClick),
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
            }

            // Trailing balance with increased font weight and distinct color
            Text(
                text = String.format(Locale.US, "$%,.2f", account.balance),
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
    onEditClick: () -> Unit,
    onBalanceClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onPickApp: (AccountEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isCC = account.type == AccountType.CC
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val haptic = LocalHapticFeedback.current

    // Imported cards can have an unknown deadline; do not invent a due date for them.
    val paymentDeadlineDay = account.paymentDeadline?.takeIf { isCC && it in 1..31 }
    val today = remember { LocalDate.now() }
    val dueLocalDate = remember(today, paymentDeadlineDay) {
        paymentDeadlineDay?.let { day ->
            val thisMonth = today.withDayOfMonth(day.coerceAtMost(today.lengthOfMonth()))
            if (!thisMonth.isBefore(today)) {
                thisMonth
            } else {
                val nextMonth = today.withDayOfMonth(1).plusMonths(1)
                nextMonth.withDayOfMonth(day.coerceAtMost(nextMonth.lengthOfMonth()))
            }
        }
    }
    val daysRemaining = remember(today, dueLocalDate) {
        dueLocalDate?.let { ChronoUnit.DAYS.between(today, it).toInt() }
    }

    val dueText = daysRemaining?.let { days ->
        when {
            days == 0 -> stringResource(R.string.ah_due_today)
            days == 1 -> stringResource(R.string.ah_due_tomorrow)
            else -> stringResource(R.string.ah_due_in_days, days)
        }
    }

    val dueBadgeColor = if (daysRemaining != null && daysRemaining <= 3) {
        if (isDark) Color(0xFFFFB3AD) else Color(0xFFD93025)
    } else {
        if (isDark) Color(0xFFA8C7FA) else Color(0xFF174EA6)
    }

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
        AccountType.CASH -> stringResource(com.example.vibefinance.R.string.assets_type_cash)
        AccountType.BANK -> stringResource(com.example.vibefinance.R.string.assets_type_bank)
        AccountType.DEBIT -> stringResource(com.example.vibefinance.R.string.assets_type_debit)
    }.uppercase(LocalConfiguration.current.locales[0])
    val context = LocalContext.current
    val targetPackage = remember(account.linkedAppPackage, account.name) {
        LocalAppManager.resolveTargetAppPackage(context, account.linkedAppPackage, account.name)
    }

    val creditLimit = account.creditLimit ?: 0.0
    val hasCreditLimit = isCC && creditLimit > 0.0
    val availableCredit = if (hasCreditLimit) (creditLimit - account.balance).coerceAtLeast(0.0) else 0.0
    val utilizationRatio = if (hasCreditLimit) (account.balance / creditLimit).coerceIn(0.0, 1.0).toFloat() else 0f
    val utilizationPercent = (utilizationRatio * 100).toInt()

    val utilizationColor = when {
        utilizationRatio > 0.70f -> if (isDark) Color(0xFFFFB3AD) else MaterialTheme.colorScheme.error
        utilizationRatio > 0.30f -> Color(0xFFF57C00)
        else -> Color(0xFF2E7D32)
    }

    // Dedicated Bento Card Container (Subtle, refined M3 surface)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow))
            .bouncyClickable(shape = RoundedCornerShape(20.dp), onClick = onEditClick),
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
                    Text(
                        text = String.format(Locale.US, "$%,.2f", account.balance),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 22.sp,
                            fontFamily = JetBrainsMonoFontFamily,
                            letterSpacing = (-0.4).sp,
                            shadow = textShadow
                        ),
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isCC && account.balance > 0) (if (isDark) Color(0xFFFFB3AD) else Color(0xFFBA1A1A)) else MaterialTheme.colorScheme.onSurface
                    )

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

            // Credit-card details remain visible without repeating the group type.
            if (isCC) FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Statement closing day is shown for every credit card, including imported cards without one.
                if (isCC) {
                    val statementDay = account.billingDate?.takeIf { it in 1..31 }
                    val statementText = if (statementDay == null) {
                        stringResource(com.example.vibefinance.R.string.acc_statement_day_unset)
                    } else {
                        stringResource(com.example.vibefinance.R.string.acc_statement_day_value, statementDay)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.35f else 0.65f),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = statementText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Credit Limit & Utilization Pill (if CC with Limit)
                if (hasCreditLimit) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = utilizationColor.copy(alpha = if (isDark) 0.18f else 0.10f),
                        border = BorderStroke(0.5.dp, utilizationColor.copy(alpha = 0.35f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { utilizationRatio },
                                modifier = Modifier.size(13.dp),
                                color = utilizationColor,
                                trackColor = utilizationColor.copy(alpha = 0.20f),
                                strokeWidth = 2.2.dp,
                                strokeCap = StrokeCap.Round
                            )
                            Text(
                                text = stringResource(R.string.ah_credit_used_available, utilizationPercent, availableCredit),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    letterSpacing = (-0.15).sp
                                ),
                                fontWeight = FontWeight.Bold,
                                color = utilizationColor
                            )
                        }
                    }
                }

                // Due Date Pill (if CC)
                if (dueText != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = dueBadgeColor.copy(alpha = if (isDark) 0.20f else 0.12f),
                        border = BorderStroke(0.5.dp, dueBadgeColor.copy(alpha = 0.40f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "⏰ $dueText",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = dueBadgeColor
                            )
                        }
                    }
                }
            }

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
    val haptic = LocalHapticFeedback.current
    var selectedRatio by remember { mutableStateOf(initialAspectRatio) }
    var scale by remember { mutableStateOf(1.0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

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
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                offset = Offset(offset.x - 20f, offset.y)
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = stringResource(R.string.ah_move_left), modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                offset = Offset(offset.x + 20f, offset.y)
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = stringResource(R.string.ah_move_right), modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                offset = Offset(offset.x, offset.y - 20f)
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = stringResource(R.string.ah_move_up), modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedRatio = key
                            },
                            label = {
                                Text(
                                    text = stringResource(label),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
