package com.sumi.pockon.data.local.gift

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface GiftDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGift(item: GiftEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGifts(items: List<GiftEntity>)

    @Query("SELECT * FROM GiftEntity WHERE NULLIF(usedDt, '') IS NULL ORDER BY id")
    fun getAllGift(): Flow<List<GiftEntity>>

    @Query("SELECT * FROM GiftEntity WHERE id = :id")
    fun getGift(id: String): Flow<GiftEntity>

    @Query("SELECT * FROM GiftEntity WHERE NULLIF(usedDt, '') IS NOT NULL")
    fun getAllUsedGift(): Flow<List<GiftEntity>>

    @Query("DELETE FROM GiftEntity")
    fun deleteAllGift()

    @Query("DELETE FROM GiftEntity WHERE id = :id")
    fun deleteGift(id: String)

    @Query("DELETE FROM GiftEntity WHERE id IN (:ids)")
    fun deleteGifts(ids: List<String>)

    // 트랜잭션으로 묶기
    @Transaction
    fun deleteAllAndInsertGifts(gifts: List<GiftEntity>) {
        deleteAllGift()
        insertGifts(gifts)
    }
}