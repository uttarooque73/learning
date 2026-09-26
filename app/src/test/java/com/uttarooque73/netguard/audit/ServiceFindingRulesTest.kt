package com.uttarooque73.netguard.audit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ServiceFindingRulesTest {
    @Test fun telnetProducesHighFinding() {
        val finding = ServiceFindingRules.evaluate(DiscoveredService("192.168.1.20", 23, serviceName = "Telnet", reachable = true))
        assertNotNull(finding)
        assertEquals(FindingSeverity.HIGH, finding!!.severity)
    }
    @Test fun unknownPortProducesNoFinding() {
        assertNull(ServiceFindingRules.evaluate(DiscoveredService("192.168.1.20", 12345, serviceName = "Unknown", reachable = true)))
    }
}
