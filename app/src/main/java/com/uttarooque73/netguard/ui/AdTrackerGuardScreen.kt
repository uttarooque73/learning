package com.uttarooque73.netguard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.net.VpnService
import androidx.core.content.ContextCompat
import com.uttarooque73.netguard.vpn.NetGuardVpnService
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.uttarooque73.netguard.features.adguard.*

@Composable
fun AdTrackerGuardScreen(){
    val context=LocalContext.current
    val store=remember{AdBlockStore(context)}
    var enabled by remember{mutableStateOf(store.enabled())}
    var rules by remember{mutableStateOf(store.rules())}
    var stats by remember{mutableStateOf(store.stats())}
    var vpnRunning by remember{mutableStateOf(NetGuardVpnService.isRunning)}
    val vpnPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            ContextCompat.startForegroundService(context, Intent(context, NetGuardVpnService::class.java))
            store.setEnabled(true)
            enabled = true
            vpnRunning = true
        }
    }
    fun requestVpn() {
        val intent = VpnService.prepare(context)
        if (intent != null) vpnPermissionLauncher.launch(intent)
        else {
            ContextCompat.startForegroundService(context, Intent(context, NetGuardVpnService::class.java))
            store.setEnabled(true)
            enabled = true
            vpnRunning = true
        }
    }
    fun stopVpn() {
        context.startService(Intent(context, NetGuardVpnService::class.java).setAction(NetGuardVpnService.ACTION_STOP))
        store.setEnabled(false)
        enabled = false
        vpnRunning = false
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        Text("Ad & Tracker Guard",style=MaterialTheme.typography.headlineMedium)
        Text("One control center for advertising and common tracking domains across your device. Blocking requires the local VPN/DNS engine to be active.",color=MaterialTheme.colorScheme.onSurfaceVariant)
        Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Text(if(vpnRunning)"Protection active" else "Protection inactive",style=MaterialTheme.typography.titleMedium)
            Text(if(vpnRunning) "Local VPN is filtering DNS requests against your enabled rules." else "Start the local VPN to activate domain filtering.")
            Text("Rules: "+rules.count{it.enabled}+" enabled")
            if (vpnRunning) {
                LoadingButton(onClick={::stopVpn}){Text("Stop protection")}
            } else {
                LoadingButton(onClick={::requestVpn}){Text("Start protection")}
            }
        }}
        Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Text("Blocked activity",style=MaterialTheme.typography.titleMedium)
            Text(stats.blockedRequests.toString()+" requests")
            Text(stats.blockedDomains.toString()+" domains")
            Text("Last: "+(stats.lastBlockedDomain?:"No blocked domain recorded"))
            LoadingTextButton(onClick={store.resetStats();stats=store.stats()}){Text("Reset statistics")}
        }}
        Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
            Text("Protection rules",style=MaterialTheme.typography.titleMedium)
            rules.forEachIndexed{index,rule->
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    Column(Modifier.weight(1f)){Text(rule.domain);Text(rule.category,style=MaterialTheme.typography.bodySmall)}
                    Switch(checked=rule.enabled,onCheckedChange={v->rules=rules.toMutableList().also{it[index]=rule.copy(enabled=v)};store.saveRules(rules)})
                }
            }
        }}
        Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
            Text("How it works",style=MaterialTheme.typography.titleMedium)
            Text("NetGuard uses Android's local VPN interface to receive DNS traffic, checks the requested domain against your enabled rules, and blocks matching domains. Allowed DNS queries are forwarded to an upstream resolver.")
            Text("No remote VPN server is used. NetGuard does not decrypt HTTPS traffic or inspect page contents.")
            Text("Limitations: apps using encrypted DNS (DoH/DoT) or direct IP connections may bypass DNS-based filtering. Android also permits only one active VPN at a time.")
        }}
    }
}
