package com.senati.saborapp.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    companion object {
        private const val PREF_NAME = "saborapp_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USUARIO = "usuario"
        private const val KEY_ROL = "rol"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * HU-12 (CA1): Guarda sesión persistente en SharedPreferences
     */
    fun guardarSesion(usuario: String, rol: String) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USUARIO, usuario)
            putString(KEY_ROL, rol)
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun obtenerUsuario(): String {
        return prefs.getString(KEY_USUARIO, "") ?: ""
    }

    fun obtenerRol(): String {
        return prefs.getString(KEY_ROL, "") ?: ""
    }

    /**
     * HU-12 (CA2): Borra la sesión de SharedPreferences al pulsar Salir
     */
    fun cerrarSesion() {
        prefs.edit().apply {
            clear()
            apply()
        }
    }
}
