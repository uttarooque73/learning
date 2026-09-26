package com.uttarooque73.netguard.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

class DeviceDiscovery(
    private val connectTimeoutMs: Int = 250
) {
    suspend fun discover(localIp: String, prefixLength: Int): List<DiscoveredDevice> = withContext(Dispatchers.IO) {
        if (prefixLength !in 1..30) return@withContext emptyList()

        val addressBytes = localIp.split('.').mapNotNull { it.toIntOrNull() }
        if (addressBytes.size != 4 || addressBytes.any { it !in 0..255 }) {
            return@withContext emptyList()
        }

        val hostBits = 32 - prefixLength
        val networkSize = 1L shl hostBits
        if (networkSize > 256L) return@withContext emptyList()

        val localValue = addressBytes.fold(0L) { acc, octet -> (acc shl 8) or octet.toLong() }
        val mask = (-1L shl hostBits) and 0xffffffffL
        val network = localValue and mask
        val first = if (networkSize > 2) network + 1 else network
        val last = if (networkSize > 2) network + networkSize - 2 else network + networkSize - 1

        val results = mutableListOf<DiscoveredDevice>()
        for (value in first..last) {
            val ip = listOf(
                (value shr 24) and 255,
                (value shr 16) and 255,
                (value shr 8) and 255,
                value and 255
            ).joinToString(".")

            if (ip == localIp) continue

            if (isReachable(ip)) {
                results += DiscoveredDevice(ipAddress = ip, reachable = true)
            }
        }
        results
    }

    private fun isReachable(ip: String): Boolean = try {
        Socket().use { socket ->
            socket.connect(InetSocketAddress(ip, 80), connectTimeoutMs)
            true
        }
    } catch (_: Exception) {
        try {
            java.net.InetAddress.getByName(ip).isReachable(connectTimeoutMs)
        } catch (_: Exception) {
            false
        }
    }
}
