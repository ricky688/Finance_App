package com.example.vibefinance.util

import android.Manifest
import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationManager
import org.robolectric.RuntimeEnvironment
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

// Settings path observed with ARTEMIS hierarchy + ADB on Waydroid before these regressions.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28, 34])
class GnssLocationManagerTest {
    private lateinit var app: Application
    private lateinit var manager: LocationManager

    @Before fun prepare() {
        app = RuntimeEnvironment.getApplication()
        manager = app.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        GnssLocationManager.setMockLocation(22.28, 114.15)
        shadowOf(app).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    @Test fun freshnessIncludesBoundaryButRejectsExpiredUnknownAndFutureTimes() {
        val now = 1_000_000L
        assertTrue(GnssLocationManager.isFreshLocationTime(now - 180_000, now, 180_000))
        assertFalse(GnssLocationManager.isFreshLocationTime(now - 180_001, now, 180_000))
        assertFalse(GnssLocationManager.isFreshLocationTime(0, now, 180_000))
        assertFalse(GnssLocationManager.isFreshLocationTime(now + 1, now, 180_000))
    }

    @Test fun zeroAgeRequiresCurrentTimestamp_andNegativeAgeIsRejected() {
        assertTrue(GnssLocationManager.isFreshLocationTime(1000, 1000, 0))
        assertFalse(GnssLocationManager.isFreshLocationTime(999, 1000, 0))
        assertFalse(GnssLocationManager.isFreshLocationTime(1000, 1000, -1))
    }

    private fun cached(provider: String, ageMs: Long, latitude: Double) {
        shadowOf(manager).setLastKnownLocation(provider, Location(provider).apply {
            this.latitude = latitude
            longitude = 114.15
            time = System.currentTimeMillis() - ageMs
        })
    }

    @Test fun staleCacheDoesNotBecomeCurrentLocation() {
        cached(LocationManager.GPS_PROVIDER, 600_000, 22.31)
        assertNull(GnssLocationManager.getInstantLocation(app))
    }

    @Test fun newestValidProviderWins() {
        cached(LocationManager.GPS_PROVIDER, 10_000, 22.31)
        cached(LocationManager.NETWORK_PROVIDER, 120_000, 22.28)
        assertEquals(22.31, GnssLocationManager.getInstantLocation(app)!!.latitude, 0.00001)
    }

    @Test fun deniedPermissionReturnsNoLocationWithoutStartingProviders() = runBlocking {
        shadowOf(app).denyPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        cached(LocationManager.GPS_PROVIDER, 1000, 22.31)
        assertNull(GnssLocationManager.getInstantLocation(app))
        assertNull(GnssLocationManager.resolveFreshLocation(app))
    }
}
