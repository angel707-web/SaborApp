package com.senati.saborapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.senati.saborapp.adapter.MesaAdapter
import com.senati.saborapp.dao.MesaDao
import com.senati.saborapp.databinding.ActivityMesasBinding

class MesasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMesasBinding
    private lateinit var mesaDao: MesaDao
    private lateinit var adapter: MesaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMesasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mesaDao = MesaDao(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Configurar RecyclerView en GridLayoutManager de 3 columnas (HU-06)
        adapter = MesaAdapter()
        binding.rvMesas.layoutManager = GridLayoutManager(this, 3)
        binding.rvMesas.adapter = adapter

        // FAB para abrir formulario de registro de mesa
        binding.fabNuevaMesa.setOnClickListener {
            startActivity(Intent(this, MesaFormActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        cargarMesas()
    }

    private fun cargarMesas() {
        val lista = mesaDao.listar()
        adapter.submitList(lista)

        if (lista.isEmpty()) {
            binding.layoutVacio.visibility = View.VISIBLE
            binding.rvMesas.visibility = View.GONE
        } else {
            binding.layoutVacio.visibility = View.GONE
            binding.rvMesas.visibility = View.VISIBLE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
