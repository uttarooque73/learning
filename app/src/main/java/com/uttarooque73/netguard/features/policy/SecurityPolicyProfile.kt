package com.uttarooque73.netguard.features.policy

data class SecurityPolicyProfile(
    val id: String,
    val name: String,
    val rules: List<SecurityPolicyRule>
)

object SecurityPolicyProfiles {
    fun defaults(): List<SecurityPolicyProfile> = listOf(
        SecurityPolicyProfile("home", "Home", SecurityPolicyEngine.defaultRules()),
        SecurityPolicyProfile(
            "work",
            "Work",
            SecurityPolicyEngine.defaultRules().map {
                when (it.id) {
                    "POL-NET-002" -> it.copy(
                        description = "SMB exposure requires explicit approval on a work network.",
                        evaluate = { input -> if (input.smbReachable) PolicyStatus.FAIL else PolicyStatus.PASS }
                    )
                    "POL-DEV-001" -> it.copy(
                        description = "USB debugging must be disabled on managed work devices.",
                        evaluate = { input -> if (input.usbDebugging) PolicyStatus.FAIL else PolicyStatus.PASS }
                    )
                    else -> it
                }
            }
        )
    )
}
