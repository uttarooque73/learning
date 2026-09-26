package com.uttarooque73.netguard.features.timeline

import org.junit.Assert.assertEquals
import org.junit.Test

class SecurityTimelineTest {
    @Test
    fun mergeSortsNewestFirst() {
        val events = SecurityTimeline.merge(
            listOf(
                SecurityTimelineEvent("1", "network", "old", "old", 100),
                SecurityTimelineEvent("2", "finding", "new", "new", 200)
            )
        )
        assertEquals(listOf("2", "1"), events.map { it.id })
    }
}
