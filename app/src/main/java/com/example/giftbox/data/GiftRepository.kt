package com.example.giftbox.data

import com.example.giftbox.model.Gift
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GiftRepository @Inject constructor(
    private val giftDataSource: GiftDataSource,
) {
    fun addGift(gjft: Gift): Flow<String?> {
        return giftDataSource.uploadData(gjft)
    }

    fun getGift(document:String): Flow<Gift?> {
        return giftDataSource.loadData(document)
    }

    fun getAllGift(uid: String): Flow<List<Gift>> {
        return giftDataSource.loadAllData(uid)
    }

    fun updateGift(gift: Gift): Flow<Boolean> {
        return giftDataSource.updateData(gift)
    }
}