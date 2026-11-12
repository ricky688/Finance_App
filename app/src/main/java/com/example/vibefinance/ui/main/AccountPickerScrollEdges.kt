package com.example.vibefinance.ui.main

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/** Sheet-only effects: record its bounded account viewport, leaving header/actions sharp. */
@Composable
internal fun AccountPickerScrollEdges(
    state: LazyGridState,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit,
) {
    val density = LocalDensity.current
    val edges by remember(state, density) { derivedStateOf {
        val info = state.layoutInfo
        val last = info.visibleItemsInfo.maxByOrNull { it.index }
        with(density) {
            val top = if (!state.canScrollBackward) 0.dp else if (state.firstVisibleItemIndex > 0) 16.dp
                else state.firstVisibleItemScrollOffset.toDp().coerceIn(0.dp, 16.dp)
            val bottom = if (!state.canScrollForward || last == null) 0.dp else if (last.index < info.totalItemsCount - 1) 16.dp
                else ((info.visibleItemsInfo.maxOf { it.offset.y + it.size.height }) -
                    (info.viewportEndOffset - info.afterContentPadding)).toDp().coerceIn(0.dp, 16.dp)
            top to bottom
        }
    } }
    val (top, bottom) = edges
    com.example.vibefinance.ui.common.ScrollEdgeEffects(top, bottom, modifier, "AccountPicker", content)
}
