package com.mauri.cryptomagic.ui.addoperation

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.chip.Chip
import com.mauri.cryptomagic.R
import com.mauri.cryptomagic.databinding.FragmentAddOperationBinding
import com.mauri.cryptomagic.model.Operation
import com.mauri.cryptomagic.ui.CryptoDetailViewModel
import com.mauri.cryptomagic.ui.CryptoViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddOperationFragment : Fragment() {

    private var _binding: FragmentAddOperationBinding? = null
    private val binding get() = _binding!!

    private val cryptoViewModel: CryptoViewModel by activityViewModels()
    private val cryptoDetailViewModel: CryptoDetailViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddOperationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        setupOperationTypeChips()
        setupCoinSelector()
        setupAmountAndPriceListeners()
        setupDatePicker()
        setupSaveButton()
    }

    private fun setupOperationTypeChips() {
        binding.chipGroupOperationType.setOnCheckedChangeListener { group, checkedId ->
            // Lògica per canviar el tipus d'operació
        }
    }

    private fun setupCoinSelector() {
        cryptoViewModel.cryptos.observe(viewLifecycleOwner) { cryptos ->
            val cryptoNames = cryptos.map { it.name }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cryptoNames)
            (binding.layoutCoin.editText as? AutoCompleteTextView)?.setAdapter(adapter)

            (binding.layoutCoin.editText as? AutoCompleteTextView)?.setOnItemClickListener { parent, _, position, _ ->
                val selectedCrypto = cryptos[position]
                binding.layoutAmount.suffixText = selectedCrypto.symbol.replace("USD", "")
            }
        }
    }

    private fun setupAmountAndPriceListeners() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calculateTotalValue()
            }
        }

        binding.editTextAmount.addTextChangedListener(textWatcher)
        binding.editTextPricePerUnit.addTextChangedListener(textWatcher)
    }

    private fun calculateTotalValue() {
        val amount = binding.editTextAmount.text.toString().toDoubleOrNull() ?: 0.0
        val price = binding.editTextPricePerUnit.text.toString().toDoubleOrNull() ?: 0.0
        val total = amount * price
        binding.editTextTotalValue.setText(String.format(Locale.US, "%.2f", total))
    }

    private fun setupDatePicker() {
        binding.editTextDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                binding.editTextDate.setText(sdf.format(calendar.time))
            }
            DatePickerDialog(requireContext(), dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun setupSaveButton() {
        binding.buttonSaveOperation.setOnClickListener {
            saveOperation()
        }
    }

    private fun saveOperation() {
        val operationType = if (binding.chipBuy.isChecked) "BUY" else "SELL"
        val cryptoName = binding.autoCompleteTextViewCoin.text.toString()
        val amount = binding.editTextAmount.text.toString().toDoubleOrNull()
        val price = binding.editTextPricePerUnit.text.toString().toDoubleOrNull()
        val dateStr = binding.editTextDate.text.toString()

        if (cryptoName.isEmpty() || amount == null || price == null || dateStr.isEmpty()) {
            Toast.makeText(context, "Si us plau, omple tots els camps obligatoris", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedCrypto = cryptoViewModel.cryptos.value?.firstOrNull { it.name == cryptoName }
        if (selectedCrypto == null) {
            Toast.makeText(context, "Criptomoneda no vàlida", Toast.LENGTH_SHORT).show()
            return
        }
        
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr)

        val operation = Operation(
            symbol = selectedCrypto.symbol,
            type = operationType,
            amount = amount,
            price = price,
            date = date ?: Date(),
            exchange = binding.editTextExchange.text.toString(),
            notes = binding.editTextNotes.text.toString()
        )

        cryptoDetailViewModel.addOperation(operation)
        Toast.makeText(context, "Operació desada correctament", Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack() // Torna a la pantalla anterior
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
