@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.settings

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vibefinance.R
import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.service.InterceptedNotificationEntry
import com.example.vibefinance.service.InterceptedNotificationLog
import com.example.vibefinance.service.InterceptedNotificationStatus
import com.example.vibefinance.service.PendingPayment
import com.example.vibefinance.ui.accounts.assetEditorFadeSpec
import com.example.vibefinance.ui.accounts.assetEditorScaleSpec
import com.example.vibefinance.ui.accounts.assetEditorSlideSpec
import com.example.vibefinance.ui.components.AppButtonDefaults
import com.example.vibefinance.ui.components.CompletePressTextButton
import com.example.vibefinance.ui.components.postPressIconBounce
import com.example.vibefinance.ui.main.VisualSlotTaggingDialog
import com.example.vibefinance.ui.motion.StaggeredPane
import com.example.vibefinance.ui.motion.paneEntrance
import com.example.vibefinance.ui.motion.predictiveBackPage
import com.example.vibefinance.ui.motion.rememberPredictiveBackPage
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import com.example.vibefinance.util.VibeHaptics
import java.text.DateFormat
import java.util.Date
import java.util.Locale

/**
 * Full-screen intercepted notifications diagnostic screen hosted with the same M3E transition as
 * the Daily add-transaction button (horizontal slide + fade + 1.15x scale entrance).
 */
@Composable
internal fun InterceptedNotificationsHost(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    transition: Transition<Boolean> = updateTransition(visible, label = "Intercepted notifications"),
) {
    val context = LocalContext.current
    LaunchedEffect(visible) {
        if (visible) InterceptedNotificationLog.initialize(context)
    }

    transition.AnimatedVisibility(
        visible = { it },
        modifier = modifier.fillMaxSize(),
        enter = slideInHorizontally(assetEditorSlideSpec()) { it } +
            fadeIn(assetEditorFadeSpec()) + scaleIn(assetEditorScaleSpec(), initialScale = 1.15f),
        exit = slideOutHorizontally(assetEditorSlideSpec()) { it } +
            fadeOut(assetEditorFadeSpec()) + scaleOut(assetEditorScaleSpec(), targetScale = 1.15f),
    ) {
        val backState = rememberPredictiveBackPage(enabled = true, onBack = onDismiss)
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .predictiveBackPage(backState)
                .testTag("InterceptedNotificationsScreen"),
            color = MaterialTheme.colorScheme.surface
        ) {
            StaggeredPane(modifier = Modifier.fillMaxSize()) { paneState ->
                InterceptedNotificationsContent(
                    onDismiss = { paneState.close(onDismiss) },
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                )
            }
        }
    }
}

@Composable
private fun InterceptedNotificationsContent(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val allEntries by InterceptedNotificationLog.entries.collectAsStateWithLifecycle()
    var selectedStatus by remember { mutableStateOf<InterceptedNotificationStatus?>(null) }
    var templateTarget by remember { mutableStateOf<InterceptedNotificationEntry?>(null) }

    val filteredEntries = remember(allEntries, selectedStatus) {
        if (selectedStatus == null) allEntries else allEntries.filter { it.status == selectedStatus }
    }

    Box(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 840.dp)
                .fillMaxSize()
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .paneEntrance(0)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                androidx.compose.material3.IconButton(
                    onClick = { VibeHaptics.selection(view); onDismiss() },
                    modifier = Modifier.testTag("InterceptedNotificationsBack")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.btn_close)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.intercepted_log_entry_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.intercepted_log_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (allEntries.isNotEmpty()) {
                    CompletePressTextButton(
                        onClick = {
                            VibeHaptics.warningAlert(view)
                            InterceptedNotificationLog.clear(context)
                        },
                        shapes = AppButtonDefaults.shapes(),
                        modifier = Modifier.testTag("InterceptedNotificationsClear")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteSweep,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp).postPressIconBounce()
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.intercepted_log_clear))
                    }
                }
            }

            // Status filter chips
            Row(
                modifier = Modifier
                    .paneEntrance(1)
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                androidx.compose.material3.FilterChip(
                    selected = selectedStatus == null,
                    onClick = { VibeHaptics.lightTick(view); selectedStatus = null },
                    shape = RoundedCornerShape(if (selectedStatus == null) 20.dp else 10.dp),
                    label = { Text("${stringResource(R.string.intercepted_log_filter_all)} (${allEntries.size})") }
                )
                InterceptedNotificationStatus.entries.forEach { status ->
                    val count = allEntries.count { it.status == status }
                    val chipSelected = selectedStatus == status
                    androidx.compose.material3.FilterChip(
                        selected = chipSelected,
                        onClick = {
                            VibeHaptics.lightTick(view)
                            selectedStatus = if (chipSelected) null else status
                        },
                        shape = RoundedCornerShape(if (chipSelected) 20.dp else 10.dp),
                        label = { Text("${stringResource(statusLabelRes(status))} ($count)") }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (filteredEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .paneEntrance(2)
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.NotificationsNone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Text(
                                text = stringResource(R.string.intercepted_log_entry_empty),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stringResource(R.string.intercepted_log_empty_state),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .paneEntrance(2)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(filteredEntries, key = { _, item -> item.id }) { _, entry ->
                        InterceptedNotificationRowCard(
                            entry = entry,
                            onCreateTemplate = {
                                VibeHaptics.lightTick(view)
                                templateTarget = entry
                            }
                        )
                    }
                }
            }
        }
    }

    templateTarget?.let { entry ->
        val syntheticPayment = remember(entry.id) {
            PendingPayment(
                id = entry.paymentId ?: entry.id,
                fingerprint = entry.id,
                sourcePackage = entry.packageName,
                assetHint = resolveAppLabel(context, entry.packageName),
                merchant = entry.merchant.orEmpty(),
                amount = entry.amount ?: 0.0,
                detectedAt = entry.receivedAt,
                rawTitle = entry.title,
                rawText = entry.text
            )
        }
        VisualSlotTaggingDialog(
            payment = syntheticPayment,
            onDismiss = { templateTarget = null },
            onSaveAndApply = { template, _ ->
                InMemoryDatabase.saveNotificationTemplate(template)
                templateTarget = null
            }
        )
    }
}

