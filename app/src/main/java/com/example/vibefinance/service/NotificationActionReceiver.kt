package com.example.vibefinance.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Private receiver; only immutable notification PendingIntents grant access to background undo. */
class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_UNDO_PAYMENT -> {
                val key = intent.getStringExtra(EXTRA_TRANSACTION_KEY) ?: return
                val paymentId = key.removePrefix("notification:")
                if (
                    key != "notification:$paymentId" ||
                        !PaymentLoggingNotification.isValidPaymentId(paymentId)
                )
                    return
                if (
                    intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1) !=
                        PaymentLoggingNotification.notificationId(paymentId)
                )
                    return
                val result = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        PendingPaymentStore.undoPayment(context.applicationContext, paymentId)
                        PaymentLoggingNotification.cancel(context, paymentId)
                    } catch (error: Exception) {
                        // Keep the notification and its Undo affordance available if persistence fails.
                        Log.e("PaymentNotification", "Could not undo intercepted payment", error)
                    } finally {
                        result?.finish()
                    }
                }
            }
            ACTION_CONFIRM_TRANSFER -> {
                val paymentId = intent.getStringExtra(EXTRA_PAYMENT_ID) ?: return
                if (!PaymentLoggingNotification.isValidPaymentId(paymentId)) return
                val accountId = intent.getLongExtra(EXTRA_ACCOUNT_ID, -1L)
                if (accountId <= 0L) return
                val result = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        PendingPaymentStore.accept(context.applicationContext, paymentId, accountId, rememberChoice = false)
                        PaymentLoggingNotification.cancel(context, paymentId)
                    } catch (error: Exception) {
                        Log.e("PaymentNotification", "Could not confirm transfer payment", error)
                    } finally {
                        result?.finish()
                    }
                }
            }
            ACTION_IGNORE_TRANSFER -> {
                val paymentId = intent.getStringExtra(EXTRA_PAYMENT_ID) ?: return
                if (!PaymentLoggingNotification.isValidPaymentId(paymentId)) return
                val result = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        PendingPaymentStore.ignore(context.applicationContext, paymentId)
                        PaymentLoggingNotification.cancel(context, paymentId)
                    } catch (error: Exception) {
                        Log.e("PaymentNotification", "Could not ignore transfer payment", error)
                    } finally {
                        result?.finish()
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_UNDO_PAYMENT = "com.example.vibefinance.ACTION_UNDO_PAYMENT"
        const val ACTION_CHANGE_CATEGORY = "com.example.vibefinance.ACTION_CHANGE_CATEGORY"
        const val ACTION_CONFIRM_TRANSFER = "com.example.vibefinance.ACTION_CONFIRM_TRANSFER"
        const val ACTION_IGNORE_TRANSFER = "com.example.vibefinance.ACTION_IGNORE_TRANSFER"
        const val ACTION_DECIDE_TRANSFER = "com.example.vibefinance.ACTION_DECIDE_TRANSFER"
        const val EXTRA_TRANSACTION_KEY = "transaction_key"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_EDIT_PAYMENT = "navigate_to_edit_transaction"
        const val EXTRA_PAYMENT_ID = "extra_payment_id"
        const val EXTRA_ACCOUNT_ID = "extra_account_id"
        const val EXTRA_DECIDE_PAYMENT = "extra_decide_payment"
    }
}
