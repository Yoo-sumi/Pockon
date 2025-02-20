package com.example.giftbox.data

import com.example.giftbox.data.remote.LoginDataSource
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

    fun removeAccount(onComplete: (Boolean) -> Unit) {
        loginDataSource.removeAccount {
            onComplete(it)
        }
    }

    fun getCurrentUser() = loginDataSource.getCurrentUser()
}