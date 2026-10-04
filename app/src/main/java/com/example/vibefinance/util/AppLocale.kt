package com.example.vibefinance.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.LocaleList
import androidx.annotation.StringRes
import com.example.vibefinance.ui.AppLanguage
import java.util.Locale

/** Read the device locale independently of the app's last explicit language override. */
fun resolveAppLocale(language: AppLanguage): Locale = when (language) {
    AppLanguage.SYSTEM -> Resources.getSystem().configuration.locales[0] ?: Locale.ENGLISH
    AppLanguage.ENGLISH -> Locale.ENGLISH
    AppLanguage.TRADITIONAL_CHINESE -> Locale.forLanguageTag("zh-Hant-HK")
}

/** Background feedback uses the same saved preference as the Compose screens. */
fun Context.appLocalizedContext(): Context {
    val name = getSharedPreferences("vibe_finance_prefs", Context.MODE_PRIVATE)
        .getString("app_language", AppLanguage.SYSTEM.name)
    val language = runCatching { AppLanguage.valueOf(name ?: AppLanguage.SYSTEM.name) }
        .getOrDefault(AppLanguage.SYSTEM)
    val locale = resolveAppLocale(language)
    val config = Configuration(resources.configuration).apply {
        setLocales(LocaleList(locale, Locale.ENGLISH))
        setLayoutDirection(locale)
    }
    return createConfigurationContext(config)
}

fun Context.appString(@StringRes id: Int, vararg args: Any): String =
    appLocalizedContext().getString(id, *args)
