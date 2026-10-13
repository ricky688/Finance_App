package com.example.vibefinance.ui.components

import org.junit.Assert.*
import org.junit.Test

class FlyingCashEasterEggTest {

    @Test
    fun easterEggTriggerDetector_triggersOn5RapidTaps() {
        val detector = EasterEggTriggerDetector(requiredTaps = 5, timeoutMillis = 1800L)

        assertFalse(detector.registerTap(1000L))
        assertEquals(1, detector.tapCount)

        assertFalse(detector.registerTap(1300L))
        assertEquals(2, detector.tapCount)

        assertFalse(detector.registerTap(1600L))
        assertEquals(3, detector.tapCount)

        assertFalse(detector.registerTap(1900L))
        assertEquals(4, detector.tapCount)

        // 5th tap triggers!
        assertTrue(detector.registerTap(2200L))
        assertEquals(0, detector.tapCount) // Resets after triggering
    }

    @Test
    fun easterEggTriggerDetector_resetsOnTimeout() {
        val detector = EasterEggTriggerDetector(requiredTaps = 5, timeoutMillis = 1800L)

        detector.registerTap(1000L)
        detector.registerTap(1300L)
        detector.registerTap(1600L)
        assertEquals(3, detector.tapCount)

        // Delayed tap past 1800ms window resets count to 1
        assertFalse(detector.registerTap(4000L))
        assertEquals(1, detector.tapCount)
    }

    @Test
    fun easterEggTriggerDetector_manualResetClearsCount() {
        val detector = EasterEggTriggerDetector(requiredTaps = 5, timeoutMillis = 1800L)

        detector.registerTap(1000L)
        detector.registerTap(1200L)
        assertEquals(2, detector.tapCount)

        detector.reset()
        assertEquals(0, detector.tapCount)
    }

    @Test
    fun generateFlyingCashParticles_generatesCorrectDistribution() {
        val particles = generateFlyingCashParticles(count = 30, seed = 42L)
        assertEquals(30, particles.size)

        val wingedMoneyCount = particles.count { it.isWingedMoney }
        assertTrue("Should contain predominantly winged money", wingedMoneyCount >= 20)

        particles.forEach { particle ->
            assertTrue("startXRatio within screen bounds", particle.startXRatio in 0.05f..0.98f)
            assertTrue("delayRatio >= 0", particle.delayRatio >= 0f)
            assertTrue("durationRatio in range", particle.durationRatio in 0.5f..1.0f)
            assertTrue("swayAmplitudeDp > 0", particle.swayAmplitudeDp > 0f)
            assertTrue("flapFrequencyHz between 4 and 7 Hz", particle.flapFrequencyHz in 4.0f..7.0f)
        }
    }

    @Test
    fun calculateFlapAngleDeg_oscillatesWithinMaxDegrees() {
        val maxDeg = 38f
        val freq = 5.0f

        // Sample across a full period (0.2s for 5Hz)
        val angles = (0..20).map { step ->
            val t = step * 0.01f
            calculateFlapAngleDeg(elapsedSeconds = t, flapFrequencyHz = freq, flapPhaseOffset = 0f, maxFlapDegrees = maxDeg)
        }

        assertTrue("Flap angle reaches near positive peak", angles.any { it > 30f })
        assertTrue("Flap angle reaches near negative peak", angles.any { it < -30f })
        assertTrue("Flap angle never exceeds max degrees", angles.all { kotlin.math.abs(it) <= maxDeg + 0.01f })
    }
}
