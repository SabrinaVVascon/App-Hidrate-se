package br.com.hidrateseplus.data.local
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocalDataSource(private val waterDao: WaterDao) {

    private fun getTodayDateString(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(Date())
    }

    suspend fun save(amount: Int) {
        val entry = WaterEntryEntity(
            amount = amount,
            date = getTodayDateString()
        )
        waterDao.insert(entry)
    }

    suspend fun getTodayTotal(): Int {
        val today = getTodayDateString()
        return waterDao.getTodayTotal(today)
    }
}