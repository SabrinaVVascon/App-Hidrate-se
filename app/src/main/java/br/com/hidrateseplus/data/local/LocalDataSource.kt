package br.com.hidrateseplus.data.local

import br.com.hidrateseplus.data.local.HistoryDay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ============================================================
// LocalDataSource — acesso à camada de dados local (Room)
// ============================================================
class LocalDataSource(private val waterDao: WaterDao) {

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    suspend fun save(amount: Int) {
        waterDao.insert(WaterEntryEntity(amount = amount, date = getTodayDateString()))
    }

    suspend fun getTodayTotal(): Int {
        return waterDao.getTodayTotal(getTodayDateString())
    }

    suspend fun deleteLastEntry() {
        val last = waterDao.getLastEntry(getTodayDateString())
        if (last != null) waterDao.deleteById(last.id)
    }

    // Busca histórico agrupado por dia — dados reais do banco
    suspend fun getHistory(): List<HistoryDay> {
        return waterDao.getHistoryByDay().map { row ->
            // Converte "yyyy-MM-dd" → "dd/MM/yyyy" para exibição
            val parts = row.date.split("-")
            val displayDate = if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else row.date
            HistoryDay(date = displayDate, total = "${row.total} ml")
        }
    }
}