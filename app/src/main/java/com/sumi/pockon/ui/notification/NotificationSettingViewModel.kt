package com.sumi.pockon.ui.notification

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumi.pockon.alarm.MyAlarmManager
import com.sumi.pockon.data.local.PreferenceRepository
import com.sumi.pockon.data.repository.GiftRepository
import com.sumi.pockon.data.model.Gift
import com.sumi.pockon.util.convertTo12HourFormat
import com.sumi.pockon.util.loadImageFromPath
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationSettingViewModel @Inject constructor(
    private val giftRepository: GiftRepository,
    private val preferenceRepository: PreferenceRepository,
    private val myAlarmManager: MyAlarmManager
) : ViewModel() {

    private val dayList = listOf(0, 1, 3, 7, 14)
    private var isNotiEndDt = preferenceRepository.isNotiEndDt()
    private val initialSelectedDay = preferenceRepository.getNotiEndDtDay()

    private val _seletedTime = mutableStateOf("")
    val seletedTime: State<String> = _seletedTime

    private val _seletedDay = mutableIntStateOf(preferenceRepository.getNotiEndDtDay())
    val seletedDay: State<Int> = _seletedDay

    private val _isShowTimePickerWheelDialog = mutableStateOf(false)
    val isShowTimePickerWheelDialog: State<Boolean> = _isShowTimePickerWheelDialog

    init {
        val time = preferenceRepository.getNotiEndDtTime()
        _seletedTime.value = convertTo12HourFormat(time.first, time.second)
    }

    fun getDayList() = dayList

    fun setSeletedDay(day: Int) {
        _seletedDay.intValue = day
        preferenceRepository.saveNotiEndDtDay(day)
    }

    fun toggleIsShowTimePickerWheelDialog() {
        _isShowTimePickerWheelDialog.value = !_isShowTimePickerWheelDialog.value
    }

    fun getNotiEndDtTime() = preferenceRepository.getNotiEndDtTime()

    fun saveNotiEndDtTime(hour24: Int, minute: Int) {
        preferenceRepository.saveNotiEndDtTime(hour24, minute)
        _seletedTime.value = convertTo12HourFormat(hour24, minute)
        toggleIsShowTimePickerWheelDialog()
    }

    fun changeNotiEndDt() {
        if (!isNotiEndDt || initialSelectedDay == _seletedDay.intValue) return

        preferenceRepository.saveAlarmList(mutableSetOf())
        viewModelScope.launch(Dispatchers.IO) {
            giftRepository.getAllGift().take(1).collectLatest { allGift ->
                val notiEndDay = preferenceRepository.getNotiEndDtDay()
                val alarmList = mutableSetOf<String>()
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
                    // 알림 등록
                    alarmList.add(gift.id)
                    myAlarmManager.cancel(tempGift.id)
                    myAlarmManager.schedule(tempGift, notiEndDay)
                }
                preferenceRepository.saveAlarmList(alarmList)
            }
        }
    }
}