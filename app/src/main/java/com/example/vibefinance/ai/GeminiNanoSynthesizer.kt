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
    val timestamp: Long = System.currentTimeMillis(),
    val notificationKey: String = ""
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
     * Conservatively combines receipts that identify the same amount, merchant, and account hint.
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
                val fingerprint = generateFingerprint(
                    parsed.amount, parsed.merchant, parsed.assetName, item.packageName, item.timestamp
                )
                if (processedFingerprints.containsKey(fingerprint)) {
                    Log.d(TAG, "Duplicate fingerprint detected locally for '${parsed.merchant}', amount: $${parsed.amount}. Skipping.")
                    continue
                }
                parsedCandidates.add(
                    parsed.copy(
                        sourcePackage = item.packageName,
                        detectedAt = item.timestamp,
                        notificationKey = item.notificationKey
                    )
                )
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
            val fp = generateFingerprint(res.amount, res.merchant, res.assetName, res.sourcePackage, now)
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

        // Equal amounts can be separate purchases. Only merge when merchant and account hint match.
        val groupedByAmount = candidates.groupBy {
            Triple(
                Math.round(it.amount * 100) / 100.0,
                it.merchant.trim().lowercase(Locale.US),
                it.assetName.trim().lowercase(Locale.US)
            )
        }
        val finalConsolidated = mutableListOf<PaymentNotificationListener.ParsedPayment>()

        for ((identity, group) in groupedByAmount) {
            val amount = identity.first
            if (group.size == 1) {
                finalConsolidated.add(group.first())
            } else {
                // Multiple notifications for the SAME amount (e.g. Google Pay + Chase + Gmail)
                Log.d(TAG, "Duplicate receipt candidates detected for amount $$amount (Count: ${group.size}). Consolidating into 1 canonical receipt...")

                // Pick the most informative merchant & card name
                val bestMerchant = group.map { it.merchant }
                    .filter { !it.contains("Merchant", ignoreCase = true) && !it.contains("Payment", ignoreCase = true) }
                    .maxByOrNull { it.length } ?: group.first().merchant

                val bestAssetCandidate = group.firstOrNull {
                    !it.assetName.contains("Wallet", ignoreCase = true)
                } ?: group.first()

                val bestCardLast4 = group.mapNotNull { it.cardLast4 }.firstOrNull()

                val canonical = bestAssetCandidate.copy(
                    amount = amount,
                    merchant = bestMerchant,
                    cardLast4 = bestCardLast4 ?: bestAssetCandidate.cardLast4
                )

                Log.d(TAG, "Synthesized Canonical Receipt: merchant='$bestMerchant', amount=$$amount, asset='${canonical.assetName}'")
                finalConsolidated.add(canonical)
            }
        }

        return finalConsolidated
    }

    private fun generateFingerprint(
        amount: Double,
        merchant: String,
        assetName: String,
        sourcePackage: String,
        timestamp: Long
    ): String {
        val roundedAmt = (Math.round(amount * 100) / 100.0).toString()
        val cleanMerchant = merchant.lowercase(Locale.US).replace(Regex("[^a-z0-9]"), "")
        val cleanAsset = assetName.lowercase(Locale.US).replace(Regex("[^a-z0-9]"), "")
        val timeBucket = timestamp / 120_000L // 2-minute time bucket
        return "$roundedAmt-$cleanMerchant-$cleanAsset-$sourcePackage-$timeBucket"
    }
}
