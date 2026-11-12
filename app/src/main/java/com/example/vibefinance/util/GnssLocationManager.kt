package com.example.vibefinance.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*
import kotlin.coroutines.resume
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

data class GnssPoint(
    val latitude: Double,
    val longitude: Double,
    val isMock: Boolean = false,
    val locationName: String = "Current Location",
    val timestamp: Long = System.currentTimeMillis(),
    val accuracyMeters: Float? = null
)

data class GnssPreset(
    val id: String,
    val title: String,
    val latitude: Double,
    val longitude: Double
)

object GnssLocationManager {

    val PRESETS = listOf(
        GnssPreset("central", "中環 Central", 22.2819, 114.1582),
        GnssPreset("causeway_bay", "銅鑼灣 Causeway Bay", 22.2800, 114.1850),
        GnssPreset("tst", "尖沙咀 Tsim Sha Tsui", 22.2988, 114.1722),
        GnssPreset("mongkok", "旺角 Mong Kok", 22.3193, 114.1694),
        GnssPreset("kwun_tong", "觀塘 Kwun Tong", 22.3128, 114.2250),
        GnssPreset("sha_tin", "沙田 Sha Tin", 22.3814, 114.1878),
        GnssPreset("tsuen_wan", "荃灣 Tsuen Wan", 22.3713, 114.1141),
        GnssPreset("airport", "赤鱲角機場 HK Airport", 22.3080, 113.9185)
    )

    private val _currentLocation = MutableStateFlow(
        GnssPoint(PRESETS[0].latitude, PRESETS[0].longitude, isMock = true, locationName = PRESETS[0].title)
    )
    val currentLocation: StateFlow<GnssPoint> = _currentLocation.asStateFlow()

    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null

    private val breadcrumbs = java.util.concurrent.ConcurrentLinkedDeque<com.example.vibefinance.transit.TimestampedBreadcrumb>()

    fun recordBreadcrumb(context: Context, latitude: Double, longitude: Double, timestamp: Long = System.currentTimeMillis()) {
        try {
            val nearest = com.example.vibefinance.transit.MtrStationRegistry.findNearestStation(context, latitude, longitude, 800.0)
            val crumb = com.example.vibefinance.transit.TimestampedBreadcrumb(
                timestamp = timestamp,
                latitude = latitude,
                longitude = longitude,
                stationId = nearest?.first?.id,
                stationName = nearest?.first?.nameZh
            )
            breadcrumbs.addLast(crumb)
            while (breadcrumbs.size > 50) {
                breadcrumbs.pollFirst()
            }
        } catch (_: Throwable) {}
    }

    fun getRecentBreadcrumbs(): List<com.example.vibefinance.transit.TimestampedBreadcrumb> {
        return breadcrumbs.toList()
    }

