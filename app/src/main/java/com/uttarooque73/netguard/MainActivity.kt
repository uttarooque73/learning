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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uttarooque73.netguard.network.NetworkDiscovery
import com.uttarooque73.netguard.network.NetworkInfo

class MainActivity : ComponentActivity() {
    private var networkInfo by mutableStateOf<NetworkInfo?>(null)

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) inspectNetwork()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NetGuardApp(
                networkInfo = networkInfo,
                onStartAudit = ::requestNetworkPermissionAndInspect
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
        networkInfo = NetworkDiscovery(this).inspect()
    }
}

@Composable
fun NetGuardApp(networkInfo: NetworkInfo?, onStartAudit: () -> Unit) {
    MaterialTheme {
        Scaffold(topBar = { TopAppBar(title = { Text("NetGuard") }) }) { padding ->
            Dashboard(
                modifier = Modifier.padding(padding),
                networkInfo = networkInfo,
                onStartAudit = onStartAudit
            )
        }
    }
}

@Composable
private fun Dashboard(
    modifier: Modifier = Modifier,
    networkInfo: NetworkInfo?,
    onStartAudit: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Network Security Audit", style = MaterialTheme.typography.headlineSmall)
        Text("Discover → Audit → Remediate → Verify", style = MaterialTheme.typography.bodyLarge)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Current network", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                if (networkInfo == null) {
                    Text("Not inspected yet")
                    Text("Start an authorized network inspection to collect local network details.")
                } else {
                    Text("SSID: ${networkInfo.ssid ?: "Unavailable"}")
                    Text("Local IP: ${networkInfo.localAddress ?: "Unavailable"}")
                    Text("Gateway: ${networkInfo.gatewayAddress ?: "Unavailable"}")
                    Text("Subnet: ${networkInfo.subnet ?: "Unavailable"}")
                    Text("DNS: ${networkInfo.dnsServers.ifEmpty { listOf("Unavailable") }.joinToString()}")
                    Text("Interface: ${networkInfo.interfaceName ?: "Unavailable"}")
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onStartAudit, modifier = Modifier.weight(1f)) {
                Text(if (networkInfo == null) "Inspect Network" else "Refresh")
            }
            Button(onClick = { }, modifier = Modifier.weight(1f)) {
                Text("Settings")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Security posture", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("No audit findings yet")
                Text("Phase 3 will add authorized service and exposure checks.")
            }
        }
    }
}
