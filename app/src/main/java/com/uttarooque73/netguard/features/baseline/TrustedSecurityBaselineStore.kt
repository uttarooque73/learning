package com.uttarooque73.netguard.features.baseline

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class TrustedSecurityBaselineStore(context: Context) {
    private val prefs = context.getSharedPreferences("netguard_trusted_baseline", Context.MODE_PRIVATE)

    fun save(snapshot: TrustedSecurityBaselineSnapshot) {
        prefs.edit().putString("snapshot", JSONObject().apply {
            put("capturedAtEpochMs", snapshot.capturedAtEpochMs)
            put("ssid", snapshot.ssid); put("bssid", snapshot.bssid); put("gateway", snapshot.gateway)
            put("deviceKeys", JSONArray(snapshot.deviceKeys)); put("serviceKeys", JSONArray(snapshot.serviceKeys)); put("appKeys", JSONArray(snapshot.appKeys))
        }.toString()).apply()
    }

    fun load(): TrustedSecurityBaselineSnapshot? = runCatching {
        val j = JSONObject(prefs.getString("snapshot", null) ?: return null)
        fun array(name: String) = List(j.optJSONArray(name)?.length() ?: 0) { j.getJSONArray(name).getString(it) }
        TrustedSecurityBaselineSnapshot(j.getLong("capturedAtEpochMs"), j.optString("ssid").ifBlank { null }, j.optString("bssid").ifBlank { null }, j.optString("gateway").ifBlank { null }, array("deviceKeys"), array("serviceKeys"), array("appKeys"))
    }.getOrNull()

    fun clear() { prefs.edit().remove("snapshot").apply() }
}
