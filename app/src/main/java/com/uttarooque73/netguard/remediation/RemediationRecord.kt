package com.uttarooque73.netguard.remediation

data class RemediationRecord(
    val findingId: String,
    val ipAddress: String,
    val status: RemediationStatus,
    val startedAtEpochMs: Long = System.currentTimeMillis()
)

enum class RemediationStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
