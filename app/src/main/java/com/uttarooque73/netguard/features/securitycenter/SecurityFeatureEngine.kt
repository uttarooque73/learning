package com.uttarooque73.netguard.features.securitycenter

import com.uttarooque73.netguard.audit.DiscoveredService
import com.uttarooque73.netguard.features.apps.AppSecurityCheck
import com.uttarooque73.netguard.mobile.MobileSecuritySnapshot
import com.uttarooque73.netguard.network.DiscoveredDevice
import com.uttarooque73.netguard.network.NetworkInfo
import com.uttarooque73.netguard.features.baseline.SecurityDrift

data class PermissionDrift(val packageName:String,val appName:String,val before:String,val after:String)
data class NetworkTrustAssessment(val score:Int,val reasons:List<String>)
data class RogueDevice(val ip:String,val status:String,val evidence:String)
data class CorrelatedIncident(val title:String,val severity:String,val events:List<String>,val explanation:String)
data class AppRiskProfile(val packageName:String,val appName:String,val risk:Int,val reasons:List<String>)
data class SecurityTrend(val timestamp:Long,val score:Int,val findings:Int)
data class SecurityPlaybook(val id:String,val title:String,val steps:List<String>)
data class SecurityWatchState(val enabled:Boolean,val intervalMinutes:Int,val lastRunEpochMs:Long)

object SecurityFeatureEngine {
    fun permissionDrift(before:List<AppSecurityCheck>, after:List<AppSecurityCheck>):List<PermissionDrift> {
        val b=before.associateBy{it.packageName}; return after.mapNotNull { a -> val x=b[a.packageName] ?: return@mapNotNull null
            val old=x.requestedDangerousPermissions.sorted().joinToString(","); val now=a.requestedDangerousPermissions.sorted().joinToString(",")
            if(old!=now) PermissionDrift(a.packageName,a.appName,old.ifBlank{"None"},now.ifBlank{"None"}) else null }
    }
    fun networkTrust(network:NetworkInfo?, services:List<DiscoveredService>, mobile:MobileSecuritySnapshot?):NetworkTrustAssessment {
        var score=100; val r=mutableListOf<String>()
        if(network==null){score-=30;r+="Current network evidence unavailable."}
        if(services.any{it.port==23&&it.reachable}){score-=25;r+="Telnet exposure observed."}
        if(services.any{it.port==445&&it.reachable}){score-=15;r+="SMB exposure observed."}
        if(mobile?.checks?.any{it.id=="MOB-DEV-002"&&it.status.name=="FAIL"}==true){score-=20;r+="USB debugging is enabled."}
        if(mobile?.checks?.any{it.id=="MOB-DNS-001"&&it.status.name=="FAIL"}==true){score-=10;r+="Private DNS requires review."}
        return NetworkTrustAssessment(score.coerceIn(0,100),r)
    }
    fun rogueDevices(baseline:List<String>, current:List<DiscoveredDevice>):List<RogueDevice> =
        current.filter{it.ipAddress !in baseline}.map{RogueDevice(it.ipAddress,"UNKNOWN","IP was not present in the trusted device inventory.")}
    fun correlate(drifts:List<SecurityDrift>):List<CorrelatedIncident> {
        if(drifts.isEmpty()) return emptyList()
        val network=drifts.filter{it.category=="NETWORK"}; val assets=drifts.filter{it.category=="DEVICE"||it.category=="SERVICE"}
        val apps=drifts.filter{it.category=="APP"}; val out=mutableListOf<CorrelatedIncident>()
        if(network.isNotEmpty()&&(assets.isNotEmpty()||apps.isNotEmpty())) out+=CorrelatedIncident("Environment changed with additional security changes","HIGH",(network+assets+apps).map{it.title},"Multiple baseline changes occurred together. Review the individual evidence before treating the changes as expected.")
        return out
    }
    fun appProfiles(apps:List<AppSecurityCheck>):List<AppRiskProfile> = apps.map { a ->
        val reasons=buildList{if(a.debuggable)add("Debuggable");if(a.cleartextAllowed==true)add("Cleartext allowed");if(a.backupAllowed==true)add("Backup allowed");if(a.exportedComponents>0)add(a.exportedComponents.toString()+" exported components");if(a.requestedDangerousPermissions.isNotEmpty())add(a.requestedDangerousPermissions.size.toString()+" sensitive permissions")}
        AppRiskProfile(a.packageName,a.appName,(reasons.size*18).coerceAtMost(100),reasons)
    }.sortedByDescending{it.risk}
    fun playbooks():List<SecurityPlaybook> = listOf(
        SecurityPlaybook("unknown-device","Unknown device detected",listOf("Identify the device","Check router/client inventory","Mark Trusted or Unknown","Re-run discovery","Record verification result")),
        SecurityPlaybook("permission-change","Application permission changed",listOf("Review the changed permission","Open Android App Info","Confirm the application source","Revoke if unnecessary","Re-run privacy scan")),
        SecurityPlaybook("network-change","Network identity changed",listOf("Verify SSID and BSSID","Verify gateway","Check DNS/Private DNS","Re-run network audit","Capture a new baseline only after verification")),
        SecurityPlaybook("high-finding","High-risk finding",listOf("Open evidence","Apply remediation guidance","Re-run the audit","Run verification","Record resolution"))
    )
}
