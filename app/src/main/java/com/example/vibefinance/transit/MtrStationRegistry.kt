package com.example.vibefinance.transit

import android.content.Context
import com.example.vibefinance.util.GnssLocationManager
import org.json.JSONArray

object MtrStationRegistry {

    private var cachedStations: List<MtrStation>? = null
    private val stationsById = mutableMapOf<Int, MtrStation>()
    private val stationsByName = mutableMapOf<String, MtrStation>()

    @Synchronized
    fun getAllStations(context: Context): List<MtrStation> {
        cachedStations?.let { return it }

        val list = mutableListOf<MtrStation>()
        try {
            val jsonString = context.assets.open("mtr/mtr_stations.json").bufferedReader().use { it.readText() }
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.getInt("id")
                val code = obj.optString("code", "")
                val nameZh = obj.optString("nameZh", "")
                val nameEn = obj.optString("nameEn", "")
                val linesArray = obj.optJSONArray("lines")
                val lines = mutableListOf<String>()
                if (linesArray != null) {
                    for (j in 0 until linesArray.length()) {
                        lines.add(linesArray.getString(j))
                    }
                }
                val lat = obj.optDouble("lat", 0.0)
                val lon = obj.optDouble("lon", 0.0)

                val station = MtrStation(
                    id = id,
                    code = code,
                    nameZh = nameZh,
                    nameEn = nameEn,
                    lines = lines,
                    latitude = lat,
                    longitude = lon
                )
                list.add(station)
                stationsById[id] = station

                val normZh = nameZh.removeSuffix("站").trim()
                stationsByName[normZh.lowercase()] = station
                stationsByName["${normZh}站".lowercase()] = station
                stationsByName[nameEn.lowercase()] = station
                stationsByName["${nameEn.lowercase()} station"] = station
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        cachedStations = list
        return list
    }

    fun getStationById(context: Context, id: Int): MtrStation? {
        getAllStations(context)
        return stationsById[id]
    }

    fun getStationByName(context: Context, name: String): MtrStation? {
        getAllStations(context)
        val query = name.trim().lowercase()
        stationsByName[query]?.let { return it }

        val stripped = query.removeSuffix("站").removeSuffix(" station").trim()
        stationsByName[stripped]?.let { return it }

        // Partial match
        return cachedStations?.firstOrNull {
            query.contains(it.nameZh.lowercase()) ||
            it.nameZh.lowercase().contains(stripped) ||
            query.contains(it.nameEn.lowercase())
        }
    }

    /**
     * Finds the nearest MTR station to a given coordinate within [maxRadiusMeters].
     * Returns Pair(Station, DistanceInMeters) or null.
     */
    fun findNearestStation(
        context: Context,
        latitude: Double,
        longitude: Double,
        maxRadiusMeters: Double = 800.0
    ): Pair<MtrStation, Double>? {
        val stations = getAllStations(context)
        var bestStation: MtrStation? = null
        var minDistance = Double.MAX_VALUE

        for (station in stations) {
            if (station.latitude == 0.0 && station.longitude == 0.0) continue
            val dist = GnssLocationManager.calculateHaversineDistance(
                latitude, longitude,
                station.latitude, station.longitude
            )
            if (dist < minDistance && dist <= maxRadiusMeters) {
                minDistance = dist
                bestStation = station
            }
        }

        return bestStation?.let { Pair(it, minDistance) }
    }
}
