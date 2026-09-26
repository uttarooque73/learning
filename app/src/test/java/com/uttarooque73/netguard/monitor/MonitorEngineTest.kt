package com.uttarooque73.netguard.monitor

import com.uttarooque73.netguard.audit.DiscoveredService
import com.uttarooque73.netguard.network.DiscoveredDevice
import org.junit.Assert.assertEquals
import org.junit.Test

class MonitorEngineTest {
    @Test fun detectsNewDevice() {
        val events = MonitorEngine.compareDevices(emptyList(), listOf(DiscoveredDevice("192.168.1.20", "printer", true)))
        assertEquals(MonitorEventType.NEW_DEVICE, events.single().type)
    }

    @Test fun detectsAddedAndRemovedServices() {
        val old = listOf(DiscoveredService("192.168.1.20", 80, serviceName = "HTTP", reachable = true))
        val current = listOf(DiscoveredService("192.168.1.20", 443, serviceName = "HTTPS", reachable = true))
        val events = MonitorEngine.compareServices(old, current)
        assertEquals(2, events.size)
    }
}