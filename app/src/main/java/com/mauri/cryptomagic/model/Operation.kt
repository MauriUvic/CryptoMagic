package com.mauri.cryptomagic.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Operation(
    @DocumentId val id: String = "",
    val symbol: String = "",
    val type: String = "", // "BUY" or "SELL"
    val amount: Double = 0.0,
    val price: Double = 0.0,
    val date: Date = Date(),
    val exchange: String = "",
    val notes: String = ""
) : Parcelable
