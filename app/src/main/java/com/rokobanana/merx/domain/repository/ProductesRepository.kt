package com.rokobanana.merx.domain.repository

import com.rokobanana.merx.domain.model.Producte

interface ProductesRepository {
    suspend fun getProductes(grupId: String): List<Producte>
    suspend fun afegirProducte(grupId: String, producte: Producte)
    suspend fun updateEstoc(grupId: String, producteId: String, talla: String, nouEstoc: Int)
    suspend fun updatePreu(grupId: String, producteId: String, nouPreu: Double)
    suspend fun eliminarProducte(grupId: String, producteId: String)
}