package com.sumi.pockon.data.repository

import com.sumi.pockon.data.local.preference.PreferenceLocalDataSource

class PreferenceRepository(private val localDataSource: PreferenceLocalDataSource) {

    fun getUid() = localDataSource.getUid()

    fun getPinNum() = localDataSource.getPinNum()

    fun isAuthPin() = localDataSource.isAuthPin()

    fun isGuestMode() = localDataSource.isGuestMode()

    fun isFirstLogin() = localDataSource.isFirstLogin()

    fun isNotiEndDt() = localDataSource.isNotiEndDt()

    fun getNotiEndDtDay() = localDataSource.getNotiEndDtDay()

    fun getNotiEndDtTime() = Pair(localDataSource.getNotiEndDtHour(), localDataSource.getNotiEndDtMinute())

    fun getAlarmList() = localDataSource.getAlarmList()

    fun saveUid(uid: String) = localDataSource.saveUid(uid)

    fun savePinNum(pinNum: String) {
        localDataSource.savePinNum(pinNum)
        localDataSource.saveIsAuthPin(true)
    }

    fun saveIsGuestMode(isGuestMode: Boolean) = localDataSource.saveIsGuestMode(isGuestMode)

    fun saveIsFirstLogin(isFirstLogin: Boolean) = localDataSource.saveIsFirstLogin(isFirstLogin)

    fun saveNotiEndDtDay(day: Int) = localDataSource.saveNotiEndDtDay(day)

    fun saveAlarmList(alarmList: MutableSet<String>?) = localDataSource.saveAlarmList(alarmList)

    fun saveNotiEndDtTime(hour24: Int, minute: Int) {
        localDataSource.saveNotiEndDtHour(hour24)
        localDataSource.saveNotiEndDtMinute(minute)
    }

    fun onOffNotiEndDt(flag: Boolean) {
        localDataSource.saveIsNotiEndDt(flag)
        localDataSource.saveAlarmList(mutableSetOf())
    }

    fun offAuthPin() {
        localDataSource.removePinNum()
        localDataSource.removeAuthPin()
    }

    fun removeAll() = localDataSource.removeAll()
}
