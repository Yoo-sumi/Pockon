package com.example.giftbox.data

import android.location.Location
import com.example.giftbox.data.local.BrandDataSource
import com.example.giftbox.data.remote.BrandSearchDataSource
import com.example.giftbox.model.Brands
import javax.inject.Inject

class BrandSearchRepository @Inject constructor(
    private val brandSearchDataSource: BrandSearchDataSource,
    private val brandDataSource: BrandDataSource
) {
    fun getBrandInfoList(location: Location?, brandNames: ArrayList<String>, onComplete: (ArrayList<String>, ArrayList<Brands?>) -> Unit) {
        val brandList = ArrayList<Brands?>()
        val keywordList = ArrayList<String>()
        brandNames.forEachIndexed { i, a ->
            brandSearchDataSource.getBrandInfo(location, a) { keyword, brand ->
                brandList.add(brand)
                keywordList.add(keyword)
                if (brandList.size == brandNames.size) {
                    onComplete(keywordList, brandList)
                    for (k in 0..keywordList.lastIndex) {
                        brandList[k]?.let { brands ->
                            brandDataSource.insertBrands(keywordList[k], brands.documents) // 브랜드별 위치정보 저장
                        }
                    }
                }
            }
        }
    }
}