package com.example.giftbox.ui.settings

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.giftbox.alarm.MyAlarmManager
import com.example.giftbox.data.BrandSearchRepository
import com.example.giftbox.data.GiftRepository
import com.example.giftbox.data.LoginRepository
import com.example.giftbox.model.Gift
import com.example.giftbox.ui.utils.getDdayInt
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val giftRepository: GiftRepository,
    private val brandSearchRepository: BrandSearchRepository,
    private val sharedPref: SharedPreferences,
    private val myAlarmManager: MyAlarmManager
) : ViewModel() {

    private var uid = sharedPref.getString("uid", "") ?: ""
    private var isAuthPin = sharedPref.getBoolean("auth_pin", false)
    private var isNotiEndDt = sharedPref.getBoolean("noti_end_dt", true)

    fun getIsNotiEndDt() = isNotiEndDt

    fun getIsAuthPin() = isAuthPin

    fun onOffNotiEndDt(flag: Boolean) {
        sharedPref.edit().putBoolean("noti_end_dt", flag).apply()
        isNotiEndDt = flag

        viewModelScope.launch(Dispatchers.IO) {
            giftRepository.getAllGift().take(1).collectLatest { allGift ->
                allGift.forEach { gift ->
                    val tempGift = Gift(id = gift.id, uid = gift.uid, photo = gift.photo, name = gift.name, brand = gift.brand, endDt = gift.endDt, addDt = gift.addDt, memo = gift.memo, usedDt = gift.usedDt, cash = gift.cash)
                    myAlarmManager.cancel(tempGift.id)
                    // 알림 등록
                    if (isNotiEndDt && getDdayInt(tempGift.endDt) in 0..1) {
                        myAlarmManager.schedule(tempGift, getDdayInt(tempGift.endDt))
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
        loginRepository.logout()
        sharedPref.edit().clear().apply()
        viewModelScope.launch(Dispatchers.IO) {
            giftRepository.deleteAllGift()
            brandSearchRepository.deleteAllBrands()
        }
    }

    fun removeAccount(onSuccess: (Boolean) -> Unit) {
        viewModelScope.launch {
            giftRepository.getAllGift().collectLatest { gifts ->
                val removeList = ArrayList<String>()
                gifts.forEach { gift ->
                    // remote 데이터 삭제
                    giftRepository.removeGift(uid, gift.id) {
                        if (!it) {
                            removeList.add(gift.id)
                        }
                    }
                }
                // 서버 삭제 깔끔하게 완료
                // 로컬 데이터 삭제
                if (removeList.size == gifts.size) {
                    viewModelScope.launch(Dispatchers.IO) {
                        giftRepository.deleteAllGift()
                        brandSearchRepository.deleteAllBrands()
                    }
                } else { // 삭제된 것만 지우기(일부)
                    viewModelScope.launch(Dispatchers.IO) {
                        brandSearchRepository.deleteAllBrands()
                        removeList.forEach { id ->
                            giftRepository.deleteGift(id)
                        }
                    }
                    onSuccess(false) // 중간에 삭제 실패하면 삭제 중단
                }

                // 모두 삭제(remote/local)
                // 계정 삭제
                loginRepository.removeAccount { result ->
                    if (result)  sharedPref.edit().clear().apply()
                    onSuccess(result)
                }
            }
        }
    }

}