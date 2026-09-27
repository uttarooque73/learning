package com.uttarooque73.netguard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        Text("Ad & Tracker Guard",style=MaterialTheme.typography.headlineMedium)
        Text("One control center for advertising and common tracking domains across your device. Blocking requires the local VPN/DNS engine to be active.",color=MaterialTheme.colorScheme.onSurfaceVariant)
        Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Text(if(enabled)"Protection enabled" else "Protection disabled",style=MaterialTheme.typography.titleMedium)
            Text("Rules: "+rules.count{it.enabled}+" enabled")
            LoadingButton(onClick={enabled=!enabled;store.setEnabled(enabled)}){Text(if(enabled)"Disable protection" else "Enable protection")}
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
            Text("Important",style=MaterialTheme.typography.titleMedium)
            Text("NetGuard will not inspect HTTPS page content or decrypt encrypted traffic. Domain-based filtering is designed to block known advertising/tracking endpoints without reading page contents.")
        }}
    }
}
