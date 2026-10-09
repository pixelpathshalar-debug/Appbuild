package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "market_guide_items")
data class MarketGuideEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nameEn: String,
    val nameBn: String,
    val sectionEn: String,
    val sectionBn: String,
    val locationTipEn: String,
    val locationTipBn: String,
    val buyingTipEn: String,
    val buyingTipBn: String,
    val minPrice: Double,
    val maxPrice: Double,
    val priceUnit: String = "kg",
    val defaultUnit: String = "kg",
    val defaultQty: Double = 1.0,
    val isCustom: Boolean = false
)
