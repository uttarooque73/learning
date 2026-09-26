package com.uttarooque73.netguard.security

data class ReleaseCheck(
    val id: String,
    val title: String,
    val passed: Boolean,
    val detail: String
)

object ReleaseChecks {
    fun evaluate(): List<ReleaseCheck> {
        return listOf(
            ReleaseCheck(
                id = "REL-001",
                title = "No credential collection",
                passed = PrivacyPolicy.NO_CREDENTIAL_COLLECTION,
                detail = "The MVP does not collect passwords or authentication secrets."
            ),
            ReleaseCheck(
                id = "REL-002",
                title = "No remote device control",
                passed = PrivacyPolicy.NO_REMOTE_DEVICE_CONTROL,
                detail = "Remediation remains guided and does not modify remote devices."
            ),
            ReleaseCheck(
                id = "REL-003",
                title = "Local-first storage",
                passed = PrivacyPolicy.LOCAL_STORAGE_ONLY,
                detail = "Security inventory and audit data remain on-device."
            ),
            ReleaseCheck(
                id = "REL-004",
                title = "No background activity by default",
                passed = PrivacyPolicy.NO_BACKGROUND_ACTIVITY_BY_DEFAULT,
                detail = "Monitoring requires an explicit user-triggered check."
            )
        )
    }
}
