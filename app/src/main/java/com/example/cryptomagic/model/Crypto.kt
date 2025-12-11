package com.example.cryptomagic.model

data class Crypto(
    val symbol: String,
    val name: String,
    val exchange: String,
    val icoDate: String?,
    val circulatingSupply: Double?,
    val totalSupply: Double?
)
