package com.uttarooque73.netguard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.DateFormat
import java.util.Date
import androidx.lifecycle.lifecycleScope
import com.uttarooque73.netguard.network.DeviceDiscovery
import com.uttarooque73.netguard.network.DiscoveredDevice
import com.uttarooque73.netguard.network.NetworkDiscovery
import com.uttarooque73.netguard.network.NetworkInfo
import com.uttarooque73.netguard.network.NetworkInventoryStore
import com.uttarooque73.netguard.audit.DiscoveredService
import com.uttarooque73.netguard.audit.ServiceAudit
import com.uttarooque73.netguard.audit.ServiceAuditStore
import com.uttarooque73.netguard.audit.Finding
import com.uttarooque73.netguard.audit.FindingStore
import com.uttarooque73.netguard.audit.ServiceFindingRules
import com.uttarooque73.netguard.audit.RiskCalculator
import com.uttarooque73.netguard.remediation.RemediationCatalog
import com.uttarooque73.netguard.remediation.RemediationRecord
import com.uttarooque73.netguard.remediation.RemediationStatus
import com.uttarooque73.netguard.remediation.RemediationStore
import com.uttarooque73.netguard.verification.VerificationEngine
import com.uttarooque73.netguard.verification.VerificationResult
import com.uttarooque73.netguard.verification.VerificationStore
import com.uttarooque73.netguard.report.AuditHistoryStore
import com.uttarooque73.netguard.report.AuditReportGenerator
import com.uttarooque73.netguard.report.AuditSnapshot
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var networkInfo by mutableStateOf<NetworkInfo?>(null)
    private var devices by mutableStateOf<List<DiscoveredDevice>>(emptyList())
    private var isDiscovering by mutableStateOf(false)
    private var discoveryError by mutableStateOf<String?>(null)
    private var selectedScreen by mutableStateOf(Screen.Dashboard)
    private lateinit var inventoryStore: NetworkInventoryStore
    private lateinit var serviceStore: ServiceAuditStore
    private var services by mutableStateOf<List<DiscoveredService>>(emptyList())
    private var auditingIp by mutableStateOf<String?>(null)
    private var findings by mutableStateOf<List<Finding>>(emptyList())
    private var selectedFinding by mutableStateOf<Finding?>(null)
    private var remediationRecords by mutableStateOf<List<RemediationRecord>>(emptyList())
    private lateinit var remediationStore: RemediationStore
    private lateinit var verificationStore: VerificationStore
    private lateinit var auditHistoryStore: AuditHistoryStore
    private var auditHistory by mutableStateOf<List<com.uttarooque73.netguard.report.AuditHistoryEntry>>(emptyList())
    private var latestReport by mutableStateOf<String?>(null)
    private var verificationResults by mutableStateOf<List<VerificationResult>>(emptyList())
    private var verifyingFindingId by mutableStateOf<String?>(null)
    private lateinit var findingStore: FindingStore

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) inspectNetwork()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inventoryStore = NetworkInventoryStore(this)
        serviceStore = ServiceAuditStore(this)
        findingStore = FindingStore(this)
        remediationStore = RemediationStore(this)
        verificationStore = VerificationStore(this)
        auditHistoryStore = AuditHistoryStore(this)
        services = serviceStore.load()
        findings = findingStore.load()
        remediationRecords = remediationStore.load()
        verificationResults = verificationStore.load()
        auditHistory = auditHistoryStore.load()
        networkInfo = inventoryStore.loadNetwork()
        devices = inventoryStore.loadDevices()
        setContent {
            NetGuardApp(
                networkInfo = networkInfo,
                devices = devices,
                isDiscovering = isDiscovering,
                onStartAudit = ::requestNetworkPermissionAndInspect,
                discoveryError = discoveryError,
                selectedScreen = selectedScreen,
                onSelectScreen = { selectedScreen = it },
                onDiscoverDevices = ::discoverDevices,
                services = services,
                auditingIp = auditingIp,
                onAuditDevice = ::auditDevice,
                findings = findings,
                selectedFinding = selectedFinding,
                onSelectFinding = { selectedFinding = it },
                remediationRecords = remediationRecords,
                onStartRemediation = ::startRemediation,
                verificationResults = verificationResults,
                verifyingFindingId = verifyingFindingId,
                onVerifyFinding = ::verifyFinding,
                auditHistory = auditHistory,
                latestReport = latestReport,
                onCreateReport = ::createReport
            )
        }
    }

    private fun requestNetworkPermissionAndInspect() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            inspectNetwork()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun inspectNetwork() {
        discoveryError = null
        runCatching { NetworkDiscovery(this).inspect() }
            .onSuccess { info ->
                networkInfo = info
                devices = emptyList()
                inventoryStore.saveNetwork(info)
                inventoryStore.clearDevices()
            }
            .onFailure { discoveryError = it.message ?: "Unable to inspect the active network." }
    }

    private fun createReport() {
        val snapshot = AuditSnapshot(
            id = java.util.UUID.randomUUID().toString(),
            createdAtEpochMs = System.currentTimeMillis(),
            network = networkInfo,
            devices = devices,
            services = services,
            findings = findings,
            remediationRecords = remediationRecords,
            verificationResults = verificationResults
        )
        val entry = com.uttarooque73.netguard.report.AuditHistoryEntry(snapshot.id, snapshot.createdAtEpochMs, snapshot.devices.size, snapshot.services.size, snapshot.findings.size)
        auditHistory = (auditHistory + entry).takeLast(20)
        auditHistoryStore.save(snapshot)
        latestReport = AuditReportGenerator.generate(snapshot)
    }
    private fun verifyFinding(finding: Finding) {
        if (verifyingFindingId != null) return
        verifyingFindingId = finding.id
        lifecycleScope.launch {
            val beforePresent = findings.any { it.id == finding.id && it.ipAddress == finding.ipAddress }
            val result = VerificationEngine().verify(finding, beforePresent)
            verificationResults = verificationResults.filterNot {
                it.findingId == result.findingId && it.ipAddress == result.ipAddress
            } + result
            verificationStore.save(verificationResults)
            verifyingFindingId = null
        }
    }

    private fun startRemediation(finding: Finding) {
        val record = RemediationRecord(finding.id, finding.ipAddress, RemediationStatus.IN_PROGRESS)
        remediationRecords = remediationRecords.filterNot { it.findingId == finding.id && it.ipAddress == finding.ipAddress } + record
        remediationStore.save(remediationRecords)
    }

    private fun auditDevice(ipAddress: String) {
        lifecycleScope.launch {
            auditingIp = ipAddress
            runCatching { ServiceAudit().audit(ipAddress) }
                .onSuccess { found ->
                    services = services.filterNot { it.ipAddress == ipAddress } + found
                    serviceStore.save(services)
                    val newFindings = found.mapNotNull(ServiceFindingRules::evaluate)
                    findings = findings.filterNot { it.ipAddress == ipAddress } + newFindings
                    findingStore.save(findings)
                }
                .onFailure { discoveryError = it.message ?: "Service audit failed." }
            auditingIp = null
        }
    }

    private fun discoverDevices() {
        val info = networkInfo ?: return
        val localIp = info.localAddress ?: return
        val prefix = info.subnet?.substringAfter('/')?.toIntOrNull() ?: return

        lifecycleScope.launch {
            isDiscovering = true
            discoveryError = null
            runCatching { DeviceDiscovery().discover(localIp, prefix) }
                .onSuccess { found ->
                    devices = found
                    inventoryStore.saveDevices(found)
                }
                .onFailure { discoveryError = it.message ?: "Device discovery failed." }
            isDiscovering = false
        }
    }
}

