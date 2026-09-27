package com.uttarooque73.netguard.features.securitycenter

import android.content.Context

class SecurityWatchdogStore(context:Context) {
    private val prefs=context.getSharedPreferences("netguard_security_watchdog",Context.MODE_PRIVATE)
    fun state():SecurityWatchState=SecurityWatchState(prefs.getBoolean("enabled",false),prefs.getInt("interval",60),prefs.getLong("lastRun",0L))
    fun enable(intervalMinutes:Int=60){prefs.edit().putBoolean("enabled",true).putInt("interval",intervalMinutes.coerceAtLeast(60)).apply()}
    fun disable(){prefs.edit().putBoolean("enabled",false).apply()}
    fun markRun(){prefs.edit().putLong("lastRun",System.currentTimeMillis()).apply()}
}
