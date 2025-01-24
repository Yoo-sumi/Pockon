package com.example.giftbox.ui.login

import android.content.SharedPreferences
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.giftbox.data.LoginRepository
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val sharedPref: SharedPreferences
) : ViewModel() {

    private val _isLogin = MutableLiveData(false)
    val isLogin: LiveData<Boolean> = _isLogin

    private var isPinUse = sharedPref.getBoolean("auth_pin", false)

    init {
        loginRepository.getCurrentUser()?.let {
            _isLogin.value = true
        }
    }

    fun getIsPinUse(): Boolean {
        return isPinUse
    }

    fun login(result: GetCredentialResponse) {
        isPinUse = true
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

                        loginRepository.login(firebaseCredential) {
                            if (it.isEmpty()) {
                                _isLogin.value = false
                            } else {
                                _isLogin.value = true
                                saveMyUid(it)
                            }
                        }
                    } catch (e: GoogleIdTokenParsingException) {
                        TODO()
                    }
                } else {
                    TODO()
                }
            }
            else -> {
                TODO()
            }
        }
    }

    private fun saveMyUid(uid: String) {
        sharedPref.edit().putString("uid", uid).apply()
    }

}