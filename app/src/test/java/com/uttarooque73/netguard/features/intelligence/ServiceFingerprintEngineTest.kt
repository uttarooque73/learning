package com.uttarooque73.netguard.features.intelligence

import com.uttarooque73.netguard.audit.DiscoveredService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ServiceFingerprintEngineTest {
    @Test fun fingerprintsKnownPortWithoutOverclaimingVersion() {
        val result = ServiceFingerprintEngine.fingerprint(
            DiscoveredService("192.168.1.10", 443, serviceName = "HTTPS", reachable = true)
        )
        assertEquals("MEDIUM", result.confidence)
        assertTrue(result.productCandidates.contains("nginx"))
        assertEquals(null, result.versionEvidence)
    }
}