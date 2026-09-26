package com.uttarooque73.netguard.report

import com.uttarooque73.netguard.audit.Finding
import com.uttarooque73.netguard.audit.FindingConfidence
import com.uttarooque73.netguard.audit.FindingSeverity
import org.junit.Assert.assertTrue
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
}