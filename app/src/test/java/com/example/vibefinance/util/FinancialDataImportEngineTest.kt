package com.example.vibefinance.util

import com.example.vibefinance.data.entity.AccountType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.time.Instant
import java.time.ZoneId

class FinancialDataImportEngineTest {

    @Test
    fun testParseCsvLine() {
        val line = "1,\"2026-09-27 01:57:20\",\"Food & Drink\",\"Expense\",15.5,\"Wallet\",\"\",\"Lunch at Cafe, with dessert\""
        val tokens = FinancialDataImportEngine.parseCsvLine(line)
        assertEquals(8, tokens.size)
        assertEquals("1", tokens[0])
        assertEquals("2026-09-27 01:57:20", tokens[1])
        assertEquals("Food & Drink", tokens[2])
        assertEquals("Expense", tokens[3])
        assertEquals("15.5", tokens[4])
        assertEquals("Wallet", tokens[5])
        assertEquals("", tokens[6])
        assertEquals("Lunch at Cafe, with dessert", tokens[7])
    }

    @Test
    fun testParseDateValueExcelSerial() {
        // 46292.08148756944 corresponds to 2026-09-27 01:57:20
        val timestamp = FinancialDataImportEngine.parseDateValue("46292.08148756944")
        val ldt = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime()
        assertEquals(2026, ldt.year)
        assertEquals(9, ldt.monthValue)
        assertEquals(27, ldt.dayOfMonth)
        assertEquals(1, ldt.hour)
        assertEquals(57, ldt.minute)
    }

    @Test
    fun testDetectAccountType() {
        assertEquals(AccountType.CC, FinancialDataImportEngine.detectAccountType("AEON card"))
        assertEquals(AccountType.CC, FinancialDataImportEngine.detectAccountType("MOX card"))
        assertEquals(AccountType.CC, FinancialDataImportEngine.detectAccountType("Visa card"))
        assertEquals(AccountType.CC, FinancialDataImportEngine.detectAccountType("wakuwaku"))
        assertEquals(AccountType.CC, FinancialDataImportEngine.detectAccountType("enjoy"))
        assertEquals(AccountType.DEBIT, FinancialDataImportEngine.detectAccountType("MOX Visa debit card"))
        assertEquals(AccountType.DEBIT, FinancialDataImportEngine.detectAccountType("BOC 扣賬卡"))
        assertEquals(AccountType.DEBIT, FinancialDataImportEngine.detectAccountType("HSBC 簽賬卡"))
        assertEquals("debit_card", FinancialDataImportEngine.detectAccountIcon(AccountType.DEBIT))
        assertEquals(AccountType.CASH, FinancialDataImportEngine.detectAccountType("Cash"))
        assertEquals(AccountType.CASH, FinancialDataImportEngine.detectAccountType("octopus card"))
        assertEquals(AccountType.CASH, FinancialDataImportEngine.detectAccountType("alipay"))
        assertEquals(AccountType.CASH, FinancialDataImportEngine.detectAccountType("go"))
        assertEquals(AccountType.BANK, FinancialDataImportEngine.detectAccountType("BOC Go UnionPay Diamond"))
        assertEquals(AccountType.BANK, FinancialDataImportEngine.detectAccountType("Mox"))
        assertEquals(AccountType.BANK, FinancialDataImportEngine.detectAccountType("HSBC"))
        assertEquals(AccountType.BANK, FinancialDataImportEngine.detectAccountType("BOC"))
    }

    @Test
    fun testParseRealFinanceManagerXlsx() {
        val xlsxFile = File("財務管家_27-9-2026.xlsx")
        if (!xlsxFile.exists()) {
            println("Skipping testParseRealFinanceManagerXlsx because file is not in working directory")
            return
        }

        val preview = FileInputStream(xlsxFile).use { stream ->
            FinancialDataImportEngine.parseStream(stream, xlsxFile.name)
        }

        assertNull(preview.error)
        assertEquals(868, preview.rawTransactions.size)
        assertEquals(647, preview.totalExpensesCount)
        assertEquals(140, preview.totalIncomeCount)
        assertEquals(81, preview.totalTransfersCount)
        assertEquals(14, preview.detectedAccounts.size)

        // Verify date range
        assertNotNull(preview.startDateMillis)
        assertNotNull(preview.endDateMillis)
        assertTrue(preview.startDateMillis!! < preview.endDateMillis!!)

        // Check account names
        val accountNames = preview.detectedAccounts.map { it.name }.toSet()
        assertTrue(accountNames.contains("octopus card"))
        assertTrue(accountNames.contains("AEON card"))
        assertTrue(accountNames.contains("Mox"))
        assertTrue(accountNames.contains("Cash"))
    }
}
