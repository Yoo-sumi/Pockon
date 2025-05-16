package com.sumi.pockon.data.repository

import android.content.Intent
import com.sumi.pockon.data.remote.login.LoginDataSource
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val loginDataSource: LoginDataSource
) {

    suspend fun getTokenForApiHigher() = loginDataSource.getIdToken()

    fun getSignInIntent(onComplete: (Intent) -> Unit) {
        loginDataSource.getSignInIntent {
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

    fun removeAccount(idToken: String?, onComplete: (Boolean) -> Unit) {
        loginDataSource.removeAccount(idToken) {
            onComplete(it)
        }
    }
}