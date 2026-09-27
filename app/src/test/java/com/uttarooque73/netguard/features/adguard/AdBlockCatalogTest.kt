package com.uttarooque73.netguard.features.adguard

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdBlockCatalogTest {
    @Test fun matchesSubdomains() {
        val rule=AdBlockRule("doubleclick.net","Advertising")
        assertTrue(AdBlockCatalog.matches("page.doubleclick.net",rule))
        assertTrue(AdBlockCatalog.matches("doubleclick.net",rule))
        assertFalse(AdBlockCatalog.matches("doubleclick.net.example.org",rule))
    }
}
