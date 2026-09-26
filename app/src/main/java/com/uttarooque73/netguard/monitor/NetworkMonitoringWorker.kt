package com.uttarooque73.netguard.monitor

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.uttarooque73.netguard.audit.ServiceAudit
import com.uttarooque73.netguard.network.DeviceDiscovery
import com.uttarooque73.netguard.network.NetworkDiscovery

class NetworkMonitoringWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return runCatching {
            val network = NetworkDiscovery(applicationContext).inspect()
            val localIp = network.localAddress ?: return Result.success()
            val subnet = network.subnet ?: return Result.success()
            val prefix = subnet.substringAfterLast('/').toIntOrNull() ?: return Result.success()

            val devices = DeviceDiscovery().discover(localIp, prefix)
            val services = devices.flatMap { ServiceAudit().audit(it.ipAddress) }

            val baselineStore = MonitorBaselineStore(applicationContext)
            val monitorStore = MonitorStore(applicationContext)

            val previousDevices = baselineStore.loadDeviceIps()
                .map { com.uttarooque73.netguard.network.DiscoveredDevice(it, reachable = true) }
            val previousServices = baselineStore.loadServices()

            val events = MonitorRunner.check(
                previousDevices = previousDevices,
                currentDevices = devices,
                previousServices = previousServices,
                currentServices = services
            )

            if (events.isNotEmpty()) {
                val history = (monitorStore.load() + events).takeLast(100)
                monitorStore.save(history)
            }

            baselineStore.saveDevices(devices)
            baselineStore.saveServices(services)
            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }
}
