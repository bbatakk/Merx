package com.rokobanana.merx

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

fun desaGrupPerUsuari(grupId: String, onFinished: (Boolean) -> Unit = {}) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return onFinished(false)
    val db = FirebaseFirestore.getInstance()
    db.collection("usuaris").document(uid).set(mapOf("grupId" to grupId), SetOptions.merge())
        .addOnSuccessListener { onFinished(true) }
        .addOnFailureListener { onFinished(false) }
}