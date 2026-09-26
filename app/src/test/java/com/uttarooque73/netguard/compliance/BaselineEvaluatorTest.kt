package com.uttarooque73.netguard.compliance

import com.uttarooque73.netguard.audit.DiscoveredService
import org.junit.Assert.assertEquals
import org.junit.Test

class BaselineEvaluatorTest {
    @Test fun telnetFailsSecureHomeBaseline() {
        val results = BaselineEvaluator.evaluate(DefaultBaselines.secureHomeNetwork(), listOf(DiscoveredService("192.168.1.10", 23, serviceName = "Telnet", reachable = true)))
        assertEquals(BaselineStatus.FAIL, results.first { it.checkId == "NO-TELNET" }.status)
    }

    @Test fun noObservedTelnetPasses() {
        val results = BaselineEvaluator.evaluate(DefaultBaselines.secureHomeNetwork(), emptyList())
        assertEquals(BaselineStatus.PASS, results.first { it.checkId == "NO-TELNET" }.status)
    }
}