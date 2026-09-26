package com.uttarooque73.netguard.security

data class AppLockPolicy(val enabled: Boolean = false, val lockOnBackground: Boolean = true, val requireBiometric: Boolean = true)

object AppLockPolicyDefaults {
    fun secure(): AppLockPolicy = AppLockPolicy(enabled = false, lockOnBackground = true, requireBiometric = true)
}