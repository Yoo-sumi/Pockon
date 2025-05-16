package com.sumi.pockon.data.remote.login

import android.content.Context
import android.content.Intent
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.sumi.pockon.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LoginDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) {

    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
        .requestEmail()
        .build()

    private val googleSignInClient = GoogleSignIn.getClient(context, gso)

    private val credentialManager = CredentialManager.create(context)

    private val googleIdOption = GetGoogleIdOption
        .Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
        .setAutoSelectEnabled(false)
        .build()

    private val request = GetCredentialRequest
        .Builder()
        .addCredentialOption(googleIdOption)
        .build()

    fun getSignInIntent(onComplete: (Intent) -> Unit) {
        // 반드시 signOut을 먼저 호출해줘야 다중 계정 선택 가능
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            onComplete(signInIntent)
        }
    }

    suspend fun getIdToken(): String? {
        var result: String? = null
        try {
            val credential = credentialManager.getCredential(
                request = request,
                context = context
            ).credential
            when (credential) {
                is CustomCredential -> {
                    result = if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        try {
                            GoogleIdTokenCredential.createFrom(credential.data).idToken
                        } catch (e: GoogleIdTokenParsingException) {
                            null
                        }
                    } else {
                        null
                    }
                }
                else -> {
                    result = null
                }
            }
            credentialManager.clearCredentialState(request = ClearCredentialStateRequest())
            return result
        } catch (e: GetCredentialException) {
            return result
        }
    }

    fun login(idToken: String, onComplete: (String) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) onComplete(task.result.user?.uid ?: "")
            else onComplete("")
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun removeAccount(idToken: String?, onComplete: (Boolean) -> Unit) {
        if (idToken.isNullOrEmpty()) {
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
}