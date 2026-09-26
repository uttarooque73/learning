package com.uttarooque73.netguard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.content.Intent
import androidx.fragment.app.FragmentActivity
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.core.content.FileProvider
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.uttarooque73.netguard.network.DeviceDiscovery
import com.uttarooque73.netguard.network.DiscoveredDevice
import com.uttarooque73.netguard.network.NetworkDiscovery
import com.uttarooque73.netguard.network.NetworkInfo
import com.uttarooque73.netguard.network.NetworkInventoryStore
import com.uttarooque73.netguard.mobile.MobileSecurityAudit
import com.uttarooque73.netguard.mobile.MobileSecurityCheck
import com.uttarooque73.netguard.mobile.MobileSecuritySnapshot
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
import com.uttarooque73.netguard.monitor.MonitorEvent
import com.uttarooque73.netguard.monitor.MonitorStore
import com.uttarooque73.netguard.monitor.MonitorBaselineStore
import com.uttarooque73.netguard.monitor.MonitorRunner
import com.uttarooque73.netguard.compliance.BaselineEvaluator
import com.uttarooque73.netguard.compliance.BaselineResult
import com.uttarooque73.netguard.compliance.BaselineStore
import com.uttarooque73.netguard.compliance.DefaultBaselines
import com.uttarooque73.netguard.admin.AdminStore
import com.uttarooque73.netguard.admin.AdminEvent
import com.uttarooque73.netguard.admin.AdminEventType
import com.uttarooque73.netguard.admin.AssetMetadata
import com.uttarooque73.netguard.admin.NetworkProfile
import kotlinx.coroutines.launch
import com.uttarooque73.netguard.features.apps.AppSecurityCheck
import com.uttarooque73.netguard.features.apps.InstalledAppSecurityAudit
import com.uttarooque73.netguard.features.network.DnsGatewayAudit
import com.uttarooque73.netguard.features.network.DnsGatewayAuditResult
import com.uttarooque73.netguard.features.wifi.WifiObservation
import com.uttarooque73.netguard.features.wifi.WifiTrustEngine
import com.uttarooque73.netguard.features.wifi.WifiTrustResult
import com.uttarooque73.netguard.features.wifi.WifiObservationStore
import com.uttarooque73.netguard.features.web.TlsHttpSecurityAudit
import com.uttarooque73.netguard.features.web.TlsAuditResult
import com.uttarooque73.netguard.features.web.HttpSecurityResult
import com.uttarooque73.netguard.features.policy.PolicyInput
import com.uttarooque73.netguard.features.policy.PolicyResult
import com.uttarooque73.netguard.features.policy.SecurityPolicyEngine
import com.uttarooque73.netguard.features.policy.SecurityPolicyProfiles
import com.uttarooque73.netguard.features.learning.SecurityLearningMode
import com.uttarooque73.netguard.features.reporting.AdvancedReportExporter
import com.uttarooque73.netguard.features.reporting.AuditPackageImporter
import com.uttarooque73.netguard.features.intelligence.NetworkTopology
import com.uttarooque73.netguard.features.intelligence.NetworkTopologyBuilder
import com.uttarooque73.netguard.features.intelligence.RiskTrendPoint
import com.uttarooque73.netguard.features.intelligence.RiskTrendCalculator
import com.uttarooque73.netguard.features.network.DnsSecurityAnalyzer
import com.uttarooque73.netguard.features.network.DnsSecurityResult
import com.uttarooque73.netguard.features.vulnerability.CandidateVulnerabilityMapper
import com.uttarooque73.netguard.features.vulnerability.VulnerabilityCandidate
import com.uttarooque73.netguard.ui.IntelligenceScreen
import com.uttarooque73.netguard.ui.ServicesFeatureScreen
import com.uttarooque73.netguard.ui.FindingsFeatureScreen
import com.uttarooque73.netguard.ui.RemediationFeatureScreen
import com.uttarooque73.netguard.ui.WifiFeatureScreen
import com.uttarooque73.netguard.ui.WebFeatureScreen
import com.uttarooque73.netguard.ui.PolicyFeatureScreen
import com.uttarooque73.netguard.ui.TimelineFeatureScreen
import com.uttarooque73.netguard.features.timeline.SecurityTimelineEvent
import com.uttarooque73.netguard.features.timeline.SecurityTimelineStore
import com.uttarooque73.netguard.features.policy.CustomPolicy
import com.uttarooque73.netguard.features.policy.CustomPolicyStore
import com.uttarooque73.netguard.features.policy.CustomPolicyEvaluator
import com.uttarooque73.netguard.features.policy.CustomPolicyEvaluation
import com.uttarooque73.netguard.features.policy.CustomPolicyRuleType
import com.uttarooque73.netguard.monitor.ScheduledMonitorConfigStore
import com.uttarooque73.netguard.monitor.ScheduledMonitorScheduler
import com.uttarooque73.netguard.security.AppLockPolicyStore

