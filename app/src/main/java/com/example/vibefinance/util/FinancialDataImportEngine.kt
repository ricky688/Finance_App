package com.example.vibefinance.util

import android.content.Context
import android.net.Uri
import com.example.vibefinance.data.entity.AccountType
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.math.roundToLong

/**
 * High-performance, zero-dependency streaming importer for Excel (.xlsx) and CSV finance backups.
 * Designed to handle both "財務管家" exports and standard VibeFinance CSV exports with sub-100ms parse times.
 */
object FinancialDataImportEngine {

    data class ParsedRawTransaction(
        val amount: Double, // In VibeFinance: >0 for expense, <0 for income, >0 for transfer
        val category: String,
        val timestamp: Long,
        val sourceAccountName: String,
        val destinationAccountName: String? = null,
        val isTransfer: Boolean = false,
        val isIncome: Boolean = false,
        val description: String = ""
    )

    data class DetectedAccountInfo(
        val name: String,
        val detectedType: AccountType,
        val transactionCount: Int,
        val calculatedNetBalance: Double
    )

    data class ImportDataPreview(
        val rawTransactions: List<ParsedRawTransaction>,
        val detectedAccounts: List<DetectedAccountInfo>,
        val totalExpensesCount: Int,
        val totalExpensesSum: Double,
        val totalIncomeCount: Int,
        val totalIncomeSum: Double,
        val totalTransfersCount: Int,
        val totalTransfersSum: Double,
        val startDateMillis: Long?,
        val endDateMillis: Long?,
        val fileName: String,
        val error: String? = null
    )

    /**
     * Parses a file from a content Uri or InputStream.
     */
    fun parseUri(context: Context, uri: Uri): ImportDataPreview {
        val fileName = getFileNameFromUri(context, uri)
        return try {
            val inputStream = try {
                context.contentResolver.openInputStream(uri)
            } catch (_: Exception) {
                null
            } ?: try {
                context.contentResolver.openFileDescriptor(uri, "r")?.let { pfd ->
                    android.os.ParcelFileDescriptor.AutoCloseInputStream(pfd)
                }
            } catch (_: Exception) {
                null
            } ?: try {
                context.contentResolver.openAssetFileDescriptor(uri, "r")?.createInputStream()
            } catch (_: Exception) {
                null
            } ?: run {
                val candidateFiles = listOfNotNull(
                    uri.path?.let { java.io.File(it) },
                    java.io.File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS), fileName),
                    java.io.File("/sdcard/Download", fileName)
                )
                candidateFiles.firstOrNull { it.exists() && it.canRead() }?.let { java.io.FileInputStream(it) }
            }

            if (inputStream == null) {
                return ImportDataPreview(
                    rawTransactions = emptyList(),
                    detectedAccounts = emptyList(),
                    totalExpensesCount = 0,
                    totalExpensesSum = 0.0,
                    totalIncomeCount = 0,
                    totalIncomeSum = 0.0,
                    totalTransfersCount = 0,
                    totalTransfersSum = 0.0,
                    startDateMillis = null,
                    endDateMillis = null,
                    fileName = fileName,
                    error = "Unable to open selected file stream"
                )
            }

