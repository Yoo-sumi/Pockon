package com.sumi.pockon.data.local.brand

import com.sumi.pockon.data.model.Document
import javax.inject.Inject

class BrandLocalDataSource @Inject constructor(
    private val brandDao: BrandDao
) {

    fun insertBrands(keyword: String, documents: List<Document>) {
        val item = BrandEntity(keyword, documents)
        brandDao.insertBrands(item)
    }

    fun getAllBrands() = brandDao.getAllBrands()

    fun deleteAllBrands() = brandDao.deleteAllBrands()
}