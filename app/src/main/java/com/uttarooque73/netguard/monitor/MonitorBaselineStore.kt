package com.uttarooque73.netguard.monitor

import android.content.Context
import com.uttarooque73.netguard.audit.DiscoveredService
import com.uttarooque73.netguard.network.DiscoveredDevice
import com.uttarooque73.netguard.network.NetworkInfo
import org.json.JSONArray
import org.json.JSONObject

class MonitorBaselineStore(context: Context) {
    private val preferences = context.getSharedPreferences("netguard_monitor_baseline", Context.MODE_PRIVATE)

    fun isInitialized(): Boolean = preferences.getBoolean("initialized", false)

    fun saveNetwork(network: NetworkInfo) {
        val json = JSONObject().apply {
            putOpt("interfaceName", network.interfaceName)
            putOpt("localAddress", network.localAddress)
            putOpt("gatewayAddress", network.gatewayAddress)
            putOpt("subnet", network.subnet)
            put("dnsServers", JSONArray(network.dnsServers))
            putOpt("ssid", network.ssid)
            putOpt("bssid", network.bssid)
            putOpt("wifiSecurity", network.wifiSecurity)
        }
        preferences.edit().putString("network", json.toString()).putBoolean("initialized", true).apply()
    }

    fun loadNetwork(): NetworkInfo? {
        val raw = preferences.getString("network", null) ?: return null
        return runCatching {
            val value = JSONObject(raw)
            NetworkInfo(
                interfaceName = value.optString("interfaceName").takeIf { it.isNotBlank() && it != "null" },
                localAddress = value.optString("localAddress").takeIf { it.isNotBlank() && it != "null" },
                gatewayAddress = value.optString("gatewayAddress").takeIf { it.isNotBlank() && it != "null" },
                subnet = value.optString("subnet").takeIf { it.isNotBlank() && it != "null" },
                dnsServers = value.optJSONArray("dnsServers")?.let { array -> List(array.length()) { array.getString(it) } } ?: emptyList(),
                ssid = value.optString("ssid").takeIf { it.isNotBlank() && it != "null" },
                bssid = value.optString("bssid").takeIf { it.isNotBlank() && it != "null" },
                wifiSecurity = value.optString("wifiSecurity").takeIf { it.isNotBlank() && it != "null" }
            )
        }.getOrNull()
    }

    fun saveDevices(devices: List<DiscoveredDevice>) {
        preferences.edit().putString("devices", JSONArray(devices.map { it.ipAddress }).toString()).putBoolean("initialized", true).apply()
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
        preferences.edit().putString("services", json.toString()).putBoolean("initialized", true).apply()
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
