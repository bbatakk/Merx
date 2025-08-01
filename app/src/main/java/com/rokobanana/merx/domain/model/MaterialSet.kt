package com.rokobanana.merx.domain.model

data class MaterialSet(
    val id: String = "",
    val nom: String = "",
    val itemIds: List<String> = emptyList()
)