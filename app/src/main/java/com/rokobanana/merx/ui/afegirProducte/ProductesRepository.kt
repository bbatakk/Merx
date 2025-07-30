package com.rokobanana.merx.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.rokobanana.merx.domain.model.Producte
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProductesRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) {
    suspend fun getProductes(grupId: String): List<Producte> {
        val snapshot = db.collection("grups").document(grupId).collection("productes").get().await()
        return snapshot.toObjects(Producte::class.java)
    }

    suspend fun afegirProducte(grupId: String, producte: Producte) {
        val doc = db.collection("grups").document(grupId).collection("productes").document()
        val producteComplet = producte.copy(id = doc.id)
        doc.set(producteComplet).await()
    }

    suspend fun updateEstoc(grupId: String, producteId: String, talla: String, nouEstoc: Int) {
        db.collection("grups").document(grupId)
            .collection("productes").document(producteId)
            .update("estocPerTalla.$talla", nouEstoc)
            .await()
    }

    suspend fun updatePreu(grupId: String, producteId: String, nouPreu: Double) {
        db.collection("grups").document(grupId)
            .collection("productes").document(producteId)
            .update("preu", nouPreu)
            .await()
    }

    suspend fun eliminarProducte(grupId: String, producteId: String) {
        db.collection("grups").document(grupId)
            .collection("productes").document(producteId)
            .delete()
            .await()
    }
}