@Composable
fun NetGuardApp(
    networkInfo: NetworkInfo?,
    devices: List<DiscoveredDevice>,
    isDiscovering: Boolean,
    discoveryError: String?,
    selectedScreen: Screen,
    onSelectScreen: (Screen) -> Unit,
    onStartAudit: () -> Unit,
    onDiscoverDevices: () -> Unit,
    services: List<DiscoveredService>,
    auditingIp: String?,
    onAuditDevice: (String) -> Unit,
    findings: List<Finding>,
    selectedFinding: Finding?,
    onSelectFinding: (Finding?) -> Unit,
    remediationRecords: List<RemediationRecord>,
    onStartRemediation: (Finding) -> Unit,
    verificationResults: List<VerificationResult>,
    verifyingFindingId: String?,
    onVerifyFinding: (Finding) -> Unit,
    auditHistory: List<com.uttarooque73.netguard.report.AuditHistoryEntry>,
    latestReport: String?,
    onCreateReport: () -> Unit
) {
    MaterialTheme {
        Scaffold(topBar = { TopAppBar(title = { Text("NetGuard") }) }) { padding ->
            Dashboard(
                modifier = Modifier.padding(padding),
                networkInfo = networkInfo,
                devices = devices,
                isDiscovering = isDiscovering,
                discoveryError = discoveryError,
                selectedScreen = selectedScreen,
                onSelectScreen = onSelectScreen,
                onStartAudit = onStartAudit,
                onDiscoverDevices = onDiscoverDevices,
                services = services,
                auditingIp = auditingIp,
                onAuditDevice = onAuditDevice,
                findings = findings,
                selectedFinding = selectedFinding,
                onSelectFinding = onSelectFinding,
                remediationRecords = remediationRecords,
                onStartRemediation = onStartRemediation,
                verificationResults = verificationResults,
                verifyingFindingId = verifyingFindingId,
                onVerifyFinding = onVerifyFinding,
                auditHistory = auditHistory,
                latestReport = latestReport,
                onCreateReport = onCreateReport
            )
        }
    }
}

