package com.uttarooque73.netguard.features.web

import org.junit.Assert.assertTrue
import org.junit.Test

class WebSecurityPolicyTest {
    @Test fun missingSecurityHeadersProduceFindings() {
        val findings = WebSecurityPolicy.evaluate("https://example.test", emptyMap())
        assertTrue(findings.any { it.id == "WEB-HSTS-001" })
        assertTrue(findings.any { it.id == "WEB-CSP-001" })
    }

    @Test fun insecureCookieFlagsAreDetected() {
        val findings = WebSecurityPolicy.evaluate(
            "https://example.test",
            mapOf("Set-Cookie" to "session=abc"),
        )
        assertTrue(findings.any { it.id == "WEB-COOKIE-SECURE-1" })
        assertTrue(findings.any { it.id == "WEB-COOKIE-HTTPONLY-1" })
        assertTrue(findings.any { it.id == "WEB-COOKIE-SAMESITE-1" })
    }
}