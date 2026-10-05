package com.senati.saborapp.model

data class Mesa(
    val id: Int = 0,
    val numero: Int,
    val capacidad: Int,
    val estado: String = "LIBRE"
)
