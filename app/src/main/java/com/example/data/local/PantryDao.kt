package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PantryDao {
    @Query("SELECT * FROM pantry_items ORDER BY isFinished ASC, quantity ASC, nameEn ASC")
    fun getAllItems(): Flow<List<PantryItemEntity>>

    @Query("SELECT * FROM pantry_items WHERE id = :id")
    suspend fun getItemById(id: Long): PantryItemEntity?

    @Query("SELECT * FROM pantry_items WHERE LOWER(TRIM(nameEn)) = LOWER(TRIM(:name)) OR LOWER(TRIM(nameBn)) = LOWER(TRIM(:name)) LIMIT 1")
    suspend fun findByName(name: String): PantryItemEntity?

    @Query("SELECT * FROM pantry_items WHERE LOWER(TRIM(nameEn)) = LOWER(TRIM(:nameEn)) OR LOWER(TRIM(nameBn)) = LOWER(TRIM(:nameBn)) OR LOWER(TRIM(nameEn)) = LOWER(TRIM(:nameBn)) OR LOWER(TRIM(nameBn)) = LOWER(TRIM(:nameEn)) LIMIT 1")
    suspend fun findExistingByName(nameEn: String, nameBn: String): PantryItemEntity?

    @Query("SELECT * FROM pantry_items WHERE LOWER(nameEn) LIKE '%' || LOWER(:keyword) || '%' OR LOWER(nameBn) LIKE '%' || LOWER(:keyword) || '%' LIMIT 1")
    suspend fun searchFirstMatching(keyword: String): PantryItemEntity?

    @Query("SELECT * FROM pantry_items WHERE isFinished = 0 AND quantity <= minThreshold AND quantity > 0")
    suspend fun getLowStockItems(): List<PantryItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: PantryItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<PantryItemEntity>)

    @Update
    suspend fun updateItem(item: PantryItemEntity)

    @Delete
    suspend fun deleteItem(item: PantryItemEntity)

    @Query("UPDATE pantry_items SET isFinished = 1, quantity = 0.0 WHERE id = :id")
    suspend fun markAsFinished(id: Long)

    @Query("UPDATE pantry_items SET quantity = :newQty, isFinished = :isFin WHERE id = :id")
    suspend fun updateQuantity(id: Long, newQty: Double, isFin: Boolean)

    @Query("DELETE FROM pantry_items")
    suspend fun deleteAll()
}
