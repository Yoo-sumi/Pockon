package com.example.giftbox.ui.list

import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.giftbox.model.Gift
import com.example.giftbox.data.GiftRepository
import com.example.giftbox.R
import com.example.giftbox.ui.utils.bitmapToString
import com.example.giftbox.ui.utils.stringTobitmap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
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
    val giftList: State<List<Gift>> = _giftList

    private val _pullRefreshState = mutableStateOf(false)
    val pullRefreshState: State<Boolean> = _pullRefreshState

    init {
        getGiftList()
    }

    fun getGiftList() {
        viewModelScope.launch {
            giftRepository.getAllGift(uid).collect { giftList ->
                if (giftList.isNotEmpty()) {
                    _giftList.value = giftList
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
        val endDate = dateFormat.parse(endDate)?.time
        if (endDate != null && startDate != null) {
            val diff = (startDate - endDate) / (24 * 60 * 60 * 1000)
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
}