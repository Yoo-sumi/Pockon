package com.example.giftbox.data

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject


class GiftPhotoDataSource @Inject constructor(
    private val firebaseStorage: FirebaseStorage
) {
    fun uploadImage(fileName: String, uri: Uri?): Flow<Boolean> {
        Log.d("업로드", "aaa4a")

        return callbackFlow {
            firebaseStorage
                .getReference("gift")
                .child("${fileName}.png")
                .putFile(uri!!)
                .addOnCompleteListener {
                    Log.d("업로드", it.result.error?.message ?: "aaaa")
                    trySend(it.isSuccessful)
                }
                .addOnFailureListener {
                    Log.d("업로드", it.message ?: "aaaa")
                }
            awaitClose()
        }
    }

    fun loadImage(fileName: String) {
        firebaseStorage
            .getReference("gift")
            .child("${fileName}.png")
            .downloadUrl
            .addOnSuccessListener { uri ->

            }.addOnFailureListener {

            }
    }

    fun deleteImage(fileName: String, uri: Uri) {
        firebaseStorage
            .getReference("gift")
            .child("${fileName}.png")
            .delete()
            .addOnSuccessListener { taskSnapshot ->

            }.addOnFailureListener {

            }
    }
}