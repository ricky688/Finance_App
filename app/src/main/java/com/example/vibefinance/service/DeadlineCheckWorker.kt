package com.example.vibefinance.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.R
import com.example.vibefinance.util.appString
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class DeadlineCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Query all credit card accounts
        val ccAccounts = InMemoryDatabase.accounts.value.filter { it.type == AccountType.CC }
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        for (account in ccAccounts) {
            val paymentDeadlineDay = account.paymentDeadline ?: continue
            
            // Calculate remaining days until deadline
            val today = LocalDate.now()
            val dueLocalDate = if (today.dayOfMonth <= paymentDeadlineDay) {
                today.withDayOfMonth(paymentDeadlineDay)
            } else {
                today.plusMonths(1).withDayOfMonth(paymentDeadlineDay)
            }
            
            val daysUntilDeadline = ChronoUnit.DAYS.between(today, dueLocalDate).toInt()
            
            // If in the "Red Alert" zone (<= 3 days), trigger notification
            if (daysUntilDeadline <= 3) {
                showRedAlertNotification(notificationManager, account.name, daysUntilDeadline)
            }
        }

        return Result.success()
    }

    fun triggerNotificationBlockForTest(cardName: String, days: Int) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        showRedAlertNotification(notificationManager, cardName, days)
    }

    private fun showRedAlertNotification(
        notificationManager: NotificationManager,
        cardName: String,
        days: Int
    ) {
        val channelId = "cc_deadline_alerts"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                applicationContext.appString(R.string.nf_deadline_channel),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = applicationContext.appString(R.string.nf_deadline_channel_description)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val textContent = if (days == 0) {
            applicationContext.appString(R.string.nf_payment_due_today)
        } else {
            applicationContext.appString(R.string.nf_payment_due_days, days)
        }

        val intent = android.content.Intent(applicationContext, com.example.vibefinance.MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_tab", "recurring")
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            applicationContext,
            cardName.hashCode(),
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_chat) // Robust system asset to ensure no resource build resolution failures
            .setContentTitle(applicationContext.appString(R.string.nf_payment_due_title, cardName))
            .setContentText(textContent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(cardName.hashCode(), builder.build())
    }
}
