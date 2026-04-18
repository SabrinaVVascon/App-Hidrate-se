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
}