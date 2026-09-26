package com.uttarooque73.netguard.features.policy

import org.junit.Assert.assertEquals
import org.junit.Test

class SecurityPolicyTest {
    @Test
    fun defaultPolicyDetectsTelnet() {
        val result = SecurityPolicyEngine.evaluate(
            SecurityPolicyEngine.defaultRules(),
            PolicyInput(telnetReachable = true)
        ).first { it.ruleId == "POL-NET-001" }
        assertEquals(PolicyStatus.FAIL, result.status)
    }
}