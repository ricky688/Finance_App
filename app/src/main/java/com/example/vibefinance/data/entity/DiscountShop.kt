package com.example.vibefinance.data.entity

import androidx.compose.runtime.Immutable

@Immutable
data class ShopDiscountOffer(
    val accountId: Long,
    val discountRate: Double, // e.g. 5.0 for 5% off / cashback
    val promoDescription: String = "",
    val validUntil: String? = null
)

@Immutable
data class DiscountShop(
    val id: Long = 0L,
    val name: String,
    val aspect: String, // e.g. "Coffee & Cafe", "Supermarket", "Gas & Fuel", "Electronics", "Dining", "Retail"
    val latitude: Double,
    val longitude: Double,
    val address: String = "",
    val offers: List<ShopDiscountOffer> = emptyList(),
    val isUserCreated: Boolean = false,
    val iconCategory: String = aspect
)
