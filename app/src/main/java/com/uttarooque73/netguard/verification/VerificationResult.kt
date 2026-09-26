package com.uttarooque73.netguard.verification

enum class VerificationStatus {
    FIXED,
    STILL_PRESENT,
    CHANGED,
    UNABLE_TO_VERIFY
}

data class VerificationResult(
    val findingId: String,
    val ipAddress: String,
    val status: VerificationStatus,
    val beforeEvidence: String,
    val afterEvidence: String,
    val verifiedAtEpochMs: Long = System.currentTimeMillis()
)
