package com.uttarooque73.netguard.network

data class NetworkInfo(
    val interfaceName: String?,
    val localAddress: String?,
    val gatewayAddress: String?,
    val subnet: String?,
    val dnsServers: List<String>,
    val ssid: String?,
    val bssid: String?,
    val wifiSecurity: String?
)
