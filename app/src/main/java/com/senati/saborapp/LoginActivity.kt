package com.senati.saborapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.senati.saborapp.data.DBHelper
import com.senati.saborapp.data.SessionManager
import com.senati.saborapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // HU-12 (CA1): Verificar sesión persistente antes de mostrar login
        sessionManager = SessionManager(this)
        if (sessionManager.isLoggedIn()) {
            val intent = Intent(this, MenuActivity::class.java).apply {
                putExtra("EXTRA_USUARIO", sessionManager.obtenerUsuario())
                putExtra("EXTRA_ROL", sessionManager.obtenerRol())
            }
            startActivity(intent)
            finish()
            return
        }

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
            val dbHelper = DBHelper(this)
            val usuarioEncontrado = dbHelper.validarUsuario(user, pass)

            if (usuarioEncontrado != null) {
                // HU-12 (CA1): Guardar sesión en SharedPreferences
                sessionManager.guardarSesion(usuarioEncontrado.usuario, usuarioEncontrado.rol)

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