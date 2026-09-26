package com.uttarooque73.netguard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uttarooque73.netguard.audit.DiscoveredService
import com.uttarooque73.netguard.features.intelligence.AssetIntelligence
import com.uttarooque73.netguard.features.intelligence.NetworkTopology
import com.uttarooque73.netguard.features.intelligence.RiskTrendPoint
import com.uttarooque73.netguard.features.network.DnsSecurityResult
import com.uttarooque73.netguard.features.vulnerability.VulnerabilityCandidate
import com.uttarooque73.netguard.network.DiscoveredDevice

@Composable
fun IntelligenceScreen(devices: List<DiscoveredDevice>, services: List<DiscoveredService>, topology: NetworkTopology?, dns: DnsSecurityResult?, candidates: List<VulnerabilityCandidate>, trends: List<RiskTrendPoint>) {
    FeatureListScreen(
        "Security Intelligence",
        "Asset fingerprints, topology, DNS posture, vulnerability candidates and risk history."
    ) {
        devices.forEach { device ->
            val fingerprint = AssetIntelligence.fingerprint(device, services)
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(fingerprint.deviceClass, style = MaterialTheme.typography.titleMedium)
                    Text(device.ipAddress + " • confidence " + fingerprint.confidence)
                    fingerprint.evidence.forEach { Text(it) }
                }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Network topology", style = MaterialTheme.typography.titleMedium)
                Text("Nodes: " + (topology?.nodes?.size ?: 0) + " • Links: " + (topology?.edges?.size ?: 0))
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("DNS security", style = MaterialTheme.typography.titleMedium)
                Text(dns?.evidence?.joinToString(" ") ?: "Not analyzed")
            }
        }
        candidates.forEach { candidate ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(candidate.title, style = MaterialTheme.typography.titleMedium)
                    Text(candidate.id + " • " + candidate.confidence + " • " + candidate.ipAddress)
                    Text(candidate.reason)
                }
            }
        }
        Text("Risk trend points: " + trends.size, style = MaterialTheme.typography.titleMedium)
        trends.takeLast(10).forEach { Text(it.score.toString() + "/100 • findings " + it.findings) }
    }
}