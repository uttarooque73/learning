package com.uttarooque73.netguard.features.autopilot

import com.uttarooque73.netguard.features.apps.AppSecurityCheck
import com.uttarooque73.netguard.features.baseline.SecurityDrift
import com.uttarooque73.netguard.mobile.MobileCheckStatus
import com.uttarooque73.netguard.mobile.MobileSecuritySnapshot
import com.uttarooque73.netguard.network.NetworkInfo
import com.uttarooque73.netguard.audit.DiscoveredService

enum class SecurityActionPriority { HIGH, MEDIUM, LOW }
data class SecurityAction(val id:String,val title:String,val priority:SecurityActionPriority,val reason:String,val evidence:String,val action:String,val verification:String)
data class AutopilotAssessment(val score:Int,val actions:List<SecurityAction>,val checkedSignals:Int,val highPriority:Int)
data class ApkSecurityReview(val packageName:String,val appName:String,val versionName:String?,val targetSdk:Int,val debuggable:Boolean,val backupAllowed:Boolean?,val cleartextAllowed:Boolean?,val exportedComponents:Int,val requestedSensitivePermissions:List<String>,val risk:Int,val reasons:List<String>)

object SecurityAutopilotEngine {
    fun assess(network:NetworkInfo?,services:List<DiscoveredService>,apps:List<AppSecurityCheck>,mobile:MobileSecuritySnapshot?,drifts:List<SecurityDrift>):AutopilotAssessment {
        val actions=mutableListOf<SecurityAction>()
        fun add(id:String,title:String,priority:SecurityActionPriority,reason:String,evidence:String,action:String,verification:String){actions+=SecurityAction(id,title,priority,reason,evidence,action,verification)}
        mobile?.checks.orEmpty().filter{it.status==MobileCheckStatus.FAIL}.forEach{check->
            add("mobile-"+check.id,check.title,if(check.id.contains("USB")||check.id.contains("DEV"))SecurityActionPriority.HIGH else SecurityActionPriority.MEDIUM,check.explanation,check.evidence,check.remediation,"Re-run Full Security Check and confirm the check is PASS.")
        }
        apps.filter{it.debuggable||it.cleartextAllowed==true||it.backupAllowed==true||it.exportedComponents>0||it.requestedDangerousPermissions.isNotEmpty()}.take(5).forEach{app->
            add("app-"+app.packageName,"Review ${app.appName}",if(app.debuggable||app.cleartextAllowed==true)SecurityActionPriority.HIGH else SecurityActionPriority.MEDIUM,"The installed app exposes one or more security/privacy posture signals.",app.evidence.joinToString(" "),"Review permissions, exported components, backup and cleartext settings. Remove or restrict the app if unnecessary.","Run the app security audit again and confirm the signal is gone or intentionally accepted.")
        }
        if(network==null)add("network-unavailable","Network security evidence unavailable",SecurityActionPriority.MEDIUM,"NetGuard cannot validate the current network path without network evidence.","No active network evidence is available.","Run a Full Security Check while connected to the network you want to assess.","A subsequent audit should contain network, gateway and DNS evidence.")
        if(services.any{it.reachable&&(it.port==23||it.port==445)})add("network-exposure","Sensitive network service exposed",SecurityActionPriority.HIGH,"A commonly sensitive service is reachable on the audited network.","Reachable TCP/23 or TCP/445 service detected.","Disable the service if it is not required and restrict access at the host/router.","Re-run service discovery and confirm the service is no longer reachable.")
        drifts.filter{it.severity=="HIGH"}.take(5).forEach{drift->add("drift-"+drift.id,drift.title,SecurityActionPriority.HIGH,"A high-severity change was detected relative to the trusted baseline.","${drift.before} → ${drift.after}",drift.recommendedAction,"Capture fresh evidence and re-run the security check.")}
        val score=(100-actions.sumOf{when(it.priority){SecurityActionPriority.HIGH->18;SecurityActionPriority.MEDIUM->8;SecurityActionPriority.LOW->3}}).coerceIn(0,100)
        return AutopilotAssessment(score,actions.sortedWith(compareBy<SecurityAction>{it.priority.ordinal}.thenBy{it.title}),actions.size,actions.count{it.priority==SecurityActionPriority.HIGH})
    }

    fun reviewApk(packageInfo:android.content.pm.PackageInfo,label:String):ApkSecurityReview{
        val app=packageInfo.applicationInfo
        val flags=app?.flags?:0
        val debuggable=flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE!=0
        val backupAllowed=if(android.os.Build.VERSION.SDK_INT>=31)flags and android.content.pm.ApplicationInfo.FLAG_ALLOW_BACKUP!=0 else null
        val cleartextAllowed=if(android.os.Build.VERSION.SDK_INT>=23)flags and android.content.pm.ApplicationInfo.FLAG_USES_CLEARTEXT_TRAFFIC!=0 else null
        val exported=listOf(packageInfo.activities,packageInfo.services,packageInfo.receivers,packageInfo.providers).sumOf{it.orEmpty().count{c->c.exported}}
        val catalog=setOf("android.permission.CAMERA","android.permission.RECORD_AUDIO","android.permission.ACCESS_FINE_LOCATION","android.permission.ACCESS_COARSE_LOCATION","android.permission.READ_CONTACTS","android.permission.WRITE_CONTACTS","android.permission.READ_SMS","android.permission.SEND_SMS","android.permission.READ_CALL_LOG","android.permission.WRITE_CALL_LOG")
        val sensitive=packageInfo.requestedPermissions.orEmpty().filter{it in catalog}.distinct()
        val reasons=buildList{if(debuggable)add("Debuggable application build");if(cleartextAllowed==true)add("Cleartext traffic is allowed");if(backupAllowed==true)add("Application backup is allowed");if(exported>0)add("$exported exported components");if(sensitive.isNotEmpty())add("Sensitive permissions: ${sensitive.joinToString()}")}
        val risk=reasons.sumOf{when{it.startsWith("Debuggable")||it.startsWith("Cleartext")->25;it.startsWith("Sensitive")->8;else->10}}.coerceIn(0,100)
        return ApkSecurityReview(packageInfo.packageName,label,packageInfo.versionName,app?.targetSdkVersion?:0,debuggable,backupAllowed,cleartextAllowed,exported,sensitive,risk,reasons)
    }
}
