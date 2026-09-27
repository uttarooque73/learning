package com.uttarooque73.netguard.features.command

import com.uttarooque73.netguard.audit.DiscoveredService
import com.uttarooque73.netguard.audit.Finding
import com.uttarooque73.netguard.audit.FindingConfidence
import com.uttarooque73.netguard.audit.FindingSeverity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityCommandCenterEngineTest {
    @Test
    fun scoreContributions_areTraceableToFindings() {
        val finding = Finding(
            id = "NET-HTTP-001",
            title = "Unencrypted HTTP service exposed",
            severity = FindingSeverity.LOW,
            confidence = FindingConfidence.MEDIUM,
            ipAddress = "192.168.1.1",
            evidence = "TCP/80 is reachable.",
            explanation = "HTTP is unencrypted.",
            remediation = "Prefer HTTPS.",
            verification = "Recheck TCP/80.",
            createdAtEpochMs = 1L
        )
        val contribution = CommandCenterEngine.scoreContributions(listOf(finding)).single()
        assertEquals("NET-HTTP-001", contribution.findingId)
        assertEquals(4, contribution.deduction)
    }

    @Test
    fun httpExperiment_usesExistingInventoryOnly() {
        val service = DiscoveredService("192.168.1.1", 80, "TCP", "HTTP", true, 1L)
        val result = CommandCenterEngine.experiment(
            CommandCenterEngine.experiments.first { it.id == "EXP-HTTP" },
            listOf(service),
            null,
            null
        )
        assertEquals(ExperimentStatus.REVIEW, result.status)
        assertTrue(result.evidence.contains("TCP/80"))
    }

    @Test
    fun analystAnswersFromStoredEvidence() {
        val answer = CommandCenterEngine.answer(
            "What changed?",
            null,
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList()
        )
        assertTrue(answer.contains("No monitoring changes"))
    }
}
