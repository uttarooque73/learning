package com.uttarooque73.netguard.monitor

import android.content.Context

/**
 * Compatibility facade for scheduled monitoring.
 *
 * Monitoring is always delegated to the user-configured scheduler so there is
 * no legacy fixed-frequency background scan path.
 */
object MonitoringScheduler {
    fun enable(context: Context, intervalHours: Long = 6L) {
        val configStore = ScheduledMonitorConfigStore(context)
        val config = configStore.load().copy(
            enabled = true,
            intervalHours = intervalHours.coerceIn(1L, 168L)
        )
        configStore.save(config)
        ScheduledMonitorScheduler.apply(context, config)
    }

    fun disable(context: Context) {
        ScheduledMonitorScheduler.disable(context)
        ScheduledMonitorConfigStore(context).save(
            ScheduledMonitorConfig(enabled = false)
        )
    }
}
