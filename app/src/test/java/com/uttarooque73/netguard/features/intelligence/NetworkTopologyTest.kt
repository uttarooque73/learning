package com.uttarooque73.netguard.features.intelligence

import com.uttarooque73.netguard.network.DiscoveredDevice
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkTopologyTest {
    @Test fun createsGatewayDeviceAndServiceNodes() {
        val topology = NetworkTopologyBuilder.build("192.168.1.1", listOf(DiscoveredDevice("192.168.1.2")), emptyList())
        assertTrue(topology.nodes.any { it.type == "GATEWAY" })
        assertTrue(topology.nodes.any { it.type == "DEVICE" })
    }
}