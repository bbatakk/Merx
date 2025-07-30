package com.rokobanana.merx.domain.model

data class Membre(
    val usuariId: String = "",
    val grupId: String = "",
    val rol: RolMembre = RolMembre.MEMBRE,
    val dataEntrada: Long = System.currentTimeMillis()
)