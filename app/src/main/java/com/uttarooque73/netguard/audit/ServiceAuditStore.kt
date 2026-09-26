package com.uttarooque73.netguard.audit

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class ServiceAuditStore(context: Context) {
    private val preferences = context.getSharedPreferences("netguard_services", Context.MODE_PRIVATE)

    fun save(services: List<DiscoveredService>) {
        val json = JSONArray()
        services.forEach { service ->
            json.put(JSONObject().apply {
                put("ipAddress", service.ipAddress)
                put("port", service.port)
                put("protocol", service.protocol)
                put("serviceName", service.serviceName)
                put("reachable", service.reachable)
                put("discoveredAtEpochMs", service.discoveredAtEpochMs)
            })
        }
        preferences.edit().putString("services", json.toString()).apply()
    }

    fun load(): List<DiscoveredService> {
        val raw = preferences.getString("services", null) ?: return emptyList()
        return runCatching {
            val json = JSONArray(raw)
            List(json.length()) { index ->
                val item = json.getJSONObject(index)
                DiscoveredService(
                    ipAddress = item.getString("ipAddress"),
                    port = item.getInt("port"),
                    protocol = item.optString("protocol", "TCP"),
                    serviceName = item.optString("serviceName", "Unknown"),
                    reachable = item.optBoolean("reachable"),
                    discoveredAtEpochMs = item.optLong("discoveredAtEpochMs")
                )
            }
        }.getOrElse { emptyList() }
    }

    fun clear() {
        preferences.edit().remove("services").apply()
    }
}
