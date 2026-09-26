package com.uttarooque73.netguard.mobile

import android.content.Context
import android.content.pm.ApplicationInfo
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import android.app.KeyguardManager

enum class MobileCheckStatus { PASS, REVIEW, FAIL, NOT_AVAILABLE }

data class MobileSecurityCheck(
    val id: String,
    val title: String,
    val status: MobileCheckStatus,
    val evidence: String,
    val whyItMatters: String,
    val remediation: List<String>,
    val verification: String
)

data class MobileSecuritySnapshot(
    val checks: List<MobileSecurityCheck>,
    val generatedAtEpochMs: Long = System.currentTimeMillis()
)

object MobileSecurityAudit {
    fun inspect(context: Context): MobileSecuritySnapshot {
        val connectivity = context.getSystemService(ConnectivityManager::class.java)
        val keyguard = context.getSystemService(KeyguardManager::class.java)
        val network = connectivity.activeNetwork
        val capabilities = network?.let { connectivity.getNetworkCapabilities(it) }

        val cellular = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
        val vpn = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
        val validated = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        val notMetered = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        val roaming = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING)?.not()
        val airplaneMode = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON,
            0
        ) == 1
        val developerOptions = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            0
        ) == 1
        val adbEnabled = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.ADB_ENABLED,
            0
        ) == 1
        val privateDnsMode = Settings.Global.getString(
            context.contentResolver,
            "private_dns_mode"
        )?.lowercase()
        val privateDnsSecure = privateDnsMode == "hostname" || privateDnsMode == "opportunistic"
        val screenLockSecure = keyguard?.isKeyguardSecure

        val checks = mutableListOf<MobileSecurityCheck>()

        checks += MobileSecurityCheck(
            id = "MOB-NET-001",
            title = "Cellular network transport",
            status = if (cellular) MobileCheckStatus.PASS else MobileCheckStatus.NOT_AVAILABLE,
            evidence = if (cellular) "The active default network uses cellular transport." else "Cellular is not the active default transport.",
            whyItMatters = "This identifies when the phone is currently using mobile data rather than Wi-Fi.",
            remediation = listOf("No action is required when cellular use is intentional.", "For sensitive work, use an organization-approved VPN when required by policy."),
            verification = "Refresh the mobile security audit and confirm the active transport."
        )

        checks += MobileSecurityCheck(
            id = "MOB-NET-002",
            title = "Cellular network validation",
            status = when {
                !cellular -> MobileCheckStatus.NOT_AVAILABLE
                validated == true -> MobileCheckStatus.PASS
                validated == false -> MobileCheckStatus.REVIEW
                else -> MobileCheckStatus.NOT_AVAILABLE
            },
            evidence = when {
                !cellular -> "No active cellular transport."
                validated == true -> "Android reports the active cellular network as validated."
                validated == false -> "Android does not report the active cellular network as validated."
                else -> "Android did not provide validation state."
            },
            whyItMatters = "An unvalidated network may indicate limited connectivity or an interception/captive-network condition; this signal alone does not prove an attack.",
            remediation = listOf("Move to a location with reliable cellular service.", "If connectivity is redirected or unexpected, disconnect and investigate the network before sending sensitive data."),
            verification = "Refresh the audit and confirm Android reports the active network as validated."
        )

        checks += MobileSecurityCheck(
            id = "MOB-NET-003",
            title = "Cellular roaming state",
            status = if (!cellular || roaming == null) MobileCheckStatus.NOT_AVAILABLE else if (roaming) MobileCheckStatus.REVIEW else MobileCheckStatus.PASS,
            evidence = when {
                !cellular -> "No active cellular transport."
                roaming == true -> "Android reports the active cellular network as roaming."
                roaming == false -> "Android reports the active cellular network as not roaming."
                else -> "Roaming state is unavailable."
            },
            whyItMatters = "Roaming can change carrier policy, charging, and network trust assumptions.",
            remediation = listOf("Confirm roaming is expected for your current location and carrier plan.", "Use your organization's approved VPN/security policy while roaming when required."),
            verification = "Refresh the audit after returning to the expected carrier network."
        )

        checks += MobileSecurityCheck(
            id = "MOB-NET-004",
            title = "VPN protection",
            status = if (cellular && !vpn) MobileCheckStatus.REVIEW else if (vpn) MobileCheckStatus.PASS else MobileCheckStatus.NOT_AVAILABLE,
            evidence = when {
                vpn -> "An active VPN transport is present."
                cellular -> "Cellular is active and no VPN transport is visible to NetGuard."
                else -> "No active cellular transport; VPN requirement is not evaluated."
            },
            whyItMatters = "A VPN can provide an additional encrypted tunnel when required by an organization's security policy, especially on untrusted networks.",
            remediation = listOf("Connect to your trusted VPN when policy or the sensitivity of the activity requires it.", "Do not install an unknown VPN provider solely to clear this check."),
            verification = "Refresh the audit and confirm the VPN transport is active."
        )

        checks += MobileSecurityCheck(
            id = "MOB-DNS-001",
            title = "Private DNS",
            status = when (privateDnsMode) {
                "hostname", "opportunistic" -> MobileCheckStatus.PASS
                "off" -> MobileCheckStatus.REVIEW
                else -> MobileCheckStatus.NOT_AVAILABLE
            },
            evidence = "Private DNS mode: " + (privateDnsMode ?: "unknown"),
            whyItMatters = "Private DNS can protect DNS queries from passive observation and tampering when the configured resolver supports it.",
            remediation = listOf("Open Android Settings → Network & internet → Private DNS.", "Prefer a trusted Private DNS provider or Automatic mode according to your security policy."),
            verification = "Return to NetGuard and refresh the mobile security audit."
        )

        checks += MobileSecurityCheck(
            id = "MOB-DEV-001",
            title = "Developer Options",
            status = if (developerOptions) MobileCheckStatus.REVIEW else MobileCheckStatus.PASS,
            evidence = if (developerOptions) "Developer Options are enabled." else "Developer Options are disabled.",
            whyItMatters = "Developer features increase the number of debugging controls available on the device.",
            remediation = listOf("Open Android Settings → System → Developer options.", "Disable Developer options when you do not need them."),
            verification = "Refresh the audit and confirm Developer Options are disabled when not required."
        )

        checks += MobileSecurityCheck(
            id = "MOB-DEV-002",
            title = "USB debugging",
            status = if (adbEnabled) MobileCheckStatus.FAIL else MobileCheckStatus.PASS,
            evidence = if (adbEnabled) "Android reports USB debugging/ADB as enabled." else "Android reports USB debugging/ADB as disabled.",
            whyItMatters = "ADB provides a powerful device-management/debugging interface and should not remain enabled unnecessarily.",
            remediation = listOf("Open Android Settings → System → Developer options.", "Turn off USB debugging when it is not required.", "Revoke USB debugging authorizations if you no longer trust previously connected computers."),
            verification = "Refresh the audit and confirm USB debugging is disabled."
        )

        checks += MobileSecurityCheck(
            id = "MOB-DEV-003",
            title = "Secure screen lock",
            status = when (screenLockSecure) {
                true -> MobileCheckStatus.PASS
                false -> MobileCheckStatus.FAIL
                else -> MobileCheckStatus.NOT_AVAILABLE
            },
            evidence = when (screenLockSecure) {
                true -> "Android reports a secure keyguard is configured."
                false -> "Android reports no secure keyguard."
                else -> "Secure keyguard state is unavailable."
            },
            whyItMatters = "A secure screen lock protects locally stored data if the phone is lost or left unattended.",
            remediation = listOf("Open Android Settings → Security & privacy → Device unlock.", "Set a strong PIN or password; configure biometrics as an additional convenience where appropriate."),
            verification = "Lock and unlock the device, then refresh the audit."
        )

        checks += MobileSecurityCheck(
            id = "MOB-DEV-004",
            title = "Airplane mode",
            status = if (airplaneMode) MobileCheckStatus.REVIEW else MobileCheckStatus.PASS,
            evidence = if (airplaneMode) "Airplane mode is enabled." else "Airplane mode is disabled.",
            whyItMatters = "Airplane mode changes radio connectivity and explains why network checks may be unavailable.",
            remediation = listOf("If network access is required, disable Airplane mode from Quick Settings or Android Settings."),
            verification = "Refresh the audit after changing the radio state."
        )


        val androidVersion = Build.VERSION.RELEASE ?: "unknown"
        val securityPatch = Build.VERSION.SECURITY_PATCH.ifBlank { null }
        val appDataEncrypted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) true else null
        val debuggableBuild = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

        checks += MobileSecurityCheck(
            id = "MOB-DEV-005",
            title = "Android platform version",
            status = if (androidVersion != "unknown") MobileCheckStatus.PASS else MobileCheckStatus.NOT_AVAILABLE,
            evidence = if (androidVersion != "unknown") {
                "Android " + androidVersion + " (SDK " + Build.VERSION.SDK_INT + ")."
            } else {
                "Android platform version is unavailable."
            },
            whyItMatters = "Android platform versions differ in available security controls and supported security APIs.",
            remediation = listOf(
                "Use a supported Android release when the device manufacturer provides one.",
                "Keep the device's operating system updated through its normal system-update workflow."
            ),
            verification = "Refresh the audit and confirm the current Android version and SDK are reported."
        )

        checks += MobileSecurityCheck(
            id = "MOB-DEV-006",
            title = "Android security patch level",
            status = if (securityPatch != null) MobileCheckStatus.PASS else MobileCheckStatus.REVIEW,
            evidence = "Security patch level: " + (securityPatch ?: "unavailable"),
            whyItMatters = "The Android security patch level identifies the security-update level reported by the device.",
            remediation = listOf(
                "Open Android Settings → System → Software update or the equivalent device update screen.",
                "Install available security updates from the device manufacturer."
            ),
            verification = "Refresh the audit and confirm the device reports its security patch level."
        )

        checks += MobileSecurityCheck(
            id = "MOB-DEV-007",
            title = "App data encryption",
            status = when (appDataEncrypted) {
                true -> MobileCheckStatus.PASS
                false -> MobileCheckStatus.FAIL
                null -> MobileCheckStatus.NOT_AVAILABLE
            },
            evidence = when (appDataEncrypted) {
                true -> "NetGuard's application data directory is reported as encrypted at rest."
                false -> "NetGuard's application data directory is not reported as encrypted at rest."
                null -> "Android did not provide an encryption-at-rest result for NetGuard's application data directory."
            },
            whyItMatters = "Encryption at rest reduces the risk of stored application data being exposed if storage is accessed outside normal Android protections.",
            remediation = listOf(
                "Keep Android and the device security software updated.",
                "If this check fails, investigate the device's storage-encryption state and avoid storing sensitive information until the device is remediated."
            ),
            verification = "Refresh the audit and confirm NetGuard's application data directory is reported as encrypted."
        )

        checks += MobileSecurityCheck(
            id = "MOB-DEV-008",
            title = "Debuggable Android build",
            status = if (debuggableBuild) MobileCheckStatus.REVIEW else MobileCheckStatus.PASS,
            evidence = if (debuggableBuild) {
                "Android reports a debuggable build."
            } else {
                "Android reports a non-debuggable build."
            },
            whyItMatters = "Debuggable Android builds expose debugging capabilities intended for development and testing.",
            remediation = listOf(
                "Use a production/non-debuggable device build for normal security-sensitive use.",
                "If a debuggable build is intentional for development, keep the device controlled and do not treat the signal as proof of compromise."
            ),
            verification = "Refresh the audit and confirm the build is non-debuggable when production posture is required."
        )

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            checks += MobileSecurityCheck(
                id = "MOB-LIMIT-001",
                title = "Android API capability",
                status = MobileCheckStatus.REVIEW,
                evidence = "Some modern Wi-Fi/mobile security signals are unavailable on Android versions below 12.",
                whyItMatters = "Older Android releases expose fewer standardized security signals to applications.",
                remediation = listOf("Keep Android and the device security patch level up to date when updates are available."),
                verification = "Install available Android security updates and refresh the audit."
            )
        }

        return MobileSecuritySnapshot(checks)
    }

    fun checkIds(): Set<String> = setOf(
        "MOB-NET-001", "MOB-NET-002", "MOB-NET-003", "MOB-NET-004",
        "MOB-DNS-001", "MOB-DEV-001", "MOB-DEV-002", "MOB-DEV-003",
        "MOB-DEV-004", "MOB-DEV-005", "MOB-DEV-006", "MOB-DEV-007",
        "MOB-DEV-008", "MOB-LIMIT-001"
    )
}
