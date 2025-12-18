package com.mauri.cryptomagic.model

data class HistoricalPriceResponse(
    val symbol: String,
    val historical: List<HistoricalPrice>
)