@Composable
private fun Dashboard(
    modifier: Modifier = Modifier,
    networkInfo: NetworkInfo?,
    devices: List<DiscoveredDevice>,
    isDiscovering: Boolean,
    discoveryError: String?,
    selectedScreen: Screen,
    onSelectScreen: (Screen) -> Unit,
    onStartAudit: () -> Unit,
    onDiscoverDevices: () -> Unit,
    services: List<DiscoveredService>,
    auditingIp: String?,
    onAuditDevice: (String) -> Unit,
    findings: List<Finding>,
    selectedFinding: Finding?,
    onSelectFinding: (Finding?) -> Unit,
    remediationRecords: List<RemediationRecord>,
    onStartRemediation: (Finding) -> Unit,
    verificationResults: List<VerificationResult>,
    verifyingFindingId: String?,
    onVerifyFinding: (Finding) -> Unit,
    auditHistory: List<com.uttarooque73.netguard.report.AuditHistoryEntry>,
    latestReport: String?,
    onCreateReport: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TextButton(onClick = { onSelectScreen(Screen.Dashboard) }) { Text("Dashboard") }
            TextButton(onClick = { onSelectScreen(Screen.Network) }) { Text("Network") }
            TextButton(onClick = { onSelectScreen(Screen.Devices) }) { Text("Devices (" + devices.size + ")") }
        }

        when (selectedScreen) {
            Screen.Dashboard -> {
                Text("Network Security Audit", style = MaterialTheme.typography.headlineSmall)
                Text("Discover → Audit → Remediate → Verify", style = MaterialTheme.typography.bodyLarge)

                discoveryError?.let { error ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text("Discovery error: " + error, modifier = Modifier.padding(16.dp))
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Current network", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        if (networkInfo == null) {
                            Text("Not inspected yet")
                            Text("Start an authorized network inspection to collect local network details.")
                        } else {
                            Text("SSID: " + (networkInfo.ssid ?: "Unavailable"))
                            Text("Local IP: " + (networkInfo.localAddress ?: "Unavailable"))
                            Text("Gateway: " + (networkInfo.gatewayAddress ?: "Unavailable"))
                            Text("Subnet: " + (networkInfo.subnet ?: "Unavailable"))
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onStartAudit, modifier = Modifier.weight(1f)) {
                        Text(if (networkInfo == null) "Inspect Network" else "Refresh")
                    }
                    Button(
                        onClick = onDiscoverDevices,
                        enabled = networkInfo != null && !isDiscovering,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isDiscovering) "Discovering…" else "Find Devices")
                    }
                }

                RiskDashboard(findings, onSelectFinding)
                ReportSection(auditHistory, latestReport, onCreateReport)
                selectedFinding?.let { FindingDetail(it, onSelectFinding, remediationRecords, onStartRemediation, verificationResults, verifyingFindingId, onVerifyFinding) }
            }
            Screen.Network -> NetworkScreen(networkInfo)
            Screen.Devices -> DevicesScreen(devices, isDiscovering, services, auditingIp, onAuditDevice, findings, onSelectFinding)
        }
    }
}

