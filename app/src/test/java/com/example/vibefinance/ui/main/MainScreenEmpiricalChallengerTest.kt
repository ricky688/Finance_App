package com.example.vibefinance.ui.main

import android.content.Context
import android.content.res.Resources
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.util.CurrencyEngine
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.lang.reflect.Method
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MainScreenEmpiricalChallengerTest {

    private lateinit var context: Context
    private lateinit var resources: Resources
    private lateinit var getCategoryDisplayNameMethod: Method

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        resources = context.resources
        val mainScreenKt = Class.forName("com.example.vibefinance.ui.main.MainScreenKt")
        getCategoryDisplayNameMethod = mainScreenKt.getDeclaredMethod(
            "getCategoryDisplayNameFromResources",
            Resources::class.java,
            String::class.java
        ).apply { isAccessible = true }
    }

    private fun callGetCategoryDisplayName(category: String): String {
        return getCategoryDisplayNameMethod.invoke(null, resources, category) as String
    }

    // =========================================================================
    // 1. Category Deduplication & Resolution Tests
    // =========================================================================

    @Test
    fun categorySynonymsResolveToSameLocalizedDisplayName() {
        val foodExpected = resources.getString(R.string.cat_food)
        val foodSynonyms = listOf("food", "Food", "FOOD", "fOOd", "food & drink", "Food & Drink", "restaurant", "RESTAURANT", "food & dining")
        for (syn in foodSynonyms) {
            assertEquals("Synonym '$syn' must resolve to cat_food", foodExpected, callGetCategoryDisplayName(syn))
        }

        val transportExpected = resources.getString(R.string.cat_transport)
        val transportSynonyms = listOf("transport", "Transit", "commute", "BUS", "taxi")
        for (syn in transportSynonyms) {
            assertEquals("Synonym '$syn' must resolve to cat_transport", transportExpected, callGetCategoryDisplayName(syn))
        }

        val shoppingExpected = resources.getString(R.string.cat_shopping)
        for (syn in listOf("shopping", "Shopping", "clothing", "CLOTHING")) {
            assertEquals(shoppingExpected, callGetCategoryDisplayName(syn))
        }

        val utilitiesExpected = resources.getString(R.string.cat_utilities)
        for (syn in listOf("utilities", "electricity", "water", "bills")) {
            assertEquals(utilitiesExpected, callGetCategoryDisplayName(syn))
        }

        val salaryExpected = resources.getString(R.string.cat_salary)
        for (syn in listOf("salary", "paycheck", "wages", "income")) {
            assertEquals(salaryExpected, callGetCategoryDisplayName(syn))
        }
    }

    @Test
    fun categoryCaseInsensitivityAndWhitespaceHandling() {
        val foodExpected = resources.getString(R.string.cat_food)
        val whitespaceVariants = listOf(
            "  food  ",
            "\tFood & Drink\n",
            "   restaurant   ",
            "  FOOD & DINING  "
        )
        for (v in whitespaceVariants) {
            assertEquals("Whitespace variant '$v' must resolve to cat_food", foodExpected, callGetCategoryDisplayName(v))
        }
    }

    @Test
    fun unknownCustomCategoriesPreserved() {
        val custom1 = "Cryptocurrency"
        val custom2 = "SideProject"
        val custom3 = "Bubble Tea"
        assertEquals(custom1, callGetCategoryDisplayName(custom1))
        assertEquals(custom2, callGetCategoryDisplayName(custom2))
        assertEquals(custom3, callGetCategoryDisplayName(custom3))
    }

    @Test
    fun categoryDeduplicationPreventsDuplicateChips() {
        val rawCategories = listOf(
            "🍜 Food",
            "Food",
            "food & drink",
            "restaurant",
            "food & dining",
            "🚖 Transport",
            "Transport",
            "commute",
            "transit",
            "Shopping",
            "clothing",
            "CustomCategory",
            "Bubble Tea"
        )

        val deduplicated = rawCategories.distinctBy { callGetCategoryDisplayName(it) }

        // Expected deduplicated list should preserve only the first instance of each semantic category
        assertEquals(listOf("🍜 Food", "🚖 Transport", "Shopping", "CustomCategory", "Bubble Tea"), deduplicated)

        // Ensure all displayed names are completely unique
        val displayNames = deduplicated.map { callGetCategoryDisplayName(it) }
        assertEquals(displayNames.size, displayNames.distinct().size)
    }

    // =========================================================================
    // 2. Account Balance Formatting Tests
    // =========================================================================

    @Test
    fun assetAccountBalanceFormatting() {
        val assetPositive = AccountEntity(id = 1, name = "Hang Seng", type = AccountType.BANK, balance = 15200.0, icon = "bank")
        val formattedBalPositive = CurrencyEngine.formatSignedCompact(assetPositive.balance, prefix = "HK$")
        assertEquals("HK$15,200", formattedBalPositive)
        val labelPositive = resources.getString(R.string.ui_recurring_account_balance, formattedBalPositive)
        assertTrue(labelPositive.contains("HK$15,200"))

        val assetNegative = AccountEntity(id = 2, name = "Cash", type = AccountType.CASH, balance = -450.0, icon = "cash")
        val formattedBalNegative = CurrencyEngine.formatSignedCompact(assetNegative.balance, prefix = "HK$")
        assertEquals("-HK$450", formattedBalNegative)
        val labelNegative = resources.getString(R.string.ui_recurring_account_balance, formattedBalNegative)
        assertTrue(labelNegative.contains("-HK$450"))

        val assetZero = AccountEntity(id = 3, name = "Debit", type = AccountType.DEBIT, balance = 0.0, icon = "debit")
        val formattedBalZero = CurrencyEngine.formatSignedCompact(assetZero.balance, prefix = "HK$")
        assertEquals("HK$0", formattedBalZero)
        val labelZero = resources.getString(R.string.ui_recurring_account_balance, formattedBalZero)
        assertTrue(labelZero.contains("HK$0"))
    }

    @Test
    fun creditCardAccountDebtFormatting() {
        // In VibeFinance, positive balance on CC represents outstanding debt
        val ccOwing = AccountEntity(id = 10, name = "Mox Credit", type = AccountType.CC, balance = 8750.0, icon = "credit_card")
        val formattedOwing = CurrencyEngine.formatSignedCompact(ccOwing.balance, prefix = "HK$")
        assertEquals("HK$8,750", formattedOwing)
        val labelOwing = resources.getString(R.string.ui_recurring_account_owing, formattedOwing)
        assertTrue("CC label should use ui_recurring_account_owing (欠款/Owing)", labelOwing.contains("HK$8,750"))

        // Overpaid CC (negative debt = credit balance)
        val ccCredit = AccountEntity(id = 11, name = "HSBC Red", type = AccountType.CC, balance = -300.0, icon = "credit_card")
        val formattedCredit = CurrencyEngine.formatSignedCompact(ccCredit.balance, prefix = "HK$")
        assertEquals("-HK$300", formattedCredit)
        val labelCredit = resources.getString(R.string.ui_recurring_account_owing, formattedCredit)
        assertTrue(labelCredit.contains("-HK$300"))

        // Zero balance CC
        val ccZero = AccountEntity(id = 12, name = "Standard Chartered", type = AccountType.CC, balance = 0.0, icon = "credit_card")
        val formattedZero = CurrencyEngine.formatSignedCompact(ccZero.balance, prefix = "HK$")
        assertEquals("HK$0", formattedZero)
        val labelZero = resources.getString(R.string.ui_recurring_account_owing, formattedZero)
        assertTrue(labelZero.contains("HK$0"))
    }

    // =========================================================================
    // 3. Live Transfer Flow & Income Boost Calculations
    // =========================================================================

    @Test
    fun liveTransferBalanceFlowCalculation() {
        val bank = AccountEntity(id = 1, name = "Bank", type = AccountType.BANK, balance = 5000.0, icon = "bank")
        val cc = AccountEntity(id = 2, name = "Credit Card", type = AccountType.CC, balance = 3000.0, icon = "credit_card")

        // Case 1: Transfer from Bank to CC (paying off credit card debt)
        val transferAmount = 3000.0
        val isSourceCc = bank.type == AccountType.CC
        val isDestCc = cc.type == AccountType.CC
        val sourceProjected = if (isSourceCc) bank.balance + transferAmount else bank.balance - transferAmount
        val destProjected = if (isDestCc) cc.balance - transferAmount else cc.balance + transferAmount
        val isDebtCleared = isDestCc && transferAmount > 0.0 && destProjected <= 0.0
        val isInsufficient = !isSourceCc && transferAmount > 0.0 && bank.balance < transferAmount

        assertEquals(2000.0, sourceProjected, 0.001)
        assertEquals(0.0, destProjected, 0.001)
        assertTrue("Paying full debt must mark debt cleared", isDebtCleared)
        assertFalse("Source has 5000 >= 3000, not insufficient", isInsufficient)

        // Case 2: Insufficient source funds
        val largeAmount = 6000.0
        val isInsufficient2 = !isSourceCc && largeAmount > 0.0 && bank.balance < largeAmount
        assertTrue("Source has 5000 < 6000, must be insufficient", isInsufficient2)
    }

    @Test
    fun liveIncomeAssetBoostCalculation() {
        val bank = AccountEntity(id = 1, name = "Bank", type = AccountType.BANK, balance = 10000.0, icon = "bank")
        val cc = AccountEntity(id = 2, name = "CC", type = AccountType.CC, balance = 4000.0, icon = "credit_card")
        val allAccounts = listOf(bank, cc)

        // Income to Bank increases liquid assets
        val incomeAmount = 5000.0
        val liquidAccounts = allAccounts.filter { it.type != AccountType.CC && it.type != AccountType.INVESTMENT }
        val currentLiquidTotal = liquidAccounts.sumOf { it.balance }
        val projectedLiquidTotal = currentLiquidTotal + incomeAmount

        assertEquals(10000.0, currentLiquidTotal, 0.001)
        assertEquals(15000.0, projectedLiquidTotal, 0.001)

        // Income to CC reduces CC debt
        val projectedCcBalance = cc.balance - incomeAmount
        assertEquals(-1000.0, projectedCcBalance, 0.001)
    }
}
