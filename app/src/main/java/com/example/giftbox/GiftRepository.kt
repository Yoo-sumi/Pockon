package com.example.giftbox

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject


class GiftRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun addGift(uid:String, gift: Gift, onComplete: (Boolean) -> Unit) {
        firestore
            .collection(uid)
            .document("Gift")
            .set(gift)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun getGift(uid:String, onComplete: (Gift?) -> Unit) {
        firestore
            .collection(uid)
            .document("Gift")
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