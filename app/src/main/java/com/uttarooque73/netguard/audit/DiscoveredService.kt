package com.uttarooque73.netguard.audit

data class DiscoveredService(
    val ipAddress: String,
    val port: Int,
    val protocol: String = "TCP",
    val serviceName: String,
    val reachable: Boolean,
    val discoveredAtEpochMs: Long = System.currentTimeMillis()
)
