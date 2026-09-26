package com.uttarooque73.netguard.monitor

import org.junit.Assert.assertEquals
import org.junit.Test

class ScheduledMonitorConfigTest {
    @Test
    fun intervalIsClampedToSupportedWindow() {
        assertEquals(60L, ScheduledMonitorConfig(intervalHours = 1).intervalMinutes)
        assertEquals(10080L, ScheduledMonitorConfig(intervalHours = 168).intervalMinutes)
        assertEquals(60L, ScheduledMonitorConfig(intervalHours = 0).intervalMinutes)
        assertEquals(10080L, ScheduledMonitorConfig(intervalHours = 999).intervalMinutes)
    }
}