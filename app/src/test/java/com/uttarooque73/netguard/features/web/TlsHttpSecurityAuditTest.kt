package com.uttarooque73.netguard.features.web

import org.junit.Assert.assertTrue
import org.junit.Test

class TlsHttpSecurityAuditTest {
    @Test
    fun tlsRequiresHttps() {
        val failed = runCatching { TlsHttpSecurityAudit.inspectTls("http://example.com") }.isFailure
        assertTrue(failed)
    }
}
