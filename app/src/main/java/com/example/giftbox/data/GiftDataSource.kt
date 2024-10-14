package com.example.giftbox.data

import android.util.Log
import com.example.giftbox.model.Gift
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject


class GiftDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun uploadData(gift: Gift): Flow<String?> {
        val document = firestore
            .collection("gift")
            .document()
        gift.document = document.id
        return callbackFlow {
            document
                .set(gift)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) trySend(document.id) else trySend(null)
                }
            awaitClose()
        }
    }

    fun loadData(document:String): Flow<Gift?> {
        return callbackFlow {
            firestore
                .collection("gift")
                .document(document)
                .get()
                .addOnCompleteListener { task ->
                    var gift: Gift? = null
                    if (task.isSuccessful) {
                        gift = task.result.toObject(Gift::class.java)
                        trySend(gift)
                    }
                    trySend(gift)
                }
            awaitClose()
        }
    }
}