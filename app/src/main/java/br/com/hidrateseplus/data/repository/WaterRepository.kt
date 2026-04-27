package br.com.hidrateseplus.data.repository

import br.com.hidrateseplus.data.local.HistoryDay
import br.com.hidrateseplus.data.local.LocalDataSource
import br.com.hidrateseplus.data.remote.RemoteDataSource

// ============================================================
// PADRÃO: Repository + Proxy
// Única fonte de verdade para todas as telas do app.
// ============================================================

sealed class WaterError {
    object NetworkError : WaterError()
    object ServerError : WaterError()
    data class Unknown(val message: String?) : WaterError()
}

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Failure(val error: WaterError) : Result<Nothing>()
}

class WaterRepository(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) {

    // Salva localmente SEMPRE; tenta enviar remotamente (best-effort)
    suspend fun addWater(amount: Int): Result<Unit> {
        return try {
            localDataSource.save(amount)
            try {
                remoteDataSource.send(amount)
            } catch (e: retrofit2.HttpException) {
                android.util.Log.w("WaterRepository", "Sync remoto falhou: ${e.code()} ${e.message()}")
            } catch (e: java.io.IOException) {
                android.util.Log.w("WaterRepository", "Sem rede ao sincronizar: ${e.message}")
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("WaterRepository", "Erro ao salvar localmente", e)
            Result.Failure(WaterError.Unknown(e.message))
        }
    }

    // Busca total do dia
    suspend fun getTodayTotal(): Result<Int> {
        return try {
            Result.Success(localDataSource.getTodayTotal())
        } catch (e: Exception) {
            android.util.Log.e("WaterRepository", "Erro ao buscar total", e)
            Result.Failure(WaterError.Unknown(e.message))
        }
    }

    // Desfaz último registro do dia
    suspend fun undoLastEntry(): Result<Unit> {
        return try {
            localDataSource.deleteLastEntry()
            Result.Success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("WaterRepository", "Erro ao desfazer entrada", e)
            Result.Failure(WaterError.Unknown(e.message))
        }
    }

    // Busca histórico agrupado por dia — usado pela HistoryActivity
    suspend fun getHistory(): Result<List<HistoryDay>> {
        return try {
            val items = localDataSource.getHistory()
            Result.Success(items)
        } catch (e: Exception) {
            android.util.Log.e("WaterRepository", "Erro ao buscar histórico", e)
            Result.Failure(WaterError.Unknown(e.message))
        }
    }
}