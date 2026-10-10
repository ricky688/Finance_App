package com.example.vibefinance.ui.preferences

import com.example.vibefinance.ui.components.AppSwitch

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.vibefinance.R
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.FinanceIntent

@Composable
fun MotionBlurSettings(state: FinanceUiState, dispatch: (FinanceIntent) -> Unit) {
    Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.experience_motion_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.experience_motion_desc), style = MaterialTheme.typography.bodySmall)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                MotionLevel.entries.forEach { level ->
                    FilterChip(state.motionLevel == level, { dispatch(FinanceIntent.SetMotionLevel(level)) },
                        label = { Text(stringResource(when (level) { MotionLevel.FULL -> R.string.experience_full
                            MotionLevel.REDUCED -> R.string.experience_reduced; MotionLevel.MINIMAL -> R.string.experience_minimal }),
                            maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis) },
                        modifier = Modifier.weight(1f).testTag("Motion_${level.name}"))
                }
            }
            Text(stringResource(R.string.experience_blur_title, (state.blurIntensity * 100).toInt()), style = MaterialTheme.typography.titleSmall)
            var slider by remember(state.blurIntensity) { mutableFloatStateOf(state.blurIntensity) }
            Slider(slider, { slider = it }, onValueChangeFinished = { dispatch(FinanceIntent.SetBlurIntensity(slider)) },
                valueRange = 0f..1f, steps = 3, modifier = Modifier.testTag("BlurIntensity"))
            Text(stringResource(R.string.experience_blur_desc), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun PrivacySettings(state: FinanceUiState, dispatch: (FinanceIntent) -> Unit) {
    val setLock = LocalAppLockAction.current
    Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PreferenceSwitch(stringResource(R.string.privacy_lock_title), stringResource(R.string.privacy_lock_desc),
                state.appLockEnabled, "AppLockSwitch", setLock)
            PreferenceSwitch(stringResource(R.string.privacy_hide_amounts), stringResource(R.string.privacy_hide_desc),
                state.hideAmounts, "HideAmountsSwitch") { dispatch(FinanceIntent.SetHideAmounts(it)) }
        }
    }
}

@Composable
private fun PreferenceSwitch(title: String, description: String, checked: Boolean, tag: String, change: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(description, style = MaterialTheme.typography.bodySmall) }
        AppSwitch(checked, change, modifier = Modifier.testTag(tag).semantics { contentDescription = title })
    }
}
