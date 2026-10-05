package com.senati.saborapp.model

data class PlatoTopVenta(
    val nombre: String,
    val categoria: String,
    val cantidadTotal: Int,
    val subtotalTotal: Double
)

data class MesaVenta(
    val numeroMesa: Int,
    val totalVendido: Double,
    val cantidadPedidos: Int
)
