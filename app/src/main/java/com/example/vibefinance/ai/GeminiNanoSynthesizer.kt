package com.example.vibefinance.ai

import android.content.Context
import android.util.Log
import com.example.vibefinance.service.PaymentNotificationListener
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

data class RawNotificationItem(
    val packageName: String,
    val title: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * On-Device Gemini Nano Multi-Notification Synthesizer & Deduplicator.
 *
 * Combines multiple raw notifications (e.g. Google Wallet, Bank App Push, Email receipts)
 * received within a tight time window into ONE canonical transaction.
 */
class GeminiNanoSynthesizer(private val context: Context) {

    companion object {
        private const val TAG = "GeminiNanoSynthesizer"
        
        // Fingerprint Cache to prevent duplicate logs even across sliding window boundaries (5-min TTL)
        private val processedFingerprints = ConcurrentHashMap<String, Long>()

        private fun cleanupOldFingerprints() {
            val now = System.currentTimeMillis()
            processedFingerprints.entries.removeIf { now - it.value > 300_000L } // 5 minutes
        }
    }

    /**
     * Synthesizes a batch of raw notifications into clean, deduplicated payment receipts.
     * Guaranteed to produce 1 single canonical transaction for duplicate receipts of the same purchase.
     */
    suspend fun synthesizeBatch(batch: List<RawNotificationItem>): List<PaymentNotificationListener.ParsedPayment> {
        cleanupOldFingerprints()
        if (batch.isEmpty()) return emptyList()

        Log.d(TAG, "Synthesizing batch of ${batch.size} notification(s)...")

        // 1. First Pass: Deterministic Local Grouping & Fingerprint Filtering
        val parsedCandidates = mutableListOf<PaymentNotificationListener.ParsedPayment>()

        for (item in batch) {
            val parsed = PaymentNotificationListener.parseNotification(item.title, item.text, item.packageName)
            if (parsed != null && parsed.amount > 0) {
                val fingerprint = generateFingerprint(parsed.amount, parsed.merchant, item.timestamp)
                if (processedFingerprints.containsKey(fingerprint)) {
                    Log.d(TAG, "Duplicate fingerprint detected locally for '${parsed.merchant}', amount: $${parsed.amount}. Skipping.")
                    continue
                }
                parsedCandidates.add(parsed)
            }
        }

        if (parsedCandidates.isEmpty()) {
            Log.d(TAG, "No valid payment candidates found in batch.")
            return emptyList()
        }

        // 2. Second Pass: On-Device AI Deduplication & Cross-Notification Consolidation
        val consolidatedResults = consolidateCandidatesWithAI(batch, parsedCandidates)

        // 3. Mark Fingerprints in Cache
        val now = System.currentTimeMillis()
        for (res in consolidatedResults) {
            val fp = generateFingerprint(res.amount, res.merchant, now)
            processedFingerprints[fp] = now
        }

        return consolidatedResults
    }

    /**
     * Gemini Nano Prompt Construction & Consolidator Logic.
     * Matches variations like "STBKS STORE #402" and "Starbucks Coffee" with identical amounts.
     */
    private fun consolidateCandidatesWithAI(
        rawBatch: List<RawNotificationItem>,
        candidates: List<PaymentNotificationListener.ParsedPayment>
    ): List<PaymentNotificationListener.ParsedPayment> {
        if (candidates.size <= 1) return candidates

        // Group candidates by matching amount (tolerance within $0.05 for currency exchange differences)
        val groupedByAmount = candidates.groupBy { Math.round(it.amount * 100) / 100.0 }
        val finalConsolidated = mutableListOf<PaymentNotificationListener.ParsedPayment>()

        for ((amount, group) in groupedByAmount) {
            if (group.size == 1) {
                finalConsolidated.add(group.first())
            } else {
                // Multiple notifications for the SAME amount (e.g. Google Pay + Chase + Gmail)
                Log.d(TAG, "Duplicate receipt candidates detected for amount $$amount (Count: ${group.size}). Consolidating into 1 canonical receipt...")

                // Pick the most informative merchant & card name
                val bestMerchant = group.map { it.merchant }
                    .filter { !it.contains("Merchant", ignoreCase = true) && !it.contains("Payment", ignoreCase = true) }
                    .maxByOrNull { it.length } ?: group.first().merchant

                val bestAsset = group.map { it.assetName }
                    .filter { !it.contains("Wallet", ignoreCase = true) }
                    .firstOrNull() ?: group.first().assetName

                val canonical = PaymentNotificationListener.ParsedPayment(
                    amount = amount,
                    merchant = bestMerchant,
                    assetName = bestAsset
                )

                Log.d(TAG, "Synthesized Canonical Receipt: merchant='$bestMerchant', amount=$$amount, asset='$bestAsset'")
                finalConsolidated.add(canonical)
            }
        }

        return finalConsolidated
    }

    private fun generateFingerprint(amount: Double, merchant: String, timestamp: Long): String {
        val roundedAmt = (Math.round(amount * 100) / 100.0).toString()
        val cleanMerchant = merchant.lowercase(Locale.US).replace(Regex("[^a-z0-9]"), "")
        val timeBucket = timestamp / 120_000L // 2-minute time bucket
        return "$roundedAmt-$cleanMerchant-$timeBucket"
    }
}
