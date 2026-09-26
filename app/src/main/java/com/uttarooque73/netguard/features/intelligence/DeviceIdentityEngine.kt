package com.uttarooque73.netguard.features.intelligence

import com.uttarooque73.netguard.audit.DiscoveredService
import com.uttarooque73.netguard.network.DiscoveredDevice

data class DeviceIdentity(
    val ipAddress: String,
    val hostname: String?,
    val deviceClass: String,
    val confidence: String,
    val evidence: List<String>
)

object DeviceIdentityEngine {
    fun identify(device: DiscoveredDevice, services: List<DiscoveredService>): DeviceIdentity {
        val observed = services.filter { it.ipAddress == device.ipAddress }
        val ports = observed.map { it.port }.toSet()
        val evidence = buildList {
            if (device.hostname != null) add("Hostname: " + device.hostname)
            if (445 in ports) add("SMB exposure observed")
            if (22 in ports) add("SSH exposure observed")
            if (80 in ports || 443 in ports || 8080 in ports || 8443 in ports) add("Web service exposure observed")
            if (53 in ports) add("DNS service exposure observed")
        }
        val clazz = when {
            445 in ports && 22 in ports -> "Server / network appliance"
            445 in ports -> "Windows / SMB-capable device"
            80 in ports || 443 in ports || 8080 in ports || 8443 in ports -> "Web-capable device"
            else -> "Unknown device"
        }
        val confidence = when {
            evidence.size >= 2 -> "HIGH"
            evidence.isNotEmpty() -> "MEDIUM"
            else -> "LOW"
        }
        return DeviceIdentity(device.ipAddress, device.hostname, clazz, confidence, evidence)
    }
}