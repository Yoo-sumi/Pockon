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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val giftRepository: GiftRepository
) : ViewModel() {

    private val _gift = mutableStateOf(Gift())
    val gift: State<Gift> = _gift

    private val _photo = mutableStateOf<Uri?>(null)
    val photo: State<Uri?> = _photo
    private val _name = mutableStateOf("")
    val name: State<String> = _name
    private val _brand = mutableStateOf("")
    val brand: State<String> = _brand
    private val _cash = mutableStateOf("")
    val cash: State<String> = _cash
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

    private val _isShowUseCashDialog = mutableStateOf(false)
    val isShowUseCashDialog: State<Boolean> = _isShowUseCashDialog

    fun getGift(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            giftRepository.getGift(id).collectLatest { gift ->
                setGift(Gift(id = gift.id, uid = gift.uid, photo = gift.photo, name = gift.name, brand = gift.brand, endDt = gift.endDt, addDt = gift.addDt, memo = gift.memo, usedDt = gift.usedDt, cash = gift.cash))
            }
        }
    }

    private fun setGift(gift: Gift) {
        this._gift.value = gift
        _name.value = gift.name
        _brand.value = gift.brand
        _cash.value = gift.cash
        _endDate.value = gift.endDt
        _memo.value = gift.memo
        _usedDt.value = gift.usedDt
        _photo.value = Uri.parse(gift.photo)
    }

    fun getLabelList(index: Int): Int {
        return when (index) {
            0 -> R.string.txt_name
            1 -> R.string.txt_brand
            2 -> R.string.txt_cash
            3 -> R.string.txt_end_date
            else -> R.string.txt_memo
        }
    }

    fun setIsShowBottomSheet(flag: Boolean) {
        _isShowBottomSheet.value = flag
    }

    fun setIsShowCancelDialog(flag: Boolean) {
        _isShowCancelDialog.value = flag
    }

    fun setIsShowUseCashDialog(flag: Boolean) {
        _isShowUseCashDialog.value = flag
    }

    fun setIsUsed(flag: Boolean, cash: Int? = null) {
        viewModelScope.launch {
            var nowDt = ""
            if ((flag && cash == null) || (flag && cash == 0)) {
                nowDt = SimpleDateFormat(
                    "yyyy.MM.dd",
                    Locale.getDefault()
                ).format(Date(System.currentTimeMillis()))
            }
            val gift = if (cash == null) _gift.value.copy(usedDt = nowDt) else _gift.value.copy(usedDt = nowDt, cash = cash.toString())
            giftRepository.updateGift(gift).collect { result ->
                if (result) {
                    // 로컬 수정
                    viewModelScope.launch(Dispatchers.IO) {
                        giftRepository.insertGift(gift)
                    }
                    _isShowBottomSheet.value = false
                    _isShowUseCashDialog.value = false
                } else { // 수정 실패
                    // 네트워크가 불안정합니다. 인터넷 연결을 확인해주세요.
                    TODO()
                }
            }
        }
    }
}