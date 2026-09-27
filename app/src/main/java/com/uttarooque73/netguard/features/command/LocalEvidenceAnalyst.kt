package com.uttarooque73.netguard.features.command

import com.uttarooque73.netguard.audit.Finding
import com.uttarooque73.netguard.audit.DiscoveredService
import com.uttarooque73.netguard.network.DiscoveredDevice
import com.uttarooque73.netguard.network.NetworkInfo

object LocalEvidenceAnalyst {
    fun buildContext(
        network: NetworkInfo?,
        devices: List<DiscoveredDevice>,
        services: List<DiscoveredService>,
        findings: List<Finding>
    ): String = buildString {
        appendLine("Network: " + (network?.ssid ?: "not inspected"))
        appendLine("Gateway: " + (network?.gatewayAddress ?: "unknown"))
        appendLine("Devices: " + devices.size)
        devices.take(50).forEach { appendLine("DEVICE " + it.ipAddress + " " + (it.hostname ?: "unknown") + " reachable=" + it.reachable) }
        appendLine("Services: " + services.size)
        services.take(100).forEach { appendLine("SERVICE " + it.ipAddress + " " + it.protocol + "/" + it.port + " " + it.serviceName + " reachable=" + it.reachable) }
        appendLine("Findings: " + findings.size)
        findings.take(50).forEach { appendLine("FINDING " + it.id + " " + it.ipAddress + " severity=" + it.severity + " confidence=" + it.confidence + " evidence=" + it.evidence) }
    }

    fun prompt(question: String, context: String): String =
        "Answer only from the NetGuard evidence below. Do not invent facts, vulnerabilities, versions, credentials, or compromise. If evidence is insufficient, say so.\n\nQuestion: " +
            question + "\n\nEvidence:\n" + context
}
