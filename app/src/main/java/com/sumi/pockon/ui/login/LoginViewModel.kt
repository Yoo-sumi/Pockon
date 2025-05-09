package com.sumi.pockon.ui.login

import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumi.pockon.data.repository.LoginRepository
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.GoogleAuthProvider
import com.sumi.pockon.data.repository.PreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    private val _isLogin = MutableLiveData(false)
    val isLogin: LiveData<Boolean> = _isLogin

    private val _isFail = MutableLiveData(false)
    val isFail: LiveData<Boolean> = _isFail

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isFirstLogin = MutableLiveData(preferenceRepository.getUid().isEmpty())
    val isFirstLogin: LiveData<Boolean> = _isFirstLogin

    private var isPinUse = preferenceRepository.isAuthPin()

    init {
        if (preferenceRepository.getUid().isNotEmpty()) {
            _isLogin.postValue(true)
        }
    }

    fun getIsPinUse(): Boolean {
        return isPinUse
    }

    fun loginAsGuest() {
        isPinUse = true
        _isLogin.postValue(true)
        preferenceRepository.saveUid(UUID.randomUUID().toString())
        preferenceRepository.saveIsGuestMode(true)
    }

    fun login(credentialManager: CredentialManager, result: GetCredentialResponse) {
        isPinUse = true
        // 구글 사용자 정보 가져오는 부분은 Repository 단에 androidx import 하고 싶지 않아서 여기서 처리
        when (val credential = result.credential) {
            is CustomCredential -> {
                _isLoading.postValue(true)
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

                        loginRepository.login(firebaseCredential) {
                            _isLoading.postValue(false)
                            if (it.isEmpty()) {
                                _isLogin.postValue(false)
                                _isFail.postValue(true)
                            } else {
                                _isLogin.postValue(true)
                                preferenceRepository.saveUid(it)
                            }
                        }
                    } catch (e: GoogleIdTokenParsingException) {
                        _isLoading.postValue(false)
                        _isFail.postValue(true)
                    }
                } else {
                    _isLoading.postValue(false)
                    _isFail.postValue(true)
                }
            }

            else -> {
                _isLoading.postValue(false)
                _isFail.postValue(true)
            }
        }
        viewModelScope.launch {
            // 구글 사용자 인증 정보 유지X
            credentialManager.clearCredentialState(request = ClearCredentialStateRequest())
        }
    }
}