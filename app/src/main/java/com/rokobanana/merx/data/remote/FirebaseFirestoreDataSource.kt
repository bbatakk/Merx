package com.rokobanana.merx.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.rokobanana.merx.domain.model.Producte
import com.rokobanana.merx.domain.model.Usuari
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseFirestoreDataSource @Inject constructor(
    private val db: FirebaseFirestore
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

    suspend fun usernameExists(nomUsuari: String): Boolean {
        val result = db.collection("usuaris")
            .whereEqualTo("nomUsuari", nomUsuari)
            .get()
            .await()
        return !result.isEmpty
    }

    suspend fun setUsuari(uid: String, usuari: Usuari) {
        db.collection("usuaris").document(uid).set(usuari).await()
    }

    suspend fun getEmailByNomUsuari(nomUsuari: String): String? {
        val query = db.collection("usuaris")
            .whereEqualTo("nomUsuari", nomUsuari)
            .get()
            .await()
        return if (!query.isEmpty) query.documents[0].getString("correu") else null
    }

    suspend fun getUsuari(uid: String): Usuari? {
        val doc = db.collection("usuaris").document(uid).get().await()
        return doc.toObject(Usuari::class.java)
    }

    suspend fun updateNomComplet(uid: String, nouNomComplet: String) {
        db.collection("usuaris").document(uid).update("nomComplet", nouNomComplet).await()
    }

    suspend fun desvincularUsuariDeGrup(uid: String, grupId: String) {
        val snapshot = db.collection("membres")
            .whereEqualTo("usuariId", uid)
            .whereEqualTo("grupId", grupId)
            .get().await()
        val batch = db.batch()
        for (doc in snapshot.documents) {
            batch.delete(doc.reference)
        }
        batch.commit().await()
    }

    suspend fun esborrarUsuari(uid: String) {
        val snapshot = db.collection("membres").whereEqualTo("usuariId", uid).get().await()
        val batch = db.batch()
        for (doc in snapshot.documents) {
            batch.delete(doc.reference)
        }
        val usuariRef = db.collection("usuaris").document(uid)
        batch.delete(usuariRef)
        batch.commit().await()
    }
}