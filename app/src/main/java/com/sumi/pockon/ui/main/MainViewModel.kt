package com.sumi.pockon.ui.main

import androidx.lifecycle.ViewModel
import com.sumi.pockon.data.local.PreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
) : ViewModel() {

    fun disableNotification() {
        preferenceRepository.onOffNotiEndDt(false)
    }
}