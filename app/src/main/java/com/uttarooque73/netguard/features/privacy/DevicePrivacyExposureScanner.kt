package com.uttarooque73.netguard.features.privacy

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import android.provider.Settings
import com.uttarooque73.netguard.features.apps.AppSecurityCheck

enum class PrivacyExposureLevel { GOOD, REVIEW, HIGH }

data class PrivacyExposureFinding(
    val id: String,
    val title: String,
    val level: PrivacyExposureLevel,
    val evidence: String,
    val action: String,
    val packageName: String? = null
)

data class PrivacyExposureReport(
    val score: Int,
    val findings: List<PrivacyExposureFinding>,
    val scannedApps: Int,
    val checkedAtEpochMs: Long
)

object DevicePrivacyExposureScanner {
    fun scan(context: Context, apps: List<AppSecurityCheck>): PrivacyExposureReport {
        val findings = mutableListOf<PrivacyExposureFinding>()

        apps.filter { it.requestedDangerousPermissions.isNotEmpty() }.take(30).forEach { app ->
            findings += PrivacyExposureFinding(
                "permissions:" + app.packageName,
                app.appName + " requests sensitive permissions",
                PrivacyExposureLevel.REVIEW,
                app.requestedDangerousPermissions.joinToString(),
                "Review whether each permission is necessary and revoke unused permissions in Android Settings.",
                app.packageName
            )
        }
        apps.filter { it.cleartextAllowed == true }.take(20).forEach { app ->
            findings += PrivacyExposureFinding(
                "cleartext:" + app.packageName,
                app.appName + " permits cleartext traffic",
                PrivacyExposureLevel.HIGH,
                "The application manifest allows cleartext network traffic.",
                "Prefer HTTPS-only communication or review the app's security configuration.",
                app.packageName
            )
        }
        apps.filter { it.debuggable }.take(20).forEach { app ->
            findings += PrivacyExposureFinding(
                "debuggable:" + app.packageName,
                app.appName + " is debuggable",
                PrivacyExposureLevel.HIGH,
                "The installed application is marked debuggable.",
                "Use a production build or update the application if this is not intentional.",
                app.packageName
            )
        }
        apps.filter { it.exportedComponents > 0 }.take(20).forEach { app ->
            findings += PrivacyExposureFinding(
                "exported:" + app.packageName,
                app.appName + " exposes Android components",
                PrivacyExposureLevel.REVIEW,
                app.exportedComponents.toString() + " exported component(s) detected.",
                "Review the app's exported activities, services, receivers and providers.",
                app.packageName
            )
        }
        apps.filter { it.backupAllowed == true }.take(20).forEach { app ->
            findings += PrivacyExposureFinding(
                "backup:" + app.packageName,
                app.appName + " allows backup",
                PrivacyExposureLevel.REVIEW,
                "Android backup is enabled by the application's manifest flags.",
                "Review whether sensitive application data should be included in backup.",
                app.packageName
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            val adbEnabled = runCatching {
                Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
            }.getOrDefault(false)
            if (adbEnabled) findings += PrivacyExposureFinding(
                "device:usb-debugging",
                "USB debugging is enabled",
                PrivacyExposureLevel.HIGH,
                "Android reports ADB debugging enabled.",
                "Disable USB debugging when you do not actively need developer access."
            )
        }

        val score = (100 - findings.sumOf {
            when (it.level) {
                PrivacyExposureLevel.HIGH -> 12
                PrivacyExposureLevel.REVIEW -> 4
                PrivacyExposureLevel.GOOD -> 0
            }
        }).coerceIn(0, 100)

        return PrivacyExposureReport(score, findings.sortedBy { it.level.ordinal }, apps.size, System.currentTimeMillis())
    }
}
