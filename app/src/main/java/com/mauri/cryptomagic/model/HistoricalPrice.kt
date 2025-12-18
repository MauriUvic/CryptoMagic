package com.mauri.cryptomagic.model

data class HistoricalPrice(
    val symbol: String,
    val date: String,
    val price: Double,
    val volume: Long
)
