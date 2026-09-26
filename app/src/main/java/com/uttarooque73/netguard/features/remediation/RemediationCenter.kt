package com.uttarooque73.netguard.features.remediation

import com.uttarooque73.netguard.audit.Finding
import com.uttarooque73.netguard.remediation.RemediationCatalog

data class RemediationItem(
    val findingId: String,
    val title: String,
    val asset: String,
    val status: String,
    val steps: List<String>,
    val verification: String
)

object RemediationCenter {
    fun queue(findings: List<Finding>, statuses: Map<String, String> = emptyMap()): List<RemediationItem> =
        findings.map { finding ->
            val playbook = RemediationCatalog.forFinding(finding.id)
            RemediationItem(
                finding.id,
                finding.title,
                finding.ipAddress,
                statuses[finding.id] ?: "NOT_STARTED",
                playbook?.steps ?: listOf(finding.remediation),
                playbook?.verification ?: finding.verification
            )
        }
}