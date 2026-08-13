package com.example.vibefinance.data.repository

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.data.entity.SubscriptionEntity
import com.example.vibefinance.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
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

    suspend fun checkAndTriggerAutoCharges(context: Context) {
        val subs = InMemoryDatabase.subscriptions.value
        val now = System.currentTimeMillis()

        for (sub in subs) {
            var currentNextDate = sub.nextPaymentDate
            var subCopy = sub

            // Process multiple cycles if the app wasn't opened for a long time
            while (now >= currentNextDate) {
                // Record the auto-charge transaction
                val tx = TransactionEntity(
                    amount = subCopy.amount,
                    category = subCopy.category,
                    timestamp = currentNextDate,
                    accountId = subCopy.accountId,
                    description = "Auto-charge: ${subCopy.name}"
                )
                transactionRepository.insertTransaction(tx)

                // Get account name for notification
                val accountName = InMemoryDatabase.accounts.value.find { it.id == subCopy.accountId }?.name ?: "Associated Card"
                showLoggedNotification(context, subCopy.amount, subCopy.name, accountName)

                // Advance next payment date
                val advancedDate = advancePaymentDate(currentNextDate, subCopy.frequency)
                
                // Prevent infinite loop if calculation fails to advance
                if (advancedDate <= currentNextDate) {
                    break
                }
                
                currentNextDate = advancedDate
                subCopy = subCopy.copy(nextPaymentDate = currentNextDate)
            }

            if (subCopy.nextPaymentDate != sub.nextPaymentDate) {
                InMemoryDatabase.updateSubscription(subCopy)
            }
        }
    }

    private fun advancePaymentDate(currentNextPaymentDate: Long, frequency: String): Long {
        return try {
            val localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(currentNextPaymentDate), ZoneId.systemDefault())
            val advanced = if (frequency.lowercase() == "weekly") {
                localDateTime.plusWeeks(1)
            } else {
                localDateTime.plusMonths(1)
            }
            advanced.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (e: Exception) {
            currentNextPaymentDate + (30L * 24 * 60 * 60 * 1000) // Fallback 30 days
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

        val notification = Notification.Builder(context, channelId)
            .setContentTitle("Subscription Charged")
            .setContentText("Automatically charged $${String.format("%.2f", amount)} for $name from $cardName")
            .setSmallIcon(android.R.drawable.ic_menu_save)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
