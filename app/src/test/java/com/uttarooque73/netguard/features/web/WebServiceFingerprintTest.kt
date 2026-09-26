package com.uttarooque73.netguard.features.web

import org.junit.Assert.assertEquals
import org.junit.Test

class WebServiceFingerprintTest {
    @Test fun extractsServerEvidence() {
        val result = WebServiceFingerprintEngine.from("https://example.test", mapOf("Server" to "example-server/1.2"))
        assertEquals("example-server/1.2", result.server)
        assertEquals("MEDIUM", result.confidence)
    }
}