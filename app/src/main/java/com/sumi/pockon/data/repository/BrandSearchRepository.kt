package com.sumi.pockon.data.repository

import com.sumi.pockon.data.local.brand.BrandLocalDataSource
import com.sumi.pockon.data.remote.brand.BrandSearchRemoteDataSource
import com.sumi.pockon.data.model.Document
import javax.inject.Inject

class BrandSearchRepository @Inject constructor(
    private val brandSearchRemoteDataSource: BrandSearchRemoteDataSource,
    private val brandLocalDataSource: BrandLocalDataSource
) {

    fun searchBrandInfoList(
        longitude: Double,
        latitude: Double,
        brandNames: ArrayList<String>,
        onComplete: (MutableMap<String, List<Document>?>) -> Unit
    ) {
        val brandInfoList = mutableMapOf<String, List<Document>?>()
        brandNames.forEachIndexed { i, a ->
            brandSearchRemoteDataSource.getBrandInfo(longitude, latitude, a) { keyword, brand ->
                brandInfoList[keyword] = brand?.documents
                if (brandInfoList.size == brandNames.size) {
                    onComplete(brandInfoList)
                }
            }
        }
    }

    fun insertBrands(keyword: String, documents: List<Document>) {
        brandLocalDataSource.insertBrands(keyword, documents)
    }

    fun getAllBrands(): MutableMap<String, List<Document>> {
        val brandInfoList = mutableMapOf<String, List<Document>>()
        brandLocalDataSource.getAllBrands().forEach {
            brandInfoList[it.keyword] = it.documents
        }
        return brandInfoList
    }

    fun deleteAllBrands() {
        brandLocalDataSource.deleteAllBrands()
    }
}