package com.example.giftbox.ui.map

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


    fun getDocumentList() = documentList
}