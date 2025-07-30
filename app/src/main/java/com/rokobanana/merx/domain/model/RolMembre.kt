package com.rokobanana.merx.domain.model

enum class RolMembre(val valor: String) {
    ADMIN("admin"),
    MEMBRE("membre");

    companion object {
        fun fromString(str: String?): RolMembre = RolMembre.entries.find { it.valor == str } ?: MEMBRE
    }
}