package com.senati.saborapp.dao

import android.content.ContentValues
import android.content.Context
import com.senati.saborapp.data.DBHelper
import com.senati.saborapp.model.DetallePedido
import com.senati.saborapp.model.Pedido
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PedidoDao(context: Context) {

    private val dbHelper = DBHelper(context)

    /**
     * Obtiene el pedido ABIERTO actual de una mesa, o null si no tiene.
     */
    fun obtenerPedidoActivoPorMesa(idMesa: Int): Pedido? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DBHelper.TABLA_PEDIDO,
            null,
            "${DBHelper.COL_PEDIDO_ID_MESA} = ? AND ${DBHelper.COL_PEDIDO_ESTADO} = 'ABIERTO'",
            arrayOf(idMesa.toString()),
            null,
            null,
            null
        )

        var pedido: Pedido? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_PEDIDO_ID))
            val mesa = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_PEDIDO_ID_MESA))
            val fecha = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_PEDIDO_FECHA))
            val estado = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_PEDIDO_ESTADO))
            val total = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_PEDIDO_TOTAL))
            pedido = Pedido(id, mesa, fecha, estado, total)
        }
        cursor.close()
        return pedido
    }

    /**
     * Obtiene un pedido por su ID.
     */
    fun obtenerPedidoPorId(idPedido: Int): Pedido? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DBHelper.TABLA_PEDIDO,
            null,
            "${DBHelper.COL_PEDIDO_ID} = ?",
            arrayOf(idPedido.toString()),
            null,
            null,
            null
        )

        var pedido: Pedido? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_PEDIDO_ID))
            val mesa = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_PEDIDO_ID_MESA))
            val fecha = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_PEDIDO_FECHA))
            val estado = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_PEDIDO_ESTADO))
            val total = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_PEDIDO_TOTAL))
            pedido = Pedido(id, mesa, fecha, estado, total)
        }
        cursor.close()
        return pedido
    }

    /**
     * Lista los items de un pedido usando JOIN con plato para obtener el nombre del plato.
     */
    fun obtenerDetallesPorPedido(idPedido: Int): List<DetallePedido> {
        val lista = mutableListOf<DetallePedido>()
        val db = dbHelper.readableDatabase

        val sql = """
            SELECT d.${DBHelper.COL_DETALLE_ID},
                   d.${DBHelper.COL_DETALLE_ID_PEDIDO},
                   d.${DBHelper.COL_DETALLE_ID_PLATO},
                   d.${DBHelper.COL_DETALLE_CANTIDAD},
                   d.${DBHelper.COL_DETALLE_PRECIO_UNIT},
                   d.${DBHelper.COL_DETALLE_SUBTOTAL},
                   p.${DBHelper.COL_PLATO_NOMBRE}
            FROM ${DBHelper.TABLA_DETALLE} d
            INNER JOIN ${DBHelper.TABLA_PLATO} p ON d.${DBHelper.COL_DETALLE_ID_PLATO} = p.${DBHelper.COL_PLATO_ID}
            WHERE d.${DBHelper.COL_DETALLE_ID_PEDIDO} = ?
            ORDER BY d.${DBHelper.COL_DETALLE_ID} ASC
        """.trimIndent()

        val cursor = db.rawQuery(sql, arrayOf(idPedido.toString()))
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_DETALLE_ID))
            val pedidoId = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_DETALLE_ID_PEDIDO))
            val platoId = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_DETALLE_ID_PLATO))
            val cantidad = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_DETALLE_CANTIDAD))
            val precioUnit = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_DETALLE_PRECIO_UNIT))
            val subtotal = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_DETALLE_SUBTOTAL))
            val nombrePlato = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_PLATO_NOMBRE))

            lista.add(DetallePedido(id, pedidoId, platoId, cantidad, precioUnit, subtotal, nombrePlato))
        }
        cursor.close()
        return lista
    }

    /**
     * HU-08 (CA2, CA3, CA4): Agrega un plato al pedido de una mesa dentro de una transacción.
     * Si no existe pedido abierto, lo crea y marca la mesa como OCUPADA.
     * Si el plato ya estaba en el pedido, acumula la cantidad y actualiza subtotal.
     * Recalcula el total del pedido.
     */
    fun agregarPlatoAlPedido(idMesa: Int, idPlato: Int, cantidad: Int, precioUnit: Double): Boolean {
        if (cantidad <= 0) return false

        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            // 1. Verificar si la mesa ya tiene pedido ABIERTO
            var pedido = obtenerPedidoActivoPorMesa(idMesa)
            val pedidoId: Long

            if (pedido == null) {
                // Crear nuevo pedido
                val formatoFecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val fechaActual = formatoFecha.format(Date())

                val cvPedido = ContentValues().apply {
                    put(DBHelper.COL_PEDIDO_ID_MESA, idMesa)
                    put(DBHelper.COL_PEDIDO_FECHA, fechaActual)
                    put(DBHelper.COL_PEDIDO_ESTADO, "ABIERTO")
                    put(DBHelper.COL_PEDIDO_TOTAL, 0.0)
                }
                pedidoId = db.insert(DBHelper.TABLA_PEDIDO, null, cvPedido)
                if (pedidoId <= 0) return false

                // Marcar mesa como OCUPADA (CA2)
                val cvMesa = ContentValues().apply {
                    put(DBHelper.COL_MESA_ESTADO, "OCUPADA")
                }
                db.update(DBHelper.TABLA_MESA, cvMesa, "${DBHelper.COL_MESA_ID} = ?", arrayOf(idMesa.toString()))
            } else {
                pedidoId = pedido.id.toLong()
            }

            // 2. Verificar si el plato ya existe en detalle_pedido
            val cursorDetalle = db.query(
                DBHelper.TABLA_DETALLE,
                null,
                "${DBHelper.COL_DETALLE_ID_PEDIDO} = ? AND ${DBHelper.COL_DETALLE_ID_PLATO} = ?",
                arrayOf(pedidoId.toString(), idPlato.toString()),
                null,
                null,
                null
            )

            if (cursorDetalle.moveToFirst()) {
                val detalleId = cursorDetalle.getInt(cursorDetalle.getColumnIndexOrThrow(DBHelper.COL_DETALLE_ID))
                val cantActual = cursorDetalle.getInt(cursorDetalle.getColumnIndexOrThrow(DBHelper.COL_DETALLE_CANTIDAD))
                val nuevaCantidad = cantActual + cantidad
                val nuevoSubtotal = nuevaCantidad * precioUnit

                val cvActualizar = ContentValues().apply {
                    put(DBHelper.COL_DETALLE_CANTIDAD, nuevaCantidad)
                    put(DBHelper.COL_DETALLE_PRECIO_UNIT, precioUnit)
                    put(DBHelper.COL_DETALLE_SUBTOTAL, nuevoSubtotal)
                }
                db.update(
                    DBHelper.TABLA_DETALLE,
                    cvActualizar,
                    "${DBHelper.COL_DETALLE_ID} = ?",
                    arrayOf(detalleId.toString())
                )
            } else {
                val subtotal = cantidad * precioUnit
                val cvDetalle = ContentValues().apply {
                    put(DBHelper.COL_DETALLE_ID_PEDIDO, pedidoId)
                    put(DBHelper.COL_DETALLE_ID_PLATO, idPlato)
                    put(DBHelper.COL_DETALLE_CANTIDAD, cantidad)
                    put(DBHelper.COL_DETALLE_PRECIO_UNIT, precioUnit)
                    put(DBHelper.COL_DETALLE_SUBTOTAL, subtotal)
                }
                db.insert(DBHelper.TABLA_DETALLE, null, cvDetalle)
            }
            cursorDetalle.close()

            // 3. Recalcular total del pedido
            val cursorTotal = db.rawQuery(
                "SELECT SUM(${DBHelper.COL_DETALLE_SUBTOTAL}) FROM ${DBHelper.TABLA_DETALLE} WHERE ${DBHelper.COL_DETALLE_ID_PEDIDO} = ?",
                arrayOf(pedidoId.toString())
            )
            var nuevoTotal = 0.0
            if (cursorTotal.moveToFirst()) {
                nuevoTotal = cursorTotal.getDouble(0)
            }
            cursorTotal.close()

            val cvTotal = ContentValues().apply {
                put(DBHelper.COL_PEDIDO_TOTAL, nuevoTotal)
            }
            db.update(DBHelper.TABLA_PEDIDO, cvTotal, "${DBHelper.COL_PEDIDO_ID} = ?", arrayOf(pedidoId.toString()))

            db.setTransactionSuccessful()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            db.endTransaction()
        }
    }

    /**
     * HU-09 (CA2): Cierra la cuenta del pedido activo de una mesa en una sola transacción.
     * El pedido pasa a CERRADO, se asegura el total final y la mesa vuelve a estado LIBRE.
     */
    fun cerrarCuenta(idPedido: Int, idMesa: Int): Boolean {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            // 1. Obtener total final acumulado
            val cursorTotal = db.rawQuery(
                "SELECT SUM(${DBHelper.COL_DETALLE_SUBTOTAL}) FROM ${DBHelper.TABLA_DETALLE} WHERE ${DBHelper.COL_DETALLE_ID_PEDIDO} = ?",
                arrayOf(idPedido.toString())
            )
            var totalFinal = 0.0
            if (cursorTotal.moveToFirst()) {
                totalFinal = cursorTotal.getDouble(0)
            }
            cursorTotal.close()

            // 2. Actualizar pedido a CERRADO y guardar total
            val cvPedido = ContentValues().apply {
                put(DBHelper.COL_PEDIDO_ESTADO, "CERRADO")
                put(DBHelper.COL_PEDIDO_TOTAL, totalFinal)
            }
            val pedidosActualizados = db.update(
                DBHelper.TABLA_PEDIDO,
                cvPedido,
                "${DBHelper.COL_PEDIDO_ID} = ?",
                arrayOf(idPedido.toString())
            )
            if (pedidosActualizados <= 0) return false

            // 3. Liberar la mesa -> LIBRE
            val cvMesa = ContentValues().apply {
                put(DBHelper.COL_MESA_ESTADO, "LIBRE")
            }
            db.update(
                DBHelper.TABLA_MESA,
                cvMesa,
                "${DBHelper.COL_MESA_ID} = ?",
                arrayOf(idMesa.toString())
            )

            db.setTransactionSuccessful()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            db.endTransaction()
        }
    }
}
