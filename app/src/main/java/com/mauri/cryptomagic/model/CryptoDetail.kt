package com.mauri.cryptomagic.model

data class CryptoDetail(
    val symbol: String,
    val name: String,
    val price: Double,
    val changesPercentage: Double,
    val change: Double,
    val dayLow: Double,
    val dayHigh: Double,
    val yearHigh: Double,
    val yearLow: Double,
    val marketCap: Double,
    val volume: Double,
    val avgVolume: Double,
    val exchange: String,
    val open: Double,
    val previousClose: Double,
    val eps: Double,
    val pe: Double,
    val earningsAnnouncement: String
)
