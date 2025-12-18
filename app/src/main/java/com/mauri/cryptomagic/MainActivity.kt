package com.mauri.cryptomagic

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.mauri.cryptomagic.databinding.ActivityMainBinding
import com.mauri.cryptomagic.ui.CryptoAdapter
import com.mauri.cryptomagic.ui.CryptoDetailActivity
import com.mauri.cryptomagic.ui.CryptoViewModel
import com.mauri.cryptomagic.ui.LoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: CryptoViewModel
    private val adapter = CryptoAdapter(onCryptoClick = { crypto ->
        val intent = Intent(this, CryptoDetailActivity::class.java)
        intent.putExtra("CRYPTO_SYMBOL", crypto.symbol)
        startActivity(intent)
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(CryptoViewModel::class.java)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        setupSearchView()
        setupButtons()
        observeViewModel()

        viewModel.fetchCryptos("yogTnXzkSHcqa5jiPm0vHeXgYUdECp8q")
    }

    private fun setupButtons() {
        binding.btnLogout.setOnClickListener {
            Firebase.auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnMyQR.setOnClickListener {
            showQRCodeDialog()
        }
    }

    private fun showQRCodeDialog() {
        val user = Firebase.auth.currentUser
        if (user?.email != null) {
            // For security, we might only share public address, but for this demo login, we share email
            // Ideally, we shouldn't share password. The QR login would work if we had a token system.
            // For the sake of the exercise "Scan to Login", I'll put email. 
            // Password sharing in QR is highly insecure, so I'll just encode the email 
            // and let the user type the password manually, OR if it's a "transfer account" feature.
            // But based on your request, I'll encode "email" so at least that part is auto-filled.
            val content = user.email ?: "" 
            val bitmap = generateQRCode(content)
            
            if (bitmap != null) {
                val imageView = ImageView(this)
                imageView.setImageBitmap(bitmap)
                imageView.setPadding(32, 32, 32, 32)
                
                AlertDialog.Builder(this)
                    .setTitle("Your Access Code")
                    .setView(imageView)
                    .setPositiveButton("Close", null)
                    .show()
            }
        }
    }

    private fun generateQRCode(text: String): Bitmap? {
        val width = 500
        val height = 500
        val bitMatrix: BitMatrix
        try {
            bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
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
        viewModel.filteredCryptos.observe(this) { cryptos ->
            adapter.updateList(cryptos)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.error.observe(this) { errorMsg ->
             android.util.Log.e("MainActivity", "Error: $errorMsg")
        }
    }
}
