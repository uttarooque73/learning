package com.uttarooque73.netguard.features.command

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uttarooque73.netguard.audit.DiscoveredService
import com.uttarooque73.netguard.audit.Finding
import com.uttarooque73.netguard.monitor.MonitorEvent
import com.uttarooque73.netguard.network.DiscoveredDevice
import com.uttarooque73.netguard.network.NetworkInfo
import com.uttarooque73.netguard.features.wifi.WifiTrustResult

@Composable
fun SecurityCommandCenterScreen(
    network: NetworkInfo?,
    devices: List<DiscoveredDevice>,
    services: List<DiscoveredService>,
    findings: List<Finding>,
    monitorEvents: List<MonitorEvent>,
    wifiTrust: WifiTrustResult?
) {
    var profile by remember { mutableStateOf(AuditProfile.STANDARD) }
    var experimentResult by remember { mutableStateOf<ExperimentResult?>(null) }
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf<String?>(null) }

    val summary = CommandCenterEngine.investigation(devices, services, findings, monitorEvents)
    val contributions = CommandCenterEngine.scoreContributions(findings)

    Column(Modifier.fillMaxWidth().padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Security Command Center", style = MaterialTheme.typography.headlineMedium)
        Text("Investigate security changes using stored, evidence-backed observations.")

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Security Detective", style = MaterialTheme.typography.titleMedium)
                Text("Score \${summary.score}/100 • \${summary.deviceCount} devices • \${summary.serviceCount} services • \${summary.findingCount} findings")
                Text("Stored changes: \${summary.changeCount}")
                summary.recommendations.forEach { Text("→ \$it") }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("What Changed?", style = MaterialTheme.typography.titleMedium)
                if (monitorEvents.isEmpty()) Text("No monitoring changes are currently stored.")
                monitorEvents.takeLast(8).reversed().forEach {
                    Text("\${it.type.name} — \${it.ipAddress}")
                    Text(it.detail)
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Security Graph", style = MaterialTheme.typography.titleMedium)
                if (network == null) {
                    Text("Inspect the network to build the asset graph.")
                } else {
                    Text("Network → \${network.gatewayAddress ?: "gateway unavailable"}")
                    devices.take(8).forEach { device ->
                        val deviceServices = services.filter { it.ipAddress == device.ipAddress }
                        Text("  ↓ \${device.ipAddress} → \${deviceServices.joinToString { "\${it.protocol}/\${it.port}" }.ifBlank { "no observed services" }}")
                        deviceServices.take(4).forEach { service ->
                            findings.filter { it.ipAddress == device.ipAddress }.take(3).forEach { finding ->
                                Text("      ↓ \${service.serviceName} → \${finding.id}")
                            }
                        }
                    }
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Why did my score change?", style = MaterialTheme.typography.titleMedium)
                Text("Current score: \${summary.score}/100")
                if (contributions.isEmpty()) {
                    Text("No finding deductions are currently contributing to the score.")
                } else {
                    contributions.forEach {
                        Text("\${it.findingId} — -\${it.deduction} points — \${it.title}")
                        Text("Asset: \${it.asset} • Severity: \${it.severity}")
                    }
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Safe Audit Profiles", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AuditProfile.values().forEach {
                        if (profile == it) Button(onClick = { profile = it }) { Text(it.label) }
                        else OutlinedButton(onClick = { profile = it }) { Text(it.label) }
                    }
                }
                Text(profile.description)
                Text("Scope only. No exploitation, brute force, stealth, credential attacks, or access-control bypass.")
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Security Experiments", style = MaterialTheme.typography.titleMedium)
                Text("Safe checks use only the existing authorized inventory.")
                CommandCenterEngine.experiments.forEach { experiment ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(experiment.title, modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = {
                            experimentResult = CommandCenterEngine.experiment(
                                experiment, services, network, wifiTrust?.status?.toString()
                            )
                        }) { Text("Run") }
                    }
                }
                experimentResult?.let {
                    Text("\${it.status}: \${it.experiment.title}")
                    Text(it.evidence)
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Evidence Analyst", style = MaterialTheme.typography.titleMedium)
                Text("Offline analyst. Answers are generated only from current stored evidence.")
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ask about score, changes, devices, services or findings") }
                )
                Button(onClick = {
                    answer = CommandCenterEngine.answer(question, network, devices, services, findings, monitorEvents)
                }, enabled = question.isNotBlank()) { Text("Analyze") }
                answer?.let { Text(it) }
            }
        }
    }
}
