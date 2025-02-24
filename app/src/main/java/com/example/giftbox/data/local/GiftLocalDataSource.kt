package com.example.giftbox.data.local

import com.example.giftbox.GiftDao
import com.example.giftbox.GiftEntity
import javax.inject.Inject

class GiftLocalDataSource @Inject constructor(
    private val giftDao: GiftDao
) {

    fun insertGift(gift: GiftEntity) {
        giftDao.insertGift(gift)
    }

    fun insertGifts(gifts: List<GiftEntity>) {
        giftDao.insertGifts(gifts)
    }

    fun getAllGift() = giftDao.getAllGift()

    fun getGift(id: String) = giftDao.getGift(id)

    fun getAllUsedGift() = giftDao.getAllUsedGift()

    fun deleteGift(id: String) = giftDao.deleteGift(id)

    fun deleteGifts(ids: List<String>) = giftDao.deleteGifts(ids)

    fun deleteAllGift() = giftDao.deleteAllGift()

}