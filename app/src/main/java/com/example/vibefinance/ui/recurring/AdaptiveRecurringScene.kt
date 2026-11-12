package com.example.vibefinance.ui.recurring

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

@Serializable private data object RecurringListPaneKey : NavKey
@Serializable private data class RecurringDetailPaneKey(val id: Long?) : NavKey

/**
 * Material 3 Canonical List-Detail Layout for Recurring & Subscription Management.
 * On wide/expanded screens (>= 724dp), renders list and detail/editor panes simultaneously.
 * On compact screens (< 724dp), seamlessly falls back to single-pane navigation with backstack.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalLayoutApi::class)
@Composable
internal fun AdaptiveRecurringScene(
    selectedId: Long?,
    isEditing: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    listContent: @Composable () -> Unit,
    hasActiveDialog: Boolean = false,
    placeholderContent: @Composable () -> Unit,
    detailContent: @Composable (Long?) -> Unit
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
        val isDetailActive = isEditing || selectedId != null
        val stack = listOfNotNull<NavKey>(
            RecurringListPaneKey,
            if (isDetailActive) RecurringDetailPaneKey(selectedId) else null
        )

        val isImeVisible = WindowInsets.isImeVisible
        val backEnabled = !canShowBoth && isDetailActive && !hasActiveDialog && !isImeVisible
        BackHandler(enabled = backEnabled) {
            onClose()
        }

        NavDisplay(
            backStack = stack,
            onBack = onClose,
            sceneStrategy = strategy,
            entryProvider = entryProvider {
                entry<RecurringListPaneKey>(
                    metadata = ListDetailSceneStrategy.listPane(
                        detailPlaceholder = {
                            Box(
                                Modifier.fillMaxSize().testTag("RecurringDetailPlaceholder"),
                                contentAlignment = androidx.compose.ui.Alignment.Center
                            ) {
                                placeholderContent()
                            }
                        }
                    )
                ) {
                    listContent()
                }
                entry<RecurringDetailPaneKey>(
                    metadata = ListDetailSceneStrategy.detailPane()
                ) { key ->
                    Box(Modifier.fillMaxSize().testTag("RecurringDetailPane")) {
                        detailContent(key.id)
                        if (!canShowBoth) {
                            SmallFloatingActionButton(
                                onClick = {
                                    com.example.vibefinance.util.VibeHaptics.selection(view)
                                    onClose()
                                },
                                modifier = Modifier
                                    .align(androidx.compose.ui.Alignment.BottomStart)
                                    .padding(16.dp)
                                    .navigationBarsPadding()
                                    .testTag("RecurringBackToList")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.adaptive_back_to_list))
                            }
                        }
                    }
                }
            }
        )
    }
}