@Composable
private fun InterceptedNotificationRowCard(
    entry: InterceptedNotificationEntry,
    onCreateTemplate: () -> Unit
) {
    val context = LocalContext.current
    val appLabel = remember(entry.packageName) { resolveAppLabel(context, entry.packageName) }
    val appIcon = remember(entry.packageName) { loadAppIconBitmap(context, entry.packageName) }
    val formattedTime = remember(entry.receivedAt) {
        DateFormat.getDateTimeInstance(
            DateFormat.SHORT,
            DateFormat.SHORT,
            context.resources.configuration.locales[0]
        ).format(Date(entry.receivedAt))
    }

    val (badgeBg, badgeFg) = when (entry.status) {
        InterceptedNotificationStatus.LOGGED ->
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        InterceptedNotificationStatus.PENDING ->
            MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        InterceptedNotificationStatus.IGNORED ->
            MaterialTheme.colorScheme.surfaceContainerHighest to MaterialTheme.colorScheme.onSurfaceVariant
        InterceptedNotificationStatus.FILTERED ->
            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onCreateTemplate)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (appIcon != null) {
                    Image(
                        bitmap = appIcon,
                        contentDescription = appLabel,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(shape = RoundedCornerShape(999.dp), color = badgeBg) {
                    Text(
                        text = stringResource(statusLabelRes(entry.status)),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = badgeFg,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            if (entry.amount != null && entry.amount > 0.0) {
                val summary = buildString {
                    append(String.format(Locale.US, "HK$%.2f", entry.amount))
                    if (!entry.merchant.isNullOrBlank()) {
                        append(" · ")
                        append(entry.merchant)
                    }
                }
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (entry.title.isNotBlank()) {
                        Text(
                            text = entry.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (entry.text.isNotBlank()) {
                        Text(
                            text = entry.text,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (entry.status == InterceptedNotificationStatus.FILTERED ||
                entry.status == InterceptedNotificationStatus.IGNORED
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(R.string.intercepted_log_create_template),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun statusLabelRes(status: InterceptedNotificationStatus): Int = when (status) {
    InterceptedNotificationStatus.LOGGED -> R.string.intercepted_log_status_logged
    InterceptedNotificationStatus.PENDING -> R.string.intercepted_log_status_pending
    InterceptedNotificationStatus.IGNORED -> R.string.intercepted_log_status_ignored
    InterceptedNotificationStatus.FILTERED -> R.string.intercepted_log_status_filtered
}

internal fun resolveAppLabel(context: android.content.Context, packageName: String): String {
    if (packageName.isBlank()) return ""
    return runCatching {
        val pm = context.packageManager
        val info = pm.getApplicationInfo(packageName, 0)
        pm.getApplicationLabel(info).toString()
    }.getOrDefault(packageName)
}

private fun loadAppIconBitmap(context: android.content.Context, packageName: String): ImageBitmap? = runCatching {
    val drawable = context.packageManager.getApplicationIcon(packageName)
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        drawable.bitmap.asImageBitmap()
    } else {
        val w = drawable.intrinsicWidth.coerceAtLeast(1)
        val h = drawable.intrinsicHeight.coerceAtLeast(1)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, w, h)
        drawable.draw(canvas)
        bmp.asImageBitmap()
    }
}.getOrNull()
