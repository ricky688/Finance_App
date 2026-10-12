@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.accounts

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
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
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AssetEditorScreen(
    state: FinanceUiState,
    editorState: AssetEditorState,
    onIntent: (FinanceIntent) -> Unit,
    saveResult: AccountSaveResult? = null,
    onAccountCreated: ((AccountEntity) -> Unit)? = null,
    onDismissCreate: (() -> Unit)? = null,
) {
    val editingAccountId = editorState.accountId
    val editingAccount = state.accounts.firstOrNull { it.id == editingAccountId }
    val initialPrefillAccount = editorState.prefillAccount
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val presentationLocale = LocalConfiguration.current.locales[0]
    val haptic = LocalHapticFeedback.current
    if (editingAccountId != null && editingAccount == null) {
        if (state.isLoading)
            Surface(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator()
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
                    interactionSource = deleteConfirmInteractionSource,
                    modifier =
                        Modifier.testTag("AssetEditorDeleteConfirm")
                            .pressBounce(interactionSource = deleteConfirmInteractionSource),
                    shapes =
                        ButtonDefaults.shapes(
                            shape = CircleShape,
                            pressedShape = RoundedCornerShape(percent = 32),
                        ),
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
                    interactionSource = deleteCancelInteractionSource,
                    modifier =
                        Modifier.pressBounce(interactionSource = deleteCancelInteractionSource),
                    shapes =
                        ButtonDefaults.shapes(
                            shape = CircleShape,
                            pressedShape = RoundedCornerShape(percent = 32),
                        ),
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
    BackHandler(
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
                TextButton(
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
                TextButton(
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
    Scaffold(
        modifier =
            Modifier.fillMaxSize().testTag("AssetEditorSheet").pointerInput(Unit) {
                // Own the entire overlay hit region while leaving child gestures untouched.
                awaitPointerEventScope {
                    while (true) awaitPointerEvent(
                            androidx.compose.ui.input.pointer.PointerEventPass.Final
                        )
                        .changes
                        .forEach { it.consume() }
                }
            },
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
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
                        shapes =
                            ButtonDefaults.shapes(
                                shape = RoundedCornerShape(12.dp),
                                pressedShape = RoundedCornerShape(8.dp),
                            ),
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
        BoxWithConstraints(
            Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).imePadding()
        ) {
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
                if (typeState == AccountType.CC) CreditCardCycleBadges(
                    AccountEntity(id = editingAccountId ?: 0L, name = "", type = typeState, balance = 0.0, icon = "",
                        creditLimit = creditLimitText.toDoubleOrNull(), billingDate = billingDateText.toIntOrNull(),
                        paymentDate = paymentDateText.toIntOrNull(), paymentDeadline = paymentDeadlineText.toIntOrNull()), state.transactions)
            }
            val previewCard: @Composable () -> Unit = {
                Card(
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(160.dp)
                            .testTag("AssetEditorPreview")
                            .then(
                                if (selectedCardImageUri.isEmpty()) {
                                    Modifier.rotate3DOnTouch()
                                } else {
                                    Modifier.pointerInput(selectedCardImageUri) {
                                        detectTransformGestures { _, pan, zoom, _ ->
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
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                ) {
                    Box(
                        modifier =
                            Modifier.fillMaxSize()
                                .clip(RoundedCornerShape(22.dp))
                                .drawCardBackground(
                                    bitmap = previewCardImage,
                                    themeName = selectedCardTheme,
                                    patternName = selectedCardPattern,
                                    isDark = isDark,
                                    offsetX = selectedCardBgOffsetX,
                                    offsetY = selectedCardBgOffsetY,
                                    scale = selectedCardBgScale,
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
                                    Modifier.matchParentSize().clip(RoundedCornerShape(22.dp)),
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
                                    color = Color.White.copy(alpha = 0.12f),
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
                                    val previewAccent =
                                        accountAccent(typeState, selectedAccentColorKey)
                                    Box(
                                        modifier =
                                            Modifier.padding(end = 8.dp)
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(previewAccent.background)
                                                .border(
                                                    1.dp,
                                                    Color.White.copy(alpha = 0.8f),
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
                                        color = Color.White,
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
                                        text =
                                            when (typeState) {
                                                AccountType.BANK ->
                                                    accountTypeDisplayLabel(AccountType.BANK)
                                                AccountType.CASH ->
                                                    accountTypeDisplayLabel(AccountType.CASH)
                                                AccountType.DEBIT ->
                                                    accountTypeDisplayLabel(AccountType.DEBIT)
                                                AccountType.CC ->
                                                    accountTypeDisplayLabel(AccountType.CC)
                                            },
                                        color = Color.White.copy(alpha = 0.7f),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 1.sp,
                                    )
                                    if (
                                        cardLast4Text.length == 4 && typeState != AccountType.CASH
                                    ) {
                                        Text(
                                            text = "•••• $cardLast4Text",
                                            color = Color.White.copy(alpha = 0.85f),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                        )
                                    } else if (
                                        nicknameText.isNotBlank() &&
                                            nicknameText.trim() != nameText.trim()
                                    ) {
                                        Text(
                                            text = nameText,
                                            color = Color.White.copy(alpha = 0.85f),
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
                                    color = Color.White,
                                )
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
                            Row(
                                modifier =
                                    Modifier.fillMaxWidth()
                                        .horizontalFadingEdge(16.dp, 16.dp)
                                        .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Spacer(modifier = Modifier.width(4.dp))
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
                                Spacer(modifier = Modifier.width(4.dp))
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
                            val plusInteraction = remember { MutableInteractionSource() }
                            CompletePressFilledTonalButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val currentVal = balanceText.toDoubleOrNull() ?: 0.0
                                    val delta = quickAdjustAmountText.toDoubleOrNull() ?: 0.0
                                    balanceText =
                                        String.format(Locale.US, "%.2f", currentVal + delta)
                                },
                                shapes =
                                    ButtonDefaults.shapes(
                                        shape = RoundedCornerShape(16.dp),
                                        pressedShape = RoundedCornerShape(12.dp),
                                    ),
                                interactionSource = plusInteraction,
                                modifier =
                                    Modifier.height(56.dp)
                                        .pressBounce(interactionSource = plusInteraction),
                            ) {
                                Text(
                                    "+",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            val minusInteraction = remember { MutableInteractionSource() }
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
                                shapes =
                                    ButtonDefaults.shapes(
                                        shape = RoundedCornerShape(16.dp),
                                        pressedShape = RoundedCornerShape(12.dp),
                                    ),
                                interactionSource = minusInteraction,
                                modifier =
                                    Modifier.height(56.dp)
                                        .pressBounce(interactionSource = minusInteraction),
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
                                        spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow,
                                        ),
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
                                LinearProgressIndicator(
                                    progress = { animatedUtil },
                                    modifier =
                                        Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                                    color = animatedBarColor,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
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
                                )
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
                                        modifier = Modifier.menuAnchor().fillMaxWidth(),
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
                                        modifier = Modifier.menuAnchor().fillMaxWidth(),
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
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
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
                                "default" to Pair(Color(0xFF161B22), Color(0xFF242C37)),
                                "ocean" to Pair(Color(0xFF0D47A1), Color(0xFF1976D2)),
                                "emerald" to Pair(Color(0xFF1B5E20), Color(0xFF388E3C)),
                                "sunset" to Pair(Color(0xFFE65100), Color(0xFFF57C00)),
                                "purple" to Pair(Color(0xFF4A148C), Color(0xFF7B1FA2)),
                                "rose" to Pair(Color(0xFF880E4F), Color(0xFFC2185B)),
                                "crimson" to Pair(Color(0xFFB71C1C), Color(0xFFD32F2F)),
                            )

                        Row(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .horizontalFadingEdge(16.dp, 16.dp)
                                    .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Spacer(modifier = Modifier.width(4.dp))
                            themes.forEach { (themeKey, colors) ->
                                val isSelected = selectedCardTheme == themeKey
                                val themeLabel = accountCardThemeLabel(themeKey)
                                val animatedScale by
                                    animateFloatAsState(
                                        targetValue = if (isSelected) 1.15f else 1.0f,
                                        animationSpec =
                                            spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMediumLow,
                                            ),
                                        label = "swatchScale_$themeKey",
                                    )
                                val animatedBorderWidth by
                                    animateDpAsState(
                                        targetValue = if (isSelected) 3.dp else 1.dp,
                                        animationSpec =
                                            spring(stiffness = Spring.StiffnessMediumLow),
                                        label = "swatchBorder_$themeKey",
                                    )
                                Box(
                                    modifier =
                                        Modifier.padding(vertical = 4.dp)
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
                                                    else Color.White.copy(alpha = 0.35f),
                                                shape = CircleShape,
                                            )
                                            .clickable { selectedCardTheme = themeKey }
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
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(4.dp))
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
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
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
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
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
                            val uploadInteraction = remember { MutableInteractionSource() }
                            CompletePressOutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    galleryLauncher.launch("image/*")
                                },
                                shapes =
                                    ButtonDefaults.shapes(
                                        shape = RoundedCornerShape(16.dp),
                                        pressedShape = RoundedCornerShape(12.dp),
                                    ),
                                interactionSource = uploadInteraction,
                                modifier =
                                    Modifier.weight(1f)
                                        .pressBounce(interactionSource = uploadInteraction),
                            ) {
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (selectedCardImageUri.isEmpty())
                                        stringResource(R.string.ah_upload_image)
                                    else stringResource(R.string.ah_change_image)
                                )
                            }
                            if (selectedCardImageUri.isNotEmpty()) {
                                val cropInteraction = remember { MutableInteractionSource() }
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
                                    shapes =
                                        ButtonDefaults.shapes(
                                            shape = RoundedCornerShape(16.dp),
                                            pressedShape = RoundedCornerShape(12.dp),
                                        ),
                                    interactionSource = cropInteraction,
                                    modifier =
                                        Modifier.pressBounce(interactionSource = cropInteraction),
                                ) {
                                    Icon(
                                        Icons.Default.Crop,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
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
                                    colors =
                                        ButtonDefaults.textButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        ),
                                    shapes =
                                        ButtonDefaults.shapes(
                                            shape = CircleShape,
                                            pressedShape = RoundedCornerShape(percent = 32),
                                        ),
                                    interactionSource = removeInteraction,
                                    modifier =
                                        Modifier.pressBounce(interactionSource = removeInteraction),
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
                                            TextButton(
                                                onClick = {
                                                    haptic.performHapticFeedback(
                                                        HapticFeedbackType.LongPress
                                                    )
                                                    selectedCardBgOffsetX = 0f
                                                    selectedCardBgOffsetY = 0f
                                                    selectedCardBgScale = 1f
                                                },
                                                contentPadding =
                                                    PaddingValues(
                                                        horizontal = 6.dp,
                                                        vertical = 2.dp,
                                                    ),
                                            ) {
                                                Icon(
                                                    Icons.Default.Refresh,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(13.dp),
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
                                            Slider(
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
                                            Slider(
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
                                            Slider(
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
                            FilledTonalButton(
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
                                shape = RoundedCornerShape(12.dp),
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
                if (!isWide) { previewCard(); cyclePreview() }
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
                if (typeState == AccountType.CC) {
                    Text(
                        stringResource(R.string.ah_billing_schedule),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    billingContent()
                }
                Text(
                    stringResource(R.string.asset_editor_appearance),
                    style = MaterialTheme.typography.titleMedium,
                )
                designContent()
                Text(
                    stringResource(R.string.asset_editor_automation),
                    style = MaterialTheme.typography.titleMedium,
                )
                automationContent()
                if (editingAccount != null)
                    CompletePressOutlinedButton(
                        onClick = { showDeleteConfirmDialog = true },
                        enabled = !saving,
                        shapes =
                            ButtonDefaults.shapes(
                                shape = RoundedCornerShape(12.dp),
                                pressedShape = RoundedCornerShape(8.dp),
                            ),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                    ) {
                        Icon(Icons.Default.Delete, null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.btn_delete))
                    }
                Spacer(Modifier.height(16.dp))
            }
            Row(
                modifier =
                    Modifier.align(Alignment.TopCenter)
                        .widthIn(max = 840.dp)
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(horizontal = if (isWide) 24.dp else 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                if (isWide)
                    Column(
                        Modifier.width(260.dp)
                            .padding(top = 12.dp)
                            .testTag("AssetEditorPreviewPane")
                    ) {
                        previewCard()
                        cyclePreview()
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
                                .padding(vertical = 12.dp)
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
