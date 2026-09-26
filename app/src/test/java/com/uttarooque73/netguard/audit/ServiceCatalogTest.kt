package com.uttarooque73.netguard.audit

import org.junit.Assert.assertTrue
import org.junit.Test

class ServiceCatalogTest {
    @Test
    fun catalog_contains_commonAuthorizedAuditPorts() {
        val ports = ServiceCatalog.ports.map { it.first }
        assertTrue(22 in ports)
        assertTrue(80 in ports)
        assertTrue(443 in ports)
        assertTrue(445 in ports)
    }
}
