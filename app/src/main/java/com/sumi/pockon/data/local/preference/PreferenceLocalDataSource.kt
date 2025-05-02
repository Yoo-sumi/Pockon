package com.sumi.pockon.data.local.preference

import android.content.SharedPreferences
import com.sumi.pockon.util.SharedPrefKeys

class PreferenceLocalDataSource(private val prefs: SharedPreferences) {

    fun getUid(): String = prefs.getString(SharedPrefKeys.UID, "") ?: ""

    fun getPinNum(): String = prefs.getString(SharedPrefKeys.PIN_NUM, "") ?: ""

    fun isAuthPin() = prefs.getBoolean(SharedPrefKeys.AUTH_PIN, false)

    fun isGuestMode() = prefs.getBoolean(SharedPrefKeys.GUEST_MODE, false)

    fun isFirstLogin() = prefs.getBoolean(SharedPrefKeys.FIRST_LOGIN, true)

    fun isNotiEndDt() = prefs.getBoolean(SharedPrefKeys.NOTI_END_DT, true)

    fun getNotiEndDtDay() = prefs.getInt(SharedPrefKeys.NOTI_END_DT_DAY, 0)

    fun getNotiEndDtHour() = prefs.getInt(SharedPrefKeys.NOTI_END_DT_HOUR, 0)

    fun getNotiEndDtMinute() = prefs.getInt(SharedPrefKeys.NOTI_END_DT_MINUTE, 0)

    fun getAlarmList(): MutableSet<String>? = prefs.getStringSet(SharedPrefKeys.ALARM_LIST, mutableSetOf())

    fun saveUid(uid: String) {
        prefs.edit().putString(SharedPrefKeys.UID, uid).apply()
    }

    fun savePinNum(pinNum: String) {
        prefs.edit().putString(SharedPrefKeys.PIN_NUM, pinNum).apply()
    }

    fun saveIsAuthPin(isAuthPin: Boolean) {
        prefs.edit().putBoolean(SharedPrefKeys.AUTH_PIN, isAuthPin).apply()
    }

    fun saveIsGuestMode(isGuestMode: Boolean) {
        prefs.edit().putBoolean(SharedPrefKeys.GUEST_MODE, isGuestMode).apply()
    }

    fun saveIsFirstLogin(isFirstLogin: Boolean) {
        prefs.edit().putBoolean(SharedPrefKeys.FIRST_LOGIN, isFirstLogin).apply()
    }

    fun saveIsNotiEndDt(isNotiEndDtDay: Boolean) {
        prefs.edit().putBoolean(SharedPrefKeys.NOTI_END_DT, isNotiEndDtDay).apply()
    }

    fun saveNotiEndDtDay(day: Int) {
        prefs.edit().putInt(SharedPrefKeys.NOTI_END_DT_DAY, day).apply()
    }

    fun saveNotiEndDtHour(hour: Int) {
        prefs.edit().putInt(SharedPrefKeys.NOTI_END_DT_HOUR, hour).apply()
    }

    fun saveNotiEndDtMinute(minute: Int) {
        prefs.edit().putInt(SharedPrefKeys.NOTI_END_DT_MINUTE, minute).apply()
    }

    fun saveAlarmList(alarmList: MutableSet<String>?) {
        prefs.edit().putStringSet(SharedPrefKeys.ALARM_LIST, alarmList).apply()
    }

    fun removePinNum() {
        prefs.edit().remove(SharedPrefKeys.PIN_NUM).apply()
    }

    fun removeAuthPin() {
        prefs.edit().remove(SharedPrefKeys.AUTH_PIN).apply()
    }

    fun removeAll() {
        prefs.edit().clear().apply()
    }
}
