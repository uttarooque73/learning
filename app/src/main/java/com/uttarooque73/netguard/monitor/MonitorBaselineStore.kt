package com.uttarooque73.netguard.monitor

import android.content.Context
import com.uttarooque73.netguard.audit.DiscoveredService
import com.uttarooque73.netguard.network.DiscoveredDevice
import org.json.JSONArray

class MonitorBaselineStore(context: Context) {
    private val preferences = context.getSharedPreferences("netguard_monitor_baseline", Context.MODE_PRIVATE)

    fun saveDevices(devices: List<DiscoveredDevice>) {
        preferences.edit().putString("devices", JSONArray(devices.map { it.ipAddress }).toString()).apply()
    }

    fun loadDeviceIps(): Set<String> = loadArray("devices")

    fun saveServices(services: List<DiscoveredService>) {
        preferences.edit().putString("services", JSONArray(services.map { "${it.ipAddress}|${it.protocol}|${it.port}" }).toString()).apply()
    }

    fun loadServiceKeys(): Set<String> = loadArray("services")

    private fun loadArray(key: String): Set<String> {
        val raw = preferences.getString(key, null) ?: return emptySet()
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { array.getString(it) }.toSet()
        }.getOrElse { emptySet() }
    }
}