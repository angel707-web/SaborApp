package com.senati.saborapp.dao

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
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

    fun obtener(id: Int): Plato? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DBHelper.TABLA_PLATO,
            null,
            "${DBHelper.COL_PLATO_ID} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        var plato: Plato? = null
        if (cursor.moveToFirst()) {
            val pId = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_PLATO_ID))
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_PLATO_NOMBRE))
            val categoria = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_PLATO_CATEGORIA))
            val precio = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_PLATO_PRECIO))
            val disponible = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_PLATO_DISPONIBLE)) == 1
            plato = Plato(pId, nombre, categoria, precio, disponible)
        }
        cursor.close()
        return plato
    }

    fun actualizar(plato: Plato): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DBHelper.COL_PLATO_NOMBRE, plato.nombre)
            put(DBHelper.COL_PLATO_CATEGORIA, plato.categoria)
            put(DBHelper.COL_PLATO_PRECIO, plato.precio)
            put(DBHelper.COL_PLATO_DISPONIBLE, if (plato.disponible) 1 else 0)
        }
        return db.update(
            DBHelper.TABLA_PLATO,
            values,
            "${DBHelper.COL_PLATO_ID} = ?",
            arrayOf(plato.id.toString())
        )
    }

    /**
     * Elimina un plato. Si tiene pedidos asociados en detalle_pedido, SQLiteConstraintException
     * es propagada para que la Activity muestre el mensaje correspondiente (CA2 HU-07).
     */
    @Throws(SQLiteConstraintException::class)
    fun eliminar(id: Int): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            DBHelper.TABLA_PLATO,
            "${DBHelper.COL_PLATO_ID} = ?",
            arrayOf(id.toString())
        )
    }

    /**
     * Lista platos con filtro opcional (LIKE) por nombre (CA3 HU-07).
     */
    fun listar(filtro: String = ""): List<Plato> {
        val lista = mutableListOf<Plato>()
        val db = dbHelper.readableDatabase

        val selection = if (filtro.isNotBlank()) "${DBHelper.COL_PLATO_NOMBRE} LIKE ?" else null
        val selectionArgs = if (filtro.isNotBlank()) arrayOf("%$filtro%") else null

        val cursor = db.query(
            DBHelper.TABLA_PLATO,
            null,
            selection,
            selectionArgs,
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

    /**
     * Solo platos disponibles para venta al tomar pedidos (CA4 HU-07).
     */
    fun listarDisponibles(): List<Plato> {
        val lista = mutableListOf<Plato>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DBHelper.TABLA_PLATO,
            null,
            "${DBHelper.COL_PLATO_DISPONIBLE} = 1",
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

            lista.add(Plato(id, nombre, categoria, precio, true))
        }
        cursor.close()
        return lista
    }
}
