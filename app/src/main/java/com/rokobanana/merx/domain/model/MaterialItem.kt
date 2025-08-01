package com.rokobanana.merx.domain.model

data class MaterialItem(
    val id: String = "",
    val nom: String = "",
    val marca: String = "",
    val model: String = "",
    val descripcio: String = "",
    val quantitat: Int = 1
)