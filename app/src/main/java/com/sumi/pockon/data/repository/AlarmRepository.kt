package com.sumi.pockon.data.repository

import com.sumi.pockon.data.local.alarm.AlarmDataSource
import com.sumi.pockon.data.model.Gift
import javax.inject.Inject

class AlarmRepository @Inject constructor(
    private val alarmDataSource: AlarmDataSource
) {

    fun setAlarm(gift: Gift, dDay: Int, time: Pair<Int, Int>) {
        alarmDataSource.schedule(gift, dDay, time)
    }

    fun cancelAlarm(id: String) {
        alarmDataSource.cancel(id)
    }
}