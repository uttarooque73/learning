package com.uttarooque73.netguard.report

import com.uttarooque73.netguard.audit.DiscoveredService
import com.uttarooque73.netguard.features.policy.CustomPolicyEvaluation
import com.uttarooque73.netguard.audit.Finding
import com.uttarooque73.netguard.network.DiscoveredDevice
import com.uttarooque73.netguard.network.NetworkInfo
import com.uttarooque73.netguard.remediation.RemediationRecord
import com.uttarooque73.netguard.verification.VerificationResult

data class AuditSnapshot(
    val id: String,
    val createdAtEpochMs: Long,
    val network: NetworkInfo?,
    val devices: List<DiscoveredDevice>,
    val services: List<DiscoveredService>,
    val findings: List<Finding>,
    val remediationRecords: List<RemediationRecord>,
    val verificationResults: List<VerificationResult>,
    val customPolicyEvaluations: List<CustomPolicyEvaluation> = emptyList()
)

import com.uttarooque73.netguard.audit.FindingSeverity

data class AuditSnapshotDiff(
    val addedDevices: List<String>,
    val removedDevices: List<String>,
    val addedServices: List<String>,
    val removedServices: List<String>,
    val newFindingIds: List<String>,
    val resolvedFindingIds: List<String>,
    val riskScoreBefore: Int,
    val riskScoreAfter: Int
) {
    val changed: Boolean
        get() = addedDevices.isNotEmpty() || removedDevices.isNotEmpty() ||
            addedServices.isNotEmpty() || removedServices.isNotEmpty() ||
            newFindingIds.isNotEmpty() || resolvedFindingIds.isNotEmpty() ||
            riskScoreBefore != riskScoreAfter
}

object AuditSnapshotDiffCalculator {
    fun compare(before: AuditSnapshot, after: AuditSnapshot): AuditSnapshotDiff {
        val beforeDevices = before.devices.map { it.ipAddress }.toSet()
        val afterDevices = after.devices.map { it.ipAddress }.toSet()
        val beforeServices = before.services.map { key(it.ipAddress, it.protocol, it.port) }.toSet()
        val afterServices = after.services.map { key(it.ipAddress, it.protocol, it.port) }.toSet()
        val beforeFindings = before.findings.map { it.id + "|" + it.ipAddress }.toSet()
        val afterFindings = after.findings.map { it.id + "|" + it.ipAddress }.toSet()
        return AuditSnapshotDiff(
            addedDevices = (afterDevices - beforeDevices).sorted(),
            removedDevices = (beforeDevices - afterDevices).sorted(),
            addedServices = (afterServices - beforeServices).sorted(),
            removedServices = (beforeServices - afterServices).sorted(),
            newFindingIds = (afterFindings - beforeFindings).sorted(),
            resolvedFindingIds = (beforeFindings - afterFindings).sorted(),
            riskScoreBefore = riskScore(before),
            riskScoreAfter = riskScore(after)
        )
    }

    private fun riskScore(snapshot: AuditSnapshot): Int =
        (100 - snapshot.findings.sumOf {
            when (it.severity) {
                FindingSeverity.CRITICAL -> 40
                FindingSeverity.HIGH -> 25
                FindingSeverity.MEDIUM -> 12
                FindingSeverity.LOW -> 4
            }
        }).coerceIn(0, 100)

    private fun key(ip: String, protocol: String, port: Int): String =
        ip + "|" + protocol.uppercase() + "|" + port
}
