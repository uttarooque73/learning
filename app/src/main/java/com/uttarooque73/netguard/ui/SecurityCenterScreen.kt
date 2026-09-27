package com.uttarooque73.netguard.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.uttarooque73.netguard.audit.DiscoveredService
import com.uttarooque73.netguard.features.apps.AppSecurityCheck
import com.uttarooque73.netguard.features.baseline.SecurityDrift
import com.uttarooque73.netguard.features.baseline.TrustedSecurityBaselineSnapshot
import com.uttarooque73.netguard.features.securitycenter.*
import com.uttarooque73.netguard.mobile.MobileSecuritySnapshot
import com.uttarooque73.netguard.network.DiscoveredDevice
import com.uttarooque73.netguard.network.NetworkInfo
import com.uttarooque73.netguard.ui.LoadingButton

@Composable
fun SecurityCenterScreen(
    network: NetworkInfo?,
    devices: List<DiscoveredDevice>,
    services: List<DiscoveredService>,
    apps: List<AppSecurityCheck>,
    mobile: MobileSecuritySnapshot?,
    baseline: TrustedSecurityBaselineSnapshot?,
    drifts: List<SecurityDrift>,
    trends: List<com.uttarooque73.netguard.features.intelligence.RiskTrendPoint>,
    onRunCheck: () -> Unit,
    onCaptureBaseline: () -> Unit,
    onExportPackage: () -> Unit
) {
    val context=LocalContext.current
    val trust=SecurityFeatureEngine.networkTrust(network,services,mobile)
    val rogue=SecurityFeatureEngine.rogueDevices(baseline?.deviceKeys.orEmpty(),devices)
    val profiles=SecurityFeatureEngine.appProfiles(apps)
    val incidents=SecurityFeatureEngine.correlate(drifts)
    val watchdog=remember{SecurityWatchdogStore(context)}
    var watchState by remember{mutableStateOf(watchdog.state())}
    var integrityOk by remember{mutableStateOf(TamperEvidentSecurityHistory(context).verify())}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        Text("Security Center",style=MaterialTheme.typography.headlineMedium)
        Text("Continuous security, privacy, network and investigation controls.",color=MaterialTheme.colorScheme.onSurfaceVariant)
        SecurityAutopilotCard(network, services, apps, mobile, drifts, onRunCheck)

        Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Text("1. Security Watchdog",style=MaterialTheme.typography.titleMedium)
            Text(if(watchState.enabled)"Enabled • every "+watchState.intervalMinutes+" minutes" else "Disabled")
            Text(if(watchState.lastRunEpochMs==0L)"No watchdog run recorded." else "Last run: "+java.text.DateFormat.getDateTimeInstance().format(java.util.Date(watchState.lastRunEpochMs)))
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){LoadingButton(onClick={if(watchState.enabled)watchdog.disable()else watchdog.enable();watchState=watchdog.state()}){Text(if(watchState.enabled)"Disable watchdog" else "Enable watchdog")};LoadingButton(onClick={watchdog.markRun();onRunCheck()}){Text("Run now")}}
        }}

        Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Text("2. Permission Drift",style=MaterialTheme.typography.titleMedium)
            val pd=drifts.filter{it.id.startsWith("permission-drift-")}
            Text(if(pd.isEmpty())"No application permission/configuration drift detected." else pd.size.toString()+" application changes require review.")
            pd.take(10).forEach{Text(it.title+" • Before: "+it.before+" • After: "+it.after)}
        }}

        Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Text("3. Network Trust Score",style=MaterialTheme.typography.titleMedium)
            Text(trust.score.toString()+"/100",style=MaterialTheme.typography.headlineSmall)
            trust.reasons.ifEmpty{listOf("No current trust deductions detected.")}.forEach{Text("• "+it)}
        }}

        Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Text("4. Rogue Device Detection",style=MaterialTheme.typography.titleMedium)
            Text(if(baseline==null)"Capture a trusted baseline to identify new devices." else rogue.size.toString()+" device(s) are not in the trusted inventory.")
            rogue.take(10).forEach{Text(it.status+" • "+it.ip+" • "+it.evidence)}
        }}

        Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Text("5. Security Event Correlation",style=MaterialTheme.typography.titleMedium)
            if(incidents.isEmpty())Text("No correlated multi-signal incident detected.")
            incidents.forEach{Text(it.severity+" • "+it.title);Text(it.explanation);it.events.take(6).forEach{e->Text("  • "+e)}}
        }}

        Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Text("6. Security Change Center",style=MaterialTheme.typography.titleMedium)
            Text(drifts.size.toString()+" baseline changes • "+drifts.count{it.severity=="HIGH"}+" high • "+drifts.count{it.severity=="REVIEW"}+" review")
            drifts.take(12).forEach{Text(it.severity+" — "+it.title+" | "+it.before+" → "+it.after)}
        }}

        Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Text("7. Attack Surface Map",style=MaterialTheme.typography.titleMedium)
            Text("Phone → "+(network?.ssid ?: "Network unavailable")+" → "+(network?.gatewayAddress ?: "Gateway unavailable")+" → "+devices.size+" devices → "+services.count{it.reachable}+" reachable services")
            Text("Use the Network/Devices/Services screens for evidence-level inspection.")
        }}

        Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Text("8. App Risk Profiles",style=MaterialTheme.typography.titleMedium)
            profiles.take(10).forEach{Text(it.risk.toString()+"/100 • "+it.appName);Text(if(it.reasons.isEmpty())"No local risk signals." else it.reasons.joinToString(" • "))}
        }}

        Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Text("9. Incident Investigation Mode",style=MaterialTheme.typography.titleMedium)
            Text(if(incidents.isEmpty())"No multi-signal investigation currently active." else "Investigation groups "+incidents.size+" correlated incident(s).")
            incidents.forEach{Text(it.title+" — review "+it.events.size+" evidence events.")}
        }}

        Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Text("10. Security Health Trends",style=MaterialTheme.typography.titleMedium)
            Text(if(trends.isEmpty())"Run a full security check to begin trend history." else "Recorded "+trends.size+" posture points.")
            trends.takeLast(6).forEach{Text(it.score.toString()+"/100 • "+it.findings+" findings")}
        }}

        Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Text("11. Emergency Lockdown",style=MaterialTheme.typography.titleMedium)
            Text("Android protects several settings from silent modification. NetGuard opens the appropriate system controls for review.")
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                LoadingButton(onClick={context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))}){Text("Security settings")}
                LoadingButton(onClick={context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))}){Text("Wi-Fi settings")}
            }
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                LoadingButton(onClick={context.startActivity(Intent(Settings.ACTION_VPN_SETTINGS))}){Text("VPN settings")}
                LoadingButton(onClick={context.startActivity(Intent(Settings.ACTION_APPLICATION_SETTINGS))}){Text("App settings")}
            }
        }}

        Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Text("12. Investigation Package",style=MaterialTheme.typography.titleMedium)
            Text("Export the current audit package for offline investigation and evidence retention.")
            LoadingButton(onClick=onExportPackage){Text("Export security package")}
        }}

        Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Text("13. Explain This Finding",style=MaterialTheme.typography.titleMedium)
            Text("Every finding should be interpreted from evidence, impact and remediation—not from the presence of an alert alone.")
            Text("Open Findings → Evidence & remediation for the detailed explanation and verification workflow.")
        }}

        Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Text("14. Security Playbooks",style=MaterialTheme.typography.titleMedium)
            SecurityFeatureEngine.playbooks().forEach{pb->Text(pb.title,style=MaterialTheme.typography.titleSmall);pb.steps.forEachIndexed{i,s->Text((i+1).toString()+". "+s)}}
        }}

        Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Text("15. Tamper-Evident Security History",style=MaterialTheme.typography.titleMedium)
            Text(if(integrityOk)"History integrity verified." else "History integrity check failed — review local security history.")
            LoadingButton(onClick={integrityOk=TamperEvidentSecurityHistory(context).verify()}){Text("Verify history integrity")}
        }}

        Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Text("Trusted Baseline",style=MaterialTheme.typography.titleMedium)
            Text(if(baseline==null)"No baseline captured." else "Captured: "+java.text.DateFormat.getDateTimeInstance().format(java.util.Date(baseline.capturedAtEpochMs)))
            LoadingButton(onClick=onCaptureBaseline){Text(if(baseline==null)"Capture trusted baseline" else "Update trusted baseline")}
        }}
    }
}
