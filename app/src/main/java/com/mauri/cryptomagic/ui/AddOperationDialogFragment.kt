package com.mauri.cryptomagic.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.mauri.cryptomagic.R
import com.mauri.cryptomagic.databinding.DialogAddOperationBinding
import com.mauri.cryptomagic.model.Operation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddOperationDialogFragment : DialogFragment() {

    private var _binding: DialogAddOperationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CryptoDetailViewModel by activityViewModels()

    private lateinit var cryptoSymbol: String
    private var operationToEdit: Operation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_CryptoMagic)
        arguments?.let {
            cryptoSymbol = it.getString("cryptoSymbol") ?: ""
            operationToEdit = it.getParcelable("operation")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddOperationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (operationToEdit != null) {
            populateFields(operationToEdit!!)
        }

        // Set current date
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        binding.editTextDate.setText(sdf.format(Date()))

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calculateTotalValue()
            }
        }

        binding.editAmount.addTextChangedListener(textWatcher)
        binding.editPrice.addTextChangedListener(textWatcher)

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            val operationType = if (binding.radioBuy.isChecked) "BUY" else "SELL"
            val amountStr = binding.editAmount.text.toString()
            val priceStr = binding.editPrice.text.toString()

            if (amountStr.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull()
            val price = priceStr.toDoubleOrNull()

            if (amount == null || price == null) {
                Toast.makeText(context, "Invalid number format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (operationToEdit != null) {
                val updatedOperation = operationToEdit!!.copy(
                    type = operationType,
                    amount = amount,
                    price = price,
                    date = Date() // Or get from a date picker
                )
                viewModel.updateOperation(updatedOperation)
            } else {
                val operation = Operation(
                    symbol = cryptoSymbol,
                    type = operationType,
                    amount = amount,
                    price = price,
                    date = Date()
                )
                viewModel.addOperation(operation)
            }

            dismiss()
        }
    }

    private fun populateFields(operation: Operation) {
        if (operation.type == "BUY") {
            binding.radioBuy.isChecked = true
        } else {
            binding.chipSell.isChecked = true
        }
        binding.editAmount.setText(operation.amount.toString())
        binding.editPrice.setText(operation.price.toString())
        // Populate other fields as needed
    }

    private fun calculateTotalValue() {
        val amountStr = binding.editAmount.text.toString()
        val priceStr = binding.editPrice.text.toString()

        val amount = amountStr.toDoubleOrNull()
        val price = priceStr.toDoubleOrNull()

        if (amount != null && price != null) {
            val totalValue = amount * price
            binding.editTextTotalValue.setText(String.format("%.2f", totalValue))
        } else {
            binding.editTextTotalValue.setText("")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(cryptoSymbol: String, operation: Operation? = null): AddOperationDialogFragment {
            val fragment = AddOperationDialogFragment()
            val args = Bundle()
            args.putString("cryptoSymbol", cryptoSymbol)
            operation?.let { args.putParcelable("operation", it) }
            fragment.arguments = args
            return fragment
        }
    }
}
