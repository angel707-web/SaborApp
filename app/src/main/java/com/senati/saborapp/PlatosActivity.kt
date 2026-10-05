package com.senati.saborapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.senati.saborapp.adapter.PlatoAdapter
import com.senati.saborapp.dao.PlatoDao
import com.senati.saborapp.databinding.ActivityPlatosBinding

class PlatosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlatosBinding
    private lateinit var platoDao: PlatoDao
    private lateinit var adapter: PlatoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlatosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        platoDao = PlatoDao(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Configurar RecyclerView con callback de click (HU-07 CA1)
        adapter = PlatoAdapter { plato ->
            val intent = Intent(this, PlatoFormActivity::class.java).apply {
                putExtra("EXTRA_ID_PLATO", plato.id)
            }
            startActivity(intent)
        }
        binding.rvPlatos.layoutManager = LinearLayoutManager(this)
        binding.rvPlatos.adapter = adapter

        // HU-07 (CA3): Buscador en tiempo real con doAfterTextChanged
        binding.etBuscar.doAfterTextChanged { editable ->
            val filtro = editable?.toString()?.trim().orEmpty()
            cargarPlatos(filtro)
        }

        // FAB para abrir formulario de nuevo plato
        binding.fabNuevoPlato.setOnClickListener {
            startActivity(Intent(this, PlatoFormActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val filtroActual = binding.etBuscar.text?.toString()?.trim().orEmpty()
        cargarPlatos(filtroActual)
    }

    private fun cargarPlatos(filtro: String = "") {
        val lista = platoDao.listar(filtro)
        adapter.submitList(lista)

        if (lista.isEmpty()) {
            binding.layoutVacio.visibility = View.VISIBLE
            binding.rvPlatos.visibility = View.GONE
        } else {
            binding.layoutVacio.visibility = View.GONE
            binding.rvPlatos.visibility = View.VISIBLE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
