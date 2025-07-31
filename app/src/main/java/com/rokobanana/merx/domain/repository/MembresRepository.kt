package com.rokobanana.merx.domain.repository

import com.rokobanana.merx.domain.model.Membre

interface MembresRepository {
    suspend fun afegirMembre(membre: Membre)
    suspend fun membresDeGrup(grupId: String): List<Membre>
    suspend fun grupsDeUsuari(usuariId: String): List<Membre>
    suspend fun eliminaMembre(usuariId: String, grupId: String)
}