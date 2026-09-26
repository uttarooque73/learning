package com.uttarooque73.netguard.security

import android.content.Context

class AppLockPolicyStore(context: Context) {
    private val preferences = context.getSharedPreferences("netguard_app_lock", Context.MODE_PRIVATE)

    fun load(): AppLockPolicy = AppLockPolicy(
        enabled = preferences.getBoolean("enabled", false),
        lockOnBackground = preferences.getBoolean("lockOnBackground", true),
        requireBiometric = preferences.getBoolean("requireBiometric", true)
    )

    fun save(policy: AppLockPolicy) {
        preferences.edit()
            .putBoolean("enabled", policy.enabled)
            .putBoolean("lockOnBackground", policy.lockOnBackground)
            .putBoolean("requireBiometric", policy.requireBiometric)
            .apply()
    }
}