package com.uttarooque73.netguard.admin

import org.junit.Assert.assertEquals
import org.junit.Test

class AdminModelsTest {
    @Test fun profileStoresNetworkIdentity() {
        val profile = NetworkProfile("1", "Home", "MyWiFi", "192.168.1.0/24")
        assertEquals("192.168.1.0/24", profile.subnet)
    }

    @Test fun assetMetadataStoresTags() {
        val asset = AssetMetadata("192.168.1.20", "Printer", listOf("iot", "printer"))
        assertEquals(2, asset.tags.size)
    }
}