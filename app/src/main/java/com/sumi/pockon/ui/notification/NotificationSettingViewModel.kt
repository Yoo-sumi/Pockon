package com.sumi.pockon.ui.notification

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumi.pockon.data.repository.PreferenceRepository
import com.sumi.pockon.data.repository.GiftRepository
import com.sumi.pockon.data.model.Gift
import com.sumi.pockon.data.repository.AlarmRepository
import com.sumi.pockon.util.convertTo12HourFormat
import com.sumi.pockon.util.loadImageFromPath
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationSettingViewModel @Inject constructor(
    private val giftRepository: GiftRepository,
    private val preferenceRepository: PreferenceRepository,
    private val alarmRepository: AlarmRepository
) : ViewModel() {

    private var giftList = listOf<Gift>()
    private var isLoading = true
    private var selectedHour = 0
    private var selectedMinute = 0

    private val dayList = listOf(0, 1, 3, 7, 14)
    private var isNotiEndDt = preferenceRepository.isNotiEndDt()

    private val _seletedTime = mutableStateOf("")
    val seletedTime: State<String> = _seletedTime

    private val _seletedDay = mutableIntStateOf(preferenceRepository.getNotiEndDtDay())
    val seletedDay: State<Int> = _seletedDay

    private val _isShowTimePickerWheelDialog = mutableStateOf(false)
    val isShowTimePickerWheelDialog: State<Boolean> = _isShowTimePickerWheelDialog

    init {
        val time = preferenceRepository.getNotiEndDtTime()
        _seletedTime.value = convertTo12HourFormat(time.first, time.second)
        selectedHour = time.first
        selectedMinute = time.second

        viewModelScope.launch(Dispatchers.IO) {
            giftRepository.getAllGift().collectLatest { allGift ->
                val tempList = ArrayList<Gift>()
                allGift.forEach { gift ->
                    val tempGift = Gift(
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
                    tempList.add(tempGift)
                }
                giftList = tempList
                isLoading = false
            }
        }
    }

    fun getDayList() = dayList

    fun setSeletedDay(day: Int) {
        _seletedDay.intValue = day
    }
    fun toggleIsShowTimePickerWheelDialog() {
        _isShowTimePickerWheelDialog.value = !_isShowTimePickerWheelDialog.value
    }

    fun getNotiEndDtTime() = preferenceRepository.getNotiEndDtTime()

    fun selectedTime(hour24: Int, minute: Int) {
        _seletedTime.value = convertTo12HourFormat(hour24, minute)
        selectedHour = hour24
        selectedMinute = minute
    }

    fun changeNotiEndDt() {
        if (!isNotiEndDt || isLoading) return

        preferenceRepository.saveNotiEndDtDay(_seletedDay.intValue)
        preferenceRepository.saveNotiEndDtTime(selectedHour, selectedMinute)
        preferenceRepository.saveAlarmList(mutableSetOf())
        val alarmList = mutableSetOf<String>()
        giftList.forEach { gift ->
            // 알림 등록
            alarmList.add(gift.id)
            alarmRepository.cancelAlarm(gift.id)
            alarmRepository.setAlarm(gift, preferenceRepository.getNotiEndDtDay(), preferenceRepository.getNotiEndDtTime())
        }
        preferenceRepository.saveAlarmList(alarmList)
    }
}