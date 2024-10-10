package com.example.giftbox.data

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
        return callbackFlow {
            document
                .set(gift)
                .addOnCompleteListener {
                    if (it.isSuccessful) trySend(document.id) else trySend(null)
                }
            awaitClose()
        }
    }

    fun loadData(uid:String, onComplete: (Gift?) -> Unit) {
        firestore
            .collection("gift")
            .document()
            .get()
            .addOnCompleteListener { task ->
                var gift: Gift? = null
                if (task.isSuccessful) {
                    gift = task.result.toObject(Gift::class.java)
                    onComplete(gift)
                }
                onComplete(gift)
            }
    }

}