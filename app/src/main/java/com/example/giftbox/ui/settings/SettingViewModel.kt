package com.example.giftbox.ui.settings

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.giftbox.data.BrandSearchRepository
import com.example.giftbox.data.GiftRepository
import com.example.giftbox.data.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val giftRepository: GiftRepository,
    private val brandSearchRepository: BrandSearchRepository,
    private val sharedPref: SharedPreferences
) : ViewModel() {

    private var isAuthPin = sharedPref.getBoolean("auth_pin", false)

    fun getIsAuthPin() = isAuthPin

    fun offAuthPin() {
        sharedPref.edit().remove("pin_num").apply()
        sharedPref.edit().remove("auth_pin").apply()
    }

    fun logout() {
        loginRepository.logout()
        sharedPref.edit().clear().apply()
        viewModelScope.launch(Dispatchers.IO) {
            giftRepository.deleteAllGift()
            brandSearchRepository.deleteAllBrands()
        }
    }

    fun removeAccount(onSuccess: (Boolean) -> Unit) {
        loginRepository.removeAccount { result ->
            if (result) {
                sharedPref.edit().clear().apply()
                viewModelScope.launch(Dispatchers.IO) {
                    giftRepository.deleteAllGift()
                    brandSearchRepository.deleteAllBrands()
                }
            }
            onSuccess(result)
        }
    }

}