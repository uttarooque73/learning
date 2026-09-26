package com.uttarooque73.netguard.network

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class NetworkInventoryStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun saveNetwork(network: NetworkInfo) {
        preferences.edit()
            .putString(KEY_NETWORK, JSONObject().apply {
                putNullable("interfaceName", network.interfaceName)
                putNullable("localAddress", network.localAddress)
                putNullable("gatewayAddress", network.gatewayAddress)
                putNullable("subnet", network.subnet)
                put("dnsServers", JSONArray(network.dnsServers))
                putNullable("ssid", network.ssid)
                putNullable("bssid", network.bssid)
                putNullable("wifiSecurity", network.wifiSecurity)
            }.toString())
            .apply()
    }

    fun loadNetwork(): NetworkInfo? {
        val raw = preferences.getString(KEY_NETWORK, null) ?: return null
        return runCatching {
            val json = JSONObject(raw)
            NetworkInfo(
                interfaceName = json.optStringOrNull("interfaceName"),
                localAddress = json.optStringOrNull("localAddress"),
                gatewayAddress = json.optStringOrNull("gatewayAddress"),
                subnet = json.optStringOrNull("subnet"),
                dnsServers = json.optJSONArray("dnsServers")?.toStringList() ?: emptyList(),
                ssid = json.optStringOrNull("ssid"),
                bssid = json.optStringOrNull("bssid"),
                wifiSecurity = json.optStringOrNull("wifiSecurity")
            )
        }.getOrNull()
    }

    fun saveDevices(devices: List<DiscoveredDevice>) {
        val json = JSONArray()
        devices.forEach { device ->
            json.put(JSONObject().apply {
                put("ipAddress", device.ipAddress)
                putNullable("hostname", device.hostname)
                put("reachable", device.reachable)
            })
        }
        preferences.edit().putString(KEY_DEVICES, json.toString()).apply()
    }

    fun loadDevices(): List<DiscoveredDevice> {
        val raw = preferences.getString(KEY_DEVICES, null) ?: return emptyList()
        return runCatching {
            val json = JSONArray(raw)
            List(json.length()) { index ->
                val item = json.getJSONObject(index)
                DiscoveredDevice(
                    ipAddress = item.getString("ipAddress"),
                    hostname = item.optStringOrNull("hostname"),
                    reachable = item.optBoolean("reachable")
                )
            }
        }.getOrElse { emptyList() }
    }

    fun clearDevices() {
        preferences.edit().remove(KEY_DEVICES).apply()
    }

    private fun JSONObject.putNullable(key: String, value: String?) {
        put(key, value ?: JSONObject.NULL)
    }

    private fun JSONObject.optStringOrNull(key: String): String? =
        if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }

    private fun JSONArray.toStringList(): List<String> =
        List(length()) { getString(it) }

    companion object {
        private const val PREFERENCES_NAME = "netguard_inventory"
        private const val KEY_NETWORK = "network"
        private const val KEY_DEVICES = "devices"
    }
}
