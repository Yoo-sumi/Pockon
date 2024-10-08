package com.example.giftbox

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class GiftRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun addGift(gift: Gift) {
        firestore
            .collection("2dlVEI04LbToNRwryAz1aL4HqUW2")
            .document("Gift")
            .set(gift)
            .addOnSuccessListener {

            }
            .addOnFailureListener { e ->

            }
    }
}