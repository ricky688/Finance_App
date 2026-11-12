package com.example.vibefinance.transit

import android.content.Context
import com.example.vibefinance.data.entity.TransactionEntity
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MtrTransitTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
    }

    @Test
    fun mtrStationRegistryLoadsStationsAccurately() {
        val stations = MtrStationRegistry.getAllStations(context)
        assertTrue("Should load 100 MTR stations", stations.size >= 90)

        // Find Siu Hong and Kowloon Bay
        val siuHong = MtrStationRegistry.getStationByName(context, "兆康")
        assertNotNull("Siu Hong should exist", siuHong)
        assertEquals("兆康", siuHong!!.nameZh)
        assertEquals("Siu Hong", siuHong.nameEn)
        assertTrue(siuHong.latitude > 22.0)
        assertTrue(siuHong.longitude > 113.0)

        val kowloonBay = MtrStationRegistry.getStationByName(context, "九龍灣")
        assertNotNull("Kowloon Bay should exist", kowloonBay)
        assertEquals("九龍灣", kowloonBay!!.nameZh)
        assertEquals("Kowloon Bay", kowloonBay.nameEn)

        // Test nearest station
        val nearest = MtrStationRegistry.findNearestStation(context, siuHong.latitude, siuHong.longitude, 500.0)
        assertNotNull(nearest)
        assertEquals(siuHong.id, nearest!!.first.id)
        assertTrue("Distance should be within 10 meters", nearest.second < 10.0)
    }

    @Test
    fun mtrFareEngineExtractsExactStatutoryFares() {
        val siuHong = MtrStationRegistry.getStationByName(context, "兆康")!!
        val kowloonBay = MtrStationRegistry.getStationByName(context, "九龍灣")!!

        // Adult fare from Siu Hong to Kowloon Bay is HK$24.90
        val adultFare = MtrFareEngine.getFare(context, siuHong.id, kowloonBay.id, MtrPassengerType.ADULT)
        assertNotNull(adultFare)
        assertEquals(24.90, adultFare!!, 0.01)

        // Student fare from Siu Hong to Kowloon Bay is HK$12.60
        val studentFare = MtrFareEngine.getFare(context, siuHong.id, kowloonBay.id, MtrPassengerType.STUDENT)
        assertNotNull(studentFare)
        assertEquals(12.60, studentFare!!, 0.01)

        // Child fare is HK$12.60
        val childFare = MtrFareEngine.getFare(context, siuHong.id, kowloonBay.id, MtrPassengerType.CHILD)
        assertNotNull(childFare)
        assertEquals(12.60, childFare!!, 0.01)

        // JoyYou fare is capped at HK$2.00
        val joyYouFare = MtrFareEngine.getFare(context, siuHong.id, kowloonBay.id, MtrPassengerType.JOYYOU)
        assertNotNull(joyYouFare)
        assertEquals(2.00, joyYouFare!!, 0.01)
    }

    @Test
    fun mtrFareEngineReverseDeducesOriginStation() {
        val siuHong = MtrStationRegistry.getStationByName(context, "兆康")!!
        val kowloonBay = MtrStationRegistry.getStationByName(context, "九龍灣")!!

        // Reverse search with exit station = Kowloon Bay, fare = 24.90, Adult
        val candidates = MtrFareEngine.deduceCandidateOrigins(context, kowloonBay.id, 24.90, MtrPassengerType.ADULT)
        assertTrue("Candidates should contain Siu Hong", candidates.any { it.first.id == siuHong.id })

        // Quad-Lock deduction with breadcrumb near Siu Hong recorded 30 minutes ago
        val now = System.currentTimeMillis()
        val breadcrumbs = listOf(
            TimestampedBreadcrumb(
                timestamp = now - 35 * 60 * 1000L,
                latitude = siuHong.latitude + 0.001,
                longitude = siuHong.longitude + 0.001,
                stationId = siuHong.id,
                stationName = siuHong.nameZh,
                accuracyMeters = 20f
            )
        )

        val route = MtrFareEngine.deduceTransitRoute(
            context = context,
            destStation = kowloonBay,
            fareAmount = 24.90,
            passengerType = MtrPassengerType.ADULT,
            breadcrumbs = breadcrumbs,
            exitTimestamp = now
        )

        assertNotNull("Route should be successfully deduced", route)
        assertEquals("Siu Hong should be the deduced origin", siuHong.id, route!!.originStation.id)
        assertEquals(kowloonBay.id, route.destinationStation.id)
        assertEquals("兆康 ➔ 九龍灣", route.routeDisplay)
    }

    @Test
    fun equalFareWithoutReliableEvidenceDoesNotChooseFirstStation() {
        val exit = MtrStationRegistry.getStationByName(context, "兆康")!!
        val candidates = MtrFareEngine.deduceCandidateOrigins(context, exit.id, 12.60, MtrPassengerType.STUDENT)
        assertTrue(candidates.any { it.first.nameZh == "九龍灣" })
        assertEquals("調景嶺", candidates.first().first.nameZh)
        assertNull(MtrFareEngine.deduceTransitRoute(context, exit, 12.60, MtrPassengerType.STUDENT, emptyList()))
    }

    @Test
    fun kowloonBayEvidenceResolvesTwelveSixtyButTiesAndPoorFixesStayUnknown() {
        val exit = MtrStationRegistry.getStationByName(context, "兆康")!!
        val origin = MtrStationRegistry.getStationByName(context, "九龍灣")!!
        val other = MtrStationRegistry.getStationByName(context, "調景嶺")!!
        val now = System.currentTimeMillis()
        fun crumb(station: MtrStation, accuracy: Float? = 20f) = TimestampedBreadcrumb(
            now - 35 * 60_000, station.latitude, station.longitude, accuracyMeters = accuracy)
        assertEquals(origin.id, MtrFareEngine.deduceTransitRoute(context, exit, 12.60,
            MtrPassengerType.STUDENT, listOf(crumb(origin)), now)!!.originStation.id)
        for (evidence in listOf(listOf(crumb(origin), crumb(other)), listOf(crumb(origin, 150f)),
            listOf(crumb(origin, null)), listOf(crumb(origin).copy(timestamp = now + 1000)),
            listOf(crumb(origin).copy(timestamp = now - 3 * 60 * 60_000)))) {
            assertNull(MtrFareEngine.deduceTransitRoute(context, exit, 12.60, MtrPassengerType.STUDENT, evidence, now))
        }
    }

    @Test
    fun mtrTransitSettingsPreservesUserPreferences() {
        MtrTransitSettings.setEnabled(context, true)
        assertTrue(MtrTransitSettings.isEnabled(context))

        MtrTransitSettings.setEnabled(context, false)
        assertFalse(MtrTransitSettings.isEnabled(context))

        MtrTransitSettings.setEnabled(context, true)

        MtrTransitSettings.setOctopusAccountId(context, "42")
        assertEquals("42", MtrTransitSettings.getOctopusAccountId(context))

        MtrTransitSettings.setOctopusAccountId(context, null)
        assertNull(MtrTransitSettings.getOctopusAccountId(context))

        MtrTransitSettings.setPassengerType(context, MtrPassengerType.STUDENT)
        assertEquals(MtrPassengerType.STUDENT, MtrTransitSettings.getPassengerType(context))

        MtrTransitSettings.setPassengerType(context, MtrPassengerType.ADULT)
        assertEquals(MtrPassengerType.ADULT, MtrTransitSettings.getPassengerType(context))
    }

    @Test
    fun transactionEntityPreservesStartingLocationFields() {
        val tx = TransactionEntity(
            id = 999L,
            amount = 24.90,
            category = "Transport",
            timestamp = 1700000000000L,
            accountId = 1L,
            description = "港鐵",
            latitude = 22.3233,
            longitude = 114.2140,
            locationName = "兆康 ➔ 九龍灣",
            startLatitude = 22.4124,
            startLongitude = 113.9786,
            startLocationName = "兆康"
        )

        assertEquals("兆康 ➔ 九龍灣", tx.locationName)
        assertEquals(22.4124, tx.startLatitude!!, 0.001)
        assertEquals(113.9786, tx.startLongitude!!, 0.001)
        assertEquals("兆康", tx.startLocationName)
    }
}
