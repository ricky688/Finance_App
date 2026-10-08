package com.example.vibefinance.data.currency

import android.util.AtomicFile
import com.example.vibefinance.util.CurrencyEngine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** A rate is fixed when submitted; future fetches must never revalue saved transactions. */
data class ExchangeRateQuote(
    val currencyCode: String,
    val rateToHkd: Double,
    val rateDate: String,
    val fetchedAtMillis: Long,
    val source: String = "Frankfurter"
) {
    init {
        require(CurrencyEngine.supportedCurrencies.any { it.code == currencyCode })
        require(rateToHkd.isFinite() && rateToHkd > 0 && rateToHkd <= 1_000_000)
        require(validRateDate(rateDate) != null)
        require(fetchedAtMillis >= 0)
        require(source.isNotBlank() && source.length <= 64)
    }

    fun convert(amount: Double): Double {
        require(amount.isFinite() && amount >= 0)
        return BigDecimal.valueOf(amount).multiply(BigDecimal.valueOf(rateToHkd))
            .setScale(2, RoundingMode.HALF_UP).toDouble().also { require(it.isFinite()) }
    }

    fun rateText(): String = BigDecimal.valueOf(rateToHkd).stripTrailingZeros().toPlainString()
}

data class ExchangeRateResult(val quote: ExchangeRateQuote?, val offline: Boolean = false)

fun interface ExchangeRateLoader {
    suspend fun load(currencyCode: String, forceRefresh: Boolean): ExchangeRateResult
}

/** ISO dates are parsed strictly without java.time, for Android 24 compatibility. */
internal fun validRateDate(value: String): Long? {
    if (!Regex("\\d{4}-\\d{2}-\\d{2}").matches(value)) return null
    return runCatching {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone("UTC")
        }.parse(value)?.time
    }.getOrNull()
}

/** Only currency codes go over the network. No expense, account or merchant data is sent. */
class ExchangeRateRepository(
    private val cacheDirectory: File,
    private val clock: () -> Long = System::currentTimeMillis,
    private val fetch: suspend (String) -> ExchangeRateQuote = { fetchFrankfurter(it, clock()) }
) : ExchangeRateLoader {
    private val cacheLock = Mutex()

    override suspend fun load(currencyCode: String, forceRefresh: Boolean): ExchangeRateResult = withContext(Dispatchers.IO) {
        require(currencyCode != "HKD" && CurrencyEngine.supportedCurrencies.any { it.code == currencyCode })
        val cached = cacheLock.withLock { readCache(currencyCode) }
        if (!forceRefresh && cached != null && clock() - cached.fetchedAtMillis in 0 until REFRESH_MILLIS) {
            return@withContext ExchangeRateResult(cached)
        }
        try {
            val quote = fetch(currencyCode)
            ensureActive()
            require(quote.currencyCode == currencyCode && usable(quote))
            cacheLock.withLock {
                // A storage failure does not invalidate a rate successfully obtained online.
                runCatching { writeCache(quote) }
            }
            ExchangeRateResult(quote)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            ensureActive()
            android.util.Log.w("ExchangeRates", "Could not fetch $currencyCode/HKD", failure)
            ExchangeRateResult(cached, offline = true)
        }
    }

    private fun usable(quote: ExchangeRateQuote): Boolean {
        val age = clock() - quote.fetchedAtMillis
        val date = validRateDate(quote.rateDate) ?: return false
        return age in 0..MAX_CACHE_AGE_MILLIS && date <= clock() + DAY_MILLIS && clock() - date <= MAX_RATE_AGE_MILLIS
    }

    private fun readCache(code: String): ExchangeRateQuote? = runCatching {
        val bytes = AtomicFile(File(cacheDirectory, "$code.json")).readFully()
        require(bytes.size <= MAX_RESPONSE_BYTES)
        val json = JSONObject(String(bytes, Charsets.UTF_8))
        val quote = ExchangeRateQuote(json.getString("currency"), json.getDouble("rate"),
            json.getString("date"), json.getLong("fetchedAt"), json.getString("source"))
        quote.takeIf { it.currencyCode == code && it.source == "Frankfurter" && usable(it) }
    }.getOrNull()

    private fun writeCache(quote: ExchangeRateQuote) {
        check(cacheDirectory.isDirectory || cacheDirectory.mkdirs())
        val file = AtomicFile(File(cacheDirectory, "${quote.currencyCode}.json"))
        val stream = file.startWrite()
        try {
            val json = JSONObject().put("currency", quote.currencyCode).put("rate", quote.rateToHkd)
                .put("date", quote.rateDate).put("fetchedAt", quote.fetchedAtMillis).put("source", quote.source)
            stream.write(json.toString().toByteArray(Charsets.UTF_8))
            file.finishWrite(stream)
        } catch (failure: Exception) {
            file.failWrite(stream)
            throw failure
        }
    }

    companion object {
        private const val DAY_MILLIS = 86_400_000L
        private const val REFRESH_MILLIS = 15 * 60_000L
        private const val MAX_CACHE_AGE_MILLIS = 7 * DAY_MILLIS
        private const val MAX_RATE_AGE_MILLIS = 14 * DAY_MILLIS
        private const val MAX_RESPONSE_BYTES = 16_384

        internal fun parseResponse(code: String, text: String, now: Long): ExchangeRateQuote {
            val json = JSONObject(text)
            require(json.getString("base") == code && json.getString("quote") == "HKD")
            return ExchangeRateQuote(code, json.getDouble("rate"), json.getString("date"), now)
        }

        private fun fetchFrankfurter(code: String, now: Long): ExchangeRateQuote {
            val connection = URL("https://api.frankfurter.dev/v2/rate/${code.lowercase(Locale.ROOT)}/hkd")
                .openConnection() as HttpsURLConnection
            try {
                connection.connectTimeout = 8_000
                connection.readTimeout = 8_000
                connection.instanceFollowRedirects = false
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty("User-Agent", "VibeFinance/1.0 (Android)")
                if (connection.responseCode != 200) throw IOException("Exchange rate unavailable")
                val bytes = connection.inputStream.use { stream ->
                    val output = java.io.ByteArrayOutputStream()
                    val buffer = ByteArray(1024)
                    while (true) {
                        val count = stream.read(buffer)
                        if (count < 0) break
                        if (output.size() + count > MAX_RESPONSE_BYTES) throw IOException("Invalid rate response")
                        output.write(buffer, 0, count)
                    }
                    output.toByteArray()
                }
                return parseResponse(code, String(bytes, Charsets.UTF_8), now)
            } finally {
                connection.disconnect()
            }
        }
    }
}
