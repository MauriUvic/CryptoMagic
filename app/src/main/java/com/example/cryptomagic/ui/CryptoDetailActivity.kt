package com.example.cryptomagic.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.cryptomagic.databinding.ActivityCryptoDetailBinding

class CryptoDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCryptoDetailBinding
    private lateinit var viewModel: CryptoDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCryptoDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val cryptoSymbol = intent.getStringExtra("CRYPTO_SYMBOL") ?: return
        
        viewModel = ViewModelProvider(this).get(CryptoDetailViewModel::class.java)

        observeViewModel()
        viewModel.fetchCryptoDetail(cryptoSymbol, "yogTnXzkSHcqa5jiPm0vHeXgYUdECp8q") // Replace with your actual API Key
    }

    private fun observeViewModel() {
        viewModel.cryptoDetail.observe(this) { detail ->
            detail?.let {
                binding.textDetailName.text = it.name
                binding.textDetailSymbol.text = it.symbol
                binding.textDetailPrice.text = String.format("Price: \$%.2f", it.price)
                binding.textDetailChanges.text = String.format("Change: %.2f%%", it.changesPercentage)
                binding.textDetailDayHigh.text = String.format("Day High: \$%.2f", it.dayHigh)
                binding.textDetailDayLow.text = String.format("Day Low: \$%.2f", it.dayLow)
                binding.textDetailYearHigh.text = String.format("Year High: \$%.2f", it.yearHigh)
                binding.textDetailYearLow.text = String.format("Year Low: \$%.2f", it.yearLow)
                binding.textDetailMarketCap.text = String.format("Market Cap: \$%.2f", it.marketCap)
                binding.textDetailVolume.text = String.format("Volume: %.0f", it.volume)
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBarDetail.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { errorMsg ->
            // Optionally show error in a Toast or Log
            android.util.Log.e("CryptoDetailActivity", "Error: $errorMsg")
            binding.textDetailName.text = "Error loading details"
        }
    }
}
