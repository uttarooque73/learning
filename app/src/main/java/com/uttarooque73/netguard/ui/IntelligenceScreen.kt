package com.uttarooque73.netguard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uttarooque73.netguard.audit.DiscoveredService
import com.uttarooque73.netguard.features.intelligence.AssetIntelligence
import com.uttarooque73.netguard.features.intelligence.DeviceIdentityEngine
import com.uttarooque73.netguard.features.intelligence.NetworkTopology
import com.uttarooque73.netguard.features.intelligence.RiskTrendPoint
import com.uttarooque73.netguard.features.network.DnsSecurityResult
import com.uttarooque73.netguard.features.vulnerability.NvdVulnerabilityLookup
import com.uttarooque73.netguard.features.vulnerability.VulnerabilityCandidate
import com.uttarooque73.netguard.features.vulnerability.VulnerabilityReference
import com.uttarooque73.netguard.network.DiscoveredDevice
import kotlinx.coroutines.launch

@Composable
fun IntelligenceScreen(
    devices: List<DiscoveredDevice>,
    services: List<DiscoveredService>,
    topology: NetworkTopology?,
    dns: DnsSecurityResult?,
    candidates: List<VulnerabilityCandidate>,
    trends: List<RiskTrendPoint>
) {
    val scope = rememberCoroutineScope()
    val references = remember { mutableStateMapOf<String, List<VulnerabilityReference>>() }
    val loading = remember { mutableStateMapOf<String, Boolean>() }

    FeatureListScreen(
        "Security Intelligence",
        "Asset fingerprints, topology, DNS posture, vulnerability candidates and risk history."
    ) {
        devices.forEach { device ->
            val fingerprint = AssetIntelligence.fingerprint(device, services)
            val identity = DeviceIdentityEngine.identify(device, services)
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(identity.deviceClass, style = MaterialTheme.typography.titleMedium)
                    Text(device.ipAddress + " • confidence " + identity.confidence)
                    identity.evidence.forEach { Text(it) }
                    Text("Asset fingerprint: " + fingerprint.deviceClass)
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Network topology", style = MaterialTheme.typography.titleMedium)
                Text("Nodes: " + (topology?.nodes?.size ?: 0) + " • Links: " + (topology?.edges?.size ?: 0))
                topology?.nodes?.take(10)?.forEach { Text(it.id + " — " + it.label) }
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
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(candidate.title, style = MaterialTheme.typography.titleMedium)
                    Text(candidate.id + " • " + candidate.confidence + " • " + candidate.ipAddress)
                    Text(candidate.reason)
                    LoadingButton(
                        onClick = {
                            loading[candidate.id + candidate.ipAddress] = true
                            scope.launch {
                                val result = runCatching {
                                    NvdVulnerabilityLookup.search(candidate.service)
                                }.getOrDefault(emptyList())
                                references[candidate.id + candidate.ipAddress] = result
                                loading[candidate.id + candidate.ipAddress] = false
                            }
                        },
                        enabled = loading[candidate.id + candidate.ipAddress] != true
                    ) {
                        Text(if (loading[candidate.id + candidate.ipAddress] == true) "Looking up…" else "Lookup CVEs")
                    }
                    references[candidate.id + candidate.ipAddress]?.let { refs ->
                        Text("NVD results are unverified candidates; service/version matching is required.", style = MaterialTheme.typography.bodySmall)
                        refs.forEach { ref ->
                            Text(ref.cveId, style = MaterialTheme.typography.titleSmall)
                            Text(ref.description.take(280))
                        }
                        if (refs.isEmpty()) Text("No NVD keyword matches returned.")
                    }
                }
            }
        }

        Text("Risk trend points: " + trends.size, style = MaterialTheme.typography.titleMedium)
        trends.takeLast(10).forEach { Text(it.score.toString() + "/100 • findings " + it.findings) }
    }
}