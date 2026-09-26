package com.uttarooque73.netguard.features.policy

enum class PolicyStatus { PASS, FAIL, REVIEW }

data class SecurityPolicyRule(
    val id: String,
    val title: String,
    val description: String,
    val evaluate: (PolicyInput) -> PolicyStatus
)

data class PolicyInput(
    val telnetReachable: Boolean = false,
    val smbReachable: Boolean = false,
    val httpReachableWithoutHttps: Boolean = false,
    val usbDebugging: Boolean = false,
    val secureScreenLock: Boolean = true
)

data class PolicyResult(
    val ruleId: String,
    val title: String,
    val status: PolicyStatus,
    val description: String
)

object SecurityPolicyEngine {
    fun evaluate(rules: List<SecurityPolicyRule>, input: PolicyInput): List<PolicyResult> =
        rules.map { rule ->
            PolicyResult(rule.id, rule.title, rule.evaluate(input), rule.description)
        }

    fun defaultRules(): List<SecurityPolicyRule> = listOf(
        SecurityPolicyRule("POL-NET-001", "No Telnet", "Telnet should not be reachable.", { if (it.telnetReachable) PolicyStatus.FAIL else PolicyStatus.PASS }),
        SecurityPolicyRule("POL-NET-002", "Restricted SMB", "SMB exposure should be intentional.", { if (it.smbReachable) PolicyStatus.REVIEW else PolicyStatus.PASS }),
        SecurityPolicyRule("POL-WEB-001", "HTTPS preferred", "HTTP without HTTPS should not be exposed.", { if (it.httpReachableWithoutHttps) PolicyStatus.FAIL else PolicyStatus.PASS }),
        SecurityPolicyRule("POL-DEV-001", "USB debugging disabled", "ADB should not remain enabled unnecessarily.", { if (it.usbDebugging) PolicyStatus.FAIL else PolicyStatus.PASS }),
        SecurityPolicyRule("POL-DEV-002", "Secure screen lock", "A secure screen lock should protect the device.", { if (it.secureScreenLock) PolicyStatus.PASS else PolicyStatus.FAIL })
    )
}