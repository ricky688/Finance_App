package com.example.vibefinance.wear

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.example.vibefinance.wear.contract.*
import com.google.android.gms.wearable.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

internal class WearBudgetTransport(private val context: Context) {
    private suspend fun phones(filter: Int) = Wearable.getCapabilityClient(context)
        .getCapability(WearBudgetSnapshot.PHONE_CAPABILITY, filter).await().nodes.map { it.id }.toSet()

    suspend fun receive(item: DataItem) {
        val path = item.uri.path ?: return
        val source = item.uri.host ?: return
        val allowed = phones(CapabilityClient.FILTER_ALL)
        var changed = false
        when (path) {
            WearBudgetSnapshot.DATA_PATH -> {
                val payload = DataMapItem.fromDataItem(item).dataMap.getByteArray(WearBudgetSnapshot.PAYLOAD_KEY) ?: return
                if (BudgetCache(context).accept(source, payload, allowed)) changed = true
            }
            WearCompanionSnapshot.DATA_PATH -> {
                val payload = DataMapItem.fromDataItem(item).dataMap.getByteArray(WearCompanionSnapshot.PAYLOAD_KEY) ?: return
                if (CompanionCache(context).accept(source, payload, allowed)) changed = true
            }
        }
        if (changed) {
            androidx.wear.tiles.TileService.getUpdater(context).requestUpdate(BudgetTileService::class.java)
            BudgetComplicationService.requestUpdate(context)
        }
    }

    suspend fun refresh() = withTimeout(12_000) {
        val reachable = phones(CapabilityClient.FILTER_REACHABLE)
        check(reachable.size == 1)
        val prevBudgetRev = BudgetCache(context).read().snapshot?.revision ?: 0L
        val prevCompRev = CompanionCache(context).read().snapshot?.revision ?: 0L
        val baseline = maxOf(prevBudgetRev, prevCompRev)

        Wearable.getMessageClient(context)
            .sendMessage(reachable.single(), WearBudgetSnapshot.REFRESH_PATH, byteArrayOf())
            .await()

        while (true) {
            val items = Wearable.getDataClient(context).dataItems.await()
            try {
                for (item in items) receive(item)
            } finally {
                items.release()
            }
            val newBudgetRev = BudgetCache(context).read().snapshot?.revision ?: 0L
            val newCompRev = CompanionCache(context).read().snapshot?.revision ?: 0L
            if (maxOf(newBudgetRev, newCompRev) > baseline) break
            delay(250)
        }
    }

    suspend fun sendCommand(command: WearCommand): WearCommandResult = withTimeout(10_000) {
        val reachable = phones(CapabilityClient.FILTER_REACHABLE)
        check(reachable.size == 1)
        val deferred = CompletableDeferred<WearCommandResult>()
        pendingReplies[command.requestId] = deferred
        try {
            Wearable.getMessageClient(context)
                .sendMessage(reachable.single(), WearCommand.COMMAND_PATH, command.encode())
                .await()
            val result = deferred.await()
            // Pull latest data items immediately so UI reflects updated budget/pending/recent lists.
            runCatching {
                val items = Wearable.getDataClient(context).dataItems.await()
                try {
                    for (item in items) receive(item)
                } finally {
                    items.release()
                }
            }
            result
        } finally {
            pendingReplies.remove(command.requestId)
        }
    }

    suspend fun flushQueuedCommands(): Int {
        val cache = CompanionCache(context)
        val queued = cache.queuedCommands()
        if (queued.isEmpty()) return 0
        var flushed = 0
        for (cmd in queued) {
            val res = runCatching { sendCommand(cmd) }.getOrNull() ?: break
            if (res.status == WearCommandStatus.SUCCESS ||
                res.status == WearCommandStatus.ALREADY_HANDLED ||
                res.status == WearCommandStatus.INVALID_REQUEST
            ) {
                cache.removeQueuedCommand(cmd.requestId)
                flushed++
            }
        }
        return flushed
    }

    suspend fun openOnPhone(target: String): Boolean {
        val uri = Uri.parse("vibefinance://open/$target")
        val remoteIntent = Intent(Intent.ACTION_VIEW, uri).addCategory(Intent.CATEGORY_BROWSABLE)
        val remoteOk = runCatching {
            withContext(Dispatchers.IO) {
                RemoteActivityHelper(context).startRemoteActivity(remoteIntent).get()
            }
            true
        }.getOrDefault(false)
        if (remoteOk) return true

        // Fallback via Wear message channel
        val fallbackCmd = WearCommand(
            requestId = UUID.randomUUID().toString(),
            issuedAt = System.currentTimeMillis(),
            action = WearCommandAction.OPEN_ON_PHONE,
            target = target
        )
        return runCatching {
            sendCommand(fallbackCmd).status == WearCommandStatus.SUCCESS
        }.getOrDefault(false)
    }

    companion object {
        private val pendingReplies = ConcurrentHashMap<String, CompletableDeferred<WearCommandResult>>()

        internal fun deliverResult(result: WearCommandResult) {
            pendingReplies.remove(result.requestId)?.complete(result)
        }
    }
}

class BudgetListenerService : WearableListenerService() {
    override fun onDataChanged(events: DataEventBuffer) {
        // ListenerService dispatches on its worker thread; finish before its buffer is released.
        runBlocking(Dispatchers.IO) {
            try {
                withTimeout(10_000) {
                    for (event in events) {
                        if (event.type == DataEvent.TYPE_CHANGED) {
                            WearBudgetTransport(this@BudgetListenerService).receive(event.dataItem)
                        }
                    }
                }
            } catch (_: Exception) {
                android.util.Log.w("WearBudget", "Budget update rejected or unavailable")
            }
        }
    }

    override fun onMessageReceived(event: MessageEvent) {
        if (event.path != WearCommandResult.RESULT_PATH) return
        val bytes = event.data
        if (bytes.isEmpty()) return
        runCatching {
            WearBudgetTransport.deliverResult(WearCommandResult.decode(bytes))
        }
    }
}
