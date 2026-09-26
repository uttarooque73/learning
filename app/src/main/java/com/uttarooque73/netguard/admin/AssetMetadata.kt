package com.uttarooque73.netguard.admin

data class AssetMetadata(
    val ipAddress: String,
    val name: String = "",
    val tags: List<String> = emptyList(),
    val notes: String = "",
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)