package com.example.cryptomagic.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptomagic.model.CryptoDetail
import com.example.cryptomagic.network.ApiClient
import kotlinx.coroutines.launch

class CryptoDetailViewModel : ViewModel() {

    private val _cryptoDetail = MutableLiveData<CryptoDetail?>()
    val cryptoDetail: LiveData<CryptoDetail?> = _cryptoDetail

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetchCryptoDetail(symbol: String, apiKey: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = ApiClient.cryptoService.getCryptoDetail(symbol, apiKey)
                if (response.isSuccessful) {
                    _cryptoDetail.value = response.body()?.firstOrNull()
                } else {
                    _error.value = "Error: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Exception: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
