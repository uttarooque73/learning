package com.uttarooque73.netguard.features.apps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Build

data class AppSecurityCheck(
    val packageName: String,
    val appName: String,
    val debuggable: Boolean,
    val backupAllowed: Boolean?,
    val cleartextAllowed: Boolean?,
    val exportedComponents: Int,
    val requestedDangerousPermissions: List<String>,
    val installerPackage: String? = null,
    val versionName: String? = null,
    val versionCode: Long? = null,
    val targetSdk: Int? = null,
    val evidence: List<String>,
    val remediation: List<String>
)

object InstalledAppSecurityAudit {
    private val dangerousPermissions = setOf(
        "android.permission.READ_CONTACTS",
        "android.permission.WRITE_CONTACTS",
        "android.permission.CAMERA",
        "android.permission.RECORD_AUDIO",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.READ_SMS",
        "android.permission.SEND_SMS",
        "android.permission.READ_CALL_LOG",
        "android.permission.WRITE_CALL_LOG",
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_EXTERNAL_STORAGE"
    )

    fun inspect(context: Context): List<AppSecurityCheck> {
        val pm = context.packageManager
        val packages = if (Build.VERSION.SDK_INT >= 33) {
            pm.getInstalledPackages(
                android.content.pm.PackageManager.PackageInfoFlags.of(
                    android.content.pm.PackageManager.GET_PERMISSIONS.toLong() or
                        android.content.pm.PackageManager.GET_ACTIVITIES.toLong() or
                        android.content.pm.PackageManager.GET_SERVICES.toLong() or
                        android.content.pm.PackageManager.GET_RECEIVERS.toLong() or
                        android.content.pm.PackageManager.GET_PROVIDERS.toLong()
                )
            )
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledPackages(
                android.content.pm.PackageManager.GET_PERMISSIONS or
                    android.content.pm.PackageManager.GET_ACTIVITIES or
                    android.content.pm.PackageManager.GET_SERVICES or
                    android.content.pm.PackageManager.GET_RECEIVERS or
                    android.content.pm.PackageManager.GET_PROVIDERS
            )
        }

        return packages.mapNotNull { pkg ->
            val appInfo = pkg.applicationInfo ?: return@mapNotNull null
            val appName = pm.getApplicationLabel(appInfo).toString()
            val debuggable = appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
            val backupAllowed = if (Build.VERSION.SDK_INT >= 31) {
                appInfo.flags and ApplicationInfo.FLAG_ALLOW_BACKUP != 0
            } else null
            val cleartextAllowed = if (Build.VERSION.SDK_INT >= 23) {
                appInfo.flags and ApplicationInfo.FLAG_USES_CLEARTEXT_TRAFFIC != 0
            } else null
            val exported = exportedCount(pkg)
            val installerPackage = runCatching {
                if (Build.VERSION.SDK_INT >= 30) pm.getInstallSourceInfo(pkg.packageName).installingPackageName
                else @Suppress("DEPRECATION") pm.getInstallerPackageName(pkg.packageName)
            }.getOrNull()
            val versionName = pkg.versionName
            val versionCode = if (Build.VERSION.SDK_INT >= 28) pkg.longVersionCode else @Suppress("DEPRECATION") pkg.versionCode.toLong()
            val targetSdk = appInfo.targetSdkVersion
            val requested = classifyDangerousPermissions(pkg.requestedPermissions.orEmpty())

            val evidence = buildList {
                if (debuggable) add("Application is marked debuggable.")
                if (backupAllowed == true) add("Application backup is allowed by its manifest flags.")
                if (cleartextAllowed == true) add("Application explicitly allows cleartext traffic.")
                if (exported > 0) add("$exported exported application components were detected.")
                if (requested.isNotEmpty()) add("Requested sensitive permissions: " + requested.joinToString())
                if (installerPackage == null) add("Installer source could not be established; review as an unknown installation source.")
                if (installerPackage != null) add("Installer package: $installerPackage")
                if (targetSdk != null) add("Target SDK: $targetSdk")
            }

            AppSecurityCheck(
                packageName = pkg.packageName,
                appName = appName,
                debuggable = debuggable,
                backupAllowed = backupAllowed,
                cleartextAllowed = cleartextAllowed,
                exportedComponents = exported,
                requestedDangerousPermissions = requested,
                installerPackage = installerPackage,
                versionName = versionName,
                versionCode = versionCode,
                targetSdk = targetSdk,
                evidence = evidence,
                remediation = listOf(
                    "Review whether the application still needs each sensitive permission.",
                    "Prefer production/non-debuggable builds for production applications.",
                    "Prefer encrypted transport and avoid cleartext traffic unless explicitly required.",
                    "Review exported components and restrict them to the minimum required exposure."
                )
            )
        }.sortedBy { it.appName.lowercase() }
    }

    internal fun classifyDangerousPermissions(permissions: List<String>): List<String> =
        permissions.filter { it in dangerousPermissions }.distinct()

    internal fun dangerousPermissionCatalog(): Set<String> = dangerousPermissions

    private fun exportedCount(pkg: PackageInfo): Int {
        var count = 0
        pkg.activities.orEmpty().forEach { if (it.exported) count++ }
        pkg.services.orEmpty().forEach { if (it.exported) count++ }
        pkg.receivers.orEmpty().forEach { if (it.exported) count++ }
        pkg.providers.orEmpty().forEach { if (it.exported) count++ }
        return count
    }
}