package com.senati.saborapp.model

data class DetallePedido(
    val id: Int = 0,
    val idPedido: Int,
    val idPlato: Int,
    val cantidad: Int,
    val precioUnit: Double,
    val subtotal: Double,
    val nombrePlato: String = "" // Útil para consultas con JOIN
)
