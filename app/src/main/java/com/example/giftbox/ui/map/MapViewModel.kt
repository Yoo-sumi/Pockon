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


    private val _giftList = MutableLiveData<List<Gift>>()
    val giftList: LiveData<List<Gift>> = _giftList

    private val _keywordList = MutableLiveData<List<String>>()
    val keywordList: LiveData<List<String>> = _keywordList

    private var documentList: List<List<Document>> = listOf()

    private var brandInfoList: Map<Document, ArrayList<String>> = mapOf()

    fun getAllBrands() {
        // 로컬 가져오기
        viewModelScope.launch(Dispatchers.IO) {
            val brands = brandSearchRepository.getAllBrands()
            _keywordList.postValue(brands.first)
            documentList = brands.second
        }
    }

    fun getAllGift() {
        // 로컬 가져오기
        viewModelScope.launch(Dispatchers.IO) {
            val giftList = giftRepository.getAllGift()
            _giftList.postValue(giftList)
        }
    }

    fun mappingInfo() {
        val infoList = mutableMapOf<Document, ArrayList<String>>()
        documentList.forEachIndexed { index, document ->
            Log.d("정보정보", "키워드> ${_keywordList.value?.get(index)}")
            document.forEach {
                if (infoList.keys.contains(it)) infoList[it]?.add(_keywordList.value?.get(index) ?: "")
                else infoList[it] = arrayListOf(_keywordList.value?.get(index) ?: "")

                Log.d("정보정보", "${it.id} / ${it.placeName} / ${it.x}/ ${it.y}/ ${it.distance}")
            }
        }

        infoList.keys.forEach { key ->
            val aa = infoList[key]
            Log.d("정보정보2", "${key.id} / ${key.placeName} / ${key.x}/ ${key.y}/ ${key.distance}")
            aa?.forEach { k ->
                Log.d("정보정보2", "키워드> ${k}")

            }
        }
    }

    fun getDocumentList() = documentList
}