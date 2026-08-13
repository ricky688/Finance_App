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

data class GnssPoint(
    val latitude: Double,
    val longitude: Double,
    val isMock: Boolean = false,
    val locationName: String = "Current Location"
)

data class GnssPreset(
    val id: String,
    val title: String,
    val latitude: Double,
    val longitude: Double
)

object GnssLocationManager {

    val PRESETS = listOf(
        GnssPreset("downtown", "Downtown City Center", 37.7749, -122.4194),
        GnssPreset("tech_hub", "Tech & Financial District", 37.7895, -122.4012),
        GnssPreset("shopping_plaza", "Grand Shopping Plaza", 37.7880, -122.4075),
        GnssPreset("university", "University Quarter", 37.7760, -122.4500),
        GnssPreset("suburbs", "Suburban Mall & Park", 37.7500, -122.4200)
    )

    private val _currentLocation = MutableStateFlow(
        GnssPoint(PRESETS[0].latitude, PRESETS[0].longitude, isMock = true, locationName = PRESETS[0].title)
    )
    val currentLocation: StateFlow<GnssPoint> = _currentLocation.asStateFlow()

    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null

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
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val lm = locationManager ?: return

            val hasGps = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val hasNetwork = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!hasGps && !hasNetwork) return

            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    _currentLocation.value = GnssPoint(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        isMock = false,
                        locationName = "Live GNSS Location"
                    )
                }

                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            val provider = if (hasGps) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
            
            // Fetch last known location first if available
            val lastLoc = lm.getLastKnownLocation(provider)
            if (lastLoc != null) {
                _currentLocation.value = GnssPoint(
                    latitude = lastLoc.latitude,
                    longitude = lastLoc.longitude,
                    isMock = false,
                    locationName = "Live GNSS Location"
                )
            }

            locationListener?.let { listener ->
                lm.requestLocationUpdates(provider, 3000L, 5f, listener)
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
}
