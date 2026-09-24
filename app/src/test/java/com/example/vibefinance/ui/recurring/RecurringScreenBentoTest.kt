package com.example.vibefinance.ui.recurring

import androidx.compose.ui.graphics.Color
import com.example.vibefinance.data.entity.SubscriptionEntity
import com.example.vibefinance.data.entity.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class RecurringScreenBentoTest {

    @Test
    fun calculateRecurringCommitment_withMixedFrequencies_computesCorrectProjections() {
        val subscriptions = listOf(
            SubscriptionEntity(id = 1, name = "Netflix", amount = 100.0, frequency = "Monthly", category = "Entertainment", accountId = 1, nextPaymentDate = 0L),
            SubscriptionEntity(id = 2, name = "Gym", amount = 20.0, frequency = "Weekly", category = "Fitness", accountId = 1, nextPaymentDate = 0L), // 20 * 4.333 = 86.66
            SubscriptionEntity(id = 3, name = "Amazon Prime", amount = 120.0, frequency = "Yearly", category = "Shopping", accountId = 1, nextPaymentDate = 0L) // 120 / 12 = 10.0
        )

        val commitment = calculateRecurringCommitment(subscriptions)

        // Monthly = 100.0 + (20.0 * 4.333) + (120.0 / 12.0) = 100.0 + 86.66 + 10.0 = 196.66
        assertEquals(196.66, commitment.monthly, 0.001)
        // Annual = 196.66 * 12.0 = 2359.92
        assertEquals(2359.92, commitment.annual, 0.001)
        // Daily Impact = 196.66 / 30.0 = 6.555333...
        assertEquals(196.66 / 30.0, commitment.dailyImpact, 0.001)
    }

    @Test
    fun calculateRecurringCommitment_emptyList_returnsZeroes() {
        val commitment = calculateRecurringCommitment(emptyList())

        assertEquals(0.0, commitment.monthly, 0.001)
        assertEquals(0.0, commitment.annual, 0.001)
        assertEquals(0.0, commitment.dailyImpact, 0.001)
    }

    @Test
    fun filterSubscriptions_dueSoon_filtersWithinRange() {
        val today = LocalDate.of(2026, 9, 17)
        val zone = ZoneId.systemDefault()

        val in3Days = today.plusDays(3).atStartOfDay(zone).toInstant().toEpochMilli()
        val in10Days = today.plusDays(10).atStartOfDay(zone).toInstant().toEpochMilli()
        val yesterday = today.minusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val twoMonthsAgo = today.minusDays(60).atStartOfDay(zone).toInstant().toEpochMilli()

        val subscriptions = listOf(
            SubscriptionEntity(id = 1, name = "Sub 1 (in 3 days)", amount = 50.0, frequency = "Monthly", category = "Tech", accountId = 1, nextPaymentDate = in3Days),
            SubscriptionEntity(id = 2, name = "Sub 2 (in 10 days)", amount = 50.0, frequency = "Monthly", category = "Tech", accountId = 1, nextPaymentDate = in10Days),
            SubscriptionEntity(id = 3, name = "Sub 3 (yesterday)", amount = 50.0, frequency = "Monthly", category = "Tech", accountId = 1, nextPaymentDate = yesterday),
            SubscriptionEntity(id = 4, name = "Sub 4 (60 days ago)", amount = 50.0, frequency = "Monthly", category = "Tech", accountId = 1, nextPaymentDate = twoMonthsAgo)
        )

        val dueSoon = filterSubscriptions(subscriptions, RecurringFilter.DUE_SOON, today)

        // Sub 1 (in 3 days) and Sub 3 (yesterday, -1 days) are within -30..7 days
        assertEquals(2, dueSoon.size)
        assertTrue(dueSoon.any { it.id == 1L })
        assertTrue(dueSoon.any { it.id == 3L })
    }

    @Test
    fun filterSubscriptions_frequencyFilters_returnMatchingFrequencies() {
        val today = LocalDate.of(2026, 9, 17)
        val subscriptions = listOf(
            SubscriptionEntity(id = 1, name = "Monthly Sub", amount = 10.0, frequency = "Monthly", category = "Tech", accountId = 1, nextPaymentDate = 0L),
            SubscriptionEntity(id = 2, name = "Weekly Sub", amount = 5.0, frequency = "Weekly", category = "Tech", accountId = 1, nextPaymentDate = 0L),
            SubscriptionEntity(id = 3, name = "Annual Sub", amount = 100.0, frequency = "Annual", category = "Tech", accountId = 1, nextPaymentDate = 0L),
            SubscriptionEntity(id = 4, name = "Yearly Sub", amount = 120.0, frequency = "Yearly", category = "Tech", accountId = 1, nextPaymentDate = 0L)
        )

        val monthly = filterSubscriptions(subscriptions, RecurringFilter.MONTHLY, today)
        assertEquals(1, monthly.size)
        assertEquals("Monthly Sub", monthly.first().name)

        val weekly = filterSubscriptions(subscriptions, RecurringFilter.WEEKLY, today)
        assertEquals(1, weekly.size)
        assertEquals("Weekly Sub", weekly.first().name)

        val yearly = filterSubscriptions(subscriptions, RecurringFilter.YEARLY, today)
        assertEquals(2, yearly.size)
        assertTrue(yearly.any { it.name == "Annual Sub" })
        assertTrue(yearly.any { it.name == "Yearly Sub" })
    }

    @Test
    fun calculateRecurringCommitment_withActiveInstallments_addsToCommitment() {
        val subscriptions = listOf(
            SubscriptionEntity(id = 1, name = "Netflix", amount = 100.0, frequency = "Monthly", category = "Entertainment", accountId = 1, nextPaymentDate = 0L)
        )
        val activeInstallments = listOf(
            InstallmentPlan(
                groupId = "group-1",
                description = "iPhone 16 Pro",
                category = "Shopping",
                accountId = 1,
                totalAmount = 12000.0,
                monthlyAmount = 1000.0,
                paidInstallments = 2,
                totalInstallments = 12,
                remainingAmount = 10000.0,
                nextDueDate = System.currentTimeMillis() + 86400000L,
                isCompleted = false
            ),
            InstallmentPlan(
                groupId = "group-2",
                description = "Old Plan (Completed)",
                category = "Shopping",
                accountId = 1,
                totalAmount = 6000.0,
                monthlyAmount = 1000.0,
                paidInstallments = 6,
                totalInstallments = 6,
                remainingAmount = 0.0,
                nextDueDate = null,
                isCompleted = true
            )
        )

        val commitment = calculateRecurringCommitment(subscriptions, activeInstallments)

        // Subscriptions = 100.0/mo. Active Installment = 1000.0/mo. Total = 1100.0/mo.
        assertEquals(1100.0, commitment.monthly, 0.001)
        // Annual = 1100.0 * 12.0 = 13200.0
        assertEquals(13200.0, commitment.annual, 0.001)
        // Daily = 1100.0 / 30.0 = 36.666...
        assertEquals(1100.0 / 30.0, commitment.dailyImpact, 0.001)
    }

    @Test
    fun extractInstallmentPlans_groupsTransactionsCorrectly() {
        val now = System.currentTimeMillis()
        val txs = listOf(
            TransactionEntity(id = 1, amount = 500.0, category = "Electronics", timestamp = now - 100000L, accountId = 1, description = "Monitor", installmentNumber = 1, totalInstallments = 3, groupId = "grp-monitor"),
            TransactionEntity(id = 2, amount = 500.0, category = "Electronics", timestamp = now + 100000L, accountId = 1, description = "Monitor", installmentNumber = 2, totalInstallments = 3, groupId = "grp-monitor"),
            TransactionEntity(id = 3, amount = 500.0, category = "Electronics", timestamp = now + 200000L, accountId = 1, description = "Monitor", installmentNumber = 3, totalInstallments = 3, groupId = "grp-monitor"),
            // Non-installment transaction
            TransactionEntity(id = 4, amount = 20.0, category = "Food", timestamp = now, accountId = 1, description = "Lunch")
        )

        val plans = extractInstallmentPlans(txs)
        assertEquals(1, plans.size)
        val plan = plans.first()
        assertEquals("grp-monitor", plan.groupId)
        assertEquals("Monitor", plan.description)
        assertEquals(3, plan.totalInstallments)
        assertEquals(1, plan.paidInstallments)
        assertEquals(500.0, plan.monthlyAmount, 0.001)
        assertEquals(1500.0, plan.totalAmount, 0.001)
        assertEquals(1000.0, plan.remainingAmount, 0.001)
        assertEquals(false, plan.isCompleted)
    }

    @Test
    fun filterInstallments_respectsFilterModes() {
        val activePlan = InstallmentPlan(
            groupId = "grp-1",
            description = "Active",
            category = "Shopping",
            accountId = 1,
            totalAmount = 3000.0,
            monthlyAmount = 1000.0,
            paidInstallments = 1,
            totalInstallments = 3,
            remainingAmount = 2000.0,
            nextDueDate = System.currentTimeMillis() + 86400000L,
            isCompleted = false
        )
        val completedPlan = activePlan.copy(groupId = "grp-2", isCompleted = true, paidInstallments = 3, remainingAmount = 0.0)
        val list = listOf(activePlan, completedPlan)

        // Under ALL, only active plans are shown
        val underAll = filterInstallments(list, RecurringFilter.ALL)
        assertEquals(1, underAll.size)
        assertEquals("grp-1", underAll.first().groupId)

        // Under INSTALLMENTS, all installment plans (both active and completed) are shown
        val underInstallments = filterInstallments(list, RecurringFilter.INSTALLMENTS)
        assertEquals(2, underInstallments.size)

        // Under MONTHLY (which is subscription-only), installments should be empty
        val underMonthly = filterInstallments(list, RecurringFilter.MONTHLY)
        assertEquals(0, underMonthly.size)
    }

    @Test
    fun buildCategoryColorMap_assignsUniqueAndSemanticColors() {
        val categories = listOf("Entertainment", "Software / AI", "Electronics", "Utilities")
        val colorMap = buildCategoryColorMap(categories)

        // All 4 categories must be present
        assertEquals(4, colorMap.size)

        // Semantic colors must match expected distinct hues
        assertEquals(Color(0xFF8B5CF6), colorMap["Entertainment"]) // Royal Purple
        assertEquals(Color(0xFF10B981), colorMap["Software / AI"])  // Emerald Mint
        assertEquals(Color(0xFF3B82F6), colorMap["Electronics"])    // Ocean Blue
        assertEquals(Color(0xFFF59E0B), colorMap["Utilities"])      // Warm Amber

        // Every single category must have a distinct color (zero collisions)
        assertEquals(4, colorMap.values.toSet().size)
    }

    @Test
    fun buildCategoryColorMap_withUnknownCategories_assignsDistinctFallbackColorsWithoutCollisions() {
        val categories = listOf("CustomCatA", "CustomCatB", "CustomCatC", "CustomCatD", "CustomCatE")
        val colorMap = buildCategoryColorMap(categories)

        assertEquals(5, colorMap.size)
        // All 5 must have different colors from the distinct palette
        assertEquals(5, colorMap.values.toSet().size)
    }

    @Test
    fun findMatchingTransactionsForSubscription_matchesAutoChargeAndDirectDescription() {
        val chatGptSub = SubscriptionEntity(
            id = 10,
            name = "ChatGPT Plus",
            amount = 160.00,
            category = "Software / AI",
            frequency = "Monthly",
            accountId = 1,
            nextPaymentDate = System.currentTimeMillis() + 86400000L
        )

        val transactions = listOf(
            TransactionEntity(id = 1, amount = 160.00, category = "Software / AI", timestamp = 1000L, accountId = 1, description = "Auto-charge: ChatGPT Plus"),
            TransactionEntity(id = 2, amount = 160.00, category = "Software / AI", timestamp = 2000L, accountId = 1, description = "ChatGPT Plus monthly payment"),
            TransactionEntity(id = 3, amount = 45.50, category = "Food & Dining", timestamp = 3000L, accountId = 1, description = "Grocery Store"),
            TransactionEntity(id = 4, amount = 200.00, category = "Transfer", timestamp = 4000L, accountId = 1, toAccountId = 2, description = "ChatGPT Plus transfer reimbursement")
        )

        val matched = findMatchingTransactionsForSubscription(chatGptSub, transactions)

        // Tx 1 (Auto-charge) and Tx 2 (ChatGPT Plus monthly payment) must match.
        // Tx 3 (Grocery) is unrelated.
        // Tx 4 is a transfer (toAccountId != null) and should be excluded.
        assertEquals(2, matched.size)
        assertTrue(matched.any { it.id == 1L })
        assertTrue(matched.any { it.id == 2L })

        val totalPaid = matched.sumOf { it.amount }
        assertEquals(320.00, totalPaid, 0.001)
    }

    @Test
    fun findMatchingTransactionsForSubscription_withEmptyOrBlankName_returnsEmptyList() {
        val emptySub = SubscriptionEntity(
            id = 11,
            name = "   ",
            amount = 50.00,
            category = "General",
            frequency = "Monthly",
            accountId = 1,
            nextPaymentDate = 0L
        )

        val transactions = listOf(
            TransactionEntity(id = 1, amount = 50.00, category = "General", timestamp = 1000L, accountId = 1, description = "Some description")
        )

        val matched = findMatchingTransactionsForSubscription(emptySub, transactions)
        assertTrue(matched.isEmpty())
    }

    @Test
    fun findMatchingTransactionsForSubscription_caseInsensitiveMatching() {
        val sub = SubscriptionEntity(
            id = 12,
            name = "Netflix Premium",
            amount = 93.00,
            category = "Entertainment",
            frequency = "Monthly",
            accountId = 1,
            nextPaymentDate = 0L
        )

        val transactions = listOf(
            TransactionEntity(id = 1, amount = 93.00, category = "Entertainment", timestamp = 1000L, accountId = 1, description = "netflix premium renewal"),
            TransactionEntity(id = 2, amount = 15.49, category = "Entertainment", timestamp = 2000L, accountId = 1, description = "NETFLIX PREMIUM")
        )

        val matched = findMatchingTransactionsForSubscription(sub, transactions)
        assertEquals(2, matched.size)
    }
}
