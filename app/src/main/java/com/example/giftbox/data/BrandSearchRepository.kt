package com.example.giftbox.data

import com.example.giftbox.data.local.BrandDataSource
import com.example.giftbox.data.remote.BrandSearchDataSource
import com.example.giftbox.model.Document
import javax.inject.Inject

class BrandSearchRepository @Inject constructor(
    private val brandSearchDataSource: BrandSearchDataSource,
    private val brandDataSource: BrandDataSource
) {
    fun searchBrandInfoList(longitude: Double, latitude: Double, brandNames: ArrayList<String>, onComplete: (MutableMap<String, List<Document>?>) -> Unit) {
        val brandInfoList = mutableMapOf<String, List<Document>?>()
        brandNames.forEachIndexed { i, a ->
            brandSearchDataSource.getBrandInfo(longitude, latitude, a) { keyword, brand ->
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

    fun deleteAllBrands() {
        brandDataSource.deleteAllBrands()
    }
}