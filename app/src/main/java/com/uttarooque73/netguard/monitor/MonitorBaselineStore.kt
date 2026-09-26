package com.uttarooque73.netguard.monitor

import android.content.Context
import com.uttarooque73.netguard.audit.DiscoveredService
import com.uttarooque73.netguard.network.DiscoveredDevice
import org.json.JSONArray
import org.json.JSONObject

class MonitorBaselineStore(context: Context) {
    private val preferences = context.getSharedPreferences("netguard_monitor_baseline", Context.MODE_PRIVATE)

    fun saveDevices(devices: List<DiscoveredDevice>) {
        preferences.edit().putString("devices", JSONArray(devices.map { it.ipAddress }).toString()).apply()
    }

    fun loadDeviceIps(): Set<String> = loadArray("devices")

    fun saveServices(services: List<DiscoveredService>) {
        val json = JSONArray()
        services.forEach { service ->
            json.put(
                JSONObject().apply {
                    put("ipAddress", service.ipAddress)
                    put("port", service.port)
                    put("protocol", service.protocol)
                    put("serviceName", service.serviceName)
                    put("reachable", service.reachable)
                }
            )
        }
        preferences.edit().putString("services", json.toString()).apply()
    }

    fun loadServices(): List<DiscoveredService> {
        val raw = preferences.getString("services", null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index ->
                val value = array.get(index)
                if (value is JSONObject) {
                    DiscoveredService(
                        ipAddress = value.getString("ipAddress"),
                        port = value.getInt("port"),
                        protocol = value.optString("protocol", "TCP"),
                        serviceName = value.optString("serviceName", "Unknown"),
                        reachable = value.optBoolean("reachable", true)
                    )
                } else {
                    // Backward compatibility with the original "ip|protocol|port" format.
                    val parts = value.toString().split('|')
                    require(parts.size == 3)
                    DiscoveredService(
                        ipAddress = parts[0],
                        port = parts[2].toInt(),
                        protocol = parts[1],
                        serviceName = parts[1],
                        reachable = true
                    )
                }
            }
        }.getOrElse { emptyList() }
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private fun loadArray(key: String): Set<String> {
        val raw = preferences.getString(key, null) ?: return emptySet()
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { array.getString(it) }.toSet()
        }.getOrElse { emptySet() }
    }
}
