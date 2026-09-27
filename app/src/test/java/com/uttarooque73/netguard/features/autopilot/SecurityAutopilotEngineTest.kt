package com.uttarooque73.netguard.features.autopilot

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityAutopilotEngineTest {
    @Test
    fun emptyEvidenceProducesNetworkReviewAction() {
        val result=SecurityAutopilotEngine.assess(null,emptyList(),emptyList(),null,emptyList())
        assertEquals(92,result.score)
        assertEquals(1,result.actions.size)
        assertEquals(SecurityActionPriority.MEDIUM,result.actions.first().priority)
    }

    @Test
    fun apkReviewFlagsDebuggableAndSensitivePermission() {
        val app=ApplicationInfo()
        app.packageName="com.example.test"
        app.targetSdkVersion=35
        app.flags=ApplicationInfo.FLAG_DEBUGGABLE
        val pkg=PackageInfo()
        pkg.packageName="com.example.test"
        pkg.applicationInfo=app
        pkg.requestedPermissions=arrayOf("android.permission.CAMERA")
        val review=SecurityAutopilotEngine.reviewApk(pkg,"Example")
        assertTrue(review.debuggable)
        assertTrue(review.requestedSensitivePermissions.contains("android.permission.CAMERA"))
        assertTrue(review.risk>0)
    }
}
