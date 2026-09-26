package com.uttarooque73.netguard.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SubnetCalculatorTest {
    @Test fun cidr24_excludesNetworkBroadcastAndLocalAddress() {
        val hosts = SubnetCalculator.hosts("192.168.1.10", 24)
        assertEquals(253, hosts.size)
        assertTrue("192.168.1.1" in hosts)
        assertTrue("192.168.1.254" in hosts)
        assertTrue("192.168.1.10" !in hosts)
        assertTrue("192.168.1.0" !in hosts)
        assertTrue("192.168.1.255" !in hosts)
    }
    @Test fun networksLargerThan24_areRejected() {
        assertTrue(SubnetCalculator.hosts("10.0.0.5", 23).isEmpty())
    }
    @Test fun invalidAddress_isRejected() {
        assertTrue(SubnetCalculator.hosts("10.0.0.999", 24).isEmpty())
    }
}
