@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.settings

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.BatterySaver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.vibefinance.util.BatteryOptimizationHelper
import com.example.vibefinance.util.LocalAppManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.R
import com.example.vibefinance.ui.preferences.LocalExperience
import com.example.vibefinance.ui.preferences.MotionLevel
import com.example.vibefinance.util.VibeHaptics

@Composable
internal fun AutoLoggingPanel(
    enabled: Boolean,
    onOpenNotificationAccess: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val view = LocalView.current
    val motion = LocalExperience.current.motion
    val effects = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
    val spatial = MaterialTheme.motionScheme.defaultSpatialSpec<androidx.compose.ui.unit.IntSize>()
    val enter = when (motion) {
        MotionLevel.FULL -> expandVertically(spatial, expandFrom = Alignment.Top) + fadeIn(effects)
        MotionLevel.REDUCED -> fadeIn(effects)
        MotionLevel.MINIMAL -> EnterTransition.None
    }
    val exit = when (motion) {
        MotionLevel.FULL -> shrinkVertically(spatial, shrinkTowards = Alignment.Top) + fadeOut(effects)
        MotionLevel.REDUCED -> fadeOut(effects)
        MotionLevel.MINIMAL -> ExitTransition.None
    }
    Column(Modifier.fillMaxWidth()) {
        AnimatedVisibility(enabled, enter = enter, exit = exit,
            modifier = Modifier.testTag("AutoLoggingDetails")) {
            Column(Modifier.fillMaxWidth(), content = content)
        }
        // Power saving guideline & battery optimization redirect
        Spacer(Modifier.height(12.dp))
        PowerSavingGuidelineCard()

        // Keep recovery reachable when logging is off or notification access has been revoked.
        Spacer(Modifier.height(12.dp))
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth().testTag("AutoLoggingRecoveryGuide")) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.auto_logging_recovery_title), style = MaterialTheme.typography.labelLarge)
                Text(stringResource(R.string.auto_logging_recovery_body), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = { VibeHaptics.lightTick(view); onOpenNotificationAccess() },
                    modifier = Modifier.heightIn(min = 48.dp).testTag("AutoLoggingNotificationAccess")) {
                    Text(stringResource(R.string.auto_logging_open_access))
                }
            }
        }
    }
}

@Composable
internal fun PowerSavingGuidelineCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isUnrestricted by remember {
        mutableStateOf(BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isUnrestricted = BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var isExpanded by rememberSaveable { mutableStateOf(!isUnrestricted) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.fillMaxWidth().testTag("PowerSavingGuidelineCard")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        VibeHaptics.lightTick(view)
                        isExpanded = !isExpanded
                    }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isUnrestricted) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isUnrestricted) Icons.Outlined.BatteryChargingFull else Icons.Outlined.BatterySaver,
                            contentDescription = null,
                            tint = if (isUnrestricted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stringResource(R.string.battery_guideline_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isUnrestricted) stringResource(R.string.battery_status_unrestricted)
                                   else stringResource(R.string.battery_status_optimized),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isUnrestricted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.battery_guideline_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.battery_guideline_tip1),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.battery_guideline_tip2),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.battery_guideline_tip3),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isUnrestricted) {
                            Button(
                                onClick = {
                                    VibeHaptics.impactMedium(view)
                                    BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(context)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).heightIn(min = 40.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.battery_btn_unrestrict),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    VibeHaptics.lightTick(view)
                                    BatteryOptimizationHelper.openAppBatterySettings(context)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).heightIn(min = 40.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.battery_btn_app_settings),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                VibeHaptics.lightTick(view)
                                BatteryOptimizationHelper.openSystemBatterySaverSettings(context)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).heightIn(min = 40.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.battery_btn_system_saver),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun AutoLoggingAppsSummary(packages: List<String>, onManage: () -> Unit) {
    val context = LocalContext.current
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
        .clickable(onClick = onManage).padding(vertical = 4.dp).testTag("AutoLoggingApps"),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(R.string.settings_manage_apps_label),
                modifier = Modifier.fillMaxWidth().testTag("AutoLoggingAppsLabel"),
                style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Text(when (packages.size) {
                0 -> stringResource(R.string.settings_apps_none_enabled)
                1 -> stringResource(R.string.settings_apps_enabled_one)
                else -> stringResource(R.string.settings_apps_enabled_count, packages.size)
            }, modifier = Modifier.fillMaxWidth().testTag("AutoLoggingAppsDescription"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).testTag("AutoLoggingAppsIcons"),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            packages.take(5).forEach { packageName ->
                val bitmap = remember(context, packageName) { LocalAppManager.getAppIcon(context, packageName) }
                bitmap?.let {
                    Image(it.asImageBitmap(), contentDescription = remember(context, packageName) {
                        LocalAppManager.getAppLabel(context, packageName)
                    }, modifier = Modifier.size(28.dp).clip(RoundedCornerShape(7.dp)).border(0.8.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f), RoundedCornerShape(7.dp)))
                }
            }
            Surface(Modifier.size(28.dp).testTag("AutoLoggingAppsManage"), shape = RoundedCornerShape(7.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Add, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}
