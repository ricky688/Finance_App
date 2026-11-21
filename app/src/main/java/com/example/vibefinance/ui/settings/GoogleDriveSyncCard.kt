@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vibefinance.R
import com.example.vibefinance.ui.components.AppSwitch
import com.example.vibefinance.util.GoogleDriveAuthState
import com.example.vibefinance.util.GoogleDriveSyncEngine
import com.example.vibefinance.util.GoogleDriveSyncStatus
import com.example.vibefinance.util.VibeHaptics
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

@Composable
fun GoogleDriveSyncCard(
    onLaunchSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()

    val authState by GoogleDriveSyncEngine.authState.collectAsStateWithLifecycle()
    val syncStatus by GoogleDriveSyncEngine.syncStatus.collectAsStateWithLifecycle()

    var autoSyncEnabled by remember {
        mutableStateOf(GoogleDriveSyncEngine.isAutoSyncEnabled(context))
    }

    val isSyncing = syncStatus is GoogleDriveSyncStatus.Syncing

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            1.dp,
            if (authState.isAuthenticated) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("GoogleDriveSyncCard")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Icon + Title + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (authState.isAuthenticated) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSyncing) {
                            LoadingIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = if (authState.isAuthenticated) Icons.Default.CloudDone else Icons.Default.Cloud,
                                contentDescription = null,
                                tint = if (authState.isAuthenticated) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.google_drive_sync_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (authState.isAuthenticated) {
                                authState.email ?: stringResource(R.string.google_drive_sync_connected)
                            } else {
                                stringResource(R.string.google_drive_sync_desc)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            maxLines = if (authState.isAuthenticated) 1 else 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // M3 Linear Progress Bar during sync
            AnimatedVisibility(
                visible = isSyncing,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val minimalMotion = com.example.vibefinance.ui.preferences.LocalExperience.current.motion ==
                    com.example.vibefinance.ui.preferences.MotionLevel.MINIMAL
                LinearWavyProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    amplitude = if (minimalMotion) 0f else 1f
                )
            }

            // If NOT connected: Show Connect Button
            if (!authState.isAuthenticated) {
                com.example.vibefinance.ui.components.CompletePressButton(
                    onClick = {
                        VibeHaptics.lightTick(view)
                        onLaunchSignIn()
                    },
                    shapes = com.example.vibefinance.ui.components.AppButtonDefaults.shapes(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp).then(com.example.vibefinance.ui.components.LocalPostPressIconBounce.current)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.google_drive_sync_connect_btn),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // If Connected: Options, Last Synced & Actions
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Last synced row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val lastSyncedTime = when (val s = syncStatus) {
                        is GoogleDriveSyncStatus.Success -> s.syncedTime
                        is GoogleDriveSyncStatus.Idle -> s.lastSyncedTime
                        is GoogleDriveSyncStatus.Error -> s.lastSyncedTime
                        else -> null
                    }

                    val lastSyncedStr = if (lastSyncedTime != null && lastSyncedTime > 0) {
                        val diff = System.currentTimeMillis() - lastSyncedTime
                        if (diff < 60_000L) {
                            stringResource(R.string.google_drive_sync_just_now)
                        } else {
                            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(lastSyncedTime))
                        }
                    } else {
                        "--"
                    }

                    Text(
                        text = stringResource(R.string.google_drive_sync_last_synced, lastSyncedStr),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Error text if any
                    if (syncStatus is GoogleDriveSyncStatus.Error) {
                        Text(
                            text = (syncStatus as GoogleDriveSyncStatus.Error).message,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Auto-sync on open toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = stringResource(R.string.google_drive_sync_auto_open),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.google_drive_sync_auto_open_desc),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                    }

                    AppSwitch(
                        checked = autoSyncEnabled,
                        onCheckedChange = { checked ->
                            VibeHaptics.selection(view)
                            autoSyncEnabled = checked
                            GoogleDriveSyncEngine.setAutoSyncEnabled(context, checked)
                        }
                    )
                }

                // Action buttons: Sync Now SplitButton + Disconnect
                var showSyncMenu by remember { mutableStateOf(false) }
                val minimalMotion = com.example.vibefinance.ui.preferences.LocalExperience.current.motion ==
                    com.example.vibefinance.ui.preferences.MotionLevel.MINIMAL
                val menuArrowRotation by animateFloatAsState(
                    targetValue = if (showSyncMenu) 180f else 0f,
                    animationSpec = if (minimalMotion) snap() else com.example.vibefinance.theme.VibeMotion.fastSpatial(),
                    label = "driveSplitArrow"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SplitButtonLayout(
                        leadingButton = {
                            com.example.vibefinance.ui.components.CompletePressTonalSplitLeadingButton(
                                onClick = {
                                    VibeHaptics.lightTick(view)
                                    coroutineScope.launch {
                                        GoogleDriveSyncEngine.sync(context, silent = false)
                                    }
                                },
                                enabled = !isSyncing
                            ) {
                                if (isSyncing) {
                                    LoadingIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.google_drive_sync_status_syncing),
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(SplitButtonDefaults.LeadingIconSize)
                                            .then(com.example.vibefinance.ui.components.LocalPostPressIconBounce.current)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.google_drive_sync_now_btn),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        },
                        trailingButton = {
                            Box {
                                com.example.vibefinance.ui.components.CompletePressTonalSplitTrailingButton(
                                    checked = showSyncMenu,
                                    onCheckedChange = {
                                        VibeHaptics.selection(view)
                                        showSyncMenu = it
                                    },
                                    enabled = !isSyncing
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = stringResource(R.string.google_drive_sync_more_options),
                                        modifier = Modifier
                                            .size(SplitButtonDefaults.TrailingIconSize)
                                            .graphicsLayer { rotationZ = menuArrowRotation }
                                    )
                                }
                                DropdownMenu(
                                    expanded = showSyncMenu,
                                    onDismissRequest = { showSyncMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.google_drive_force_upload)) },
                                        leadingIcon = { Icon(Icons.Default.CloudUpload, contentDescription = null) },
                                        onClick = {
                                            showSyncMenu = false
                                            VibeHaptics.lightTick(view)
                                            coroutineScope.launch {
                                                GoogleDriveSyncEngine.forceUpload(context)
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.google_drive_force_download)) },
                                        leadingIcon = { Icon(Icons.Default.CloudDownload, contentDescription = null) },
                                        onClick = {
                                            showSyncMenu = false
                                            VibeHaptics.warningAlert(view)
                                            coroutineScope.launch {
                                                GoogleDriveSyncEngine.forceDownload(context)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    )

                    com.example.vibefinance.ui.components.CompletePressTextButton(
                        onClick = {
                            VibeHaptics.warningAlert(view)
                            GoogleDriveSyncEngine.signOut(context)
                        },
                        shapes = com.example.vibefinance.ui.components.AppButtonDefaults.shapes(),
                    ) {
                        Text(
                            text = stringResource(R.string.google_drive_sync_disconnect),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}
