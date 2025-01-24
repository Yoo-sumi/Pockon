package com.example.giftbox.ui.settings

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val sharedPref: SharedPreferences
) : ViewModel() {

    private var isAuthPin = sharedPref.getBoolean("auth_pin", false)

    fun getIsAuthPin() = isAuthPin

}