class MainActivity : FragmentActivity() {
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
    private lateinit var monitorStore: MonitorStore
    private lateinit var monitorBaselineStore: MonitorBaselineStore
    private var monitorEvents by mutableStateOf<List<MonitorEvent>>(emptyList())
    private var monitoring by mutableStateOf(false)
    private lateinit var baselineStore: BaselineStore
    private var baselineResults by mutableStateOf<List<BaselineResult>>(emptyList())
    private lateinit var adminStore: AdminStore
    private var profiles by mutableStateOf<List<NetworkProfile>>(emptyList())
    private var assets by mutableStateOf<List<AssetMetadata>>(emptyList())
    private var adminEvents by mutableStateOf<List<AdminEvent>>(emptyList())
    private var verificationResults by mutableStateOf<List<VerificationResult>>(emptyList())
    private var verifyingFindingId by mutableStateOf<String?>(null)
    private lateinit var findingStore: FindingStore
    private var mobileSecurity by mutableStateOf<MobileSecuritySnapshot?>(null)
    private var mobileAuditRunning by mutableStateOf(false)
    private var appSecurityChecks by mutableStateOf<List<AppSecurityCheck>>(emptyList())
    private var dnsGatewayResult by mutableStateOf<DnsGatewayAuditResult?>(null)
    private var wifiTrustResult by mutableStateOf<WifiTrustResult?>(null)
    private lateinit var wifiObservationStore: WifiObservationStore
    private var tlsResult by mutableStateOf<TlsAuditResult?>(null)
    private var httpResult by mutableStateOf<HttpSecurityResult?>(null)
    private var policyResults by mutableStateOf<List<PolicyResult>>(emptyList())
    private var topology by mutableStateOf<NetworkTopology?>(null)
    private var dnsSecurity by mutableStateOf<DnsSecurityResult?>(null)
    private var vulnerabilityCandidates by mutableStateOf<List<VulnerabilityCandidate>>(emptyList())
    private var riskTrend by mutableStateOf<List<RiskTrendPoint>>(emptyList())
    private var customPolicyEvaluations by mutableStateOf<List<CustomPolicyEvaluation>>(emptyList())
    private var selectedPolicyProfile by mutableStateOf("Home")
    private lateinit var timelineStore: SecurityTimelineStore
    private lateinit var appLockPolicyStore: AppLockPolicyStore
    private var appLocked by mutableStateOf(false)
    private var authenticating = false
    private var backgroundedAtElapsedMs = 0L
    private companion object { const val APP_LOCK_GRACE_MS = 5_000L }
    private var timelineEvents by mutableStateOf<List<SecurityTimelineEvent>>(emptyList())

