package br.com.hidrateseplus.data.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import br.com.hidrateseplus.data.repository.Result      // Importante: usar o seu Result
import br.com.hidrateseplus.data.repository.WaterError   // Importante

class RemoteDataSource {

    private val firestore = FirebaseFirestore.getInstance()
    private val userId: String = "demo_user_123"   // Temporário

    suspend fun send(amount: Int): Result<Unit> {
        return try {
            val entry = hashMapOf<String, Any>(
                "userId" to userId,
                "amount" to amount,
                "date" to LocalDate.now().toString(),
                "timestamp" to FieldValue.serverTimestamp()
            )

            firestore.collection("water_entries")
                .add(entry)
                .await()

            Log.d("RemoteDataSource", "✅ Enviado para Firestore: $amount ml")
            Result.Success(Unit)

        } catch (e: Exception) {
            Log.e("RemoteDataSource", "❌ Erro ao enviar para Firestore", e)
            Result.Failure(WaterError.Unknown(e.message))
        }
    }
}