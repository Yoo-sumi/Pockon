package com.example.giftbox.data.local

import com.example.giftbox.BrandsDao
import com.example.giftbox.BrandsEntity
import com.example.giftbox.model.Brands
import com.example.giftbox.model.Document
import com.google.firebase.firestore.proto.NoDocument
import javax.inject.Inject

class BrandDataSource @Inject constructor(
    private val brandsDao: BrandsDao
) {

    fun insertBrands(keyword: String, documents: List<Document>) {
        val item = BrandsEntity(keyword, documents)
        brandsDao.insertBrands(item)
    }

    fun getAllBrands() = brandsDao.getAllBrands()

    fun deleteAllBrands() = brandsDao.deleteAllBrands()

}