package com.uttarooque73.netguard.features.autopilot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityAutopilotEngineTest {
    @Test
    fun emptyEvidenceProducesNetworkReviewAction() {
        val result = SecurityAutopilotEngine.assess(null, emptyList(), emptyList(), null, emptyList())
        assertEquals(92, result.score)
        assertEquals(1, result.actions.size)
        assertEquals(SecurityActionPriority.MEDIUM, result.actions.first().priority)
    }

    @Test
    fun apkReviewFlagsDebuggableAndSensitivePermission() {
        val review = ApkSecurityReviewEngine.review(
            ApkSecurityReviewInput(
                packageName = "com.example.test",
                appName = "Example",
                versionName = "1.0",
                targetSdk = 35,
                debuggable = true,
                backupAllowed = true,
                cleartextAllowed = false,
                exportedComponents = 1,
                requestedSensitivePermissions = listOf("android.permission.CAMERA")
            )
        )
        assertTrue(review.debuggable)
        assertTrue(review.requestedSensitivePermissions.contains("android.permission.CAMERA"))
        assertTrue(review.risk > 0)
    }
}
