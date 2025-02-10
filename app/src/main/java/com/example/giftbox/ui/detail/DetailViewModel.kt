package com.example.giftbox.ui.detail

import android.content.SharedPreferences
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.giftbox.R
import com.example.giftbox.alarm.MyAlarmManager
import com.example.giftbox.data.GiftRepository
import com.example.giftbox.model.Gift
import com.example.giftbox.ui.utils.getDdayInt
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
    private val giftRepository: GiftRepository,
    private val myAlarmManager: MyAlarmManager,
    private val sharedPref: SharedPreferences
) : ViewModel() {

    private var isNotiEndDt = sharedPref.getBoolean("noti_end_dt", true)

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

    private val _isShowDatePicker = mutableStateOf(false)
    val isShowDatePicker: State<Boolean> = _isShowDatePicker

    private val _isCheckedCash = mutableStateOf(false)
    val isCheckedCash: State<Boolean> = _isCheckedCash

    private val _isEdit = mutableStateOf(false)
    val isEdit: State<Boolean> = _isEdit

    fun getGift(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            giftRepository.getGift(id).collectLatest { gift ->
                setGift(Gift(id = gift.id, uid = gift.uid, photo = gift.photo, name = gift.name, brand = gift.brand, endDt = gift.endDt, addDt = gift.addDt, memo = gift.memo, usedDt = gift.usedDt, cash = gift.cash))
            }
        }
    }

    private fun setGift(gift: Gift) {
        if (!_isEdit.value) this._gift.value = gift
        _name.value = gift.name
        _brand.value = gift.brand
        _cash.value = gift.cash
        _endDate.value = gift.endDt
        _memo.value = gift.memo
        _usedDt.value = gift.usedDt
        _photo.value = Uri.parse(gift.photo)
        _isCheckedCash.value = gift.cash.isNotEmpty()
    }

    fun setGift(index: Int, value: String) {
        when (index) {
            0 -> _name.value = value
            1 -> _brand.value = value.trim()
            2 -> _cash.value = value
            3 -> _endDate.value = value
            4 -> _memo.value = value
        }
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

    fun setIsEdit(flag: Boolean) {
        _isEdit.value = flag
    }

    fun setPhoto(photo: Uri?) {
        _photo.value = photo
    }

    fun updateGift(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val updateGift = if (_isCheckedCash.value) {
                Gift(id = _gift.value.id, uid = _gift.value.uid, photo = _photo.value.toString(), name = _name.value, brand = _brand.value, endDt = _endDate.value, addDt = _gift.value.addDt, memo = _memo.value, usedDt = _gift.value.usedDt, cash = _cash.value)
            } else {
                Gift(id = _gift.value.id, uid = _gift.value.uid, photo = _photo.value.toString(), name = _name.value, brand = _brand.value, endDt = _endDate.value, addDt = _gift.value.addDt, memo = _memo.value, usedDt = _gift.value.usedDt, cash = "")
            }
            giftRepository.updateGift(updateGift).collect { result ->
                // 수정 성공
                if (result) {
                    // 로컬 수정
                    viewModelScope.launch(Dispatchers.IO) {
                        giftRepository.insertGift(updateGift)
                    }
                    myAlarmManager.cancel(updateGift.id)
                    // 알림 등록
                    if (isNotiEndDt && getDdayInt(updateGift.endDt) in 0..1) {
                        myAlarmManager.schedule(updateGift, getDdayInt(updateGift.endDt))
                    }
                    _gift.value = updateGift
                    _isEdit.value = false
                    onComplete(true)
                } else { // 수정 실패
                    onComplete(false)
                }
            }
        }
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

    fun chgCheckedCash() {
        _isCheckedCash.value = !_isCheckedCash.value
    }

    fun changeDatePickerState() {
        _isShowDatePicker.value = !_isShowDatePicker.value
    }

    fun isValid(): Int? {
        var msg: Int? = null
        if (_photo.value == null) {
            msg = R.string.msg_no_photo
        } else if (_name.value.isEmpty()) {
            msg = R.string.msg_no_name
        } else if (_brand.value.isEmpty()) {
            msg = R.string.msg_no_brand
        } else if (_endDate.value.isEmpty() || _endDate.value.length < 8) {
            msg = R.string.msg_no_end_date
        } else if (_isCheckedCash.value && _cash.value.isEmpty()) {
            msg = R.string.msg_no_cash
        }

        return msg
    }
}