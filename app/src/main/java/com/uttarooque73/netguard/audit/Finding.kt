package com.uttarooque73.netguard.audit

enum class FindingSeverity { LOW, MEDIUM, HIGH, CRITICAL }
enum class FindingConfidence { LOW, MEDIUM, HIGH }

data class Finding(
    val id: String,
    val title: String,
    val severity: FindingSeverity,
    val confidence: FindingConfidence,
    val ipAddress: String,
    val evidence: String,
    val explanation: String,
    val remediation: String,
    val verification: String,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)
