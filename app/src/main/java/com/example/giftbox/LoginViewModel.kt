package com.example.giftbox

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.ViewModel
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val sharedPref: SharedPreferences
) : ViewModel() {
    private val _isLoginState = mutableStateOf(false)
    val isLoginState: State<Boolean> = _isLoginState

    init {
        loginRepository.getCurrentUser()?.let {
            _isLoginState.value = true
            saveMyUid(it.uid)
        }
    }

    fun login(result: GetCredentialResponse) {
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

                    loginRepository.login(firebaseCredential) {
                        _isLoginState.value = it
                    }
                }
            }
        }
    }

    fun logout() {
        loginRepository.logout()
        _isLoginState.value = false
    }

    fun removeAccount() {
        loginRepository.removeAccount { result ->
            _isLoginState.value = !result
        }
    }

    fun saveMyUid(uid: String) {
        sharedPref.edit().putString("uid", uid).apply()
    }
}