package com.sumi.pockon.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumi.pockon.alarm.MyAlarmManager
import com.sumi.pockon.data.local.PreferenceRepository
import com.sumi.pockon.data.repository.BrandSearchRepository
import com.sumi.pockon.data.repository.GiftRepository
import com.sumi.pockon.data.repository.LoginRepository
import com.sumi.pockon.data.model.Gift
import com.sumi.pockon.util.loadImageFromPath
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
    private val preferenceRepository: PreferenceRepository,
    private val myAlarmManager: MyAlarmManager
) : ViewModel() {

    private var uid = preferenceRepository.getUid()
    private var isAuthPin = preferenceRepository.isAuthPin()
    private var isNotiEndDt = preferenceRepository.isNotiEndDt()
    private var isGuestMode = preferenceRepository.isGuestMode()

    fun getIsNotiEndDt() = isNotiEndDt

    fun getIsAuthPin() = isAuthPin

    fun onOffNotiEndDt(flag: Boolean) {
        preferenceRepository.onOffNotiEndDt(flag)
        isNotiEndDt = flag

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
                    myAlarmManager.cancel(tempGift.id)
                    if (isNotiEndDt) {
                        // 알림 등록
                        alarmList.add(gift.id)
                        myAlarmManager.schedule(tempGift, notiEndDay)
                    }
                }
                if (isNotiEndDt) {
                    preferenceRepository.saveAlarmList(alarmList)
                } else {
                    preferenceRepository.saveAlarmList(mutableSetOf())
                }
            }
        }
    }

    fun offAuthPin() {
        preferenceRepository.offAuthPin()
    }

    fun logout() {
        if (!isGuestMode) loginRepository.logout()
        preferenceRepository.removeAll()
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
                        preferenceRepository.removeAll()
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