package com.example.giftbox.ui.home

import android.content.SharedPreferences
import android.location.Location
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.giftbox.data.BrandSearchRepository
import com.example.giftbox.data.GiftRepository
import com.example.giftbox.model.Document
import com.example.giftbox.model.Gift
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val giftRepository: GiftRepository,
    private val brandSearchRepository: BrandSearchRepository,
    private val sharedPref: SharedPreferences
) : ViewModel() {

    private var uid = sharedPref.getString("uid", "") ?: ""
    private var giftList:List<Gift> = listOf()

    private val _displayGiftList = mutableStateOf<List<Pair<Gift, Document>>>(listOf())
    val displayGiftList: State<List<Pair<Gift, Document>>> = _displayGiftList
    
    fun getGiftList(location: Location?) {
        giftRepository.getAllGift(uid) { giftList ->
            if (giftList.isNotEmpty()) {
                this.giftList = giftList
                // 로컬 저장
                viewModelScope.launch(Dispatchers.IO) {
                    giftRepository.deleteAllGift()
                    giftList.forEach { gift ->
                        giftRepository.insertGift(gift)
                    }
                }
                getBrandInfoList(location)
            } else {
                this.giftList = listOf()
            }
        }
    }

    private fun getBrandInfoList(location: Location?) {
        val allList: ArrayList<Pair<Gift, Document>> = arrayListOf()

        val brandNames = ArrayList<String>()
        giftList.forEach {
            if (!brandNames.contains(it.brand)) brandNames.add(it.brand)
        }
        if (brandNames.isNotEmpty()) brandSearchRepository.searchBrandInfoList(location, brandNames) { brandInfoList ->
            giftList.forEach { gift ->
                // 가장 가까운 첫번째 위치만 보여준다(여러개의 스타벅스 중 가장 가까이 있는 한 곳)
                brandInfoList[gift.brand]?.sortedBy { Integer.parseInt(it.distance) }?.get(0).let { doc ->
                    // gift, doc
                    if (doc != null) allList.add(Pair(gift, doc))
                }
            }
            // 거리순으로 정렬(스타벅스, 투썸..)
            allList.sortBy { it.second.distance }
            _displayGiftList.value = allList

            // 로컬 저장
            viewModelScope.launch(Dispatchers.IO) {
                brandInfoList.forEach { keyword, documents ->
                    if (documents != null) brandSearchRepository.insertBrands(keyword, documents) // 키워드별 브랜드 위치정보 저장
                }
            }
        }
    }

    fun formatString(endDate: String): String {
        return endDate.mapIndexed { index, c ->
            if (index == 3 || index == 5) "${c}." else c
        }.joinToString("")
    }
}