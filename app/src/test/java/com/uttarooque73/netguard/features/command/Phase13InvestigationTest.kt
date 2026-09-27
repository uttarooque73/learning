package com.uttarooque73.netguard.features.command

import com.uttarooque73.netguard.audit.DiscoveredService
import com.uttarooque73.netguard.network.DiscoveredDevice
import com.uttarooque73.netguard.network.NetworkInfo
import com.uttarooque73.netguard.report.AuditSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Phase13InvestigationTest {
    @Test
    fun profilesAreBounded() {
        assertEquals(8, AuditProfilePlanner.plan(AuditProfile.QUICK).maxDevices)
        assertEquals(32, AuditProfilePlanner.plan(AuditProfile.STANDARD).maxDevices)
        assertEquals(128, AuditProfilePlanner.plan(AuditProfile.DEEP).maxDevices)
    }

    @Test
    fun snapshotDiffFindsExposureAndRiskChanges() {
        val before = AuditSnapshot("before", 1L, null,
            listOf(DiscoveredDevice("192.168.1.2", null, true, 1L)),
            listOf(DiscoveredService("192.168.1.2", 80, "TCP", "HTTP", true, 1L)),
            emptyList(), emptyList(), emptyList())
        val after = before.copy(
            id = "after",
            createdAtEpochMs = 2L,
            devices = before.devices + DiscoveredDevice("192.168.1.3", null, true, 2L),
            services = before.services + DiscoveredService("192.168.1.3", 445, "TCP", "SMB", true, 2L)
        )
        val diff = InvestigationDiffEngine.compare(before, after)
        assertTrue(diff.addedDevices.contains("192.168.1.3"))
        assertTrue(diff.addedServices.contains("192.168.1.3|TCP|445"))
    }

    @Test
    fun evidenceContextIsBoundedAndGrounded() {
        val context = LocalEvidenceAnalyst.buildContext(
            NetworkInfo(null, "192.168.1.10", "192.168.1.1", "192.168.1.0/24", emptyList(), "Test", null, "WPA2"),
            emptyList(), emptyList(), emptyList()
        )
        assertTrue(context.contains("Devices: 0"))
        assertTrue(LocalEvidenceAnalyst.prompt("what changed?", context).contains("Do not invent facts"))
    }
}
