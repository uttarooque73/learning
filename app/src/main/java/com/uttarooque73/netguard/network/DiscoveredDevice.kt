package com.uttarooque73.netguard.network

data class DiscoveredDevice(
    val ipAddress: String,
    val hostname: String? = null,
    val reachable: Boolean = false
)
