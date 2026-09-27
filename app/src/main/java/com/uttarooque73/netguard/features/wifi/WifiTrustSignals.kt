package com.uttarooque73.netguard.features.wifi

data class WifiTrustSignal(
    val id: String,
    val severity: String,
    val title: String,
    val evidence: String,
    val remediation: String
)

object WifiTrustSignals {
    fun evaluate(previous: WifiObservation?, current: WifiObservation): List<WifiTrustSignal> {
        if (previous == null) return listOf(
            WifiTrustSignal(
                "WIFI-TRUST-BASELINE",
                "INFO",
                "Wi-Fi baseline created",
                "No previous trusted observation exists; the current SSID/BSSID/gateway/security values establish the initial baseline.",
                "Confirm the network identity is expected before treating it as trusted."
            )
        )
        val signals = mutableListOf<WifiTrustSignal>()
        if (previous.ssid == current.ssid && previous.bssid != current.bssid) {
            signals += WifiTrustSignal(
                "WIFI-BSSID-CHANGE", "MEDIUM", "BSSID changed on the same SSID",
                "SSID remained '" + (current.ssid ?: "unknown") + "' while the BSSID changed.",
                "Confirm the access point change is expected; roaming or mesh networks can legitimately change BSSID."
            )
        }
        if (previous.ssid == current.ssid &&
            previous.security != null && current.security != null &&
            previous.security != current.security
        ) {
            signals += WifiTrustSignal(
                "WIFI-SECURITY-DOWNGRADE", "HIGH", "Wi-Fi security mode changed",
                "Security mode changed from '" + previous.security + "' to '" + current.security + "'.",
                "Disconnect and verify the expected AP security configuration before sending sensitive data."
            )
        }
        if (previous.ssid != current.ssid && previous.gateway != current.gateway) {
            signals += WifiTrustSignal(
                "WIFI-NETWORK-CONTEXT-CHANGE", "MEDIUM", "Wi-Fi network context changed",
                "SSID and gateway both changed.",
                "Treat the connection as a new network until its identity and security are verified."
            )
        }
        return signals
    }
}
