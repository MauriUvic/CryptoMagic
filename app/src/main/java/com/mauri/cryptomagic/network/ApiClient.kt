package com.mauri.cryptomagic.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://financialmodelingprep.com/stable/"

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val cryptoService: CryptoService = retrofit.create(CryptoService::class.java)
}
