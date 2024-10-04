package com.example.giftbox

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val giftRepository: GiftRepository
) : ViewModel() {
    private val _isLoad = MutableStateFlow(false)
    val isLoad: StateFlow<Boolean> = _isLoad

    fun addGift(inputDataList: List<String>) {
        val gift = Gift(name = inputDataList[0], brand = inputDataList[1], endDate = inputDataList[2], memo = inputDataList[3])
        giftRepository.addGift(gift)
    }
}