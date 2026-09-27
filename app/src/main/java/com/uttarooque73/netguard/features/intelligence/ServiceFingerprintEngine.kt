package com.uttarooque73.netguard.features.intelligence

import com.uttarooque73.netguard.audit.DiscoveredService

data class RichServiceFingerprint(
    val ipAddress: String,
    val port: Int,
    val protocol: String,
    val service: String,
    val productCandidates: List<String>,
    val versionEvidence: String?,
    val confidence: String,
    val evidence: List<String>
)

object ServiceFingerprintEngine {
    private data class Profile(
        val service: String,
        val products: List<String>,
        val evidence: String
    )

    private val profiles = mapOf(
        22 to Profile("SSH", listOf("OpenSSH", "Dropbear", "other SSH server"), "Port 22 identifies SSH reachability; software/version is not asserted without a banner."),
        23 to Profile("Telnet", listOf("Telnet daemon"), "Port 23 identifies Telnet reachability; implementation/version is not asserted."),
        53 to Profile("DNS", listOf("BIND", "dnsmasq", "Unbound", "other DNS server"), "Port 53 identifies DNS reachability; resolver implementation requires protocol/application evidence."),
        80 to Profile("HTTP", listOf("Apache HTTP Server", "nginx", "Microsoft IIS", "other HTTP server"), "Port 80 identifies HTTP reachability; the Server header is required for a product claim."),
        443 to Profile("HTTPS", listOf("Apache HTTP Server", "nginx", "Microsoft IIS", "other HTTPS server"), "Port 443 identifies HTTPS reachability; certificate/HTTP headers are required for a product claim."),
        445 to Profile("SMB", listOf("Samba", "Windows SMB", "other SMB server"), "Port 445 identifies SMB reachability; implementation requires SMB protocol evidence."),
        8080 to Profile("HTTP-alt", listOf("Apache Tomcat", "Jetty", "nginx", "other HTTP server"), "Port 8080 commonly hosts HTTP services; application-level evidence is required."),
        8443 to Profile("HTTPS-alt", listOf("Tomcat", "Jetty", "other HTTPS server"), "Port 8443 commonly hosts HTTPS services; application-level evidence is required.")
    )

    fun fingerprint(service: DiscoveredService): RichServiceFingerprint {
        val profile = profiles[service.port]
        if (profile == null) {
            return RichServiceFingerprint(
                service.ipAddress, service.port, service.protocol, service.serviceName,
                emptyList(), null, "LOW",
                listOf("TCP reachability confirmed on port " + service.port + ".")
            )
        }
        return RichServiceFingerprint(
            service.ipAddress, service.port, service.protocol, profile.service,
            profile.products, null, "MEDIUM",
            listOf(
                "TCP reachability confirmed on port " + service.port + ".",
                profile.evidence
            )
        )
    }
}
