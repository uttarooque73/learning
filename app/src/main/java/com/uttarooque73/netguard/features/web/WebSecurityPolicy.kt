package com.uttarooque73.netguard.features.web

data class WebSecurityFinding(
    val id: String,
    val severity: String,
    val evidence: String,
    val remediation: String
)

object WebSecurityPolicy {
    fun evaluate(
        url: String,
        headers: Map<String, String>,
        redirectsToHttps: Boolean? = null
    ): List<WebSecurityFinding> {
        val h = headers.mapKeys { it.key.lowercase() }
        val findings = mutableListOf<WebSecurityFinding>()
        if (url.startsWith("http://", ignoreCase = true) && redirectsToHttps == false) {
            findings += WebSecurityFinding(
                "WEB-HTTPS-001", "HIGH",
                "HTTP endpoint did not redirect to HTTPS.",
                "Redirect HTTP requests to HTTPS and use HSTS after HTTPS is correctly deployed."
            )
        }
        if (url.startsWith("https://", ignoreCase = true) && !h.containsKey("strict-transport-security")) {
            findings += WebSecurityFinding(
                "WEB-HSTS-001", "MEDIUM",
                "Strict-Transport-Security header was not observed.",
                "Deploy HSTS after confirming the site is fully HTTPS-capable."
            )
        }
        if (!h.containsKey("content-security-policy")) {
            findings += WebSecurityFinding(
                "WEB-CSP-001", "MEDIUM",
                "Content-Security-Policy header was not observed.",
                "Define a restrictive Content-Security-Policy appropriate for the application."
            )
        }
        if (!h["x-content-type-options"].orEmpty().equals("nosniff", ignoreCase = true)) {
            findings += WebSecurityFinding(
                "WEB-MIME-001", "LOW",
                "X-Content-Type-Options: nosniff was not observed.",
                "Set X-Content-Type-Options to nosniff."
            )
        }
        if (!h.containsKey("referrer-policy")) {
            findings += WebSecurityFinding(
                "WEB-REFERRER-001", "LOW",
                "Referrer-Policy header was not observed.",
                "Set an explicit Referrer-Policy appropriate for the application."
            )
        }
        h["set-cookie"]?.let { listOf(it) }.orEmpty().forEachIndexed { index, cookie ->
            if (!cookie.contains("Secure", ignoreCase = true)) {
                findings += WebSecurityFinding(
                    "WEB-COOKIE-SECURE-" + (index + 1), "MEDIUM",
                    "A Set-Cookie value lacks the Secure attribute.",
                    "Set Secure on cookies that must only travel over HTTPS."
                )
            }
            if (!cookie.contains("HttpOnly", ignoreCase = true)) {
                findings += WebSecurityFinding(
                    "WEB-COOKIE-HTTPONLY-" + (index + 1), "LOW",
                    "A Set-Cookie value lacks the HttpOnly attribute.",
                    "Set HttpOnly on cookies that do not need JavaScript access."
                )
            }
            if (!cookie.contains("SameSite=", ignoreCase = true)) {
                findings += WebSecurityFinding(
                    "WEB-COOKIE-SAMESITE-" + (index + 1), "LOW",
                    "A Set-Cookie value lacks an explicit SameSite attribute.",
                    "Set an appropriate SameSite value."
                )
            }
        }
        return findings
    }
}
