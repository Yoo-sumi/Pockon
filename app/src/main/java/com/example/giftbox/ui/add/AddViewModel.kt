package com.example.giftbox.ui.add

import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.giftbox.model.Gift
import com.example.giftbox.data.GiftRepository
import com.example.giftbox.R
import com.example.giftbox.ui.utils.bitmapToString
import com.example.giftbox.ui.utils.stringTobitmap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AddViewModel @Inject constructor(
    private val giftRepository: GiftRepository,
    private val sharedPref: SharedPreferences
) : ViewModel() {
    private var uid = sharedPref.getString("uid", "") ?: ""

    private val _isShowDatePicker = mutableStateOf(false)
    val isShowDatePicker: State<Boolean> = _isShowDatePicker

    private val _photo = mutableStateOf<Bitmap?>(null)
    val photo: State<Bitmap?> = _photo
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

    private fun getGift(document: String? = null) {
        if (document == null) return
        viewModelScope.launch {
            giftRepository.getGift(uid).collect { gift->
                gift?.let {
                    _photo.value = stringTobitmap(gift.photo)
                    _name.value = gift.name
                    _brand.value = gift.brand
                    _endDate.value = gift.endDate
                    _memo.value = gift.memo
                }
            }
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

    fun addGift(onAddComplete: (Boolean) -> Unit) {
        val gift = Gift(uid = uid, photo = bitmapToString(_photo.value!!), name = _name.value, brand = _brand.value, endDate = _endDate.value, memo = _memo.value)
        viewModelScope.launch {
            giftRepository.addGift(gift).collect { docId ->
                onAddComplete(docId != null)
            }
        }
    }

    fun setPhoto(photo: Bitmap?) {
        _photo.value = photo
    }

    fun changeDatePickerState() {
        _isShowDatePicker.value = !_isShowDatePicker.value
    }

    fun isValid(): Int? {
        var msg: Int? = null
        if (_photo.value == null) {
            msg = R.string.mgs_no_photo
        } else if (_name.value.isEmpty()) {
            msg = R.string.mgs_no_name
        } else if (_brand.value.isEmpty()) {
            msg = R.string.mgs_no_brand
        } else if (_endDate.value.isEmpty() || _endDate.value.length < 8) {
            msg = R.string.mgs_no_end_date
        }

        return msg
    }

    fun getLabelList(index: Int): Int {
        return when (index) {
            0 -> R.string.txt_name
            1 -> R.string.txt_brand
            2 -> R.string.txt_end_date
            else -> R.string.txt_memo
        }
    }
}