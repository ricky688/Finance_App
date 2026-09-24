package com.example.vibefinance.ui.accounts

import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import org.junit.Assert.assertEquals
import org.junit.Test

class AccountsScreenBentoTest {

    @Test
    fun calculateNetWorth_withMixedAccounts_computesAssetsMinusDebtCorrectly() {
        val accounts = listOf(
            AccountEntity(id = 1, name = "Checking", type = AccountType.BANK, balance = 5000.0, icon = "bank"),
            AccountEntity(id = 2, name = "Savings", type = AccountType.BANK, balance = 12000.0, icon = "bank"),
            AccountEntity(id = 3, name = "Cash Wallet", type = AccountType.CASH, balance = 350.0, icon = "cash"),
            AccountEntity(id = 4, name = "Visa CC", type = AccountType.CC, balance = 1500.0, icon = "cc"),
            AccountEntity(id = 5, name = "Amex CC", type = AccountType.CC, balance = 850.0, icon = "cc")
        )

        val breakdown = calculateNetWorth(accounts)

        // Assets = 5000 + 12000 + 350 = 17350
        assertEquals(17350.0, breakdown.totalAssets, 0.001)
        // Debt = 1500 + 850 = 2350
        assertEquals(2350.0, breakdown.totalDebt, 0.001)
        // Net Worth = 17350 - 2350 = 15000
        assertEquals(15000.0, breakdown.netWorth, 0.001)
    }

    @Test
    fun calculateNetWorth_withNoDebt_returnsTotalAssetsAsNetWorth() {
        val accounts = listOf(
            AccountEntity(id = 1, name = "Checking", type = AccountType.BANK, balance = 3000.0, icon = "bank"),
            AccountEntity(id = 2, name = "Savings", type = AccountType.BANK, balance = 7000.0, icon = "bank")
        )

        val breakdown = calculateNetWorth(accounts)

        assertEquals(10000.0, breakdown.totalAssets, 0.001)
        assertEquals(0.0, breakdown.totalDebt, 0.001)
        assertEquals(10000.0, breakdown.netWorth, 0.001)
    }

    @Test
    fun calculateNetWorth_withDebtExceedingAssets_returnsNegativeNetWorth() {
        val accounts = listOf(
            AccountEntity(id = 1, name = "Checking", type = AccountType.BANK, balance = 1000.0, icon = "bank"),
            AccountEntity(id = 2, name = "Credit Card", type = AccountType.CC, balance = 3500.0, icon = "cc")
        )

        val breakdown = calculateNetWorth(accounts)

        assertEquals(1000.0, breakdown.totalAssets, 0.001)
        assertEquals(3500.0, breakdown.totalDebt, 0.001)
        assertEquals(-2500.0, breakdown.netWorth, 0.001)
    }

    @Test
    fun calculateNetWorth_emptyList_returnsZeroValues() {
        val breakdown = calculateNetWorth(emptyList())

        assertEquals(0.0, breakdown.totalAssets, 0.001)
        assertEquals(0.0, breakdown.totalDebt, 0.001)
        assertEquals(0.0, breakdown.netWorth, 0.001)
    }

    @Test
    fun filterAccounts_returnsExpectedSubsets() {
        val accounts = listOf(
            AccountEntity(id = 1, name = "Checking", type = AccountType.BANK, balance = 5000.0, icon = "bank"),
            AccountEntity(id = 2, name = "Savings", type = AccountType.BANK, balance = 12000.0, icon = "bank"),
            AccountEntity(id = 3, name = "Cash", type = AccountType.CASH, balance = 200.0, icon = "cash"),
            AccountEntity(id = 4, name = "Amex", type = AccountType.CC, balance = 1200.0, icon = "cc")
        )

        val all = filterAccounts(accounts, AssetFilter.ALL)
        assertEquals(4, all.size)

        val cashAndBank = filterAccounts(accounts, AssetFilter.CASH_BANK)
        assertEquals(3, cashAndBank.size)
        assert(cashAndBank.none { it.type == AccountType.CC })

        val creditCards = filterAccounts(accounts, AssetFilter.CREDIT_CARDS)
        assertEquals(1, creditCards.size)
        assertEquals("Amex", creditCards.first().name)
    }
}
