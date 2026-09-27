package com.uttarooque73.netguard.monitor

enum class MonitorEventType { NETWORK_CHANGED, NEW_DEVICE, DEVICE_CHANGED, NEW_SERVICE, SERVICE_CHANGED, SERVICE_REMOVED, FINDING_CHANGED }

data class MonitorEvent(
    val id: String,
    val type: MonitorEventType,
    val ipAddress: String,
    val detail: String,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)