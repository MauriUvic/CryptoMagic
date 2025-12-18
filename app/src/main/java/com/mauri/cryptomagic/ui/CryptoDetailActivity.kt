package com.mauri.cryptomagic.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.mauri.cryptomagic.R
import com.mauri.cryptomagic.adapter.OperationAdapter
import com.mauri.cryptomagic.databinding.ActivityCryptoDetailBinding
import com.mauri.cryptomagic.model.HistoricalPrice
import com.mauri.cryptomagic.model.Operation
import java.text.SimpleDateFormat
import java.util.Locale

class CryptoDetailActivity : AppCompatActivity(), OperationAdapter.OnOperationClickListener {

    private lateinit var binding: ActivityCryptoDetailBinding
    private lateinit var viewModel: CryptoDetailViewModel
    private lateinit var operationAdapter: OperationAdapter
    private lateinit var cryptoSymbol: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCryptoDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cryptoSymbol = intent.getStringExtra("CRYPTO_SYMBOL") ?: return
        
        viewModel = ViewModelProvider(this).get(CryptoDetailViewModel::class.java)

        setupRecyclerView()
        setupChart()
        setupListeners()
        observeViewModel()
        viewModel.fetchCryptoDetail(cryptoSymbol, "yogTnXzkSHcqa5jiPm0vHeXgYUdECp8q") 
    }

    private fun setupRecyclerView() {
        operationAdapter = OperationAdapter(emptyList(), this)
        binding.operationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CryptoDetailActivity)
            adapter = operationAdapter
        }
    }

    private fun setupChart() {
        binding.priceChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)
            legend.isEnabled = false
            
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.textColor = ContextCompat.getColor(context, R.color.meta_text_secondary)
            
            axisLeft.textColor = ContextCompat.getColor(context, R.color.meta_text_secondary)
            axisLeft.setDrawGridLines(true)
            axisLeft.gridColor = Color.parseColor("#33FFFFFF") 
            axisRight.isEnabled = false
        }
    }

    private fun setupListeners() {
        binding.fabAddOperation.setOnClickListener {
            val dialog = AddOperationDialogFragment.newInstance(cryptoSymbol)
            dialog.show(supportFragmentManager, "AddOperationDialog")
        }
    }

    private fun observeViewModel() {
        viewModel.cryptoQuote.observe(this) { quote ->
            quote?.let {
                binding.textDetailPrice.text = String.format("\$%.2f", it.price)
                binding.textDetailVolume.text = formatVolume(it.volume)
            }
        }

        viewModel.historicalPrices.observe(this) { history ->
            if (history.isNotEmpty()) {
                updateChart(history)
                val currentPrice = history.lastOrNull()?.price ?: 0.0
                operationAdapter.updateCurrentPrice(currentPrice)
            }
        }

        viewModel.operations.observe(this) { operations ->
            operationAdapter.updateOperations(operations)
        }

        viewModel.profitValue.observe(this) { profit ->
            if (profit != null) {
                binding.textProfitPercentage.text = String.format("\$%+.2f", profit)
                if (profit >= 0) {
                    binding.textProfitPercentage.setTextColor(ContextCompat.getColor(this, R.color.meta_positive))
                } else {
                    binding.textProfitPercentage.setTextColor(ContextCompat.getColor(this, R.color.meta_negative))
                }
            } else {
                binding.textProfitPercentage.text = "--"
            }
        }

        viewModel.spotValue.observe(this) { spotValue ->
            if (spotValue != null) {
                binding.textSpotValue.text = String.format("\$%.2f", spotValue)
            } else {
                binding.textSpotValue.text = "--"
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBarDetail.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { errorMsg ->
            android.util.Log.e("CryptoDetailActivity", "Error: $errorMsg")
        }
    }

    private fun updateChart(history: List<HistoricalPrice>) {
        val entries = history.mapIndexed { index, price ->
            Entry(index.toFloat(), price.price.toFloat())
        }

        val dataSet = LineDataSet(entries, "Price").apply {
            color = ContextCompat.getColor(this@CryptoDetailActivity, R.color.meta_primary)
            setCircleColor(ContextCompat.getColor(this@CryptoDetailActivity, R.color.meta_primary))
            lineWidth = 2f
            circleRadius = 3f
            setDrawCircleHole(false)
            setDrawValues(false) 
            mode = LineDataSet.Mode.CUBIC_BEZIER
            
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(this@CryptoDetailActivity, R.color.meta_primary)
            fillAlpha = 50
        }

        val lineData = LineData(dataSet)
        binding.priceChart.data = lineData
        
        binding.priceChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                if (index >= 0 && index < history.size) {
                    val dateStr = history[index].date
                    return try {
                        dateStr.substring(5) // Returns MM-DD
                    } catch (e: Exception) {
                        dateStr
                    }
                }
                return ""
            }
        }

        binding.priceChart.invalidate() // Refresh chart
    }

    private fun formatVolume(volume: Double): String {
        return when {
            volume >= 1_000_000_000 -> String.format("%.2fB", volume / 1_000_000_000)
            volume >= 1_000_000 -> String.format("%.2fM", volume / 1_000_000)
            volume >= 1_000 -> String.format("%.2fK", volume / 1_000)
            else -> String.format("%.0f", volume)
        }
    }

    override fun onEditClick(operation: Operation) {
        val dialog = AddOperationDialogFragment.newInstance(cryptoSymbol, operation)
        dialog.show(supportFragmentManager, "AddOperationDialog")
    }

    override fun onDeleteClick(operation: Operation) {
        AlertDialog.Builder(this)
            .setTitle("Delete Operation")
            .setMessage("Are you sure you want to delete this operation?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteOperation(operation)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
