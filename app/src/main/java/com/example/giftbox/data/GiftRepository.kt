package com.example.giftbox.data

import android.graphics.Bitmap
import com.example.giftbox.GiftEntity
import com.example.giftbox.data.local.GiftLocalDataSource
import com.example.giftbox.data.remote.GiftDataSource
import com.example.giftbox.data.remote.GiftPhotoDataSource
import com.example.giftbox.model.Gift
import javax.inject.Inject

class GiftRepository @Inject constructor(
    private val giftDataSource: GiftDataSource,
    private val giftPhotoDataSource: GiftPhotoDataSource,
    private val giftLocalDataSource: GiftLocalDataSource
) {
    fun addGift(gift: Gift, photo: Bitmap, onComplete: (Boolean) -> Unit) {
        giftDataSource.uploadData(gift) { id ->
            if (id == null) {
                onComplete(false)
            } else {
                giftPhotoDataSource.uploadData(photo, gift.uid, id) {
                    onComplete(it)
                }
            }
        }
    }

    fun getGift(uid: String, id:String, onComplete: (Gift?) -> Unit) {
        giftDataSource.loadData(id) { gift ->
            giftPhotoDataSource.downloadData(uid, id) { photo ->
                onComplete(gift?.copy(photo = photo))
            }
        }
    }

    fun getAllGift(uid: String, onComplete: (List<Gift>) -> (Unit)) {
        val giftList = mutableListOf<Gift>()
        giftDataSource.loadAllData(uid) { gifts ->
            if (gifts.isEmpty()) onComplete(giftList)
            gifts.forEach { gift ->
                giftPhotoDataSource.downloadData(gift.uid, gift.id) { photo ->
                    giftList.add(gift.copy(photo = photo))
                    if (gifts.size == giftList.size) {
                        onComplete(giftList)
                    }
                }
            }
        }
    }

    fun updateGift(gift: Gift, isPhoto: Boolean, onComplete: (Boolean) -> Unit){
        val photo = gift.photo
        giftDataSource.updateData(gift.copy(photo = null)) { result ->
            if (result) {
                if (!isPhoto) {
                    onComplete(true)
                } else if (photo != null) {
                    giftPhotoDataSource.uploadData(photo, gift.uid, gift.id) {
                        onComplete(it)
                    }
                } else {
                    onComplete(false)
                }
            } else {
                onComplete(false)
            }
        }
    }

    fun removeGift(uid: String, document: String, onComplete: (Boolean) -> Unit) {
        giftPhotoDataSource.removeData(uid, document) { result ->
            if (!result) {
                onComplete(false) // 사진 삭제 실패이면 정보 삭제 X
            } else {
                giftDataSource.deleteData(document) {
                    onComplete(it)
                }
            }
        }
    }

    /* 로컬 */
    fun insertGift(gift: Gift) {
        val giftEntity = GiftEntity(id = gift.id, uid = gift.uid, photo = gift.photo, name = gift.name, brand = gift.brand, endDt = gift.endDt, addDt = gift.addDt, memo = gift.memo, usedDt = gift.usedDt, cash = gift.cash)
        giftLocalDataSource.insertGift(giftEntity)
    }

    fun getAllGift() = giftLocalDataSource.getAllGift()

    fun getGift(id: String) = giftLocalDataSource.getGift(id)

    fun getAllUsedGift() = giftLocalDataSource.getAllUsedGift()

    fun deleteGift(id: String) = giftLocalDataSource.deleteGift(id)

    fun deleteGifts(ids: List<String>) = giftLocalDataSource.deleteGifts(ids)

    fun deleteAllGift() = giftLocalDataSource.deleteAllGift()

    fun deleteAllAndInsertGifts(gifts: List<Gift>) {
        val giftEntityList = gifts.map { gift ->
            GiftEntity(id = gift.id, uid = gift.uid, photo = gift.photo, name = gift.name, brand = gift.brand, endDt = gift.endDt, addDt = gift.addDt, memo = gift.memo, usedDt = gift.usedDt, cash = gift.cash)
        }
        giftLocalDataSource.deleteAllAndInsertGifts(giftEntityList)
    }
}