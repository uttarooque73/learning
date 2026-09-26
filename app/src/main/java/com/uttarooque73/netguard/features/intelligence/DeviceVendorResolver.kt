package com.uttarooque73.netguard.features.intelligence

object DeviceVendorResolver {
    fun resolve(macAddress: String?): String? {
        val normalized = macAddress?.replace(":", "")?.replace("-", "")?.uppercase() ?: return null
        if (normalized.length < 6) return null
        return null
    }
}