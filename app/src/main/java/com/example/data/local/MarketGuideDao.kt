package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketGuideDao {
    @Query("SELECT * FROM market_guide_items ORDER BY id ASC")
    fun getAllMarketItems(): Flow<List<MarketGuideEntity>>

    @Query("SELECT COUNT(*) FROM market_guide_items")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: MarketGuideEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<MarketGuideEntity>)

    @Update
    suspend fun updateItem(item: MarketGuideEntity)

    @Delete
    suspend fun deleteItem(item: MarketGuideEntity)

    @Query("DELETE FROM market_guide_items WHERE id = :id")
    suspend fun deleteById(id: Long)
}
