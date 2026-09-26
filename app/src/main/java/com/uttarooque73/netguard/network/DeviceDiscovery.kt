package com.uttarooque73.netguard.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

class DeviceDiscovery(
    private val connectTimeoutMs: Int = 250,
    private val maxConcurrentChecks: Int = 32
) {
    suspend fun discover(localIp: String, prefixLength: Int): List<DiscoveredDevice> =
        withContext(Dispatchers.IO) {
            val hosts = SubnetCalculator.hosts(localIp, prefixLength)
            if (hosts.isEmpty()) return@withContext emptyList()

            coroutineScope {
                hosts.chunked(maxConcurrentChecks.coerceAtLeast(1)).flatMap { batch ->
                    batch.map { ip ->
                        async(Dispatchers.IO) {
                            if (isReachable(ip)) DiscoveredDevice(ipAddress = ip, reachable = true) else null
                        }
                    }.awaitAll().filterNotNull()
                }
            }
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
