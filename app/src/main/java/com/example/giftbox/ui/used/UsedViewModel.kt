package com.example.giftbox.ui.used

import android.content.SharedPreferences
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.giftbox.data.GiftRepository
import com.example.giftbox.model.Gift
import com.example.giftbox.ui.utils.loadImageFromPath
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class UsedViewModel @Inject constructor(
    private val giftRepository: GiftRepository,
    private val sharedPref: SharedPreferences
) : ViewModel() {

    private var uid = sharedPref.getString("uid", "") ?: ""
    private var isGuestMode = sharedPref.getBoolean("guest_mode", false)

    private var removeGift: Gift? = null

    private val _giftList = mutableStateOf<List<Gift>>(listOf())
    val giftList: State<List<Gift>> = _giftList

    private val _checkedGiftList = mutableStateOf<List<String>>(listOf())
    val checkedGiftList: State<List<String>> = _checkedGiftList

    private val _isAllSelect = mutableStateOf<Boolean>(false)
    val isAllSelect: State<Boolean> = _isAllSelect

    private val _isShowIndicator = mutableStateOf(false)
    val isShowIndicator: State<Boolean> = _isShowIndicator

    init {
        observeGiftList()
    }

    // 로컬 기프티콘 목록 변화 감지해서 가져오기
    private fun observeGiftList() {
        viewModelScope.launch(Dispatchers.IO) {
            giftRepository.getAllUsedGift().collectLatest { allGift ->
                if (allGift.isNotEmpty()) {
                    val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
                    _giftList.value =  allGift.map { gift ->
                        Gift(id = gift.id, uid = gift.uid, photo = loadImageFromPath(gift.photoPath), name = gift.name, brand = gift.brand, endDt = gift.endDt, addDt = gift.addDt, memo = gift.memo, usedDt = gift.usedDt, cash = gift.cash)
                    }.sortedByDescending {  gift -> dateFormat.parse(gift.endDt)?.time }
                } else {
                    // 기프티콘 없음
                    _giftList.value = listOf()
                }
            }
        }
    }

    fun setIsAllSelect(flag: Boolean) {
        _isAllSelect.value = flag
    }

    // 선택된 기프티콘 리스트 초기화
    fun clearCheckedGiftList() {
        _checkedGiftList.value = listOf()
    }

    // 선택 삭제/전체 삭제
    fun deleteSelection(onComplete: (Boolean) -> Unit) {
        _isShowIndicator.value = true
        val resultList = ArrayList<Boolean>()
        var isFail = false
        _checkedGiftList.value.forEach { giftId ->
            giftRepository.removeGift(isGuestMode, uid, giftId) { result ->
                resultList.add(result)
                if (!result) isFail = true
                // end
                if (resultList.size == _checkedGiftList.value.size) {
                    if (isFail) {
                        onComplete(false)
                    } else {
                        // 로컬 삭제
                        viewModelScope.launch(Dispatchers.IO) {
                            giftRepository.deleteGifts(_checkedGiftList.value)
                        }
                        onComplete(true)
                    }
                    _isShowIndicator.value = false
                }
            }
        }
    }

    // 선택된 기프티콘 리스트에 추가(for 삭제)
    fun checkedGift(id: String) {
        val filterList = _checkedGiftList.value.filter { it != id }
        if (filterList.size == _checkedGiftList.value.size) { // 선택
            val checkedList = _checkedGiftList.value.toMutableList()
            checkedList.add(id)
            _checkedGiftList.value = checkedList

            if (_checkedGiftList.value.size == _giftList.value.size) _isAllSelect.value = true
        } else { // 해제
            _checkedGiftList.value = filterList
            if (_checkedGiftList.value.size != _giftList.value.size) _isAllSelect.value = false
        }
    }

    // 전체선택/전체해제
    fun onClickAllSelect() {
        _isAllSelect.value = !_isAllSelect.value
        if (_isAllSelect.value) {
            _checkedGiftList.value = _giftList.value.map { it.id }
        } else {
            _checkedGiftList.value = listOf()
        }
    }

}