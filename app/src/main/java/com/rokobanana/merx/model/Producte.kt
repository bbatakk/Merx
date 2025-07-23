package com.rokobanana.merx.model

data class Producte(
    val id: String = "",
    val nom: String = "",
    val tipus: String = "",
    val imageUrl: String = "",
    val usaTalles: Boolean = false,
    val estocPerTalla: Map<String, Int> = if (usaTalles) {
        mapOf (
            "XS" to 0,
            "S" to 0,
            "M" to 0,
            "L" to 0,
            "XL" to 0,
            "XXL" to 0
        )
    } else {
        mapOf("general" to 0)
    }
)
