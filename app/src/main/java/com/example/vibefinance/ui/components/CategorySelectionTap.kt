package com.example.vibefinance.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics

/** Category filtering is an explicit short tap, never a drag-to-scrub side effect.
 * Moves remain unconsumed so the containing history list can scroll normally.
 */
@Composable
internal fun Modifier.categorySelectionTap(enabled: Boolean, onSelect: () -> Unit): Modifier {
    val currentSelect = rememberUpdatedState(onSelect)
    return semantics(mergeDescendants = true) {
        role = Role.Button
        if (!enabled) disabled()
        onClick {
            if (enabled) currentSelect.value()
            enabled
        }
    }.pointerInput(enabled) {
        if (!enabled) return@pointerInput
        awaitEachGesture {
            val down = awaitFirstDown()
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Main)
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                val outside = change.position.x < 0f || change.position.x >= size.width ||
                    change.position.y < 0f || change.position.y >= size.height
                if (change.isConsumed || outside ||
                    (change.position - down.position).getDistance() > viewConfiguration.touchSlop ||
                    change.uptimeMillis - down.uptimeMillis >= viewConfiguration.longPressTimeoutMillis ||
                    event.changes.any { it.id != down.id && it.pressed }
                ) break
                // A parent may consume the movement during the remainder of the Main pass.
                val final = awaitPointerEvent(PointerEventPass.Final)
                if (final.changes.any { it.isConsumed }) break
                if (!change.pressed) {
                    change.consume()
                    currentSelect.value()
                    break
                }
            }
        }
    }
}
