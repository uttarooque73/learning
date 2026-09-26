package com.uttarooque73.netguard.audit

object ServiceFindingRules {
    fun evaluate(service: DiscoveredService): Finding? = when (service.port) {
        23 -> Finding("NET-TELNET-001", "Telnet service exposed", FindingSeverity.HIGH, FindingConfidence.HIGH, service.ipAddress, "TCP/23 is reachable on " + service.ipAddress + ".", "Telnet commonly transmits session credentials and data without transport encryption.", "Disable Telnet where it is not required. Prefer SSH or another authenticated encrypted administration protocol.", "Rescan TCP/23 and confirm that the service is no longer reachable.")
        445 -> Finding("NET-SMB-001", "SMB service exposed", FindingSeverity.MEDIUM, FindingConfidence.HIGH, service.ipAddress, "TCP/445 is reachable on " + service.ipAddress + ".", "SMB exposure can increase attack surface when unnecessary or broadly accessible.", "Restrict SMB to trusted hosts and networks, disable it when unnecessary, and keep the device patched.", "Rescan TCP/445 and confirm the exposure matches the intended network policy.")
        80 -> Finding("NET-HTTP-001", "Unencrypted HTTP service exposed", FindingSeverity.LOW, FindingConfidence.MEDIUM, service.ipAddress, "TCP/80 is reachable on " + service.ipAddress + ".", "HTTP does not provide transport encryption by itself; risk depends on the application and data.", "Prefer HTTPS for sensitive application traffic and redirect HTTP to HTTPS where supported.", "Confirm sensitive traffic is protected with HTTPS.")
        else -> null
    }
}
