package com.mauri.cryptomagic.network

import com.mauri.cryptomagic.model.Crypto
import com.mauri.cryptomagic.model.CryptoDetail
import com.mauri.cryptomagic.model.CryptoQuote
import com.mauri.cryptomagic.model.HistoricalPrice
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CryptoService {
    @GET("cryptocurrency-list")
    suspend fun getCryptos(@Query("apikey") apiKey: String): Response<List<Crypto>>

    @GET("cryptocurrency-list") 
    suspend fun getCryptoDetail(@Query("symbol") symbol: String, @Query("apikey") apiKey: String): Response<List<CryptoDetail>>

    @GET("quote-short")
    suspend fun getCryptoQuote(@Query("symbol") symbol: String, @Query("apikey") apiKey: String): Response<List<CryptoQuote>>

    @GET("historical-price-eod/light")
    suspend fun getHistoricalPrices(@Query("symbol") symbol: String, @Query("apikey") apiKey: String): Response<List<HistoricalPrice>>
}
