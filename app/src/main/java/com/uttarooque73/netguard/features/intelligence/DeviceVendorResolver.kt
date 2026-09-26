package com.uttarooque73.netguard.features.intelligence

object DeviceVendorResolver {
    fun resolve(macAddress: String?): String? {
        val normalized = macAddress?.replace(":", "")?.replace("-", "")?.uppercase() ?: return null
        if (normalized.length < 6) return null
        return when (normalized.take(6)) {
            "001C42", "3C0754" -> "Apple (candidate)"
            "001A11", "D850E6" -> "Google (candidate)"
            "002339", "5C0A5B" -> "Samsung (candidate)"
            "50C7BF", "B0487A" -> "TP-Link (candidate)"
            else -> null
        }
    }
}