enum class Screen { Dashboard, Network, Devices }

@Composable
private fun NetworkScreen(networkInfo: NetworkInfo?) {
    Text("Network Inventory", style = MaterialTheme.typography.headlineSmall)
    if (networkInfo == null) {
        Text("No network inventory available. Inspect the current network first.")
        return
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Connection", style = MaterialTheme.typography.titleMedium)
            Text("SSID: " + (networkInfo.ssid ?: "Unavailable"))
            Text("BSSID: " + (networkInfo.bssid ?: "Unavailable"))
            Text("Interface: " + (networkInfo.interfaceName ?: "Unavailable"))
            Text("Local address: " + (networkInfo.localAddress ?: "Unavailable"))
            Text("Gateway: " + (networkInfo.gatewayAddress ?: "Unavailable"))
            Text("Subnet: " + (networkInfo.subnet ?: "Unavailable"))
            Text("DNS: " + networkInfo.dnsServers.ifEmpty { listOf("Unavailable") }.joinToString())
            Text("Wi-Fi security: " + (networkInfo.wifiSecurity ?: "Not determined"))
        }
    }
}

@Composable
private fun DevicesScreen(
    devices: List<DiscoveredDevice>,
    isDiscovering: Boolean,
    services: List<DiscoveredService>,
    auditingIp: String?,
    onAuditDevice: (String) -> Unit,
    findings: List<Finding>,
    onSelectFinding: (Finding?) -> Unit
) {
    Text("Device Inventory", style = MaterialTheme.typography.headlineSmall)
    if (isDiscovering) CircularProgressIndicator()
    if (!isDiscovering && devices.isEmpty()) Text("No reachable devices have been discovered.")
    devices.forEach { device ->
        val deviceServices = services.filter { it.ipAddress == device.ipAddress }
        val deviceFindings = findings.filter { it.ipAddress == device.ipAddress }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(device.ipAddress, style = MaterialTheme.typography.titleMedium)
                Text("Status: " + if (device.reachable) "Reachable" else "Not reachable")
                Text("Hostname: " + (device.hostname ?: "Unavailable"))
                Button(onClick = { onAuditDevice(device.ipAddress) }, enabled = auditingIp == null) {
                    Text(if (auditingIp == device.ipAddress) "Auditing…" else "Audit Services")
                }
                deviceServices.forEach { service ->
                    Text(service.port.toString() + "/" + service.protocol + " — " + service.serviceName)
                }
                deviceFindings.forEach { finding ->
                    TextButton(onClick = { onSelectFinding(finding) }) { Text(finding.severity.name + ": " + finding.title) }
                }
            }
        }
    }
}

