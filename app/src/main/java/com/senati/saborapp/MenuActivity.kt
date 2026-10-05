package com.senati.saborapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.senati.saborapp.databinding.ActivityMenuBinding
import kotlin.jvm.java

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val usuario = intent.getStringExtra("EXTRA_USUARIO") ?: "admin"
        val rol = intent.getStringExtra("EXTRA_ROL") ?: "ADMIN"

        binding.tvSaludo.text = "Hola, $usuario"
        binding.tvRol.text = if (rol == "ADMIN") "Rol: Administrador" else "Rol: Mozo"
        binding.tvAvatarRole.text = usuario.firstOrNull()?.uppercase() ?: "U"

        // CA4: Ocultar reportes si es Mozo
        if (rol == "MOZO") {
            binding.cardReportes.visibility = View.GONE
        }

        // CA2: Navegación hacia cada pantalla (stubs)
        binding.cardPlatos.setOnClickListener {
            startActivity(Intent(this, PlatosActivity::class.java))
        }
        binding.cardMesas.setOnClickListener {
            startActivity(Intent(this, MesasActivity::class.java))
        }
        binding.cardPedidos.setOnClickListener {
            startActivity(Intent(this, PedidoActivity::class.java))
        }
        binding.cardReportes.setOnClickListener {
            startActivity(Intent(this, ReportesActivity::class.java))
        }

        // CA3: Regresar al login
        binding.btnSalir.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}