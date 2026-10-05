package com.senati.saborapp.dao

import android.content.ContentValues
import android.content.Context
import com.senati.saborapp.data.DBHelper
import com.senati.saborapp.model.Plato

class PlatoDao(context: Context) {

    private val dbHelper = DBHelper(context)

    fun insertar(plato: Plato): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DBHelper.COL_PLATO_NOMBRE, plato.nombre)
            put(DBHelper.COL_PLATO_CATEGORIA, plato.categoria)
            put(DBHelper.COL_PLATO_PRECIO, plato.precio)
            put(DBHelper.COL_PLATO_DISPONIBLE, if (plato.disponible) 1 else 0)
        }
        return db.insert(DBHelper.TABLA_PLATO, null, values)
    }

    fun listar(): List<Plato> {
        val lista = mutableListOf<Plato>()
        val db = dbHelper.readableDatabase
        // Ordenado por categoría y nombre (CA3 HU-05)
        val cursor = db.query(
            DBHelper.TABLA_PLATO,
            null,
            null,
            null,
            null,
            null,
            "${DBHelper.COL_PLATO_CATEGORIA} ASC, ${DBHelper.COL_PLATO_NOMBRE} ASC"
        )

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_PLATO_ID))
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_PLATO_NOMBRE))
            val categoria = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_PLATO_CATEGORIA))
            val precio = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_PLATO_PRECIO))
            val disponible = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_PLATO_DISPONIBLE)) == 1

            lista.add(Plato(id, nombre, categoria, precio, disponible))
        }
        cursor.close()
        return lista
    }
}
