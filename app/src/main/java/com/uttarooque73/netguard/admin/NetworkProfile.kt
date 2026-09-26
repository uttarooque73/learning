package com.uttarooque73.netguard.admin

data class NetworkProfile(
    val id: String,
    val name: String,
    val ssid: String?,
    val subnet: String?,
    val notes: String = "",
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)