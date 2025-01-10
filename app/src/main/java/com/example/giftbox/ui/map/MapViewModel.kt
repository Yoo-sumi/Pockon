package com.example.giftbox.ui.map

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
class MapViewModel @Inject constructor(
    private val brandSearchRepository: BrandSearchRepository,
    private val giftRepository: GiftRepository
) : ViewModel() {

    private val _displayInfoList = MutableLiveData<ArrayList<Pair<Document, List<Gift>>>>()
    val displayInfoList: LiveData<ArrayList<Pair<Document, List<Gift>>>> = _displayInfoList

    private var giftList = listOf<Gift>()
    private var brandInfoList = mutableMapOf<String, List<Document>>()
    private var nearestDoc: Document? = null

    fun getAllGift() {
        // 기프티콘 정보 가져오기(로컬)
        viewModelScope.launch(Dispatchers.IO) {
            giftList = giftRepository.getAllGift()
            getAllBrands() // 키워드별 브랜드 위치 정보 가져오기(로컬)
        }
    }

    private fun getAllBrands() {
        // 로컬 가져오기
        viewModelScope.launch(Dispatchers.IO) {
            // keyword, documents
            brandInfoList = brandSearchRepository.getAllBrands()
            mappingInfo()
        }
    }

    // 브랜드별 사용 가능 기프티콘 매핑하기
    private fun mappingInfo() {
        val mappingList = mutableMapOf<Document, MutableSet<String>>()
        nearestDoc = null

        // keyword, documents
        brandInfoList = brandSearchRepository.getAllBrands()

        brandInfoList.forEach { (keyword, documents) ->
            documents.forEach { document ->
                // 가장 가까운곳 뽑아내기
                if (nearestDoc == null) nearestDoc = document
                else if (nearestDoc!!.distance.toDouble() > document.distance.toDouble()) nearestDoc = document

                if (mappingList.keys.contains(document)) mappingList[document]?.add(keyword)
                else mappingList[document] = mutableSetOf(keyword)
            }
        }

        val markerInGiftList = ArrayList<Pair<Document, List<Gift>>>()
        mappingList.forEach { (document, keywordList) ->
            val filterGiftList = giftList.filter { keywordList.contains(it.brand) }
            markerInGiftList.add(Pair(document, filterGiftList))
        }

        _displayInfoList.postValue(markerInGiftList)
    }

    fun getNearestDoc() = this.nearestDoc

}