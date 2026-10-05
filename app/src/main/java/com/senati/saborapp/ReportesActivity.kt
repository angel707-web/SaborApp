package com.senati.saborapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.senati.saborapp.adapter.MesaVentaAdapter
import com.senati.saborapp.adapter.TopPlatosAdapter
import com.senati.saborapp.dao.ReporteDao
import com.senati.saborapp.databinding.ActivityReportesBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportesBinding
    private lateinit var reporteDao: ReporteDao
    private lateinit var topPlatosAdapter: TopPlatosAdapter
    private lateinit var mesaVentaAdapter: MesaVentaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        reporteDao = ReporteDao(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Configurar RecyclerViews
        topPlatosAdapter = TopPlatosAdapter()
        binding.rvTopPlatos.layoutManager = LinearLayoutManager(this)
        binding.rvTopPlatos.adapter = topPlatosAdapter

        mesaVentaAdapter = MesaVentaAdapter()
        binding.rvVentasMesa.layoutManager = LinearLayoutManager(this)
        binding.rvVentasMesa.adapter = mesaVentaAdapter

        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.tvFechaHoy.text = "Fecha: ${formatoFecha.format(Date())}"
    }

    override fun onResume() {
        super.onResume()
        cargarReportes()
    }

    private fun cargarReportes() {
        // HU-10 (CA1 & CA3): Ventas de hoy
        val ventaHoy = reporteDao.ventaDelDia()
        if (ventaHoy <= 0.0) {
            binding.tvVentaHoy.text = getString(R.string.sin_ventas_hoy)
            binding.tvVentaHoy.textSize = 24f
        } else {
            binding.tvVentaHoy.text = getString(R.string.formato_precio, ventaHoy)
            binding.tvVentaHoy.textSize = 32f
        }

        // HU-10 (CA2): Top 5 platos más pedidos
        val topPlatos = reporteDao.topPlatos(5)
        topPlatosAdapter.submitList(topPlatos)
        if (topPlatos.isEmpty()) {
            binding.tvTopPlatosVacio.visibility = View.VISIBLE
            binding.rvTopPlatos.visibility = View.GONE
        } else {
            binding.tvTopPlatosVacio.visibility = View.GONE
            binding.rvTopPlatos.visibility = View.VISIBLE
        }

        // Ventas por mesa
        val ventasMesa = reporteDao.ventaPorMesa()
        mesaVentaAdapter.submitList(ventasMesa)
        if (ventasMesa.isEmpty()) {
            binding.tvVentasMesaVacio.visibility = View.VISIBLE
            binding.rvVentasMesa.visibility = View.GONE
        } else {
            binding.tvVentasMesaVacio.visibility = View.GONE
            binding.rvVentasMesa.visibility = View.VISIBLE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
