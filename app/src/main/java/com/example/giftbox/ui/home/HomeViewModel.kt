package com.example.giftbox.ui.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.giftbox.data.BrandSearchRepository
import com.example.giftbox.data.GiftRepository
import com.example.giftbox.model.Document
import com.example.giftbox.model.Gift
import com.example.giftbox.ui.utils.loadImageFromPath
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
) : ViewModel() {

    private var giftList:List<Gift> = listOf()

    private val _displayGiftList = mutableStateOf<List<Pair<Gift, Document>>>(listOf())
    val displayGiftList: State<List<Pair<Gift, Document>>> = _displayGiftList

    private val _closeToGiftList = mutableStateOf<List<Gift>>(listOf())
    val closeToGiftList: State<List<Gift>> = _closeToGiftList

    private var longitude: Double? = null
    private var latitude: Double? = null

    init {
        observeGiftList()
    }

    // 로컬 기프티콘 목록 변화 감지해서 가져오기
    private fun observeGiftList() {
        viewModelScope.launch(Dispatchers.IO) {
            giftRepository.getAllGift().collectLatest { allGift ->
                if (allGift.isNotEmpty()) {
                    giftList = allGift.map { gift ->
                        Gift(id = gift.id, uid = gift.uid, photo = loadImageFromPath(gift.photoPath), name = gift.name, brand = gift.brand, endDt = gift.endDt, addDt = gift.addDt, memo = gift.memo, usedDt = gift.usedDt, cash = gift.cash)
                    }

                    // 기한 임박 기프티콘 목록(TOP 30)
                    val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
                    _closeToGiftList.value = giftList.sortedBy { gift -> dateFormat.parse(gift.endDt)?.time }.filterIndexed { index, gift -> index < 30 }

                    getBrandInfoList() // 브랜드 검색
                } else {
                    _closeToGiftList.value = listOf()
                    _displayGiftList.value = listOf()
                    giftList = listOf()
                }
            }
        }
    }

    // 브랜드 검색 후 로컬에 저장
    private fun getBrandInfoList() {
        val allList: ArrayList<Pair<Gift, Document>> = arrayListOf()

        val brandNames = ArrayList<String>()
        giftList.forEach {
            if (!brandNames.contains(it.brand)) brandNames.add(it.brand)
        }
        if (brandNames.isNotEmpty() && longitude != null && latitude != null)  {
            brandSearchRepository.searchBrandInfoList(longitude!!, latitude!!, brandNames) { brandInfoList ->
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
                _displayGiftList.value = allList

                // 로컬 저장
                viewModelScope.launch(Dispatchers.IO) {
                    brandSearchRepository.deleteAllBrands()
                    brandInfoList.forEach { (keyword, documents) ->
                        if (documents != null) brandSearchRepository.insertBrands(keyword, documents) // 키워드별 브랜드 위치정보 저장
                    }
                }
            }
        }
    }

    fun setLocation(longitude: Double?, latitude: Double?) {
        this.longitude = longitude
        this.latitude = latitude

        if (giftList.isNotEmpty()) {
            val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
            _closeToGiftList.value = giftList.sortedBy { gift -> dateFormat.parse(gift.endDt)?.time }.filterIndexed { index, gift -> index < 30 }
            getBrandInfoList()
        } else {
            _closeToGiftList.value = listOf()
            _displayGiftList.value = listOf()
        }
    }
}