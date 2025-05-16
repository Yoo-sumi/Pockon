package com.sumi.pockon.ui.login

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumi.pockon.data.repository.LoginRepository
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

    fun getSignInIntent(onComplete: (Intent) -> Unit) {
        loginRepository.getSignInIntent {
            onComplete(it)
        }
    }

    fun loginAsGuest() {
        _isLogin.postValue(true)
        preferenceRepository.saveUid(UUID.randomUUID().toString())
        preferenceRepository.saveIsGuestMode(true)
    }

    fun loginForApiLower(idToken: String?) {
        if (idToken.isNullOrEmpty()) {
            _isLogin.postValue(false)
            _isFail.postValue(true)
            return
        }

        _isLoading.postValue(true)
        loginRepository.login(idToken) {
            if (it.isEmpty()) {
                _isLogin.postValue(false)
                _isFail.postValue(true)
            } else {
                _isLogin.postValue(true)
                preferenceRepository.saveUid(it)
            }
            _isLoading.postValue(false)
        }
    }

    fun loginForApiHigher() {
        _isLoading.postValue(true)
        viewModelScope.launch {
            val idToken = loginRepository.getTokenForApiHigher()
            if (idToken.isNullOrEmpty()) {
                _isLogin.postValue(false)
                _isFail.postValue(true)
                _isLoading.postValue(false)
                return@launch
            }

            loginRepository.login(idToken) {
                if (it.isEmpty()) {
                    _isLogin.postValue(false)
                    _isFail.postValue(true)
                } else {
                    _isLogin.postValue(true)
                    preferenceRepository.saveUid(it)
                }
                _isLoading.postValue(false)
            }
        }
    }
}