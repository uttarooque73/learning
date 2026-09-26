package com.uttarooque73.netguard.monitor

import com.uttarooque73.netguard.audit.DiscoveredService
import com.uttarooque73.netguard.network.DiscoveredDevice

object MonitorRunner {
    fun check(
        previousDevices: List<DiscoveredDevice>,
        currentDevices: List<DiscoveredDevice>,
        previousServices: List<DiscoveredService>,
        currentServices: List<DiscoveredService>
    ): List<MonitorEvent> =
        MonitorEngine.compareDevices(previousDevices, currentDevices) +
            MonitorEngine.compareServices(previousServices, currentServices)
}