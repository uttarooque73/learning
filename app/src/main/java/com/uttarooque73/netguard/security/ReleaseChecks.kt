package com.uttarooque73.netguard.security

data class ReleaseCheck(
    val id: String,
    val title: String,
    val passed: Boolean,
    val detail: String
)

object ReleaseChecks {
    fun evaluate(): List<ReleaseCheck> = listOf(
        ReleaseCheck("REL-001", "No credential collection", PrivacyPolicy.NO_CREDENTIAL_COLLECTION, "The MVP does not collect passwords or authentication secrets."),
        ReleaseCheck("REL-002", "No remote device control", PrivacyPolicy.NO_REMOTE_DEVICE_CONTROL, "Remediation remains guided and does not modify remote devices."),
        ReleaseCheck("REL-003", "Local-first storage", PrivacyPolicy.LOCAL_STORAGE_ONLY, "Security inventory and audit data remain on-device."),
        ReleaseCheck("REL-004", "No background activity by default", PrivacyPolicy.NO_BACKGROUND_ACTIVITY_BY_DEFAULT, "Monitoring requires an explicit user-triggered check.")
    )
)
