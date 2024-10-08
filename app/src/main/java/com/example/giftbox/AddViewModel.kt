package com.example.giftbox

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddViewModel @Inject constructor(
    private val giftRepository: GiftRepository
) : ViewModel() {
    private val _isShowDatePicker = mutableStateOf(false)
    val isShowDatePicker: State<Boolean> = _isShowDatePicker

    fun addGift(photo: String, name: String, brand: String, endDate: String, memo: String) {
        val gift = Gift(photo = photo, name = name, brand = brand, endDate = endDate, memo = memo)
        giftRepository.addGift(gift)
    }

    fun changeDatePickerState() {
        _isShowDatePicker.value = !_isShowDatePicker.value
    }
}