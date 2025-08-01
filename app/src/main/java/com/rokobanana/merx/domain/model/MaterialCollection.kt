package com.rokobanana.merx.domain.model

data class MaterialCollection(
    val id: String = "",
    val name: String = "",
    val data: String = "",
    val setIds: List<String> = emptyList(),
    val grupId: String = ""
)