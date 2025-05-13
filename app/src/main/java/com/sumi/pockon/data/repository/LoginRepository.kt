package com.sumi.pockon.data.repository

import com.sumi.pockon.data.remote.login.LoginDataSource
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

    fun removeAccount(idToken: String, onComplete: (Boolean) -> Unit) {
        loginDataSource.removeGoogleAccount(idToken) {
            onComplete(it)
        }
    }
}