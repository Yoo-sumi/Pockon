package com.example.giftbox.ui.add

import android.content.SharedPreferences
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.giftbox.model.Gift
import com.example.giftbox.data.GiftRepository
import com.example.giftbox.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AddViewModel @Inject constructor(
    private val giftRepository: GiftRepository,
    private val sharedPref: SharedPreferences
) : ViewModel() {
    private var uid = sharedPref.getString("uid", "") ?: ""

    private val _isShowDatePicker = mutableStateOf(false)
    val isShowDatePicker: State<Boolean> = _isShowDatePicker

    private val _photo = mutableStateOf<Uri?>(null)
    val photo: State<Uri?> = _photo
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

    private fun getGift(id: String? = null) {
        if (id == null) return
        giftRepository.getGift(uid, id) { gift ->
            gift?.let {
                _name.value = it.name
                _brand.value = it.brand
                _endDate.value = it.endDt
                _memo.value = it.memo
                _photo.value = Uri.parse(it.photo)
            }
        }
    }

    fun setGift(index: Int, value: String) {
        when (index) {
            0 -> _name.value = value
            1 -> _brand.value = value.trim()
            2 -> _endDate.value = value
            3 -> _memo.value = value
        }
    }

    fun addGift(onAddComplete: (Boolean) -> Unit) {
        val addDate = SimpleDateFormat(
            "yyyyMMddHHmmss",
            Locale.getDefault()
        ).format(Date(System.currentTimeMillis()))

        val gift = Gift(uid = uid, name = _name.value, brand = _brand.value, endDt = _endDate.value, addDt = addDate, memo = _memo.value)
        viewModelScope.launch {
            giftRepository.addGift(gift, _photo.value!!) { isSuccess ->
                onAddComplete(isSuccess)
            }
        }
    }

    fun setPhoto(photo: Uri?) {
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