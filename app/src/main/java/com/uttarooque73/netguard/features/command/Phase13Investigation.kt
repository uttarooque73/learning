package com.uttarooque73.netguard.features.command

import com.uttarooque73.netguard.audit.DiscoveredService
import com.uttarooque73.netguard.audit.Finding
import com.uttarooque73.netguard.report.AuditSnapshot
import com.uttarooque73.netguard.report.AuditSnapshotDiffCalculator
import com.uttarooque73.netguard.remediation.RemediationStatus

data class AuditProfilePlan(
    val profile: AuditProfile,
    val maxDevices: Int,
    val maxServiceAudits: Int,
    val includeHostnameResolution: Boolean,
    val resourceWarning: String
)

object AuditProfilePlanner {
    fun plan(profile: AuditProfile): AuditProfilePlan = when (profile) {
        AuditProfile.QUICK -> AuditProfilePlan(profile, 8, 4, false, "Lowest network and battery impact.")
        AuditProfile.STANDARD -> AuditProfilePlan(profile, 32, 16, true, "Normal bounded discovery and service auditing.")
        AuditProfile.DEEP -> AuditProfilePlan(profile, 128, 64, true, "Higher network and battery use; remains bounded.")
    }
}

data class InvestigationDiff(
    val beforeId: String,
    val afterId: String,
    val addedDevices: List<String>,
    val removedDevices: List<String>,
    val addedServices: List<String>,
    val removedServices: List<String>,
    val newFindings: List<String>,
    val resolvedFindings: List<String>,
    val remediationChanges: List<String>,
    val riskBefore: Int,
    val riskAfter: Int
) {
    val changed: Boolean
        get() = addedDevices.isNotEmpty() || removedDevices.isNotEmpty() ||
            addedServices.isNotEmpty() || removedServices.isNotEmpty() ||
            newFindings.isNotEmpty() || resolvedFindings.isNotEmpty() ||
            remediationChanges.isNotEmpty() || riskBefore != riskAfter
}

object InvestigationDiffEngine {
    fun compare(before: AuditSnapshot, after: AuditSnapshot): InvestigationDiff {
        val base = AuditSnapshotDiffCalculator.compare(before, after)
        val beforeRem = before.remediationRecords.associate { it.findingId + "|" + it.ipAddress to it.status }
        val afterRem = after.remediationRecords.associate { it.findingId + "|" + it.ipAddress to it.status }
        val changes = (beforeRem.keys + afterRem.keys).toSet().mapNotNull { key ->
            val old = beforeRem[key]
            val now = afterRem[key]
            if (old != now) key + ": " + (old?.name ?: "NONE") + " -> " + (now?.name ?: "NONE") else null
        }.sorted()
        return InvestigationDiff(
            before.id, after.id, base.addedDevices, base.removedDevices,
            base.addedServices, base.removedServices, base.newFindingIds,
            base.resolvedFindingIds, changes, base.riskScoreBefore, base.riskScoreAfter
        )
    }
}

data class AssetSecurityProfile(
    val ipAddress: String,
    val hostname: String?,
    val firstSeenEpochMs: Long,
    val lastSeenEpochMs: Long,
    val reachable: Boolean,
    val services: List<DiscoveredService>,
    val findings: List<Finding>,
    val remediationState: String,
    val changeCount: Int
)

object AssetSecurityProfileBuilder {
    fun build(snapshot: AuditSnapshot, previous: AuditSnapshot? = null): List<AssetSecurityProfile> {
        val previousDevices = previous?.devices?.associateBy { it.ipAddress }.orEmpty()
        val previousServices = previous?.services.orEmpty()
        return snapshot.devices.map { device ->
            val services = snapshot.services.filter { it.ipAddress == device.ipAddress }
            val findings = snapshot.findings.filter { it.ipAddress == device.ipAddress }
            val old = previousDevices[device.ipAddress]
            val serviceChanges = services.count { current ->
                previousServices.none {
                    it.ipAddress == current.ipAddress &&
                        it.protocol.equals(current.protocol, true) &&
                        it.port == current.port
                }
            }
            val remediation = if (findings.isEmpty()) {
                "NO_OPEN_FINDINGS"
            } else {
                findings.map { finding ->
                    snapshot.remediationRecords.firstOrNull {
                        it.findingId == finding.id && it.ipAddress == finding.ipAddress
                    }?.status?.name ?: RemediationStatus.NOT_STARTED.name
                }.distinct().joinToString(",")
            }
            AssetSecurityProfile(
                device.ipAddress, device.hostname,
                old?.discoveredAtEpochMs ?: device.discoveredAtEpochMs,
                device.discoveredAtEpochMs, device.reachable,
                services, findings, remediation, serviceChanges
            )
        }
    }
}
