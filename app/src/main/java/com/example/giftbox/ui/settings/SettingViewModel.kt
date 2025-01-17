package com.example.giftbox.ui.settings

import android.content.SharedPreferences
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.giftbox.data.BrandSearchRepository
import com.example.giftbox.data.GiftRepository
import com.example.giftbox.data.LoginRepository
import com.example.giftbox.model.Document
import com.example.giftbox.model.Gift
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val sharedPref: SharedPreferences
) : ViewModel() {

    private val _giftList = mutableStateOf<List<Gift>>(listOf())
    val giftList: State<List<Gift>> = _giftList


}