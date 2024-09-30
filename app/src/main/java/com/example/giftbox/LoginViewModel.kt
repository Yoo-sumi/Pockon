package com.example.giftbox

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
    private val auth: FirebaseAuth,
) : ViewModel() {
    private val _isLoginState = mutableStateOf(false)
    val isLoginState: State<Boolean> = _isLoginState

    init {
        if (auth.currentUser != null) _isLoginState.value = true
        else _isLoginState.value = false
    }

    fun login(result: GetCredentialResponse) {
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                _isLoginState.value = true
                            } else {
                                _isLoginState.value = false
                            }
                        }
                }
            }
        }
    }

    fun logout() {
        auth.signOut()
        _isLoginState.value = false
    }

    fun removeAccount() {
        auth.currentUser?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _isLoginState.value = false
            } else {
                // 탈퇴 실패
            }
        }

    }
}