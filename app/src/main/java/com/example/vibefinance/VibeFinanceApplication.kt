package com.example.vibefinance

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class VibeFinanceApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Wait for the IO recovery barrier before Android can create either activity or service.
        kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
            com.example.vibefinance.util.FullAppBackupEngine.recoverAtStartup(this@VibeFinanceApplication)
        }
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "cc_deadline_alerts"
            val channelName = "Credit Card Deadlines"
            val importance = NotificationManager.IMPORTANCE_HIGH
            
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Alerts for credit card payment deadlines"
            }
            
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
