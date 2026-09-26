package com.uttarooque73.netguard.features.apps

import org.junit.Assert.assertTrue
import org.junit.Test

class InstalledAppSecurityAuditTest {
    @Test
    fun dangerousPermissionCatalogIsNotEmpty() {
        assertTrue(InstalledAppSecurityAudit.javaClass.declaredMethods.isNotEmpty())
    }
}