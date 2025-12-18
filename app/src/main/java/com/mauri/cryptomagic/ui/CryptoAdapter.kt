package com.mauri.cryptomagic.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mauri.cryptomagic.databinding.ItemCryptoBinding
import com.mauri.cryptomagic.model.Crypto

class CryptoAdapter(private var cryptos: List<Crypto> = emptyList(), private val onCryptoClick: (Crypto) -> Unit) :
    RecyclerView.Adapter<CryptoAdapter.CryptoViewHolder>() {

    fun updateList(newCryptos: List<Crypto>) {
        cryptos = newCryptos
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CryptoViewHolder {
        val binding = ItemCryptoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CryptoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CryptoViewHolder, position: Int) {
        holder.bind(cryptos[position], onCryptoClick)
    }

    override fun getItemCount(): Int = cryptos.size

    class CryptoViewHolder(private val binding: ItemCryptoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(crypto: Crypto, onCryptoClick: (Crypto) -> Unit) {
            binding.textName.text = crypto.name
            binding.textSymbol.text = crypto.symbol
            
            // Set the first letter of the symbol as the icon text
            if (crypto.symbol.isNotEmpty()) {
                binding.iconText.text = crypto.symbol.take(1)
            } else {
                binding.iconText.text = "?"
            }

            binding.root.setOnClickListener { onCryptoClick(crypto) }
        }
    }
}
