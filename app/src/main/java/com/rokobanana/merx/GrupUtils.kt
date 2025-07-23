package com.rokobanana.merx

import android.content.Context
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rokobanana.merx.data.DataStoreHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun verificarOcrearGrup(
    grupId: String,
    context: Context,
    onResult: (success: Boolean, errorMessage: String?) -> Unit
) {
    val db = Firebase.firestore
    val grupRef = db.collection("grups").document(grupId)

    grupRef.get().addOnSuccessListener { doc ->
        if (!doc.exists()) {
            grupRef.set(mapOf("creat" to true))
                .addOnSuccessListener {
                    saveGrupIdAndNotifySuccess(grupId, context, onResult)
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    onResult(false, "Error creant el grup")
                }
        } else {
            saveGrupIdAndNotifySuccess(grupId, context, onResult)
        }
    }.addOnFailureListener { e ->
        e.printStackTrace()
        onResult(false, "Error recuperant el grup")
    }
}

private fun saveGrupIdAndNotifySuccess(
    grupId: String,
    context: Context,
    onResult: (Boolean, String?) -> Unit
) {
    val dataStoreHelper = DataStoreHelper(context)
    CoroutineScope(Dispatchers.IO).launch {
        try {
            dataStoreHelper.saveGrupId(grupId)
            withContext(Dispatchers.Main) {
                onResult(true, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onResult(false, "Error guardant el grup localment")
            }
        }
    }
}

