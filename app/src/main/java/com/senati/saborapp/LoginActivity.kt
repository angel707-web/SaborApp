package com.senati.saborapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.senati.saborapp.databinding.ActivityLoginBinding
import kotlin.jvm.java

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnIngresar.setOnClickListener {
            val user = binding.etUsuario.text.toString().trim()
            val pass = binding.etClave.text.toString().trim()

            // Limpiar errores previos
            binding.tilUsuario.error = null
            binding.tilClave.error = null

            // CA1: Validar campos vacíos
            var isValid = true
            if (user.isEmpty()) {
                binding.tilUsuario.error = getString(R.string.err_usuario_requerido)
                isValid = false
            }
            if (pass.isEmpty()) {
                binding.tilClave.error = getString(R.string.err_clave_requerida)
                isValid = false
            }

            if (!isValid) return@setOnClickListener

            // CA2 y CA3: Validación mock de credenciales para Sprint 1
            if (user == "admin" && pass == "1234") {
                val intent = Intent(this, MenuActivity::class.java).apply {
                    putExtra("EXTRA_USUARIO", "admin")
                    putExtra("EXTRA_ROL", "ADMIN")
                }
                startActivity(intent)
                finish() // CA2: Cierra login para no volver atrás
            } else if (user == "mozo" && pass == "1234") {
                val intent = Intent(this, MenuActivity::class.java).apply {
                    putExtra("EXTRA_USUARIO", "mozo")
                    putExtra("EXTRA_ROL", "MOZO")
                }
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, getString(R.string.err_credenciales_invalidas), Toast.LENGTH_SHORT).show()
            }
        }
    }
}