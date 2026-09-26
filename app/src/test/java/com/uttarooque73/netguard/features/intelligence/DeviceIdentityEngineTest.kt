package com.uttarooque73.netguard.features.intelligence

import com.uttarooque73.netguard.audit.DiscoveredService
import com.uttarooque73.netguard.network.DiscoveredDevice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceIdentityEngineTest {
    @Test
    fun smbAndWebServicesProduceHigherConfidenceIdentity() {
        val device = DiscoveredDevice("192.168.1.10", "server.local", true)
        val services = listOf(
            DiscoveredService("192.168.1.10", 445, serviceName = "SMB", reachable = true),
            DiscoveredService("192.168.1.10", 443, serviceName = "HTTPS", reachable = true)
        )
        val identity = DeviceIdentityEngine.identify(device, services)
        assertEquals("Windows / SMB-capable device", identity.deviceClass)
        assertEquals("HIGH", identity.confidence)
        assertTrue(identity.evidence.isNotEmpty())
    }
}