package com.example.giftbox.ui.login

import android.content.SharedPreferences
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.ViewModel
import com.example.giftbox.data.LoginRepository
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
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

    private val _isPinAuthUse = mutableStateOf(sharedPref.getBoolean("auth_pin", false))
    val isPinAuthUse: State<Boolean> = _isPinAuthUse

    private val _isPinAuth = mutableStateOf(false)
    val isPinAuth: State<Boolean> = _isPinAuth

    private val _pinNumber = mutableStateOf("")
    val pinNumber: State<String> = _pinNumber

    init {
        loginRepository.getCurrentUser()?.let {
            _isLoginState.value = true
            saveMyUid(it.uid)
        }
    }

    fun getCurrentUser(): Boolean {
        loginRepository.getCurrentUser()?.let {
//            _isLoginState.value = true
            saveMyUid(it.uid)
            return true
        } ?: return false
    }


    fun reFreshAuthState() {
        _isPinAuthUse.value = sharedPref.getBoolean("auth_pin", false)
        _pinNumber.value = sharedPref.getString("pin_num", "") ?: ""
        _isPinAuth.value = true
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
                        _isPinAuthUse.value = it
                    }
                }
            }
        }
    }

    fun logout() {
        loginRepository.logout()
        _isLoginState.value = false
        sharedPref.edit().clear().apply()
        _isPinAuthUse.value = sharedPref.getBoolean("auth_pin", false)
        _pinNumber.value = sharedPref.getString("pin_num", "") ?: ""
        _isPinAuth.value = false
    }

    fun removeAccount() {
        loginRepository.removeAccount { result ->
            _isLoginState.value = !result
        }
    }

    private fun saveMyUid(uid: String) {
        sharedPref.edit().putString("uid", uid).apply()
    }
}