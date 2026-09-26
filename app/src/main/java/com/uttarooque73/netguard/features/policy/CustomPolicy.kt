package com.uttarooque73.netguard.features.policy

data class CustomPolicy(
    val id: String,
    val title: String,
    val description: String,
    val ruleType: CustomPolicyRuleType = CustomPolicyRuleType.INFORMATIONAL,
    val port: Int? = null,
    val enabled: Boolean = true
)

enum class CustomPolicyRuleType {
    INFORMATIONAL,
    BLOCK_PORT,
    REQUIRE_HTTPS
}

data class CustomPolicyEvaluation(
    val policy: CustomPolicy,
    val passed: Boolean,
    val evidence: String
)

object CustomPolicyValidator {
    fun validate(policy: CustomPolicy): List<String> = buildList {
        if (policy.id.isBlank()) add("Policy id is required")
        if (policy.id.length > 64) add("Policy id must be 64 characters or fewer")
        if (policy.title.isBlank()) add("Policy title is required")
        if (policy.title.length > 120) add("Policy title must be 120 characters or fewer")
        if (policy.description.isBlank()) add("Policy description is required")
        if (policy.ruleType == CustomPolicyRuleType.BLOCK_PORT && (policy.port == null || policy.port !in 1..65535)) {
            add("BLOCK_PORT policies require a TCP/UDP port from 1 to 65535")
        }
        if (policy.ruleType != CustomPolicyRuleType.BLOCK_PORT && policy.port != null) {
            add("Port is only valid for BLOCK_PORT policies")
        }
    }
}

object CustomPolicyEvaluator {
    fun evaluate(
        policies: List<CustomPolicy>,
        openPorts: Set<Int>,
        hasHttps: Boolean
    ): List<CustomPolicyEvaluation> =
        policies.filter { it.enabled }.map { policy ->
            when (policy.ruleType) {
                CustomPolicyRuleType.INFORMATIONAL ->
                    CustomPolicyEvaluation(policy, true, "Informational policy; no automated condition configured.")
                CustomPolicyRuleType.BLOCK_PORT -> {
                    val port = policy.port!!
                    CustomPolicyEvaluation(
                        policy,
                        port !in openPorts,
                        if (port in openPorts) "Port $port is reachable." else "Port $port was not observed."
                    )
                }
                CustomPolicyRuleType.REQUIRE_HTTPS ->
                    CustomPolicyEvaluation(
                        policy,
                        hasHttps,
                        if (hasHttps) "HTTPS service was observed." else "No HTTPS service was observed."
                    )
            }
        }
}