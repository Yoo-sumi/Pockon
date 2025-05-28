package com.sumi.pockon.ui.map

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.sumi.pockon.data.repository.BrandSearchRepository
import com.sumi.pockon.data.repository.GiftRepository
import com.sumi.pockon.data.model.Document
import com.sumi.pockon.data.model.Gift
import com.sumi.pockon.util.loadImageFromPath
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

    private val _displayInfoList = MutableLiveData<List<Pair<Document, List<Gift>>>>(listOf())
    val displayInfoList: LiveData<List<Pair<Document, List<Gift>>>> = _displayInfoList
    private val _cameraPosition = mutableStateOf<CameraPosition?>(null)
    val cameraPosition: State<CameraPosition?> = _cameraPosition

    private val _currentLocation = mutableStateOf<LatLng?>(null)
    val currentLocation: State<LatLng?> = _currentLocation
    // 선택된 마커 index
    private val _selectedMarkerIndex = mutableStateOf<Int?>(null)
    val selectedMarkerIndex: State<Int?> = _selectedMarkerIndex

    private var giftList = listOf<Gift>()
    private var brandInfoList = mutableMapOf<String, List<Document>>()
    private var nearestDoc: Document? = null
    private var pageIndex = 0
    private var isInitialCameraMoved = false

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
                            cash = gift.cash,
                            isFavorite = gift.isFavorite
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
                else if (nearestDoc!!.distance.toDouble() > document.distance.toDouble()) nearestDoc = document

                if (mappingList.keys.contains(document)) mappingList[document]?.add(keyword)
                else mappingList[document] = mutableSetOf(keyword)
            }
        }

        val markerInGiftList = ArrayList<Pair<Document, List<Gift>>>()
        mappingList.forEach { (document, keywordList) ->
            val filterGiftList = giftList.filter { keywordList.contains(it.brand) }
            val sortedList = filterGiftList.sortedWith(
                compareBy(
                    { it.brand },     // 브랜드명 순
                    { it.name },      // 상품명 순
                    { it.endDt ?: "99991231" } // null 또는 빈 값은 가장 마지막으로 정렬
                )
            )
            markerInGiftList.add(Pair(document, sortedList))
        }

        nearestDoc?.let { doc ->
            val index = markerInGiftList.indexOfFirst { it.first.id == doc.id }
            selectMarker(index)
        }

        _displayInfoList.postValue(markerInGiftList)
    }

    fun getNearestDoc() = this.nearestDoc

    fun updateCameraPosition(position: CameraPosition) {
        _cameraPosition.value = position
    }

    fun updateCurrentLocation(location: LatLng) {
        _currentLocation.value = location
    }

    fun selectMarker(index: Int) {
        _selectedMarkerIndex.value = index
    }

    fun getIsInitialCameraMoved() = this.isInitialCameraMoved

    fun setIsInitialCameraMoved(isInitialCameraMoved: Boolean) {
        this.isInitialCameraMoved = isInitialCameraMoved
    }

    fun setPageIndex(pageIndex: Int) {
        this.pageIndex = pageIndex
    }

    fun getPageIndex() = this.pageIndex
}