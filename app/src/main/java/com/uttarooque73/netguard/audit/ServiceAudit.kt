package com.uttarooque73.netguard.audit

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

class ServiceAudit(
    private val connectTimeoutMs: Int = 300,
    private val maxConcurrentChecks: Int = 16
) {
    suspend fun audit(ipAddress: String): List<DiscoveredService> = withContext(Dispatchers.IO) {
        coroutineScope {
            ServiceCatalog.ports
                .chunked(maxConcurrentChecks.coerceAtLeast(1))
                .flatMap { batch ->
                    batch.map { (port, name) ->
                        async(Dispatchers.IO) {
                            if (isOpen(ipAddress, port)) {
                                DiscoveredService(
                                    ipAddress = ipAddress,
                                    port = port,
                                    serviceName = name,
                                    reachable = true
                                )
                            } else null
                        }
                    }.awaitAll().filterNotNull()
                }
        }
    }

    private fun isOpen(ipAddress: String, port: Int): Boolean = runCatching {
        Socket().use { socket ->
            socket.connect(InetSocketAddress(ipAddress, port), connectTimeoutMs)
        }
        true
    }.getOrDefault(false)
}
