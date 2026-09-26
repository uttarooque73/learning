package com.uttarooque73.netguard.security

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.uttarooque73.netguard.R

object NotificationHelper {
    private const val CHANNEL = "security_alerts"
    fun notifyFinding(context: Context, title: String, detail: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(NotificationChannel(CHANNEL, "Security alerts", NotificationManager.IMPORTANCE_DEFAULT))
        val notification = NotificationCompat.Builder(context, CHANNEL).setSmallIcon(R.mipmap.ic_launcher).setContentTitle(title).setContentText(detail).setAutoCancel(true).build()
        manager.notify(title.hashCode(), notification)
    }
}