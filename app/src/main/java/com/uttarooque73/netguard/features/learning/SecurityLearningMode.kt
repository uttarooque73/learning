package com.uttarooque73.netguard.features.learning

data class LearningTopic(
    val id: String,
    val title: String,
    val explanation: String,
    val evidenceGuide: String,
    val remediationConcept: String,
    val verificationGuide: String
)

object SecurityLearningMode {
    val topics = listOf(
        LearningTopic(
            "LEARN-TELNET",
            "Why Telnet exposure matters",
            "Telnet commonly provides remote administration without the protections expected from modern encrypted administration protocols.",
            "An open TCP/23 result is evidence of reachability; it does not by itself prove compromise.",
            "Prefer a secure administration protocol and restrict management access to trusted networks.",
            "Re-run the service audit and confirm the exposed service is no longer reachable."
        ),
        LearningTopic(
            "LEARN-DNS",
            "Why DNS security matters",
            "DNS translates names to addresses and can influence where applications connect.",
            "A DNS-server change is a change signal, not proof of malicious activity.",
            "Use trusted DNS controls and investigate unexpected resolver changes.",
            "Refresh the network audit and compare the resolver list with the expected baseline."
        ),
        LearningTopic(
            "LEARN-TLS",
            "Understanding TLS evidence",
            "TLS protects application traffic and uses certificates to authenticate server identities.",
            "Certificate subject, issuer, expiry, and negotiated protocol are observable evidence.",
            "Keep certificates valid and use modern TLS configurations.",
            "Repeat the TLS check and compare certificate and protocol evidence."
        ),
        LearningTopic(
            "LEARN-ANDROID",
            "Android device posture",
            "Screen lock, security updates, debugging controls, and application storage protections affect device security.",
            "NetGuard reports Android-exposed signals and explicitly labels unavailable signals.",
            "Apply the corresponding Android Settings remediation where appropriate.",
            "Refresh the mobile security audit after changing device posture."
        )
    )
}