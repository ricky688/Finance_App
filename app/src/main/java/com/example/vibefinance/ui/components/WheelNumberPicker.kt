package com.example.vibefinance.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WheelNumberPicker(
    selectedValue: Int,
    onValueSelected: (Int) -> Unit,
    range: IntRange = 2..36,
    itemHeight: Dp = 64.dp,
    modifier: Modifier = Modifier
) {
    val items = remember(range) {
        listOf("") + range.map { "%02d".format(it) } + listOf("")
    }
    val lazyListState = rememberLazyListState()
    val density = LocalDensity.current
    val itemHeightPx = remember(itemHeight) { with(density) { itemHeight.toPx() } }

    // Scroll to initial value on mount or when selectedValue changes externally/layout completes
    LaunchedEffect(selectedValue, lazyListState.layoutInfo.visibleItemsInfo.isNotEmpty()) {
        val targetIndex = range.indexOf(selectedValue)
        if (targetIndex != -1 && lazyListState.layoutInfo.visibleItemsInfo.isNotEmpty()) {
            val currentIndex = lazyListState.firstVisibleItemIndex
            val currentOffset = lazyListState.firstVisibleItemScrollOffset
            if (currentIndex != targetIndex || currentOffset != 0) {
                lazyListState.scrollToItem(targetIndex, 0)
            }
        }
    }

    // Snapping logic when scrolling stops
    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (!lazyListState.isScrollInProgress) {
            val offset = lazyListState.firstVisibleItemScrollOffset
            val targetIndex = if (offset > itemHeightPx / 2) {
                lazyListState.firstVisibleItemIndex + 1
            } else {
                lazyListState.firstVisibleItemIndex
            }
            lazyListState.animateScrollToItem(targetIndex, 0)
        }
    }

    // Determine current selected index dynamically based on scroll offset
    val selectedIndex = remember(lazyListState.firstVisibleItemIndex, lazyListState.firstVisibleItemScrollOffset) {
        val offset = lazyListState.firstVisibleItemScrollOffset
        if (offset > itemHeightPx / 2) {
            lazyListState.firstVisibleItemIndex + 2
        } else {
            lazyListState.firstVisibleItemIndex + 1
        }
    }

    // Notify value selection when selectedIndex changes
    LaunchedEffect(selectedIndex) {
        val selectedItemText = items.getOrNull(selectedIndex)
        if (!selectedItemText.isNullOrEmpty()) {
            val selectedVal = selectedItemText.toIntOrNull()
            if (selectedVal != null && selectedVal != selectedValue) {
                onValueSelected(selectedVal)
            }
        }
    }

    val view = LocalView.current

    Box(
        modifier = modifier
            .height(itemHeight * 3)
            .width(100.dp)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent()
                        // Prevent vertical parent scroll containers from intercepting our drag events
                        view.parent?.requestDisallowInterceptTouchEvent(true)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Transparent borderless scrolling wheel to match Google Clock timer design

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(items) { index, item ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item,
                        style = if (isSelected) {
                            MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 38.sp
                            )
                        } else {
                            MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                                fontSize = 24.sp
                            )
                        }
                    )
                }
            }
        }
    }
}
