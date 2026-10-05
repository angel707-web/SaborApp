package com.senati.saborapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.senati.saborapp.R
import com.senati.saborapp.databinding.ItemPlatoBinding
import com.senati.saborapp.model.Plato

class PlatoAdapter(
    private var lista: List<Plato> = emptyList(),
    private val onItemClick: ((Plato) -> Unit)? = null
) : RecyclerView.Adapter<PlatoAdapter.PlatoViewHolder>() {

    fun submitList(nuevaLista: List<Plato>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlatoViewHolder {
        val binding = ItemPlatoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlatoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlatoViewHolder, position: Int) {
        holder.bind(lista[position])
    }

    override fun getItemCount(): Int = lista.size

    inner class PlatoViewHolder(private val binding: ItemPlatoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(plato: Plato) {
            val context = binding.root.context
            binding.tvNombrePlato.text = plato.nombre
            binding.tvCategoriaPlato.text = plato.categoria
            binding.tvPrecioPlato.text = context.getString(R.string.formato_precio, plato.precio)

            if (plato.disponible) {
                binding.tvDisponiblePlato.text = context.getString(R.string.estado_disponible)
                binding.tvDisponiblePlato.setTextColor(ContextCompat.getColor(context, R.color.success))
            } else {
                binding.tvDisponiblePlato.text = context.getString(R.string.estado_no_disponible)
                binding.tvDisponiblePlato.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            }

            binding.root.setOnClickListener {
                onItemClick?.invoke(plato)
            }
        }
    }
}
