package com.rokobanana.merx.data.repository

import com.rokobanana.merx.data.remote.FirebaseFirestoreDataSource
import com.rokobanana.merx.domain.model.Producte
import com.rokobanana.merx.domain.repository.ProductesRepository
import javax.inject.Inject

class ProductesRepositoryImpl @Inject constructor(
    private val firestoreDataSource: FirebaseFirestoreDataSource
) : ProductesRepository {

    override suspend fun getProductes(grupId: String): List<Producte> =
        firestoreDataSource.getProductes(grupId)

    override suspend fun afegirProducte(grupId: String, producte: Producte) =
        firestoreDataSource.afegirProducte(grupId, producte)

    override suspend fun updateEstoc(grupId: String, producteId: String, talla: String, nouEstoc: Int) =
        firestoreDataSource.updateEstoc(grupId, producteId, talla, nouEstoc)

    override suspend fun updatePreu(grupId: String, producteId: String, nouPreu: Double) =
        firestoreDataSource.updatePreu(grupId, producteId, nouPreu)

    override suspend fun eliminarProducte(grupId: String, producteId: String) =
        firestoreDataSource.eliminarProducte(grupId, producteId)
}