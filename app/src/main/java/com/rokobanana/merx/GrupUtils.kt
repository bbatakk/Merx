package com.rokobanana.merx

import android.content.Context
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

fun verificarOcrearGrup(grupId: String, context: Context, onSuccess: () -> Unit) {
    val db = Firebase.firestore
    val grupRef = db.collection("grups").document(grupId)

    grupRef.get().addOnSuccessListener { doc ->
        if (!doc.exists()) {
            grupRef.set(mapOf("creat" to true))
        }
        // Desa el grupId localment
        val prefs = context.getSharedPreferences("config", Context.MODE_PRIVATE)
        prefs.edit().putString("grupId", grupId).apply()

        onSuccess()
    }.addOnFailureListener {
        it.printStackTrace()
    }
}