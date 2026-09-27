package com.uttarooque73.netguard.features.web

import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.URI
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

data class TlsAuditResult(
    val url: String,
    val reachable: Boolean,
    val protocol: String?,
    val cipherSuite: String?,
    val certificateSubject: String?,
    val certificateIssuer: String?,
    val expiresAtEpochMs: Long?,
    val hostnameVerified: Boolean?,
    val certificateExpired: Boolean? = null,
    val daysUntilExpiry: Long? = null,
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
        val uri = runCatching { URI(url) }.getOrElse { error("Invalid URL.") }
        require(uri.scheme.equals("https", true) && !uri.host.isNullOrBlank()) { "TLS audit requires a valid HTTPS URL." }
        require(uri.userInfo == null && uri.fragment == null) { "URL user information/fragments are not supported." }
        val connection = uri.toURL().openConnection() as HttpsURLConnection
        return try {
            connection.connectTimeout = timeoutMs
            connection.readTimeout = timeoutMs
            connection.instanceFollowRedirects = false
            connection.requestMethod = "GET"
            connection.connect()
            val cert = connection.serverCertificates.firstOrNull() as? java.security.cert.X509Certificate
            val socketEvidence = runCatching { inspectTlsSocket(uri.host, uri.port.takeIf { it > 0 } ?: 443, timeoutMs) }.getOrNull()
            val now = System.currentTimeMillis()
            val daysUntilExpiry = cert?.let { ((it.notAfter.time - now) / 86_400_000L) }
            val certificateExpired = cert?.let { it.notAfter.time <= now }
            val evidence = buildList {
                add("HTTPS endpoint reachable.")
                certificateExpired?.let { add(if (it) "Certificate is expired." else "Certificate is currently within its validity period.") }
                daysUntilExpiry?.let { add("Certificate expires in approximately $it day(s).") }
                add("Negotiated cipher suite: " + (socketEvidence?.cipherSuite ?: connection.cipherSuite))
                add("TLS protocol: " + (socketEvidence?.protocol ?: "not available"))
                cert?.let {
                    add("Certificate subject: " + it.subjectX500Principal.name)
                    add("Certificate issuer: " + it.issuerX500Principal.name)
                    add("Certificate expiry: " + it.notAfter.time)
                }
            }
            TlsAuditResult(
                url = url,
                reachable = true,
                protocol = socketEvidence?.protocol,
                cipherSuite = socketEvidence?.cipherSuite ?: connection.cipherSuite,
                certificateSubject = cert?.subjectX500Principal?.name,
                certificateIssuer = cert?.issuerX500Principal?.name,
                expiresAtEpochMs = cert?.notAfter?.time,
                hostnameVerified = true,
                certificateExpired = certificateExpired,
                daysUntilExpiry = daysUntilExpiry,
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

    private fun inspectTlsSocket(host: String, port: Int, timeoutMs: Int): TlsSocketEvidence {
        val factory = SSLSocketFactory.getDefault() as SSLSocketFactory
        val socket = factory.createSocket() as SSLSocket
        return socket.use {
            it.connect(InetSocketAddress(host, port), timeoutMs)
            it.soTimeout = timeoutMs
            it.sslParameters = it.sslParameters.apply { endpointIdentificationAlgorithm = "HTTPS" }
            it.startHandshake()
            TlsSocketEvidence(it.session.protocol, it.session.cipherSuite)
        }
    }

    private data class TlsSocketEvidence(val protocol: String, val cipherSuite: String)

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
                .mapKeys { it.key!!.trim().lowercase() }
                .mapValues { it.value.joinToString(", ") }
            val setCookies = connection.headerFields.entries
                .filter { it.key?.equals("set-cookie", ignoreCase = true) == true }
                .flatMap { it.value.orEmpty() }
            val insecureCookies = setCookies.count { !it.contains("; Secure", ignoreCase = true) }
            val missingHttpOnly = setCookies.count { !it.contains("; HttpOnly", ignoreCase = true) }
            val missingSameSite = setCookies.count { !it.contains("SameSite=", ignoreCase = true) }
            val httpsRedirect = location?.let {
                runCatching { URI(it).scheme.equals("https", ignoreCase = true) }.getOrDefault(false)
            } ?: false
            HttpSecurityResult(
                url = url,
                reachable = true,
                statusCode = connection.responseCode,
                redirectsToHttps = if (url.startsWith("http://", true)) httpsRedirect else null,
                headers = headers,
                evidence = listOf(
                    "HTTP status: " + connection.responseCode,
                    "HSTS: " + (headers["strict-transport-security"] ?: "not observed"),
                    "CSP: " + (headers["content-security-policy"] ?: "not observed"),
                    "X-Content-Type-Options: " + (headers["x-content-type-options"] ?: "not observed"),
                    "Referrer-Policy: " + (headers["referrer-policy"] ?: "not observed"),
                    "Set-Cookie headers observed: " + setCookies.size,
                    "Cookies without Secure attribute: " + insecureCookies,
                    "Cookies without HttpOnly attribute: " + missingHttpOnly,
                    "Cookies without SameSite attribute: " + missingSameSite
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