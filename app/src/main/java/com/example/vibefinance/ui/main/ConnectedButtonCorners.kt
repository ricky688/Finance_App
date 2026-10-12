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
        val Entry = ConnectedButtonCorners(28.dp, 28.dp, 16.dp, 12.dp)
    }
}
