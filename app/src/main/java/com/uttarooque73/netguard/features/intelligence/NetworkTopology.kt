package com.uttarooque73.netguard.features.intelligence

import com.uttarooque73.netguard.audit.DiscoveredService
import com.uttarooque73.netguard.network.DiscoveredDevice

data class TopologyNode(val id: String, val label: String, val type: String)
data class TopologyEdge(val from: String, val to: String, val relation: String)
data class NetworkTopology(val nodes: List<TopologyNode>, val edges: List<TopologyEdge>)

object NetworkTopologyBuilder {
    fun build(gateway: String?, devices: List<DiscoveredDevice>, services: List<DiscoveredService>): NetworkTopology {
        val nodes = mutableListOf<TopologyNode>()
        val edges = mutableListOf<TopologyEdge>()
        gateway?.let { nodes += TopologyNode(it, "Gateway", "GATEWAY") }
        devices.forEach { device ->
            nodes += TopologyNode(device.ipAddress, device.hostname ?: device.ipAddress, "DEVICE")
            gateway?.let { edges += TopologyEdge(it, device.ipAddress, "LOCAL_NETWORK") }
        }
        services.forEach { service -> nodes += TopologyNode(service.ipAddress + ":" + service.port, service.serviceName, "SERVICE"); edges += TopologyEdge(service.ipAddress, service.ipAddress + ":" + service.port, "EXPOSES") }
        return NetworkTopology(nodes.distinctBy { it.id }, edges.distinct())
    }
}