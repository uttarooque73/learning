package com.uttarooque73.netguard.compliance

data class SecurityBaseline(
    val id: String,
    val name: String,
    val checks: List<BaselineCheck>
)

enum class BaselineCheckType { NO_TELNET, NO_SMB, HTTPS_ONLY_FOR_WEB }

data class BaselineCheck(
    val id: String,
    val type: BaselineCheckType,
    val title: String,
    val description: String
)

object DefaultBaselines {
    fun secureHomeNetwork(): SecurityBaseline = SecurityBaseline(
        id = "HOME-SECURE-001",
        name = "Secure Home Network",
        checks = listOf(
            BaselineCheck("NO-TELNET", BaselineCheckType.NO_TELNET, "No Telnet exposure", "TCP/23 should not be reachable."),
            BaselineCheck("NO-SMB", BaselineCheckType.NO_SMB, "Restricted SMB exposure", "TCP/445 should not be reachable by default."),
            BaselineCheck("HTTPS-WEB", BaselineCheckType.HTTPS_ONLY_FOR_WEB, "Prefer HTTPS for web services", "HTTP exposure should be reviewed when HTTPS is available.")
        )
    )
}