    /**
     * Calculates geodesic distance between two GNSS coordinates using the Haversine formula.
     * @return Distance in meters.
     */
    fun calculateHaversineDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadiusMeters = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusMeters * c
    }

    /**
     * Formats distance into human readable string (e.g. "350 m" or "2.4 km")
     */
    fun formatDistance(meters: Double): String {
        return if (meters < 1000) {
            "${meters.roundToInt()} m"
        } else {
            String.format(java.util.Locale.US, "%.1f km", meters / 1000.0)
        }
    }

    /**
     * Sets a manual mock location (e.g. from preset or custom pin drop)
     */
    fun setMockLocation(latitude: Double, longitude: Double, name: String = "Custom Pin Location") {
        stopGnssUpdates()
        _currentLocation.value = GnssPoint(latitude, longitude, isMock = true, locationName = name)
    }

    fun selectPreset(preset: GnssPreset) {
        setMockLocation(preset.latitude, preset.longitude, preset.title)
    }

    /**
     * Attempts to start real GNSS location updates if permissions are granted.
     */
    @SuppressLint("MissingPermission")
    fun startGnssUpdates(context: Context) {
        try {
            val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasFine && !hasCoarse) return

            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val lm = locationManager ?: return

            if (locationListener == null) {
                locationListener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        recordBreadcrumb(context, location.latitude, location.longitude, if (location.time > 0) location.time else System.currentTimeMillis())
                        _currentLocation.value = GnssPoint(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            isMock = false,
                            locationName = String.format(java.util.Locale.US, "%.5f, %.5f", location.latitude, location.longitude),
                            timestamp = location.time,
                            accuracyMeters = location.accuracy.takeIf { location.hasAccuracy() }
                        )
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }
            }

            val listener = locationListener ?: return

            val candidateProviders = mutableListOf<String>()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                try {
                    if (lm.isProviderEnabled(LocationManager.FUSED_PROVIDER)) {
                        candidateProviders.add(LocationManager.FUSED_PROVIDER)
                    }
                } catch (_: Throwable) {}
            }
            try {
                if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    candidateProviders.add(LocationManager.NETWORK_PROVIDER)
                }
            } catch (_: Throwable) {}
            try {
                if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    candidateProviders.add(LocationManager.GPS_PROVIDER)
                }
            } catch (_: Throwable) {}

            for (p in candidateProviders) {
                val lastLoc = try { lm.getLastKnownLocation(p) } catch (_: Throwable) { null }
                if (lastLoc != null && _currentLocation.value.isMock) {
                    _currentLocation.value = GnssPoint(
                        latitude = lastLoc.latitude,
                        longitude = lastLoc.longitude,
                        isMock = false,
                        locationName = String.format(java.util.Locale.US, "%.5f, %.5f", lastLoc.latitude, lastLoc.longitude),
                        timestamp = lastLoc.time,
                        accuracyMeters = lastLoc.accuracy.takeIf { lastLoc.hasAccuracy() }
                    )
                }
                try {
                    lm.requestLocationUpdates(p, 5000L, 5f, listener)
                } catch (_: Throwable) {}
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopGnssUpdates() {
        try {
            locationListener?.let { listener ->
                locationManager?.removeUpdates(listener)
            }
            locationListener = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Resolves the best available device location immediately (cached or last known location).
     * Non-blocking, zero wait, battery-friendly.
     * @param maxAgeMs Maximum age in milliseconds for the location to be considered fresh. Defaults to 3 minutes (180,000ms).
     */
    @SuppressLint("MissingPermission")
    fun getInstantLocation(context: Context, maxAgeMs: Long = 180_000L): GnssPoint? {
        return try {
            val live = _currentLocation.value
            val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasFine && !hasCoarse) return null
            if (!live.isMock && isFreshLocationTime(live.timestamp, System.currentTimeMillis(), maxAgeMs)) {
                return live
            }

            val lm = (locationManager ?: context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager) ?: return null
            val providers = LinkedHashSet<String>().apply {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    add(LocationManager.FUSED_PROVIDER)
                }
                add("fused")
                add(LocationManager.GPS_PROVIDER)
                add(LocationManager.NETWORK_PROVIDER)
                add(LocationManager.PASSIVE_PROVIDER)
                try {
                    addAll(lm.getProviders(true))
                } catch (_: Throwable) {}
            }
            var bestLoc: Location? = null
            val now = System.currentTimeMillis()
            for (p in providers) {
                val loc = try {
                    lm.getLastKnownLocation(p)
                } catch (_: Throwable) { null } ?: continue
                if (!isFreshLocationTime(loc.time, now, maxAgeMs)) continue
                if (bestLoc == null || loc.time > bestLoc.time) {
                    bestLoc = loc
                }
            }
            if (bestLoc != null) {
                GnssPoint(
                    latitude = bestLoc.latitude,
                    longitude = bestLoc.longitude,
                    isMock = false,
                    locationName = String.format(java.util.Locale.US, "%.5f, %.5f", bestLoc.latitude, bestLoc.longitude),
                    timestamp = if (bestLoc.time > 0) bestLoc.time else now,
                    accuracyMeters = bestLoc.accuracy.takeIf { bestLoc.hasAccuracy() }
                )
            } else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Resolves a fresh location fix asynchronously.
     * Uses cancellable AndroidX current-location requests, falling back to recent cached location.
     * @param maxAgeMs Maximum acceptable age for location cache fallback. Defaults to 3 minutes.
     */
    @SuppressLint("MissingPermission")
    suspend fun resolveFreshLocation(context: Context, maxAgeMs: Long = 180_000L, recordHistory: Boolean = true, resolveName: Boolean = true): GnssPoint? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (!hasFine && !hasCoarse) return@withContext null

        val lm = (locationManager ?: context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager)
            ?: return@withContext getInstantLocation(context, maxAgeMs)

        // Query providers together: one unavailable provider must not delay GPS by another 5 seconds.
        val targetLoc = coroutineScope {
            listOf("fused", LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
                .distinct().filter { provider ->
                    try { lm.isProviderEnabled(provider) } catch (_: Exception) { false }
                }.map { provider ->
                    async {
                        withTimeoutOrNull(5_000L) {
                            suspendCancellableCoroutine<Location?> { continuation ->
                                val signal = android.os.CancellationSignal()
                                continuation.invokeOnCancellation { signal.cancel() }
                                try {
                                    androidx.core.location.LocationManagerCompat.getCurrentLocation(
                                        lm, provider, signal,
                                        androidx.core.content.ContextCompat.getMainExecutor(context)
                                    ) { location ->
                                        if (continuation.isActive) continuation.resume(location)
                                    }
                                } catch (e: Exception) {
                                    signal.cancel()
                                    android.util.Log.w("VibeFinanceLocation", "Location provider unavailable: $provider", e)
                                    if (continuation.isActive) continuation.resume(null)
                                }
                            }
                        }
                    }
                }.awaitAll().filterNotNull()
                .filter { isFreshLocationTime(it.time, System.currentTimeMillis(), maxAgeMs) }
                .sortedWith(compareByDescending<Location> { it.time }.thenBy { it.accuracy })
                .firstOrNull()
        }

        if (targetLoc != null) {
            val locTime = if (targetLoc.time > 0) targetLoc.time else System.currentTimeMillis()
            if (recordHistory) recordBreadcrumb(context, targetLoc.latitude, targetLoc.longitude, locTime)
            val addressText = if (resolveName) resolveAddress(context, targetLoc.latitude, targetLoc.longitude) else null
            val point = GnssPoint(
                latitude = targetLoc.latitude,
                longitude = targetLoc.longitude,
                isMock = false,
                locationName = addressText ?: String.format(java.util.Locale.US, "%.5f, %.5f", targetLoc.latitude, targetLoc.longitude),
                timestamp = locTime,
                accuracyMeters = targetLoc.accuracy.takeIf { targetLoc.hasAccuracy() }
            )
            _currentLocation.value = point
            point
        } else {
            // If genuinely no fresh location is available, return null rather than ancient stale coordinates
            getInstantLocation(context, maxAgeMs)
        }
    }

    internal fun isFreshLocationTime(timestamp: Long, now: Long, maxAgeMs: Long): Boolean =
        timestamp > 0 && timestamp <= now && maxAgeMs >= 0 && now - timestamp <= maxAgeMs

    /**
     * Resolves readable address / street name from coordinates using on-device Geocoder.
     * Completely free, no API keys needed.
     */
    fun resolveAddress(context: Context, latitude: Double, longitude: Double): String? {
        return try {
            if (android.location.Geocoder.isPresent()) {
                val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                @Suppress("DEPRECATION")
                val list = geocoder.getFromLocation(latitude, longitude, 1)
                val addr = list?.firstOrNull() ?: return null
                when {
                    !addr.thoroughfare.isNullOrBlank() && !addr.subThoroughfare.isNullOrBlank() ->
                        "${addr.thoroughfare} ${addr.subThoroughfare}".trim()
                    !addr.thoroughfare.isNullOrBlank() -> addr.thoroughfare
                    !addr.subLocality.isNullOrBlank() -> addr.subLocality
                    !addr.locality.isNullOrBlank() -> addr.locality
                    else -> addr.getAddressLine(0)
                }
            } else null
        } catch (_: Exception) {
            null
        }
    }
}