    private val auditPackageLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) importAuditPackage(uri)
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            inspectNetwork()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appLockPolicyStore = AppLockPolicyStore(this)
        appLocked = appLockPolicyStore.load().enabled
        inventoryStore = NetworkInventoryStore(this)
        serviceStore = ServiceAuditStore(this)
        findingStore = FindingStore(this)
        remediationStore = RemediationStore(this)
        verificationStore = VerificationStore(this)
        auditHistoryStore = AuditHistoryStore(this)
        monitorStore = MonitorStore(this)
        monitorBaselineStore = MonitorBaselineStore(this)
        baselineStore = BaselineStore(this)
        adminStore = AdminStore(this)
        wifiObservationStore = WifiObservationStore(this)
        timelineStore = SecurityTimelineStore(this)
        services = serviceStore.load()
        findings = findingStore.load()
        remediationRecords = remediationStore.load()
        verificationResults = verificationStore.load()
        auditHistory = auditHistoryStore.load()
        monitorEvents = monitorStore.load()
        val savedBaseline = baselineStore.load() ?: DefaultBaselines.secureHomeNetwork().also { baselineStore.save(it) }
        baselineResults = BaselineEvaluator.evaluate(savedBaseline, services)
        profiles = adminStore.loadProfiles()
        assets = adminStore.loadAssets()
        adminEvents = adminStore.loadEvents()
        timelineEvents = timelineStore.load()
        networkInfo = inventoryStore.loadNetwork()
        devices = inventoryStore.loadDevices()
        mobileSecurity = MobileSecurityAudit.inspect(this)
        setContent {
            if (appLocked) {
                LockScreen(onUnlock = ::authenticateApp)
            } else {
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
                onCreateReport = ::createReport,
                onImportAuditPackage = ::launchAuditPackageImport,
                monitoring = monitoring,
                monitorEvents = monitorEvents,
                onCheckChanges = ::checkForChanges,
                baselineResults = baselineResults,
                onEvaluateBaseline = ::evaluateBaseline,
                profiles = profiles,
                assets = assets,
                adminEvents = adminEvents,
                onCreateProfile = ::createProfile,
                onUpdateAsset = ::updateAsset,
                mobileSecurity = mobileSecurity,
                mobileAuditRunning = mobileAuditRunning,
                onRefreshMobileSecurity = ::refreshMobileSecurity,
                appSecurityChecks = appSecurityChecks,
                dnsGatewayResult = dnsGatewayResult,
                wifiTrustResult = wifiTrustResult,
                tlsResult = tlsResult,
                httpResult = httpResult,
                policyResults = policyResults,
                selectedPolicyProfile = selectedPolicyProfile,
                onSelectPolicyProfile = { selectedPolicyProfile = it },
                onRunAdvancedAudit = ::runAdvancedAudit,
                onExportReport = ::exportReport,
                timelineEvents = timelineEvents,
                topology = topology,
                dnsSecurity = dnsSecurity,
                vulnerabilityCandidates = vulnerabilityCandidates,
                riskTrend = riskTrend,
                customPolicyEvaluations = customPolicyEvaluations
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        val policy = appLockPolicyStore.load()
        if (policy.enabled && policy.lockOnBackground && !authenticating) {
            backgroundedAtElapsedMs = SystemClock.elapsedRealtime()
        }
    }

    override fun onResume() {
        super.onResume()
        val policy = appLockPolicyStore.load()
        if (!policy.enabled) {
            appLocked = false
            return
        }
        if (!appLocked && policy.lockOnBackground && backgroundedAtElapsedMs > 0L &&
            SystemClock.elapsedRealtime() - backgroundedAtElapsedMs >= APP_LOCK_GRACE_MS
        ) {
            appLocked = true
        }
        if (appLocked) authenticateApp()
    }

    private fun authenticateApp() {
        if (authenticating) return
        val policy = appLockPolicyStore.load()
        if (!policy.enabled) {
            appLocked = false
            return
        }
        val manager = BiometricManager.from(this)
        val authenticators = if (policy.requireBiometric) {
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        } else {
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        }
        if (manager.canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_SUCCESS) return
        authenticating = true
        val executor = ContextCompat.getMainExecutor(this)
        BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                authenticating = false
                appLocked = false
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                authenticating = false
                appLocked = true
            }
            override fun onAuthenticationFailed() {
                appLocked = true
            }
        }).authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock NetGuard")
                .setSubtitle("Authenticate to access security audit data")
                .setAllowedAuthenticators(authenticators)
                .apply { if (policy.requireBiometric) setNegativeButtonText("Cancel") }
                .build()
        )
    }

    private fun requestNetworkPermissionAndInspect() {
        val fineGranted = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            inspectNetwork()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun recordTimeline(category: String, title: String, detail: String) {
        timelineEvents = (timelineEvents + SecurityTimelineEvent(java.util.UUID.randomUUID().toString(), category, title, detail, System.currentTimeMillis())).takeLast(200)
        timelineStore.save(timelineEvents)
    }

    private fun inspectNetwork() {
        discoveryError = null
        runCatching { NetworkDiscovery(this).inspect() }
            .onSuccess { info ->
                networkInfo = info
                recordTimeline("network", "Network inspected", info.ssid ?: "Active network inspected")
                devices = emptyList()
                services = emptyList()
                findings = emptyList()
                verificationResults = emptyList()
                selectedFinding = null
                val baseline = baselineStore.load() ?: DefaultBaselines.secureHomeNetwork()
                baselineResults = BaselineEvaluator.evaluate(baseline, emptyList())
                inventoryStore.saveNetwork(info)
                inventoryStore.clearDevices()
                monitorBaselineStore.clear()
                serviceStore.clear()
                findingStore.save(emptyList())
                verificationStore.save(emptyList())
            }
            .onFailure { discoveryError = it.message ?: "Unable to inspect the active network." }
    }

    private fun refreshMobileSecurity() {
        if (mobileAuditRunning) return
        mobileAuditRunning = true
        lifecycleScope.launch {
            mobileSecurity = runCatching { MobileSecurityAudit.inspect(this@MainActivity) }
                .getOrNull()
            mobileAuditRunning = false
        }
    }

    private fun exportReport(format: String) {
        val snapshot = AuditSnapshot(
            id = java.util.UUID.randomUUID().toString(),
            createdAtEpochMs = System.currentTimeMillis(),
            network = networkInfo,
            devices = devices,
            services = services,
            findings = findings,
            remediationRecords = remediationRecords,
            verificationResults = verificationResults,
            customPolicyEvaluations = customPolicyEvaluations
        )
        val file = when (format) {
            "json" -> java.io.File(cacheDir, "netguard-audit-" + snapshot.id + ".json").also { it.writeText(AdvancedReportExporter.json(snapshot)) }
            "csv" -> java.io.File(cacheDir, "netguard-audit-" + snapshot.id + ".csv").also { it.writeText(AdvancedReportExporter.csv(snapshot)) }
            "pdf" -> AdvancedReportExporter.pdf(this, snapshot)
            else -> AdvancedReportExporter.packageAudit(this, snapshot)
        }
        val uri = FileProvider.getUriForFile(this, "com.uttarooque73.netguard.fileprovider", file)
        startActivity(Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = when (format) {
                    "json" -> "application/json"
                    "csv" -> "text/csv"
                    "pdf" -> "application/pdf"
                    else -> "application/zip"
                }
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },
            "Share NetGuard report"
        ))
    }

    private fun launchAuditPackageImport() {
        auditPackageLauncher.launch(arrayOf("application/zip", "application/octet-stream"))
    }

    private fun importAuditPackage(uri: android.net.Uri) {
        lifecycleScope.launch {
            runCatching {
                contentResolver.openInputStream(uri)?.use { input ->
                    AuditPackageImporter.readSnapshot(input)
                } ?: error("Unable to open audit package.")
            }.onSuccess { snapshot ->
                auditHistoryStore.save(snapshot)
                auditHistory = auditHistoryStore.load()
                recordTimeline("report", "Audit package imported", "Imported audit ${snapshot.id}")
            }.onFailure {
                discoveryError = "Audit import failed: " + (it.message ?: "invalid package")
            }
        }
    }

    private fun runAdvancedAudit(url: String?) {
        lifecycleScope.launch {
            appSecurityChecks = runCatching { InstalledAppSecurityAudit.inspect(this@MainActivity) }.getOrDefault(emptyList())
            dnsGatewayResult = runCatching { DnsGatewayAudit.inspect(this@MainActivity) }.getOrNull()
            networkInfo?.let { info ->
                val current = WifiObservation(info.ssid, info.bssid, info.gatewayAddress, info.wifiSecurity)
                val previous = wifiObservationStore.load()
                wifiTrustResult = WifiTrustEngine.compare(previous, current)
                wifiObservationStore.save(current)
            }
            if (!url.isNullOrBlank()) {
                if (url.startsWith("https://", true)) {
                    tlsResult = runCatching { TlsHttpSecurityAudit.inspectTls(url) }.getOrNull()
                }
                if (url.startsWith("http://", true) || url.startsWith("https://", true)) {
                    httpResult = runCatching { TlsHttpSecurityAudit.inspectHttp(url) }.getOrNull()
                }
            }
            val mobile = mobileSecurity
            val input = PolicyInput(
                telnetReachable = services.any { it.port == 23 && it.reachable },
                smbReachable = services.any { it.port == 445 && it.reachable },
                httpReachableWithoutHttps = services.any { it.port == 80 && it.reachable } &&
                    services.none { it.port == 443 && it.reachable },
                usbDebugging = mobile?.checks?.any { it.id == "MOB-DEV-002" && it.status == com.uttarooque73.netguard.mobile.MobileCheckStatus.FAIL } == true,
                secureScreenLock = mobile?.checks?.firstOrNull { it.id == "MOB-DEV-003" }?.status ==
                    com.uttarooque73.netguard.mobile.MobileCheckStatus.PASS
            )
            val profile = SecurityPolicyProfiles.defaults().firstOrNull { it.name == selectedPolicyProfile }
                ?: SecurityPolicyProfiles.defaults().first()
            policyResults = SecurityPolicyEngine.evaluate(profile.rules, input)
            val customPolicies = CustomPolicyStore(this@MainActivity).load()
            customPolicyEvaluations = CustomPolicyEvaluator.evaluate(
                policies = customPolicies,
                openPorts = services.filter { it.reachable }.map { it.port }.toSet(),
                hasHttps = services.any { it.reachable && it.port == 443 }
            )
        }
    }

    private fun createProfile() {
        val info = networkInfo ?: return
        val profile = NetworkProfile(java.util.UUID.randomUUID().toString(), info.ssid ?: "Network", info.ssid, info.subnet)
        profiles = (profiles.filterNot { it.ssid == profile.ssid } + profile)
        adminStore.saveProfiles(profiles)
        recordAdminEvent(AdminEventType.PROFILE_CREATED, profile.id, "Created profile ${profile.name}")
    }

    private fun updateAsset(ipAddress: String) {
        val existing = assets.firstOrNull { it.ipAddress == ipAddress }
        val updated = AssetMetadata(ipAddress, existing?.name ?: "", existing?.tags ?: emptyList(), existing?.notes ?: "")
        assets = assets.filterNot { it.ipAddress == ipAddress } + updated
        adminStore.saveAssets(assets)
        recordAdminEvent(AdminEventType.ASSET_UPDATED, ipAddress, "Updated asset metadata")
    }

    private fun recordAdminEvent(type: AdminEventType, subject: String, detail: String) {
        adminEvents = (adminEvents + AdminEvent(java.util.UUID.randomUUID().toString(), type, subject, detail)).takeLast(200)
        adminStore.saveEvents(adminEvents)
    }
    private fun evaluateBaseline() {
        val baseline = baselineStore.load() ?: DefaultBaselines.secureHomeNetwork().also { baselineStore.save(it) }
        baselineResults = BaselineEvaluator.evaluate(baseline, services)
    }
    private fun checkForChanges() {
        if (monitoring) return
        monitoring = true
        lifecycleScope.launch {
            val previousDeviceIps = monitorBaselineStore.loadDeviceIps()
            val previousDevices = previousDeviceIps.map { DiscoveredDevice(it, reachable = true) }
            val previousServices = monitorBaselineStore.loadServices()
            val events = MonitorRunner.check(previousDevices, devices, previousServices, services)
            monitorEvents = (monitorEvents + events).takeLast(100)
            monitorStore.save(monitorEvents)
            monitorBaselineStore.saveDevices(devices)
            monitorBaselineStore.saveServices(services)
            monitoring = false
        }
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
        riskTrend = riskTrend + RiskTrendPoint(snapshot.createdAtEpochMs, RiskCalculator.score(findings), findings.size)
        recordTimeline("report", "Audit report created", "Report ${snapshot.id}")
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
            recordTimeline("verification", "Finding verified", "${finding.id} on ${finding.ipAddress}: ${result.status.name}")
            verifyingFindingId = null
        }
    }

    private fun startRemediation(finding: Finding) {
        val record = RemediationRecord(finding.id, finding.ipAddress, RemediationStatus.IN_PROGRESS)
        remediationRecords = remediationRecords.filterNot { it.findingId == finding.id && it.ipAddress == finding.ipAddress } + record
        remediationStore.save(remediationRecords)
        recordTimeline("remediation", "Remediation started", "${finding.id} on ${finding.ipAddress}")
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
                    recordTimeline("service", "Service audit", "$ipAddress: ${found.size} reachable services")
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
                    recordTimeline("device", "Device discovery", "Discovered ${found.size} reachable devices")
                }
                .onFailure { discoveryError = it.message ?: "Device discovery failed." }
            isDiscovering = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    onCreateReport: () -> Unit,
    onImportAuditPackage: () -> Unit,
    monitoring: Boolean,
    monitorEvents: List<MonitorEvent>,
    onCheckChanges: () -> Unit,
    baselineResults: List<BaselineResult>,
    onEvaluateBaseline: () -> Unit,
    profiles: List<NetworkProfile>,
    assets: List<AssetMetadata>,
    adminEvents: List<AdminEvent>,
    onCreateProfile: () -> Unit,
    onUpdateAsset: (String) -> Unit,
    mobileSecurity: MobileSecuritySnapshot?,
    mobileAuditRunning: Boolean,
    onRefreshMobileSecurity: () -> Unit,
    appSecurityChecks: List<AppSecurityCheck>,
    dnsGatewayResult: DnsGatewayAuditResult?,
    wifiTrustResult: WifiTrustResult?,
    tlsResult: TlsAuditResult?,
    httpResult: HttpSecurityResult?,
    policyResults: List<PolicyResult>,
    selectedPolicyProfile: String,
    onSelectPolicyProfile: (String) -> Unit,
    onRunAdvancedAudit: (String?) -> Unit,
    onExportReport: (String) -> Unit,
    timelineEvents: List<SecurityTimelineEvent>,
    topology: NetworkTopology?,
    dnsSecurity: DnsSecurityResult?,
    vulnerabilityCandidates: List<VulnerabilityCandidate>,
    riskTrend: List<RiskTrendPoint>,
    customPolicyEvaluations: List<CustomPolicyEvaluation>
) {
    MaterialTheme {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val drawerScope = rememberCoroutineScope()
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Text("NETGUARD", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(24.dp))
                    HorizontalDivider()
                    val screens = listOf(
                        Screen.Dashboard, Screen.Network, Screen.Devices, Screen.Services, Screen.Intelligence,
                        Screen.Findings, Screen.Remediation, Screen.Monitoring, Screen.Baseline,
                        Screen.Mobile, Screen.Wifi, Screen.Web, Screen.Policies, Screen.Timeline,
                        Screen.Reports, Screen.Administration, Screen.Learning, Screen.Advanced
                    )
                    screens.forEach { screen ->
                        NavigationDrawerItem(
                            label = { Text(screenTitle(screen)) },
                            selected = selectedScreen == screen,
                            onClick = { onSelectScreen(screen); drawerScope.launch { drawerState.close() } },
                            colors = NavigationDrawerItemDefaults.colors()
                        )
                    }
                }
            }
        ) {
            Scaffold(topBar = { TopAppBar(title = { Text(screenTitle(selectedScreen)) }, navigationIcon = { TextButton(onClick = { drawerScope.launch { drawerState.open() } }) { Text("☰") } }) }) { padding ->
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
                onCreateReport = onCreateReport,
                monitoring = monitoring,
                monitorEvents = monitorEvents,
                onCheckChanges = onCheckChanges,
                baselineResults = baselineResults,
                onEvaluateBaseline = onEvaluateBaseline,
                profiles = profiles,
                assets = assets,
                adminEvents = adminEvents,
                onCreateProfile = onCreateProfile,
                onUpdateAsset = onUpdateAsset,
                mobileSecurity = mobileSecurity,
                mobileAuditRunning = mobileAuditRunning,
                onRefreshMobileSecurity = onRefreshMobileSecurity,
                appSecurityChecks = appSecurityChecks,
                dnsGatewayResult = dnsGatewayResult,
                wifiTrustResult = wifiTrustResult,
                tlsResult = tlsResult,
                httpResult = httpResult,
                policyResults = policyResults,
                selectedPolicyProfile = selectedPolicyProfile,
                onSelectPolicyProfile = onSelectPolicyProfile,
                onImportAuditPackage = onImportAuditPackage,
                onRunAdvancedAudit = onRunAdvancedAudit,
                onExportReport = onExportReport,
                timelineEvents = timelineEvents,
                topology = topology,
                dnsSecurity = dnsSecurity,
                vulnerabilityCandidates = vulnerabilityCandidates,
                riskTrend = riskTrend,
                customPolicyEvaluations = customPolicyEvaluations
            )
        }
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
    onCreateReport: () -> Unit,
    monitoring: Boolean,
    monitorEvents: List<MonitorEvent>,
    onCheckChanges: () -> Unit,
    baselineResults: List<BaselineResult>,
    onEvaluateBaseline: () -> Unit,
    profiles: List<NetworkProfile>,
    assets: List<AssetMetadata>,
    adminEvents: List<AdminEvent>,
    onCreateProfile: () -> Unit,
    onUpdateAsset: (String) -> Unit,
    mobileSecurity: MobileSecuritySnapshot?,
    mobileAuditRunning: Boolean,
    onRefreshMobileSecurity: () -> Unit,
    appSecurityChecks: List<AppSecurityCheck>,
    dnsGatewayResult: DnsGatewayAuditResult?,
    wifiTrustResult: WifiTrustResult?,
    tlsResult: TlsAuditResult?,
    httpResult: HttpSecurityResult?,
    policyResults: List<PolicyResult>,
    selectedPolicyProfile: String,
    onSelectPolicyProfile: (String) -> Unit,
    onImportAuditPackage: () -> Unit,
    onRunAdvancedAudit: (String?) -> Unit,
    onExportReport: (String) -> Unit,
    timelineEvents: List<SecurityTimelineEvent>,
    topology: NetworkTopology?,
    dnsSecurity: DnsSecurityResult?,
    vulnerabilityCandidates: List<VulnerabilityCandidate>,
    riskTrend: List<RiskTrendPoint>,
    customPolicyEvaluations: List<CustomPolicyEvaluation>
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                            Text("Network CIDR: " + (networkInfo.subnet ?: "Unavailable"))
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
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Security workflow", style = MaterialTheme.typography.titleMedium)
                        Text("Open the navigation drawer to access each security capability.")
                    }
                }
            }
            Screen.Network -> NetworkScreen(networkInfo)
            Screen.Devices -> DevicesScreen(devices, isDiscovering, services, auditingIp, onAuditDevice, findings, onSelectFinding)
            Screen.Services -> ServicesFeatureScreen(services)
            Screen.Intelligence -> IntelligenceScreen(devices, services, topology, dnsSecurity, vulnerabilityCandidates, riskTrend)
            Screen.Findings -> FindingsFeatureScreen(findings) { onSelectFinding(it) }
            Screen.Remediation -> RemediationFeatureScreen(findings, remediationRecords, onStartRemediation, verificationResults, onVerifyFinding, verifyingFindingId)
            Screen.Monitoring -> MonitoringSection(monitoring, monitorEvents, onCheckChanges)
            Screen.Baseline -> BaselineSection(baselineResults, onEvaluateBaseline)
            Screen.Mobile -> MobileSecuritySection(mobileSecurity, mobileAuditRunning, onRefreshMobileSecurity)
            Screen.Wifi -> WifiFeatureScreen(wifiTrustResult)
            Screen.Web -> WebFeatureScreen(tlsResult, httpResult)
            Screen.Policies -> PolicyFeatureScreen(policyResults, selectedPolicyProfile, onSelectPolicyProfile)
            Screen.Timeline -> TimelineFeatureScreen(timelineEvents)
            Screen.Reports -> ReportSection(auditHistory, latestReport, onCreateReport, onImportAuditPackage)
            Screen.Administration -> AdministrationSection(profiles, assets, adminEvents, onCreateProfile, onUpdateAsset)
            Screen.Learning -> LearningScreen()
            Screen.Advanced -> AdvancedSecuritySection(appSecurityChecks, dnsGatewayResult, wifiTrustResult, tlsResult, httpResult, policyResults, selectedPolicyProfile, onSelectPolicyProfile, onRunAdvancedAudit, onExportReport, timelineEvents, customPolicyEvaluations)
        }
    }
}

