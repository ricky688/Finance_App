@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.accounts

import com.example.vibefinance.ui.motion.paneEntrance
import com.example.vibefinance.ui.components.AppButtonDefaults
import com.example.vibefinance.ui.components.AppSlider

import androidx.compose.material.icons.automirrored.filled.TrendingUp
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.vibefinance.ui.motion.predictiveBackPage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.material3.ScrollField
import androidx.compose.material3.ScrollFieldDefaults
import androidx.compose.material3.rememberScrollFieldState
import kotlinx.coroutines.flow.drop
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.example.vibefinance.util.rememberVibeHapticFeedback
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.theme.JetBrainsMonoFontFamily
import com.example.vibefinance.ui.AccountSaveResult
import com.example.vibefinance.ui.FinanceIntent
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.common.ContentEntranceViewport
import com.example.vibefinance.ui.common.PageContentEntrance
import com.example.vibefinance.ui.common.ScrollBlurContainer
import com.example.vibefinance.ui.common.bouncyClickable
import com.example.vibefinance.ui.common.horizontalFadingEdge
import com.example.vibefinance.ui.common.pressBounce
import com.example.vibefinance.ui.components.CompletePressButton
import com.example.vibefinance.ui.components.CompletePressFilledTonalButton
import com.example.vibefinance.ui.components.CompletePressOutlinedButton
import com.example.vibefinance.ui.components.CompletePressTextButton
import com.example.vibefinance.ui.components.RollingNumberText
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import com.example.vibefinance.util.LocalAppManager
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/** Full-window form; the historical sheet tag is retained for automation compatibility. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, androidx.compose.animation.ExperimentalSharedTransitionApi::class)
@Composable
fun AssetEditorScreen(
    state: FinanceUiState,
    editorState: AssetEditorState,
    onIntent: (FinanceIntent) -> Unit,
    saveResult: AccountSaveResult? = null,
    onAccountCreated: ((AccountEntity) -> Unit)? = null,
    onDismissCreate: (() -> Unit)? = null,
    transition: Transition<Boolean>? = null,
    transformProgress: Float = 1f,
    /** Large screens: drawn inside the Assets right pane rather than over the whole window. */
    inPane: Boolean = false,
) {
    val fallbackTransition = updateTransition(editorState.isOpen, label = "AssetEditorFallback")
    val effectiveTransition = transition ?: fallbackTransition

    val effectiveProgress = if (transition != null) transformProgress else (if (editorState.isOpen) 1f else 0f)

    val sourceBounds = editorState.cardBounds
    // Adding in the large-screen pane: staggered content entrance and exit, no shared element.
    val paneStagger = inPane && sourceBounds == null
    var destCardBounds by remember { mutableStateOf<Rect?>(null) }
    // The flight starts drawing one frame after the card's slot is measured, so its first frame is
    // never drawn from a stale layout.
    var flightReady by remember { mutableStateOf(editorState.cardBounds == null) }
    LaunchedEffect(destCardBounds != null) {
        if (destCardBounds != null && !flightReady) {
            androidx.compose.runtime.withFrameNanos { }
            flightReady = true
        }
    }
    val density = LocalDensity.current
    val isWideWindow = (com.example.vibefinance.ui.main.LocalMainWindowWidth.current
        ?: LocalConfiguration.current.screenWidthDp.dp) >= 720.dp

    // Compute spatial delta between source (clicked card on right inspector or list) and destination (left preview card)
    val deltaX: Float
    val deltaY: Float
    val initialScaleX: Float
    val initialScaleY: Float

    if (sourceBounds != null && destCardBounds != null) {
        deltaX = sourceBounds.left - destCardBounds!!.left
        deltaY = sourceBounds.top - destCardBounds!!.top
        // Uniform scale (no squashing of the card face or its text), wide enough to start exactly
        // on the source card even when the list card and the preview differ a lot in size.
        val uniform = minOf(
            sourceBounds.width / destCardBounds!!.width.coerceAtLeast(1f),
            sourceBounds.height / destCardBounds!!.height.coerceAtLeast(1f)
        ).coerceIn(0.25f, 4f)
        initialScaleX = uniform
        initialScaleY = uniform
    } else if (paneStagger) {
        // Adding on a large screen: no flight; the card enters with the staggered sections.
        deltaX = 0f
        deltaY = 0f
        initialScaleX = 1f
        initialScaleY = 1f
    } else if (sourceBounds != null) {
        val estimatedDestTopPx = with(density) { 96.dp.toPx() }
        val estimatedDestLeftPx = with(density) { (if (isWideWindow) 24.dp else 16.dp).toPx() }
        deltaX = sourceBounds.left - estimatedDestLeftPx
        deltaY = sourceBounds.top - estimatedDestTopPx
        initialScaleX = 1f
        initialScaleY = 1f
    } else {
        deltaX = 0f
        deltaY = with(density) { 180.dp.toPx() }
        initialScaleX = 0.92f
        initialScaleY = 0.92f
    }

    // 1. Shared-element Container Transform (driven directly by VibeMotion.slowSpatial() spring)
    val cardT = effectiveProgress.coerceIn(0f, 1f)

    val currentCardTranslationX = androidx.compose.ui.util.lerp(deltaX, 0f, cardT)
    val currentCardTranslationY = androidx.compose.ui.util.lerp(deltaY, 0f, cardT)
    val currentCardScaleX = androidx.compose.ui.util.lerp(initialScaleX, 1f, cardT)
    val currentCardScaleY = androidx.compose.ui.util.lerp(initialScaleY, 1f, cardT)
    // Flat during the flight and at rest: the preview card carries no shadow.
    val currentCardElevation = 0.dp

    // 2. Top Bar Animation (Fades and slides down smoothly)
    val topBarProgress = ((effectiveProgress - 0.20f) / 0.80f).coerceIn(0f, 1f)
    val topBarAlpha = if (paneStagger) 1f else FastOutSlowInEasing.transform(topBarProgress)
    val topBarOffsetY = (-36).dp * (1f - topBarAlpha)

    // 3. Form exit alpha (open arrival is driven by staggered PageContentEntrance containers)
    val formProgress = ((effectiveProgress - 0.28f) / 0.72f).coerceIn(0f, 1f)
    val formAlpha = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f).transform(formProgress)
    val closingFormAlpha = if (editorState.isOpen || paneStagger) 1f else formAlpha

    // 4. Scaffold Background Alpha (Smoothly dissolves in as card ascends)
    val scaffoldBgAlpha = if (paneStagger) 1f else FastOutSlowInEasing.transform(effectiveProgress.coerceIn(0f, 1f))
    val animatedContainerColor = (if (inPane) MaterialTheme.colorScheme.surfaceContainerLow
        else MaterialTheme.colorScheme.surface).copy(alpha = scaffoldBgAlpha)

    val editingAccountId = editorState.accountId
    val editingAccount = state.accounts.firstOrNull { it.id == editingAccountId }
    val initialPrefillAccount = editorState.prefillAccount
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val presentationLocale = LocalConfiguration.current.locales[0]
    val haptic = rememberVibeHapticFeedback()
    if (editingAccountId != null && editingAccount == null) {
        if (state.isLoading)
            Surface(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LoadingIndicator()
                }
            }
        else
            LaunchedEffect(editingAccountId) {
                editorState.close()
                onDismissCreate?.invoke()
            }
        return
    }
    val openingAccountJson =
        rememberSaveable(editorState.sessionId) {
            (editingAccount ?: initialPrefillAccount)?.let(::encodeEditorAccount).orEmpty()
        }
    val openingAccount =
        remember(openingAccountJson) {
            openingAccountJson.takeIf { it.isNotEmpty() }?.let(::decodeEditorAccount)
        }
    val coroutineScope = rememberCoroutineScope()
    val editorScrollState = rememberScrollState()
    var showDeleteConfirmDialog by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    var nameText by
        rememberSaveable(editorState.sessionId) {
            mutableStateOf(openingAccount?.name ?: initialPrefillAccount?.name ?: "")
        }
    var nicknameText by
        rememberSaveable(editorState.sessionId) {
            mutableStateOf(openingAccount?.nickname.orEmpty())
        }
    var typeState by
        rememberSaveable(editorState.sessionId) {
            mutableStateOf(openingAccount?.type ?: initialPrefillAccount?.type ?: AccountType.BANK)
        }
    val originalBalance by
        rememberSaveable(editorState.sessionId) {
            mutableStateOf(editingAccount?.balance)
        }
    var balanceText by
        rememberSaveable(editorState.sessionId) {
            mutableStateOf(openingAccount?.balance?.toString() ?: "0.0")
        }
    var quickAdjustAmountText by rememberSaveable { mutableStateOf("") }

    var creditLimitText by
        rememberSaveable(editorState.sessionId) {
            mutableStateOf(openingAccount?.creditLimit?.toString() ?: "5000")
        }
    var billingDateText by
        rememberSaveable(editorState.sessionId) {
            mutableStateOf(openingAccount?.billingDate?.toString().orEmpty())
        }
    var paymentDateText by
        rememberSaveable(editorState.sessionId) {
            mutableStateOf(openingAccount?.paymentDate?.toString().orEmpty())
        }
    var paymentDeadlineText by
        rememberSaveable(editorState.sessionId) {
            mutableStateOf(openingAccount?.paymentDeadline?.toString().orEmpty())
        }

    var selectedCardTheme by
        rememberSaveable(editorState.sessionId) {
            mutableStateOf(openingAccount?.cardTheme ?: "default")
        }
    var selectedAccentColorKey by
        rememberSaveable(editorState.sessionId) {
            mutableStateOf(
                openingAccount?.accentColorKey?.takeIf {
                    it in setOf("primary", "secondary", "tertiary")
                }
            )
        }
    var selectedLinkedAppPackage by
        rememberSaveable(editorState.sessionId) {
            mutableStateOf(
                openingAccount?.linkedAppPackage ?: initialPrefillAccount?.linkedAppPackage
            )
        }
    var showAppPickerDialog by rememberSaveable { mutableStateOf(false) }
    var cardLast4Text by
        rememberSaveable(editorState.sessionId) {
            mutableStateOf(openingAccount?.cardLast4 ?: initialPrefillAccount?.cardLast4 ?: "")
        }
    var notificationAliasesList by
        rememberSaveable(
            editorState.sessionId,
            stateSaver = listSaver<List<String>, String>(save = { it }, restore = { it.toList() }),
        ) {
            mutableStateOf(
                openingAccount?.getNotificationAliasList()
                    ?: initialPrefillAccount?.getNotificationAliasList()
                    ?: emptyList()
            )
        }
    var newAliasInput by rememberSaveable { mutableStateOf("") }
    val defaultAccountName = stringResource(R.string.ah_new_account)
    val invalidLastFour =
        cardLast4Text.isNotEmpty() &&
            (cardLast4Text.length != 4 || cardLast4Text.any { it !in '0'..'9' })
    var selectedCardProtocol by rememberSaveable {
        mutableStateOf(
            when (openingAccount?.cardProtocol?.lowercase(Locale.US)) {
                "visa" -> "Visa"
                "mastercard" -> "Mastercard"
                "unionpay" -> "UnionPay"
                "jcb" -> "JCB"
                else -> "None"
            }
        )
    }
    var selectedCardPattern by rememberSaveable {
        mutableStateOf(
            when (openingAccount?.cardPattern?.lowercase(Locale.US)) {
                "cyber grid" -> "Cyber Grid"
                "neon waves" -> "Neon Waves"
                "geometric mesh" -> "Geometric Mesh"
                else -> "None"
            }
        )
    }
    var selectedCardImageUri by rememberSaveable {
        mutableStateOf(openingAccount?.cardImageUri ?: "")
    }
    var selectedAspectRatio by rememberSaveable {
        mutableStateOf(openingAccount?.customImageAspectRatio ?: "1.586:1")
    }
    var selectedCardBgOffsetX by rememberSaveable {
        mutableStateOf(openingAccount?.cardBgOffsetX ?: 0f)
    }
    var selectedCardBgOffsetY by rememberSaveable {
        mutableStateOf(openingAccount?.cardBgOffsetY ?: 0f)
    }
    var selectedCardBgScale by rememberSaveable {
        mutableStateOf(openingAccount?.cardBgScale ?: 1f)
    }
    var cropSourcePath by rememberSaveable { mutableStateOf<String?>(null) }
    val tempBitmapToCrop = remember(cropSourcePath) { cropSourcePath?.let(::loadCropBitmap) }
    var showCropDialog by rememberSaveable { mutableStateOf(false) }

    val galleryLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
            uri: Uri? ->
            if (uri != null) {
                try {
                    val imagesDir = File(context.filesDir, "account_images")
                    if (!imagesDir.exists()) imagesDir.mkdirs()
                    val userPickedFile =
                        File(imagesDir, "raw_user_${System.currentTimeMillis()}.png")

                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(userPickedFile).use { output ->
                            input.copyTo(output)
                        }
                    }

                    if (userPickedFile.exists() && userPickedFile.length() > 0) {
                        if (loadCropBitmap(userPickedFile.absolutePath) != null) {
                            cropSourcePath = userPickedFile.absolutePath
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
            },
        )
    }
    val previewCardImage = rememberCardImagePainter(selectedCardImageUri)

    val isFromCard = sourceBounds != null
    val isDefaultSurfaceTheme = selectedCardTheme.equals("default", ignoreCase = true) && previewCardImage == null
    val listCardColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(
        alpha = if (previewCardImage != null) (if (isDark) 0.22f else 0.30f) else (if (isDark) 0.60f else 0.85f)
    )
    val openingColors = remember(openingAccount?.cardTheme, isDark) {
        getCardThemeColors(openingAccount?.cardTheme, isDark)
    }
    val openingIsDefault = (openingAccount?.cardTheme.isNullOrBlank() || openingAccount?.cardTheme.equals("default", ignoreCase = true)) && previewCardImage == null
    val targetThemeColors = remember(selectedCardTheme, isDark, isDefaultSurfaceTheme, listCardColor) {
        if (isDefaultSurfaceTheme) listCardColor to listCardColor else getCardThemeColors(selectedCardTheme, isDark)
    }
    val animatedThemeColor1 by animateColorAsState(
        targetValue = targetThemeColors.first,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "themeColor1"
    )
    val animatedThemeColor2 by animateColorAsState(
        targetValue = targetThemeColors.second,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "themeColor2"
    )

    val startCardColor1 = if (isFromCard) (if (openingIsDefault) listCardColor else openingColors.first) else animatedThemeColor1
    val startCardColor2 = if (isFromCard) (if (openingIsDefault) listCardColor else openingColors.second) else animatedThemeColor2
    val startTextColor = if (isFromCard && openingIsDefault) MaterialTheme.colorScheme.onSurface else Color.White
    val startSubtextColor = if (isFromCard && openingIsDefault) MaterialTheme.colorScheme.onSurfaceVariant else Color.White.copy(alpha = 0.7f)
    val startLast4Color = if (isFromCard && openingIsDefault) MaterialTheme.colorScheme.onSurfaceVariant else Color.White.copy(alpha = 0.85f)
    val defaultCardBorderColor = if (selectedAccentColorKey == null) {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.25f else 0.35f)
    } else {
        accountAccent(typeState, selectedAccentColorKey).foreground.copy(alpha = 0.40f)
    }
    val startBorderColor = if (isFromCard && openingIsDefault) defaultCardBorderColor else Color.White.copy(alpha = 0.16f)

    val targetTextColor by animateColorAsState(
        targetValue = if (isDefaultSurfaceTheme) MaterialTheme.colorScheme.onSurface else Color.White,
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "targetTextColor"
    )
    val targetSubtextColor by animateColorAsState(
        targetValue = if (isDefaultSurfaceTheme) MaterialTheme.colorScheme.onSurfaceVariant else Color.White.copy(alpha = 0.7f),
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "targetSubtextColor"
    )
    val targetLast4Color by animateColorAsState(
        targetValue = if (isDefaultSurfaceTheme) MaterialTheme.colorScheme.onSurfaceVariant else Color.White.copy(alpha = 0.85f),
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "targetLast4Color"
    )
    val targetBorderColor by animateColorAsState(
        targetValue = if (isDefaultSurfaceTheme) defaultCardBorderColor else Color.White.copy(alpha = 0.16f),
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "targetBorderColor"
    )

    val currentCardColor1 = androidx.compose.ui.graphics.lerp(startCardColor1, animatedThemeColor1, cardT)
    val currentCardColor2 = androidx.compose.ui.graphics.lerp(startCardColor2, animatedThemeColor2, cardT)
    val animatedCardGradient = Brush.verticalGradient(listOf(currentCardColor1, currentCardColor2))

    val currentTextColor = androidx.compose.ui.graphics.lerp(startTextColor, targetTextColor, cardT)
    val currentSubtextColor = androidx.compose.ui.graphics.lerp(startSubtextColor, targetSubtextColor, cardT)
    val currentLast4Color = androidx.compose.ui.graphics.lerp(startLast4Color, targetLast4Color, cardT)
    val currentBorderColor = androidx.compose.ui.graphics.lerp(startBorderColor, targetBorderColor, cardT)

    val currentPreviewAccent = accountAccent(typeState, selectedAccentColorKey)
    val animatedAccentBg by animateColorAsState(
        targetValue = currentPreviewAccent.background,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "accentBg"
    )
    val startAccentBorderColor = if (isFromCard && openingIsDefault) {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    } else Color.White.copy(alpha = 0.8f)
    val targetAccentBorderColor = if (isDefaultSurfaceTheme) {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    } else Color.White.copy(alpha = 0.8f)
    val currentAccentBorderColor = androidx.compose.ui.graphics.lerp(
        startAccentBorderColor,
        targetAccentBorderColor,
        cardT
    )

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
            title = {
                Text(stringResource(R.string.ah_delete_account_title), fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    stringResource(
                        R.string.ah_delete_account_message,
                        editingAccount?.name.orEmpty(),
                    )
                )
            },
            confirmButton = {
                CompletePressButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showDeleteConfirmDialog = false
                        onIntent(FinanceIntent.DeleteAccount(editingAccount!!))
                        editorState.close()
                    },
                    modifier = Modifier.testTag("AssetEditorDeleteConfirm"),
                    shapes = com.example.vibefinance.ui.components.AppButtonDefaults.shapes(),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
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
                    shapes = com.example.vibefinance.ui.components.AppButtonDefaults.shapes(),
                ) {
                    Text(stringResource(R.string.btn_cancel))
                }
            },
        )
    }

    if (showAppPickerDialog) {
        AppPickerDialog(
            currentPackage = selectedLinkedAppPackage,
            onAppSelected = { selectedLinkedAppPackage = it },
            onDismissRequest = { showAppPickerDialog = false },
        )
    }

    var pendingSaveRequest by rememberSaveable { mutableStateOf<String?>(null) }
    var completed by remember { mutableStateOf(false) }
    LaunchedEffect(editorState.sessionId) {
        // A process restart loses the ViewModel operation, but must not strand the restored draft.
        if (pendingSaveRequest != null && saveResult?.requestId != pendingSaveRequest)
            pendingSaveRequest = null
    }
    val currentResult = saveResult?.takeIf { it.requestId == pendingSaveRequest }
    val saving =
        pendingSaveRequest != null &&
            currentResult !is AccountSaveResult.Failed &&
            currentResult !is AccountSaveResult.Saved
    val draftSignature =
        org.json
            .JSONArray(
                listOf(
                    nameText,
                    nicknameText,
                    typeState.name,
                    balanceText,
                    creditLimitText,
                    billingDateText,
                    paymentDateText,
                    paymentDeadlineText,
                    selectedCardTheme,
                    selectedAccentColorKey,
                    selectedLinkedAppPackage,
                    cardLast4Text,
                    notificationAliasesList,
                    newAliasInput,
                    quickAdjustAmountText,
                    selectedCardProtocol,
                    selectedCardPattern,
                    selectedCardImageUri,
                    selectedAspectRatio,
                    selectedCardBgOffsetX,
                    selectedCardBgOffsetY,
                    selectedCardBgScale,
                )
            )
            .toString()
    val baseline by rememberSaveable { mutableStateOf(draftSignature) }
    var showDiscard by rememberSaveable { mutableStateOf(false) }
    val closeEditor: () -> Unit = {
        if (
            pendingSaveRequest == null ||
                (currentResult is AccountSaveResult.Failed &&
                    currentResult.requestId == pendingSaveRequest)
        ) {
            if (draftSignature != baseline) showDiscard = true
            else {
                editorState.close()
                onDismissCreate?.invoke()
            }
        }
    }
    val imeVisible = WindowInsets.isImeVisible
    // M3 predictive back: the page shrinks and rounds with the gesture before closing.
    val predictiveBack = com.example.vibefinance.ui.motion.rememberPredictiveBackPage(
        enabled =
            !imeVisible &&
                !showCropDialog &&
                !showAppPickerDialog &&
                !showDeleteConfirmDialog &&
                !showDiscard &&
                !billingDayDropdownExpanded &&
                !paymentDayDropdownExpanded &&
                !paymentDeadlineDayDropdownExpanded &&
                !protocolDropdownExpanded &&
                !patternDropdownExpanded
    ) {
        closeEditor()
    }
    LaunchedEffect(currentResult) {
        if (currentResult is AccountSaveResult.Saved && !completed) {
            completed = true
            editorState.close()
            onAccountCreated?.invoke(currentResult.account)
        }
    }
    if (showDiscard)
        AlertDialog(
            onDismissRequest = { showDiscard = false },
            title = { Text(stringResource(R.string.asset_editor_discard_title)) },
            text = { Text(stringResource(R.string.asset_editor_discard_message)) },
            confirmButton = {
                CompletePressTextButton(
                    onClick = {
                        showDiscard = false
                        editorState.close()
                        onDismissCreate?.invoke()
                    },
                    modifier = Modifier.testTag("AssetEditorDiscard"),
                ) {
                    Text(stringResource(R.string.asset_editor_discard))
                }
            },
            dismissButton = {
                CompletePressTextButton(
                    onClick = { showDiscard = false },
                    modifier = Modifier.testTag("AssetEditorKeepEditing"),
                ) {
                    Text(stringResource(R.string.asset_editor_keep_editing))
                }
            },
        )
    val submit: () -> Unit = {
        // Read the mutable request ID at invocation time: two events in one frame must not
        // submit twice before the disabled button has had a chance to recompose.
        if (
            !invalidLastFour &&
                (pendingSaveRequest == null ||
                    (currentResult is AccountSaveResult.Failed &&
                        currentResult.requestId == pendingSaveRequest))
        ) {
            val name = nameText.trim().ifEmpty { defaultAccountName }
            val balance = balanceText.toDoubleOrNull() ?: 0.0
            val limit = creditLimitText.toDoubleOrNull() ?: 0.0
            val billing = billingDateText.toIntOrNull()?.takeIf { it in 1..31 }
            val payment = paymentDateText.toIntOrNull()?.takeIf { it in 1..31 }
            val deadline = paymentDeadlineText.toIntOrNull()?.takeIf { it in 1..31 }

            val acc =
                AccountEntity(
                    id = editingAccount?.id ?: 0L,
                    name = name,
                    type = typeState,
                    balance = balance,
                    icon =
                        when (typeState) {
                            AccountType.CASH -> "wallet"
                            AccountType.BANK -> "bank"
                            AccountType.DEBIT -> "debit_card"
                            AccountType.CC -> "credit_card"
                            AccountType.INVESTMENT -> "investment"
                        },
                    creditLimit = if (typeState == AccountType.CC) limit else null,
                    billingDate = if (typeState == AccountType.CC) billing else null,
                    paymentDate = if (typeState == AccountType.CC) payment else null,
                    paymentDeadline = if (typeState == AccountType.CC) deadline else null,
                    cardTheme = selectedCardTheme,
                    cardLast4 = cardLast4Text.takeIf { it.length == 4 },
                    nickname = nicknameText.trim().takeIf { it.isNotEmpty() },
                    accentColorKey = selectedAccentColorKey,
                    cardProtocol =
                        if (selectedCardProtocol == "None") null
                        else selectedCardProtocol.lowercase(Locale.US),
                    // Preserve legacy issuer data after retiring its editor.
                    cardIssuer = openingAccount?.cardIssuer,
                    cardPattern =
                        if (selectedCardPattern == "None") null
                        else selectedCardPattern.lowercase(Locale.US),
                    cardImageUri =
                        if (selectedCardImageUri.isEmpty()) null else selectedCardImageUri,
                    customImageAspectRatio = selectedAspectRatio,
                    cardBgOffsetX = selectedCardBgOffsetX,
                    cardBgOffsetY = selectedCardBgOffsetY,
                    cardBgScale = selectedCardBgScale,
                    minSpendThreshold = openingAccount?.minSpendThreshold,
                    linkedAppPackage = selectedLinkedAppPackage,
                    notificationAliases =
                        notificationAliasesList.joinToString(", ").takeIf { it.isNotBlank() },
                )

            pendingSaveRequest = editorState.sessionId + ":" + UUID.randomUUID().toString()
            onIntent(FinanceIntent.SaveAccount(acc, originalBalance, pendingSaveRequest))
        }
    }
    // Adding in the pane: staggered entrance on open and staggered exit on close. Elsewhere this
    // wrapper is inert (no stagger state is provided to the sections).
    com.example.vibefinance.ui.motion.StaggeredPane(
        Modifier.fillMaxSize(),
        exiting = if (paneStagger) !editorState.isOpen else false,
        enabled = paneStagger
    ) { paneEntranceState ->
    androidx.compose.runtime.CompositionLocalProvider(
        com.example.vibefinance.ui.common.LocalContentEntranceDisabled provides paneStagger,
        com.example.vibefinance.ui.motion.LocalPaneEntrance provides paneEntranceState.takeIf { paneStagger }
    ) {
    Scaffold(
        modifier =
            Modifier.fillMaxSize()
                .predictiveBackPage(predictiveBack)
                .then(if (inPane) Modifier.clip(RoundedCornerShape(28.dp)) else Modifier)
                .testTag("AssetEditorSheet"),
        containerColor = animatedContainerColor,
        contentWindowInsets = if (inPane) WindowInsets(0) else androidx.compose.material3.ScaffoldDefaults.contentWindowInsets,
        topBar = {
            TopAppBar(
                windowInsets = if (inPane) WindowInsets(0) else androidx.compose.material3.TopAppBarDefaults.windowInsets,
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = if (inPane) Color.Transparent else MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.paneEntrance(0).graphicsLayer {
                    alpha = topBarAlpha
                    translationY = topBarOffsetY.toPx()
                },
                title = {
                    Text(
                        if (editingAccount == null) stringResource(R.string.ah_add_asset_card)
                        else stringResource(R.string.ah_modify_account, editingAccount.name),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = closeEditor, enabled = !saving) {
                        Icon(Icons.Default.Close, stringResource(R.string.btn_close))
                    }
                },
                actions = {
                    CompletePressFilledTonalButton(
                        onClick = submit,
                        enabled = !saving && !invalidLastFour,
                        modifier =
                            Modifier.heightIn(min = 48.dp)
                                .padding(end = 8.dp)
                                .testTag("AssetEditorSave"),
                        shapes = com.example.vibefinance.ui.components.AppButtonDefaults.largeShapes(),
                    ) {
                        Text(
                            stringResource(
                                if (saving) R.string.asset_editor_saving else R.string.btn_save
                            ),
                            maxLines = 1,
                        )
                    }
                },
            )
        },
    ) { padding ->
        // Shared element first: the form's staggered entrance waits until the card has landed.
        val entranceGate = androidx.compose.runtime.rememberUpdatedState(
            sourceBounds == null || effectiveProgress >= 0.94f || !editorState.isOpen
        )
        androidx.compose.runtime.CompositionLocalProvider(
            com.example.vibefinance.ui.common.LocalEntranceGate provides entranceGate
        ) {
        ContentEntranceViewport(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding(),
            tagPrefix = "AssetEditorArrival"
        ) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val isWide = maxWidth >= 720.dp
                val editorBlurActive =
                    LocalWindowInfo.current.isWindowFocused &&
                        !showCropDialog &&
                        !showAppPickerDialog &&
                        !showDeleteConfirmDialog &&
                        !showDiscard &&
                        !billingDayDropdownExpanded &&
                        !paymentDayDropdownExpanded &&
                        !paymentDeadlineDayDropdownExpanded &&
                        !protocolDropdownExpanded &&
                        !patternDropdownExpanded
                val cyclePreview: @Composable () -> Unit = {
                    if (typeState == AccountType.CC) {
                        PageContentEntrance("editor-cycle-preview", modifier = Modifier.paneEntrance(2), delayMillis = 40) {
                            Box(
                                modifier = Modifier.graphicsLayer {
                                    alpha = closingFormAlpha
                                }
                            ) {
                                CreditCardCycleBadges(
                                    AccountEntity(id = editingAccountId ?: 0L, name = "", type = typeState, balance = 0.0, icon = "",
                                        creditLimit = creditLimitText.toDoubleOrNull(), billingDate = billingDateText.toIntOrNull(),
                                        paymentDate = paymentDateText.toIntOrNull(), paymentDeadline = paymentDeadlineText.toIntOrNull()), state.transactions)
                            }
                        }
                    }
                }
                val previewCard: @Composable () -> Unit = {
                    Box(
                        modifier = Modifier
                            .paneEntrance(1)
                            .fillMaxWidth()
                            .onGloballyPositioned { coords ->
                                if (coords.isAttached) {
                                    destCardBounds = coords.boundsInRoot()
                                }
                            }
                    ) {
                        val flightScope = com.example.vibefinance.ui.motion.LocalSharedTransitionScope.current
                        Card(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .height(160.dp)
                                    // In flight the card is drawn above everything, so it leaves the
                                    // pressed control itself rather than appearing at the pane edge.
                                    .then(
                                        if (flightScope != null && sourceBounds != null) with(flightScope) {
                                            Modifier.renderInSharedTransitionScopeOverlay(
                                                zIndexInOverlay = 2f,
                                                renderInOverlay = { flightReady && destCardBounds != null && editorState.isOpen && cardT < 0.999f }
                                            )
                                        } else Modifier
                                    )
                                    .graphicsLayer {
                                        transformOrigin = TransformOrigin(0f, 0f)
                                        translationX = currentCardTranslationX
                                        translationY = currentCardTranslationY
                                        scaleX = currentCardScaleX
                                        scaleY = currentCardScaleY
                                        shadowElevation = currentCardElevation.toPx()
                                        // Until its slot is measured the flight start is only an estimate:
                                        // keep the card hidden for that frame rather than flash it elsewhere.
                                        alpha = if (sourceBounds != null && (destCardBounds == null || !flightReady)) 0f else 1f
                                    }
                                    .testTag("AssetEditorPreview")
                            .then(
                                if (selectedCardImageUri.isEmpty()) {
                                    Modifier
                                } else {
                                    Modifier.pointerInput(selectedCardImageUri) {
                                        detectCardImageTransform { pan, zoom ->
                                            val spanX = size.width * 0.4f
                                            val spanY = size.height * 0.4f
                                            if (spanX > 0f && spanY > 0f) {
                                                selectedCardBgOffsetX =
                                                    ((selectedCardBgOffsetX + pan.x / spanX)
                                                            .coerceIn(-1.0f, 1.0f) * 100)
                                                        .roundToInt() / 100f
                                                selectedCardBgOffsetY =
                                                    ((selectedCardBgOffsetY + pan.y / spanY)
                                                            .coerceIn(-1.0f, 1.0f) * 100)
                                                        .roundToInt() / 100f
                                            }
                                            if (zoom != 1f) {
                                                selectedCardBgScale =
                                                    ((selectedCardBgScale * zoom).coerceIn(
                                                            0.5f,
                                                            2.5f,
                                                        ) * 20)
                                                        .roundToInt() / 20f
                                            }
                                        }
                                    }
                                }
                            ),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(0.8.dp, currentBorderColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Box(
                        modifier =
                            Modifier.fillMaxSize()
                                .clip(RoundedCornerShape(20.dp))
                                .drawCardBackground(
                                    bitmap = previewCardImage,
                                    themeName = selectedCardTheme,
                                    patternName = selectedCardPattern,
                                    isDark = isDark,
                                    offsetX = selectedCardBgOffsetX,
                                    offsetY = selectedCardBgOffsetY,
                                    scale = selectedCardBgScale,
                                    customBrush = animatedCardGradient,
                                )
                    ) {
                        if (selectedCardImageUri.isEmpty()) {
                            val previewFallback = accountTypeDisplayLabel(typeState)
                            val previewWatermark =
                                remember(
                                    nameText,
                                    nicknameText,
                                    previewFallback,
                                    presentationLocale,
                                ) {
                                    val cleanName =
                                        nicknameText.trim().ifEmpty {
                                            nameText.trim().ifEmpty { previewFallback }
                                        }
                                    val tokens =
                                        cleanName.split(Regex("[\\s_\\-/]+")).filter {
                                            it.isNotBlank()
                                        }
                                    val firstToken = tokens.firstOrNull().orEmpty()
                                    if (
                                        firstToken.length in 2..10 &&
                                            firstToken.any { it.isLetter() }
                                    ) {
                                        firstToken.uppercase(Locale.US)
                                    } else if (cleanName.length in 2..6) {
                                        cleanName.uppercase(presentationLocale)
                                    } else {
                                        previewFallback
                                    }
                                }
                            Box(
                                modifier =
                                    Modifier.matchParentSize().clip(RoundedCornerShape(20.dp)),
                                contentAlignment = Alignment.CenterEnd,
                            ) {
                                Text(
                                    text = previewWatermark,
                                    style =
                                        MaterialTheme.typography.displayLarge.copy(
                                            fontSize = 58.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = (-2).sp,
                                            fontFamily = JetBrainsMonoFontFamily,
                                        ),
                                    color = androidx.compose.ui.graphics.lerp(
                                        if (openingIsDefault && !isDark) currentPreviewAccent.foreground.copy(alpha = 0.09f) else Color.White.copy(alpha = 0.08f),
                                        if (isDefaultSurfaceTheme && !isDark) currentPreviewAccent.foreground.copy(alpha = 0.09f)
                                        else if (isDefaultSurfaceTheme) Color.White.copy(alpha = 0.08f)
                                        else Color.White.copy(alpha = 0.12f),
                                        cardT
                                    ),
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Clip,
                                    modifier =
                                        Modifier.graphicsLayer {
                                            translationX = 14.dp.toPx()
                                            translationY = 6.dp.toPx()
                                        },
                                )
                            }
                        }
                        Column(
                            verticalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f, fill = false),
                                ) {
                                    Box(
                                        modifier =
                                            Modifier.padding(end = 8.dp)
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(animatedAccentBg)
                                                .border(
                                                    1.dp,
                                                    currentAccentBorderColor,
                                                    CircleShape,
                                                )
                                    )
                                    Text(
                                        text =
                                            nicknameText.trim().ifEmpty {
                                                nameText.ifEmpty {
                                                    stringResource(R.string.ah_card_preview)
                                                }
                                            },
                                        color = currentTextColor,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                if (selectedCardProtocol != "None") {
                                    CardProtocolLogo(protocol = selectedCardProtocol)
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom,
                            ) {
                                Column {
                                    Text(
                                        text = accountTypeDisplayLabel(typeState),
                                        color = currentSubtextColor,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 1.sp,
                                    )
                                    if (
                                        cardLast4Text.length == 4 && typeState != AccountType.CASH
                                    ) {
                                        Text(
                                            text = "•••• $cardLast4Text",
                                            color = currentLast4Color,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                        )
                                    } else if (
                                        nicknameText.isNotBlank() &&
                                            nicknameText.trim() != nameText.trim()
                                    ) {
                                        Text(
                                            text = nameText,
                                            color = currentLast4Color,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                                val formattedBalance =
                                    String.format(
                                        Locale.US,
                                        "$%,.2f",
                                        balanceText.toDoubleOrNull() ?: 0.0,
                                    )
                                RollingNumberText(
                                    text = formattedBalance,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = currentTextColor,
                                )
                            }
                        }
                    }
                }
            }
        }

            val basicContent: @Composable () -> Unit = {
                AssetEditorSection {

                    // Bento 1: Account Identity

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier =
                                    Modifier.size(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer.copy(
                                                alpha = 0.7f
                                            )
                                        ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                            Text(
                                text = stringResource(R.string.ah_account_identity),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.2.sp,
                            )
                        }

                        OutlinedTextField(
                            value = nameText,
                            onValueChange = { nameText = it },
                            label = { Text(stringResource(R.string.ah_account_card_name)) },
                            leadingIcon = {
                                Icon(
                                    imageVector =
                                        when (typeState) {
                                            AccountType.BANK -> Icons.Default.AccountBalance
                                            AccountType.CASH -> Icons.Default.Savings
                                            AccountType.DEBIT -> Icons.Default.CreditCard
                                            AccountType.CC -> Icons.Default.CreditCard
                                            AccountType.INVESTMENT -> Icons.AutoMirrored.Filled.TrendingUp
                                        },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            },
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().assetEditorBringIntoView(),
                        )

                        OutlinedTextField(
                            value = nicknameText,
                            onValueChange = { nicknameText = it },
                            label = {
                                Text(
                                    stringResource(
                                        com.example.vibefinance.R.string.assets_identity_nickname
                                    )
                                )
                            },
                            supportingText = {
                                Text(
                                    stringResource(
                                        com.example.vibefinance.R.string
                                            .assets_identity_nickname_hint
                                    )
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().assetEditorBringIntoView(),
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = stringResource(R.string.ah_account_type),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                FilterChip(
                                    selected = typeState == AccountType.BANK,
                                    onClick = {
                                        haptic.performHapticFeedback(
                                            HapticFeedbackType.TextHandleMove
                                        )
                                        typeState = AccountType.BANK
                                    },
                                    label = {
                                        Text(
                                            accountTypeDisplayLabel(AccountType.BANK),
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.AccountBalance,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    },
                                    modifier = Modifier.widthIn(min = 100.dp),
                                )
                                FilterChip(
                                    selected = typeState == AccountType.CASH,
                                    onClick = {
                                        haptic.performHapticFeedback(
                                            HapticFeedbackType.TextHandleMove
                                        )
                                        typeState = AccountType.CASH
                                    },
                                    label = {
                                        Text(
                                            accountTypeDisplayLabel(AccountType.CASH),
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Savings,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    },
                                    modifier = Modifier.widthIn(min = 100.dp),
                                )
                                FilterChip(
                                    selected = typeState == AccountType.DEBIT,
                                    onClick = {
                                        haptic.performHapticFeedback(
                                            HapticFeedbackType.TextHandleMove
                                        )
                                        typeState = AccountType.DEBIT
                                    },
                                    label = {
                                        Text(
                                            stringResource(
                                                com.example.vibefinance.R.string.assets_type_debit
                                            ),
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.CreditCard,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    },
                                    modifier = Modifier.widthIn(min = 100.dp),
                                )
                                FilterChip(
                                    selected = typeState == AccountType.CC,
                                    onClick = {
                                        haptic.performHapticFeedback(
                                            HapticFeedbackType.TextHandleMove
                                        )
                                        typeState = AccountType.CC
                                    },
                                    label = {
                                        Text(
                                            accountTypeDisplayLabel(AccountType.CC),
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.CreditCard,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    },
                                    modifier = Modifier.widthIn(min = 100.dp),
                                )
                                FilterChip(
                                    selected = typeState == AccountType.INVESTMENT,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        typeState = AccountType.INVESTMENT
                                    },
                                    label = {
                                        Text(accountTypeDisplayLabel(AccountType.INVESTMENT),
                                            maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
                                    },
                                    leadingIcon = {
                                        Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null,
                                            modifier = Modifier.size(16.dp))
                                    },
                                    modifier = Modifier.widthIn(min = 100.dp),
                                )
                            }
                            if (typeState == AccountType.INVESTMENT) {
                                Text(
                                    stringResource(R.string.assets_investment_notice),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        OutlinedTextField(
                            value = cardLast4Text,
                            onValueChange = {
                                if (it.length <= 4 && it.all { char -> char in '0'..'9' })
                                    cardLast4Text = it
                            },
                            label = {
                                Text(
                                    stringResource(
                                        com.example.vibefinance.R.string.assets_identity_last_four
                                    )
                                )
                            },
                            placeholder = { Text(stringResource(R.string.ah_last_four_example)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = invalidLastFour,
                            supportingText =
                                if (invalidLastFour) {
                                    {
                                        Text(
                                            stringResource(
                                                com.example.vibefinance.R.string
                                                    .assets_identity_last_four_error
                                            )
                                        )
                                    }
                                } else null,
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().assetEditorBringIntoView(),
                        )

                        Text(
                            text =
                                stringResource(
                                    com.example.vibefinance.R.string.assets_identity_accent
                                ),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            listOf(
                                    null to
                                        com.example.vibefinance.R.string
                                            .assets_identity_accent_default,
                                    "primary" to
                                        com.example.vibefinance.R.string
                                            .assets_identity_accent_primary,
                                    "secondary" to
                                        com.example.vibefinance.R.string
                                            .assets_identity_accent_secondary,
                                    "tertiary" to
                                        com.example.vibefinance.R.string
                                            .assets_identity_accent_tertiary,
                                )
                                .forEach { (key, labelRes) ->
                                    val accent = accountAccent(typeState, key)
                                    FilterChip(
                                        selected = selectedAccentColorKey == key,
                                        onClick = {
                                            haptic.performHapticFeedback(
                                                HapticFeedbackType.TextHandleMove
                                            )
                                            selectedAccentColorKey = key
                                        },
                                        label = {
                                            Text(
                                                stringResource(labelRes),
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        },
                                        leadingIcon = {
                                            Box(
                                                Modifier.size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(accent.background)
                                                    .border(
                                                        1.dp,
                                                        accent.foreground.copy(alpha = 0.45f),
                                                        CircleShape,
                                                    )
                                            )
                                        },
                                    )
                                }
                        }
                    }

                    // Bento 2: Balance & Quick Adjust

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier =
                                    Modifier.size(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer.copy(
                                                alpha = 0.7f
                                            )
                                        ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Savings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                            Text(
                                text =
                                    if (typeState == AccountType.CC)
                                        stringResource(R.string.ah_current_balance_debt)
                                    else stringResource(R.string.ah_current_balance),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.2.sp,
                            )
                        }
                        OutlinedTextField(
                            visualTransformation =
                                if (
                                    com.example.vibefinance.ui.preferences.LocalExperience.current
                                        .hideAmounts
                                )
                                    androidx.compose.ui.text.input.PasswordVisualTransformation()
                                else androidx.compose.ui.text.input.VisualTransformation.None,
                            value = balanceText,
                            onValueChange = { balanceText = it },
                            supportingText = if (typeState == AccountType.CC) {
                                { Text(stringResource(R.string.ah_credit_balance_help)) }
                            } else null,
                            label = {
                                Text(
                                    if (typeState == AccountType.CC)
                                        stringResource(R.string.ah_current_debt_amount)
                                    else stringResource(R.string.ah_current_balance_amount)
                                )
                            },
                            prefix = { Text("$ ", fontWeight = FontWeight.Bold) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().assetEditorBringIntoView(),
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = stringResource(R.string.ah_quick_adjust),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            val quickAdjustScrollState = rememberScrollState()
                            Row(
                                modifier =
                                    Modifier.fillMaxWidth()
                                        .horizontalFadingEdge(quickAdjustScrollState, 16.dp)
                                        .horizontalScroll(quickAdjustScrollState),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                listOf(50.0, 100.0, 500.0, 1000.0).forEach { delta ->
                                    SuggestionChip(
                                        onClick = {
                                            haptic.performHapticFeedback(
                                                HapticFeedbackType.TextHandleMove
                                            )
                                            val curr = balanceText.toDoubleOrNull() ?: 0.0
                                            balanceText =
                                                String.format(Locale.US, "%.2f", curr + delta)
                                        },
                                        label = {
                                            Text(
                                                "+$${delta.toInt()}",
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        },
                                        colors =
                                            SuggestionChipDefaults.suggestionChipColors(
                                                containerColor =
                                                    MaterialTheme.colorScheme.primaryContainer.copy(
                                                        alpha = 0.65f
                                                    ),
                                                labelColor =
                                                    MaterialTheme.colorScheme.onPrimaryContainer,
                                            ),
                                        shape = RoundedCornerShape(12.dp),
                                    )
                                }
                                listOf(50.0, 100.0).forEach { delta ->
                                    SuggestionChip(
                                        onClick = {
                                            haptic.performHapticFeedback(
                                                HapticFeedbackType.TextHandleMove
                                            )
                                            val curr = balanceText.toDoubleOrNull() ?: 0.0
                                            balanceText =
                                                String.format(
                                                    Locale.US,
                                                    "%.2f",
                                                    (curr - delta).coerceAtLeast(0.0),
                                                )
                                        },
                                        label = {
                                            Text(
                                                "-$${delta.toInt()}",
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        },
                                        colors =
                                            SuggestionChipDefaults.suggestionChipColors(
                                                containerColor =
                                                    MaterialTheme.colorScheme.errorContainer.copy(
                                                        alpha = 0.5f
                                                    ),
                                                labelColor =
                                                    MaterialTheme.colorScheme.onErrorContainer,
                                            ),
                                        shape = RoundedCornerShape(12.dp),
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                visualTransformation =
                                    if (
                                        com.example.vibefinance.ui.preferences.LocalExperience
                                            .current
                                            .hideAmounts
                                    )
                                        androidx.compose.ui.text.input
                                            .PasswordVisualTransformation()
                                    else androidx.compose.ui.text.input.VisualTransformation.None,
                                value = quickAdjustAmountText,
                                onValueChange = { quickAdjustAmountText = it },
                                label = { Text(stringResource(R.string.ah_custom_adjust)) },
                                placeholder = { Text(stringResource(R.string.ah_adjust_example)) },
                                keyboardOptions =
                                    KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors =
                                    OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f).assetEditorBringIntoView(),
                            )
                            CompletePressFilledTonalButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val currentVal = balanceText.toDoubleOrNull() ?: 0.0
                                    val delta = quickAdjustAmountText.toDoubleOrNull() ?: 0.0
                                    balanceText =
                                        String.format(Locale.US, "%.2f", currentVal + delta)
                                },
                                shapes = com.example.vibefinance.ui.components.AppButtonDefaults.largeShapes(),
                                modifier = Modifier.height(56.dp),
                            ) {
                                Text(
                                    "+",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            CompletePressFilledTonalButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val currentVal = balanceText.toDoubleOrNull() ?: 0.0
                                    val delta = quickAdjustAmountText.toDoubleOrNull() ?: 0.0
                                    balanceText =
                                        String.format(
                                            Locale.US,
                                            "%.2f",
                                            (currentVal - delta).coerceAtLeast(0.0),
                                        )
                                },
                                colors =
                                    ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                    ),
                                shapes = com.example.vibefinance.ui.components.AppButtonDefaults.largeShapes(),
                                modifier = Modifier.height(56.dp),
                            ) {
                                Text(
                                    "-",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }

                    // Bento 3: CC Limits (if CC)
                    if (typeState == AccountType.CC) {

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Box(
                                    modifier =
                                        Modifier.size(28.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                MaterialTheme.colorScheme.primaryContainer.copy(
                                                    alpha = 0.7f
                                                )
                                            ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CreditCard,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.ah_limit_utilization),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.2.sp,
                                )
                            }
                            OutlinedTextField(
                                visualTransformation =
                                    if (
                                        com.example.vibefinance.ui.preferences.LocalExperience
                                            .current
                                            .hideAmounts
                                    )
                                        androidx.compose.ui.text.input
                                            .PasswordVisualTransformation()
                                    else androidx.compose.ui.text.input.VisualTransformation.None,
                                value = creditLimitText,
                                onValueChange = { creditLimitText = it },
                                label = { Text(stringResource(R.string.ah_credit_limit_amount)) },
                                keyboardOptions =
                                    KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors =
                                    OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().assetEditorBringIntoView(),
                            )

                            val limitVal = creditLimitText.toDoubleOrNull() ?: 1.0
                            val debtVal = balanceText.toDoubleOrNull() ?: 0.0
                            val util =
                                if (limitVal > 0) (debtVal / limitVal).coerceIn(0.0, 1.0).toFloat()
                                else 0f
                            val utilPercent = (util * 100).toInt()

                            val animatedUtil by
                                animateFloatAsState(
                                    targetValue = util,
                                    animationSpec =
                                        com.example.vibefinance.theme.VibeMotion.slowSpatial(),
                                    label = "utilProgressSpring",
                                )
                            val targetColor =
                                when {
                                    utilPercent < 30 -> MaterialTheme.colorScheme.primary
                                    utilPercent < 60 -> Color(0xFFFFA000)
                                    else -> MaterialTheme.colorScheme.error
                                }
                            val animatedBarColor by
                                animateColorAsState(
                                    targetValue = targetColor,
                                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                                    label = "utilColorSpring",
                                )

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = stringResource(R.string.ah_credit_utilization),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = "$utilPercent%",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = animatedBarColor,
                                    )
                                }
                                val minimalMotion =
                                    com.example.vibefinance.ui.preferences.LocalExperience.current.motion ==
                                        com.example.vibefinance.ui.preferences.MotionLevel.MINIMAL
                                LinearWavyProgressIndicator(
                                    progress = { animatedUtil },
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 10.dp),
                                    color = animatedBarColor,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    amplitude = {
                                        if (minimalMotion) 0f
                                        else WavyProgressIndicatorDefaults.indicatorAmplitude(it)
                                    },
                                )
                            }
                        }
                    }
                }
            }
            val billingContent: @Composable () -> Unit = {
                if (typeState == AccountType.CC) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border =
                            BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                            ),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            var showDayWheelPicker by rememberSaveable { mutableStateOf(false) }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Box(
                                    modifier =
                                        Modifier.size(28.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                MaterialTheme.colorScheme.primaryContainer.copy(
                                                    alpha = 0.7f
                                                )
                                            ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.ah_billing_schedule),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.2.sp,
                                    modifier = Modifier.weight(1f),
                                )
                                FilterChip(
                                    selected = showDayWheelPicker,
                                    onClick = {
                                        haptic.performHapticFeedback(
                                            HapticFeedbackType.TextHandleMove
                                        )
                                        showDayWheelPicker = !showDayWheelPicker
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    },
                                    label = { Text("1–31", maxLines = 1, softWrap = false) },
                                )
                            }

                            AnimatedVisibility(visible = showDayWheelPicker) {
                                val wheelColors = ScrollFieldDefaults.colors()
                                val statementLabel =
                                    stringResource(
                                        com.example.vibefinance.R.string.acc_statement_date
                                    )
                                val paymentLabel = stringResource(R.string.ah_payment_due)
                                val billingWheelState =
                                    rememberScrollFieldState(
                                        itemCount = 31,
                                        index =
                                            ((billingDateText.toIntOrNull() ?: 1) - 1)
                                                .coerceIn(0, 30),
                                    )
                                val paymentWheelState =
                                    rememberScrollFieldState(
                                        itemCount = 31,
                                        index =
                                            ((paymentDateText.toIntOrNull() ?: 1) - 1)
                                                .coerceIn(0, 30),
                                    )

                                LaunchedEffect(billingWheelState) {
                                    snapshotFlow { billingWheelState.selectedOption }
                                        .drop(1)
                                        .collect { opt ->
                                            val dayStr = (opt + 1).toString()
                                            if (billingDateText != dayStr) {
                                                billingDateText = dayStr
                                                haptic.performHapticFeedback(
                                                    HapticFeedbackType.TextHandleMove
                                                )
                                            }
                                        }
                                }
                                LaunchedEffect(billingDateText) {
                                    val target =
                                        billingDateText.toIntOrNull()?.let {
                                            (it - 1).coerceIn(0, 30)
                                        }
                                    if (
                                        target != null &&
                                            billingWheelState.selectedOption != target &&
                                            !billingWheelState.isScrollInProgress
                                    ) {
                                        billingWheelState.scrollToOption(target)
                                    }
                                }
                                LaunchedEffect(paymentWheelState) {
                                    snapshotFlow { paymentWheelState.selectedOption }
                                        .drop(1)
                                        .collect { opt ->
                                            val dayStr = (opt + 1).toString()
                                            if (paymentDateText != dayStr) {
                                                paymentDateText = dayStr
                                                haptic.performHapticFeedback(
                                                    HapticFeedbackType.TextHandleMove
                                                )
                                            }
                                        }
                                }
                                LaunchedEffect(paymentDateText) {
                                    val target =
                                        paymentDateText.toIntOrNull()?.let {
                                            (it - 1).coerceIn(0, 30)
                                        }
                                    if (
                                        target != null &&
                                            paymentWheelState.selectedOption != target &&
                                            !paymentWheelState.isScrollInProgress
                                    ) {
                                        paymentWheelState.scrollToOption(target)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Row(
                                        modifier =
                                            Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Text(
                                                text = statementLabel,
                                                style = MaterialTheme.typography.labelSmall,
                                                color =
                                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                softWrap = false,
                                            )
                                            ScrollField(
                                                state = billingWheelState,
                                                contentDescription = statementLabel,
                                                colors = wheelColors,
                                                fieldAccessibilityDescription = {
                                                    (it + 1).toString()
                                                },
                                                field = { index, selected, enabled ->
                                                    ScrollFieldDefaults.Item(
                                                        index + 1,
                                                        selected,
                                                        enabled,
                                                        wheelColors,
                                                    )
                                                },
                                            )
                                        }
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Text(
                                                text = paymentLabel,
                                                style = MaterialTheme.typography.labelSmall,
                                                color =
                                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                softWrap = false,
                                            )
                                            ScrollField(
                                                state = paymentWheelState,
                                                contentDescription = paymentLabel,
                                                colors = wheelColors,
                                                fieldAccessibilityDescription = {
                                                    (it + 1).toString()
                                                },
                                                field = { index, selected, enabled ->
                                                    ScrollFieldDefaults.Item(
                                                        index + 1,
                                                        selected,
                                                        enabled,
                                                        wheelColors,
                                                    )
                                                },
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                ExposedDropdownMenuBox(
                                    expanded = billingDayDropdownExpanded,
                                    onExpandedChange = {
                                        billingDayDropdownExpanded = !billingDayDropdownExpanded
                                    },
                                    modifier = Modifier.weight(1f).assetEditorBringIntoView(),
                                ) {
                                    OutlinedTextField(
                                        value = billingDateText,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = {
                                            Text(
                                                stringResource(
                                                    com.example.vibefinance.R.string
                                                        .acc_statement_date
                                                )
                                            )
                                        },
                                        placeholder = {
                                            Text(
                                                stringResource(
                                                    com.example.vibefinance.R.string
                                                        .acc_statement_day_select
                                                )
                                            )
                                        },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(
                                                expanded = billingDayDropdownExpanded
                                            )
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                    )
                                    ExposedDropdownMenu(
                                        expanded = billingDayDropdownExpanded,
                                        onDismissRequest = { billingDayDropdownExpanded = false },
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    stringResource(
                                                        com.example.vibefinance.R.string
                                                            .acc_statement_day_clear
                                                    )
                                                )
                                            },
                                            onClick = {
                                                billingDateText = ""
                                                billingDayDropdownExpanded = false
                                            },
                                        )
                                        (1..31).forEach { day ->
                                            DropdownMenuItem(
                                                text = { Text(day.toString()) },
                                                onClick = {
                                                    billingDateText = day.toString()
                                                    billingDayDropdownExpanded = false
                                                },
                                            )
                                        }
                                    }
                                }

                                ExposedDropdownMenuBox(
                                    expanded = paymentDayDropdownExpanded,
                                    onExpandedChange = {
                                        paymentDayDropdownExpanded = !paymentDayDropdownExpanded
                                    },
                                    modifier = Modifier.weight(1f).assetEditorBringIntoView(),
                                ) {
                                    OutlinedTextField(
                                        value = paymentDateText,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text(stringResource(R.string.ah_payment_due)) },
                                        placeholder = {
                                            Text(
                                                stringResource(
                                                    com.example.vibefinance.R.string
                                                        .acc_statement_day_select
                                                )
                                            )
                                        },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(
                                                expanded = paymentDayDropdownExpanded
                                            )
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                    )
                                    ExposedDropdownMenu(
                                        expanded = paymentDayDropdownExpanded,
                                        onDismissRequest = { paymentDayDropdownExpanded = false },
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    stringResource(
                                                        com.example.vibefinance.R.string
                                                            .acc_statement_day_clear
                                                    )
                                                )
                                            },
                                            onClick = {
                                                paymentDateText = ""
                                                paymentDayDropdownExpanded = false
                                            },
                                        )
                                        (1..31).forEach { day ->
                                            DropdownMenuItem(
                                                text = { Text(day.toString()) },
                                                onClick = {
                                                    paymentDateText = day.toString()
                                                    paymentDayDropdownExpanded = false
                                                },
                                            )
                                        }
                                    }
                                }
                            }

                            ExposedDropdownMenuBox(
                                expanded = paymentDeadlineDayDropdownExpanded,
                                onExpandedChange = {
                                    paymentDeadlineDayDropdownExpanded =
                                        !paymentDeadlineDayDropdownExpanded
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                OutlinedTextField(
                                    value = paymentDeadlineText,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = {
                                        Text(stringResource(R.string.ah_payment_deadline_day))
                                    },
                                    placeholder = {
                                        Text(
                                            stringResource(
                                                com.example.vibefinance.R.string
                                                    .acc_statement_day_select
                                            )
                                        )
                                    },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = paymentDeadlineDayDropdownExpanded
                                        )
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                )
                                ExposedDropdownMenu(
                                    expanded = paymentDeadlineDayDropdownExpanded,
                                    onDismissRequest = {
                                        paymentDeadlineDayDropdownExpanded = false
                                    },
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                stringResource(
                                                    com.example.vibefinance.R.string
                                                        .acc_statement_day_clear
                                                )
                                            )
                                        },
                                        onClick = {
                                            paymentDeadlineText = ""
                                            paymentDeadlineDayDropdownExpanded = false
                                        },
                                    )
                                    (1..31).forEach { day ->
                                        DropdownMenuItem(
                                            text = { Text(day.toString()) },
                                            onClick = {
                                                paymentDeadlineText = day.toString()
                                                paymentDeadlineDayDropdownExpanded = false
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            val designContent: @Composable () -> Unit = {
                AssetEditorSection {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier =
                                    Modifier.size(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer.copy(
                                                alpha = 0.7f
                                            )
                                        ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                            Text(
                                text = stringResource(R.string.ah_gradient_palette),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.2.sp,
                            )
                        }

                        val themes =
                            listOf(
                                "default" to Pair(listCardColor, listCardColor),
                                "obsidian" to Pair(Color(0xFF161B22), Color(0xFF242C37)),
                                "ocean" to Pair(Color(0xFF0D47A1), Color(0xFF1976D2)),
                                "emerald" to Pair(Color(0xFF1B5E20), Color(0xFF388E3C)),
                                "sunset" to Pair(Color(0xFFE65100), Color(0xFFF57C00)),
                                "purple" to Pair(Color(0xFF4A148C), Color(0xFF7B1FA2)),
                                "rose" to Pair(Color(0xFF880E4F), Color(0xFFC2185B)),
                                "crimson" to Pair(Color(0xFFB71C1C), Color(0xFFD32F2F)),
                            )

                        FlowRow(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            themes.forEach { (themeKey, colors) ->
                                val isSelected =
                                    selectedCardTheme.equals(themeKey, ignoreCase = true) ||
                                        (themeKey == "obsidian" &&
                                            selectedCardTheme.equals("dark stealth", ignoreCase = true))
                                val themeLabel = accountCardThemeLabel(themeKey)
                                val animatedScale by
                                    animateFloatAsState(
                                        targetValue = if (isSelected) 1.12f else 1.0f,
                                        animationSpec =
                                            com.example.vibefinance.theme.VibeMotion.defaultSpatial(),
                                        label = "swatchScale_$themeKey",
                                    )
                                val animatedBorderWidth by
                                    animateDpAsState(
                                        targetValue = if (isSelected) 3.dp else 1.dp,
                                        animationSpec =
                                            spring(stiffness = Spring.StiffnessMediumLow),
                                        label = "swatchBorder_$themeKey",
                                    )
                                val unselectedBorder =
                                    if (themeKey == "default") MaterialTheme.colorScheme.outline.copy(alpha = 0.50f)
                                    else Color.White.copy(alpha = 0.40f)
                                val checkTint =
                                    if (themeKey == "default") MaterialTheme.colorScheme.onSurface
                                    else Color.White
                                Box(
                                    modifier =
                                        Modifier.padding(2.dp)
                                            .size(42.dp)
                                            .graphicsLayer {
                                                scaleX = animatedScale
                                                scaleY = animatedScale
                                            }
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(colors.first, colors.second)
                                                )
                                            )
                                            .border(
                                                width = animatedBorderWidth,
                                                color =
                                                    if (isSelected)
                                                        MaterialTheme.colorScheme.primary
                                                    else unselectedBorder,
                                                shape = CircleShape,
                                            )
                                            .testTag("CardThemeSwatch_$themeKey")
                                            .clickable {
                                                haptic.performHapticFeedback(
                                                    HapticFeedbackType.TextHandleMove
                                                )
                                                selectedCardTheme = themeKey
                                            }
                                            .semantics {
                                                contentDescription = themeLabel
                                                selected = isSelected
                                            },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription =
                                                stringResource(R.string.ah_selected),
                                            tint = checkTint,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier =
                                    Modifier.size(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer.copy(
                                                alpha = 0.7f
                                            )
                                        ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                            Text(
                                text = stringResource(R.string.ah_protocol),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.2.sp,
                            )
                        }

                        ExposedDropdownMenuBox(
                            expanded = protocolDropdownExpanded,
                            onExpandedChange = {
                                protocolDropdownExpanded = !protocolDropdownExpanded
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            OutlinedTextField(
                                value = cardDesignOptionLabel(selectedCardProtocol),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.ah_protocol)) },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = protocolDropdownExpanded
                                    )
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            )
                            ExposedDropdownMenu(
                                expanded = protocolDropdownExpanded,
                                onDismissRequest = { protocolDropdownExpanded = false },
                            ) {
                                listOf("None", "Visa", "Mastercard", "UnionPay", "JCB").forEach { p
                                    ->
                                    DropdownMenuItem(
                                        text = { Text(cardDesignOptionLabel(p)) },
                                        onClick = {
                                            selectedCardProtocol = p
                                            protocolDropdownExpanded = false
                                        },
                                    )
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier =
                                    Modifier.size(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer.copy(
                                                alpha = 0.7f
                                            )
                                        ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                            Text(
                                text = stringResource(R.string.ah_texture_art),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.2.sp,
                            )
                        }

                        ExposedDropdownMenuBox(
                            expanded = patternDropdownExpanded,
                            onExpandedChange = {
                                patternDropdownExpanded = !patternDropdownExpanded
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            OutlinedTextField(
                                value = cardDesignOptionLabel(selectedCardPattern),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.ah_pattern_overlay)) },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = patternDropdownExpanded
                                    )
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            )
                            ExposedDropdownMenu(
                                expanded = patternDropdownExpanded,
                                onDismissRequest = { patternDropdownExpanded = false },
                            ) {
                                listOf("None", "Cyber Grid", "Neon Waves", "Geometric Mesh")
                                    .forEach { pat ->
                                        DropdownMenuItem(
                                            text = { Text(cardDesignOptionLabel(pat)) },
                                            onClick = {
                                                selectedCardPattern = pat
                                                patternDropdownExpanded = false
                                            },
                                        )
                                    }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CompletePressOutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    galleryLauncher.launch("image/*")
                                },
                                shapes = com.example.vibefinance.ui.components.AppButtonDefaults.shapes(),
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp).then(com.example.vibefinance.ui.components.LocalPostPressIconBounce.current),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (selectedCardImageUri.isEmpty())
                                        stringResource(R.string.ah_upload_image)
                                    else stringResource(R.string.ah_change_image)
                                )
                            }
                            if (selectedCardImageUri.isNotEmpty()) {
                                CompletePressOutlinedButton(
                                    onClick = {
                                        haptic.performHapticFeedback(
                                            HapticFeedbackType.TextHandleMove
                                        )
                                        try {
                                            val file = File(selectedCardImageUri)
                                            if (file.exists()) {
                                                val bmap =
                                                    BitmapFactory.decodeFile(file.absolutePath)
                                                if (bmap != null) {
                                                    cropSourcePath = file.absolutePath
                                                    showCropDialog = true
                                                }
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    },
                                    shapes = com.example.vibefinance.ui.components.AppButtonDefaults.shapes(),
                                ) {
                                    Icon(
                                        Icons.Default.Crop,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp).then(com.example.vibefinance.ui.components.LocalPostPressIconBounce.current),
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.ah_crop))
                                }

                                CompletePressTextButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedCardImageUri = ""
                                        selectedCardBgOffsetX = 0f
                                        selectedCardBgOffsetY = 0f
                                        selectedCardBgScale = 1f
                                    },
                                    colors =
                                        ButtonDefaults.textButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        ),
                                    shapes = com.example.vibefinance.ui.components.AppButtonDefaults.shapes(),
                                ) {
                                    Text(stringResource(R.string.ah_remove))
                                }
                            }
                        }

                        if (selectedCardImageUri.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color =
                                    MaterialTheme.colorScheme.surface.copy(
                                        alpha = if (isDark) 0.40f else 0.65f
                                    ),
                                border =
                                    BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant.copy(
                                            alpha = 0.35f
                                        ),
                                    ),
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    // Header Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Box(
                                                modifier =
                                                    Modifier.size(26.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(
                                                            MaterialTheme.colorScheme
                                                                .primaryContainer
                                                                .copy(alpha = 0.7f)
                                                        ),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Tune,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(15.dp),
                                                )
                                            }
                                            Text(
                                                text = stringResource(R.string.ah_position_axes),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                letterSpacing = 1.1.sp,
                                            )
                                        }
                                        if (
                                            selectedCardBgOffsetX != 0f ||
                                                selectedCardBgOffsetY != 0f ||
                                                selectedCardBgScale != 1f
                                        ) {
                                            CompletePressTextButton(
                                                onClick = {
                                                    haptic.performHapticFeedback(
                                                        HapticFeedbackType.LongPress
                                                    )
                                                    selectedCardBgOffsetX = 0f
                                                    selectedCardBgOffsetY = 0f
                                                    selectedCardBgScale = 1f
                                                },
                                                shapes = com.example.vibefinance.ui.components.AppButtonDefaults.shapes(),
                                                contentPadding =
                                                    PaddingValues(
                                                        horizontal = 6.dp,
                                                        vertical = 2.dp,
                                                    ),
                                            ) {
                                                Icon(
                                                    Icons.Default.Refresh,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(13.dp).then(com.example.vibefinance.ui.components.LocalPostPressIconBounce.current),
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    stringResource(R.string.ah_reset),
                                                    style = MaterialTheme.typography.labelSmall,
                                                )
                                            }
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color =
                                            MaterialTheme.colorScheme.primaryContainer.copy(
                                                alpha = 0.45f
                                            ),
                                    ) {
                                        Row(
                                            modifier =
                                                Modifier.fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Tune,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp),
                                            )
                                            Text(
                                                text = stringResource(R.string.ah_gesture_tip),
                                                style =
                                                    MaterialTheme.typography.bodySmall.copy(
                                                        fontSize = 11.sp
                                                    ),
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )
                                        }
                                    }

                                    // X Axis Controller
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text = stringResource(R.string.ah_horizontal_axis),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                            val xPercent =
                                                (selectedCardBgOffsetX * 100).roundToInt()
                                            val xDesc =
                                                when {
                                                    xPercent < 0 ->
                                                        stringResource(
                                                            R.string.ah_offset_left,
                                                            -xPercent,
                                                        )
                                                    xPercent > 0 ->
                                                        stringResource(
                                                            R.string.ah_offset_right,
                                                            xPercent,
                                                        )
                                                    else -> stringResource(R.string.ah_center_zero)
                                                }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color =
                                                    MaterialTheme.colorScheme.primaryContainer.copy(
                                                        alpha = 0.6f
                                                    ),
                                            ) {
                                                Text(
                                                    text = xDesc,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color =
                                                        MaterialTheme.colorScheme
                                                            .onPrimaryContainer,
                                                    modifier =
                                                        Modifier.padding(
                                                            horizontal = 6.dp,
                                                            vertical = 2.dp,
                                                        ),
                                                )
                                            }
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    haptic.performHapticFeedback(
                                                        HapticFeedbackType.TextHandleMove
                                                    )
                                                    selectedCardBgOffsetX =
                                                        ((selectedCardBgOffsetX - 0.05f).coerceIn(
                                                                -1.0f,
                                                                1.0f,
                                                            ) * 100)
                                                            .roundToInt() / 100f
                                                },
                                                modifier = Modifier.size(28.dp),
                                            ) {
                                                Icon(
                                                    Icons.Default.ChevronLeft,
                                                    contentDescription =
                                                        stringResource(R.string.ah_shift_left),
                                                    modifier = Modifier.size(18.dp),
                                                )
                                            }
                                            AppSlider(
                                                value = selectedCardBgOffsetX,
                                                onValueChange = {
                                                    selectedCardBgOffsetX =
                                                        (it * 100).roundToInt() / 100f
                                                },
                                                valueRange = -1.0f..1.0f,
                                                modifier = Modifier.weight(1f),
                                            )
                                            IconButton(
                                                onClick = {
                                                    haptic.performHapticFeedback(
                                                        HapticFeedbackType.TextHandleMove
                                                    )
                                                    selectedCardBgOffsetX =
                                                        ((selectedCardBgOffsetX + 0.05f).coerceIn(
                                                                -1.0f,
                                                                1.0f,
                                                            ) * 100)
                                                            .roundToInt() / 100f
                                                },
                                                modifier = Modifier.size(28.dp),
                                            ) {
                                                Icon(
                                                    Icons.Default.ChevronRight,
                                                    contentDescription =
                                                        stringResource(R.string.ah_shift_right),
                                                    modifier = Modifier.size(18.dp),
                                                )
                                            }
                                        }
                                    }

                                    // Y Axis Controller
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text = stringResource(R.string.ah_vertical_axis),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                            val yPercent =
                                                (selectedCardBgOffsetY * 100).roundToInt()
                                            val yDesc =
                                                when {
                                                    yPercent < 0 ->
                                                        stringResource(
                                                            R.string.ah_offset_up,
                                                            -yPercent,
                                                        )
                                                    yPercent > 0 ->
                                                        stringResource(
                                                            R.string.ah_offset_down,
                                                            yPercent,
                                                        )
                                                    else -> stringResource(R.string.ah_center_zero)
                                                }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color =
                                                    MaterialTheme.colorScheme.primaryContainer.copy(
                                                        alpha = 0.6f
                                                    ),
                                            ) {
                                                Text(
                                                    text = yDesc,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color =
                                                        MaterialTheme.colorScheme
                                                            .onPrimaryContainer,
                                                    modifier =
                                                        Modifier.padding(
                                                            horizontal = 6.dp,
                                                            vertical = 2.dp,
                                                        ),
                                                )
                                            }
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    haptic.performHapticFeedback(
                                                        HapticFeedbackType.TextHandleMove
                                                    )
                                                    selectedCardBgOffsetY =
                                                        ((selectedCardBgOffsetY - 0.05f).coerceIn(
                                                                -1.0f,
                                                                1.0f,
                                                            ) * 100)
                                                            .roundToInt() / 100f
                                                },
                                                modifier = Modifier.size(28.dp),
                                            ) {
                                                Icon(
                                                    Icons.Default.KeyboardArrowUp,
                                                    contentDescription =
                                                        stringResource(R.string.ah_shift_up),
                                                    modifier = Modifier.size(18.dp),
                                                )
                                            }
                                            AppSlider(
                                                value = selectedCardBgOffsetY,
                                                onValueChange = {
                                                    selectedCardBgOffsetY =
                                                        (it * 100).roundToInt() / 100f
                                                },
                                                valueRange = -1.0f..1.0f,
                                                modifier = Modifier.weight(1f),
                                            )
                                            IconButton(
                                                onClick = {
                                                    haptic.performHapticFeedback(
                                                        HapticFeedbackType.TextHandleMove
                                                    )
                                                    selectedCardBgOffsetY =
                                                        ((selectedCardBgOffsetY + 0.05f).coerceIn(
                                                                -1.0f,
                                                                1.0f,
                                                            ) * 100)
                                                            .roundToInt() / 100f
                                                },
                                                modifier = Modifier.size(28.dp),
                                            ) {
                                                Icon(
                                                    Icons.Default.KeyboardArrowDown,
                                                    contentDescription =
                                                        stringResource(R.string.ah_shift_down),
                                                    modifier = Modifier.size(18.dp),
                                                )
                                            }
                                        }
                                    }

                                    // Scale / Zoom Controller
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text = stringResource(R.string.ah_zoom_scale),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color =
                                                    MaterialTheme.colorScheme.secondaryContainer
                                                        .copy(alpha = 0.6f),
                                            ) {
                                                Text(
                                                    text =
                                                        String.format(
                                                            Locale.US,
                                                            "%.2fx",
                                                            selectedCardBgScale,
                                                        ),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color =
                                                        MaterialTheme.colorScheme
                                                            .onSecondaryContainer,
                                                    modifier =
                                                        Modifier.padding(
                                                            horizontal = 6.dp,
                                                            vertical = 2.dp,
                                                        ),
                                                )
                                            }
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    haptic.performHapticFeedback(
                                                        HapticFeedbackType.TextHandleMove
                                                    )
                                                    selectedCardBgScale =
                                                        ((selectedCardBgScale - 0.1f).coerceIn(
                                                                0.5f,
                                                                2.5f,
                                                            ) * 20)
                                                            .roundToInt() / 20f
                                                },
                                                modifier = Modifier.size(28.dp),
                                            ) {
                                                Icon(
                                                    Icons.Default.ZoomOut,
                                                    contentDescription =
                                                        stringResource(R.string.ah_zoom_out),
                                                    modifier = Modifier.size(18.dp),
                                                )
                                            }
                                            AppSlider(
                                                value = selectedCardBgScale,
                                                onValueChange = {
                                                    selectedCardBgScale =
                                                        (it * 20).roundToInt() / 20f
                                                },
                                                valueRange = 0.5f..2.5f,
                                                modifier = Modifier.weight(1f),
                                            )
                                            IconButton(
                                                onClick = {
                                                    haptic.performHapticFeedback(
                                                        HapticFeedbackType.TextHandleMove
                                                    )
                                                    selectedCardBgScale =
                                                        ((selectedCardBgScale + 0.1f).coerceIn(
                                                                0.5f,
                                                                2.5f,
                                                            ) * 20)
                                                            .roundToInt() / 20f
                                                },
                                                modifier = Modifier.size(28.dp),
                                            ) {
                                                Icon(
                                                    Icons.Default.ZoomIn,
                                                    contentDescription =
                                                        stringResource(R.string.ah_zoom_in),
                                                    modifier = Modifier.size(18.dp),
                                                )
                                            }
                                        }
                                    }

                                    // Quick Preset Alignment Chips
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        val presets =
                                            listOf(
                                                R.string.ah_align_center to Pair(0f, 0f),
                                                R.string.ah_align_top to Pair(0f, -0.30f),
                                                R.string.ah_align_bottom to Pair(0f, 0.30f),
                                                R.string.ah_align_left to Pair(-0.30f, 0f),
                                                R.string.ah_align_right to Pair(0.30f, 0f),
                                            )
                                        presets.forEach { (name, pos) ->
                                            val isCurrent =
                                                kotlin.math.abs(selectedCardBgOffsetX - pos.first) <
                                                    0.05f &&
                                                    kotlin.math.abs(
                                                        selectedCardBgOffsetY - pos.second
                                                    ) < 0.05f
                                            FilterChip(
                                                selected = isCurrent,
                                                onClick = {
                                                    haptic.performHapticFeedback(
                                                        HapticFeedbackType.TextHandleMove
                                                    )
                                                    selectedCardBgOffsetX = pos.first
                                                    selectedCardBgOffsetY = pos.second
                                                },
                                                label = {
                                                    Text(
                                                        stringResource(name),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        maxLines = 1,
                                                        softWrap = false,
                                                        overflow = TextOverflow.Ellipsis,
                                                    )
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            val automationContent: @Composable () -> Unit = {
                AssetEditorSection {

                    // Bento: Linked App for Quick Redirect

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier =
                                    Modifier.size(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer.copy(
                                                alpha = 0.7f
                                            )
                                        ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                            Column {
                                Text(
                                    text =
                                        stringResource(
                                            com.example.vibefinance.R.string.assets_linked_app_title
                                        ),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.2.sp,
                                )
                                Text(
                                    text =
                                        stringResource(
                                            com.example.vibefinance.R.string.assets_linked_app_desc
                                        ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        val linkedAppShape = RoundedCornerShape(14.dp)
                        Surface(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .bouncyClickable(
                                        shape = linkedAppShape,
                                        onClick = { showAppPickerDialog = true },
                                    ),
                            shape = linkedAppShape,
                            color = MaterialTheme.colorScheme.surface,
                            border =
                                BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                ),
                        ) {
                            Row(
                                modifier =
                                    Modifier.fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                val resolvedAppPkg =
                                    LocalAppManager.resolveTargetAppPackage(
                                        context = context,
                                        linkedAppPackage = selectedLinkedAppPackage,
                                    )
                                val resolvedAppIcon =
                                    remember(resolvedAppPkg) {
                                        resolvedAppPkg?.let {
                                            LocalAppManager.getAppIcon(context, it)
                                        }
                                    }
                                val resolvedAppLabel =
                                    remember(resolvedAppPkg) {
                                        resolvedAppPkg?.let {
                                            LocalAppManager.getAppLabel(context, it)
                                        }
                                    }

                                if (resolvedAppIcon != null) {
                                    Image(
                                        bitmap = resolvedAppIcon.asImageBitmap(),
                                        contentDescription = null,
                                        modifier =
                                            Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)),
                                        contentScale = ContentScale.Fit,
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Block,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp),
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    val statusText =
                                        if (resolvedAppPkg != null && resolvedAppLabel != null) {
                                            resolvedAppLabel
                                        } else {
                                            stringResource(
                                                com.example.vibefinance.R.string
                                                    .assets_linked_app_none
                                            )
                                        }
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint =
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.6f
                                        ),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }

                    // Bento: Notification Auto-Log Identifiers / Aliases

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier =
                                    Modifier.size(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer.copy(
                                                alpha = 0.7f
                                            )
                                        ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                            Column {
                                Text(
                                    text =
                                        stringResource(
                                            com.example.vibefinance.R.string
                                                .assets_notification_aliases_title
                                        ),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text =
                                        stringResource(
                                            com.example.vibefinance.R.string
                                                .assets_notification_aliases_desc
                                        ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        // Active alias chips
                        if (notificationAliasesList.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                notificationAliasesList.forEach { alias ->
                                    InputChip(
                                        selected = true,
                                        onClick = {
                                            notificationAliasesList =
                                                notificationAliasesList.filterNot {
                                                    it.equals(alias, ignoreCase = true)
                                                }
                                        },
                                        label = {
                                            Text(
                                                alias,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                            )
                                        },
                                    )
                                }
                            }
                        } else {
                            Text(
                                text =
                                    stringResource(
                                        com.example.vibefinance.R.string
                                            .assets_notification_aliases_empty
                                    ),
                                style = MaterialTheme.typography.bodySmall,
                                color =
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            )
                        }

                        // Quick suggestion if cardLast4Text is filled and not in list
                        if (
                            cardLast4Text.length == 4 &&
                                notificationAliasesList.none {
                                    it.equals(cardLast4Text, ignoreCase = true)
                                }
                        ) {
                            AssistChip(
                                onClick = {
                                    notificationAliasesList =
                                        notificationAliasesList + cardLast4Text
                                },
                                label = {
                                    Text(
                                        stringResource(
                                            com.example.vibefinance.R.string
                                                .assets_notification_aliases_suggest_last4,
                                            cardLast4Text,
                                        ),
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                },
                            )
                        }

                        // Input to add a new alias
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedTextField(
                                value = newAliasInput,
                                onValueChange = { newAliasInput = it },
                                placeholder = {
                                    Text(
                                        stringResource(
                                            com.example.vibefinance.R.string
                                                .assets_notification_aliases_add_placeholder
                                        )
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier.weight(1f).assetEditorBringIntoView(),
                                shape = RoundedCornerShape(12.dp),
                                colors =
                                    OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    ),
                                keyboardActions =
                                    KeyboardActions(
                                        onDone = {
                                            val trimmed = newAliasInput.trim()
                                            if (
                                                trimmed.isNotEmpty() &&
                                                    notificationAliasesList.none {
                                                        it.equals(trimmed, ignoreCase = true)
                                                    }
                                            ) {
                                                notificationAliasesList =
                                                    notificationAliasesList + trimmed
                                                newAliasInput = ""
                                            }
                                        }
                                    ),
                            )
                            CompletePressFilledTonalButton(
                                onClick = {
                                    val trimmed = newAliasInput.trim()
                                    if (
                                        trimmed.isNotEmpty() &&
                                            notificationAliasesList.none {
                                                it.equals(trimmed, ignoreCase = true)
                                            }
                                    ) {
                                        notificationAliasesList = notificationAliasesList + trimmed
                                        newAliasInput = ""
                                    }
                                },
                                enabled = newAliasInput.trim().isNotEmpty(),
                                shapes = com.example.vibefinance.ui.components.AppButtonDefaults.shapes(),
                            ) {
                                Text(
                                    stringResource(
                                        com.example.vibefinance.R.string
                                            .assets_notification_aliases_add_btn
                                    )
                                )
                            }
                        }
                    }
                }
            }
            val formContent: @Composable () -> Unit = {
                if (!isWide) {
                    previewCard()
                    cyclePreview()
                }
                Column(
                    modifier =
                        Modifier.fillMaxWidth().graphicsLayer {
                            alpha = closingFormAlpha
                        },
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    PageContentEntrance("editor-basic", modifier = Modifier.paneEntrance(3), delayMillis = 40) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            if (currentResult is AccountSaveResult.Failed)
                                Text(
                                    currentResult.message,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.testTag("AssetEditorSaveError"),
                                )
                            Text(
                                stringResource(R.string.asset_editor_basic),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            basicContent()
                        }
                    }
                    if (typeState == AccountType.CC) {
                        PageContentEntrance("editor-billing", modifier = Modifier.paneEntrance(4), delayMillis = 70) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text(
                                    stringResource(R.string.ah_billing_schedule),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                billingContent()
                            }
                        }
                    }
                    // Wide windows edit appearance beside the preview (Appearance Studio pane).
                    if (!isWide) {
                        PageContentEntrance("editor-appearance", modifier = Modifier.paneEntrance(5), delayMillis = 85) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text(
                                    stringResource(R.string.asset_editor_appearance),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                designContent()
                            }
                        }
                    }
                    PageContentEntrance("editor-automation", modifier = Modifier.paneEntrance(6), delayMillis = 100) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                stringResource(R.string.asset_editor_automation),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            automationContent()
                        }
                    }
                    if (editingAccount != null) {
                        PageContentEntrance("editor-delete", modifier = Modifier.paneEntrance(7), delayMillis = 110) {
                            CompletePressOutlinedButton(
                                onClick = { showDeleteConfirmDialog = true },
                                enabled = !saving,
                                shapes = com.example.vibefinance.ui.components.AppButtonDefaults.largeShapes(),
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                colors =
                                    ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                            ) {
                                Icon(Icons.Default.Delete, null, modifier = Modifier.then(com.example.vibefinance.ui.components.LocalPostPressIconBounce.current))
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.btn_delete))
                            }
                        }
                    }
                    Spacer(Modifier.height(48.dp))
                }
            }
            // Appearance Studio: on wide windows the left pane keeps the card preview fixed and puts
            // the appearance controls right under it, so every style change is seen as it is made.
            val studioPaneWidth = (minOf(maxWidth, 1080.dp) * 0.4f).coerceIn(280.dp, 380.dp)
            Row(
                modifier =
                    Modifier.align(Alignment.TopCenter)
                        .widthIn(max = if (isWide) 1080.dp else 840.dp)
                        .fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(if (isWide) 24.dp else 0.dp),
            ) {
                if (isWide)
                    Column(
                        Modifier.width(studioPaneWidth)
                            .fillMaxHeight()
                            .padding(start = 24.dp, top = 12.dp)
                            .testTag("AssetEditorPreviewPane")
                    ) {
                        previewCard()
                        cyclePreview()
                        Column(
                            Modifier.weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(top = 12.dp, bottom = 12.dp)
                                .graphicsLayer { alpha = closingFormAlpha }
                                .testTag("AssetEditorAppearanceStudio"),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            PageContentEntrance("editor-appearance-studio", modifier = Modifier.paneEntrance(4), delayMillis = 70) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Text(
                                        stringResource(R.string.asset_editor_appearance),
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    designContent()
                                }
                            }
                        }
                    }
                ScrollBlurContainer(
                    scrollState = editorScrollState,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    vertical = true,
                    tagPrefix = "AssetEditorScroll",
                    blurActive = editorBlurActive,
                ) { sourceModifier ->
                    Column(
                        modifier =
                            sourceModifier
                                .fillMaxWidth()
                                .verticalScroll(editorScrollState, overscrollEffect = null)
                                .padding(
                                    start = if (isWide) 0.dp else 16.dp,
                                    end = if (isWide) 24.dp else 16.dp,
                                    top = 12.dp,
                                    bottom = 12.dp,
                                )
                                .testTag("AssetEditorScrollContent"),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        formContent()
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

/** React to actual viewport/IME changes rather than using fixed delays for field scrolling. */
private fun Modifier.assetEditorBringIntoView(): Modifier = composed {
    val requester = remember { BringIntoViewRequester() }
    var focused by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val ime = WindowInsets.ime
    LaunchedEffect(focused, density) {
        if (focused)
            snapshotFlow { ime.getBottom(density) }
                .collectLatest {
                    withFrameNanos {}
                    requester.bringIntoView()
                }
    }
    bringIntoViewRequester(requester).onFocusChanged { focused = it.isFocused }
}

@Composable
private fun AssetEditorSection(
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

/** Multi-touch only image transformation detector that leaves single-finger drag unconsumed for vertical scroll. */
private suspend fun PointerInputScope.detectCardImageTransform(
    onTransform: (pan: Offset, zoom: Float) -> Unit
) {
    awaitEachGesture {
        var prevCentroid = Offset.Zero
        var prevSpan = 0f
        var isMultiTouch = false
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Main)
            val pressed = event.changes.filter { it.pressed }
            if (pressed.size >= 2) {
                isMultiTouch = true
                val centroid =
                    pressed.map { it.position }.reduce { a, b -> a + b } / pressed.size.toFloat()
                val span = (pressed[0].position - pressed[1].position).getDistance()
                if (prevSpan > 0f) {
                    val pan = centroid - prevCentroid
                    val zoom = span / prevSpan
                    onTransform(pan, zoom)
                    pressed.forEach { it.consume() }
                }
                prevCentroid = centroid
                prevSpan = span
            } else if (pressed.isEmpty()) {
                break
            } else {
                if (isMultiTouch) break
                prevSpan = 0f
                // Single touch remains unconsumed so parent vertical scroll handles scrolling.
            }
        }
    }
}
