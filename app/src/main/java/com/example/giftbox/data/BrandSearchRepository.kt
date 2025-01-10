package com.example.giftbox.data

import android.location.Location
import com.example.giftbox.data.local.BrandDataSource
import com.example.giftbox.data.remote.BrandSearchDataSource
import com.example.giftbox.model.Brands
import com.example.giftbox.model.Document
import javax.inject.Inject

class BrandSearchRepository @Inject constructor(
    private val brandSearchDataSource: BrandSearchDataSource,
    private val brandDataSource: BrandDataSource
) {
    fun searchBrandInfoList(location: Location?, brandNames: ArrayList<String>, onComplete: (MutableMap<String, List<Document>?>) -> Unit) {
        val brandInfoList = mutableMapOf<String, List<Document>?>()
        brandNames.forEachIndexed { i, a ->
            brandSearchDataSource.getBrandInfo(location, a) { keyword, brand ->
                brandInfoList[keyword] = brand?.documents
                if (brandInfoList.size == brandNames.size) {
                    onComplete(brandInfoList)
                }
            }
        }
    }

    fun insertBrands(keyword: String, documents: List<Document>) {
        brandDataSource.insertBrands(keyword, documents)
    }

    fun getAllBrands(): MutableMap<String, List<Document>> {
        val brandInfoList = mutableMapOf<String, List<Document>>()
        brandDataSource.getAllBrands().forEach {
            brandInfoList[it.keyword] = it.documents
        }
        return brandInfoList
    }
}