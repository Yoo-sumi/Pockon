package com.example.giftbox.data.local

import com.example.giftbox.BrandDao
import com.example.giftbox.BrandEntity
import com.example.giftbox.model.Document
import javax.inject.Inject

class BrandDataSource @Inject constructor(
    private val brandDao: BrandDao
) {

    fun insertBrands(keyword: String, documents: List<Document>) {
        val item = BrandEntity(keyword, documents)
        brandDao.insertBrands(item)
    }

    fun getAllBrands() = brandDao.getAllBrands()

    fun deleteAllBrands() = brandDao.deleteAllBrands()

}