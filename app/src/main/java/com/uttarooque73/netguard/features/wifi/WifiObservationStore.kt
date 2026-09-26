package com.uttarooque73.netguard.features.wifi

import android.content.Context

class WifiObservationStore(context: Context) {
    private val prefs = context.getSharedPreferences("netguard_wifi_trust", Context.MODE_PRIVATE)

    fun load(): WifiObservation? {
        val ssid = prefs.getString("ssid", null)
        val bssid = prefs.getString("bssid", null)
        val gateway = prefs.getString("gateway", null)
        val security = prefs.getString("security", null)
        val observed = prefs.getLong("observedAt", 0L)
        if (ssid == null && bssid == null && gateway == null && security == null) return null
        return WifiObservation(ssid, bssid, gateway, security, observed)
    }

    fun save(observation: WifiObservation) {
        prefs.edit()
            .putString("ssid", observation.ssid)
            .putString("bssid", observation.bssid)
            .putString("gateway", observation.gateway)
            .putString("security", observation.security)
            .putLong("observedAt", observation.observedAtEpochMs)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}