package com.uttarooque73.netguard.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.uttarooque73.netguard.consumer.SecurityPosture

object SecurityNotificationHelper {
    private const val CHANNEL_ID = "security_alerts"
    fun notifyPosture(context: Context, posture: SecurityPosture) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            manager.createNotificationChannel(NotificationChannel(CHANNEL_ID, "Security alerts", NotificationManager.IMPORTANCE_DEFAULT))
        }
        if (android.os.Build.VERSION.SDK_INT >= 33 && androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) return
        val critical = posture.findings.count { it.severity.name == "CRITICAL" }
        val high = posture.findings.count { it.severity.name == "HIGH" }
        if (critical == 0 && high == 0 && posture.recommendations.isEmpty()) return
        val text = when {
            critical > 0 -> critical.toString() + " critical security finding(s) need attention."
            high > 0 -> high.toString() + " high-priority security finding(s) need review."
            else -> posture.recommendations.first().title
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("NetGuard security update")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text + " Open NetGuard to review evidence and recommended actions."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        manager.notify(1001, notification)
    }
}
