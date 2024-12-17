package com.example.giftbox.data

import android.net.Uri
import com.example.giftbox.model.Gift
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GiftRepository @Inject constructor(
    private val giftDataSource: GiftDataSource,
    private val giftPhotoDataSource: GiftPhotoDataSource
) {
    fun addGift(gift: Gift, photo: Uri, onComplete: (Boolean) -> Unit) {
        giftDataSource.uploadData(gift) { document ->
            giftPhotoDataSource.uploadData(photo, gift.uid, document) {
                onComplete(it)
            }
        }
    }

    fun getGift(uid: String, document:String, onComplete: (Gift?) -> Unit) {
        giftDataSource.loadData(document) { gift ->
            giftPhotoDataSource.downloadData(uid, document) { uri ->
                onComplete(gift?.copy(photo = uri.toString()))
            }
        }
    }

    fun getAllGift(uid: String, onComplete: (List<Gift>) -> (Unit)) {
        val giftList = mutableListOf<Gift>()
        giftDataSource.loadAllData(uid) { gifts ->
            if (gifts.isEmpty()) onComplete(giftList)
            gifts.forEachIndexed { idx, gift ->
                giftPhotoDataSource.downloadData(gift.uid, gift.document) { uri ->
                    giftList.add(gift.copy(photo = uri.toString()))
                    if (gifts.size == giftList.size) {
                        onComplete(giftList)
                    }
                }
            }
        }
    }

    fun updateGift(gift: Gift): Flow<Boolean> {
        return giftDataSource.updateData(gift)
    }

    fun removeGift(document: String): Flow<Boolean> {
        return giftDataSource.deleteData(document)
    }
}