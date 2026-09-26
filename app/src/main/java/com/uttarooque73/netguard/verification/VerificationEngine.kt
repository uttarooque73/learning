package com.uttarooque73.netguard.verification

import com.uttarooque73.netguard.audit.Finding
import com.uttarooque73.netguard.audit.ServiceAudit

class VerificationEngine(
    private val serviceAudit: ServiceAudit = ServiceAudit()
) {
    suspend fun verify(finding: Finding, beforeServicePresent: Boolean): VerificationResult {
        val port = portForFinding(finding.id)
            ?: return VerificationResult(
                finding.id,
                finding.ipAddress,
                VerificationStatus.UNABLE_TO_VERIFY,
                finding.evidence,
                "No verification rule is available."
            )

        val currentServices = runCatching { serviceAudit.audit(finding.ipAddress) }.getOrNull()
            ?: return VerificationResult(
                finding.id,
                finding.ipAddress,
                VerificationStatus.UNABLE_TO_VERIFY,
                finding.evidence,
                "Service recheck could not be completed."
            )

        val afterPresent = currentServices.any { it.port == port && it.protocol == "TCP" && it.reachable }
        val status = when {
            beforeServicePresent && !afterPresent -> VerificationStatus.FIXED
            !beforeServicePresent && afterPresent -> VerificationStatus.CHANGED
            afterPresent -> VerificationStatus.STILL_PRESENT
            else -> VerificationStatus.FIXED
        }

        return VerificationResult(
            finding.id,
            finding.ipAddress,
            status,
            finding.evidence,
            if (afterPresent) "TCP/$port is reachable." else "TCP/$port is not reachable."
        )
    }

    private fun portForFinding(findingId: String): Int? = when (findingId) {
        "NET-TELNET-001" -> 23
        "NET-SMB-001" -> 445
        "NET-HTTP-001" -> 80
        else -> null
    }
}
