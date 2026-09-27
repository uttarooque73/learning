package com.uttarooque73.netguard.network

enum class DeviceObservationStatus { REACHABLE, UNREACHABLE, STALE }

data class DiscoveredDevice(
    val ipAddress: String,
    val hostname: String? = null,
    val reachable: Boolean = false,
    val discoveredAtEpochMs: Long = System.currentTimeMillis()
) {
    fun observationStatus(nowEpochMs: Long, staleAfterMs: Long = DEFAULT_STALE_AFTER_MS): DeviceObservationStatus {
        require(staleAfterMs >= 0L) { "staleAfterMs must not be negative" }
        if (nowEpochMs < discoveredAtEpochMs) return if (reachable) DeviceObservationStatus.REACHABLE else DeviceObservationStatus.UNREACHABLE
        if (nowEpochMs - discoveredAtEpochMs >= staleAfterMs) return DeviceObservationStatus.STALE
        return if (reachable) DeviceObservationStatus.REACHABLE else DeviceObservationStatus.UNREACHABLE
    }

    companion object { const val DEFAULT_STALE_AFTER_MS = 15 * 60 * 1000L }
}
