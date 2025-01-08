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

    fun getAllGift() = giftDao.getAllGift()

    fun deleteAllGift() = giftDao.deleteAllGift()

}