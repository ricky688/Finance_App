package com.example.vibefinance.ui.accounts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.ui.AccountSaveResult
import com.example.vibefinance.ui.FinanceIntent
import com.example.vibefinance.ui.FinanceUiState
import java.util.UUID

@Stable
class AssetEditorState {
    var isOpen by mutableStateOf(false)
        private set

    var sessionId by mutableStateOf(UUID.randomUUID().toString())
        private set

    var accountId by mutableStateOf<Long?>(null)
        private set

    var prefillAccount by mutableStateOf<AccountEntity?>(null)
        private set

    var originPaymentId by mutableStateOf<String?>(null)
        private set

    var originTab by mutableStateOf<String?>(null)
        private set

    fun open(
        accountId: Long? = null,
        prefill: AccountEntity? = null,
        paymentId: String? = null,
        tab: String? = null,
    ) {
        this.accountId = accountId
        prefillAccount = prefill
        originPaymentId = paymentId
        originTab = tab
        sessionId = UUID.randomUUID().toString()
        isOpen = true
    }

    fun close() {
        isOpen = false
    }

    companion object {
        val Saver =
            listSaver<AssetEditorState, Any>(
                save = {
                    listOf(
                        it.isOpen,
                        it.sessionId,
                        it.accountId ?: -1L,
                        it.prefillAccount?.let(::encodeEditorAccount).orEmpty(),
                        it.originPaymentId.orEmpty(),
                        it.originTab.orEmpty(),
                    )
                },
                restore = { saved ->
                    AssetEditorState().apply {
                        isOpen = saved[0] as Boolean
                        sessionId = saved[1] as String
                        accountId = (saved[2] as Long).takeIf { it >= 0 }
                        prefillAccount =
                            (saved[3] as String)
                                .takeIf { it.isNotEmpty() }
                                ?.let(::decodeEditorAccount)
                        originPaymentId = (saved[4] as String).takeIf { it.isNotEmpty() }
                        originTab = (saved[5] as String).takeIf { it.isNotEmpty() }
                    }
                },
            )
    }
}

@Composable
fun rememberAssetEditorState(): AssetEditorState =
    rememberSaveable(saver = AssetEditorState.Saver) { AssetEditorState() }

@Composable
fun AssetEditorHost(
    state: FinanceUiState,
    editorState: AssetEditorState,
    onIntent: (FinanceIntent) -> Unit,
    saveResult: AccountSaveResult? = null,
    onAccountCreated: ((AccountEntity) -> Unit)? = null,
    onDismissCreate: (() -> Unit)? = null,
    transition: Transition<Boolean> = updateTransition(editorState.isOpen, label = "Asset editor"),
) {
    transition.AnimatedVisibility(
        visible = { it },
        enter =
            slideInHorizontally(assetEditorSlideSpec()) { it } +
                fadeIn(assetEditorFadeSpec()) +
                scaleIn(assetEditorScaleSpec(), initialScale = 1.15f),
        exit =
            slideOutHorizontally(assetEditorSlideSpec()) { it } +
                fadeOut(assetEditorFadeSpec()) +
                scaleOut(assetEditorScaleSpec(), targetScale = 1.15f),
    ) {
        key(editorState.sessionId) {
            AssetEditorScreen(
                state,
                editorState,
                onIntent,
                saveResult,
                onAccountCreated,
                onDismissCreate,
            )
        }
    }
}

// Saved UI state only; no financial storage or backup schema changes.
internal fun encodeEditorAccount(account: AccountEntity): String =
    org.json
        .JSONObject()
        .apply {
            put("id", account.id)
            put("name", account.name)
            put("nickname", account.nickname ?: org.json.JSONObject.NULL)
            put("type", account.type.name)
            put("balance", account.balance)
            put("icon", account.icon)
            put("creditLimit", account.creditLimit ?: org.json.JSONObject.NULL)
            put("billingDate", account.billingDate ?: org.json.JSONObject.NULL)
            put("paymentDate", account.paymentDate ?: org.json.JSONObject.NULL)
            put("paymentDeadline", account.paymentDeadline ?: org.json.JSONObject.NULL)
            put("cardTheme", account.cardTheme ?: org.json.JSONObject.NULL)
            put("accentColorKey", account.accentColorKey ?: org.json.JSONObject.NULL)
            put("cardLast4", account.cardLast4 ?: org.json.JSONObject.NULL)
            put("cardProtocol", account.cardProtocol ?: org.json.JSONObject.NULL)
            put("cardIssuer", account.cardIssuer ?: org.json.JSONObject.NULL)
            put("cardPattern", account.cardPattern ?: org.json.JSONObject.NULL)
            put("cardImageUri", account.cardImageUri ?: org.json.JSONObject.NULL)
            put(
                "customImageAspectRatio",
                account.customImageAspectRatio ?: org.json.JSONObject.NULL,
            )
            put("cardBgOffsetX", account.cardBgOffsetX)
            put("cardBgOffsetY", account.cardBgOffsetY)
            put("cardBgScale", account.cardBgScale)
            put("minSpendThreshold", account.minSpendThreshold ?: org.json.JSONObject.NULL)
            put("linkedAppPackage", account.linkedAppPackage ?: org.json.JSONObject.NULL)
            put("notificationAliases", account.notificationAliases ?: org.json.JSONObject.NULL)
        }
        .toString()

internal fun decodeEditorAccount(value: String): AccountEntity {
    val json = org.json.JSONObject(value)
    return AccountEntity(
        id = json.getLong("id"),
        name = json.getString("name"),
        nickname = if (json.isNull("nickname")) null else json.getString("nickname"),
        type = AccountType.valueOf(json.getString("type")),
        balance = json.getDouble("balance"),
        icon = json.getString("icon"),
        creditLimit = if (json.isNull("creditLimit")) null else json.getDouble("creditLimit"),
        billingDate = if (json.isNull("billingDate")) null else json.getInt("billingDate"),
        paymentDate = if (json.isNull("paymentDate")) null else json.getInt("paymentDate"),
        paymentDeadline =
            if (json.isNull("paymentDeadline")) null else json.getInt("paymentDeadline"),
        cardTheme = if (json.isNull("cardTheme")) null else json.getString("cardTheme"),
        accentColorKey =
            if (json.isNull("accentColorKey")) null else json.getString("accentColorKey"),
        cardLast4 = if (json.isNull("cardLast4")) null else json.getString("cardLast4"),
        cardProtocol = if (json.isNull("cardProtocol")) null else json.getString("cardProtocol"),
        cardIssuer = if (json.isNull("cardIssuer")) null else json.getString("cardIssuer"),
        cardPattern = if (json.isNull("cardPattern")) null else json.getString("cardPattern"),
        cardImageUri = if (json.isNull("cardImageUri")) null else json.getString("cardImageUri"),
        customImageAspectRatio =
            if (json.isNull("customImageAspectRatio")) null
            else json.getString("customImageAspectRatio"),
        cardBgOffsetX = json.getDouble("cardBgOffsetX").toFloat(),
        cardBgOffsetY = json.getDouble("cardBgOffsetY").toFloat(),
        cardBgScale = json.getDouble("cardBgScale").toFloat(),
        minSpendThreshold =
            if (json.isNull("minSpendThreshold")) null else json.getDouble("minSpendThreshold"),
        linkedAppPackage =
            if (json.isNull("linkedAppPackage")) null else json.getString("linkedAppPackage"),
        notificationAliases =
            if (json.isNull("notificationAliases")) null else json.getString("notificationAliases"),
    )
}
