package com.uttarooque73.netguard.network

import android.net.wifi.WifiInfo
import org.junit.Assert.assertEquals
import org.junit.Test

class WifiSecurityClassifierTest {
    @Test fun mapsOpenNetwork() {
        assertEquals("Open", WifiSecurityClassifier.classify(WifiInfo.SECURITY_TYPE_OPEN))
    }

    @Test fun mapsWpa2Psk() {
        assertEquals("WPA2-PSK", WifiSecurityClassifier.classify(WifiInfo.SECURITY_TYPE_PSK))
    }

    @Test fun mapsWpa3Sae() {
        assertEquals("WPA3-SAE", WifiSecurityClassifier.classify(WifiInfo.SECURITY_TYPE_SAE))
    }

    @Test fun unknownTypeIsExplicit() {
        assertEquals("Unknown", WifiSecurityClassifier.classify(WifiInfo.SECURITY_TYPE_UNKNOWN))
    }
}
