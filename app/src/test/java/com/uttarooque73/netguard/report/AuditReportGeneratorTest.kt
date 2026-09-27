package com.uttarooque73.netguard.report

import com.uttarooque73.netguard.audit.Finding
import com.uttarooque73.netguard.audit.FindingConfidence
import com.uttarooque73.netguard.audit.FindingSeverity
import com.uttarooque73.netguard.network.DiscoveredDevice
import org.junit.Assert.assertTrue
import com.uttarooque73.netguard.features.reporting.AdvancedReportExporter
import org.junit.Test

class AuditReportGeneratorTest {
    @Test
    fun reportContainsSecuritySummary() {
        val finding = Finding(
            id = "NET-TELNET-001",
            title = "Telnet exposed",
            severity = FindingSeverity.HIGH,
            confidence = FindingConfidence.HIGH,
            ipAddress = "192.168.1.10",
            evidence = "TCP/23 is reachable.",
            explanation = "Administrative traffic may be unencrypted.",
            remediation = "Disable Telnet.",
            verification = "Rescan TCP/23."
        )
        val report = AuditReportGenerator.generate(AuditSnapshot("test", 1L, null, emptyList(), emptyList(), listOf(finding), emptyList(), emptyList()))
        assertTrue(report.contains("NETGUARD SECURITY AUDIT REPORT"))
        assertTrue(report.contains("NET-TELNET-001"))
        assertTrue(report.contains("Risk score: 75/100"))
    }
    @Test
    fun advancedCsvContainsFindingRemediationVerificationAndPolicyRecords() {
        val finding = Finding(
            id = "NET-TELNET-001",
            title = "Telnet exposed",
            severity = FindingSeverity.HIGH,
            confidence = FindingConfidence.HIGH,
            ipAddress = "192.168.1.10",
            evidence = "TCP/23 is reachable.",
            explanation = "Legacy plaintext management.",
            remediation = "Disable Telnet.",
            verification = "Rescan TCP/23."
        )
        val snapshot = AuditSnapshot("export-test", 1L, null, emptyList(), emptyList(), listOf(finding), emptyList(), emptyList())
        val csv = AdvancedReportExporter.csv(snapshot)
        assertTrue(csv.contains("record_type"))
        assertTrue(csv.contains("finding"))
        assertTrue(csv.contains("NET-TELNET-001"))
    }
    @Test
    fun snapshotDiffDetectsDeviceAndFindingChanges() {
        val finding = Finding(
            id = "NET-TELNET-001",
            title = "Telnet exposed",
            severity = FindingSeverity.HIGH,
            confidence = FindingConfidence.HIGH,
            ipAddress = "192.168.1.10",
            evidence = "TCP/23 is reachable.",
            explanation = "Legacy plaintext management.",
            remediation = "Disable Telnet.",
            verification = "Rescan TCP/23."
        )
        val before = AuditSnapshot("before", 1L, null, listOf(DiscoveredDevice("192.168.1.10")), emptyList(), listOf(finding), emptyList(), emptyList())
        val after = AuditSnapshot("after", 2L, null, listOf(DiscoveredDevice("192.168.1.20")), emptyList(), emptyList(), emptyList(), emptyList())
        val diff = AuditSnapshotDiffCalculator.compare(before, after)
        assertTrue(diff.addedDevices.contains("192.168.1.20"))
        assertTrue(diff.removedDevices.contains("192.168.1.10"))
        assertTrue(diff.resolvedFindingIds.contains("NET-TELNET-001|192.168.1.10"))
        assertTrue(diff.changed)
    }
}