package com.uttarooque73.netguard.verification

import com.uttarooque73.netguard.audit.Finding
import com.uttarooque73.netguard.audit.FindingConfidence
import com.uttarooque73.netguard.audit.FindingSeverity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class VerificationEngineTest {
    @Test
    fun unknownFindingCannotBeVerified() = runBlocking {
        val finding = Finding(
            id = "UNKNOWN",
            title = "Unknown",
            severity = FindingSeverity.LOW,
            confidence = FindingConfidence.LOW,
            ipAddress = "192.168.1.10",
            evidence = "test",
            explanation = "test",
            remediation = "test",
            verification = "test"
        )

        val result = VerificationEngine().verify(finding, beforeServicePresent = true)

        assertEquals(VerificationStatus.UNABLE_TO_VERIFY, result.status)
    }
}
