@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.settings

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
