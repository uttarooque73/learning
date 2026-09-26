package com.uttarooque73.netguard.features.web

import java.net.HttpURLConnection
import java.net.URI
import javax.net.ssl.HttpsURLConnection

data class TlsAuditResult(
    val url: String,
    val reachable: Boolean,
    val protocol: String?,
    val cipherSuite: String?,
    val certificateSubject: String?,
    val certificateIssuer: String?,
    val expiresAtEpochMs: Long?,
    val hostnameVerified: Boolean?,
    val evidence: List<String>,
    val remediation: List<String>
)

data class HttpSecurityResult(
    val url: String,
    val reachable: Boolean,
    val statusCode: Int?,
    val redirectsToHttps: Boolean?,
    val headers: Map<String, String>,
    val evidence: List<String>,
    val remediation: List<String>
)

object TlsHttpSecurityAudit {
    fun inspectTls(url: String, timeoutMs: Int = 3000): TlsAuditResult {
        require(url.startsWith("https://", ignoreCase = true)) { "TLS audit requires an HTTPS URL." }
        val connection = URI(url).toURL().openConnection() as HttpsURLConnection
        return try {
            connection.connectTimeout = timeoutMs
            connection.readTimeout = timeoutMs
            connection.requestMethod = "GET"
            connection.connect()
            val cert = connection.serverCertificates.firstOrNull() as? java.security.cert.X509Certificate
            val evidence = buildList {
                add("HTTPS endpoint reachable.")
                add("Negotiated cipher suite: " + connection.cipherSuite)
                add("TLS protocol: not exposed by Android HttpsURLConnection; verify server TLS configuration separately.")
                cert?.let {
                    add("Certificate subject: " + it.subjectX500Principal.name)
                    add("Certificate issuer: " + it.issuerX500Principal.name)
                    add("Certificate expiry: " + it.notAfter.time)
                }
            }
            TlsAuditResult(
                url = url,
                reachable = true,
                protocol = null,
                cipherSuite = connection.cipherSuite,
                certificateSubject = cert?.subjectX500Principal?.name,
                certificateIssuer = cert?.issuerX500Principal?.name,
                expiresAtEpochMs = cert?.notAfter?.time,
                hostnameVerified = true,
                evidence = evidence,
                remediation = listOf(
                    "Use a certificate issued for the requested hostname.",
                    "Keep certificates within their validity period.",
                    "Prefer modern TLS configurations supported by the server."
                )
            )
        } finally {
            connection.disconnect()
        }
    }

    fun inspectHttp(url: String, timeoutMs: Int = 3000): HttpSecurityResult {
        require(url.startsWith("http://", ignoreCase = true) || url.startsWith("https://", ignoreCase = true))
        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = timeoutMs
            connection.readTimeout = timeoutMs
            connection.instanceFollowRedirects = false
            connection.requestMethod = "GET"
            connection.connect()
            val location = connection.getHeaderField("Location")
            val headers = connection.headerFields
                .filterKeys { it != null }
                .mapKeys { it.key!! }
                .mapValues { it.value.joinToString(", ") }
            val httpsRedirect = location?.startsWith("https://", ignoreCase = true) ?: false
            HttpSecurityResult(
                url = url,
                reachable = true,
                statusCode = connection.responseCode,
                redirectsToHttps = if (url.startsWith("http://", true)) httpsRedirect else null,
                headers = headers,
                evidence = listOf(
                    "HTTP status: " + connection.responseCode,
                    "HSTS: " + (headers["Strict-Transport-Security"] ?: "not observed"),
                    "CSP: " + (headers["Content-Security-Policy"] ?: "not observed"),
                    "X-Content-Type-Options: " + (headers["X-Content-Type-Options"] ?: "not observed"),
                    "Referrer-Policy: " + (headers["Referrer-Policy"] ?: "not observed")
                ),
                remediation = listOf(
                    "Redirect HTTP traffic to HTTPS.",
                    "Enable HSTS after HTTPS is correctly deployed.",
                    "Add an appropriate Content-Security-Policy.",
                    "Set X-Content-Type-Options: nosniff.",
                    "Set an appropriate Referrer-Policy."
                )
            )
        } finally {
            connection.disconnect()
        }
    }
}