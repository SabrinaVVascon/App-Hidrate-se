package br.com.hidrateseplus.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WaterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(waterEntry: WaterEntryEntity)

    @Query("SELECT COALESCE(SUM(amount), 0) FROM water WHERE date = :today")
    suspend fun getTodayTotal(today: String): Int

    @Query("SELECT * FROM water WHERE date = :today ORDER BY id DESC LIMIT 1")
    suspend fun getLastEntry(today: String): WaterEntryEntity?

    @Query("DELETE FROM water WHERE id = :id")
    suspend fun deleteById(id: Int)
}