            inputStream.use { stream ->
                parseStream(stream, fileName)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ImportDataPreview(
                rawTransactions = emptyList(),
                detectedAccounts = emptyList(),
                totalExpensesCount = 0,
                totalExpensesSum = 0.0,
                totalIncomeCount = 0,
                totalIncomeSum = 0.0,
                totalTransfersCount = 0,
                totalTransfersSum = 0.0,
                startDateMillis = null,
                endDateMillis = null,
                fileName = fileName,
                error = "Import failed: ${e.localizedMessage ?: e.javaClass.simpleName}"
            )
        }
    }

    /**
     * Parses an InputStream based on file name or format inspection.
     */
    fun parseStream(stream: InputStream, fileName: String): ImportDataPreview {
        return try {
            if (fileName.endsWith(".xlsx", ignoreCase = true)) {
                parseXlsx(stream, fileName)
            } else {
                parseCsv(stream, fileName)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ImportDataPreview(
                rawTransactions = emptyList(),
                detectedAccounts = emptyList(),
                totalExpensesCount = 0,
                totalExpensesSum = 0.0,
                totalIncomeCount = 0,
                totalIncomeSum = 0.0,
                totalTransfersCount = 0,
                totalTransfersSum = 0.0,
                startDateMillis = null,
                endDateMillis = null,
                fileName = fileName,
                error = "Import failed: ${e.localizedMessage ?: e.javaClass.simpleName}"
            )
        }
    }

    /**
     * Parses an OpenXML (.xlsx) Excel workbook via java.util.zip.ZipFile + XmlPullParser.
     * Uses ZipFile over temporary storage to guarantee compatibility with archives containing data descriptors (0-size local headers).
     */
    private fun parseXlsx(stream: InputStream, fileName: String): ImportDataPreview {
        val tempFile = java.io.File.createTempFile("xlsx_import_", ".zip")
        try {
            tempFile.outputStream().use { out ->
                stream.copyTo(out)
            }

            val zipFile = java.util.zip.ZipFile(tempFile)
            try {
                // 1. Extract shared strings from xl/sharedStrings.xml
                val sharedStrings = extractSharedStrings(zipFile)

                // 2. Extract sheet rows from xl/worksheets/sheet1.xml (or first sheet)
                val rawRows = extractSheetRows(zipFile, sharedStrings)

                if (rawRows.isEmpty()) {
                    return ImportDataPreview(
                        rawTransactions = emptyList(),
                        detectedAccounts = emptyList(),
                        totalExpensesCount = 0,
                        totalExpensesSum = 0.0,
                        totalIncomeCount = 0,
                        totalIncomeSum = 0.0,
                        totalTransfersCount = 0,
                        totalTransfersSum = 0.0,
                        startDateMillis = null,
                        endDateMillis = null,
                        fileName = fileName,
                        error = "Excel sheet contains no data rows"
                    )
                }

                val headerRow = rawRows.first()
                val dataRows = rawRows.drop(1)

                return buildPreviewFromRows(headerRow, dataRows, fileName)
            } finally {
                zipFile.close()
            }
        } finally {
            tempFile.delete()
        }
    }

    /**
     * Extracts shared strings table from xl/sharedStrings.xml in the zip file.
     */
    private fun extractSharedStrings(zipFile: java.util.zip.ZipFile): List<String> {
        val result = mutableListOf<String>()
        val entry = zipFile.getEntry("xl/sharedStrings.xml") ?: return result

        zipFile.getInputStream(entry).use { stream ->
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(stream, "UTF-8")

            var eventType = parser.eventType
            var insideSi = false
            var insideT = false
            val currentSiText = StringBuilder()

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "si" -> {
                                insideSi = true
                                currentSiText.setLength(0)
                            }
                            "t" -> insideT = true
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (insideSi && insideT) {
                            currentSiText.append(parser.text)
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (parser.name) {
                            "t" -> insideT = false
                            "si" -> {
                                insideSi = false
                                result.add(currentSiText.toString())
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        }
        return result
    }

    /**
     * Streams rows and cells from sheet1.xml (or first worksheet).
     */
    private fun extractSheetRows(zipFile: java.util.zip.ZipFile, sharedStrings: List<String>): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val sheetEntry = zipFile.getEntry("xl/worksheets/sheet1.xml")
            ?: zipFile.entries().asSequence().firstOrNull {
                it.name.startsWith("xl/worksheets/sheet") && it.name.endsWith(".xml")
            }
            ?: return rows

        zipFile.getInputStream(sheetEntry).use { stream ->
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(stream, "UTF-8")

            var eventType = parser.eventType
            val currentRow = mutableMapOf<Int, String>()
            var currentCellRef = ""
            var currentCellType = ""
            var insideV = false
            var insideInlineT = false
            val currentText = StringBuilder()

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "row" -> {
                                currentRow.clear()
                            }
                            "c" -> {
                                currentCellRef = parser.getAttributeValue(null, "r") ?: ""
                                currentCellType = parser.getAttributeValue(null, "t") ?: ""
                                currentText.setLength(0)
                            }
                            "v" -> insideV = true
                            "t" -> insideInlineT = true
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (insideV || insideInlineT) {
                            currentText.append(parser.text)
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (parser.name) {
                            "v" -> insideV = false
                            "t" -> insideInlineT = false
                            "c" -> {
                                val colIndex = cellRefToColumnIndex(currentCellRef)
                                val rawVal = currentText.toString()
                                val resolvedVal = when (currentCellType) {
                                    "s" -> {
                                        val sIdx = rawVal.toIntOrNull()
                                        if (sIdx != null && sIdx in sharedStrings.indices) {
                                            sharedStrings[sIdx]
                                        } else {
                                            rawVal
                                        }
                                    }
                                    else -> rawVal
                                }
                                currentRow[colIndex] = resolvedVal
                            }
                            "row" -> {
                                if (currentRow.isNotEmpty()) {
                                    val maxCol = currentRow.keys.maxOrNull() ?: 0
                                    val rowList = (0..maxCol).map { col ->
                                        currentRow[col] ?: ""
                                    }
                                    rows.add(rowList)
                                }
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        }
        return rows
    }

    /**
     * Converts a cell reference (e.g. "A1", "K2", "AA5") into a 0-based column index.
     */
    private fun cellRefToColumnIndex(ref: String): Int {
        var col = 0
        for (ch in ref) {
            if (ch in 'A'..'Z') {
                col = col * 26 + (ch - 'A' + 1)
            } else if (ch in 'a'..'z') {
                col = col * 26 + (ch - 'a' + 1)
            } else {
                break
            }
        }
        return if (col > 0) col - 1 else 0
    }

    /**
     * Parses RFC 4180 CSV stream.
     */
    private fun parseCsv(stream: InputStream, fileName: String): ImportDataPreview {
        val reader = BufferedReader(InputStreamReader(stream, Charsets.UTF_8))
        val rows = mutableListOf<List<String>>()

        var line = reader.readLine()
        while (line != null) {
            val trimmed = line.trim()
            if (trimmed.isNotEmpty()) {
                rows.add(parseCsvLine(trimmed))
            }
            line = reader.readLine()
        }

        if (rows.isEmpty()) {
            return ImportDataPreview(
                rawTransactions = emptyList(),
                detectedAccounts = emptyList(),
                totalExpensesCount = 0,
                totalExpensesSum = 0.0,
                totalIncomeCount = 0,
                totalIncomeSum = 0.0,
                totalTransfersCount = 0,
                totalTransfersSum = 0.0,
                startDateMillis = null,
                endDateMillis = null,
                fileName = fileName,
                error = "CSV file is empty"
            )
        }

        val headerRow = rows.first()
        val dataRows = rows.drop(1)

        return buildPreviewFromRows(headerRow, dataRows, fileName)
    }

    /**
     * Parses a single CSV line with support for quotes and escaped quotes.
     */
    fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                    sb.append('"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim())
                sb.setLength(0)
            } else {
                sb.append(c)
            }
            i++
        }
        tokens.add(sb.toString().trim())
        return tokens
    }

    /**
     * Builds structured ImportDataPreview from parsed 2D row grid.
     */
    private fun buildPreviewFromRows(
        headerRow: List<String>,
        dataRows: List<List<String>>,
        fileName: String
    ): ImportDataPreview {
        val headers = headerRow.map { it.trim() }

        val dateCol = findColumnIndex(headers, listOf("日", "date", "time", "日期", "時間"))
        val accountCol = findColumnIndex(headers, listOf("資產", "account", "帳戶", "賬戶", "來源帳戶"))
        val categoryCol = findColumnIndex(headers, listOf("類別", "category", "分類"))
        val subCategoryCol = findColumnIndex(headers, listOf("子類別", "subcategory", "子分類"))
        val contentCol = findColumnIndex(headers, listOf("內容", "content", "項目", "商戶"))
        val amountCol = findColumnIndex(headers, listOf("hkd", "金額", "amount", "total", "價錢"))
        val typeCol = findColumnIndex(headers, listOf("收入/支出", "type", "類型", "收支"))
        val memoCol = findColumnIndex(headers, listOf("備忘錄", "memo", "description", "備註", "說明"))
        val destAccountCol = findColumnIndex(headers, listOf("destination account", "目標帳戶", "轉入帳戶"))

        if (dateCol == -1 || accountCol == -1 || amountCol == -1) {
            return ImportDataPreview(
                rawTransactions = emptyList(),
                detectedAccounts = emptyList(),
                totalExpensesCount = 0,
                totalExpensesSum = 0.0,
                totalIncomeCount = 0,
                totalIncomeSum = 0.0,
                totalTransfersCount = 0,
                totalTransfersSum = 0.0,
                startDateMillis = null,
                endDateMillis = null,
                fileName = fileName,
                error = "Could not identify Date, Account, or Amount columns in header"
            )
        }

        val rawTransactions = mutableListOf<ParsedRawTransaction>()
        val accountCounts = mutableMapOf<String, Int>()
        val accountBalances = mutableMapOf<String, Double>()

        var totalExpCount = 0
        var totalExpSum = 0.0
        var totalIncCount = 0
        var totalIncSum = 0.0
        var totalTrfCount = 0
        var totalTrfSum = 0.0
        var minTime = Long.MAX_VALUE
        var maxTime = Long.MIN_VALUE

        for (row in dataRows) {
            val dateStr = row.getOrNull(dateCol)?.trim() ?: ""
            val srcAcc = row.getOrNull(accountCol)?.trim() ?: ""
            val amtStr = row.getOrNull(amountCol)?.trim() ?: ""

            if (dateStr.isBlank() || srcAcc.isBlank() || amtStr.isBlank()) continue

            val rawAmt = amtStr.replace("$", "").replace(",", "").toDoubleOrNull() ?: continue
            val timestamp = parseDateValue(dateStr)

            if (timestamp in 1 until minTime) minTime = timestamp
            if (timestamp > maxTime) maxTime = timestamp

            val rawType = if (typeCol != -1) row.getOrNull(typeCol)?.trim() ?: "" else ""
            val category = if (categoryCol != -1) row.getOrNull(categoryCol)?.trim() ?: "Other" else "Other"
            val subCat = if (subCategoryCol != -1) row.getOrNull(subCategoryCol)?.trim() ?: "" else ""
            val content = if (contentCol != -1) row.getOrNull(contentCol)?.trim() ?: "" else ""
            val memo = if (memoCol != -1) row.getOrNull(memoCol)?.trim() ?: "" else ""

            val descParts = listOf(subCat, content, memo).filter { it.isNotBlank() }
            val description = descParts.joinToString(" · ")

            accountCounts[srcAcc] = (accountCounts[srcAcc] ?: 0) + 1

            // Schema translation
            val isTransfer = rawType.contains("轉帳") || rawType.contains("转账") ||
                    rawType.equals("transfer", ignoreCase = true)
            val isIncome = !isTransfer && (rawType.contains("收入") || rawType.equals("income", ignoreCase = true) || rawAmt < 0)

            if (isTransfer) {
                // In 財務管家, transfer destination is in Category column unless explicit destAccountCol exists
                val destAcc = if (destAccountCol != -1 && row.getOrNull(destAccountCol)?.isNotBlank() == true) {
                    row[destAccountCol].trim()
                } else {
                    category
                }

                if (destAcc.isNotBlank()) {
                    accountCounts[destAcc] = (accountCounts[destAcc] ?: 0) + 1
                    accountBalances[srcAcc] = (accountBalances[srcAcc] ?: 0.0) - kotlin.math.abs(rawAmt)
                    accountBalances[destAcc] = (accountBalances[destAcc] ?: 0.0) + kotlin.math.abs(rawAmt)
                }

                val absAmt = kotlin.math.abs(rawAmt)
                totalTrfCount++
                totalTrfSum += absAmt

                rawTransactions.add(
                    ParsedRawTransaction(
                        amount = absAmt,
                        category = "Transfer",
                        timestamp = timestamp,
                        sourceAccountName = srcAcc,
                        destinationAccountName = destAcc,
                        isTransfer = true,
                        isIncome = false,
                        description = description
                    )
                )
            } else if (isIncome) {
                val absAmt = kotlin.math.abs(rawAmt)
                totalIncCount++
                totalIncSum += absAmt

                accountBalances[srcAcc] = (accountBalances[srcAcc] ?: 0.0) + absAmt

                rawTransactions.add(
                    ParsedRawTransaction(
                        amount = -absAmt, // VibeFinance convention: negative for income
                        category = category.ifBlank { "Salary" },
                        timestamp = timestamp,
                        sourceAccountName = srcAcc,
                        destinationAccountName = null,
                        isTransfer = false,
                        isIncome = true,
                        description = description
                    )
                )
            } else {
                // Expense
                val absAmt = kotlin.math.abs(rawAmt)
                totalExpCount++
                totalExpSum += absAmt

                accountBalances[srcAcc] = (accountBalances[srcAcc] ?: 0.0) - absAmt

                rawTransactions.add(
                    ParsedRawTransaction(
                        amount = absAmt, // VibeFinance convention: positive for expense
                        category = category.ifBlank { "Other" },
                        timestamp = timestamp,
                        sourceAccountName = srcAcc,
                        destinationAccountName = null,
                        isTransfer = false,
                        isIncome = false,
                        description = description
                    )
                )
            }
        }

        // Build detected accounts list
        val detectedAccounts = accountCounts.keys.sorted().map { accName ->
            val detectedType = detectAccountType(accName)
            val netBal = accountBalances[accName] ?: 0.0
            // A credit card's balance is debt, so invert its cash-flow delta.
            // Keep a negative balance when payments exceed charges (card credit).
            val displayBalance = if (detectedType == AccountType.CC) -netBal else netBal
            DetectedAccountInfo(
                name = accName,
                detectedType = detectedType,
                transactionCount = accountCounts[accName] ?: 0,
                calculatedNetBalance = displayBalance
            )
        }

        return ImportDataPreview(
            rawTransactions = rawTransactions,
            detectedAccounts = detectedAccounts,
            totalExpensesCount = totalExpCount,
            totalExpensesSum = totalExpSum,
            totalIncomeCount = totalIncCount,
            totalIncomeSum = totalIncSum,
            totalTransfersCount = totalTrfCount,
            totalTransfersSum = totalTrfSum,
            startDateMillis = if (minTime != Long.MAX_VALUE) minTime else null,
            endDateMillis = if (maxTime != Long.MIN_VALUE) maxTime else null,
            fileName = fileName,
            error = null
        )
    }

    /**
     * Finds column index matching any synonym.
     */
    private fun findColumnIndex(headers: List<String>, candidates: List<String>): Int {
        for (candidate in candidates) {
            val idx = headers.indexOfFirst { it.equals(candidate, ignoreCase = true) }
            if (idx != -1) return idx
        }
        for (candidate in candidates) {
            val idx = headers.indexOfFirst { it.contains(candidate, ignoreCase = true) }
            if (idx != -1) return idx
        }
        return -1
    }

    /**
     * Resolves date strings or Excel serial numbers into epoch milliseconds.
     */
    fun parseDateValue(value: String): Long {
        val trimmed = value.trim()
        val doubleVal = trimmed.toDoubleOrNull()
        if (doubleVal != null && doubleVal > 30000.0 && doubleVal < 100000.0) {
            // Excel serial date format (days since 1899-12-30)
            val days = doubleVal.toLong()
            val fractionalDay = doubleVal - days
            val localDate = LocalDate.of(1899, 12, 30).plusDays(days)
            val totalSecondsInDay = (fractionalDay * 86400.0).roundToLong()
            val hours = (totalSecondsInDay / 3600) % 24
            val minutes = (totalSecondsInDay / 60) % 60
            val seconds = totalSecondsInDay % 60
            val ldt = localDate.atTime(hours.toInt(), minutes.toInt(), seconds.toInt())
            return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }

        val formats = listOf(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
        )

        for (fmt in formats) {
            try {
                val ldt = LocalDateTime.parse(trimmed, fmt)
                return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (_: Exception) {}
            try {
                val ld = LocalDate.parse(trimmed, fmt)
                return ld.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (_: Exception) {}
        }

        return System.currentTimeMillis()
    }

    /**
     * Classifies an account name into AccountType based on keyword heuristics.
     */
    fun detectAccountType(name: String): AccountType {
        val lower = name.lowercase(Locale.US).trim()
        return when {
            // Only an explicit debit label changes a card into an asset. Card networks
            // (Visa, Mastercard, UnionPay) also issue credit cards, so they are not enough.
            lower.contains("debit") || lower.contains("debet") ||
                    lower.contains("扣賬卡") || lower.contains("扣帳卡") ||
                    lower.contains("借記卡") || lower.contains("簽賬卡") -> AccountType.DEBIT

            lower.contains("cash") || lower.contains("wallet") || lower.contains("octopus") ||
                    lower.contains("八達通") || lower.contains("alipay") || lower.contains("payme") ||
                    lower.contains("wechat") || lower.contains("現金") || lower.contains("錢包") ||
                    lower.contains("brother") || lower == "go" -> AccountType.CASH

            lower.contains("card") || lower.contains("卡") || lower.contains("aeon") ||
                    lower.contains("credit") || lower.contains("enjoy") || lower.contains("wakuwaku") ||
                    lower.contains("visa") || lower.contains("master") -> AccountType.CC

            else -> AccountType.BANK
        }
    }

    fun detectAccountIcon(type: AccountType): String {
        return when (type) {
            AccountType.CASH -> "wallet"
            AccountType.CC -> "credit_card"
            AccountType.BANK -> "bank"
            AccountType.DEBIT -> "debit_card"
        }
    }

    fun getFileNameFromUri(context: Context, uri: Uri): String {
        var name = uri.lastPathSegment ?: "Imported_File"
        if (name.contains("/")) {
            name = name.substringAfterLast("/")
        }
        try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        val displayName = it.getString(nameIndex)
                        if (!displayName.isNullOrBlank()) {
                            name = displayName
                        }
                    }
                }
            }
        } catch (_: Exception) {}
        return name
    }
}
