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
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var networkInfo by mutableStateOf<NetworkInfo?>(null)
    private var devices by mutableStateOf<List<DiscoveredDevice>>(emptyList())
    private var isDiscovering by mutableStateOf(false)
    private var discoveryError by mutableStateOf<String?>(null)
    private var selectedScreen by mutableStateOf(Screen.Dashboard)
    private lateinit var inventoryStore: NetworkInventoryStore

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) inspectNetwork()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inventoryStore = NetworkInventoryStore(this)
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
                onDiscoverDevices = ::discoverDevices
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
    onDiscoverDevices: () -> Unit
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
                onDiscoverDevices = onDiscoverDevices
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
    onDiscoverDevices: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Network Security Audit", style = MaterialTheme.typography.headlineSmall)
        Text("Discover → Audit → Remediate → Verify", style = MaterialTheme.typography.bodyLarge)

        discoveryError?.let { error ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Text("Discovery error: $error", modifier = Modifier.padding(16.dp))
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
            Button(
                onClick = onDiscoverDevices,
                enabled = networkInfo != null && !isDiscovering,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (isDiscovering) "Discovering…" else "Find Devices")
            }
        }

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
                        Text("SSID: " + (networkInfo?.ssid ?: "Unavailable"))
                        Text("Local IP: " + (networkInfo?.localAddress ?: "Unavailable"))
                        Text("Gateway: " + (networkInfo?.gatewayAddress ?: "Unavailable"))
                        Text("Subnet: " + (networkInfo?.subnet ?: "Unavailable"))
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
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Security posture", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("No audit findings yet")
                        Text("Phase 3 will add authorized service and exposure checks.")
                    }
                }
            }
            Screen.Network -> NetworkScreen(networkInfo)
            Screen.Devices -> DevicesScreen(devices, isDiscovering)
        }
    }
}

private enum class Screen { Dashboard, Network, Devices }

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
private fun DevicesScreen(devices: List<DiscoveredDevice>, isDiscovering: Boolean) {
    Text("Device Inventory", style = MaterialTheme.typography.headlineSmall)
    if (isDiscovering) CircularProgressIndicator()
    if (!isDiscovering && devices.isEmpty()) Text("No reachable devices have been discovered.")
    devices.forEach { device ->
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(device.ipAddress, style = MaterialTheme.typography.titleMedium)
                Text("Status: " + if (device.reachable) "Reachable" else "Not reachable")
                Text("Hostname: " + (device.hostname ?: "Unavailable"))
                if (device.discoveredAtEpochMs > 0) {
                    Text("Discovered: " + DateFormat.getDateTimeInstance().format(Date(device.discoveredAtEpochMs)))
                }
            }
        }
    }
}
