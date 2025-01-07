package com.example.giftbox.ui.home

import android.content.SharedPreferences
import android.location.Location
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.giftbox.data.BrandsRepository
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
    private val brandsRepository: BrandsRepository,
    private val sharedPref: SharedPreferences
) : ViewModel() {

    private var uid = sharedPref.getString("uid", "") ?: ""
    private var giftList:List<Gift> = listOf()
    private var brandMappingList: Map<String, List<Document>> = mapOf()

    private val _displayGiftList = mutableStateOf<List<Pair<Gift, Document>>>(listOf())
    val displayGiftList: State<List<Pair<Gift, Document>>> = _displayGiftList
    
    fun getGiftList(location: Location?) {
        giftRepository.getAllGift(uid) { giftList ->
            if (giftList.isNotEmpty()) {
                this.giftList = giftList
                getBrandInfoList(location)
            } else {
                this.giftList = listOf()
            }
        }
    }

    private fun getBrandInfoList(location: Location?) {
        val mappingList: MutableMap<String, ArrayList<Document>> = mutableMapOf()
        val allList: ArrayList<Pair<Gift, Document>> = arrayListOf()

        val brandNames = ArrayList<String>()
        giftList.forEach {
            if (!brandNames.contains(it.brand)) brandNames.add(it.brand)
        }
        if (brandNames.isNotEmpty()) brandsRepository.searchBrandInfoList(location, brandNames) { keywords, brands ->
            brands.forEachIndexed { index, brand ->
                val sortedList = brand?.documents?.sortedBy { Integer.parseInt(it.distance) }
                sortedList?.forEach { document ->
                    if (mappingList.keys.contains(keywords[index])) mappingList[keywords[index]]?.add(document)
                    else mappingList[keywords[index]] = arrayListOf(document)
                }
            }
            brandMappingList = mappingList
            giftList.forEach { gift ->
                // 가장 가까운 첫번째 위치만 보여준다
                brandMappingList[gift.brand]?.get(0)?.let { doc ->
                    allList.add(Pair(gift, doc))
                }
            }
            allList.sortBy { it.second.distance }
            _displayGiftList.value = allList

            // 로컬 저장
            viewModelScope.launch(Dispatchers.IO) {
                for (k in 0..keywords.lastIndex) {
                    brands[k]?.let { brand ->
                        brandsRepository.insertBrands(keywords[k], brand.documents) // 브랜드별 위치정보 저장
                    }
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