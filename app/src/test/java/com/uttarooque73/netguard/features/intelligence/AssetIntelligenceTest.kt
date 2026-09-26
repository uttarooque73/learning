package com.uttarooque73.netguard.features.intelligence

import com.uttarooque73.netguard.audit.DiscoveredService
import com.uttarooque73.netguard.network.DiscoveredDevice
import org.junit.Assert.assertEquals
import org.junit.Test

class AssetIntelligenceTest {
    @Test fun classifiesWebDevice() {
        val device = DiscoveredDevice("192.168.1.20", "server")
        val services = listOf(DiscoveredService("192.168.1.20", 443, serviceName = "HTTPS", reachable = true))
        assertEquals("Web-capable device", AssetIntelligence.fingerprint(device, services).deviceClass)
    }
}