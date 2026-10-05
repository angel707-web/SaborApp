package com.senati.saborapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.senati.saborapp.adapter.DetallePedidoAdapter
import com.senati.saborapp.adapter.MesaAdapter
import com.senati.saborapp.dao.MesaDao
import com.senati.saborapp.dao.PedidoDao
import com.senati.saborapp.dao.PlatoDao
import com.senati.saborapp.databinding.ActivityPedidoBinding
import com.senati.saborapp.model.Mesa
import com.senati.saborapp.model.Plato

class PedidoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPedidoBinding
    private lateinit var mesaDao: MesaDao
    private lateinit var platoDao: PlatoDao
    private lateinit var pedidoDao: PedidoDao

    private lateinit var mesaAdapter: MesaAdapter
    private lateinit var detalleAdapter: DetallePedidoAdapter

    private var mesaSeleccionada: Mesa? = null
    private var listaPlatosDisponibles: List<Plato> = emptyList()
    private var cantidadActual: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPedidoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mesaDao = MesaDao(this)
        platoDao = PlatoDao(this)
        pedidoDao = PedidoDao(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        configurarRecyclers()
        configurarContadorCantidad()
        configurarBotonAgregar()

        binding.btnVerCuenta.setOnClickListener {
            val mesa = mesaSeleccionada ?: return@setOnClickListener
            val intent = Intent(this, CuentaActivity::class.java).apply {
                putExtra("EXTRA_ID_MESA", mesa.id)
                putExtra("EXTRA_NUMERO_MESA", mesa.numero)
            }
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        cargarMesas()
        cargarPlatosDisponibles()
        actualizarMesaSeleccionada()
    }

    private fun configurarRecyclers() {
        // Grilla horizontal de mesas (HU-08 CA1)
        mesaAdapter = MesaAdapter { mesa ->
            seleccionarMesa(mesa)
        }
        binding.rvMesasSeleccion.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvMesasSeleccion.adapter = mesaAdapter

        // Lista de detalles del pedido
        detalleAdapter = DetallePedidoAdapter()
        binding.rvDetallesPedido.layoutManager = LinearLayoutManager(this)
        binding.rvDetallesPedido.adapter = detalleAdapter
    }

    private fun configurarContadorCantidad() {
        binding.tvCantidad.text = cantidadActual.toString()

        binding.btnMenosCantidad.setOnClickListener {
            if (cantidadActual > 1) {
                cantidadActual--
                binding.tvCantidad.text = cantidadActual.toString()
            }
        }

        binding.btnMasCantidad.setOnClickListener {
            cantidadActual++
            binding.tvCantidad.text = cantidadActual.toString()
        }
    }

    private fun cargarMesas() {
        val mesas = mesaDao.listar()
        mesaAdapter.submitList(mesas)

        // Si no hay mesa seleccionada, seleccionar la primera por defecto si existe
        if (mesaSeleccionada == null && mesas.isNotEmpty()) {
            seleccionarMesa(mesas.first())
        }
    }

    private fun seleccionarMesa(mesa: Mesa) {
        mesaSeleccionada = mesa
        actualizarMesaSeleccionada()
    }

    private fun actualizarMesaSeleccionada() {
        val mesa = mesaSeleccionada
        if (mesa == null) {
            binding.cardMesaActiva.visibility = View.GONE
            binding.cardAgregarPlato.visibility = View.GONE
            return
        }

        binding.cardMesaActiva.visibility = View.VISIBLE
        binding.cardAgregarPlato.visibility = View.VISIBLE

        // Refrescar estado de la mesa desde la base de datos
        val mesasActuales = mesaDao.listar()
        val mesaActualizada = mesasActuales.find { it.id == mesa.id } ?: mesa
        mesaSeleccionada = mesaActualizada

        binding.tvMesaSeleccionada.text =
            getString(R.string.mesa_seleccionada, mesaActualizada.numero, mesaActualizada.estado)
        binding.tvHeaderPedidoActivo.text =
            getString(R.string.pedido_activo_header, "Mesa ${mesaActualizada.numero}")

        // Cargar pedido activo de la mesa
        val pedidoActivo = pedidoDao.obtenerPedidoActivoPorMesa(mesaActualizada.id)

        if (pedidoActivo != null) {
            binding.tvEstadoDetalleMesa.text = "Pedido abierto (${pedidoActivo.fecha})"
            binding.btnVerCuenta.isEnabled = true

            val detalles = pedidoDao.obtenerDetallesPorPedido(pedidoActivo.id)
            detalleAdapter.submitList(detalles)

            if (detalles.isEmpty()) {
                binding.tvPedidoVacio.visibility = View.VISIBLE
                binding.rvDetallesPedido.visibility = View.GONE
            } else {
                binding.tvPedidoVacio.visibility = View.GONE
                binding.rvDetallesPedido.visibility = View.VISIBLE
            }

            binding.tvTotalPedido.text = getString(R.string.total_cuenta, pedidoActivo.total)
        } else {
            binding.tvEstadoDetalleMesa.text = "Mesa libre. Agregue platos para abrir pedido."
            binding.btnVerCuenta.isEnabled = false
            detalleAdapter.submitList(emptyList())
            binding.tvPedidoVacio.visibility = View.VISIBLE
            binding.rvDetallesPedido.visibility = View.GONE
            binding.tvTotalPedido.text = getString(R.string.total_cuenta, 0.0)
        }
    }

    /**
     * HU-07 (CA4): Solo carga platos disponibles para venta
     */
    private fun cargarPlatosDisponibles() {
        listaPlatosDisponibles = platoDao.listarDisponibles()

        val nombres = listaPlatosDisponibles.map {
            "${it.nombre} (${it.categoria}) - S/ ${String.format("%.2f", it.precio)}"
        }

        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            nombres
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spPlatosDisponibles.adapter = spinnerAdapter
    }

    /**
     * HU-08 (CA2, CA3, CA4): Agrega plato con cantidad > 0, crea pedido ABIERTO si es el primero,
     * marca mesa como OCUPADA, recalcula subtotal y total dentro de transacción.
     */
    private fun configurarBotonAgregar() {
        binding.btnAgregarPlato.setOnClickListener {
            val mesa = mesaSeleccionada
            if (mesa == null) {
                Toast.makeText(this, "Seleccione una mesa primero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val pos = binding.spPlatosDisponibles.selectedItemPosition
            if (pos < 0 || pos >= listaPlatosDisponibles.size) {
                Toast.makeText(this, getString(R.string.err_seleccione_plato), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val plato = listaPlatosDisponibles[pos]
            val cantidad = cantidadActual

            if (cantidad <= 0) {
                Toast.makeText(this, getString(R.string.err_cantidad_invalida), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Guardar en transacción (HU-08 CA4)
            val exito = pedidoDao.agregarPlatoAlPedido(
                idMesa = mesa.id,
                idPlato = plato.id,
                cantidad = cantidad,
                precioUnit = plato.precio
            )

            if (exito) {
                Toast.makeText(this, getString(R.string.msg_plato_agregado), Toast.LENGTH_SHORT).show()
                // Reiniciar contador
                cantidadActual = 1
                binding.tvCantidad.text = "1"
                // Recargar estado de mesas y pedido
                cargarMesas()
                actualizarMesaSeleccionada()
            } else {
                Toast.makeText(this, "Error al agregar el plato al pedido", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