@Composable
private fun RiskDashboard(findings: List<Finding>, onSelectFinding: (Finding?) -> Unit) {
    val score = RiskCalculator.score(findings)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Security posture", style = MaterialTheme.typography.titleMedium)
            Text("Risk score: " + score + "/100")
            Text("Critical: " + findings.count { it.severity == com.uttarooque73.netguard.audit.FindingSeverity.CRITICAL })
            Text("High: " + findings.count { it.severity == com.uttarooque73.netguard.audit.FindingSeverity.HIGH })
            Text("Medium: " + findings.count { it.severity == com.uttarooque73.netguard.audit.FindingSeverity.MEDIUM })
            Text("Low: " + findings.count { it.severity == com.uttarooque73.netguard.audit.FindingSeverity.LOW })
            if (findings.isEmpty()) Text("No findings recorded yet. Audit discovered devices to populate findings.")
            findings.take(5).forEach { finding ->
                TextButton(onClick = { onSelectFinding(finding) }) {
                    Text(finding.severity.name + " — " + finding.title + " (" + finding.ipAddress + ")")
                }
            }
        }
    }
}

@Composable
private fun FindingDetail(
    finding: Finding,
    onClose: (Finding?) -> Unit,
    remediationRecords: List<RemediationRecord>,
    onStartRemediation: (Finding) -> Unit,
    verificationResults: List<VerificationResult>,
    verifyingFindingId: String?,
    onVerifyFinding: (Finding) -> Unit
) {
    val playbook = RemediationCatalog.forFinding(finding.id)
    val record = remediationRecords.lastOrNull { it.findingId == finding.id && it.ipAddress == finding.ipAddress }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(finding.title, style = MaterialTheme.typography.titleLarge)
            Text("Severity: " + finding.severity.name)
            Text("Confidence: " + finding.confidence.name)
            Text("Asset: " + finding.ipAddress)
            Text("Evidence: " + finding.evidence)
            Text("Explanation: " + finding.explanation)
            Text("Remediation: " + finding.remediation)
            playbook?.let {
                Text(it.title, style = MaterialTheme.typography.titleMedium)
                Text("Why it matters: " + it.whyItMatters)
                Text("Prerequisite: " + it.prerequisites.joinToString())
                it.steps.forEachIndexed { index, step -> Text((index + 1).toString() + ". " + step) }
                Text("Verification: " + it.verification)
                Text("Status: " + (record?.status?.name ?: RemediationStatus.NOT_STARTED.name))
                val verification = verificationResults.lastOrNull {
                    it.findingId == finding.id && it.ipAddress == finding.ipAddress
                }
                Text("Verification status: " + (verification?.status?.name ?: "NOT_VERIFIED"))
                verification?.let {
                    Text("Before: " + it.beforeEvidence)
                    Text("After: " + it.afterEvidence)
                }
                Button(
                    onClick = { onVerifyFinding(finding) },
                    enabled = verifyingFindingId == null
                ) {
                    Text(if (verifyingFindingId == finding.id) "Verifying..." else "Verify now")
                }
                Button(
                    onClick = { onStartRemediation(finding) },
                    enabled = record?.status != RemediationStatus.IN_PROGRESS
                ) {
                    Text(if (record?.status == RemediationStatus.IN_PROGRESS) "Remediation in progress" else "Start guided remediation")
                }
            }
            TextButton(onClick = { onClose(null) }) { Text("Close") }
        }
    }
}

@Composable
private fun ReportSection(
    history: List<com.uttarooque73.netguard.report.AuditHistoryEntry>,
    latestReport: String?,
    onCreateReport: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Reports & audit history", style = MaterialTheme.typography.titleMedium)
            Button(onClick = onCreateReport) { Text("Create audit report") }
            Text("Saved audits: " + history.size)
            history.takeLast(5).reversed().forEach { entry ->
                Text(entry.id + " — devices " + entry.deviceCount + ", services " + entry.serviceCount + ", findings " + entry.findingCount)
            }
            latestReport?.let { report ->
                Text("Latest report", style = MaterialTheme.typography.titleSmall)
                Text(report)
            }
        }
    }
}