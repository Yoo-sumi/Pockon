package com.sumi.pockon.ui.home

import android.content.SharedPreferences
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumi.pockon.alarm.MyAlarmManager
import com.sumi.pockon.data.local.gift.GiftEntity
import com.sumi.pockon.data.repository.BrandSearchRepository
import com.sumi.pockon.data.repository.GiftRepository
import com.sumi.pockon.data.model.Document
import com.sumi.pockon.data.model.Gift
import com.sumi.pockon.util.getDdayInt
import com.sumi.pockon.util.loadImageFromPath
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val giftRepository: GiftRepository,
    private val brandSearchRepository: BrandSearchRepository,
    private val sharedPref: SharedPreferences,
    private val myAlarmManager: MyAlarmManager
) : ViewModel() {

    private var longitude: Double? = null
    private var latitude: Double? = null

    private val uid = sharedPref.getString("uid", "") ?: ""
    private val isGuestMode = sharedPref.getBoolean("guest_mode", false)
    private val isFirstLogin = sharedPref.getBoolean("first_login", true)
    private val isNotiEndDt = sharedPref.getBoolean("noti_end_dt", true)
    private val notiEndDay = sharedPref.getInt("noti_end_dt_day", 0)

    private var giftList: List<Gift> = listOf()

    private val _nearGiftList = mutableStateOf<List<Pair<Gift, Document>>>(listOf())
    val nearGiftList: State<List<Pair<Gift, Document>>> = _nearGiftList

    private val _favoriteGiftList = mutableStateOf<List<Gift>>(listOf())
    val favoriteGiftList: State<List<Gift>> = _favoriteGiftList

    private val _isShowIndicator = mutableStateOf(false)
    val isShowIndicator: State<Boolean> = _isShowIndicator

    init {
        observeGiftList()
        getGiftList()
    }

    // 서버에서 기프티콘 리스트 가져오기
    private fun getGiftList() {
        _isShowIndicator.value = true

        if (isGuestMode || !isFirstLogin) {
            _isShowIndicator.value = false
            return
        } // 게스트 모드 또는 최초 로그인이 아니면 서버 안탐

        giftRepository.getAllGift(uid) { giftList ->
            if (giftList.isNotEmpty()) {
                // 로컬 저장(기프티콘)
                viewModelScope.launch(Dispatchers.IO) {
                    giftRepository.deleteAllAndInsertGifts(giftList)
                    if (isFirstLogin) sharedPref.edit().putBoolean("first_login", false).apply()
                }
            } else {
                this.giftList = listOf()
                _nearGiftList.value = listOf()
                _favoriteGiftList.value = listOf()
            }
            _isShowIndicator.value = false
        }
    }

    // 로컬 기프티콘 목록 변화 감지해서 가져오기
    private fun observeGiftList() {
        viewModelScope.launch(Dispatchers.IO) {
            giftRepository.getAllGift(1).collectLatest { allGift ->
                showGiftList(allGift)
            }
        }
    }

    private fun showGiftList(allGift: List<GiftEntity>) {
        if (allGift.isNotEmpty()) {
            giftList = allGift.map { gift ->
                Gift(
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
            }

            val alarmList = sharedPref.getStringSet("alarm_list", mutableSetOf())?.toMutableSet()
            giftList.forEach { gift ->
                if (isNotiEndDt) {
                    // 알림 등록
                    if (getDdayInt(gift.endDt) == notiEndDay && alarmList?.contains(gift.id) == false) {
                        alarmList.add(gift.id)
                        sharedPref.edit().putStringSet("alarm_list", alarmList).apply()
                        myAlarmManager.schedule(gift, getDdayInt(gift.endDt))
                    }
                } else {
                    sharedPref.edit().putStringSet("alarm_list", mutableSetOf()).apply()
                    myAlarmManager.cancel(gift.id)
                }
            }

            // 즐겨찾기 기프티콘 목록
            _favoriteGiftList.value = giftList.filter { it.isFavorite }
            getBrandInfoList() // 브랜드 검색
        } else {
            _favoriteGiftList.value = listOf()
            _nearGiftList.value = listOf()
            giftList = listOf()
        }
    }

    // 브랜드 검색 후 로컬에 저장
    private fun getBrandInfoList() {
        val allList: ArrayList<Pair<Gift, Document>> = arrayListOf()

        val brandNames = ArrayList<String>()
        giftList.filter { it.usedDt.isEmpty() }.forEach {
            if (!brandNames.contains(it.brand)) brandNames.add(it.brand)
        }
        if (brandNames.isNotEmpty() && longitude != null && latitude != null) {
            brandSearchRepository.searchBrandInfoList(
                longitude!!,
                latitude!!,
                brandNames
            ) { brandInfoList ->
                giftList.forEach { gift ->
                    // 가장 가까운 첫번째 위치만 보여준다(여러개의 스타벅스 중 가장 가까이 있는 한 곳)
                    if (brandInfoList[gift.brand]?.isEmpty() == true) return@forEach // 검색 결과가 없는 경우 스킵
                    val filterList = brandInfoList[gift.brand]
                        ?.filter { doc ->
                            try {
                                Integer.parseInt(doc.distance) // 변환이 가능하면 true
                                true
                            } catch (e: NumberFormatException) {
                                false // 변환 불가능한 경우 false
                            }
                        }
                    if (filterList?.isNotEmpty() == true) {
                        filterList
                            .sortedBy { Integer.parseInt(it.distance) }[0]
                            .let { doc ->
                                // gift, doc
                                allList.add(Pair(gift, doc))
                            }

                    }
                }
                // 거리순으로 정렬(스타벅스, 투썸..)
                allList.sortBy { it.second.distance.toDouble() }
                _nearGiftList.value = allList

                // 로컬 저장
                viewModelScope.launch(Dispatchers.IO) {
                    brandSearchRepository.deleteAllBrands()
                    brandInfoList.forEach { (keyword, documents) ->
                        if (documents != null) brandSearchRepository.insertBrands(
                            keyword,
                            documents
                        ) // 키워드별 브랜드 위치정보 저장
                    }
                }
            }
        }
    }

    fun setLocation(longitude: Double?, latitude: Double?) {
        this.longitude = longitude
        this.latitude = latitude

        if (giftList.isNotEmpty()) {
            _favoriteGiftList.value = giftList.filter { it.isFavorite }
            getBrandInfoList()
        } else {
            _favoriteGiftList.value = listOf()
            _nearGiftList.value = listOf()
        }
    }
}