package com.uttarooque73.netguard.features.policy

data class CustomPolicy(val id: String, val title: String, val description: String, val enabled: Boolean = true)

object CustomPolicyValidator {
    fun validate(policy: CustomPolicy): List<String> = buildList {
        if (policy.id.isBlank()) add("Policy id is required")
        if (policy.title.isBlank()) add("Policy title is required")
        if (policy.description.isBlank()) add("Policy description is required")
    }
}