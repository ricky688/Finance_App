package com.example.vibefinance.transit

import com.example.vibefinance.util.GnssPoint
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28, 34])
class NfcTapHistoryTest {
    private val now = 10_000_000L
    private fun fix(at: Long = now, accuracy: Float = 20f) = GnssPoint(22.3235, 114.214,
        timestamp = at, accuracyMeters = accuracy)

    @Test fun observedRfBurstsAreOneContact_andNewContactsAreKept() {
        for (edges in listOf(listOf(0L, 687L, 689L, 706L), listOf(0L, 12L, 20L, 591L, 591L, 613L))) {
            val gate = NfcContactGate()
            val accepted = edges.mapIndexed { i, t -> gate.accept(i % 2 == 0, t) }
            assertEquals(1, accepted.count { it })
            assertTrue(accepted.first())
            assertTrue(gate.accept(true, 10_000))
            assertFalse(gate.accept(true, 11_000))
        }
    }
    @Test fun goodOriginSurvivesSerializationAndIsEligibleAfterFiveMinutes() {
        val h = NfcTapHistory()
        val id = h.begin(now)
        h.locate(id, fix(), now + 1000)
        val restored = NfcTapHistory.decode(h.encode(), now + 600_000)
        assertEquals(1, restored.origins(now + 600_000).size)
        assertTrue(restored.origins(now + 299_999).isEmpty())
    }
    @Test fun paymentBeforeOrAfterTapExcludesOnlyItsTimeWindow() {
        val h = NfcTapHistory()
        val first = h.begin(now)
        h.locate(first, fix(), now + 1000)
        val paid = h.begin(now + 600_000)
        h.locate(paid, fix(now + 600_000), now + 601_000)
        h.payment(now + 605_000, now + 605_000)
        assertEquals(listOf(now), h.origins(now + 1_000_000).map { it.timestamp })
        val before = NfcTapHistory()
        before.payment(now, now)
        before.begin(now + 1000)
        assertTrue(before.taps.single().excluded)
    }
    @Test fun duplicateOrReplayedReceiptsDoNotEraseOldOrigins() {
        val h = NfcTapHistory()
        val id = h.begin(now)
        h.locate(id, fix(), now + 1000)
        h.payment(now - 120_000, now)
        h.payment(now + 600_000, now + 600_000)
        h.payment(now + 600_000, now + 600_000)
        assertEquals(1, h.origins(now + 900_000).size)
    }
    @Test fun ambiguousNearbyPaymentsCannotBecomeOriginEvidence() {
        val h = NfcTapHistory()
        h.begin(now); h.begin(now + 5000)
        h.payment(now + 10_000, now + 10_000)
        assertTrue(h.taps.all { it.excluded })
    }
    @Test fun staleInaccurateMockAndFutureFixesAreRejected() {
        for (point in listOf(fix(now - 16_000), fix(accuracy = 101f), fix().copy(isMock = true),
            fix(now + 5000), fix().copy(accuracyMeters = null), fix().copy(latitude = Double.NaN))) {
            val h = NfcTapHistory(); val id = h.begin(now)
            h.locate(id, point, now + 1000)
            assertTrue(h.origins(now + 600_000).isEmpty())
        }
    }
    @Test fun lateFixCannotRevivePaidContact() {
        val h = NfcTapHistory(); val id = h.begin(now)
        h.payment(now + 1000, now + 1000)
        h.locate(id, fix(), now + 2000)
        assertNull(h.taps.single().latitude)
    }
    @Test fun candidatesAreBoundedExpireAndAreConsumedByJourney() {
        val h = NfcTapHistory()
        repeat(40) { h.begin(now + it) }
        assertEquals(32, h.taps.size)
        h.closeJourney(now + 600_000)
        assertTrue(h.taps.isEmpty())
        h.begin(now)
        h.prune(now + NfcTapHistory.RETENTION_MS + 1)
        assertTrue(h.taps.isEmpty())
    }
    @Test fun corruptOversizedInvalidAndFutureStateFailsClosed() {
        for (raw in listOf("broken", "x".repeat(32_769), """{"taps":[{"id":"x","at":10000000,"excluded":false,"lat":999}],"payments":[]}""")) {
            assertTrue(NfcTapHistory.decode(raw, now).taps.isEmpty())
        }
        val h = NfcTapHistory(); h.begin(now + 1)
        assertTrue(NfcTapHistory.decode(h.encode(), now).taps.isEmpty())
    }
}
