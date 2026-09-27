package com.uttarooque73.netguard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uttarooque73.netguard.audit.*
import com.uttarooque73.netguard.remediation.*
import com.uttarooque73.netguard.verification.VerificationResult
import com.uttarooque73.netguard.features.wifi.*
import com.uttarooque73.netguard.features.web.*
import com.uttarooque73.netguard.features.policy.PolicyResult
import com.uttarooque73.netguard.features.timeline.SecurityTimelineEvent

@Composable fun FeatureListScreen(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        content()
    }
}

@Composable fun ServicesFeatureScreen(services: List<DiscoveredService>) = FeatureListScreen("Services", "Reachable services found during authorized audits.") {
    var query by remember { mutableStateOf("") }; var reachableOnly by remember { mutableStateOf(false) }
    val filtered = services.filter { !reachableOnly || it.reachable }.filter { query.isBlank() || it.ipAddress.contains(query, true) || it.serviceName.contains(query, true) || it.protocol.contains(query, true) || it.port.toString().contains(query) }
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Exposure summary", style = MaterialTheme.typography.titleMedium)
        Text(services.size.toString() + " recorded • " + services.count { it.reachable } + " reachable • " + services.map { it.ipAddress }.distinct().size + " assets")
        OutlinedTextField(query, { query = it }, label = { Text("Search IP, port, service or protocol") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        FilterChip(reachableOnly, { reachableOnly = !reachableOnly }, label = { Text("Reachable only") })
    } }
    if (filtered.isEmpty()) Text(if (services.isEmpty()) "No services recorded." else "No services match the current filter.")
    filtered.forEach { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(it.ipAddress + ":" + it.port, style = MaterialTheme.typography.titleMedium); Text(it.protocol + " — " + it.serviceName); Text(if (it.reachable) "Reachable" else "Unavailable")
    } } }
}

@Composable fun FindingsFeatureScreen(findings: List<Finding>, onSelect: (Finding) -> Unit) = FeatureListScreen("Findings", "Evidence-backed security findings.") {
    var severity by remember { mutableStateOf<String?>(null) }; var query by remember { mutableStateOf("") }
    val filtered = findings.filter { severity == null || it.severity.name == severity }.filter { query.isBlank() || it.title.contains(query, true) || it.ipAddress.contains(query, true) || it.evidence.contains(query, true) }
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Security posture", style = MaterialTheme.typography.titleMedium); Text("Risk score: " + RiskCalculator.score(findings) + "/100")
        Text("Critical " + findings.count { it.severity == FindingSeverity.CRITICAL } + " • High " + findings.count { it.severity == FindingSeverity.HIGH } + " • Medium " + findings.count { it.severity == FindingSeverity.MEDIUM } + " • Low " + findings.count { it.severity == FindingSeverity.LOW })
        OutlinedTextField(query, { query = it }, label = { Text("Search findings") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) { FilterChip(severity == null, { severity = null }, label = { Text("All") }); FindingSeverity.values().forEach { s -> FilterChip(severity == s.name, { severity = s.name }, label = { Text(s.name) }) } }
        Text("Showing " + filtered.size + " of " + findings.size, style = MaterialTheme.typography.bodySmall)
    } }
    if (filtered.isEmpty()) Text(if (findings.isEmpty()) "No findings recorded." else "No findings match the current filter.")
    filtered.forEach { f -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) { Text(f.title, style = MaterialTheme.typography.titleMedium); Text(f.severity.name + " • " + f.confidence.name + " • " + f.ipAddress); Text(f.evidence); LoadingTextButton(onClick = { onSelect(f) }) { Text("Open evidence & remediation") } } } }
}

