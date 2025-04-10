package com.sumi.giftbox.data.local.brand

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BrandDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBrands(item: BrandEntity)

    @Query("SELECT * FROM BrandEntity")
    fun getAllBrands(): List<BrandEntity>

    @Query("DELETE FROM BrandEntity")
    fun deleteAllBrands()
}