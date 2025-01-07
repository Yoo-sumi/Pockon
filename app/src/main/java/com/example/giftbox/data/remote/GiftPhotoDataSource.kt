package com.example.giftbox.data.remote

import android.net.Uri
import com.google.firebase.storage.StorageReference
import javax.inject.Inject


class GiftPhotoDataSource @Inject constructor(
    private val storageRef: StorageReference
) {
    fun uploadData(data: Uri, uid: String, document: String?, onComplete: (Boolean) -> Unit) {
        if (document == null) onComplete(false)
        storageRef.child("${uid}/${document}.jpg")
            .putFile(data)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun downloadData(uid: String, document: String?, onComplete: (Uri?) -> Unit) {
        storageRef.child("${uid}/${document}.jpg")
            .downloadUrl
            .addOnSuccessListener { uri ->
                onComplete(uri)
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }
}