package com.example.giftbox.data.remote

import com.example.giftbox.model.Gift
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject


class GiftDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun uploadData(gift: Gift, onComplete: (String?) -> Unit) {
        val document = firestore
            .collection("gift")
            .document()
        gift.id = document.id
        document
            .set(gift)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onComplete(document.id) else onComplete(null)
            }
    }

    fun loadData(document:String, onComplete: (Gift?) -> Unit) {
        firestore
            .collection("gift")
            .document(document)
            .get()
            .addOnCompleteListener { task ->
                var gift: Gift? = null
                if (task.isSuccessful) {
                    gift = task.result.toObject(Gift::class.java)
                }
                onComplete(gift)
            }
    }

    fun loadAllData(uid: String, onComplete: (List<Gift>) -> Unit) {
        firestore
            .collection("gift")
            .whereEqualTo("uid", uid)
            .whereEqualTo("usedDt", "")
            .get()
            .addOnCompleteListener { task ->
                val giftList: MutableList<Gift>  = mutableListOf()
                if (task.isSuccessful) {
                    for (document in task.result) {
                        giftList.add(document.toObject(Gift::class.java))
                    }
                }
                onComplete(giftList)
            }
    }

    fun updateData(gift: Gift): Flow<Boolean> {
        return callbackFlow {
            firestore
                .collection("gift")
                .document(gift.id)
                .set(gift)
                .addOnCompleteListener { task ->
                    trySend(task.isSuccessful)
                }
            awaitClose()
        }
    }

    fun deleteData(document: String): Flow<Boolean> {
        return callbackFlow {
            firestore
                .collection("gift")
                .document(document)
                .delete()
                .addOnCompleteListener { task ->
                    trySend(task.isSuccessful)
                }
            awaitClose()
        }
    }
}