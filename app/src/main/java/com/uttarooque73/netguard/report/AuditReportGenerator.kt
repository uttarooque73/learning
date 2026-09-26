package com.uttarooque73.netguard.report

import com.uttarooque73.netguard.audit.RiskCalculator

object AuditReportGenerator {
    fun generate(snapshot: AuditSnapshot): String {
        val findings = snapshot.findings
        val risk = RiskCalculator.score(findings)
        val lines = mutableListOf<String>()
        lines += "NETGUARD SECURITY AUDIT REPORT"
        lines += "Audit ID: ${snapshot.id}"
        lines += "Created: ${snapshot.createdAtEpochMs}"
        lines += ""
        lines += "EXECUTIVE SUMMARY"
        lines += "Risk score: ${risk}/100"
        lines += "Devices: ${snapshot.devices.size}"
        lines += "Services: ${snapshot.services.size}"
        lines += "Findings: ${findings.size}"
        lines += "Critical: ${findings.count { it.severity.name == "CRITICAL" }}"
        lines += "High: ${findings.count { it.severity.name == "HIGH" }}"
        lines += "Medium: ${findings.count { it.severity.name == "MEDIUM" }}"
        lines += "Low: ${findings.count { it.severity.name == "LOW" }}"
        lines += ""
        lines += "FINDINGS"
        findings.forEach { finding ->
            lines += "- ${finding.id}: ${finding.title}"
            lines += "  Severity: ${finding.severity}"
            lines += "  Confidence: ${finding.confidence}"
            lines += "  Asset: ${finding.ipAddress}"
            lines += "  Evidence: ${finding.evidence}"
            lines += "  Remediation: ${finding.remediation}"
            val verification = snapshot.verificationResults.lastOrNull { it.findingId == finding.id && it.ipAddress == finding.ipAddress }
            lines += "  Verification: ${verification?.status ?: "NOT_VERIFIED"}"
        }
        lines += ""
        lines += "REMEDIATION HISTORY"
        snapshot.remediationRecords.forEach { record ->
            lines += "- ${record.findingId} @ ${record.ipAddress}: ${record.status}"
        }
        return lines.joinToString("\n")
    }
}