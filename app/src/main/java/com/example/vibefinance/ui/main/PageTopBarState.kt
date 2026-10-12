package com.example.vibefinance.ui.main

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Stable
internal class PageTopBarState(initiallyExpanded: Boolean) {
    var expanded by mutableStateOf(initiallyExpanded)
        internal set
    internal var scrollingUp by mutableStateOf(false)
        private set

    fun onScroll(deltaY: Float) {
        if (deltaY != 0f) scrollingUp = deltaY > 0f
    }
}

/** Follow the page's real leading position, including restored/programmatic scrolls.
 * Shrinking padding can make short lists fit. Don't expand again during the same downward
 * drag; restore the title when the gesture ends or the user returns toward the leading edge.
 */
@Composable
internal fun rememberPageTopBarState(listState: LazyListState): PageTopBarState {
    val threshold = with(LocalDensity.current) { 32.dp.toPx() }
    fun nearTop() = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < threshold
    val state = remember(listState) { PageTopBarState(nearTop()) }
    LaunchedEffect(listState, threshold) {
        snapshotFlow { Triple(nearTop(), listState.isScrollInProgress, state.scrollingUp) }
            .collect { (atLeadingEdge, scrolling, upward) ->
                if (!atLeadingEdge) state.expanded = false
                else if (!scrolling || upward) state.expanded = true
            }
    }
    return state
}
