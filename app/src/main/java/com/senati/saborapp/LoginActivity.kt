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

            // HU-04 (Sprint 2): Validación real con SQLite parametrizada
            val dbHelper = com.senati.saborapp.data.DBHelper(this)
            val usuarioEncontrado = dbHelper.validarUsuario(user, pass)

            if (usuarioEncontrado != null) {
                val intent = Intent(this, MenuActivity::class.java).apply {
                    putExtra("EXTRA_USUARIO", usuarioEncontrado.usuario)
                    putExtra("EXTRA_ROL", usuarioEncontrado.rol)
                }
                startActivity(intent)
                finish() // Cierra login para no volver atrás
            } else {
                Toast.makeText(this, getString(R.string.err_credenciales_invalidas), Toast.LENGTH_SHORT).show()
            }
        }
    }
}