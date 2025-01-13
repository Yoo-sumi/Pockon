package com.example.giftbox.ui.list

import com.example.giftbox.R
import android.content.SharedPreferences
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.giftbox.model.Gift
import com.example.giftbox.data.GiftRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val giftRepository: GiftRepository,
    private val sharedPref: SharedPreferences
) : ViewModel() {
    private var uid = sharedPref.getString("uid", "") ?: ""
    private var removeGift: Gift? = null

    private val _giftList = mutableStateOf<List<Gift>>(listOf())
    val giftList: State<List<Gift>> = _giftList

    private val _copyGiftList = mutableStateOf<List<Gift>>(listOf())
    val copyGiftList: State<List<Gift>> = _copyGiftList

    private var filterList = listOf<String>()

    private var _chipElement = mutableStateOf<Map<String, Boolean>?>(null)
    val chipElement: State<Map<String, Boolean>?> = _chipElement

    private val _topTitle = mutableIntStateOf(R.string.top_app_bar_recent)
    val topTitle: State<Int> = _topTitle

    init {
        observeGiftList() // 관찰자 등록
        getGiftList()
    }

    fun setTopTitle(title: Int) {
        _topTitle.intValue = title
    }

    // 로컬 기프티콘 목록 변화 감지해서 가져오기
    private fun observeGiftList() {
        viewModelScope.launch(Dispatchers.IO) {
            giftRepository.getAllGift().collectLatest { allGift ->
                if (allGift.isNotEmpty()) {
                    _giftList.value = allGift.map { gift ->
                        Gift(id = gift.id, uid = gift.uid, photo = gift.photo, name = gift.name, brand = gift.brand, endDt = gift.endDt, addDt = gift.addDt, memo = gift.memo, usedDt = gift.usedDt)
                    }
                    _copyGiftList.value = _giftList.value
                    sortChips()
                    orderBy()
                } else {
                    _giftList.value = listOf()
                    _copyGiftList.value = listOf()
                    filterList = listOf()
                }
            }
        }
    }

    // 서버에서 기프티콘 리스트 가져오기
    fun getGiftList() {
        giftRepository.getAllGift(uid) { giftList ->
            if (giftList.isNotEmpty()) {
                // 로컬 저장(기프티콘)
                viewModelScope.launch(Dispatchers.IO) {
                    giftRepository.deleteAllGift()
                    giftList.forEach { gift ->
                        giftRepository.insertGift(gift)
                    }
                }
            } else {
                _giftList.value = listOf()
                _copyGiftList.value = listOf()
                filterList = listOf()
            }
        }
    }

    private fun sortChips() {
        val element = mutableMapOf("" to true)
        _giftList.value.forEach {
            if (!element.containsKey(it.brand)) element[it.brand] = false
        }
        _chipElement.value = element.toList().sortedWith(compareBy { it.first }).toMap()
    }

    fun setRemoveGift(gift: Gift) {
        removeGift = gift
    }

    fun formatString(endDate: String): String {
        return endDate.mapIndexed { index, c ->
            if (index == 3 || index == 5) "${c}." else c
        }.joinToString("")
    }

    fun getDday(endDate: String): String {
        val formatter = DateTimeFormatter.BASIC_ISO_DATE
        val current = LocalDateTime.now().format(formatter)

        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
        val startDate = dateFormat.parse(current)?.time
        val parseEndDate = dateFormat.parse(endDate)?.time
        if (parseEndDate != null && startDate != null) {
            val diff = (startDate - parseEndDate) / (24 * 60 * 60 * 1000)
            return if (diff.toInt() > 0) {
                "+$diff"
            } else if (diff.toInt() == 0) {
                "-$diff"
            } else {
                "$diff"
            }
        }
        return ""
    }

    fun changeChipState(targetList: List<String>) {
        val beforeElements = mutableMapOf<String, Boolean>()
        val beforeFilters = mutableListOf<String>()

        _chipElement.value?.keys?.forEach { key ->
            val state = _chipElement.value!![key]
            if (targetList.contains(key)) beforeElements[key] = !state!! else beforeElements[key] = state!!

            if (targetList.contains("") && key.isNotEmpty()) { // 전체 클릭
                beforeElements[key] = false
            }
            if (!targetList.contains("") && key.isEmpty()) { // 전체 이외 클릭
                beforeElements[key] = false
            }

            if (beforeElements[key] == true && key.isNotEmpty()) beforeFilters.add(key)
        }

        if (beforeFilters.isEmpty() && beforeElements[""] == false) {
            beforeElements[""] = true
        }

        _chipElement.value = beforeElements
        filterList = beforeFilters
        filterList()
        orderBy()
    }

    private fun filterList() {
        val filtered = mutableListOf<Gift>()
        _giftList.value.forEach {
            if (filterList.contains(it.brand) || filterList.isEmpty()) filtered.add(it)
        }
        _copyGiftList.value = filtered
    }

    fun orderBy() {
        if (_topTitle.intValue == R.string.top_app_bar_recent) {
            _copyGiftList.value = _copyGiftList.value.sortedByDescending {
                val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA)
                dateFormat.parse(it.addDt)?.time
            }
        } else { // 디데이순
            val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
            _copyGiftList.value = _copyGiftList.value.sortedBy {
                dateFormat.parse(it.endDt)?.time
            }
        }
    }

    // 기프티콘 수정
    fun usedGift(gift: Gift) {
        viewModelScope.launch {
            val nowDt = SimpleDateFormat(
                "yyyy.MM.dd",
                Locale.getDefault()
            ).format(Date(System.currentTimeMillis()))
            val updateGift = gift.copy(usedDt = nowDt)
            giftRepository.updateGift(updateGift).collect { result ->
                // 수정 성공
                if (result) {
                    // 로컬 수정
                    viewModelScope.launch(Dispatchers.IO) {
                        giftRepository.insertGift(updateGift)
                    }
                } else { // 수정 실패
                    // 네트워크가 불안정합니다. 인터넷 연결을 확인해주세요.
                    TODO()
                }
            }
        }
    }

    // 기프티콘 삭제
    fun removeGift() {
        if (removeGift ==  null) return
        if (removeGift?.id?.isEmpty() == true) return
        val id = removeGift!!.id
        removeGift = null
        viewModelScope.launch {
            giftRepository.removeGift(id).collect { result ->
                if (result) {
                    // 로컬 삭제
                    viewModelScope.launch(Dispatchers.IO) {
                        giftRepository.deleteGift(id)
                    }
                } else { // 삭제 실패
                    // 네트워크가 불안정합니다. 인터넷 연결을 확인해주세요.
                    TODO()
                }
            }
        }
    }
}