enum class Screen { Dashboard, Network, Devices, Services, Intelligence, Findings, Remediation, Monitoring, Baseline, Mobile, Wifi, Web, Policies, Timeline, Reports, Administration, Learning, Advanced }

private fun screenTitle(screen: Screen): String = when (screen) {
    Screen.Dashboard -> "Overview"
    Screen.Network -> "Network"
    Screen.Devices -> "Devices"
    Screen.Services -> "Services"
    Screen.Intelligence -> "Security Intelligence"
    Screen.Findings -> "Findings"
    Screen.Remediation -> "Remediation"
    Screen.Monitoring -> "Monitoring"
    Screen.Baseline -> "Baseline"
    Screen.Mobile -> "Mobile Security"
    Screen.Wifi -> "Wi-Fi Trust"
    Screen.Web -> "Web Security"
    Screen.Policies -> "Security Policies"
    Screen.Timeline -> "Security Timeline"
    Screen.Reports -> "Reports"
    Screen.Administration -> "Administration"
    Screen.Learning -> "Security Learning"
    Screen.Advanced -> "Advanced Security"
}

@Composable
private fun LearningScreen() {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Security Learning Mode", style = MaterialTheme.typography.headlineMedium)
        SecurityLearningMode.topics.forEach {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(it.title, style = MaterialTheme.typography.titleMedium)
                    Text(it.explanation)
                    Text("Evidence: " + it.evidenceGuide)
                    Text("Remediation: " + it.remediationConcept)
                    Text("Verification: " + it.verificationGuide)
                }
            }
        }
    }
}
@Composable
private fun NetworkScreen(networkInfo: NetworkInfo?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Network Inventory", style = MaterialTheme.typography.headlineSmall)
        if (networkInfo == null) {
            Text("No network inventory available. Inspect the current network first.")
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Connection", style = MaterialTheme.typography.titleMedium)
            Text("SSID: " + (networkInfo.ssid ?: "Unavailable"))
            Text("BSSID: " + (networkInfo.bssid ?: "Unavailable"))
            Text("Interface: " + (networkInfo.interfaceName ?: "Unavailable"))
            Text("Local address: " + (networkInfo.localAddress ?: "Unavailable"))
            Text("Gateway: " + (networkInfo.gatewayAddress ?: "Unavailable"))
            Text("Network CIDR: " + (networkInfo.subnet ?: "Unavailable"))
            Text("DNS: " + networkInfo.dnsServers.ifEmpty { listOf("Unavailable") }.joinToString())
            Text("Wi-Fi security: " + (networkInfo.wifiSecurity ?: "Not determined"))
            }
        }
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                if (deviceServices.isEmpty()) {
                    Text("No catalogued services detected.")
                } else {
                    Text("Services", style = MaterialTheme.typography.titleSmall)
                    deviceServices.forEach { service ->
                        Text(
                            service.port.toString() + "/" + service.protocol + " — " +
                                service.serviceName + if (service.reachable) " — reachable" else " — unavailable"
                        )
                    }
                }
                if (deviceFindings.isNotEmpty()) {
                    Text("Findings", style = MaterialTheme.typography.titleSmall)
                    deviceFindings.forEach { finding ->
                        TextButton(onClick = { onSelectFinding(finding) }) {
                            Text(finding.severity.name + ": " + finding.title)
                        }
                    }
                }
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
    onCreateReport: () -> Unit,
    onImportAuditPackage: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Reports & audit history", style = MaterialTheme.typography.titleMedium)
            Button(onClick = onCreateReport) { Text("Create audit report") }
            Button(onClick = onImportAuditPackage) { Text("Import audit package") }
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
@Composable
private fun MonitoringSection(
    monitoring: Boolean,
    events: List<MonitorEvent>,
    onCheckChanges: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Monitoring & alerts", style = MaterialTheme.typography.titleMedium)
            Text("Checks the existing authorized inventory for changes.")
            Button(onClick = onCheckChanges, enabled = !monitoring) {
                Text(if (monitoring) "Checking..." else "Check for changes")
            }
            Text("Events: " + events.size)
            events.takeLast(5).reversed().forEach { event ->
                Text(event.type.name + " — " + event.ipAddress + " — " + event.detail)
            }
        }
    }
}
@Composable
private fun BaselineSection(
    results: List<BaselineResult>,
    onEvaluate: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Security baseline", style = MaterialTheme.typography.titleMedium)
            Text("Evidence-backed checks for the selected local baseline.")
            Button(onClick = onEvaluate) { Text("Evaluate baseline") }
            results.forEach { result ->
                Text(result.status.name + " — " + result.title)
                Text(result.evidence)
            }
        }
    }
}
@Composable
private fun AdministrationSection(
    profiles: List<NetworkProfile>,
    assets: List<AssetMetadata>,
    events: List<AdminEvent>,
    onCreateProfile: () -> Unit,
    onUpdateAsset: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Administration", style = MaterialTheme.typography.titleMedium)
            Button(onClick = onCreateProfile) { Text("Save current network profile") }
            Text("Profiles: " + profiles.size)
            profiles.takeLast(5).forEach { Text(it.name + " — " + (it.subnet ?: "subnet unavailable")) }
            Text("Asset metadata: " + assets.size)
            assets.takeLast(5).forEach { asset ->
                TextButton(onClick = { onUpdateAsset(asset.ipAddress) }) { Text(asset.ipAddress + " — edit metadata") }
            }
            Text("Administrative events: " + events.size)
            events.takeLast(5).reversed().forEach { Text(it.type.name + " — " + it.subject) }
        }
    }
}

