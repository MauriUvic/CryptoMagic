package com.example.cryptomagic.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cryptomagic.databinding.ItemCryptoBinding
import com.example.cryptomagic.model.Crypto

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
            binding.root.setOnClickListener { onCryptoClick(crypto) }
        }
    }
}
