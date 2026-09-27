package com.uttarooque73.netguard.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.uttarooque73.netguard.audit.DiscoveredService
import com.uttarooque73.netguard.features.apps.AppSecurityCheck
import com.uttarooque73.netguard.features.autopilot.*
import com.uttarooque73.netguard.features.baseline.SecurityDrift
import com.uttarooque73.netguard.mobile.MobileSecuritySnapshot
import com.uttarooque73.netguard.network.NetworkInfo
import java.text.DateFormat
import java.util.Date

@Composable
fun SecurityAutopilotCard(network:NetworkInfo?,services:List<DiscoveredService>,apps:List<AppSecurityCheck>,mobile:MobileSecuritySnapshot?,drifts:List<SecurityDrift>,onRunCheck:()->Unit){
    val context=LocalContext.current
    var assessment by remember(network,services,apps,mobile,drifts){mutableStateOf(SecurityAutopilotEngine.assess(network,services,apps,mobile,drifts))}
    var apkReview by remember{mutableStateOf<ApkSecurityReview?>(null)}
    var selectedAction by remember{mutableStateOf<SecurityAction?>(null)}
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){uri:Uri?->
        if(uri!=null) runCatching{
            val apkFile=java.io.File(context.cacheDir,"netguard-review.apk")
            context.contentResolver.openInputStream(uri)?.use { input -> apkFile.outputStream().use { output -> input.copyTo(output) } }
            val path=apkFile.absolutePath
            val flags=android.content.pm.PackageManager.GET_PERMISSIONS or android.content.pm.PackageManager.GET_ACTIVITIES or android.content.pm.PackageManager.GET_SERVICES or android.content.pm.PackageManager.GET_RECEIVERS or android.content.pm.PackageManager.GET_PROVIDERS
            val info=if(Build.VERSION.SDK_INT>=33){
                context.packageManager.getPackageArchiveInfo(path,android.content.pm.PackageManager.PackageInfoFlags.of(flags.toLong()))
            }else{
                @Suppress("DEPRECATION") context.packageManager.getPackageArchiveInfo(path,flags)
            }
            if(info!=null){
                val label=info.applicationInfo?.loadLabel(context.packageManager)?.toString()?:info.packageName
                apkReview=SecurityAutopilotEngine.reviewApk(info,label)
            }
            apkFile.delete()
        }
    }

    fun refresh(){assessment=SecurityAutopilotEngine.assess(network,services,apps,mobile,drifts)}
    Card(Modifier.fillMaxWidth()){
        Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
            Text("Security Autopilot",style=MaterialTheme.typography.headlineSmall)
            Text("Turn security evidence into the next actions that actually improve your phone.",color=MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){
                Column(Modifier.weight(1f)){Text(assessment.score.toString(),style=MaterialTheme.typography.displaySmall);Text("Security posture")}
                Column(Modifier.weight(1f)){Text(assessment.highPriority.toString(),style=MaterialTheme.typography.headlineMedium);Text("High-priority actions")}
            }
            if(assessment.actions.isEmpty())Text("No immediate action was generated from the current evidence.")
            assessment.actions.take(4).forEach{action->
                Card(colors=CardDefaults.cardColors(containerColor=if(action.priority==SecurityActionPriority.HIGH)MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant)){
                    Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){
                        Text(action.priority.name+" • "+action.title,style=MaterialTheme.typography.titleMedium)
                        Text(action.reason)
                        Text("Evidence: "+action.evidence,style=MaterialTheme.typography.bodySmall)
                        Text("Do: "+action.action,style=MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                            LoadingButton(onClick={
                                selectedAction=action
                                when{
                                    action.id.startsWith("mobile-")&&action.id.contains("USB") -> context.startActivity(Intent(Settings.ACTION_SETTINGS))
                                    action.id.startsWith("app-") -> {
                                        val pkg=action.id.removePrefix("app-")
                                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:$pkg")))
                                    }
                                    else -> context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                                }
                            }){Text("Fix / Review")}
                            LoadingTextButton(onClick={selectedAction=action}){Text("Why?")}
                        }
                        if(selectedAction?.id==action.id)Text("Verify: "+action.verification,style=MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                LoadingButton(onClick={onRunCheck}){Text("Run Security Autopilot")}
                LoadingButton(onClick={picker.launch("application/vnd.android.package-archive")}){Text("Review APK")}
            }
            Text("Checked signals: "+assessment.checkedSignals,style=MaterialTheme.typography.bodySmall)
        }
    }

    apkReview?.let{review->
        Card(Modifier.fillMaxWidth()){
            Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){
                Text("Before You Install",style=MaterialTheme.typography.headlineSmall)
                Text(review.appName+" • "+review.packageName,style=MaterialTheme.typography.titleMedium)
                Text("Local APK risk: "+review.risk+"/100")
                Text("Target SDK: "+review.targetSdk+" • Version: "+(review.versionName?:"Unknown"))
                Text(if(review.debuggable)"⚠ Debuggable build" else "✓ Not marked debuggable")
                Text(if(review.cleartextAllowed==true)"⚠ Cleartext traffic allowed" else "✓ Cleartext traffic not enabled")
                Text(if(review.backupAllowed==true)"⚠ Backup allowed" else "✓ Backup not enabled")
                Text("Exported components: "+review.exportedComponents)
                if(review.requestedSensitivePermissions.isNotEmpty())Text("Sensitive permissions: "+review.requestedSensitivePermissions.joinToString())
                if(review.reasons.isEmpty())Text("No local manifest risk signals detected.")
                else review.reasons.forEach{Text("• "+it)}
                Text("This review is based on APK manifest metadata available to Android; it does not prove the app is malware.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                LoadingTextButton(onClick={apkReview=null}){Text("Close review")}
            }
        }
    }
}
