package com.mauri.cryptomagic.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mauri.cryptomagic.R
import com.mauri.cryptomagic.databinding.ItemOperationBinding
import com.mauri.cryptomagic.model.Operation
import java.text.SimpleDateFormat
import java.util.Locale

class OperationAdapter(
    private var operations: List<Operation>,
    private val listener: OnOperationClickListener
) : RecyclerView.Adapter<OperationAdapter.OperationViewHolder>() {

    private var currentPrice: Double = 0.0

    interface OnOperationClickListener {
        fun onEditClick(operation: Operation)
        fun onDeleteClick(operation: Operation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OperationViewHolder {
        val binding = ItemOperationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OperationViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: OperationViewHolder, position: Int) {
        holder.bind(operations[position], currentPrice)
    }

    override fun getItemCount() = operations.size

    fun updateOperations(newOperations: List<Operation>) {
        operations = newOperations
        notifyDataSetChanged()
    }

    fun updateCurrentPrice(price: Double) {
        currentPrice = price
        notifyDataSetChanged()
    }

    class OperationViewHolder(
        private val binding: ItemOperationBinding,
        private val listener: OnOperationClickListener
    ) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        fun bind(operation: Operation, currentPrice: Double) {
            binding.textOperationType.text = operation.type
            binding.textOperationDate.text = dateFormat.format(operation.date)
            binding.textOperationAmount.text = String.format("%.4f %s", operation.amount, operation.symbol)
            binding.textOperationPrice.text = String.format("@ $%.2f", operation.price)

            if (operation.type == "BUY") {
                binding.textOperationType.setTextColor(ContextCompat.getColor(binding.root.context, R.color.meta_positive))

                if (currentPrice > 0 && operation.price > 0) {
                    val profit = (currentPrice - operation.price) * operation.amount
                    binding.textOperationProfit.visibility = View.VISIBLE
                    binding.textOperationProfit.text = String.format("%+.2f", profit)
                    if (profit >= 0) {
                        binding.textOperationProfit.setTextColor(ContextCompat.getColor(binding.root.context, R.color.meta_positive))
                    } else {
                        binding.textOperationProfit.setTextColor(ContextCompat.getColor(binding.root.context, R.color.meta_negative))
                    }
                } else {
                    binding.textOperationProfit.visibility = View.GONE
                }

            } else { // SELL
                binding.textOperationType.setTextColor(ContextCompat.getColor(binding.root.context, R.color.meta_negative))
                binding.textOperationProfit.visibility = View.GONE
            }

            fun showActions(show: Boolean) {
                binding.actionsLayout.visibility = if (show) View.VISIBLE else View.GONE
                binding.textOperationProfit.visibility = if (show) View.GONE else View.VISIBLE
                binding.linearLayout2.visibility = if (show) View.GONE else View.VISIBLE
            }

            itemView.setOnLongClickListener {
                showActions(true)
                true
            }

            itemView.setOnClickListener {
                showActions(false)
            }

            binding.btnEdit.setOnClickListener {
                listener.onEditClick(operation)
                showActions(false)
            }

            binding.btnDelete.setOnClickListener {
                listener.onDeleteClick(operation)
                showActions(false)
            }
        }
    }
}
