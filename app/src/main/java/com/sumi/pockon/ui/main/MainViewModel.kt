package com.sumi.pockon.ui.main

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.sumi.pockon.data.repository.PreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
) : ViewModel() {

    private val _isPermRationale = mutableStateOf(preferenceRepository.isPermRationale())
    val isPermRationale: State<Boolean> = _isPermRationale

    fun disableNotification() {
        preferenceRepository.onOffNotiEndDt(false)
    }

    fun saveIsPermRationale() {
        if (!_isPermRationale.value) preferenceRepository.saveIsPermRationale(true)
        _isPermRationale.value = true
    }
}