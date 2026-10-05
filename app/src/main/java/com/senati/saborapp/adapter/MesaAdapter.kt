package com.senati.saborapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.senati.saborapp.R
import com.senati.saborapp.databinding.ItemMesaBinding
import com.senati.saborapp.model.Mesa

class MesaAdapter(
    private var lista: List<Mesa> = emptyList(),
    private val onItemClick: ((Mesa) -> Unit)? = null
) : RecyclerView.Adapter<MesaAdapter.MesaViewHolder>() {

    fun submitList(nuevaLista: List<Mesa>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MesaViewHolder {
        val binding = ItemMesaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MesaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MesaViewHolder, position: Int) {
        holder.bind(lista[position])
    }

    override fun getItemCount(): Int = lista.size

    inner class MesaViewHolder(private val binding: ItemMesaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(mesa: Mesa) {
            val context = binding.root.context
            binding.tvNumeroMesa.text = context.getString(R.string.formato_mesa, mesa.numero)
            binding.tvCapacidadMesa.text = "${mesa.capacidad} pers."
            binding.tvEstadoMesa.text = mesa.estado

            // Distinción de colores entre LIBRE y OCUPADA (CA1 HU-08)
            if (mesa.estado == "LIBRE") {
                binding.tvEstadoMesa.setTextColor(ContextCompat.getColor(context, R.color.success))
                binding.tvEstadoMesa.setBackgroundResource(R.drawable.shape_badge_estado)
            } else {
                binding.tvEstadoMesa.setTextColor(ContextCompat.getColor(context, R.color.warning))
                binding.tvEstadoMesa.setBackgroundResource(R.drawable.shape_badge_category)
            }

            binding.root.setOnClickListener {
                onItemClick?.invoke(mesa)
            }
        }
    }
}
