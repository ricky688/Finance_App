package com.example.vibefinance.ui.main

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Shape-only tokens: row dimensions and touch targets are independent of corner changes. */
@Immutable
data class ConnectedButtonCorners(
    val selected: Dp,
    val outer: Dp,
    val inner: Dp,
    val pressed: Dp
) {
    companion object {
        val Standard = ConnectedButtonCorners(20.dp, 24.dp, 8.dp, 6.dp)
        // Keep the pressed radius below half a natural-height chip, so its outline still morphs.
        val Entry = ConnectedButtonCorners(32.dp, 32.dp, 20.dp, 14.dp)
    }
}
