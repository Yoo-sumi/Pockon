package com.sumi.pockon.ui.notification

import android.content.SharedPreferences
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumi.pockon.alarm.MyAlarmManager
import com.sumi.pockon.data.repository.GiftRepository
import com.sumi.pockon.data.model.Gift
import com.sumi.pockon.util.getDdayInt
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
    private val _seletedDay = mutableIntStateOf(sharedPref.getInt("noti_end_dt_day", 0))
    val seletedDay: State<Int> = _seletedDay
    private val initialSelectedDay = sharedPref.getInt("noti_end_dt_day", 0)

    fun getDayList() = dayList

    fun setSeletedDay(day: Int) {
        _seletedDay.intValue = day
        sharedPref.edit().putInt("noti_end_dt_day", day).apply()
    }

    fun changeNotiEndDt() {
        if (!isNotiEndDt || initialSelectedDay == _seletedDay.intValue) return

        val notiEndDay = sharedPref.getInt("noti_end_dt_day", 0)
        sharedPref.edit().putStringSet("alarm_list", mutableSetOf()).apply()
        viewModelScope.launch(Dispatchers.IO) {
            giftRepository.getAllGift().take(1).collectLatest { allGift ->
                val alarmList = sharedPref.getStringSet("alarm_list", mutableSetOf())?.toMutableSet()
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
                        cash = gift.cash
                    )
                    // 알림 등록
                    if (getDdayInt(tempGift.endDt) == notiEndDay && alarmList?.contains(gift.id) == false) {
                        alarmList.add(gift.id)
                        sharedPref.edit().putStringSet("alarm_list", alarmList).apply()
                        myAlarmManager.schedule(tempGift, getDdayInt(tempGift.endDt))
                    }
                }
            }
        }
    }
}