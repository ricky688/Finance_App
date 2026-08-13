package com.example.vibefinance.service

import com.example.vibefinance.ai.RawNotificationItem
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Sliding Window Notification Batch Buffer.
 * Collects notifications received within a tight time window (e.g. 3-5 seconds)
 * into a single batch for on-device Gemini Nano synthesis & deduplication.
 */
class NotificationBatchBuffer(
    private val scope: CoroutineScope,
    private val windowMs: Long = 3000L, // 3-second debounce window
    private val onBatchReady: suspend (List<RawNotificationItem>) -> Unit
) {
    private val queue = ConcurrentLinkedQueue<RawNotificationItem>()
    private var debounceJob: Job? = null

    fun push(notification: RawNotificationItem) {
        queue.add(notification)

        // Reset debounce timer on every new incoming notification
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(windowMs)
            flush()
        }
    }

    suspend fun flush() {
        val batch = mutableListOf<RawNotificationItem>()
        while (queue.isNotEmpty()) {
            queue.poll()?.let { batch.add(it) }
        }
        if (batch.isNotEmpty()) {
            onBatchReady(batch)
        }
    }
}
