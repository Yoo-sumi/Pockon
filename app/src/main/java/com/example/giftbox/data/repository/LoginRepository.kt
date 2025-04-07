package com.example.giftbox.data.repository

import com.example.giftbox.data.remote.login.LoginDataSource
import com.google.firebase.auth.AuthCredential
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val loginDataSource: LoginDataSource
) {

    fun login(firebaseCredential: AuthCredential, onComplete: (String) -> Unit) {
        loginDataSource.login(firebaseCredential) { uid ->
            onComplete(uid)
        }
    }

    fun logout() {
        loginDataSource.logout()
    }
}