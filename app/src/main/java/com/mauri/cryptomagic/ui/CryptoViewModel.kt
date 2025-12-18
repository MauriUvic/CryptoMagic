package com.mauri.cryptomagic.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mauri.cryptomagic.model.Crypto
import com.mauri.cryptomagic.network.ApiClient
import kotlinx.coroutines.launch
import java.util.Locale

class CryptoViewModel : ViewModel() {

    private val _cryptos = MutableLiveData<List<Crypto>>()
    val cryptos: LiveData<List<Crypto>> = _cryptos
    
    private val _filteredCryptos = MutableLiveData<List<Crypto>>()
    val filteredCryptos: LiveData<List<Crypto>> = _filteredCryptos

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private var allCryptos = listOf<Crypto>()

    private val allowedSymbols = listOf(
        "BTCUSD", "ETHUSD", "USDTUSD", "BNBUSD", "SOLUSD", "USDCUSD", "XRPUSD", "DOGEUSD", "TONUSD", "TRXUSD",
        "ADAUSD", "AVAXUSD", "SHIBUSD", "LINKUSD", "DOTUSD", "BCHUSD", "DAIUSD", "LEOUSD", "LTCUSD", "NEARUSD",
        "KASUSD", "UNIUSD", "ICPUSD", "FETUSD", "XMRUSD", "PEPEUSD", "SUIUSD", "APTUSD", "XLMUSD", "POLUSD",
        "FDUSD", "ETCUSD", "OKBUSD", "STXUSD", "TAOUSD", "CROUSD", "AAVEUSD", "FILUSD", "IMXUSD", "HBARUSD",
        "MNTUSD", "INJUSD", "ARBUSD", "VETUSD", "OPUSD", "ATOMUSD", "WIFUSD", "FTMUSD", "MKRUSD", "GRTUSD",
        "RUNEUSD", "THETAUSD", "BGBUSD", "ARUSD", "MATICUSD", "HNTUSD", "BONKUSD", "FLOKIUSD", "ALGOUSD", "SEIUSD",
        "PYTHUSD", "JUPUSD", "TIAUSD", "JASMYUSD", "KCSUSD", "BSVUSD", "OMUSD", "LDOUSD", "QNTUSD", "ONDOUSD",
        "BTTUSD", "FLOWUSD", "COREUSD", "PYUSD", "NOTUSD", "BRETTUSD", "USDDUSD", "GTUSD", "EOSUSD", "FLRUSD",
        "BEAMUSD", "CKBUSD", "POPCATUSD", "STRKUSD", "EGLDUSD", "AXSUSD", "NEOUSD", "ORDIUSD", "WLDUSD", "XTZUSD",
        "GALAUSD", "XECUSD", "CFXUSD", "AKTUSD", "SANDUSD", "DYDXUSD", "BNXUSD"
    )

    fun fetchCryptos(apiKey: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = ApiClient.cryptoService.getCryptos(apiKey)
                if (response.isSuccessful) {
                    val list = response.body() ?: emptyList()
                    // Filter list to only include the allowed symbols
                    val filteredAndSortedList = list.filter { it.symbol in allowedSymbols }
                        .sortedBy { allowedSymbols.indexOf(it.symbol) }
                    allCryptos = filteredAndSortedList
                    _cryptos.value = filteredAndSortedList
                    _filteredCryptos.value = filteredAndSortedList
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
    
    fun filterCryptos(query: String) {
        if (query.isEmpty()) {
            _filteredCryptos.value = allCryptos
        } else {
            val lowerCaseQuery = query.lowercase(Locale.ROOT)
            _filteredCryptos.value = allCryptos.filter { crypto ->
                crypto.name.lowercase(Locale.ROOT).contains(lowerCaseQuery) ||
                crypto.symbol.lowercase(Locale.ROOT).contains(lowerCaseQuery)
            }
        }
    }
}
