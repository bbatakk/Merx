package com.rokobanana.merx.data.repository

import com.rokobanana.merx.data.remote.FirebaseFirestoreMembresDataSource
import com.rokobanana.merx.domain.model.Membre
import com.rokobanana.merx.domain.repository.MembresRepository
import javax.inject.Inject

class MembresRepositoryImpl @Inject constructor(
    private val dataSource: FirebaseFirestoreMembresDataSource
) : MembresRepository {
    override suspend fun afegirMembre(membre: Membre) = dataSource.afegirMembre(membre)
    override suspend fun membresDeGrup(grupId: String): List<Membre> = dataSource.membresDeGrup(grupId)
    override suspend fun grupsDeUsuari(usuariId: String): List<Membre> = dataSource.grupsDeUsuari(usuariId)
    override suspend fun eliminaMembre(usuariId: String, grupId: String) = dataSource.eliminaMembre(usuariId, grupId)
}