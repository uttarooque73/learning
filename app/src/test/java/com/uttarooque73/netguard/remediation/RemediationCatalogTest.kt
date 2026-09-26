package com.uttarooque73.netguard.remediation

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RemediationCatalogTest {
    @Test fun knownFindingHasPlaybook() { assertNotNull(RemediationCatalog.forFinding("NET-TELNET-001")) }
    @Test fun unknownFindingHasNoPlaybook() { assertNull(RemediationCatalog.forFinding("UNKNOWN")) }
}
