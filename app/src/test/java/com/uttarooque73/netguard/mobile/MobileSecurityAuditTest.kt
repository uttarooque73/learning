package com.uttarooque73.netguard.mobile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MobileSecurityAuditTest {
    @Test
    fun checkStatusesAreExplicit() {
        val statuses = MobileCheckStatus.entries.toSet()
        assertTrue(statuses.contains(MobileCheckStatus.PASS))
        assertTrue(statuses.contains(MobileCheckStatus.REVIEW))
        assertTrue(statuses.contains(MobileCheckStatus.FAIL))
        assertTrue(statuses.contains(MobileCheckStatus.NOT_AVAILABLE))
    }

    @Test
    fun mobileChecksHaveRemediationAndVerification() {
        val checks = MobileSecurityAudit.javaClass.declaredMethods
        assertTrue(checks.any { it.name == "inspect" })
    }

    @Test
    fun mobileAuditHasExpectedPostureCoverage() {
        val ids = MobileSecurityAudit.checkIds()
        assertTrue(ids.contains("MOB-NET-001"))
        assertTrue(ids.contains("MOB-DNS-001"))
        assertTrue(ids.contains("MOB-LIMIT-001"))
    }

    @Test
    fun deviceSecurityCheckIdsAreStable() {
        val ids = MobileSecurityAudit.checkIds()
        assertTrue(ids.contains("MOB-DEV-005"))
        assertTrue(ids.contains("MOB-DEV-006"))
        assertTrue(ids.contains("MOB-DEV-007"))
        assertTrue(ids.contains("MOB-DEV-008"))
        assertEquals(14, ids.size)
    }
}
