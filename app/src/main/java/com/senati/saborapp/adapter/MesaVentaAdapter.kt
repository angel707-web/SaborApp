package com.senati.saborapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.senati.saborapp.R
import com.senati.saborapp.databinding.ItemMesaVentaBinding
import com.senati.saborapp.model.MesaVenta

class MesaVentaAdapter(
    private var lista: List<MesaVenta> = emptyList()
) : RecyclerView.Adapter<MesaVentaAdapter.MesaVentaViewHolder>() {

    fun submitList(nuevaLista: List<MesaVenta>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MesaVentaViewHolder {
        val binding = ItemMesaVentaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MesaVentaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MesaVentaViewHolder, position: Int) {
        holder.bind(lista[position])
    }

    override fun getItemCount(): Int = lista.size

    inner class MesaVentaViewHolder(private val binding: ItemMesaVentaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MesaVenta) {
            val context = binding.root.context
            binding.tvNumeroMesa.text = context.getString(R.string.formato_mesa, item.numeroMesa)
            binding.tvCantPedidos.text = context.getString(R.string.formato_pedidos_cant, item.cantidadPedidos)
            binding.tvTotalMesa.text = context.getString(R.string.formato_precio, item.totalVendido)
        }
    }
}
