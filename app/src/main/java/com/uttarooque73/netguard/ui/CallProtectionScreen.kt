package com.uttarooque73.netguard.ui

import android.app.role.RoleManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uttarooque73.netguard.security.CallProtectionLog
import java.text.DateFormat
import java.util.Date

@Composable
fun CallProtectionScreen(
    logs: List<CallProtectionLog>,
    blockedNumbers: Set<String>,
    onBlock: (String) -> Unit,
    onUnblock: (String) -> Unit,
    onClearLogs: () -> Unit,
    onEnableScreening: () -> Unit,
    screeningEnabled: Boolean,
    callLogError: String?,
    onRequestCallLogPermission: () -> Unit,
    callLogPermissionGranted: Boolean
) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Call Protection", style = MaterialTheme.typography.headlineSmall)
        Text("Review recent call history, see screening events, and block unwanted numbers locally.")

        if (!callLogPermissionGranted) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Call history permission is not granted.", style = MaterialTheme.typography.titleMedium)
                    Text("NetGuard needs this permission only to display your recent system call history here.")
                    callLogError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    LoadingButton(onClick = onRequestCallLogPermission) { Text("Allow call history") }
                }
            }
        }

        if (!screeningEnabled) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Call screening is not enabled.")
                    LoadingButton(onClick = onEnableScreening) { Text("Enable call screening") }
                }
            }
        } else Text("Call screening is enabled.", color = MaterialTheme.colorScheme.primary)

        Text("Blocked numbers (" + blockedNumbers.size + ")", style = MaterialTheme.typography.titleMedium)
        blockedNumbers.forEach { number ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(number, Modifier.weight(1f))
                LoadingTextButton(onClick = { onUnblock(number) }) { Text("Unblock") }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Recent calls", style = MaterialTheme.typography.titleMedium)
            LoadingTextButton(onClick = onClearLogs) { Text("Clear") }
        }

        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(logs, key = { it.id }) { log ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(log.contactName ?: log.number, style = MaterialTheme.typography.titleSmall)
                        if (log.contactName != null) Text(log.number)
                        Text(log.direction + " • " + DateFormat.getDateTimeInstance().format(Date(log.timestamp)))
                        Text(if (log.blocked) "Blocked by NetGuard" else "Allowed")
                        if (!log.blocked && log.number.isNotBlank() && log.number != "Unknown number") {
                            LoadingTextButton(onClick = { onBlock(log.number) }) { Text("Block number") }
                        }
                    }
                }
            }
        }
    }
}

fun requestCallScreeningRole(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = context.getSystemService(RoleManager::class.java)
        if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
            context.startActivity(roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
        }
    }
}
