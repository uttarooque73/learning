package com.uttarooque73.netguard.features.command

import com.uttarooque73.netguard.audit.DiscoveredService
import com.uttarooque73.netguard.audit.Finding
import com.uttarooque73.netguard.audit.FindingSeverity
import com.uttarooque73.netguard.audit.RiskCalculator
import com.uttarooque73.netguard.monitor.MonitorEvent
import com.uttarooque73.netguard.network.DiscoveredDevice
import com.uttarooque73.netguard.network.NetworkInfo

enum class AuditProfile(val label: String, val description: String) {
    QUICK("Quick Check", "Minimal authorized checks for a fast posture snapshot."),
    STANDARD("Standard Audit", "Normal authorized discovery and service checks."),
    DEEP("Deep Audit", "Broader supported checks with explicit resource impact.")
}

enum class ExperimentStatus { PASS, REVIEW, NOT_OBSERVED }

data class SafeExperiment(val id: String, val title: String, val description: String)
data class ExperimentResult(val experiment: SafeExperiment, val status: ExperimentStatus, val evidence: String)
data class ScoreContribution(val findingId: String, val title: String, val severity: FindingSeverity, val deduction: Int, val asset: String)
data class InvestigationSummary(
    val score: Int,
    val findingCount: Int,
    val deviceCount: Int,
    val serviceCount: Int,
    val changeCount: Int,
    val recommendations: List<String>
)

object CommandCenterEngine {
    val experiments = listOf(
        SafeExperiment("EXP-HTTP", "HTTP exposure", "Check the existing authorized inventory for reachable TCP/80."),
        SafeExperiment("EXP-TELNET", "Telnet exposure", "Check the existing authorized inventory for reachable TCP/23."),
        SafeExperiment("EXP-SMB", "SMB exposure", "Check the existing authorized inventory for reachable TCP/445."),
        SafeExperiment("EXP-WIFI", "Wi-Fi identity", "Review the observed Wi-Fi identity without attempting authentication or cracking.")
    )

    fun scoreContributions(findings: List<Finding>): List<ScoreContribution> = findings.map {
        ScoreContribution(it.id, it.title, it.severity, when (it.severity) {
            FindingSeverity.CRITICAL -> 40
            FindingSeverity.HIGH -> 25
            FindingSeverity.MEDIUM -> 12
            FindingSeverity.LOW -> 4
        }, it.ipAddress)
    }.sortedByDescending { it.deduction }

    fun investigation(devices: List<DiscoveredDevice>, services: List<DiscoveredService>, findings: List<Finding>, events: List<MonitorEvent>): InvestigationSummary {
        val recommendations = buildList {
            events.firstOrNull { it.type.name == "NEW_DEVICE" }?.let { add("Inspect newly observed asset \${it.ipAddress} and establish its expected identity.") }
            events.firstOrNull { it.type.name == "NEW_SERVICE" }?.let { add("Review newly exposed service evidence on \${it.ipAddress}.") }
            findings.firstOrNull()?.let { add("Open \${it.id} on \${it.ipAddress} and compare its evidence with remediation state.") }
            if (isEmpty()) add("No immediate investigation lead is present in the stored evidence.")
        }
        return InvestigationSummary(RiskCalculator.score(findings), findings.size, devices.size, services.size, events.size, recommendations.take(4))
    }

    fun experiment(experiment: SafeExperiment, services: List<DiscoveredService>, network: NetworkInfo?, wifiTrustStatus: String?): ExperimentResult =
        when (experiment.id) {
            "EXP-HTTP" -> resultForPort(experiment, services, 80, "HTTP")
            "EXP-TELNET" -> resultForPort(experiment, services, 23, "Telnet")
            "EXP-SMB" -> resultForPort(experiment, services, 445, "SMB")
            else -> ExperimentResult(
                experiment,
                if (network == null) ExperimentStatus.NOT_OBSERVED else ExperimentStatus.REVIEW,
                wifiTrustStatus ?: "No Wi-Fi trust observation is currently available."
            )
        }

    private fun resultForPort(experiment: SafeExperiment, services: List<DiscoveredService>, port: Int, name: String): ExperimentResult {
        val observed = services.any { it.port == port && it.reachable }
        return ExperimentResult(
            experiment,
            if (observed) ExperimentStatus.REVIEW else ExperimentStatus.PASS,
            if (observed) "Reachable TCP/\$port (\$name) is present in the existing inventory."
            else "No reachable TCP/\$port (\$name) service is present in the existing inventory."
        )
    }

    fun answer(question: String, network: NetworkInfo?, devices: List<DiscoveredDevice>, services: List<DiscoveredService>, findings: List<Finding>, events: List<MonitorEvent>): String {
        val q = question.lowercase()
        val score = RiskCalculator.score(findings)
        return when {
            "score" in q || "risk" in q -> "Current score: \$score/100. It is derived from \${findings.size} stored findings. Open Score Explainability to see each contribution."
            "change" in q || "changed" in q -> if (events.isEmpty()) "No monitoring changes are currently stored." else "\${events.size} monitoring change event(s) are stored. Latest: \${events.last().detail}"
            "device" in q -> "The current authorized inventory contains \${devices.size} device(s)."
            "service" in q || "port" in q -> "The current authorized inventory contains \${services.count { it.reachable }} reachable service(s)."
            "finding" in q -> if (findings.isEmpty()) "No findings are currently stored." else "There are \${findings.size} finding(s). Highest severity: \${findings.minByOrNull { it.severity.ordinal }?.severity}."
            "network" in q || "wifi" in q -> "Current network: \${network?.ssid ?: "not inspected"}, gateway \${network?.gatewayAddress ?: "unavailable"}, local address \${network?.localAddress ?: "unavailable"}."
            else -> "I can answer from stored evidence about the network, devices, services, findings, score, and monitoring changes."
        }
    }
}
