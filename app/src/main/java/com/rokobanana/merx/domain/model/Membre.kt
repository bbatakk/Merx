package com.rokobanana.merx.domain.model

data class Membre(
    val usuariId: String = "",
    val grupId: String = "",
    val rol: String = "membre", // o "admin"
    val dataEntrada: Long = System.currentTimeMillis()
)