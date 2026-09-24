package com.example.vibefinance.data

import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.data.entity.BudgetEntity
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.data.repository.AccountRepository
import com.example.vibefinance.data.repository.BudgetRepository
import com.example.vibefinance.data.repository.SubscriptionRepository
import com.example.vibefinance.data.repository.TransactionRepository
import com.example.vibefinance.data.entity.SubscriptionEntity
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSeeder @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val subscriptionRepository: SubscriptionRepository
) {
    suspend fun seedDatabase() {
        val today = LocalDate.now()
        val startMilli = today.with(java.time.temporal.TemporalAdjusters.firstDayOfMonth())
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMilli = today.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth())
            .atTime(23, 59, 59)
            .atZone(ZoneId.systemDefault())
            .toInstant().toEpochMilli()

        // 1. Clear database first to ensure clean seed
        InMemoryDatabase.clearAllTables()

        // 2. Set monthly budget using custom period (Start of Month -> End of Month)
        budgetRepository.insertBudget(
            BudgetEntity(
                id = "ACTIVE_PERIOD",
                totalBudgetAmount = 1500.0,
                startDate = startMilli,
                endDate = endMilli
            )
        )

        // 3. Create dummy accounts
        val cashId = accountRepository.insertAccount(
            AccountEntity(
                name = "Wallet (Cash)",
                type = AccountType.CASH,
                balance = 250.0,
                icon = "wallet"
            )
        )

        val bankId = accountRepository.insertAccount(
            AccountEntity(
                name = "Chase Checking",
                type = AccountType.BANK,
                balance = 5400.0,
                icon = "bank"
            )
        )

        val ccId = accountRepository.insertAccount(
            AccountEntity(
                name = "Amex Gold Card",
                type = AccountType.CC,
                balance = 450.0,
                icon = "credit_card",
                creditLimit = 10000.0,
                billingDate = 10,
                paymentDate = 25,
                paymentDeadline = 10
            )
        )

        // Time calculations for timestamps
        val nowMilli = System.currentTimeMillis()
        val dayMilli = 24 * 60 * 60 * 1000L
        val yesterdayMilli = nowMilli - dayMilli
        val twoDaysAgoMilli = nowMilli - 2 * dayMilli

        // 4. Create sample transactions (plus installment group)

        // Tx 1: Starbucks Coffee (Expense on Cash)
        transactionRepository.insertTransaction(
            TransactionEntity(
                amount = 6.50,
                category = "Food & Drink",
                timestamp = nowMilli - (2 * 60 * 60 * 1000L), // 2 hours ago
                accountId = cashId,
                description = "Starbucks Coffee"
            )
        )

        // Tx 2: Salary Paycheck (Income on Bank)
        transactionRepository.insertTransaction(
            TransactionEntity(
                amount = -2500.0,
                category = "Income",
                timestamp = yesterdayMilli,
                accountId = bankId,
                description = "Monthly Paycheck"
            )
        )

        // Tx 3: Target Grocery (Expense on Credit Card)
        transactionRepository.insertTransaction(
            TransactionEntity(
                amount = 85.00,
                category = "Groceries",
                timestamp = twoDaysAgoMilli,
                accountId = ccId,
                description = "Target Supercenter"
            )
        )

        // Tx 4: Internal Transfer (Chase Bank -> Amex CC payment)
        transactionRepository.insertTransaction(
            TransactionEntity(
                amount = 200.00,
                category = "Transfer",
                timestamp = yesterdayMilli + (1 * 60 * 60 * 1000L),
                accountId = bankId,
                toAccountId = ccId,
                isExcludedFromDailyBudget = true,
                description = "Amex CC Payment"
            )
        )

        // Tx 5: Netflix Subscription (Expense on CC)
        transactionRepository.insertTransaction(
            TransactionEntity(
                amount = 15.49,
                category = "Entertainment",
                timestamp = nowMilli - (10 * 60 * 1000L), // 10 mins ago
                accountId = ccId,
                description = "Netflix Premium"
            )
        )

        // Tx 6: CC Installment Example (iPhone purchase split into 12 installments)
        transactionRepository.insertInstallmentTransaction(
            baseTransaction = TransactionEntity(
                amount = 1200.00,
                category = "Electronics",
                timestamp = twoDaysAgoMilli,
                accountId = ccId,
                description = "iPhone 16 Pro Installment"
            ),
            installments = 12
        )

        // Tx 7: ChatGPT Plus Subscription Payment (Previous paid record)
        transactionRepository.insertTransaction(
            TransactionEntity(
                amount = 160.00,
                category = "Software / AI",
                timestamp = nowMilli - (18 * dayMilli),
                accountId = ccId,
                description = "Auto-charge: ChatGPT Plus"
            )
        )

        // 5. Seed realistic sample subscriptions
        val twoDaysLaterMilli = nowMilli + (2 * dayMilli)
        val fiveDaysLaterMilli = nowMilli + (5 * dayMilli)
        val twelveDaysLaterMilli = nowMilli + (12 * dayMilli)
        val twentyDaysLaterMilli = nowMilli + (20 * dayMilli)

        subscriptionRepository.insertSubscription(
            SubscriptionEntity(
                name = "Netflix Premium",
                amount = 93.00,
                category = "Entertainment",
                frequency = "Monthly",
                nextPaymentDate = twoDaysLaterMilli,
                accountId = ccId
            )
        )

        subscriptionRepository.insertSubscription(
            SubscriptionEntity(
                name = "Spotify Family",
                amount = 68.00,
                category = "Entertainment",
                frequency = "Monthly",
                nextPaymentDate = fiveDaysLaterMilli,
                accountId = ccId
            )
        )

        subscriptionRepository.insertSubscription(
            SubscriptionEntity(
                name = "ChatGPT Plus",
                amount = 160.00,
                category = "Software / AI",
                frequency = "Monthly",
                nextPaymentDate = twelveDaysLaterMilli,
                accountId = ccId
            )
        )

        subscriptionRepository.insertSubscription(
            SubscriptionEntity(
                name = "iCloud+ 200GB",
                amount = 23.00,
                category = "Utilities",
                frequency = "Monthly",
                nextPaymentDate = twentyDaysLaterMilli,
                accountId = bankId
            )
        )

        // 6. Seed real-world shops with GNSS location & credit card discounts
        seedDiscountShops(ccId)
    }

    private fun seedDiscountShops(ccAccountId: Long) {
        val baseLat = 37.7749
        val baseLng = -122.4194

        val defaultShops = listOf(
            com.example.vibefinance.data.entity.DiscountShop(
                name = "Starbucks Reserve",
                aspect = "Coffee & Cafe",
                latitude = baseLat + 0.0018,
                longitude = baseLng + 0.0012,
                address = "555 Market St, Downtown",
                offers = listOf(
                    com.example.vibefinance.data.entity.ShopDiscountOffer(
                        accountId = ccAccountId,
                        discountRate = 10.0,
                        promoDescription = "10% Cashback on Handcrafted Drinks & Beans"
                    )
                )
            ),
            com.example.vibefinance.data.entity.DiscountShop(
                name = "Whole Foods Market",
                aspect = "Supermarket",
                latitude = baseLat + 0.0035,
                longitude = baseLng - 0.0025,
                address = "399 4th Street",
                offers = listOf(
                    com.example.vibefinance.data.entity.ShopDiscountOffer(
                        accountId = ccAccountId,
                        discountRate = 6.0,
                        promoDescription = "6% Cashback on Organic Groceries"
                    )
                )
            ),
            com.example.vibefinance.data.entity.DiscountShop(
                name = "Shell Gas & Station",
                aspect = "Gas & Fuel",
                latitude = baseLat - 0.0022,
                longitude = baseLng + 0.0038,
                address = "1200 Howard St",
                offers = listOf(
                    com.example.vibefinance.data.entity.ShopDiscountOffer(
                        accountId = ccAccountId,
                        discountRate = 5.0,
                        promoDescription = "5% Instant Savings on Fuel & Car Wash"
                    )
                )
            ),
            com.example.vibefinance.data.entity.DiscountShop(
                name = "Best Buy Megastore",
                aspect = "Electronics",
                latitude = baseLat + 0.0060,
                longitude = baseLng + 0.0050,
                address = "1717 Harrison St",
                offers = listOf(
                    com.example.vibefinance.data.entity.ShopDiscountOffer(
                        accountId = ccAccountId,
                        discountRate = 8.0,
                        promoDescription = "8% Rewards Back on Gadgets & Tech"
                    )
                )
            ),
            com.example.vibefinance.data.entity.DiscountShop(
                name = "Blue Bottle Coffee",
                aspect = "Coffee & Cafe",
                latitude = baseLat - 0.0015,
                longitude = baseLng - 0.0018,
                address = "66 Mint Plaza",
                offers = listOf(
                    com.example.vibefinance.data.entity.ShopDiscountOffer(
                        accountId = ccAccountId,
                        discountRate = 12.0,
                        promoDescription = "12% Flash Discount on Espresso & Pastries"
                    )
                )
            ),
            com.example.vibefinance.data.entity.DiscountShop(
                name = "Target Supercenter",
                aspect = "Retail & Shopping",
                latitude = baseLat + 0.0042,
                longitude = baseLng - 0.0045,
                address = "789 Mission St",
                offers = listOf(
                    com.example.vibefinance.data.entity.ShopDiscountOffer(
                        accountId = ccAccountId,
                        discountRate = 5.0,
                        promoDescription = "5% Off Home Goods & Apparel"
                    )
                )
            ),
            com.example.vibefinance.data.entity.DiscountShop(
                name = "Subway Fresh Sandwich",
                aspect = "Dining",
                latitude = baseLat - 0.0030,
                longitude = baseLng - 0.0032,
                address = "450 3rd St",
                offers = listOf(
                    com.example.vibefinance.data.entity.ShopDiscountOffer(
                        accountId = ccAccountId,
                        discountRate = 7.5,
                        promoDescription = "7.5% Off Footlong Meals & Combos"
                    )
                )
            )
        )

        defaultShops.forEach { shop ->
            InMemoryDatabase.insertDiscountShop(shop)
        }
    }
}
