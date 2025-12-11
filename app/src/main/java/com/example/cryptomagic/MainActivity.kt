package com.example.cryptomagic

import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cryptomagic.databinding.ActivityMainBinding
import com.example.cryptomagic.ui.CryptoAdapter
import com.example.cryptomagic.ui.CryptoViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: CryptoViewModel
    private val adapter = CryptoAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(CryptoViewModel::class.java)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        setupSearchView()
        observeViewModel()

        // Usando la API Key que parecia estar mal colocada en el servicio
        viewModel.fetchCryptos("yogTnXzkSHcqa5jiPm0vHeXgYUdECp8q")
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterCryptos(newText ?: "")
                return true
            }
        })
    }

    private fun observeViewModel() {
        // Observamos filteredCryptos en lugar de cryptos para reflejar el filtro
        viewModel.filteredCryptos.observe(this) { cryptos ->
            adapter.updateList(cryptos)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.error.observe(this) { errorMsg ->
            // Opcional: Mostrar error en un Toast o Log si es necesario
             android.util.Log.e("MainActivity", "Error: $errorMsg")
        }
    }
}
