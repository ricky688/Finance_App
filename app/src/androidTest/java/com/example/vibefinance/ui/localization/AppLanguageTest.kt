package com.example.vibefinance.ui.localization

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vibefinance.R
import com.example.vibefinance.ui.AppLanguage
import com.example.vibefinance.util.appString
import com.example.vibefinance.util.resolveAppLocale
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

/** Real Settings switching is exercised by captures/localization-2026-10-04/verify_languages.py.
 * Background feedback needs no idle UI; verify saved language against actual Android resources.
 */
@RunWith(AndroidJUnit4::class)
class AppLanguageTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val preferences get() = context.getSharedPreferences("vibe_finance_prefs", Context.MODE_PRIVATE)
    private var previousLanguage: String? = null
    private lateinit var previousLocale: Locale

    @Before fun rememberPreference() {
        previousLanguage = preferences.getString("app_language", null)
        previousLocale = Locale.getDefault()
    }
    @After fun restorePreference() {
        preferences.edit().apply {
            if (previousLanguage == null) remove("app_language") else putString("app_language", previousLanguage)
        }.commit()
        Locale.setDefault(previousLocale)
    }
    @Test fun feedbackAndNotificationFormattingFollowSavedLanguage() {
        preferences.edit().putString("app_language", AppLanguage.TRADITIONAL_CHINESE.name).commit()
        assertEquals("交易已記錄。", context.appString(R.string.feedback_transaction_recorded))
        assertEquals("已將於 Store 消費的 $12.60 記入 Wallet", context.appString(R.string.nf_logged_expense_message, 12.6, "Store", "Wallet", ""))
        preferences.edit().putString("app_language", AppLanguage.ENGLISH.name).commit()
        assertEquals("Transaction recorded!", context.appString(R.string.feedback_transaction_recorded))
        assertEquals("Logged $12.60 at Store to Wallet", context.appString(R.string.nf_logged_expense_message, 12.6, "Store", "Wallet", ""))
    }
    @Test fun systemPreferenceUsesDeviceResourcesAfterGlobalLocaleOverride() {
        val deviceLocale = resolveAppLocale(AppLanguage.SYSTEM)
        Locale.setDefault(Locale.forLanguageTag("zh-Hant-HK"))
        preferences.edit().putString("app_language", AppLanguage.SYSTEM.name).commit()
        assertEquals(deviceLocale, resolveAppLocale(AppLanguage.SYSTEM))
        val config = Configuration(context.resources.configuration).apply {
            setLocales(LocaleList(deviceLocale, Locale.ENGLISH))
        }
        assertEquals(context.createConfigurationContext(config).getString(R.string.feedback_transaction_recorded), context.appString(R.string.feedback_transaction_recorded))
    }
}
