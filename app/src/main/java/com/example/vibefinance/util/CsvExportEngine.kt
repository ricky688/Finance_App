package com.example.vibefinance.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.data.entity.AccountEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExportEngine {

    fun generateCsvContent(
        transactions: List<TransactionEntity>,
        accounts: List<AccountEntity>
    ): String {
        val accountMap = accounts.associateBy { it.id }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

        val sb = StringBuilder()
        // Header
        sb.append("ID,Date,Category,Type,Amount (HKD),Account,Destination Account,Description\n")

        for (tx in transactions) {
            val dateStr = dateFormat.format(Date(tx.timestamp))
            val accountName = accountMap[tx.accountId]?.name ?: "Unknown"
            val destName = if (tx.toAccountId != null) accountMap[tx.toAccountId]?.name ?: "Unknown" else ""
            val typeStr = if (tx.toAccountId != null) "Transfer" else if (tx.installmentNumber != null) "Installment (${tx.installmentNumber}/${tx.totalInstallments})" else "Expense"
            val cleanDesc = tx.description.replace(",", " ")

            sb.append("${tx.id},\"${dateStr}\",\"${tx.category}\",\"${typeStr}\",${tx.amount},\"${accountName}\",\"${destName}\",\"${cleanDesc}\"\n")
        }

        return sb.toString()
    }

    fun shareCsvFile(context: Context, csvContent: String) {
        try {
            val file = File(context.cacheDir, "VibeFinance_Transactions_${System.currentTimeMillis()}.csv")
            file.writeText(csvContent)

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "VibeFinance Expense Export")
                putExtra(Intent.EXTRA_TEXT, "Here is your exported transaction history from VibeFinance.")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Export Transactions CSV").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            android.util.Log.e("CsvExportEngine", "Failed to share CSV file", e)
        }
    }
}
