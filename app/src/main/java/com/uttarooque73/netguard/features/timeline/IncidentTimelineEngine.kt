package com.uttarooque73.netguard.features.timeline

enum class IncidentEventType {
    NETWORK, DEVICE, APP, FINDING, REMEDIATION, VERIFICATION, ALERT, SECURITY_CHECK, REPORT, OTHER
}

enum class IncidentSeverity {
    INFO, REVIEW, HIGH
}

data class IncidentEvent(
    val source: SecurityTimelineEvent,
    val type: IncidentEventType,
    val severity: IncidentSeverity,
    val entity: String,
    val evidence: String,
    val whyItMatters: String,
    val recommendedAction: String
)

object IncidentTimelineEngine {
    fun replay(events: List<SecurityTimelineEvent>): List<IncidentEvent> =
        events.sortedByDescending { it.createdAtEpochMs }.map { event ->
            val category = event.category.lowercase()
            val title = event.title.lowercase()
            val type = when {
                category.contains("baseline") && (title.contains("network") || title.contains("dns") || title.contains("wi-fi") || title.contains("gateway")) -> IncidentEventType.NETWORK
                category.contains("baseline") && (title.contains("device") || title.contains("service")) -> IncidentEventType.DEVICE
                category.contains("baseline") && title.contains("application") -> IncidentEventType.APP
                category.contains("baseline") -> IncidentEventType.SECURITY_CHECK
                category.contains("network") || category.contains("wifi") || category.contains("dns") -> IncidentEventType.NETWORK
                category.contains("device") || category.contains("service") || category.contains("command-center") -> IncidentEventType.DEVICE
                category.contains("app") || category.contains("privacy") || title.contains("application") -> IncidentEventType.APP
                category.contains("finding") || title.contains("finding") -> IncidentEventType.FINDING
                category.contains("remediation") || title.contains("remediat") -> IncidentEventType.REMEDIATION
                category.contains("verification") || title.contains("verif") -> IncidentEventType.VERIFICATION
                category.contains("alert") || category.contains("notification") -> IncidentEventType.ALERT
                category.contains("security-check") || title.contains("security check") || category.contains("mobile") -> IncidentEventType.SECURITY_CHECK
                category.contains("report") -> IncidentEventType.REPORT
                else -> IncidentEventType.OTHER
            }
            val severity = when {
                category.contains("alert") || title.contains("critical") || title.contains("high risk") -> IncidentSeverity.HIGH
                category.contains("finding") || category.contains("privacy") || title.contains("changed") ||
                    title.contains("new device") || title.contains("new application") -> IncidentSeverity.REVIEW
                else -> IncidentSeverity.INFO
            }
            val entity = when (type) {
                IncidentEventType.NETWORK -> "Network environment"
                IncidentEventType.DEVICE -> "Device or service"
                IncidentEventType.APP -> "Installed application"
                IncidentEventType.FINDING -> "Security finding"
                IncidentEventType.REMEDIATION -> "Remediation workflow"
                IncidentEventType.VERIFICATION -> "Verification workflow"
                IncidentEventType.ALERT -> "Security alert"
                IncidentEventType.SECURITY_CHECK -> "Device security posture"
                IncidentEventType.REPORT -> "Audit report"
                IncidentEventType.OTHER -> "NetGuard activity"
            }
            val why = when (type) {
                IncidentEventType.NETWORK -> "Network identity or network-security evidence changed or was refreshed."
                IncidentEventType.DEVICE -> "The observed device or service inventory changed or was audited."
                IncidentEventType.APP -> "Application privacy or security exposure was observed during an audit."
                IncidentEventType.FINDING -> "A security condition was recorded and may require review."
                IncidentEventType.REMEDIATION -> "A security finding entered a remediation workflow."
                IncidentEventType.VERIFICATION -> "NetGuard recorded whether a remediation condition was verified."
                IncidentEventType.ALERT -> "NetGuard generated a security notification based on observed posture."
                IncidentEventType.SECURITY_CHECK -> "A complete or device-focused security assessment refreshed the evidence set."
                IncidentEventType.REPORT -> "An audit artifact was created or exported for later investigation."
                IncidentEventType.OTHER -> "This event contributes to the local security history."
            }
            val action = when (type) {
                IncidentEventType.FINDING, IncidentEventType.APP -> "Review the evidence and follow the recommended remediation."
                IncidentEventType.NETWORK -> "Confirm the network identity and re-run the relevant audit if the change is unexpected."
                IncidentEventType.DEVICE -> "Review the device/service evidence and investigate unexpected assets."
                IncidentEventType.REMEDIATION -> "Complete the remediation and run verification."
                IncidentEventType.VERIFICATION -> "Use the verification result to confirm whether the original condition changed."
                IncidentEventType.ALERT -> "Open the related findings and verify the underlying evidence."
                IncidentEventType.SECURITY_CHECK -> "Review the latest recommendations and unresolved findings."
                IncidentEventType.REPORT -> "Retain the report when it is needed for an incident record."
                IncidentEventType.OTHER -> "Open the event details and review the associated evidence."
            }
            IncidentEvent(event, type, severity, entity, event.detail, why, action)
        }
}
