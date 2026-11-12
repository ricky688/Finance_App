package com.example.vibefinance.transit

import android.content.Context
import android.content.SharedPreferences

object MtrTransitSettings {

    internal const val PREFS_NAME = "vibe_finance_mtr_transit"
    private const val KEY_ENABLED = "mtr_transit_tracking_enabled"
    private const val KEY_OCTOPUS_ACCOUNT_ID = "mtr_octopus_account_id"
    private const val KEY_PASSENGER_TYPE = "mtr_passenger_type"

    internal fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ENABLED, true)
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    internal fun nfcPrefs(context: Context): SharedPreferences =
        com.example.vibefinance.util.CoordinatedPreferences.get(context, "vibe_finance_prefs")

    fun isNfcEnabled(context: Context): Boolean = nfcPrefs(context).getBoolean("nfc_origin_enabled", false)

    fun setNfcEnabled(context: Context, enabled: Boolean) {
        nfcPrefs(context).edit().putBoolean("nfc_origin_enabled", enabled).apply()
    }

    fun getOctopusAccountId(context: Context): String? {
        val id = getPrefs(context).getString(KEY_OCTOPUS_ACCOUNT_ID, null)
        return if (id.isNullOrBlank()) null else id
    }

    fun setOctopusAccountId(context: Context, accountId: String?) {
        getPrefs(context).edit().putString(KEY_OCTOPUS_ACCOUNT_ID, accountId).apply()
    }

    fun getPassengerType(context: Context): MtrPassengerType {
        val raw = getPrefs(context).getString(KEY_PASSENGER_TYPE, null)
        return MtrPassengerType.fromString(raw)
    }

    fun setPassengerType(context: Context, type: MtrPassengerType) {
        getPrefs(context).edit().putString(KEY_PASSENGER_TYPE, type.name).apply()
    }
}
