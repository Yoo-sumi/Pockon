package com.example.giftbox.ui.used

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.giftbox.data.BrandSearchRepository
import com.example.giftbox.data.GiftRepository
import com.example.giftbox.model.Document
import com.example.giftbox.model.Gift
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class UsedViewModel @Inject constructor(
    private val giftRepository: GiftRepository
) : ViewModel() {

    private val _giftList = mutableStateOf<List<Gift>>(listOf())
    val giftList: State<List<Gift>> = _giftList

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
                        Gift(id = gift.id, uid = gift.uid, photo = gift.photo, name = gift.name, brand = gift.brand, endDt = gift.endDt, addDt = gift.addDt, memo = gift.memo, usedDt = gift.usedDt, cash = gift.cash)
                    }.sortedByDescending {  gift -> dateFormat.parse(gift.endDt)?.time }
                } else {
                    // 기프티콘 없음
                    _giftList.value = listOf()
                }
            }
        }
    }

}