package com.example.giftbox.data

import android.location.Location
import android.util.Log
import com.example.giftbox.model.Brand
import javax.inject.Inject

class BrandSearchRepository @Inject constructor(
    private val brandSearchDataSource: BrandSearchDataSource,
) {
    fun getBrandInfoList(location: Location?, brandNames: ArrayList<String>, onComplete: (ArrayList<String>, ArrayList<Brand?>) -> Unit) {
        val brandList = ArrayList<Brand?>()
        val keywordList = ArrayList<String>()
        brandNames.forEachIndexed { i, a ->
            brandSearchDataSource.getBrandInfo(location, a) { keyword, brand ->
                brandList.add(brand)
                keywordList.add(keyword)
                if (brandList.size == brandNames.size) {
                    onComplete(keywordList, brandList)
                }
            }
        }
    }
}