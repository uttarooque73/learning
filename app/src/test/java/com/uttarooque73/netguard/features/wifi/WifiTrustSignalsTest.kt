package com.uttarooque73.netguard.features.wifi

import org.junit.Assert.assertTrue
import org.junit.Test

class WifiTrustSignalsTest {
    @Test fun detectsSecurityDowngrade() {
        val previous = WifiObservation("Home", "aa:bb:cc:dd:ee:ff", "192.168.1.1", "WPA3")
        val current = WifiObservation("Home", "aa:bb:cc:dd:ee:00", "192.168.1.1", "WPA2")
        val ids = WifiTrustSignals.evaluate(previous, current).map { it.id }
        assertTrue(ids.contains("WIFI-BSSID-CHANGE"))
        assertTrue(ids.contains("WIFI-SECURITY-DOWNGRADE"))
    }
}