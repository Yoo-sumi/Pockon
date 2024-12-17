package com.example.giftbox.ui.detail

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.giftbox.R
import com.example.giftbox.data.GiftRepository
import com.example.giftbox.model.Gift
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val giftRepository: GiftRepository
) : ViewModel() {
    private val _gift = mutableStateOf<Gift>(Gift())
    val gift: State<Gift> = _gift

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
    private val _usedDt = mutableStateOf("")
    val usedDt: State<String> = _usedDt

    private val _isShowBottomSheet = mutableStateOf(false)
    val isShowBottomSheet: State<Boolean> = _isShowBottomSheet

    private val _isShowCancelDialog = mutableStateOf(false)
    val isShowCancelDialog: State<Boolean> = _isShowCancelDialog

    fun setGift(gift: Gift) {
        this._gift.value = gift
        _name.value = gift.name
        _brand.value = gift.brand
        _endDate.value = gift.endDt
        _memo.value = gift.memo
        _usedDt.value = gift.usedDt
        _photo.value = Uri.parse(gift.photo)
    }

    fun getLabelList(index: Int): Int {
        return when (index) {
            0 -> R.string.txt_name
            1 -> R.string.txt_brand
            2 -> R.string.txt_end_date
            else -> R.string.txt_memo
        }
    }

    fun setIsShowBottomSheet(flag: Boolean) {
        _isShowBottomSheet.value = flag
    }

    fun setIsShowCancelDialog(flag: Boolean) {
        _isShowCancelDialog.value = flag
    }

    fun setIsUsed(flag: Boolean) {
        viewModelScope.launch {
            var nowDt = ""
            if (flag) {
                nowDt = SimpleDateFormat(
                    "yyyy.MM.dd",
                    Locale.getDefault()
                ).format(Date(System.currentTimeMillis()))
            }
            _gift.value = _gift.value.copy(usedDt = nowDt)
            giftRepository.updateGift(_gift.value).collect { result ->
                if (result) {
                    _usedDt.value = _gift.value.usedDt
                    _isShowBottomSheet.value = false
                }
            }
        }
    }
}