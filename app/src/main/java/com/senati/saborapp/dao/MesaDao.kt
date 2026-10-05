package com.senati.saborapp.dao

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import com.senati.saborapp.data.DBHelper
import com.senati.saborapp.model.Mesa

class MesaDao(context: Context) {

    private val dbHelper = DBHelper(context)

    /**
     * Inserta una mesa nueva. Captura SQLiteConstraintException si el número ya existe.
     * Retorna el id generado o -1L en caso de error/duplicado.
     */
    fun insertar(mesa: Mesa): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DBHelper.COL_MESA_NUMERO, mesa.numero)
            put(DBHelper.COL_MESA_CAPACIDAD, mesa.capacidad)
            put(DBHelper.COL_MESA_ESTADO, mesa.estado)
        }
        return try {
            db.insertOrThrow(DBHelper.TABLA_MESA, null, values)
        } catch (e: SQLiteConstraintException) {
            -1L
        }
    }

    fun existeNumero(numero: Int): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT ${DBHelper.COL_MESA_ID} FROM ${DBHelper.TABLA_MESA} WHERE ${DBHelper.COL_MESA_NUMERO} = ?",
            arrayOf(numero.toString())
        )
        val existe = cursor.count > 0
        cursor.close()
        return existe
    }

    fun listar(): List<Mesa> {
        val lista = mutableListOf<Mesa>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DBHelper.TABLA_MESA,
            null,
            null,
            null,
            null,
            null,
            "${DBHelper.COL_MESA_NUMERO} ASC"
        )

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_MESA_ID))
            val numero = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_MESA_NUMERO))
            val capacidad = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_MESA_CAPACIDAD))
            val estado = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_MESA_ESTADO))

            lista.add(Mesa(id, numero, capacidad, estado))
        }
        cursor.close()
        return lista
    }
}
