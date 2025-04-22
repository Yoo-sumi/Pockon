package com.sumi.pockon.ui.notification

import android.content.SharedPreferences
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumi.pockon.alarm.MyAlarmManager
import com.sumi.pockon.data.repository.GiftRepository
import com.sumi.pockon.data.model.Gift
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
    private val sharedPref: SharedPreferences,
    private val myAlarmManager: MyAlarmManager
) : ViewModel() {

    private val dayList = listOf(0, 1, 3, 7, 14)
    private var isNotiEndDt = sharedPref.getBoolean("noti_end_dt", true)
    private val initialSelectedDay = sharedPref.getInt("noti_end_dt_day", 0)

    private val _seletedDay = mutableIntStateOf(sharedPref.getInt("noti_end_dt_day", 0))
    val seletedDay: State<Int> = _seletedDay

    private val _isShowTimePickerWheelDialog = mutableStateOf(false)
    val isShowTimePickerWheelDialog: State<Boolean> = _isShowTimePickerWheelDialog

    fun getDayList() = dayList

    fun setSeletedDay(day: Int) {
        _seletedDay.intValue = day
        sharedPref.edit().putInt("noti_end_dt_day", day).apply()
    }

    fun toggleIsShowTimePickerWheelDialog() {
        _isShowTimePickerWheelDialog.value = !_isShowTimePickerWheelDialog.value
    }

    fun changeNotiEndDt() {
        if (!isNotiEndDt || initialSelectedDay == _seletedDay.intValue) return

        sharedPref.edit().putStringSet("alarm_list", mutableSetOf()).apply()
        viewModelScope.launch(Dispatchers.IO) {
            giftRepository.getAllGift().take(1).collectLatest { allGift ->
                val notiEndDay = sharedPref.getInt("noti_end_dt_day", 0)
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
                sharedPref.edit().putStringSet("alarm_list", alarmList).apply()
            }
        }
    }
}