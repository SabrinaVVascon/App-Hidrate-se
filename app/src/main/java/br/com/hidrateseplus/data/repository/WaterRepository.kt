package br.com.hidrateseplus.data.repository

import android.util.Log
import br.com.hidrateseplus.data.local.HistoryDay
import br.com.hidrateseplus.data.local.LocalDataSource
import br.com.hidrateseplus.data.remote.RemoteDataSource
import kotlinx.coroutines.withTimeoutOrNull

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
            Log.d(TAG, "Salvando localmente...")
            localDataSource.save(amount)
            Log.d(TAG, "Salvo localmente com sucesso")

            Log.d(TAG, "Tentando sync Firestore...")
            try {
                withTimeoutOrNull(FIRESTORE_SYNC_TIMEOUT_MS) {
                    remoteDataSource.send(amount)
                } ?: Log.w(TAG, "Sync Firestore expirou (offline ok)")
            } catch (e: Exception) {
                Log.w(TAG, "Sync com Firestore falhou (offline ok)", e)
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar localmente", e)
            Result.Failure(WaterError.Unknown(e.message))
        }
    }

    companion object {
        private const val TAG = "WaterRepository"
        private const val FIRESTORE_SYNC_TIMEOUT_MS = 5_000L
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