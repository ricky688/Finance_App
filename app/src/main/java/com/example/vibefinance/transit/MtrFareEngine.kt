package com.example.vibefinance.transit

import android.content.Context
import com.example.vibefinance.util.GnssLocationManager
import org.json.JSONObject
import kotlin.math.abs

object MtrFareEngine {

    // Key: (srcId shl 16) or destId -> FloatArray(4) [adt, std, chd, joy]
    private var fareTable: MutableMap<Long, FloatArray>? = null

    @Synchronized
    private fun loadFareTable(context: Context) {
        if (fareTable != null) return
        val map = HashMap<Long, FloatArray>(9500)
        try {
            val jsonString = context.assets.open("mtr/mtr_lines_fares.json").bufferedReader().use { it.readText() }
            val root = JSONObject(jsonString)
            val keys = root.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val parts = key.split('_')
                if (parts.size == 2) {
                    val src = parts[0].toIntOrNull() ?: continue
                    val dst = parts[1].toIntOrNull() ?: continue
                    val arr = root.getJSONArray(key)
                    val floats = FloatArray(4) { idx ->
                        arr.optDouble(idx, 0.0).toFloat()
                    }
                    val packedKey = (src.toLong() shl 16) or (dst.toLong() and 0xFFFFL)
                    map[packedKey] = floats
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        fareTable = map
    }

    private fun packKey(srcId: Int, dstId: Int): Long = (srcId.toLong() shl 16) or (dstId.toLong() and 0xFFFFL)

    /**
     * Gets exact statutory fare between stations.
     */
    fun getFare(
        context: Context,
        srcStationId: Int,
        destStationId: Int,
        passengerType: MtrPassengerType
    ): Double? {
        loadFareTable(context)
        val table = fareTable ?: return null
        val values = table[packKey(srcStationId, destStationId)] ?: return null
        val fare = when (passengerType) {
            MtrPassengerType.ADULT -> values[0].toDouble()
            MtrPassengerType.STUDENT -> values[1].toDouble()
            MtrPassengerType.CHILD -> values[2].toDouble()
            MtrPassengerType.JOYYOU -> values[3].toDouble()
        }
        return if (fare >= 0) fare else null
    }

    /**
     * Finds all candidate origin stations whose statutory fare to [destStationId] matches [fareAmount].
     */
    fun deduceCandidateOrigins(
        context: Context,
        destStationId: Int,
        fareAmount: Double,
        configuredPassengerType: MtrPassengerType? = null
    ): List<Pair<MtrStation, MtrPassengerType>> {
        loadFareTable(context)
        val allStations = MtrStationRegistry.getAllStations(context)
        val candidates = mutableListOf<Pair<MtrStation, MtrPassengerType>>()

        val preferredTypes = if (configuredPassengerType != null) {
            listOf(configuredPassengerType)
        } else {
            // Adaptive: If fare is exactly $2.00, prioritise JoyYou; otherwise test all
            if (abs(fareAmount - 2.00) < 0.01) {
                listOf(MtrPassengerType.JOYYOU, MtrPassengerType.ADULT)
            } else {
                listOf(MtrPassengerType.ADULT, MtrPassengerType.STUDENT, MtrPassengerType.CHILD)
            }
        }

        // 1. First pass: Test preferred passenger type(s)
        for (candidate in allStations) {
            if (candidate.id == destStationId) continue
            for (pType in preferredTypes) {
                val expected = getFare(context, candidate.id, destStationId, pType) ?: continue
                if (abs(expected - fareAmount) < 0.05) {
                    candidates.add(Pair(candidate, pType))
                    break // Avoid duplicates for the same station across types
                }
            }
        }

        // 2. Adaptive Fallback: If configured type yielded 0 candidates
        // (e.g. $12.60 at 兆康 has 0 Adult matches, but matches Student/Child from East Kowloon)
        if (candidates.isEmpty()) {
            val fallbackTypes = MtrPassengerType.entries.filter { !preferredTypes.contains(it) }
            for (candidate in allStations) {
                if (candidate.id == destStationId) continue
                for (pType in fallbackTypes) {
                    val expected = getFare(context, candidate.id, destStationId, pType) ?: continue
                    if (abs(expected - fareAmount) < 0.05) {
                        candidates.add(Pair(candidate, pType))
                        break
                    }
                }
            }
        }

        return candidates
    }

    /**
     * Complete Quad-Lock deduction engine:
     * Resolves origin station using Exit Station, Transaction Amount, Passenger Type, and Rolling Breadcrumbs.
     */
    fun deduceTransitRoute(
        context: Context,
        destStation: MtrStation,
        fareAmount: Double,
        passengerType: MtrPassengerType?,
        breadcrumbs: List<TimestampedBreadcrumb>,
        exitTimestamp: Long = System.currentTimeMillis()
    ): MtrTransitRoute? {
        val candidates = deduceCandidateOrigins(context, destStation.id, fareAmount, passengerType)
        if (candidates.isEmpty()) return null

        if (candidates.size == 1) {
            val (origin, type) = candidates.first()
            return MtrTransitRoute(
                originStation = origin,
                destinationStation = destStation,
                fare = fareAmount,
                passengerType = type,
                confidence = 1.0f
            )
        }

        val relevant = breadcrumbs.filter {
            exitTimestamp - it.timestamp in 300_000L..NfcTapHistory.RETENTION_MS &&
                it.accuracyMeters != null && it.accuracyMeters in 0.01f..100f &&
                it.latitude.isFinite() && it.latitude in -90.0..90.0 &&
                it.longitude.isFinite() && it.longitude in -180.0..180.0
        }
        if (relevant.isEmpty()) return null
        val ranked = candidates.map { candidate ->
            val nearest = relevant.minBy { crumb ->
                GnssLocationManager.calculateHaversineDistance(crumb.latitude, crumb.longitude, candidate.first.latitude, candidate.first.longitude)
            }
            val distance = GnssLocationManager.calculateHaversineDistance(nearest.latitude, nearest.longitude,
                candidate.first.latitude, candidate.first.longitude)
            Triple(candidate, distance, nearest.accuracyMeters!!.toDouble())
        }.sortedBy { it.second }
        val best = ranked.first()
        if (best.second > 500.0) return null
        val runnerUp = ranked.getOrNull(1)
        // A tie or overlapping accuracy bounds is uncertainty, never a station-order tie-break.
        if (runnerUp != null && runnerUp.second - best.second < 200.0 + best.third + runnerUp.third) return null
        val chosen = best.first
        val confidence = 0.95f

        return MtrTransitRoute(
            originStation = chosen.first,
            destinationStation = destStation,
            fare = fareAmount,
            passengerType = chosen.second,
            confidence = confidence
        )
    }
}
