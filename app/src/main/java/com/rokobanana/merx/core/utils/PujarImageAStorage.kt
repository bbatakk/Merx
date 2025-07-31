package com.rokobanana.merx.core.utils

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

suspend fun pujarImatgeAStorage(uri: Uri): String {
    val storage = FirebaseStorage.getInstance()
    val fileName = UUID.randomUUID().toString()
    val ref = storage.reference.child("imatges_productes/$fileName")

    ref.putFile(uri).await() // puja el fitxer
    return ref.downloadUrl.await().toString() // retorna URL
}