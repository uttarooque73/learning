package com.uttarooque73.netguard.features.apps

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InstalledAppSecurityAuditTest {
    @Test
    fun dangerousPermissionCatalogContainsExpectedSensitivePermissions() {
        val catalog = InstalledAppSecurityAudit.dangerousPermissionCatalog()

        assertTrue(catalog.isNotEmpty())
        assertTrue("android.permission.CAMERA" in catalog)
        assertTrue("android.permission.RECORD_AUDIO" in catalog)
        assertTrue("android.permission.ACCESS_FINE_LOCATION" in catalog)
        assertTrue("android.permission.READ_CONTACTS" in catalog)
        assertTrue("android.permission.READ_CALL_LOG" in catalog)
    }

    @Test
    fun dangerousPermissionsAreFilteredAndDeduplicated() {
        val permissions = listOf(
            "android.permission.CAMERA",
            "android.permission.INTERNET",
            "android.permission.CAMERA",
            "android.permission.READ_CONTACTS"
        )

        assertEquals(
            listOf(
                "android.permission.CAMERA",
                "android.permission.READ_CONTACTS"
            ),
            InstalledAppSecurityAudit.classifyDangerousPermissions(permissions)
        )
    }

    @Test
    fun nonSensitivePermissionsProduceEmptyResult() {
        val permissions = listOf(
            "android.permission.INTERNET",
            "android.permission.ACCESS_NETWORK_STATE"
        )

        assertTrue(
            InstalledAppSecurityAudit.classifyDangerousPermissions(permissions).isEmpty()
        )
    }
}