@Composable
private fun MobileSecuritySection(
    snapshot: MobileSecuritySnapshot?,
    running: Boolean,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Mobile Security", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Android device and cellular security posture. Checks are evidence-based; REVIEW does not mean a compromise."
        )
        Button(onClick = onRefresh, enabled = !running) {
            Text(if (running) "Auditing..." else "Refresh mobile audit")

        if (snapshot == null) {
            Text("Mobile security audit has not run yet.")
        } else {
            snapshot.checks.forEach { check ->
                MobileSecurityCheckCard(check)
            }
        }
    }
}
}

@Composable
private fun MobileSecurityCheckCard(check: MobileSecurityCheck) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(check.title, style = MaterialTheme.typography.titleMedium)
            Text("Status: " + check.status.name)
            Text("Evidence: " + check.evidence)
            Text("Why it matters: " + check.whyItMatters)
            Text("How to fix / improve", style = MaterialTheme.typography.titleSmall)
            check.remediation.forEachIndexed { index, step ->
                Text((index + 1).toString() + ". " + step)
            }
            Text("Verification: " + check.verification)
        }
    }
}


@Composable
private fun AdvancedSecuritySection(
    apps: List<AppSecurityCheck>,
    dns: DnsGatewayAuditResult?,
    wifi: WifiTrustResult?,
    tls: TlsAuditResult?,
    http: HttpSecurityResult?,
    policies: List<PolicyResult>,
    selectedPolicyProfile: String,
    onSelectPolicyProfile: (String) -> Unit,
    onRunAudit: (String?) -> Unit,
    onExportReport: (String) -> Unit,
    timelineEvents: List<SecurityTimelineEvent>,
    customPolicyEvaluations: List<CustomPolicyEvaluation>
) {
    var url by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current
    val scheduledStore = remember { ScheduledMonitorConfigStore(context) }
    val lockStore = remember { AppLockPolicyStore(context) }
    val customStore = remember { CustomPolicyStore(context) }
    var scheduled by remember { mutableStateOf(scheduledStore.load()) }
    var lockPolicy by remember { mutableStateOf(lockStore.load()) }
    var customPolicies by remember { mutableStateOf(customStore.load()) }
    var customId by remember { mutableStateOf("") }
    var customTitle by remember { mutableStateOf("") }
    var customDescription by remember { mutableStateOf("") }
    var customRuleType by remember { mutableStateOf(CustomPolicyRuleType.INFORMATIONAL) }
    var customPort by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Advanced Security", style = MaterialTheme.typography.headlineSmall)
        Text("Security operations, scheduling, policy customization, reporting and audit intelligence.")

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Scheduled monitoring", style = MaterialTheme.typography.titleMedium)
                Text("Runs bounded discovery and service audits periodically and records inventory changes locally.")
                Button(onClick = {
                    scheduled = scheduled.copy(enabled = !scheduled.enabled)
                    scheduledStore.save(scheduled)
                    ScheduledMonitorScheduler.apply(context, scheduled)
                }) { Text(if (scheduled.enabled) "Disable schedule" else "Enable schedule") }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1L, 6L, 24L).forEach { hours ->
                        TextButton(onClick = {
                            scheduled = scheduled.copy(intervalHours = hours)
                            scheduledStore.save(scheduled)
                            if (scheduled.enabled) ScheduledMonitorScheduler.apply(context, scheduled)
                        }) { Text(hours.toString() + "h") }
                    }
                }
                TextButton(onClick = {
                    scheduled = scheduled.copy(notifyOnChanges = !scheduled.notifyOnChanges)
                    scheduledStore.save(scheduled)
                }) { Text("Notifications: " + if (scheduled.notifyOnChanges) "ON" else "OFF") }
                Text("Configured interval: " + scheduled.intervalHours + " hours")
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("App protection policy", style = MaterialTheme.typography.titleMedium)
                Text("Policy is persisted locally and exposes the intended protection posture.")
                TextButton(onClick = {
                    lockPolicy = lockPolicy.copy(enabled = !lockPolicy.enabled)
                    lockStore.save(lockPolicy)
                }) { Text("App lock: " + if (lockPolicy.enabled) "ENABLED" else "DISABLED") }
                TextButton(onClick = {
                    lockPolicy = lockPolicy.copy(lockOnBackground = !lockPolicy.lockOnBackground)
                    lockStore.save(lockPolicy)
                }) { Text("Lock on background: " + if (lockPolicy.lockOnBackground) "ON" else "OFF") }
                TextButton(onClick = {
                    lockPolicy = lockPolicy.copy(requireBiometric = !lockPolicy.requireBiometric)
                    lockStore.save(lockPolicy)
                }) { Text("Require biometric: " + if (lockPolicy.requireBiometric) "ON" else "OFF") }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Custom security policies", style = MaterialTheme.typography.titleMedium)
                androidx.compose.material3.OutlinedTextField(value = customId, onValueChange = { customId = it }, label = { Text("Policy ID") }, modifier = Modifier.fillMaxWidth())
                androidx.compose.material3.OutlinedTextField(value = customTitle, onValueChange = { customTitle = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                androidx.compose.material3.OutlinedTextField(value = customDescription, onValueChange = { customDescription = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                TextButton(onClick = {
                    customRuleType = when (customRuleType) {
                        CustomPolicyRuleType.INFORMATIONAL -> CustomPolicyRuleType.BLOCK_PORT
                        CustomPolicyRuleType.BLOCK_PORT -> CustomPolicyRuleType.REQUIRE_HTTPS
                        CustomPolicyRuleType.REQUIRE_HTTPS -> CustomPolicyRuleType.INFORMATIONAL
                    }
                }) { Text("Rule type: " + customRuleType.name) }
                if (customRuleType == CustomPolicyRuleType.BLOCK_PORT) {
                    androidx.compose.material3.OutlinedTextField(
                        value = customPort,
                        onValueChange = { customPort = it.filter(Char::isDigit).take(5) },
                        label = { Text("Port") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Button(onClick = {
                    runCatching {
                        CustomPolicy(
                            id = customId.trim(),
                            title = customTitle.trim(),
                            description = customDescription.trim(),
                            ruleType = customRuleType,
                            port = if (customRuleType == CustomPolicyRuleType.BLOCK_PORT) customPort.toIntOrNull() else null
                        ).also(customStore::upsert)
                    }.onSuccess {
                        customPolicies = customStore.load()
                        customId = ""
                        customTitle = ""
                        customDescription = ""
                        customPort = ""
                        customRuleType = CustomPolicyRuleType.INFORMATIONAL
                    }
                }) { Text("Save policy") }
                customPolicyEvaluations.forEach { evaluation ->
                    Text(evaluation.policy.id + " — " + if (evaluation.passed) "PASS" else "FAIL")
                    Text(evaluation.evidence)
                }
                customPolicies.forEach { policy ->
                    Text(policy.id + " — " + policy.title)
                    Text(policy.description)
                    TextButton(onClick = {
                        customStore.delete(policy.id)
                        customPolicies = customStore.load()
                    }) { Text("Delete") }
                }
            }
        }

        Button(onClick = { onRunAudit(url) }) { Text("Run security audit") }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onExportReport("json") }) { Text("JSON") }
            Button(onClick = { onExportReport("csv") }) { Text("CSV") }
            Button(onClick = { onExportReport("pdf") }) { Text("PDF") }
            Button(onClick = { onExportReport("zip") }) { Text("ZIP") }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Installed applications", style = MaterialTheme.typography.titleMedium)
                Text("Applications analyzed: " + apps.size)
                apps.take(10).forEach {
                    Text(it.appName + " — " + it.packageName)
                    if (it.evidence.isNotEmpty()) Text(it.evidence.joinToString(" "))
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("DNS & Gateway", style = MaterialTheme.typography.titleMedium)
                Text(dns?.evidence?.joinToString(" ") ?: "Not audited")
                dns?.remediation?.forEach { Text("Fix: " + it) }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Wi-Fi trust", style = MaterialTheme.typography.titleMedium)
                Text(wifi?.status?.name ?: "Not audited")
                Text(wifi?.evidence ?: "No observation yet")
                wifi?.remediation?.forEach { Text("Guidance: " + it) }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("TLS / HTTP analyzer", style = MaterialTheme.typography.titleMedium)
                androidx.compose.material3.OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("URL to audit") }, modifier = Modifier.fillMaxWidth())
                tls?.evidence?.forEach { Text(it) }
                http?.evidence?.forEach { Text(it) }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Security policies", style = MaterialTheme.typography.titleMedium)
                Text("Profile: " + selectedPolicyProfile)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { onSelectPolicyProfile("Home") }) { Text("Home") }
                    TextButton(onClick = { onSelectPolicyProfile("Work") }) { Text("Work") }
                }
                policies.forEach { Text(it.status.name + " — " + it.title) }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Security Learning Mode", style = MaterialTheme.typography.titleMedium)
                SecurityLearningMode.topics.forEach {
                    Text(it.title, style = MaterialTheme.typography.titleSmall)
                    Text(it.explanation)
                    Text("Evidence: " + it.evidenceGuide)
                    Text("Remediation: " + it.remediationConcept)
                    Text("Verification: " + it.verificationGuide)
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Security Timeline", style = MaterialTheme.typography.titleMedium)
                timelineEvents.takeLast(10).reversed().forEach {
                    Text(it.category.uppercase() + " — " + it.title)
                    Text(it.detail)
                }
            }
        }
    }
}


@Composable
private fun LockScreen(onUnlock: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("NetGuard locked", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Text("Security audit data is protected. Authenticate to continue.")
        Spacer(Modifier.height(16.dp))
        Button(onClick = onUnlock) { Text("Unlock") }
    }
}
