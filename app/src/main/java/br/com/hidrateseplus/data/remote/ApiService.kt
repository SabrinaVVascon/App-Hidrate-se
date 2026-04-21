package br.com.hidrateseplus.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("water")
    suspend fun sendWater(@Body entry: Map<String, Int>)
}