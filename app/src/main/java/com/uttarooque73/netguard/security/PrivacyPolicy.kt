package com.uttarooque73.netguard.security

object PrivacyPolicy {
    const val LOCAL_STORAGE_ONLY = true
    const val NO_CREDENTIAL_COLLECTION = true
    const val NO_REMOTE_DEVICE_CONTROL = true
    const val NO_BACKGROUND_ACTIVITY_BY_DEFAULT = true

    fun dataStoredLocally(): List<String> = listOf(
        "network inventory",
        "service reachability",
        "security findings",
        "remediation state",
        "verification results",
        "audit history",
        "monitoring events",
        "administrative metadata"
    )
}
