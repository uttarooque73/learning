package com.uttarooque73.netguard.features.wifi

data class WifiObservation(
    val ssid: String?,
    val bssid: String?,
    val gateway: String?,
    val security: String?,
    val observedAtEpochMs: Long = System.currentTimeMillis()
)

enum class WifiTrustStatus { KNOWN, CHANGED, UNKNOWN }

data class WifiTrustResult(
    val status: WifiTrustStatus,
    val evidence: String,
    val remediation: List<String>
)

object WifiTrustEngine {
    fun compare(previous: WifiObservation?, current: WifiObservation): WifiTrustResult {
        if (previous == null) {
            return WifiTrustResult(
                WifiTrustStatus.UNKNOWN,
                "No previous observation exists for this Wi-Fi identity.",
                listOf("Observe the expected network before using change detection.")
            )
        }
        val changes = buildList {
            if (previous.ssid != current.ssid) add("SSID changed.")
            if (previous.bssid != current.bssid) add("BSSID changed.")
            if (previous.gateway != current.gateway) add("Gateway changed.")
            if (previous.security != current.security) add("Wi-Fi security mode changed.")
        }
        return if (changes.isEmpty()) {
            WifiTrustResult(WifiTrustStatus.KNOWN, "No tracked Wi-Fi identity changes.", emptyList())
        } else {
            WifiTrustResult(
                WifiTrustStatus.CHANGED,
                changes.joinToString(" "),
                listOf(
                    "Confirm the SSID, BSSID, gateway, and security mode are expected.",
                    "Do not assume a changed BSSID proves a rogue access point.",
                    "Disconnect and investigate unexpected changes before sending sensitive data."
                )
            )
        }
    }
}