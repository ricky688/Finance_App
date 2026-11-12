package com.example.vibefinance.data.repository

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.data.entity.SubscriptionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepository @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    val allSubscriptionsFlow: Flow<List<SubscriptionEntity>> = InMemoryDatabase.subscriptions

    fun insertSubscription(sub: SubscriptionEntity): Long {
        return InMemoryDatabase.insertSubscription(sub)
    }

    fun updateSubscription(sub: SubscriptionEntity) {
        InMemoryDatabase.updateSubscription(sub)
    }

    fun deleteSubscription(sub: SubscriptionEntity) {
        InMemoryDatabase.deleteSubscription(sub)
    }

    fun setSubscriptionPaidStatus(subscriptionId: Long, isPaid: Boolean, recordExpense: Boolean = false): Boolean {
        return InMemoryDatabase.setSubscriptionPaidStatus(subscriptionId, isPaid, recordExpense)
    }

    suspend fun checkAndTriggerAutoCharges(context: Context) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val subs = InMemoryDatabase.subscriptions.value
        val now = System.currentTimeMillis()

        for (sub in subs) {
            while (true) {
                val current = InMemoryDatabase.subscriptions.value.firstOrNull { it.id == sub.id } ?: break
                if (!InMemoryDatabase.chargeSubscriptionIfDue(current, now)) break
                val accountName = InMemoryDatabase.accounts.value.find { it.id == current.accountId }?.name ?: "Associated Card"
                // A notification failure cannot replay an already committed occurrence.
                runCatching { showLoggedNotification(context, current.amount, current.name, accountName) }
            }
        }
    }

    private fun showLoggedNotification(context: Context, amount: Double, name: String, cardName: String) {
        val channelId = "payment_logging"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Automatic Logging",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(context, com.example.vibefinance.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_tab", "recurring")
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            name.hashCode(),
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(context, channelId)
            .setContentTitle("Subscription Charged")
            .setContentText("Automatically charged $${String.format("%.2f", amount)} for $name from $cardName")
            .setSmallIcon(android.R.drawable.ic_menu_save)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
