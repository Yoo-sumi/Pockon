package com.sumi.pockon.data.repository

import com.sumi.pockon.data.local.preference.PreferenceLocalDataSource
import javax.inject.Inject

class PreferenceRepository @Inject constructor(
    private val preferenceLocalDataSource: PreferenceLocalDataSource
) {

    fun getUid() = preferenceLocalDataSource.getUid()

    fun getEmail() = preferenceLocalDataSource.getEmail()

    fun getPinNum() = preferenceLocalDataSource.getPinNum()

    fun isAuthPin() = preferenceLocalDataSource.isAuthPin()

    fun isGuestMode() = preferenceLocalDataSource.isGuestMode()

    fun isFirstLogin() = preferenceLocalDataSource.isFirstLogin()

    fun isNotiEndDt() = preferenceLocalDataSource.isNotiEndDt()

    fun getNotiEndDtDay() = preferenceLocalDataSource.getNotiEndDtDay()

    fun getNotiEndDtTime() = Pair(preferenceLocalDataSource.getNotiEndDtHour(), preferenceLocalDataSource.getNotiEndDtMinute())

    fun saveUid(uid: String) = preferenceLocalDataSource.saveUid(uid)

    fun saveEmail(email: String) = preferenceLocalDataSource.saveEmail(email)

    fun savePinNum(pinNum: String) {
        preferenceLocalDataSource.savePinNum(pinNum)
        preferenceLocalDataSource.saveIsAuthPin(true)
    }

    fun saveIsGuestMode(isGuestMode: Boolean) = preferenceLocalDataSource.saveIsGuestMode(isGuestMode)

    fun saveIsFirstLogin(isFirstLogin: Boolean) = preferenceLocalDataSource.saveIsFirstLogin(isFirstLogin)

    fun saveNotiEndDtDay(day: Int) = preferenceLocalDataSource.saveNotiEndDtDay(day)

    fun saveNotiEndDtTime(hour24: Int, minute: Int) {
        preferenceLocalDataSource.saveNotiEndDtHour(hour24)
        preferenceLocalDataSource.saveNotiEndDtMinute(minute)
    }

    fun onOffNotiEndDt(flag: Boolean) {
        preferenceLocalDataSource.saveIsNotiEndDt(flag)
    }

    fun offAuthPin() {
        preferenceLocalDataSource.removePinNum()
        preferenceLocalDataSource.removeAuthPin()
    }

    fun removeAll() = preferenceLocalDataSource.removeAll()
}
