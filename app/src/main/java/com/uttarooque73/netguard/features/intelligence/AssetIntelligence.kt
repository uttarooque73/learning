package com.uttarooque73.netguard.features.intelligence

import com.uttarooque73.netguard.audit.DiscoveredService
import com.uttarooque73.netguard.network.DiscoveredDevice

data class AssetFingerprint(val ipAddress: String, val hostname: String?, val deviceClass: String, val vendor: String?, val confidence: String, val evidence: List<String>)

data class ServiceFingerprint(val ipAddress: String, val port: Int, val service: String, val version: String?, val evidence: List<String>)

object AssetIntelligence {
    fun fingerprint(device: DiscoveredDevice, services: List<DiscoveredService>): AssetFingerprint {
        val ports = services.filter { it.ipAddress == device.ipAddress }.map { it.port }.toSet()
        val deviceClass = when {
            22 in ports && 80 in ports && 443 in ports -> "Server / network appliance"
            445 in ports -> "Windows / SMB-capable device"
            80 in ports || 443 in ports -> "Web-capable device"
            else -> "Unknown device"
        }
        val evidence = buildList {
            if (device.hostname != null) add("Hostname: " + device.hostname)
            if (ports.isNotEmpty()) add("Observed TCP ports: " + ports.sorted().joinToString())
        }
        return AssetFingerprint(device.ipAddress, device.hostname, deviceClass, null, if (evidence.size >= 2) "MEDIUM" else "LOW", evidence)
    }

    fun service(service: DiscoveredService): ServiceFingerprint = ServiceFingerprint(
        service.ipAddress, service.port, service.serviceName, null,
        listOf("TCP reachability confirmed on port " + service.port, "Exact software version requires application-level evidence."))
}