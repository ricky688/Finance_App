package com.example.vibefinance.wear

import android.content.Context
import com.example.vibefinance.wear.contract.WearBudgetSnapshot
import com.google.android.gms.wearable.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

internal class WearBudgetTransport(private val context: Context) {
    private suspend fun phones(filter: Int) = Wearable.getCapabilityClient(context)
        .getCapability(WearBudgetSnapshot.PHONE_CAPABILITY, filter).await().nodes.map { it.id }.toSet()

    suspend fun receive(item: DataItem) {
        if (item.uri.path != WearBudgetSnapshot.DATA_PATH) return
        val source = item.uri.host ?: return
        val payload = DataMapItem.fromDataItem(item).dataMap.getByteArray(WearBudgetSnapshot.PAYLOAD_KEY) ?: return
        if (BudgetCache(context).accept(source, payload, phones(CapabilityClient.FILTER_ALL))) {
            androidx.wear.tiles.TileService.getUpdater(context).requestUpdate(BudgetTileService::class.java)
        }
    }

    suspend fun refresh() = withTimeout(12_000) {
        val phones = phones(CapabilityClient.FILTER_REACHABLE)
        check(phones.size == 1)
        val revision = BudgetCache(context).read().snapshot?.revision ?: 0
        Wearable.getMessageClient(context).sendMessage(phones.single(), WearBudgetSnapshot.REFRESH_PATH, byteArrayOf()).await()
        while (true) {
            val items = Wearable.getDataClient(context).dataItems.await()
            try { for (item in items) receive(item) } finally { items.release() }
            if ((BudgetCache(context).read().snapshot?.revision ?: 0) > revision) break
            delay(350)
        }
    }
}

class BudgetListenerService : WearableListenerService() {
    override fun onDataChanged(events: DataEventBuffer) {
        // ListenerService dispatches on its worker thread; finish before its buffer is released.
        runBlocking(Dispatchers.IO) {
            try {
                withTimeout(10_000) {
                    for (event in events) if (event.type == DataEvent.TYPE_CHANGED) {
                        WearBudgetTransport(this@BudgetListenerService).receive(event.dataItem)
                    }
                }
            } catch (_: Exception) {
                android.util.Log.w("WearBudget", "Budget update rejected or unavailable")
            }
        }
    }
}
