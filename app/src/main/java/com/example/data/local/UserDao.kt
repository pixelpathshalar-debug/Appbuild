package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE identifier = :identifier LIMIT 1")
    suspend fun findByIdentifier(identifier: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET status = :status WHERE id = :userId")
    suspend fun updateUserStatus(userId: String, status: String)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)

    // Admin Credentials
    @Query("SELECT * FROM admin_credentials WHERE id = 1")
    suspend fun getAdminCredentials(): AdminCredentialsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAdminCredentials(creds: AdminCredentialsEntity)

    // Activity Logs
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentLogs(): Flow<List<ActivityLogEntity>>

    @Insert
    suspend fun insertLog(log: ActivityLogEntity)

    // Webhook Config
    @Query("SELECT * FROM webhook_configs WHERE id = 1")
    fun getWebhookConfig(): Flow<WebhookConfigEntity?>

    @Query("SELECT * FROM webhook_configs WHERE id = 1")
    suspend fun getWebhookConfigOnce(): WebhookConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWebhookConfig(config: WebhookConfigEntity)

    // Shari-Key (চাবির গোছা - Family Access Keys) CRUD
    @Query("SELECT * FROM shari_keys ORDER BY createdAt DESC")
    fun getAllShariKeys(): Flow<List<ShariKeyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShariKey(key: ShariKeyEntity): Long

    @Update
    suspend fun updateShariKey(key: ShariKeyEntity)

    @Query("DELETE FROM shari_keys WHERE id = :id")
    suspend fun deleteShariKey(id: Long)

    @Query("SELECT * FROM shari_keys WHERE keyCode = :code LIMIT 1")
    suspend fun getShariKeyByCode(code: String): ShariKeyEntity?

    // Scanned Images & Receipts
    @Query("SELECT * FROM scanned_images ORDER BY createdAt DESC")
    fun getAllScannedImages(): Flow<List<ScannedImageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScannedImage(image: ScannedImageEntity): Long

    @Query("DELETE FROM scanned_images WHERE id = :id")
    suspend fun deleteScannedImage(id: Long)
}
