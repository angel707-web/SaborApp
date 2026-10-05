package com.senati.saborapp.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.senati.saborapp.model.Usuario

class DBHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        const val DB_NAME = "saborapp.db"
        const val DB_VERSION = 1

        // Tabla usuario
        const val TABLA_USUARIO = "usuario"
        const val COL_USUARIO_ID = "id"
        const val COL_USUARIO_USER = "usuario"
        const val COL_USUARIO_CLAVE = "clave"
        const val COL_USUARIO_ROL = "rol"

        // Tabla plato
        const val TABLA_PLATO = "plato"
        const val COL_PLATO_ID = "id"
        const val COL_PLATO_NOMBRE = "nombre"
        const val COL_PLATO_CATEGORIA = "categoria"
        const val COL_PLATO_PRECIO = "precio"
        const val COL_PLATO_DISPONIBLE = "disponible"

        // Tabla mesa
        const val TABLA_MESA = "mesa"
        const val COL_MESA_ID = "id"
        const val COL_MESA_NUMERO = "numero"
        const val COL_MESA_CAPACIDAD = "capacidad"
        const val COL_MESA_ESTADO = "estado"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 1. Crear tabla usuario
        val sqlUsuario = """
            CREATE TABLE $TABLA_USUARIO (
                $COL_USUARIO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USUARIO_USER TEXT UNIQUE NOT NULL,
                $COL_USUARIO_CLAVE TEXT NOT NULL,
                $COL_USUARIO_ROL TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(sqlUsuario)

        // Insertar usuarios iniciales
        val cvAdmin = ContentValues().apply {
            put(COL_USUARIO_USER, "admin")
            put(COL_USUARIO_CLAVE, "1234")
            put(COL_USUARIO_ROL, "ADMIN")
        }
        db.insert(TABLA_USUARIO, null, cvAdmin)

        val cvMozo = ContentValues().apply {
            put(COL_USUARIO_USER, "mozo")
            put(COL_USUARIO_CLAVE, "1234")
            put(COL_USUARIO_ROL, "MOZO")
        }
        db.insert(TABLA_USUARIO, null, cvMozo)

        // 2. Crear tabla plato
        val sqlPlato = """
            CREATE TABLE $TABLA_PLATO (
                $COL_PLATO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PLATO_NOMBRE TEXT NOT NULL,
                $COL_PLATO_CATEGORIA TEXT,
                $COL_PLATO_PRECIO REAL CHECK($COL_PLATO_PRECIO > 0),
                $COL_PLATO_DISPONIBLE INTEGER DEFAULT 1
            )
        """.trimIndent()
        db.execSQL(sqlPlato)

        // 3. Crear tabla mesa
        val sqlMesa = """
            CREATE TABLE $TABLA_MESA (
                $COL_MESA_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_MESA_NUMERO INTEGER UNIQUE NOT NULL,
                $COL_MESA_CAPACIDAD INTEGER NOT NULL,
                $COL_MESA_ESTADO TEXT DEFAULT 'LIBRE'
            )
        """.trimIndent()
        db.execSQL(sqlMesa)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Reservado para Sprint 3 (pedidos y detalle_pedido)
    }

    /**
     * CA2: Valida credenciales con consulta parametrizada (rawQuery con ?)
     */
    fun validarUsuario(usuario: String, clave: String): Usuario? {
        val db = readableDatabase
        val query = "SELECT $COL_USUARIO_ID, $COL_USUARIO_USER, $COL_USUARIO_CLAVE, $COL_USUARIO_ROL FROM $TABLA_USUARIO WHERE $COL_USUARIO_USER = ? AND $COL_USUARIO_CLAVE = ?"
        val cursor = db.rawQuery(query, arrayOf(usuario, clave))

        var usuarioEncontrado: Usuario? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USUARIO_ID))
            val user = cursor.getString(cursor.getColumnIndexOrThrow(COL_USUARIO_USER))
            val pass = cursor.getString(cursor.getColumnIndexOrThrow(COL_USUARIO_CLAVE))
            val rol = cursor.getString(cursor.getColumnIndexOrThrow(COL_USUARIO_ROL))
            usuarioEncontrado = Usuario(id, user, pass, rol)
        }
        cursor.close()
        return usuarioEncontrado
    }
}
