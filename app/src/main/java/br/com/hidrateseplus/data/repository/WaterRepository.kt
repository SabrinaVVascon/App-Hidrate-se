package br.com.hidrateseplus.data.repository

import br.com.hidrateseplus.data.local.HistoryDay
import br.com.hidrateseplus.data.local.LocalDataSource
import br.com.hidrateseplus.data.remote.RemoteDataSource

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

    suspend fun addWater(amount: Int): Result<Unit> {
        return try {
            // Sempre salva localmente primeiro (Offline-First)
            localDataSource.save(amount)

            // Tenta sincronizar com Firebase (não quebra se falhar)
            try {
                remoteDataSource.send(amount)
            } catch (e: Exception) {
                android.util.Log.w("WaterRepository", "Sync com Firestore falhou (offline ok)", e)
                // Não propaga o erro - app continua funcionando
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("WaterRepository", "Erro ao salvar localmente", e)
            Result.Failure(WaterError.Unknown(e.message))
        }
    }

    suspend fun getTodayTotal(): Result<Int> {
        return try {
            Result.Success(localDataSource.getTodayTotal())
        } catch (e: Exception) {
            android.util.Log.e("WaterRepository", "Erro ao buscar total", e)
            Result.Failure(WaterError.Unknown(e.message))
        }
    }

    suspend fun undoLastEntry(): Result<Unit> {
        return try {
            localDataSource.deleteLastEntry()
            Result.Success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("WaterRepository", "Erro ao desfazer entrada", e)
            Result.Failure(WaterError.Unknown(e.message))
        }
    }

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