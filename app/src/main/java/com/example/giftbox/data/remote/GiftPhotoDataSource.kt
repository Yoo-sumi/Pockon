package com.example.giftbox.data.remote

import android.net.Uri
import com.google.firebase.storage.StorageReference
import javax.inject.Inject


class GiftPhotoDataSource @Inject constructor(
    private val storageRef: StorageReference
) {
    fun uploadData(data: Uri, uid: String, id: String?, onComplete: (Boolean) -> Unit) {
        if (id == null) onComplete(false)
        storageRef.child("${uid}/${id}.jpg")
            .putFile(data)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun downloadData(uid: String, id: String?, onComplete: (Uri?) -> Unit) {
        storageRef.child("${uid}/${id}.jpg")
            .downloadUrl
            .addOnSuccessListener { uri ->
                onComplete(uri)
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    fun removeData(uid: String, id: String, onComplete: (Boolean) -> Unit) {
        storageRef.child("${uid}/${id}.jpg")
            .delete()
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }
}