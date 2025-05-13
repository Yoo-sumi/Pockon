package com.sumi.pockon.data.remote.login

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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

    fun removeGoogleAccount(idToken: String, onComplete: (Boolean) -> Unit) {
        if (idToken.isEmpty()) {
            onComplete(false)
            return
        }
        val user = auth.currentUser
        if (user != null) {
            val credential = GoogleAuthProvider.getCredential(idToken, null)

            user.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        user.delete()
                            .addOnCompleteListener { deleteTask ->
                                onComplete(deleteTask.isSuccessful)
                            }
                    } else {
                        onComplete(false)
                    }
                }
        } else {
            onComplete(false)
        }
    }

    fun getCurrentUser() = auth.currentUser
}