@Composable fun RemediationFeatureScreen(findings: List<Finding>, records: List<RemediationRecord>, onStart: (Finding) -> Unit, results: List<VerificationResult>, onVerify: (Finding) -> Unit, verifying: String?) = FeatureListScreen("Remediation", "Guided remediation and verification tracking.") {
    val completed = findings.count { f -> records.lastOrNull { it.findingId == f.id && it.ipAddress == f.ipAddress }?.status == RemediationStatus.COMPLETED }
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("Remediation queue", style = MaterialTheme.typography.titleMedium); Text((findings.size - completed).toString() + " pending • " + completed + " completed • " + results.size + " verification records") } }
    findings.forEach { f -> val record = records.firstOrNull { it.findingId == f.id && it.ipAddress == f.ipAddress }; val status = record?.status ?: RemediationStatus.NOT_STARTED
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(f.title, style = MaterialTheme.typography.titleMedium); Text("Status: " + status); Text(f.remediation); Text("Verification: " + f.verification)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { LoadingButton(onClick = { onStart(f) }, enabled = status == RemediationStatus.NOT_STARTED || status == RemediationStatus.CANCELLED) { Text(if (status == RemediationStatus.IN_PROGRESS) "Started" else "Start") }; LoadingButton(onClick = { onVerify(f) }, enabled = verifying == null && status != RemediationStatus.NOT_STARTED) { Text(if (verifying == f.id) "Verifying…" else "Verify") } }
            results.firstOrNull { it.findingId == f.id && it.ipAddress == f.ipAddress }?.let { Text("Last verification: " + it.status); Text("After evidence: " + it.afterEvidence) }
        } }
    }
}

@Composable fun WifiFeatureScreen(result: WifiTrustResult?) = FeatureListScreen("Wi-Fi Trust", "Detect changes in observed Wi-Fi identity and understand what to verify.") {
    val status = result?.status
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = when (status) { WifiTrustStatus.CHANGED -> MaterialTheme.colorScheme.errorContainer; WifiTrustStatus.KNOWN -> MaterialTheme.colorScheme.primaryContainer; else -> MaterialTheme.colorScheme.surfaceVariant })) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text("Current trust state", style = MaterialTheme.typography.titleMedium); Text(status?.name ?: "NOT AUDITED", style = MaterialTheme.typography.headlineSmall); Text(result?.evidence ?: "No observation has been recorded yet.") } }
    result?.remediation?.takeIf { it.isNotEmpty() }?.let { actions -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text("Recommended actions", style = MaterialTheme.typography.titleMedium); actions.forEachIndexed { i, a -> Text((i + 1).toString() + ". " + a) } } } }
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("Interpretation", style = MaterialTheme.typography.titleMedium); Text("A changed BSSID alone does not prove a rogue access point. Verify the network identity before trusting it.") } }
}

@Composable fun WebFeatureScreen(tls: TlsAuditResult?, http: HttpSecurityResult?) = FeatureListScreen("Web Security", "TLS and HTTP security evidence.") {
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("Audit summary", style = MaterialTheme.typography.titleMedium); Text("TLS signals: " + (tls?.evidence?.size ?: 0) + " • HTTP signals: " + (http?.evidence?.size ?: 0)); Text("Review signals: " + ((tls?.evidence?.count { it.contains("expired", true) || it.contains("invalid", true) || it.contains("failed", true) } ?: 0) + (http?.evidence?.count { it.contains("missing", true) || it.contains("insecure", true) || it.contains("absent", true) } ?: 0))) } }
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("TLS", style = MaterialTheme.typography.titleMedium); if (tls == null) Text("No TLS audit recorded.") else tls.evidence.forEach { Text(it) } } }
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("HTTP", style = MaterialTheme.typography.titleMedium); if (http == null) Text("No HTTP audit recorded.") else { http.evidence.forEach { Text(it) }; val fp = WebServiceFingerprintEngine.from(http.url, http.headers); Text("Server: " + (fp.server ?: "Not disclosed")); Text("Version evidence: " + (fp.versionEvidence ?: "Not available")) } } }
}

