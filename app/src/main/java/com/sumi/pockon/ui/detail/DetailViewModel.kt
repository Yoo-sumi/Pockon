package com.sumi.pockon.ui.detail

import android.graphics.Bitmap
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumi.pockon.R
import com.sumi.pockon.data.repository.PreferenceRepository
import com.sumi.pockon.data.repository.GiftRepository
import com.sumi.pockon.data.model.Gift
import com.sumi.pockon.data.repository.AlarmRepository
import com.sumi.pockon.util.NetworkMonitor
import com.sumi.pockon.util.loadImageFromPath
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
    private val alarmRepository: AlarmRepository,
    private val preferenceRepository: PreferenceRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val isGuestMode = preferenceRepository.isGuestMode()

    private val _gift = mutableStateOf(Gift())
    val gift: State<Gift> = _gift

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
    private val _usedDt = mutableStateOf("")
    val usedDt: State<String> = _usedDt
    private val _isFavorite = mutableStateOf(false)
    val isFavorite: State<Boolean> = _isFavorite

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

    private val _isShowIndicator = mutableStateOf(false)
    val isShowIndicator: State<Boolean> = _isShowIndicator

    private val _isShowNoInternetDialog = mutableStateOf(false)
    val isShowNoInternetDialog: State<Boolean> = _isShowNoInternetDialog

    fun getGift(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            giftRepository.getGift(id).collectLatest { gift ->
                setGift(
                    Gift(
                        id = gift.id,
                        uid = gift.uid,
                        photo = loadImageFromPath(gift.photoPath),
                        name = gift.name,
                        brand = gift.brand,
                        endDt = gift.endDt,
                        addDt = gift.addDt,
                        memo = gift.memo,
                        usedDt = gift.usedDt,
                        cash = gift.cash,
                        isFavorite = gift.isFavorite
                    )
                )
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
        _photo.value = gift.photo
        _isCheckedCash.value = gift.cash.isNotEmpty()
        _isFavorite.value = gift.isFavorite
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

    fun setPhoto(photo: Bitmap?) {
        _photo.value = photo
    }

    fun toggleFavorite() {
        if (!isGuestMode && !networkMonitor.isConnected()) {
            _isShowNoInternetDialog.value = true
            return
        }

        val isFavorite = !_isFavorite.value
        giftRepository.updateGiftIsFavorite(isGuestMode, _gift.value.id, isFavorite) { result ->
            if (!result) return@updateGiftIsFavorite
            viewModelScope.launch(Dispatchers.IO) {
                giftRepository.updateGiftIsFavorite(_gift.value.id, isFavorite)
                _isFavorite.value = isFavorite
            }
        }
    }

    fun updateGift(onComplete: (Boolean) -> Unit) {
        if (!isGuestMode && !networkMonitor.isConnected()) {
            onComplete(false)
            _isShowNoInternetDialog.value = true
            return
        }

        _isShowIndicator.value = true
        val updateGift = if (_isCheckedCash.value) {
            Gift(
                id = _gift.value.id,
                uid = _gift.value.uid,
                photo = _photo.value,
                name = _name.value,
                brand = _brand.value,
                endDt = _endDate.value,
                addDt = _gift.value.addDt,
                memo = _memo.value,
                usedDt = _gift.value.usedDt,
                cash = _cash.value,
                isFavorite = _isFavorite.value
            )
        } else {
            Gift(
                id = _gift.value.id,
                uid = _gift.value.uid,
                photo = _photo.value,
                name = _name.value,
                brand = _brand.value,
                endDt = _endDate.value,
                addDt = _gift.value.addDt,
                memo = _memo.value,
                usedDt = _gift.value.usedDt,
                cash = "",
                isFavorite = _isFavorite.value
            )
        }
        giftRepository.updateGift(isGuestMode, updateGift, true) { result ->
            // 수정 성공
            if (result) {
                // 로컬 수정
                viewModelScope.launch(Dispatchers.IO) {
                    giftRepository.insertGift(updateGift)
                }
                alarmRepository.cancelAlarm(updateGift.id, preferenceRepository.getNotiEndDtDay())
                _gift.value = updateGift
                _isEdit.value = false
                onComplete(true)
            } else { // 수정 실패
                onComplete(false)
            }
            _isShowIndicator.value = false
        }
    }

    fun setIsUsed(flag: Boolean, cash: Int? = null, onComplete: (Boolean) -> Unit) {
        if (!isGuestMode && !networkMonitor.isConnected()) {
            onComplete(false)
            _isShowNoInternetDialog.value = true
            return
        }

        _isShowIndicator.value = true
        var nowDt = ""
        if ((flag && cash == null) || (flag && cash == 0)) {
            nowDt = SimpleDateFormat(
                "yyyy.MM.dd",
                Locale.getDefault()
            ).format(Date(System.currentTimeMillis()))
        }
        val gift = if (cash == null) _gift.value.copy(usedDt = nowDt) else _gift.value.copy(
            usedDt = nowDt,
            cash = cash.toString()
        )
        giftRepository.updateGift(isGuestMode, gift, false) { result ->
            if (result) {
                // 로컬 수정
                viewModelScope.launch(Dispatchers.IO) {
                    giftRepository.insertGift(gift)
                }
                _isShowBottomSheet.value = false
                _isShowUseCashDialog.value = false
            } else { // 수정 실패
                onComplete(false)
            }
            _isShowIndicator.value = false
        }
    }

    fun chgCheckedCash() {
        _isCheckedCash.value = !_isCheckedCash.value
    }

    fun changeDatePickerState() {
        _isShowDatePicker.value = !_isShowDatePicker.value
    }

    fun changeNoInternetDialogState() {
        _isShowNoInternetDialog.value = !_isShowNoInternetDialog.value
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