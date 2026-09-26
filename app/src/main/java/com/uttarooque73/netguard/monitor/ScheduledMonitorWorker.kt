package com.uttarooque73.netguard.monitor

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.uttarooque73.netguard.network.DeviceDiscovery
import com.uttarooque73.netguard.network.NetworkInventoryStore
import com.uttarooque73.netguard.security.NotificationHelper
import com.uttarooque73.netguard.audit.ServiceAudit

class ScheduledMonitorWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val context = applicationContext
        val config = ScheduledMonitorConfigStore(context).load()
        if (!config.enabled) return Result.success()
        val locationGranted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        if (!locationGranted) return Result.failure()
        return runCatching {
            val inventory = NetworkInventoryStore(context)
            val network = inventory.loadNetwork() ?: return@runCatching 0
            val localIp = network.localAddress ?: return@runCatching 0
            val prefix = network.subnet?.substringAfter('/')?.toIntOrNull() ?: return@runCatching 0
            val currentDevices = DeviceDiscovery().discover(localIp, prefix)
            val currentServices = currentDevices.flatMap { ServiceAudit().audit(it.ipAddress) }
            val baseline = MonitorBaselineStore(context)
            val previousDevices = baseline.loadDeviceIps().map {
                com.uttarooque73.netguard.network.DiscoveredDevice(it, reachable = true)
            }
            val previousServices = baseline.loadServices()
            val events = MonitorRunner.check(previousDevices, currentDevices, previousServices, currentServices)
            MonitorStore(context).let { store -> store.save((store.load() + events).takeLast(100)) }
            inventory.saveDevices(currentDevices)
            baseline.saveDevices(currentDevices)
            baseline.saveServices(currentServices)
            if (config.notifyOnChanges && events.isNotEmpty()) {
                val first = events.first()
                NotificationHelper.notifyFinding(
                    context,
                    "NetGuard: network changed",
                    events.size.toString() + " inventory change(s), including " + first.type.name
                )
            }
            events.size
        }.fold({ Result.success() }, { Result.retry() })
    }
}