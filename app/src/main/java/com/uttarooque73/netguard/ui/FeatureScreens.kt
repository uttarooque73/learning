package com.uttarooque73.netguard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uttarooque73.netguard.audit.DiscoveredService
import com.uttarooque73.netguard.audit.Finding
import com.uttarooque73.netguard.audit.FindingSeverity
import com.uttarooque73.netguard.audit.RiskCalculator
import com.uttarooque73.netguard.remediation.RemediationRecord
import com.uttarooque73.netguard.remediation.RemediationStatus
import com.uttarooque73.netguard.verification.VerificationResult
import com.uttarooque73.netguard.features.wifi.WifiTrustResult
import com.uttarooque73.netguard.features.web.TlsAuditResult
import com.uttarooque73.netguard.features.web.HttpSecurityResult
import com.uttarooque73.netguard.features.web.WebServiceFingerprintEngine
import com.uttarooque73.netguard.features.policy.PolicyResult
import com.uttarooque73.netguard.features.timeline.SecurityTimelineEvent

@Composable
fun FeatureListScreen(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(subtitle, style = MaterialTheme.typography.bodyLarge)
        content()
    }
}

@Composable
fun ServicesFeatureScreen(services: List<DiscoveredService>) = FeatureListScreen("Services", "Reachable services found during authorized audits.") {
    if (services.isEmpty()) Text("No services recorded.")
    services.forEach {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(it.ipAddress + ":" + it.port, style = MaterialTheme.typography.titleMedium)
                Text(it.protocol + " — " + it.serviceName)
                Text(if (it.reachable) "Reachable" else "Unavailable")
            }
        }
    }
}

@Composable
fun FindingsFeatureScreen(findings: List<Finding>, onSelect: (Finding) -> Unit) = FeatureListScreen("Findings", "Evidence-backed security findings.") {
    val score = RiskCalculator.score(findings)
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Security posture", style = MaterialTheme.typography.titleMedium)
            Text("Risk score: " + score + "/100")
            Text("Critical " + findings.count { it.severity == FindingSeverity.CRITICAL } + "  •  High " + findings.count { it.severity == FindingSeverity.HIGH } + "  •  Medium " + findings.count { it.severity == FindingSeverity.MEDIUM } + "  •  Low " + findings.count { it.severity == FindingSeverity.LOW })
        }
    }
    findings.forEach { finding ->
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(finding.title, style = MaterialTheme.typography.titleMedium)
                Text(finding.severity.name + " • " + finding.confidence.name + " • " + finding.ipAddress)
                Text(finding.evidence)
                TextButton(onClick = { onSelect(finding) }) { Text("Open") }
            }
        }
    }
}

@Composable
fun RemediationFeatureScreen(findings: List<Finding>, records: List<RemediationRecord>, onStart: (Finding) -> Unit, results: List<VerificationResult>, onVerify: (Finding) -> Unit, verifying: String?) = FeatureListScreen("Remediation", "Guided remediation and verification tracking.") {
    findings.forEach { finding ->
        val record = records.firstOrNull { it.findingId == finding.id && it.ipAddress == finding.ipAddress }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(finding.title, style = MaterialTheme.typography.titleMedium)
                val status = record?.status ?: RemediationStatus.NOT_STARTED
                Text("Status: " + status)
                Text("Remediation guidance", style = MaterialTheme.typography.titleSmall)
                Text(finding.remediation)
                Text("Verification: " + finding.verification, style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onStart(finding) },
                        enabled = status == RemediationStatus.NOT_STARTED || status == RemediationStatus.CANCELLED
                    ) {
                        Text(
                            when (status) {
                                RemediationStatus.IN_PROGRESS -> "Started"
                                RemediationStatus.COMPLETED -> "Completed"
                                else -> "Start"
                            }
                        )
                    }
                    Button(
                        onClick = { onVerify(finding) },
                        enabled = verifying == null && status != RemediationStatus.NOT_STARTED
                    ) {
                        Text(if (verifying == finding.id) "Verifying…" else "Verify")
                    }
                }
                results.firstOrNull { it.findingId == finding.id && it.ipAddress == finding.ipAddress }?.let {
                    Text("Last verification: " + it.status)
                    Text("After evidence: " + it.afterEvidence)
                }
            }
        }
    }
}

@Composable
fun WifiFeatureScreen(result: WifiTrustResult?) = FeatureListScreen("Wi-Fi Trust", "Track changes in the observed Wi-Fi identity.") {
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("Status: " + (result?.status ?: "NOT AUDITED")); Text(result?.evidence ?: "No observation yet") } }
}

@Composable
fun WebFeatureScreen(tls: TlsAuditResult?, http: HttpSecurityResult?) = FeatureListScreen("Web Security", "TLS and HTTP security evidence.") {
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("TLS", style = MaterialTheme.typography.titleMedium); tls?.evidence?.forEach { Text(it) } } }
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("HTTP", style = MaterialTheme.typography.titleMedium); http?.evidence?.forEach { Text(it) }; http?.let { val fingerprint = WebServiceFingerprintEngine.from(it.url, it.headers); Text("Server: " + (fingerprint.server ?: "Not disclosed")); Text("Version evidence: " + (fingerprint.versionEvidence ?: "Not available")) } } }
}

@Composable
fun PolicyFeatureScreen(results: List<PolicyResult>, profile: String, onProfile: (String) -> Unit) = FeatureListScreen("Security Policies", "Evaluate posture using configurable profiles.") {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { TextButton(onClick = { onProfile("Home") }) { Text("Home") }; TextButton(onClick = { onProfile("Work") }) { Text("Work") } }
    Text("Profile: " + profile)
    results.forEach { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(it.title, style = MaterialTheme.typography.titleMedium); Text(it.status.name); Text(it.description) } } }
}

@Composable
fun TimelineFeatureScreen(events: List<SecurityTimelineEvent>) = FeatureListScreen("Security Timeline", "Local chronological security activity.") {
    if (events.isEmpty()) Text("No events recorded.")
    events.reversed().forEach { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp)) { Text(it.category.uppercase() + " — " + it.title, style = MaterialTheme.typography.titleMedium); Text(it.detail) } } }
}