package com.sumi.pockon.ui.add

import android.graphics.Bitmap
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumi.pockon.R
import com.sumi.pockon.alarm.MyAlarmManager
import com.sumi.pockon.data.local.PreferenceRepository
import com.sumi.pockon.data.repository.GiftRepository
import com.sumi.pockon.data.model.Gift
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AddViewModel @Inject constructor(
    private val giftRepository: GiftRepository,
    private val preferenceRepository: PreferenceRepository,
    private val myAlarmManager: MyAlarmManager
) : ViewModel() {

    private val uid = preferenceRepository.getUid()
    private val isNotiEndDt = preferenceRepository.isNotiEndDt()
    private val isGuestMode = preferenceRepository.isGuestMode()

    private val _isShowDatePicker = mutableStateOf(false)
    val isShowDatePicker: State<Boolean> = _isShowDatePicker

    private val _isCheckedCash = mutableStateOf(false)
    val isCheckedCash: State<Boolean> = _isCheckedCash

    private val _isShowIndicator = mutableStateOf(false)
    val isShowIndicator: State<Boolean> = _isShowIndicator

    private val _photo = mutableStateOf<Bitmap?>(null)
    val photo: State<Bitmap?> = _photo
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

    fun setGift(index: Int, value: String) {
        when (index) {
            0 -> _name.value = value
            1 -> _brand.value = value.trim()
            2 -> _cash.value = value
            3 -> _endDate.value = value
            4 -> _memo.value = value
        }
    }

    fun addGift(onAddComplete: (Boolean) -> Unit) {
        _isShowIndicator.value = true
        val addDate = SimpleDateFormat(
            "yyyyMMddHHmmss",
            Locale.getDefault()
        ).format(Date(System.currentTimeMillis()))

        val gift = if (_isCheckedCash.value) {
            Gift(
                uid = uid,
                name = _name.value,
                photo = _photo.value,
                brand = _brand.value,
                endDt = _endDate.value,
                addDt = addDate,
                memo = _memo.value,
                cash = _cash.value,
                isFavorite = false
            )
        } else {
            Gift(
                uid = uid,
                name = _name.value,
                photo = _photo.value,
                brand = _brand.value,
                endDt = _endDate.value,
                addDt = addDate,
                memo = _memo.value,
                isFavorite = false
            )
        }
        viewModelScope.launch {
            giftRepository.addGift(isGuestMode, gift) { id ->
                if (id != null) {
                    // 로컬 수정
                    viewModelScope.launch(Dispatchers.IO) {
                        giftRepository.insertGift(gift.copy(id = id))
                    }
                    val alarmList = preferenceRepository.getAlarmList()
                    // 알림 등록
                    if (isNotiEndDt && alarmList?.contains(gift.id) == false) {
                        alarmList.add(gift.id)
                        preferenceRepository.saveAlarmList(alarmList)
                        myAlarmManager.schedule(gift, preferenceRepository.getNotiEndDtDay(), preferenceRepository.getNotiEndDtTime())
                    }
                }
                _isShowIndicator.value = false
                onAddComplete(id != null)
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

    fun getLabelList(index: Int): Int {
        return when (index) {
            0 -> R.string.txt_name
            1 -> R.string.txt_brand
            2 -> R.string.txt_cash
            3 -> R.string.txt_end_date
            else -> R.string.txt_memo
        }
    }

    fun chgCheckedCash() {
        _isCheckedCash.value = !_isCheckedCash.value
    }
}