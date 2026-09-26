package com.uttarooque73.netguard.security

import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseChecksTest {
    @Test
    fun currentReleaseChecksPass() {
        assertTrue(ReleaseChecks.evaluate().all { it.passed })
    }
}
