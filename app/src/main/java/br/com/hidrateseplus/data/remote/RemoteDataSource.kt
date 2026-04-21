package br.com.hidrateseplus.data.remote

class RemoteDataSource(
    private val api: ApiService
) {

    suspend fun send(amount: Int) {
        try {
            api.sendWater(mapOf("amount" to amount))
            println("✅ Enviado para API: $amount ml") // log para testar
        } catch (e: Exception) {
            println("❌ Erro ao enviar: ${e.message}")
            throw e // repassa o erro para o Repository tratar
        }
    }
}