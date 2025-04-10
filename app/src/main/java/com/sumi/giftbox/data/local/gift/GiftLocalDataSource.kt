package com.sumi.giftbox.data.local.gift

import javax.inject.Inject

class GiftLocalDataSource @Inject constructor(
    private val giftDao: GiftDao
) {

    fun insertGift(gift: GiftEntity) {
        giftDao.insertGift(gift)
    }

    fun getAllGift() = giftDao.getAllGift()

    fun getGift(id: String) = giftDao.getGift(id)

    fun getAllUsedGift() = giftDao.getAllUsedGift()

    fun deleteGift(id: String) = giftDao.deleteGift(id)

    fun deleteGifts(ids: List<String>) = giftDao.deleteGifts(ids)

    fun deleteAllGift() = giftDao.deleteAllGift()

    fun deleteAllAndInsertGifts(gifts: List<GiftEntity>) = giftDao.deleteAllAndInsertGifts(gifts)
}