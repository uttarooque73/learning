package com.uttarooque73.netguard.consumer

import com.uttarooque73.netguard.audit.Finding
import com.uttarooque73.netguard.audit.FindingSeverity
import com.uttarooque73.netguard.audit.RiskCalculator
import com.uttarooque73.netguard.mobile.MobileCheckStatus
import com.uttarooque73.netguard.mobile.MobileSecuritySnapshot
import com.uttarooque73.netguard.network.NetworkInfo

data class SecurityRecommendation(val id: String, val title: String, val whyItMatters: String, val action: String, val priority: Int)
data class SecurityPosture(val score: Int, val findings: List<Finding>, val recommendations: List<SecurityRecommendation>, val checkedAtEpochMs: Long, val hasNetworkEvidence: Boolean)

object ConsumerSecurityEngine {
    fun posture(findings: List<Finding>, mobile: MobileSecuritySnapshot?, network: NetworkInfo?, checkedAtEpochMs: Long): SecurityPosture {
        val recommendations = buildList {
            findings.sortedBy { severityRank(it.severity) }.take(5).forEach { finding ->
                add(SecurityRecommendation("finding:" + finding.id, finding.title, finding.explanation, finding.remediation.joinToString(" "), severityRank(finding.severity)))
            }
            mobile?.checks?.filter { it.status == MobileCheckStatus.FAIL }?.take(3)?.forEach { check ->
                add(SecurityRecommendation("mobile:" + check.id, check.title, check.evidence, check.remediation.joinToString(" "), 1))
            }
            mobile?.checks?.filter { it.status == MobileCheckStatus.REVIEW }?.take(2)?.forEach { check ->
                add(SecurityRecommendation("mobile-review:" + check.id, "Review: " + check.title, check.evidence, check.remediation.joinToString(" "), 3))
            }
            if (network == null) add(SecurityRecommendation("network:missing", "Inspect your current network", "NetGuard does not yet have current network evidence.", "Run the network inspection from the Security Check.", 2))
        }.distinctBy { it.id }.sortedBy { it.priority }.take(5)
        return SecurityPosture(RiskCalculator.score(findings), findings, recommendations, checkedAtEpochMs, network != null)
    }
    private fun severityRank(severity: FindingSeverity): Int = when (severity) {
        FindingSeverity.CRITICAL -> 0
        FindingSeverity.HIGH -> 1
        FindingSeverity.MEDIUM -> 2
        FindingSeverity.LOW -> 3
    }
}

data class PermissionExplanation(val permission: String, val title: String, val purpose: String, val requestedWhen: String)

object PermissionCenter {
    val explanations = listOf(
        PermissionExplanation("ACCESS_FINE_LOCATION", "Precise location", "Android requires location access for certain Wi-Fi/network identity data.", "When you inspect the current Wi-Fi/network."),
        PermissionExplanation("ACCESS_COARSE_LOCATION", "Approximate location", "Used as the less precise alternative when supported by the device.", "When network inspection needs Wi-Fi context."),
        PermissionExplanation("READ_CONTACTS", "Contacts", "Lets you choose trusted contact numbers for Call Protection.", "Only when you open Mobile Numbers."),
        PermissionExplanation("READ_CALL_LOG", "Call history", "Lets Call Protection display recent system call history.", "Only when you request call-history review."),
        PermissionExplanation("POST_NOTIFICATIONS", "Notifications", "Lets NetGuard notify you about meaningful monitored security changes.", "Only when notification alerts are enabled.")
    )
}
