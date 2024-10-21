package com.example.giftbox.ui.list

import com.example.giftbox.R
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.giftbox.model.Gift
import com.example.giftbox.data.GiftRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val giftRepository: GiftRepository,
    private val sharedPref: SharedPreferences
) : ViewModel() {
    private var uid = sharedPref.getString("uid", "") ?: ""

    private val _giftList = mutableStateOf<List<Gift>>(listOf())

    private val _copyGiftList = mutableStateOf<List<Gift>>(listOf())
    val copyGiftList: State<List<Gift>> = _copyGiftList

    private val _filterList = mutableStateOf(listOf<String>())
    val filterList: State<List<String>> = _filterList

    private var _chipElement = mutableStateOf<Map<String, Boolean>?>(null)
    val chipElement: State<Map<String, Boolean>?> = _chipElement

    private val _topTitle = mutableIntStateOf(R.string.top_app_bar_recent)
    val topTitle: State<Int> = _topTitle

    init {
        getGiftList()
    }

    fun setTopTitle(title: Int) {
        _topTitle.intValue = title
    }

    fun getGiftList() {
        viewModelScope.launch {
            giftRepository.getAllGift(uid).collect { giftList ->
                if (giftList.isNotEmpty()) {
                    _giftList.value = giftList
                    _copyGiftList.value = _giftList.value

                    val element = mutableMapOf("" to true)
                    giftList.forEach {
                        if (!element.containsKey(it.brand)) element[it.brand] = false
                    }
                    _chipElement.value = element.toList().sortedWith(compareBy({it.first})).toMap()

                    orderBy()
                }
            }
        }
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

    fun changeChipState(target: String) {
        val beforeElements = mutableMapOf<String, Boolean>()
        val beforeFilters = mutableListOf<String>()

        _chipElement.value?.keys?.forEach { key ->
            val state = _chipElement.value!![key]
            if (target == key) beforeElements[key] = !state!! else beforeElements[key] = state!!

            if (target.isEmpty() && key.isNotEmpty()) {
                beforeElements[key] = false
            }
            if (target.isNotEmpty() && key.isEmpty()) {
                beforeElements[key] = false
            }

            if (beforeElements[key] == true && key.isNotEmpty()) beforeFilters.add(key)
        }

        if (beforeFilters.isEmpty() && beforeElements[""] == false) {
            beforeElements[""] = true
        }

        _chipElement.value = beforeElements
        _filterList.value = beforeFilters
        filterList()
        orderBy()
    }

    private fun filterList() {
        val filtered = mutableListOf<Gift>()
        _giftList.value.forEach {
            if (_filterList.value.contains(it.brand) || _filterList.value.isEmpty()) filtered.add(it)
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
}