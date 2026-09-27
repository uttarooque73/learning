package com.uttarooque73.netguard.features.adguard

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdBlockFilterTest {
    private val rules = listOf(
        AdBlockRule("doubleclick.net", "Advertising"),
        AdBlockRule("example.com", "Custom", enabled = false)
    )

    @Test fun blocksEnabledDomainAndSubdomain() {
        assertTrue(AdBlockFilter.isBlocked("doubleclick.net", rules))
        assertTrue(AdBlockFilter.isBlocked("cdn.doubleclick.net", rules))
    }

    @Test fun doesNotBlockDisabledRule() {
        assertFalse(AdBlockFilter.isBlocked("example.com", rules))
        assertFalse(AdBlockFilter.isBlocked("cdn.example.com", rules))
    }

    @Test fun doesNotMatchLookalikeDomain() {
        assertFalse(AdBlockFilter.isBlocked("doubleclick.net.attacker.example", rules))
    }
}
