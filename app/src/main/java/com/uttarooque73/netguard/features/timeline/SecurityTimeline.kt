package com.uttarooque73.netguard.features.timeline

data class SecurityTimelineEvent(
    val id: String,
    val category: String,
    val title: String,
    val detail: String,
    val createdAtEpochMs: Long
)

object SecurityTimeline {
    fun merge(vararg eventGroups: List<SecurityTimelineEvent>): List<SecurityTimelineEvent> =
        eventGroups.flatten().sortedByDescending { it.createdAtEpochMs }
}