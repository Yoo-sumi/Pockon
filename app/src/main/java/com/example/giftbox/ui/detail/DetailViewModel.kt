package com.example.giftbox.ui.detail

import android.graphics.Bitmap
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.giftbox.R
import com.example.giftbox.model.Gift
import com.example.giftbox.ui.utils.stringTobitmap
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor() : ViewModel() {
    private val _photo = mutableStateOf<Bitmap?>(null)
    val photo: State<Bitmap?> = _photo
    private val _name = mutableStateOf("")
    val name: State<String> = _name
    private val _brand = mutableStateOf("")
    val brand: State<String> = _brand
    private val _endDate = mutableStateOf("")
    val endDate: State<String> = _endDate
    private val _memo = mutableStateOf("")
    val memo: State<String> = _memo

    fun setGift(gift: Gift) {
        _photo.value = stringTobitmap(gift.photo)
        _name.value = gift.name
        _brand.value = gift.brand
        _endDate.value = gift.endDate
        _memo.value = gift.memo
    }

    fun getLabelList(index: Int): Int {
        return when (index) {
            0 -> R.string.txt_name
            1 -> R.string.txt_brand
            2 -> R.string.txt_end_date
            else -> R.string.txt_memo
        }
    }
}