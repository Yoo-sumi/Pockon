package com.example.giftbox.data

import android.net.Uri
import com.example.giftbox.model.Gift
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GiftRepository @Inject constructor(
    private val giftDataSource: GiftDataSource,
    private val giftPhotoDataSource: GiftPhotoDataSource
) {
    fun addGift(gjft: Gift): Flow<String?> {
        return giftDataSource.uploadData(gjft)
    }

    fun getGift(uid:String, onComplete: (Gift?) -> Unit) {
        giftDataSource.loadData(uid, onComplete)
    }

    fun addPhoto(docId: String, uri: Uri) {
        giftPhotoDataSource.uploadImage(docId, uri)
    }
}