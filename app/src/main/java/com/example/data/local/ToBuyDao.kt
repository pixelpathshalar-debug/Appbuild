package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ToBuyDao {
    @Query("SELECT * FROM to_buy_items ORDER BY isBought ASC, createdAt DESC")
    fun getAllToBuy(): Flow<List<ToBuyItemEntity>>

    @Query("SELECT * FROM to_buy_items WHERE LOWER(TRIM(nameEn)) = LOWER(TRIM(:name)) OR LOWER(TRIM(nameBn)) = LOWER(TRIM(:name)) LIMIT 1")
    suspend fun findExisting(name: String): ToBuyItemEntity?

    @Query("SELECT * FROM to_buy_items WHERE LOWER(TRIM(nameEn)) = LOWER(TRIM(:nameEn)) OR LOWER(TRIM(nameBn)) = LOWER(TRIM(:nameBn)) OR LOWER(TRIM(nameEn)) = LOWER(TRIM(:nameBn)) OR LOWER(TRIM(nameBn)) = LOWER(TRIM(:nameEn)) LIMIT 1")
    suspend fun findExistingByName(nameEn: String, nameBn: String): ToBuyItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ToBuyItemEntity): Long

    @Update
    suspend fun update(item: ToBuyItemEntity)

    @Delete
    suspend fun delete(item: ToBuyItemEntity)

    @Query("UPDATE to_buy_items SET isBought = :bought WHERE id = :id")
    suspend fun setBought(id: Long, bought: Boolean)

    @Query("DELETE FROM to_buy_items WHERE isBought = 1")
    suspend fun clearCompleted()

    @Query("DELETE FROM to_buy_items")
    suspend fun deleteAll()
}