@Composable fun PolicyFeatureScreen(results: List<PolicyResult>, profile: String, onProfile: (String) -> Unit) = FeatureListScreen("Security Policies", "Evaluate posture using configurable profiles.") {
    val pass = results.count { it.status.name == "PASS" }; Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("Policy posture", style = MaterialTheme.typography.titleMedium); Text(pass.toString() + " passing • " + (results.size - pass) + " requiring review • Profile: " + profile); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { LoadingTextButton(onClick = { onProfile("Home") }) { Text("Home") }; LoadingTextButton(onClick = { onProfile("Work") }) { Text("Work") } } } }
    results.forEach { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { Text(it.status.name + " — " + it.title, style = MaterialTheme.typography.titleMedium); Text(it.description) } } }
}

@Composable fun TimelineFeatureScreen(
    events: List<SecurityTimelineEvent>,
    onExport: () -> Unit
) = FeatureListScreen("Security Timeline", "Replay what changed, why it matters, and what to verify.") {
    var type by remember { mutableStateOf<com.uttarooque73.netguard.features.timeline.IncidentEventType?>(null) }
    var severity by remember { mutableStateOf<com.uttarooque73.netguard.features.timeline.IncidentSeverity?>(null) }
    var expandedId by remember { mutableStateOf<String?>(null) }
    val incidents = com.uttarooque73.netguard.features.timeline.IncidentTimelineEngine.replay(events)
    val filtered = incidents.filter { type == null || it.type == type }.filter { severity == null || it.severity == severity }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Incident replay", style = MaterialTheme.typography.titleMedium)
            Text(filtered.size.toString() + " events • " + incidents.count { it.severity == com.uttarooque73.netguard.features.timeline.IncidentSeverity.HIGH } + " high • " + incidents.count { it.severity == com.uttarooque73.netguard.features.timeline.IncidentSeverity.REVIEW } + " review")
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(type == null, { type = null }, label = { Text("All") })
                com.uttarooque73.netguard.features.timeline.IncidentEventType.values().forEach { t ->
                    FilterChip(type == t, { type = if (type == t) null else t }, label = { Text(t.name) })
                }
            }
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(severity == null, { severity = null }, label = { Text("All severity") })
                com.uttarooque73.netguard.features.timeline.IncidentSeverity.values().forEach { s ->
                    FilterChip(severity == s, { severity = if (severity == s) null else s }, label = { Text(s.name) })
                }
            }
            LoadingTextButton(onClick = onExport) { Text("Export incident timeline") }
        }
    }

    if (filtered.isEmpty()) {
        Text("No security events match the current filters.")
    }
    filtered.forEach { incident ->
        val expanded = expandedId == incident.source.id
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(incident.source.title, style = MaterialTheme.typography.titleMedium)
                Text(incident.type.name + " • " + incident.severity.name + " • " + incident.entity)
                Text(java.text.DateFormat.getDateTimeInstance().format(java.util.Date(incident.source.createdAtEpochMs)))
                Text("What happened", style = MaterialTheme.typography.labelLarge)
                Text(incident.source.detail)
                if (expanded) {
                    Text("Evidence", style = MaterialTheme.typography.labelLarge)
                    Text(incident.evidence)
                    Text("Why it matters", style = MaterialTheme.typography.labelLarge)
                    Text(incident.whyItMatters)
                    Text("Recommended action", style = MaterialTheme.typography.labelLarge)
                    Text(incident.recommendedAction)
                    val previous = incidents.dropWhile { it.source.id != incident.source.id }.drop(1).firstOrNull()
                    Text("Replay context", style = MaterialTheme.typography.labelLarge)
                    Text(if (previous == null) "This is the earliest related event in the current local timeline." else "Previous recorded event: " + previous.source.title)
                }
                LoadingTextButton(onClick = { expandedId = if (expanded) null else incident.source.id }) {
                    Text(if (expanded) "Hide investigation details" else "Replay event")
                }
            }
        }
    }
}
