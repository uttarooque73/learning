package com.uttarooque73.netguard.features.baseline

import com.uttarooque73.netguard.features.apps.AppSecurityCheck
import com.uttarooque73.netguard.network.DiscoveredDevice
import com.uttarooque73.netguard.audit.DiscoveredService
import com.uttarooque73.netguard.network.NetworkInfo

data class SecurityDrift(
    val id: String,
    val category: String,
    val title: String,
    val before: String,
    val after: String,
    val severity: String,
    val recommendedAction: String
)

data class TrustedSecurityBaselineSnapshot(
    val capturedAtEpochMs: Long,
    val ssid: String?,
    val bssid: String?,
    val gateway: String?,
    val dnsKeys: List<String> = emptyList(),
    val wifiSecurity: String? = null,
    val deviceKeys: List<String>,
    val serviceKeys: List<String>,
    val appKeys: List<String>,
    val appSecurityKeys: List<String> = emptyList()
)

object SecurityDriftEngine {
    fun snapshot(network: NetworkInfo?, devices: List<DiscoveredDevice>, services: List<DiscoveredService>, apps: List<AppSecurityCheck>, now: Long): TrustedSecurityBaselineSnapshot =
        TrustedSecurityBaselineSnapshot(
            now, network?.ssid, network?.bssid, network?.gatewayAddress, network?.dnsServers.orEmpty().sorted(), network?.wifiSecurity,
            devices.map { it.ipAddress }.sorted(),
            services.map { it.ipAddress + "|" + it.protocol + "|" + it.port }.sorted(),
            apps.map { it.packageName + "|" + (it.versionCode ?: 0L) }.sorted(),
            apps.map { it.packageName + "|" + it.requestedDangerousPermissions.sorted().joinToString(",") + "|" + it.debuggable + "|" + it.cleartextAllowed + "|" + it.backupAllowed + "|" + it.exportedComponents }.sorted()
        )

    fun compare(before: TrustedSecurityBaselineSnapshot, after: TrustedSecurityBaselineSnapshot): List<SecurityDrift> {
        val out = mutableListOf<SecurityDrift>()
        if (before.ssid != after.ssid || before.bssid != after.bssid) out += SecurityDrift("network-identity", "NETWORK", "Wi-Fi identity changed", before.ssid + " / " + before.bssid, after.ssid + " / " + after.bssid, "REVIEW", "Confirm the current Wi-Fi network and BSSID.")
        if (before.dnsKeys != after.dnsKeys) out += SecurityDrift("dns-change", "NETWORK", "DNS configuration changed", before.dnsKeys.joinToString().ifBlank { "Unavailable" }, after.dnsKeys.joinToString().ifBlank { "Unavailable" }, "REVIEW", "Verify the DNS servers and Private DNS configuration.")
        if (before.wifiSecurity != after.wifiSecurity) out += SecurityDrift("wifi-security-change", "NETWORK", "Wi-Fi security mode changed", before.wifiSecurity ?: "Unavailable", after.wifiSecurity ?: "Unavailable", "HIGH", "Verify the access point security configuration.")
        if (before.gateway != after.gateway) out += SecurityDrift("gateway", "NETWORK", "Default gateway changed", before.gateway ?: "Unavailable", after.gateway ?: "Unavailable", "HIGH", "Verify the router or gateway identity.")
        (after.deviceKeys - before.deviceKeys).forEach { out += SecurityDrift("device-added-" + it, "DEVICE", "New network device observed", "Not present in trusted baseline", it, "REVIEW", "Identify the device and verify that it is authorized.") }
        (before.deviceKeys - after.deviceKeys).forEach { out += SecurityDrift("device-removed-" + it, "DEVICE", "Previously observed device disappeared", it, "Not currently observed", "INFO", "Re-run discovery if the device should still be online.") }
        (after.serviceKeys - before.serviceKeys).forEach { out += SecurityDrift("service-added-" + it, "SERVICE", "New network service observed", "Not present in trusted baseline", it, "HIGH", "Identify the service and confirm it is intentionally exposed.") }
        (before.serviceKeys - after.serviceKeys).forEach { out += SecurityDrift("service-removed-" + it, "SERVICE", "Previously observed service disappeared", it, "Not currently observed", "INFO", "Re-run the service audit if the service should remain available.") }
        val beforeApps = before.appKeys.map { it.substringBefore("|") }.toSet()
        val afterApps = after.appKeys.map { it.substringBefore("|") }.toSet()
        (afterApps - beforeApps).forEach { out += SecurityDrift("app-added-" + it, "APP", "New application observed", "Not present in trusted baseline", it, "REVIEW", "Review the application's source, permissions and security posture.") }
        (beforeApps - afterApps).forEach { out += SecurityDrift("app-removed-" + it, "APP", "Application no longer installed", it, "Not currently installed", "INFO", "Confirm the removal was intentional.") }
        val beforeSecurity = before.appSecurityKeys.associate { it.substringBefore("|") to it.substringAfter("|") }
        after.appSecurityKeys.forEach { key ->
            val pkg = key.substringBefore("|")
            val value = key.substringAfter("|")
            if (beforeSecurity[pkg] != null && beforeSecurity[pkg] != value) out += SecurityDrift("permission-drift-" + pkg, "APP", "Application security permissions/configuration changed", beforeSecurity[pkg] ?: "Unknown", value, "HIGH", "Review the changed permissions and application security configuration.")
        }
        val beforeVersions = before.appKeys.associate { it.substringBefore("|") to it.substringAfter("|") }
        after.appKeys.forEach { key ->
            val pkg = key.substringBefore("|")
            val version = key.substringAfter("|")
            if (beforeVersions[pkg] != null && beforeVersions[pkg] != version) out += SecurityDrift("app-updated-" + pkg, "APP", "Application version changed", beforeVersions[pkg] ?: "Unknown", version, "REVIEW", "Review the application's updated permissions and security posture.")
        }
        return out
    }
}
