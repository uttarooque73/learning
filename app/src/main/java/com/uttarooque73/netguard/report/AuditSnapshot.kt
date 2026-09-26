package com.uttarooque73.netguard.report

import com.uttarooque73.netguard.audit.DiscoveredService
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
    val verificationResults: List<VerificationResult>
)