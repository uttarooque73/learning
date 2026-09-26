package com.uttarooque73.netguard.network

import com.uttarooque73.netguard.audit.ServiceCatalog
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
                            if (isReachable(ip)) DiscoveredDevice(ipAddress = ip, hostname = resolveHostname(ip), reachable = true) else null
                        }
                    }.awaitAll().filterNotNull()
                }
            }
        }

    private fun resolveHostname(ip: String): String? = runCatching {
        val host = java.net.InetAddress.getByName(ip).canonicalHostName
        host.takeUnless { it == ip }
    }.getOrNull()

    private fun isReachable(ip: String): Boolean {
        if (ServiceCatalog.ports.any { (port, _) -> canConnect(ip, port) }) return true
        return runCatching {
            java.net.InetAddress.getByName(ip).isReachable(connectTimeoutMs)
        }.getOrDefault(false)
    }

    private fun canConnect(ip: String, port: Int): Boolean = runCatching {
        Socket().use { socket ->
            socket.connect(InetSocketAddress(ip, port), connectTimeoutMs)
            true
        }
    }.getOrDefault(false)
}
