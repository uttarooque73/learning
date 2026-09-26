package com.uttarooque73.netguard.admin

enum class AdminEventType { PROFILE_CREATED, PROFILE_SELECTED, ASSET_UPDATED, AUDIT_STARTED, REPORT_CREATED }

data class AdminEvent(
    val id: String,
    val type: AdminEventType,
    val subject: String,
    val detail: String,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)