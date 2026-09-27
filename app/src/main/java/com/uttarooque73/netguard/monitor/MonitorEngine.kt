package com.uttarooque73.netguard.monitor

import com.uttarooque73.netguard.audit.DiscoveredService
import com.uttarooque73.netguard.network.DiscoveredDevice
import com.uttarooque73.netguard.network.NetworkInfo
import java.util.UUID

object MonitorEngine {
    fun compareNetwork(previous: NetworkInfo?, current: NetworkInfo): List<MonitorEvent> {
        if (previous == null || sameNetwork(previous, current)) return emptyList()
        return listOf(
            MonitorEvent(
                UUID.randomUUID().toString(),
                MonitorEventType.NETWORK_CHANGED,
                current.gatewayAddress ?: current.localAddress ?: "network",
                "Network context changed: interface/local/gateway/subnet/Wi-Fi identity differs"
            )
        )
    }

    fun compareDevices(previous: List<DiscoveredDevice>, current: List<DiscoveredDevice>): List<MonitorEvent> {
        val oldIps = previous.map { it.ipAddress }.toSet()
        return current.filter { it.ipAddress !in oldIps }.map {
            MonitorEvent(UUID.randomUUID().toString(), MonitorEventType.NEW_DEVICE, it.ipAddress, "New device discovered: ${it.hostname ?: "unknown hostname"}")
        }
    }

    fun compareServices(previous: List<DiscoveredService>, current: List<DiscoveredService>): List<MonitorEvent> {
        val oldServices = previous.map { key(it) }.toSet()
        val currentServices = current.map { key(it) }.toSet()
        val added = current.filter { key(it) !in oldServices }.map {
            MonitorEvent(UUID.randomUUID().toString(), MonitorEventType.NEW_SERVICE, it.ipAddress, "New reachable service: TCP/${it.port} ${it.serviceName}")
        }
        val removed = previous.filter { key(it) !in currentServices }.map {
            MonitorEvent(UUID.randomUUID().toString(), MonitorEventType.SERVICE_REMOVED, it.ipAddress, "Previously observed service is no longer reachable: TCP/${it.port} ${it.serviceName}")
        }
        return added + removed
    }

    private fun key(service: DiscoveredService): String = "${service.ipAddress}|${service.protocol.uppercase()}|${service.port}"

    private fun sameNetwork(previous: NetworkInfo, current: NetworkInfo): Boolean =
        previous.interfaceName == current.interfaceName &&
            previous.localAddress == current.localAddress &&
            previous.gatewayAddress == current.gatewayAddress &&
            previous.subnet == current.subnet &&
            previous.ssid == current.ssid &&
            previous.bssid == current.bssid
}