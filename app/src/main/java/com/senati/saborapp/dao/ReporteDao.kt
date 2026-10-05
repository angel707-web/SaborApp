package com.senati.saborapp.dao

import android.content.Context
import com.senati.saborapp.data.DBHelper
import com.senati.saborapp.model.MesaVenta
import com.senati.saborapp.model.PlatoTopVenta
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReporteDao(context: Context) {

    private val dbHelper = DBHelper(context)

    /**
     * HU-10 (CA1, CA3): Obtiene la suma de ventas del día actual para pedidos CERRADOS.
     * Retorna 0.0 si no hay ventas.
     */
    fun ventaDelDia(): Double {
        val db = dbHelper.readableDatabase
        val hoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        val sql = """
            SELECT SUM(${DBHelper.COL_PEDIDO_TOTAL})
            FROM ${DBHelper.TABLA_PEDIDO}
            WHERE ${DBHelper.COL_PEDIDO_ESTADO} = 'CERRADO'
              AND (${DBHelper.COL_PEDIDO_FECHA} LIKE ? OR date(${DBHelper.COL_PEDIDO_FECHA}) = date('now', 'localtime'))
        """.trimIndent()

        val cursor = db.rawQuery(sql, arrayOf("$hoy%"))
        var total = 0.0
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0)
        }
        cursor.close()
        return total
    }

    /**
     * HU-10 (CA2): Top 5 platos más pedidos agrupados por cantidad vendida (SUM + GROUP BY)
     */
    fun topPlatos(limite: Int = 5): List<PlatoTopVenta> {
        val lista = mutableListOf<PlatoTopVenta>()
        val db = dbHelper.readableDatabase

        val sql = """
            SELECT p.${DBHelper.COL_PLATO_NOMBRE},
                   p.${DBHelper.COL_PLATO_CATEGORIA},
                   SUM(d.${DBHelper.COL_DETALLE_CANTIDAD}) AS total_cant,
                   SUM(d.${DBHelper.COL_DETALLE_SUBTOTAL}) AS total_monto
            FROM ${DBHelper.TABLA_DETALLE} d
            INNER JOIN ${DBHelper.TABLA_PLATO} p ON d.${DBHelper.COL_DETALLE_ID_PLATO} = p.${DBHelper.COL_PLATO_ID}
            INNER JOIN ${DBHelper.TABLA_PEDIDO} pe ON d.${DBHelper.COL_DETALLE_ID_PEDIDO} = pe.${DBHelper.COL_PEDIDO_ID}
            WHERE pe.${DBHelper.COL_PEDIDO_ESTADO} = 'CERRADO'
            GROUP BY p.${DBHelper.COL_PLATO_ID}, p.${DBHelper.COL_PLATO_NOMBRE}, p.${DBHelper.COL_PLATO_CATEGORIA}
            ORDER BY total_cant DESC, total_monto DESC
            LIMIT ?
        """.trimIndent()

        val cursor = db.rawQuery(sql, arrayOf(limite.toString()))
        while (cursor.moveToNext()) {
            val nombre = cursor.getString(0)
            val categoria = cursor.getString(1)
            val cantidad = cursor.getInt(2)
            val monto = cursor.getDouble(3)
            lista.add(PlatoTopVenta(nombre, categoria, cantidad, monto))
        }
        cursor.close()
        return lista
    }

    /**
     * HU-10: Venta acumulada por mesa para pedidos cerrados
     */
    fun ventaPorMesa(): List<MesaVenta> {
        val lista = mutableListOf<MesaVenta>()
        val db = dbHelper.readableDatabase

        val sql = """
            SELECT m.${DBHelper.COL_MESA_NUMERO},
                   SUM(pe.${DBHelper.COL_PEDIDO_TOTAL}) AS total_mesa,
                   COUNT(pe.${DBHelper.COL_PEDIDO_ID}) AS cant_pedidos
            FROM ${DBHelper.TABLA_PEDIDO} pe
            INNER JOIN ${DBHelper.TABLA_MESA} m ON pe.${DBHelper.COL_PEDIDO_ID_MESA} = m.${DBHelper.COL_MESA_ID}
            WHERE pe.${DBHelper.COL_PEDIDO_ESTADO} = 'CERRADO'
            GROUP BY m.${DBHelper.COL_MESA_ID}, m.${DBHelper.COL_MESA_NUMERO}
            ORDER BY total_mesa DESC
        """.trimIndent()

        val cursor = db.rawQuery(sql, null)
        while (cursor.moveToNext()) {
            val numero = cursor.getInt(0)
            val total = cursor.getDouble(1)
            val cant = cursor.getInt(2)
            lista.add(MesaVenta(numero, total, cant))
        }
        cursor.close()
        return lista
    }
}
