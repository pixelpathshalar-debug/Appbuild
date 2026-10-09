package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pantry_items")
data class PantryItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nameEn: String,
    val nameBn: String,
    val category: String, // VEGETABLES, SPICES, STAPLES, MEDICINE
    val quantity: Double,
    val unit: String, // kg, g, pcs, bundle, packet, strip, tablet, bottle
    val minThreshold: Double = 1.0,
    val rate: Double = 0.0, // Unit price/rate (e.g. 40/kg or 15/strip)
    val price: Double = 0.0, // Total calculated or entered price
    val purchaseDate: Long = System.currentTimeMillis(),
    val isFinished: Boolean = false,
    val isPrivate: Boolean = false, // If true, only visible to creator, hidden from companion
    val reminderTime: Long? = null, // Optional reminder timestamp
    val notes: String = "",
    val userId: String = "default_user"
) {
    val isLowStock: Boolean
        get() = !isFinished && quantity <= minThreshold && quantity > 0.0
}
