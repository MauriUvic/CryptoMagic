package com.mauri.cryptomagic.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.mauri.cryptomagic.model.CryptoQuote
import com.mauri.cryptomagic.model.HistoricalPrice
import com.mauri.cryptomagic.model.Operation
import com.mauri.cryptomagic.network.ApiClient
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CryptoDetailViewModel : ViewModel() {

    private val _cryptoQuote = MutableLiveData<CryptoQuote?>()
    val cryptoQuote: LiveData<CryptoQuote?> = _cryptoQuote

    private val _historicalPrices = MutableLiveData<List<HistoricalPrice>>()
    val historicalPrices: LiveData<List<HistoricalPrice>> = _historicalPrices

    private val _operations = MutableLiveData<List<Operation>>()
    val operations: LiveData<List<Operation>> = _operations

    private val _profitValue = MutableLiveData<Double?>()
    val profitValue: LiveData<Double?> = _profitValue

    private val _spotValue = MutableLiveData<Double?>()
    val spotValue: LiveData<Double?> = _spotValue

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    fun fetchCryptoDetail(symbol: String, apiKey: String) {
        _isLoading.value = true
        viewModelScope.launch {
            _error.value = ""

            val quoteDeferred = async { fetchQuote(symbol, apiKey) }
            val historyDeferred = async { fetchHistory(symbol, apiKey) }

            quoteDeferred.await()
            historyDeferred.await()

            fetchOperations(symbol)
            
            _isLoading.value = false
        }
    }

    fun addOperation(operation: Operation) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _error.value = "User not logged in"
            return
        }

        viewModelScope.launch {
            try {
                db.collection("users").document(userId).collection("operations").add(operation).await()
            } catch (e: Exception) {
                _error.value = "Failed to add operation: ${e.message}"
            }
        }
    }

    fun updateOperation(operation: Operation) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _error.value = "User not logged in"
            return
        }

        viewModelScope.launch {
            try {
                db.collection("users").document(userId).collection("operations").document(operation.id).set(operation).await()
            } catch (e: Exception) {
                _error.value = "Failed to update operation: ${e.message}"
            }
        }
    }

    fun deleteOperation(operation: Operation) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _error.value = "User not logged in"
            return
        }

        viewModelScope.launch {
            try {
                db.collection("users").document(userId).collection("operations").document(operation.id).delete().await()
            } catch (e: Exception) {
                _error.value = "Failed to delete operation: ${e.message}"
            }
        }
    }

    private suspend fun fetchQuote(symbol: String, apiKey: String) {
        try {
            val response = ApiClient.cryptoService.getCryptoQuote(symbol, apiKey)
            if (response.isSuccessful) {
                _cryptoQuote.postValue(response.body()?.firstOrNull())
            } else {
                _error.postValue((_error.value ?: "") + "\nError fetching quote: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("CryptoDetailViewModel", "Error fetching quote", e)
            _error.postValue((_error.value ?: "") + "\nQuote fetch failed: ${e.message}")
        }
    }

    private suspend fun fetchHistory(symbol: String, apiKey: String) {
        try {
            val response = ApiClient.cryptoService.getHistoricalPrices(symbol, apiKey)
            if (response.isSuccessful) {
                val historyData = response.body() ?: emptyList()
                val sortedHistory = historyData.sortedByDescending { it.date }.take(30).sortedBy { it.date }
                _historicalPrices.postValue(sortedHistory)
                calculateProfit()
            } else {
                _error.postValue((_error.value ?: "") + "\nError fetching history: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("CryptoDetailViewModel", "Error fetching history", e)
            _error.postValue((_error.value ?: "") + "\nHistory fetch failed: ${e.message}")
        }
    }

    private fun fetchOperations(symbol: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _error.value = "User not logged in"
            return
        }

        db.collection("users").document(userId).collection("operations")
            .whereEqualTo("symbol", symbol)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    _error.value = "Error fetching operations: ${e.message}"
                    return@addSnapshotListener
                }

                val fetchedOperations = snapshots?.toObjects<Operation>() ?: emptyList()
                _operations.value = fetchedOperations
                calculateProfit()
            }
    }

    private fun calculateProfit() {
        val currentPrice = _historicalPrices.value?.lastOrNull()?.price
        val userOperations = _operations.value

        if (currentPrice == null || userOperations.isNullOrEmpty()) {
            _profitValue.value = null
            _spotValue.value = null
            return
        }

        var totalCost = 0.0
        var totalAmount = 0.0

        for (op in userOperations) {
            if (op.type == "BUY") {
                totalCost += op.amount * op.price
                totalAmount += op.amount
            } else if (op.type == "SELL") {
                totalCost -= op.amount * op.price 
                totalAmount -= op.amount
            }
        }

        val currentValue = totalAmount * currentPrice
        _spotValue.value = currentValue
        
        val profit = currentValue - totalCost
        _profitValue.value = profit
    }
}
