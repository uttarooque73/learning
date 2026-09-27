package com.uttarooque73.netguard.monitor

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ScheduledMonitorScheduler {
    private const val WORK_NAME = "netguard-scheduled-monitor"

    fun apply(context: Context, config: ScheduledMonitorConfig) {
        val manager = WorkManager.getInstance(context)
        if (!config.enabled) {
            manager.cancelUniqueWork(WORK_NAME)
            return
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<ScheduledMonitorWorker>(
            config.intervalHours.coerceIn(1L, 168L),
            TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        manager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun disable(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
