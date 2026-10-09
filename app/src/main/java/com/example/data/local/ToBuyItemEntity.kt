package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "to_buy_items")
data class ToBuyItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pantryItemId: Long? = null,
    val nameEn: String,
    val nameBn: String,
    val category: String, // VEGETABLES, SPICES, STAPLES, MEDICINE
    val quantity: Double = 1.0,
    val unit: String = "kg",
    val rate: Double = 0.0, // Unit price / rate
    val estimatedPrice: Double = 0.0,
    val isBought: Boolean = false,
    val addedFromVoice: Boolean = false,
    val isPrivate: Boolean = false, // Private between husband/wife
    val reminderTime: Long? = null,
    val notes: String = "",
    val imageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
