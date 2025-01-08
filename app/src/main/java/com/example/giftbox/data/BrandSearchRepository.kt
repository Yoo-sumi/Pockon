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
    fun searchBrandInfoList(location: Location?, brandNames: ArrayList<String>, onComplete: (ArrayList<String>, ArrayList<Brands?>) -> Unit) {
        val brandList = ArrayList<Brands?>()
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

    fun insertBrands(keyword: String, documents: List<Document>) {
        brandDataSource.insertBrands(keyword, documents)
    }

    fun getAllBrands(): Pair<ArrayList<String>, ArrayList<List<Document>>> {
        val keywordList = ArrayList<String>()
        val documentList = ArrayList<List<Document>>()
        brandDataSource.getAllBrands().forEach {
            keywordList.add(it.keyword)
            documentList.add(it.documents)
        }
        return Pair(keywordList, documentList)
    }
}