package com.example.vibefinance.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import com.example.vibefinance.ui.common.ContentEntrance
import com.example.vibefinance.ui.common.ContentEntranceState

internal typealias DailyCardEntranceState = ContentEntranceState

@Composable
internal fun DailyCardEntrance(
    id: String,
    state: DailyCardEntranceState,
    viewport: Rect,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    content: @Composable () -> Unit,
) = ContentEntrance(id, state, viewport, modifier, delayMillis, "DailyArrival", content)
