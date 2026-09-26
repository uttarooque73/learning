package com.uttarooque73.netguard.features.policy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityPolicyProfileTest {
    @Test
    fun defaultsProvideHomeAndWorkProfiles() {
        val profiles = SecurityPolicyProfiles.defaults()
        assertEquals(setOf("Home", "Work"), profiles.map { it.name }.toSet())
        assertTrue(profiles.all { it.rules.isNotEmpty() })
    }

    @Test
    fun workProfileTreatsSmbAsFailure() {
        val work = SecurityPolicyProfiles.defaults().first { it.name == "Work" }
        val result = SecurityPolicyEngine.evaluate(
            work.rules,
            PolicyInput(smbReachable = true)
        )
        assertEquals(PolicyStatus.FAIL, result.first { it.ruleId == "POL-NET-002" }.status)
    }
}
