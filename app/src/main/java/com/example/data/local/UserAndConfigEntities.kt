package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val identifier: String, // phone or email
    val role: String = "USER", // "USER" or "ADMIN"
    val status: String = "ACTIVE", // "ACTIVE", "SUSPENDED", "DEACTIVATED"
    val preferredLanguage: String = "bn",
    val companionIdentifier: String? = null, // Companion phone or email (husband/wife)
    val companionRole: String = "SPOUSE", // "HUSBAND", "WIFE", "PARTNER"
    val currencySymbol: String = "₹", // INR ₹, USD $, EUR €, etc.
    val preferredUnitSystem: String = "METRIC", // METRIC, IMPERIAL
    val backupFrequency: String = "DAILY", // OFF, DAILY, WEEKLY, MONTHLY
    val lastBackupTime: Long = 0L,
    val avatarColor: Long = 0xFF2E7D32,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "admin_credentials")
data class AdminCredentialsEntity(
    @PrimaryKey
    val id: Int = 1,
    val adminUserId: String = "admin@kitchen.com",
    val adminPasswordHash: String = "admin123",
    val lastSyncedToSheets: Long = 0L
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val titleEn: String,
    val titleBn: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "INFO" // "VOICE", "INVENTORY", "MARKET", "WEBHOOK", "COMPANION", "ADMIN"
)

@Entity(tableName = "webhook_configs")
data class WebhookConfigEntity(
    @PrimaryKey
    val id: Int = 1,
    val webhookUrl: String = "",
    val autoSyncEnabled: Boolean = true,
    val lastSyncTimestamp: Long = 0L,
    val lastSyncStatus: String = "Never synced",
    val syncCount: Int = 0
)

@Entity(tableName = "shari_keys")
data class ShariKeyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memberName: String,
    val relation: String = "SPOUSE",
    val contactIdentifier: String,
    val keyCode: String,
    val role: String = "MEMBER",
    val canCheckStock: Boolean = true,
    val canEditToBuy: Boolean = true,
    val canCheckOut: Boolean = true,
    val isActive: Boolean = true,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "scanned_images")
data class ScannedImageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val imagePath: String,
    val extractedSummary: String = "",
    val itemsCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
