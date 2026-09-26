package com.uttarooque73.netguard.remediation

data class RemediationPlaybook(
    val findingId: String,
    val title: String,
    val whyItMatters: String,
    val steps: List<String>,
    val prerequisites: List<String>,
    val verification: String
)

object RemediationCatalog {
    private val playbooks = listOf(
        RemediationPlaybook(
            findingId = "NET-TELNET-001",
            title = "Disable Telnet and use SSH",
            whyItMatters = "Telnet does not provide transport encryption for administrative sessions.",
            steps = listOf(
                "Confirm Telnet is not required by the device or application.",
                "Disable the Telnet service in the device or server administration interface.",
                "If remote administration is required, configure SSH with strong authentication.",
                "Restrict administrative services to trusted management hosts or networks."
            ),
            prerequisites = listOf("Authorized administrator access to the affected device."),
            verification = "Rescan TCP/23 and confirm that Telnet is no longer reachable."
        ),
        RemediationPlaybook(
            findingId = "NET-SMB-001",
            title = "Restrict unnecessary SMB exposure",
            whyItMatters = "SMB increases network attack surface when exposed beyond the hosts that need it.",
            steps = listOf(
                "Identify which applications or users require SMB.",
                "Disable SMB if it is not required.",
                "Otherwise restrict SMB access to trusted hosts or network segments.",
                "Keep the operating system and SMB implementation patched."
            ),
            prerequisites = listOf("Authorized administrator access and knowledge of required file-sharing clients."),
            verification = "Rescan TCP/445 and confirm exposure matches the intended network policy."
        ),
        RemediationPlaybook(
            findingId = "NET-HTTP-001",
            title = "Protect HTTP traffic with HTTPS",
            whyItMatters = "HTTP does not encrypt application traffic in transit.",
            steps = listOf(
                "Determine whether the service handles sensitive or authenticated data.",
                "Enable HTTPS with a valid certificate where supported.",
                "Redirect HTTP requests to HTTPS where appropriate.",
                "Update clients or integrations to use HTTPS."
            ),
            prerequisites = listOf("Authorized administrator access to the affected web service."),
            verification = "Confirm sensitive traffic is served through HTTPS and that HTTP is redirected or disabled where appropriate."
        )
    )

    fun forFinding(findingId: String): RemediationPlaybook? =
        playbooks.firstOrNull { it.findingId == findingId }
}
