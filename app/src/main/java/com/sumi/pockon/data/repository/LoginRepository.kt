package com.sumi.pockon.data.repository

import android.content.Intent
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.sumi.pockon.data.remote.login.LoginDataSource
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val loginDataSource: LoginDataSource
) {

    suspend fun getTokenForApiHigher() = loginDataSource.getIdToken()

    fun getSignInIntent(accountName: String?, onComplete: (Intent) -> Unit) {
        loginDataSource.getSignInIntent(accountName) {
            onComplete(it)
        }
    }

    fun login(idToken: String, onComplete: (String) -> Unit) {
        loginDataSource.login(idToken) { uid ->
            onComplete(uid)
        }
    }

    fun logout() {
        loginDataSource.logout()
    }

    fun removeAccount(idToken: String?, credential: GoogleIdTokenCredential? = null, onComplete: (Boolean) -> Unit) {
        loginDataSource.removeAccount(idToken, credential) {
            onComplete(it)
        }
    }
}