package com.senati.saborapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.senati.saborapp.adapter.DetallePedidoAdapter
import com.senati.saborapp.dao.PedidoDao
import com.senati.saborapp.databinding.ActivityCuentaBinding
import com.senati.saborapp.model.Pedido

class CuentaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCuentaBinding
    private lateinit var pedidoDao: PedidoDao
    private lateinit var adapter: DetallePedidoAdapter

    private var idMesa: Int = -1
    private var numeroMesa: Int = -1
    private var pedidoActivo: Pedido? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCuentaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pedidoDao = PedidoDao(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        idMesa = intent.getIntExtra("EXTRA_ID_MESA", -1)
        numeroMesa = intent.getIntExtra("EXTRA_NUMERO_MESA", -1)

        binding.tvTituloMesa.text = getString(R.string.titulo_cuenta, numeroMesa)

        // Configurar RecyclerView
        adapter = DetallePedidoAdapter()
        binding.rvDetallesCuenta.layoutManager = LinearLayoutManager(this)
        binding.rvDetallesCuenta.adapter = adapter

        cargarCuenta()

        binding.btnCerrarCuenta.setOnClickListener {
            confirmarCerrarCuenta()
        }
    }

    private fun cargarCuenta() {
        if (idMesa <= 0) {
            binding.btnCerrarCuenta.isEnabled = false
            return
        }

        pedidoActivo = pedidoDao.obtenerPedidoActivoPorMesa(idMesa)

        // HU-09 (CA3): Si la mesa está LIBRE o sin pedido, botón deshabilitado
        if (pedidoActivo == null) {
            binding.tvFechaPedido.text = ""
            binding.tvTotalCuenta.text = getString(R.string.formato_precio, 0.0)
            binding.btnCerrarCuenta.isEnabled = false
            binding.tvCuentaVacia.visibility = View.VISIBLE
            binding.rvDetallesCuenta.visibility = View.GONE
            adapter.submitList(emptyList())
        } else {
            // HU-09 (CA1): Mostrar platos, cantidades, subtotales y total
            binding.btnCerrarCuenta.isEnabled = true
            binding.tvCuentaVacia.visibility = View.GONE
            binding.rvDetallesCuenta.visibility = View.VISIBLE

            binding.tvFechaPedido.text = "Fecha: ${pedidoActivo?.fecha}"
            val detalles = pedidoDao.obtenerDetallesPorPedido(pedidoActivo!!.id)
            adapter.submitList(detalles)

            val total = detalles.sumOf { it.subtotal }
            binding.tvTotalCuenta.text = getString(R.string.formato_precio, total)
        }
    }

    /**
     * HU-09 (CA2): Confirmar y cerrar cuenta en una sola transacción
     */
    private fun confirmarCerrarCuenta() {
        val pedido = pedidoActivo ?: return
        val total = pedido.total

        val mensaje = getString(R.string.dialog_mensaje_cerrar_cuenta, numeroMesa, total)

        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_titulo_cerrar_cuenta)
            .setMessage(mensaje)
            .setPositiveButton(R.string.btn_confirmar) { _, _ ->
                val exito = pedidoDao.cerrarCuenta(pedido.id, idMesa)
                if (exito) {
                    Toast.makeText(this, getString(R.string.msg_cuenta_cerrada), Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error al cerrar la cuenta", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
