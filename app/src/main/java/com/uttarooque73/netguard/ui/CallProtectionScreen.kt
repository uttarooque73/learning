package com.uttarooque73.netguard.ui

import android.app.role.RoleManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.uttarooque73.netguard.security.CallProtectionLog
import java.text.DateFormat
import java.util.Date

private enum class CallFilter { ALL, BLOCKED, ALLOWED, MISSED }

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
    var filter by remember { mutableStateOf(CallFilter.ALL) }
    var numberToBlock by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }

    val blockedCalls = logs.count { it.blocked }
    val missedCalls = logs.count { it.direction.equals("Missed", ignoreCase = true) }
    val filteredLogs = logs.filter {
        when (filter) {
            CallFilter.ALL -> true
            CallFilter.BLOCKED -> it.blocked
            CallFilter.ALLOWED -> !it.blocked
            CallFilter.MISSED -> it.direction.equals("Missed", ignoreCase = true)
        }
    }

    fun addManualBlock() {
        val normalized = numberToBlock.filter(Char::isDigit).takeLast(15)
        when {
            normalized.length < 7 -> inputError = "Enter a valid phone number."
            blockedNumbers.contains(normalized) -> inputError = "This number is already blocked."
            else -> {
                onBlock(numberToBlock)
                numberToBlock = ""
                inputError = null
            }
        }
    }

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Column {
                Text("Call Protection", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Block unwanted numbers, review recent calls, and control Android call screening.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProtectionStat("Blocked calls", blockedCalls.toString(), Modifier.weight(1f))
                ProtectionStat("Blocked list", blockedNumbers.size.toString(), Modifier.weight(1f))
                ProtectionStat("Missed", missedCalls.toString(), Modifier.weight(1f))
            }
        }

        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (screeningEnabled)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        if (screeningEnabled) "● Call screening is active" else "○ Call screening is off",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        if (screeningEnabled)
                            "NetGuard can apply your local blocklist through Android's call-screening service."
                        else
                            "Enable the Android call-screening role before blocked numbers can be automatically rejected.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!screeningEnabled) {
                        LoadingButton(onClick = onEnableScreening) { Text("Enable call screening") }
                    }
                }
            }
        }

        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Call history", style = MaterialTheme.typography.titleMedium)
                    if (!callLogPermissionGranted) {
                        Text(
                            "Allow access only if you want NetGuard to display recent system call history. No broad storage permission is required.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        callLogError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        LoadingButton(onClick = onRequestCallLogPermission) { Text("Allow call history") }
                    } else {
                        Text("Permission granted. Recent calls are shown below.", style = MaterialTheme.typography.bodySmall)
                        LoadingTextButton(onClick = onRequestCallLogPermission) { Text("Refresh call history") }
                    }
                }
            }
        }

        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Block a number", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Numbers are normalized and stored locally on this device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = numberToBlock,
                        onValueChange = { numberToBlock = it; inputError = null },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Phone number") },
                        placeholder = { Text("+91 98765 43210") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        isError = inputError != null
                    )
                    inputError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                    LoadingButton(
                        onClick = ::addManualBlock,
                        enabled = numberToBlock.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Block number") }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Blocked numbers", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (blockedNumbers.isEmpty()) "No numbers are blocked."
                        else blockedNumbers.size.toString() + " number" + if (blockedNumbers.size == 1) "" else "s" + " currently blocked.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (blockedNumbers.isNotEmpty()) {
            items(blockedNumbers.toList().sorted()) { number ->
                Card(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(number, Modifier.weight(1f))
                        LoadingTextButton(onClick = { onUnblock(number) }) { Text("Unblock") }
                    }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Recent calls", style = MaterialTheme.typography.titleMedium)
                    Text(
                        filteredLogs.size.toString() + " shown • " + logs.size + " total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (logs.isNotEmpty()) {
                    LoadingTextButton(onClick = onClearLogs) { Text("Clear") }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CallFilterChip("All", filter == CallFilter.ALL) { filter = CallFilter.ALL }
                CallFilterChip("Blocked", filter == CallFilter.BLOCKED) { filter = CallFilter.BLOCKED }
                CallFilterChip("Allowed", filter == CallFilter.ALLOWED) { filter = CallFilter.ALLOWED }
                CallFilterChip("Missed", filter == CallFilter.MISSED) { filter = CallFilter.MISSED }
            }
        }

        if (filteredLogs.isEmpty()) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.fillMaxWidth().padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("No calls to show", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Recent system call history and screening events will appear here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredLogs, key = { it.id }) { log ->
                CallLogCard(log, onBlock)
            }
        }
    }
}

@Composable
private fun ProtectionStat(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CallFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}

@Composable
private fun CallLogCard(log: CallProtectionLog, onBlock: (String) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(log.contactName ?: log.number, style = MaterialTheme.typography.titleSmall)
                    if (log.contactName != null) Text(log.number, style = MaterialTheme.typography.bodySmall)
                }
                AssistChip(
                    onClick = {},
                    label = { Text(if (log.blocked) "Blocked" else log.direction) }
                )
            }
            Text(DateFormat.getDateTimeInstance().format(Date(log.timestamp)), style = MaterialTheme.typography.bodySmall)
            if (!log.blocked && log.number.isNotBlank() && log.number != "Unknown number") {
                LoadingTextButton(onClick = { onBlock(log.number) }) { Text("Block number") }
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
