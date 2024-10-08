package com.example.giftbox

import android.content.SharedPreferences
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddViewModel @Inject constructor(
    private val giftRepository: GiftRepository,
    private val sharedPref: SharedPreferences
) : ViewModel() {
    private var uid = sharedPref.getString("uid", "") ?: ""

    private val _isShowDatePicker = mutableStateOf(false)
    val isShowDatePicker: State<Boolean> = _isShowDatePicker

    private val _photo = mutableStateOf("")
    val photo: State<String> = _photo
    private val _name = mutableStateOf("")
    val name: State<String> = _name
    private val _brand = mutableStateOf("")
    val brand: State<String> = _brand
    private val _endDate = mutableStateOf("")
    val endDate: State<String> = _endDate
    private val _memo = mutableStateOf("")
    val memo: State<String> = _memo

    init {
        getGift()
    }

    private fun getGift() {
        giftRepository.getGift(uid) { gift ->
            _photo.value = gift?.photo ?: ""
            _name.value = gift?.name ?: ""
            _brand.value = gift?.brand ?: ""
            _endDate.value = gift?.endDate ?: ""
            _memo.value = gift?.memo ?: ""
        }
    }

    fun setGift(index: Int, value: String) {
        when (index) {
            0 -> _name.value = value
            1 -> _brand.value = value
            2 -> _endDate.value = value
            3 -> _memo.value = value
        }
    }

    fun addGift(photo: String, name: String, brand: String, endDate: String, memo: String, onComplete: (Boolean) -> Unit) {
        val gift = Gift(photo = photo, name = name, brand = brand, endDate = endDate, memo = memo)
        giftRepository.addGift(uid, gift) { result ->
            if (result) {
                _name.value = name
                _brand.value = brand
                _endDate.value = endDate
                _memo.value = memo
            }
            onComplete(result)
        }
    }

    fun changeDatePickerState() {
        _isShowDatePicker.value = !_isShowDatePicker.value
    }
}