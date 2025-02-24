package com.example.giftbox.data.remote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.giftbox.ui.utils.getBytesFromBitmap
import com.google.firebase.storage.StorageReference
import javax.inject.Inject


class GiftPhotoDataSource @Inject constructor(
    private val storageRef: StorageReference
) {
    fun uploadData(data: Bitmap, uid: String, id: String, onComplete: (Boolean) -> Unit) {
        storageRef.child("${uid}/${id}.jpeg")
            .putBytes(getBytesFromBitmap(data))
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun downloadData(uid: String, id: String?, onComplete: (Bitmap?) -> Unit) {
        storageRef.child("${uid}/${id}.jpeg")
            .getBytes(Long.MAX_VALUE)
            .addOnSuccessListener { bytes ->
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                onComplete(bitmap)
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    fun removeData(uid: String, id: String, onComplete: (Boolean) -> Unit) {
        storageRef.child("${uid}/${id}.jpeg")
            .delete()
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }
}