package com.example.vibefinance.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.vibefinance.MainActivity
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.util.appString
import java.util.Locale

/** A short-lived receipt, with best-effort Android 16 promotion and identical fallback actions. */
object PaymentLoggingNotification {
    const val CHANNEL_ID = "payment_live"
    const val TIMEOUT_MS = 30_000L

    fun isValidPaymentId(id: String): Boolean = id.matches(Regex("[a-f0-9]{64}"))

    fun notificationId(paymentId: String): Int = paymentId.hashCode() and Int.MAX_VALUE

    fun cancel(context: Context, paymentId: String) {
        if (!isValidPaymentId(paymentId)) return
        context
            .getSystemService(NotificationManager::class.java)
            .cancel(paymentId, notificationId(paymentId))
    }

    internal fun build(
        context: Context,
        paymentId: String,
        title: String,
        content: String,
        amount: Double,
    ): Notification {
        require(isValidPaymentId(paymentId))
        val manager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    context.appString(R.string.nf_live_log_channel),
                    NotificationManager.IMPORTANCE_HIGH,
                )
            )
        }
        fun activityIntent(edit: Boolean) =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                action =
                    if (edit) NotificationActionReceiver.ACTION_CHANGE_CATEGORY
                    else Intent.ACTION_VIEW
                data =
                    Uri.Builder()
                        .scheme("vibefinance")
                        .authority("payment")
                        .appendPath(paymentId)
                        .appendPath(if (edit) "category" else "history")
                        .build()
                putExtra("navigate_tab", "history")
                if (edit) putExtra(NotificationActionReceiver.EXTRA_EDIT_PAYMENT, paymentId)
            }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val contentIntent = PendingIntent.getActivity(context, 0, activityIntent(false), flags)
        // Launch the Activity directly: receiver -> startActivity is blocked on Android 12+.
        val categoryIntent = PendingIntent.getActivity(context, 0, activityIntent(true), flags)
        val undoIntent =
            PendingIntent.getBroadcast(
                context,
                0,
                Intent(context, NotificationActionReceiver::class.java).apply {
                    action = NotificationActionReceiver.ACTION_UNDO_PAYMENT
                    data =
                        Uri.Builder()
                            .scheme("vibefinance")
                            .authority("payment")
                            .appendPath(paymentId)
                            .appendPath("undo")
                            .build()
                    putExtra(
                        NotificationActionReceiver.EXTRA_TRANSACTION_KEY,
                        "notification:$paymentId",
                    )
                    putExtra(
                        NotificationActionReceiver.EXTRA_NOTIFICATION_ID,
                        notificationId(paymentId),
                    )
                },
                flags,
            )
        val builder =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_payment_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .setTimeoutAfter(TIMEOUT_MS)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .addAction(
                    android.R.drawable.ic_menu_revert,
                    context.appString(R.string.nf_action_undo),
                    undoIntent,
                )
                .addAction(
                    android.R.drawable.ic_menu_edit,
                    context.appString(R.string.nf_action_change_category),
                    categoryIntent,
                )
        if (Build.VERSION.SDK_INT >= 36) {
            // Core bridges API 36/36.1 differences without reflection or linkage errors.
            builder
                .setRequestPromotedOngoing(true)
                .setShortCriticalText(
                    String.format(Locale.ROOT, "$%.2f", amount).takeIf { it.length <= 7 }
                )
        }
        return builder.build()
    }

    internal fun buildDecisionNotification(
        context: Context,
        payment: PendingPayment,
        suggestedAccount: AccountEntity?,
    ): Notification {
        require(isValidPaymentId(payment.id))
        val manager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    context.appString(R.string.nf_live_log_channel),
                    NotificationManager.IMPORTANCE_HIGH,
                )
            )
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        val decideActivityIntent = Intent(context, MainActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            action = NotificationActionReceiver.ACTION_DECIDE_TRANSFER
            data = Uri.Builder()
                .scheme("vibefinance")
                .authority("payment")
                .appendPath(payment.id)
                .appendPath("decide")
                .build()
            putExtra(NotificationActionReceiver.EXTRA_DECIDE_PAYMENT, payment.id)
        }
        val contentIntent = PendingIntent.getActivity(context, 0, decideActivityIntent, flags)
        val decideIntent = PendingIntent.getActivity(context, 1, decideActivityIntent, flags)

        val ignoreIntent = PendingIntent.getBroadcast(
            context,
            2,
            Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_IGNORE_TRANSFER
                data = Uri.Builder()
                    .scheme("vibefinance")
                    .authority("payment")
                    .appendPath(payment.id)
                    .appendPath("ignore")
                    .build()
                putExtra(NotificationActionReceiver.EXTRA_PAYMENT_ID, payment.id)
            },
            flags
        )

        val title = context.appString(R.string.nf_decision_transfer_title)
        val content = if (suggestedAccount != null) {
            context.appString(R.string.nf_decision_content_with_account, payment.merchant, payment.amount, suggestedAccount.name)
        } else {
            context.appString(R.string.nf_decision_content_no_account, payment.merchant, payment.amount)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_payment_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setTimeoutAfter(TIMEOUT_MS)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)

        if (suggestedAccount != null) {
            val confirmIntent = PendingIntent.getBroadcast(
                context,
                3,
                Intent(context, NotificationActionReceiver::class.java).apply {
                    action = NotificationActionReceiver.ACTION_CONFIRM_TRANSFER
                    data = Uri.Builder()
                        .scheme("vibefinance")
                        .authority("payment")
                        .appendPath(payment.id)
                        .appendPath("confirm")
                        .build()
                    putExtra(NotificationActionReceiver.EXTRA_PAYMENT_ID, payment.id)
                    putExtra(NotificationActionReceiver.EXTRA_ACCOUNT_ID, suggestedAccount.id)
                },
                flags
            )
            builder.addAction(
                android.R.drawable.ic_menu_save,
                context.appString(R.string.nf_action_confirm, suggestedAccount.name),
                confirmIntent
            )
        }

        builder.addAction(
            android.R.drawable.ic_menu_edit,
            context.appString(R.string.nf_action_decide),
            decideIntent
        )

        builder.addAction(
            android.R.drawable.ic_menu_close_clear_cancel,
            context.appString(R.string.nf_action_ignore),
            ignoreIntent
        )

        if (Build.VERSION.SDK_INT >= 36) {
            builder
                .setRequestPromotedOngoing(true)
                .setShortCriticalText(
                    String.format(Locale.ROOT, "$%.2f", payment.amount).takeIf { it.length <= 7 }
                )
        }
        return builder.build()
    }

    fun showDecision(
        context: Context,
        payment: PendingPayment,
        suggestedAccount: AccountEntity?
    ) {
        val notification = buildDecisionNotification(context, payment, suggestedAccount)
        val manager = context.getSystemService(NotificationManager::class.java)
        try {
            manager.notify(payment.id, notificationId(payment.id), notification)
            if (Build.VERSION.SDK_INT < 26) {
                Handler(Looper.getMainLooper())
                    .postDelayed({ cancel(context, payment.id) }, TIMEOUT_MS)
            }
        } catch (_: SecurityException) {
        }
    }

    fun show(context: Context, paymentId: String, title: String, content: String, amount: Double) {
        val notification = build(context, paymentId, title, content, amount)
        val manager = context.getSystemService(NotificationManager::class.java)
        try {
            manager.notify(paymentId, notificationId(paymentId), notification)
            // Native timeout is API 26+. Preserve a best-effort timeout on the app's API 24/25
            // minimum.
            if (Build.VERSION.SDK_INT < 26)
                Handler(Looper.getMainLooper())
                    .postDelayed({ cancel(context, paymentId) }, TIMEOUT_MS)
        } catch (_: SecurityException) {
            // The payment stays recorded when notification permission is denied.
        }
    }
}
