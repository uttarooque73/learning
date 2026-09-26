package com.uttarooque73.netguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NetGuardApp() }
    }
}

@Composable
fun NetGuardApp() {
    MaterialTheme {
        Scaffold(
            topBar = { TopAppBar(title = { Text("NetGuard") }) }
        ) { padding ->
            Dashboard(modifier = Modifier.padding(padding))
        }
    }
}

@Composable
private fun Dashboard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Network Security Audit", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Discover → Audit → Remediate → Verify",
            style = MaterialTheme.typography.bodyLarge
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Current network", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("Not scanned yet")
                Text("Network discovery will be added in Phase 2.")
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { /* Phase 2: start discovery */ }, modifier = Modifier.weight(1f)) {
                Text("Start Audit")
            }
            Button(onClick = { /* Phase 1: settings screen */ }, modifier = Modifier.weight(1f)) {
                Text("Settings")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Security posture", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("No findings yet")
                Text("Audit results will appear here after the first authorized scan.")
            }
        }
    }
}
