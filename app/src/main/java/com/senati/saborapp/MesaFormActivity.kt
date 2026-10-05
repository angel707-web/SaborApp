package com.senati.saborapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.senati.saborapp.dao.MesaDao
import com.senati.saborapp.databinding.ActivityMesaFormBinding
import com.senati.saborapp.model.Mesa

class MesaFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMesaFormBinding
    private lateinit var mesaDao: MesaDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMesaFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mesaDao = MesaDao(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnGuardarMesa.setOnClickListener {
            guardarMesa()
        }
    }

    private fun guardarMesa() {
        val numeroStr = binding.etNumero.text.toString().trim()
        val capacidadStr = binding.etCapacidad.text.toString().trim()

        binding.tilNumero.error = null
        binding.tilCapacidad.error = null

        var valido = true

        // Validar número requerido
        if (numeroStr.isEmpty()) {
            binding.tilNumero.error = getString(R.string.err_numero_mesa_requerido)
            valido = false
        } else {
            val num = numeroStr.toIntOrNull()
            if (num == null || num <= 0) {
                binding.tilNumero.error = getString(R.string.err_numero_mesa_invalido)
                valido = false
            } else if (mesaDao.existeNumero(num)) {
                // CA1: Número de mesa ya existe
                binding.tilNumero.error = getString(R.string.err_mesa_ya_existe)
                Toast.makeText(this, getString(R.string.err_mesa_ya_existe), Toast.LENGTH_SHORT).show()
                valido = false
            }
        }

        // CA2: Capacidad no está entre 1 y 12
        if (capacidadStr.isEmpty()) {
            binding.tilCapacidad.error = getString(R.string.err_capacidad_invalida)
            Toast.makeText(this, getString(R.string.err_capacidad_invalida), Toast.LENGTH_SHORT).show()
            valido = false
        } else {
            val cap = capacidadStr.toIntOrNull()
            if (cap == null || cap !in 1..12) {
                binding.tilCapacidad.error = getString(R.string.err_capacidad_invalida)
                Toast.makeText(this, getString(R.string.err_capacidad_invalida), Toast.LENGTH_SHORT).show()
                valido = false
            }
        }

        if (!valido) return

        val numero = numeroStr.toInt()
        val capacidad = capacidadStr.toInt()

        // CA3: Estado inicial LIBRE
        val nuevaMesa = Mesa(
            numero = numero,
            capacidad = capacidad,
            estado = "LIBRE"
        )

        val id = mesaDao.insertar(nuevaMesa)
        if (id > 0) {
            Toast.makeText(this, getString(R.string.msg_mesa_guardada), Toast.LENGTH_SHORT).show()
            finish()
        } else {
            binding.tilNumero.error = getString(R.string.err_mesa_ya_existe)
            Toast.makeText(this, getString(R.string.err_mesa_ya_existe), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
