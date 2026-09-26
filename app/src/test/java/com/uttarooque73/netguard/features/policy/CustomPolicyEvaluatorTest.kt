package com.uttarooque73.netguard.features.policy

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomPolicyEvaluatorTest {
    @Test
    fun blockedPortFailsWhenObserved() {
        val policy = CustomPolicy("no-telnet", "No Telnet", "Telnet must not be exposed", CustomPolicyRuleType.BLOCK_PORT, 23)
        val result = CustomPolicyEvaluator.evaluate(listOf(policy), setOf(23), false).single()
        assertFalse(result.passed)
    }

    @Test
    fun httpsRequirementPassesWhenHttpsExists() {
        val policy = CustomPolicy("https", "HTTPS required", "Require HTTPS", CustomPolicyRuleType.REQUIRE_HTTPS)
        val result = CustomPolicyEvaluator.evaluate(listOf(policy), emptySet(), true).single()
        assertTrue(result.passed)
    }
}