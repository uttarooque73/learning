package com.uttarooque73.netguard.features.autopilot

data class ApkSecurityReviewInput(
    val packageName: String,
    val appName: String,
    val versionName: String?,
    val targetSdk: Int,
    val debuggable: Boolean,
    val backupAllowed: Boolean?,
    val cleartextAllowed: Boolean?,
    val exportedComponents: Int,
    val requestedSensitivePermissions: List<String>
)

object ApkSecurityReviewEngine {
    private val sensitivePermissionCatalog = setOf(
        "android.permission.CAMERA",
        "android.permission.RECORD_AUDIO",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.READ_CONTACTS",
        "android.permission.WRITE_CONTACTS",
        "android.permission.READ_SMS",
        "android.permission.SEND_SMS",
        "android.permission.READ_CALL_LOG",
        "android.permission.WRITE_CALL_LOG"
    )

    fun review(input: ApkSecurityReviewInput): ApkSecurityReview {
        val sensitive = input.requestedSensitivePermissions
            .filter { it in sensitivePermissionCatalog }
            .distinct()

        val reasons = buildList {
            if (input.debuggable) add("Debuggable application build")
            if (input.cleartextAllowed == true) add("Cleartext traffic is allowed")
            if (input.backupAllowed == true) add("Application backup is allowed")
            if (input.exportedComponents > 0) add("${input.exportedComponents} exported components")
            if (sensitive.isNotEmpty()) add("Sensitive permissions: ${sensitive.joinToString()}")
        }

        val risk = reasons.fold(0) { total, reason ->
            total + when {
                reason.startsWith("Debuggable") || reason.startsWith("Cleartext") -> 25
                reason.startsWith("Sensitive") -> 8
                else -> 10
            }
        }.coerceIn(0, 100)

        return ApkSecurityReview(
            input.packageName,
            input.appName,
            input.versionName,
            input.targetSdk,
            input.debuggable,
            input.backupAllowed,
            input.cleartextAllowed,
            input.exportedComponents,
            sensitive,
            risk,
            reasons
        )
    }
}
