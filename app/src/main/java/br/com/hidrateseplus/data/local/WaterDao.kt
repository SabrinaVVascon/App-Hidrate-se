package br.com.hidrateseplus.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// Classe auxiliar para o resultado da query de histórico agrupado
data class HistoryRow(val date: String, val total: Int)

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

    // Agrupa todos os registros por dia, ordena do mais recente para o mais antigo
    @Query("SELECT date, SUM(amount) as total FROM water GROUP BY date ORDER BY date DESC")
    suspend fun getHistoryByDay(): List<HistoryRow>
}