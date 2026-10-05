package com.senati.saborapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.senati.saborapp.R
import com.senati.saborapp.databinding.ItemTopPlatoBinding
import com.senati.saborapp.model.PlatoTopVenta

class TopPlatosAdapter(
    private var lista: List<PlatoTopVenta> = emptyList()
) : RecyclerView.Adapter<TopPlatosAdapter.TopPlatoViewHolder>() {

    fun submitList(nuevaLista: List<PlatoTopVenta>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopPlatoViewHolder {
        val binding = ItemTopPlatoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TopPlatoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TopPlatoViewHolder, position: Int) {
        holder.bind(lista[position], position + 1)
    }

    override fun getItemCount(): Int = lista.size

    inner class TopPlatoViewHolder(private val binding: ItemTopPlatoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PlatoTopVenta, posicion: Int) {
            val context = binding.root.context
            binding.tvRanking.text = "#$posicion"
            binding.tvNombrePlato.text = item.nombre
            binding.tvCategoriaPlato.text = item.categoria
            binding.tvVentasPlato.text =
                context.getString(R.string.formato_plato_vendido, item.cantidadTotal, item.subtotalTotal)
        }
    }
}
