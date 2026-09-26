package com.uttarooque73.netguard.core.model

enum class Severity { INFO, LOW, MEDIUM, HIGH, CRITICAL }

enum class FindingStatus { OPEN, REMEDIATION_PENDING, FIXED, STILL_PRESENT, UNABLE_TO_VERIFY }

data class NetworkAsset(
    val id: String,
    val address: String,
    val hostname: String? = null,
    val displayName: String? = null
)

data class Finding(
    val id: String,
    val title: String,
    val severity: Severity,
    val assetId: String,
    val evidence: String,
    val explanation: String,
    val remediation: List<String> = emptyList(),
    val verification: String? = null,
    val status: FindingStatus = FindingStatus.OPEN
)

data class AuditSummary(
    val totalAssets: Int = 0,
    val openFindings: Int = 0,
    val highSeverityFindings: Int = 0,
    val lastAuditEpochMillis: Long? = null
)
