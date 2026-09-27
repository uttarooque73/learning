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
    @Test fun detectsNetworkContextChange() {
        val previous = com.uttarooque73.netguard.network.NetworkInfo(
            "wlan0", "192.168.1.10", "192.168.1.1", "192.168.1.0/24",
            listOf("192.168.1.1"), "Home", "aa:bb:cc:dd:ee:ff", "WPA2"
        )
        val current = previous.copy(gatewayAddress = "192.168.2.1", subnet = "192.168.2.0/24")
        val events = MonitorEngine.compareNetwork(previous, current)
        assertEquals(MonitorEventType.NETWORK_CHANGED, events.single().type)
    }

    @Test fun ignoresEquivalentNetworkContext() {
        val network = com.uttarooque73.netguard.network.NetworkInfo(
            "wlan0", "192.168.1.10", "192.168.1.1", "192.168.1.0/24",
            listOf("192.168.1.1"), "Home", "aa:bb:cc:dd:ee:ff", "WPA2"
        )
        assertEquals(0, MonitorEngine.compareNetwork(network, network.copy()).size)
    }

    @Test fun detectsDeviceMetadataChange() {
        val old = listOf(DiscoveredDevice("192.168.1.20", "old", true))
        val current = listOf(DiscoveredDevice("192.168.1.20", "new", true))
        assertEquals(MonitorEventType.DEVICE_CHANGED, MonitorEngine.compareDevices(old, current).single().type)
    }

    @Test fun detectsServiceMetadataChange() {
        val old = listOf(DiscoveredService("192.168.1.20", 80, serviceName = "HTTP", reachable = true))
        val current = listOf(DiscoveredService("192.168.1.20", 80, serviceName = "Web", reachable = true))
        assertEquals(MonitorEventType.SERVICE_CHANGED, MonitorEngine.compareServices(old, current).single().type)
    }
}