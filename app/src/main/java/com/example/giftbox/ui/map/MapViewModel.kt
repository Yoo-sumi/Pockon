package com.example.giftbox.ui.map

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.giftbox.data.BrandsRepository
import com.example.giftbox.model.Document
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val brandSearchRepository: BrandsRepository
) : ViewModel() {


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

    fun getDocumentList() = documentList
}