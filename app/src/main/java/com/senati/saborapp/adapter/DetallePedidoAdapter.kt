package com.senati.saborapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.senati.saborapp.R
import com.senati.saborapp.databinding.ItemDetallePedidoBinding
import com.senati.saborapp.model.DetallePedido

class DetallePedidoAdapter(
    private var lista: List<DetallePedido> = emptyList()
) : RecyclerView.Adapter<DetallePedidoAdapter.DetalleViewHolder>() {

    fun submitList(nuevaLista: List<DetallePedido>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetalleViewHolder {
        val binding = ItemDetallePedidoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetalleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetalleViewHolder, position: Int) {
        holder.bind(lista[position])
    }

    override fun getItemCount(): Int = lista.size

    inner class DetalleViewHolder(private val binding: ItemDetallePedidoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(detalle: DetallePedido) {
            val context = binding.root.context
            binding.tvNombrePlato.text = detalle.nombrePlato
            binding.tvCantidadYPrecio.text = "${detalle.cantidad} x ${context.getString(R.string.formato_precio, detalle.precioUnit)}"
            binding.tvSubtotal.text = context.getString(R.string.formato_precio, detalle.subtotal)
        }
    }
}
