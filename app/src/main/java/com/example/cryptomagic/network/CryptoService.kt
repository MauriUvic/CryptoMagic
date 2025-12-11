package com.example.cryptomagic.network

import com.example.cryptomagic.model.Crypto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CryptoService {
    @GET("cryptocurrency-list")
    suspend fun getCryptos(@Query("apikey") apiKey: String): Response<List<Crypto>>
}
