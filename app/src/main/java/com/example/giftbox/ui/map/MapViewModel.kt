package com.example.giftbox.ui.map

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.giftbox.data.repository.BrandSearchRepository
import com.example.giftbox.data.repository.GiftRepository
import com.example.giftbox.data.model.Document
import com.example.giftbox.data.model.Gift
import com.example.giftbox.util.loadImageFromPath
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val brandSearchRepository: BrandSearchRepository,
    private val giftRepository: GiftRepository
) : ViewModel() {

    private val _displayInfoList = mutableStateOf<List<Pair<Document, List<Gift>>>>(listOf())
    val displayInfoList: State<List<Pair<Document, List<Gift>>>> = _displayInfoList

    private var giftList = listOf<Gift>()
    private var brandInfoList = mutableMapOf<String, List<Document>>()
    private var nearestDoc: Document? = null

    init {
        observeGiftList()
    }

    // 로컬 기프티콘 목록 변화 감지해서 가져오기
    private fun observeGiftList() {
        viewModelScope.launch(Dispatchers.IO) {
            giftRepository.getAllGift().take(1).collectLatest { allGift -> // 지도에서는 실시간 갱신 안함 > 1로제한
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
                            cash = gift.cash
                        )
                    }
                    getAllBrands() // 키워드별 브랜드 위치 정보 가져오기(로컬)
                } else {
                    // 기프티콘 없음
                    giftList = listOf()
                }
            }
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

        brandInfoList.forEach { (keyword, documents) ->
            documents.forEach { document ->
                // 가장 가까운곳 뽑아내기
                if (nearestDoc == null) nearestDoc = document
                else if (nearestDoc!!.distance.toDouble() > document.distance.toDouble()) nearestDoc =
                    document

                if (mappingList.keys.contains(document)) mappingList[document]?.add(keyword)
                else mappingList[document] = mutableSetOf(keyword)
            }
        }

        val markerInGiftList = ArrayList<Pair<Document, List<Gift>>>()
        mappingList.forEach { (document, keywordList) ->
            val filterGiftList = giftList.filter { keywordList.contains(it.brand) }
            markerInGiftList.add(Pair(document, filterGiftList))
        }

        _displayInfoList.value = markerInGiftList
    }

    fun getNearestDoc() = this.nearestDoc
}