package com.uttarooque73.netguard.monitor

import android.content.Context

data class ScheduledMonitorConfig(
    val enabled: Boolean = false,
    val intervalHours: Long = 6L,
    val notifyOnChanges: Boolean = true
) {
    val intervalMinutes: Long
        get() = intervalHours.coerceIn(1L, 168L) * 60L
}

class ScheduledMonitorConfigStore(context: Context) {
    private val preferences = context.getSharedPreferences("netguard_scheduled_monitor", Context.MODE_PRIVATE)

    fun load(): ScheduledMonitorConfig = ScheduledMonitorConfig(
        enabled = preferences.getBoolean("enabled", false),
        intervalHours = preferences.getLong("intervalHours", 6L).coerceIn(1L, 168L),
        notifyOnChanges = preferences.getBoolean("notifyOnChanges", true)
    )

    fun save(config: ScheduledMonitorConfig) {
        preferences.edit()
            .putBoolean("enabled", config.enabled)
            .putLong("intervalHours", config.intervalHours.coerceIn(1L, 168L))
            .putBoolean("notifyOnChanges", config.notifyOnChanges)
            .apply()
    }
}