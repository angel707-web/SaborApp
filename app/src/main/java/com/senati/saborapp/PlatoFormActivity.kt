package com.senati.saborapp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.senati.saborapp.dao.PlatoDao
import com.senati.saborapp.databinding.ActivityPlatoFormBinding
import com.senati.saborapp.model.Plato

class PlatoFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlatoFormBinding
    private lateinit var platoDao: PlatoDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlatoFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        platoDao = PlatoDao(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Configurar Spinner de categorías (CA3)
        val adapterCategorias = ArrayAdapter.createFromResource(
            this,
            R.array.categorias_platos,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spCategoria.adapter = adapterCategorias

        // Guardar plato
        binding.btnGuardarPlato.setOnClickListener {
            guardarPlato()
        }
    }

    private fun guardarPlato() {
        val nombre = binding.etNombre.text.toString().trim()
        val precioStr = binding.etPrecio.text.toString().trim()
        val categoria = binding.spCategoria.selectedItem?.toString() ?: "Fondos"
        val disponible = binding.swDisponible.isChecked

        // Limpiar errores previos
        binding.tilNombre.error = null
        binding.tilPrecio.error = null

        var valido = true

        // CA1: Validar nombre vacío
        if (nombre.isEmpty()) {
            binding.tilNombre.error = getString(R.string.err_nombre_requerido)
            valido = false
        }

        // CA1 & CA2: Validar precio vacío o <= 0
        if (precioStr.isEmpty()) {
            binding.tilPrecio.error = getString(R.string.err_precio_requerido)
            valido = false
        } else {
            val precio = precioStr.toDoubleOrNull()
            if (precio == null || precio <= 0.0) {
                binding.tilPrecio.error = getString(R.string.err_precio_invalido)
                valido = false
            }
        }

        if (!valido) return

        val precioFinal = precioStr.toDouble()
        val plato = Plato(
            nombre = nombre,
            categoria = categoria,
            precio = precioFinal,
            disponible = disponible
        )

        val id = platoDao.insertar(plato)
        if (id > 0) {
            Toast.makeText(this, getString(R.string.msg_plato_guardado), Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error al guardar el plato", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
