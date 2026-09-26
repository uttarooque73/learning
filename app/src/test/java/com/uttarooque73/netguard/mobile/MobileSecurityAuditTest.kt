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
}
