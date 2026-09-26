package com.uttarooque73.netguard.features.network

import org.junit.Assert.assertTrue
import org.junit.Test

class DnsSecurityAnalyzerTest {
    @Test fun identifiesPrivateResolver() {
        assertTrue(DnsSecurityAnalyzer.analyze(listOf("192.168.1.1")).privateResolver)
    }
}