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