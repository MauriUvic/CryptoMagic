package com.mauri.cryptomagic.model

data class CryptoQuote(
    val symbol: String,
    val price: Double,
    val change: Double,
    val volume: Double
)
