@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.main

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import com.example.vibefinance.util.rememberVibeHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vibefinance.R
import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.ui.common.pressBounce
import com.example.vibefinance.ui.components.AppSwitch
import com.example.vibefinance.ui.components.CompletePressButton
import com.example.vibefinance.util.InstalledAppInfo
import com.example.vibefinance.util.LocalAppManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AllowedInterceptAppsDialog(onDismiss: () -> Unit) {
    val selectedApps by InMemoryDatabase.selectedInterceptApps.collectAsStateWithLifecycle()
    AllowedInterceptAppsPicker(
        selectedApps = selectedApps,
        onToggleApp = InMemoryDatabase::toggleInterceptApp,
        onDismiss = onDismiss,
    )
}

@Composable
internal fun AllowedInterceptAppsPicker(
    selectedApps: Set<String>,
    onToggleApp: (String) -> Unit,
    onDismiss: () -> Unit,
    loadApps: suspend (Context) -> List<InstalledAppInfo> = LocalAppManager::loadInstalledApps,
    loadIcon: suspend (Context, String) -> Bitmap? = ::loadAllowedAppIcon,
) {
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var retryGeneration by remember { mutableStateOf(0) }
    // Compose the dialog before scanning packages; cancellation follows the dialog lifecycle.
    val catalogResult by
        produceState<Result<List<InstalledAppInfo>>?>(null, context, retryGeneration) {
            value = null
            value =
                try {
                    Result.success(withContext(Dispatchers.IO) { loadApps(context) })
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (failure: Exception) {
                    Result.failure(failure)
                }
        }
    val installedApps = catalogResult?.getOrNull().orEmpty()

    val filteredApps =
        remember(searchQuery, installedApps) {
            if (searchQuery.isBlank()) {
                installedApps
            } else {
                installedApps.filter {
                    it.appName.contains(searchQuery, ignoreCase = true) ||
                        it.packageName.contains(searchQuery, ignoreCase = true)
                }
            }
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = stringResource(R.string.allowed_apps_dialog_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.allowed_apps_dialog_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            stringResource(R.string.allowed_apps_search_hint),
                            fontSize = 13.sp,
                        )
                    },
                    modifier = Modifier.fillMaxWidth().testTag("AllowedAppsSearch"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.ui_main_search),
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.ui_main_clear),
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    },
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(380.dp).testTag("AllowedAppsList"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (catalogResult == null) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            LoadingIndicator(
                                modifier = Modifier.size(48.dp).testTag("AllowedAppsLoading")
                            )
                        }
                    }
                } else if (catalogResult?.isFailure == true) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(stringResource(R.string.assets_linked_app_load_failed))
                            TextButton(onClick = { retryGeneration++ }) {
                                Text(stringResource(R.string.assets_linked_app_retry))
                            }
                        }
                    }
                } else if (filteredApps.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.allowed_apps_no_matches),
                                style = MaterialTheme.typography.bodyMedium,
                                color =
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            )
                        }
                    }
                } else {
                    items(filteredApps, key = { it.packageName }) { appInfo ->
                        val iconBitmap by
                            produceState<Bitmap?>(
                                appInfo.iconBitmap,
                                context,
                                appInfo.packageName,
                            ) {
                                value = loadIcon(context, appInfo.packageName)
                            }
                        val isChecked = selectedApps.contains(appInfo.packageName)

                        Surface(
                            onClick = { onToggleApp(appInfo.packageName) },
                            shape = RoundedCornerShape(14.dp),
                            color =
                                if (isChecked)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                            border =
                                BorderStroke(
                                    1.dp,
                                    if (isChecked)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    else
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                ),
                            modifier =
                                Modifier.fillMaxWidth()
                                    .testTag("AllowedAppsOption_${appInfo.packageName}"),
                        ) {
                            Row(
                                modifier =
                                    Modifier.fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Box(Modifier.size(36.dp)) {
                                        iconBitmap?.let { bitmap ->
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = null,
                                                modifier =
                                                    Modifier.fillMaxSize()
                                                        .clip(RoundedCornerShape(9.dp))
                                                        .border(
                                                            0.8.dp,
                                                            MaterialTheme.colorScheme.outlineVariant
                                                                .copy(alpha = 0.4f),
                                                            RoundedCornerShape(9.dp),
                                                        ),
                                            )
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = appInfo.appName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow =
                                                androidx.compose.ui.text.style.TextOverflow
                                                    .Ellipsis,
                                        )
                                        Text(
                                            text = appInfo.packageName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color =
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                    alpha = 0.6f
                                                ),
                                            maxLines = 1,
                                            overflow =
                                                androidx.compose.ui.text.style.TextOverflow
                                                    .Ellipsis,
                                        )
                                    }
                                }

                                AppSwitch(
                                    checked = isChecked,
                                    onCheckedChange = { onToggleApp(appInfo.packageName) },
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val confirmInteraction = remember { MutableInteractionSource() }
            val haptic = rememberVibeHapticFeedback()
            CompletePressButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onDismiss()
                },
                shapes =
                    ButtonDefaults.shapes(
                        shape = CircleShape,
                        pressedShape = RoundedCornerShape(percent = 32),
                    ),
                interactionSource = confirmInteraction,
                modifier = Modifier.pressBounce(interactionSource = confirmInteraction),
            ) {
                Text(stringResource(R.string.btn_confirm))
            }
        },
    )
}

private suspend fun loadAllowedAppIcon(context: Context, packageName: String): Bitmap? =
    withContext(Dispatchers.IO) { LocalAppManager.getAppIcon(context, packageName) }
