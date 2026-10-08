package com.example.vibefinance.data.currency

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExchangeRatesTest {
    @get:Rule val files = TemporaryFolder()
    private val now = validRateDate("2026-10-08")!! + 12 * 3_600_000L
    private fun quote(code: String = "USD", rate: Double = 7.1234, date: String = "2026-10-08", fetched: Long = now) =
        ExchangeRateQuote(code, rate, date, fetched)

    @Test fun conversionRoundsToHongKongCentsWithoutFloatingPointTruncation() {
        assertEquals(49.86, quote().convert(7.0), 0.0)
        assertEquals(0.01, quote(rate = 0.005).convert(1.0), 0.0)
        assertEquals(0.37, quote("JPY", 0.0523).convert(7.0), 0.0)
    }

    @Test fun invalidRateAmountAndDateAreRejected() {
        for (rate in listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertThrows(IllegalArgumentException::class.java) { quote(rate = rate) }
        }
        for (date in listOf("2026-02-30", "2026-2-03", "garbage")) {
            assertThrows(IllegalArgumentException::class.java) { quote(date = date) }
        }
        assertThrows(IllegalArgumentException::class.java) { quote().convert(Double.NaN) }
        assertThrows(IllegalArgumentException::class.java) { quote().convert(-1.0) }
        assertThrows(IllegalArgumentException::class.java) { quote("../../other") }
    }

    @Test fun responseRequiresTheRequestedCurrencyAndHkdQuote() {
        val valid = """{"date":"2026-10-08","base":"USD","quote":"HKD","rate":7.1234}"""
        assertEquals(quote(), ExchangeRateRepository.parseResponse("USD", valid, now))
        assertThrows(IllegalArgumentException::class.java) { ExchangeRateRepository.parseResponse("JPY", valid, now) }
        assertThrows(IllegalArgumentException::class.java) {
            ExchangeRateRepository.parseResponse("USD", valid.replace("HKD", "EUR"), now)
        }
        assertThrows(Exception::class.java) { ExchangeRateRepository.parseResponse("USD", "<html>error</html>", now) }
    }

    @Test fun recentCacheSurvivesANewRepositoryAndAvoidsADuplicateFetch() = runBlocking {
        val cache = files.newFolder()
        var requests = 0
        val first = ExchangeRateRepository(cache, { now }) { requests++; quote() }
        assertEquals(quote(), first.load("USD", false).quote)
        val second = ExchangeRateRepository(cache, { now }) { requests++; error("Should use cache") }
        val cached = second.load("USD", false)
        assertEquals(1, requests)
        assertEquals(quote(), cached.quote)
        assertFalse(cached.offline)
    }

    @Test fun refreshUsesNewRateAndOfflineFallbackIsMarked() = runBlocking {
        val cache = files.newFolder()
        ExchangeRateRepository(cache, { now }) { quote() }.load("USD", false)
        val refreshed = ExchangeRateRepository(cache, { now }) { quote(rate = 8.01) }.load("USD", true)
        assertEquals(8.01, refreshed.quote!!.rateToHkd, 0.0)
        val offline = ExchangeRateRepository(cache, { now }) { throw IOException() }.load("USD", true)
        assertTrue(offline.offline)
        assertEquals(8.01, offline.quote!!.rateToHkd, 0.0)
    }

    @Test fun expiredCacheTriggersFetchAndRatesOlderThanAWeekCannotBeUsedOffline() = runBlocking {
        val cache = files.newFolder()
        ExchangeRateRepository(cache, { now }) { quote() }.load("USD", false)
        val later = now + 16 * 60_000
        var requested = false
        val refreshed = ExchangeRateRepository(cache, { later }) { requested = true; quote(fetched = later) }.load("USD", false)
        assertTrue(requested)
        assertEquals(later, refreshed.quote!!.fetchedAtMillis)
        val expired = ExchangeRateRepository(cache, { now + 8 * 86_400_000 }) { throw IOException() }.load("USD", false)
        assertNull(expired.quote)
        assertTrue(expired.offline)
    }

    @Test fun corruptCacheAndFirstUseOfflineNeverSubstituteAPreset() = runBlocking {
        val cache = files.newFolder()
        File(cache, "USD.json").writeText("broken")
        val offline = ExchangeRateRepository(cache, { now }) { throw IOException() }.load("USD", false)
        assertNull(offline.quote)
        assertTrue(offline.offline)
    }

    @Test fun mismatchedFutureAndStaleResponsesAreRejectedWithoutOverwritingCache() = runBlocking {
        val cache = files.newFolder()
        ExchangeRateRepository(cache, { now }) { quote() }.load("USD", false)
        for (bad in listOf(quote("JPY"), quote(date = "2026-10-10"), quote(date = "2026-09-01"))) {
            val result = ExchangeRateRepository(cache, { now }) { bad }.load("USD", true)
            assertTrue(result.offline)
            assertEquals(quote(), result.quote)
        }
    }

    @Test fun cacheStorageFailureDoesNotDiscardTheOnlineRate() = runBlocking {
        val path = files.newFile()
        val result = ExchangeRateRepository(path, { now }) { quote() }.load("USD", true)
        assertEquals(quote(), result.quote)
        assertFalse(result.offline)
    }

    @Test fun cancellationDoesNotBecomeAnOfflineSuccess() = runBlocking {
        val repository = ExchangeRateRepository(files.newFolder(), { now }) { throw CancellationException("cancelled") }
        try {
            repository.load("USD", true)
            fail("Cancellation must propagate")
        } catch (_: CancellationException) { }
    }
}
