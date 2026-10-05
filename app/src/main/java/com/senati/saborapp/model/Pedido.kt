package com.senati.saborapp.model

data class Pedido(
    val id: Int = 0,
    val idMesa: Int,
    val fecha: String,
    val estado: String = "ABIERTO", // "ABIERTO" o "CERRADO"
    val total: Double = 0.0
)
