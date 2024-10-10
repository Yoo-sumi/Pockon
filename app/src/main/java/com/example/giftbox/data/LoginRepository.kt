package com.example.giftbox.data

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val auth: FirebaseAuth
) {
    fun login(firebaseCredential: AuthCredential, onComplete: (Boolean) -> Unit) {
        auth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun removeAccount(onComplete: (Boolean) -> Unit) {
        auth.currentUser?.delete()?.addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }

    fun getCurrentUser() = auth.currentUser
}