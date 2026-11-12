package com.example.vibefinance.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable
import com.example.vibefinance.R

@Serializable private data object RecordList : NavKey
@Serializable private data class RecordDetail(val id: Long) : NavKey

/** The native SceneStrategy owns both single-pane navigation and wide list-detail motion. */
@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalLayoutApi::class)
@Composable
internal fun AdaptiveRecordScene(
    selectedId: Long?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    listContent: @Composable () -> Unit,
    hasActiveDialog: Boolean = false,
    detailContent: @Composable (Long) -> Unit
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val view = androidx.compose.ui.platform.LocalView.current
        val adaptiveInfo = currentWindowAdaptiveInfo()
        val verticalSeparatingHinge = adaptiveInfo.windowPosture.hingeList.firstOrNull { it.isSeparating && it.isVertical }
        val canShowBoth = verticalSeparatingHinge != null || maxWidth >= 724.dp

        val directive = calculatePaneScaffoldDirective(adaptiveInfo).copy(
            maxHorizontalPartitions = if (canShowBoth) 2 else 1,
            maxVerticalPartitions = 1,
            horizontalPartitionSpacerSize = if (verticalSeparatingHinge != null) 0.dp else 24.dp,
            defaultPanePreferredWidth = if (canShowBoth && maxWidth < 800.dp) 340.dp else 400.dp
        )
        val strategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)
        val stack = listOfNotNull<NavKey>(RecordList, selectedId?.let(::RecordDetail))

        val isImeVisible = WindowInsets.isImeVisible
        val backEnabled = !canShowBoth && (selectedId != null) && !hasActiveDialog && !isImeVisible
        BackHandler(enabled = backEnabled) {
            onClose()
        }

        NavDisplay(backStack = stack, onBack = onClose, sceneStrategy = strategy,
            entryProvider = entryProvider {
                entry<RecordList>(metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = {
                    Box(Modifier.fillMaxSize().testTag("InspectionPlaceholder"), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text(stringResource(R.string.adaptive_choose_record))
                    }
                })) { listContent() }
                entry<RecordDetail>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
                    Box(Modifier.fillMaxSize().testTag("InspectionDetail")) {
                        detailContent(key.id)
                        if (!canShowBoth) SmallFloatingActionButton(onClick = { com.example.vibefinance.util.VibeHaptics.selection(view); onClose() },
                            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomStart)
                                .padding(16.dp).navigationBarsPadding().testTag("InspectionBack")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.adaptive_back_to_list))
                        }
                    }
                }
            })
    }
}
