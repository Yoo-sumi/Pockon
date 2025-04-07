package com.example.giftbox.ui.settings

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.giftbox.alarm.MyAlarmManager
import com.example.giftbox.data.repository.BrandSearchRepository
import com.example.giftbox.data.repository.GiftRepository
import com.example.giftbox.data.repository.LoginRepository
import com.example.giftbox.data.model.Gift
import com.example.giftbox.util.getDdayInt
import com.example.giftbox.util.loadImageFromPath
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val giftRepository: GiftRepository,
    private val brandSearchRepository: BrandSearchRepository,
    private val sharedPref: SharedPreferences,
    private val myAlarmManager: MyAlarmManager
) : ViewModel() {

    private var uid = sharedPref.getString("uid", "") ?: ""
    private var isAuthPin = sharedPref.getBoolean("auth_pin", false)
    private var isNotiEndDt = sharedPref.getBoolean("noti_end_dt", true)
    private var isGuestMode = sharedPref.getBoolean("guest_mode", false)

    fun getIsNotiEndDt() = isNotiEndDt

    fun getIsAuthPin() = isAuthPin

    fun onOffNotiEndDt(flag: Boolean) {
        sharedPref.edit().putBoolean("noti_end_dt", flag).apply()
        isNotiEndDt = flag

        viewModelScope.launch(Dispatchers.IO) {
            giftRepository.getAllGift().take(1).collectLatest { allGift ->
                val alarmList =
                    sharedPref.getStringSet("alarm_list", mutableSetOf())?.toMutableSet()
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
                    if (isNotiEndDt) {
                        // 알림 등록
                        if (getDdayInt(tempGift.endDt) in 0..1 && alarmList?.contains(gift.id) == false) {
                            alarmList.add(gift.id)
                            sharedPref.edit().putStringSet("alarm_list", alarmList).apply()
                            myAlarmManager.schedule(tempGift, getDdayInt(tempGift.endDt))
                        }
                    } else {
                        sharedPref.edit().putStringSet("alarm_list", mutableSetOf()).apply()
                        myAlarmManager.cancel(tempGift.id)
                    }
                }
            }
        }
    }

    fun offAuthPin() {
        sharedPref.edit().remove("pin_num").apply()
        sharedPref.edit().remove("auth_pin").apply()
    }

    fun logout() {
        if (!isGuestMode) loginRepository.logout()
        sharedPref.edit().clear().apply()
        viewModelScope.launch(Dispatchers.IO) {
            giftRepository.deleteAllGift()
            brandSearchRepository.deleteAllBrands()
        }
    }

    fun removeAccount(onSuccess: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            giftRepository.getAllGift().take(1).collectLatest { gifts ->
                // remote 데이터 삭제
                giftRepository.removeGifts(isGuestMode, uid, gifts.map { it.id }) { result ->
                    if (result) {
                        // local 데이터 삭제
                        viewModelScope.launch(Dispatchers.IO) {
                            giftRepository.deleteAllGift()
                            brandSearchRepository.deleteAllBrands()
                        }
                        if (!isGuestMode) loginRepository.logout()
                        sharedPref.edit().clear().apply()
                        onSuccess(true)
                    } else {
                        onSuccess(false)
                    }
                }
            }
        }
    }

    fun getIsGuestMode() = this.isGuestMode
}