package com.rokobanana.merx.domain.model

data class Producte(
    val id: String = "",
    val nom: String = "",
    val tipus: String = "",
    val imageUrl: String = "",
    val usaTalles: Boolean = false,
    val estocPerTalla: Map<String, Int> = emptyMap(),
    val preu: Double = 0.0
)
