package com.uttarooque73.netguard.features.web

data class WebServiceFingerprint(val url: String, val server: String?, val poweredBy: String?, val versionEvidence: String?, val confidence: String)

object WebServiceFingerprintEngine {
    fun from(url: String, headers: Map<String, String>): WebServiceFingerprint {
        val normalized = headers.mapKeys { it.key.lowercase() }
        val server = normalized["server"]
        val poweredBy = normalized["x-powered-by"]
        val evidence = listOfNotNull(server?.let { "Server header: $it" }, poweredBy?.let { "X-Powered-By header: $it" })
        return WebServiceFingerprint(url, server, poweredBy, evidence.takeIf { it.isNotEmpty() }?.joinToString("; "), if (evidence.isEmpty()) "LOW" else "MEDIUM")
    }
}