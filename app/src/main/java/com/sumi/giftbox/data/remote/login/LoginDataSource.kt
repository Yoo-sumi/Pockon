package com.sumi.giftbox.data.remote.login

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class LoginDataSource @Inject constructor(
    private val auth: FirebaseAuth
) {

    fun login(firebaseCredential: AuthCredential, onComplete: (String) -> Unit) {
        auth.signInWithCredential(firebaseCredential).addOnCompleteListener { task ->
            if (task.isSuccessful) onComplete(task.result.user?.uid ?: "")
            else onComplete("")
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun removeAccount(onComplete: (Boolean) -> Unit) {
        auth.currentUser?.delete()?.addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        } ?: onComplete(false)
    }

    fun getCurrentUser() = auth.currentUser
}