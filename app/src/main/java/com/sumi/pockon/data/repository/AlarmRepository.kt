package com.sumi.pockon.data.repository

import com.sumi.pockon.data.local.alarm.AlarmDataSource
import com.sumi.pockon.data.local.preference.PreferenceLocalDataSource
import com.sumi.pockon.data.model.Gift
import javax.inject.Inject

class AlarmRepository @Inject constructor(
    private val alarmDataSource: AlarmDataSource,
    private val preferenceLocalDataSource: PreferenceLocalDataSource
) {

    fun setAlarm(gift: Gift, dDay: Int, time: Pair<Int, Int>) {
        alarmDataSource.schedule(gift, dDay, time)
        val count = preferenceLocalDataSource.getNotiGroupCount()
        preferenceLocalDataSource.saveNotiGroupCount(count + 1)
    }

    fun cancelAlarm(id: String, dDay: Int) {
        alarmDataSource.cancel(id, dDay)
        val count = preferenceLocalDataSource.getNotiGroupCount()
        preferenceLocalDataSource.saveNotiGroupCount(if (count > 0) count - 1 else 0)
    }
}