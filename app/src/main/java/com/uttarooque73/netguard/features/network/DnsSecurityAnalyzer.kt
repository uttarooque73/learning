package com.uttarooque73.netguard.features.network

data class DnsSecurityResult(val configuredServers: List<String>, val privateResolver: Boolean, val encryptedTransportObserved: Boolean?, val evidence: List<String>, val recommendations: List<String>)

object DnsSecurityAnalyzer {
    private val privateRanges = listOf("10.", "192.168.", "172.16.", "172.17.", "172.18.", "172.19.", "172.20.", "172.21.", "172.22.", "172.23.", "172.24.", "172.25.", "172.26.", "172.27.", "172.28.", "172.29.", "172.30.", "172.31.")
    fun analyze(servers: List<String>): DnsSecurityResult {
        val privateResolver = servers.any { server -> privateRanges.any { server.startsWith(it) } }
        return DnsSecurityResult(servers, privateResolver, null,
            listOf("Configured DNS servers: " + (servers.ifEmpty { listOf("Unavailable") }.joinToString()), "Encrypted DNS transport cannot be confirmed from resolver addresses alone."),
            listOf("Prefer a trusted resolver.", "Use Private DNS / DNS-over-TLS when supported.", "Treat encrypted transport as REVIEW until observed or configured explicitly."))
    }
}