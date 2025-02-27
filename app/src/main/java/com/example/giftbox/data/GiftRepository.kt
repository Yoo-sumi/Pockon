package com.example.giftbox.data

import com.example.giftbox.GiftEntity
import com.example.giftbox.data.local.GiftLocalDataSource
import com.example.giftbox.data.remote.GiftDataSource
import com.example.giftbox.data.remote.GiftPhotoDataSource
import com.example.giftbox.model.Gift
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class GiftRepository @Inject constructor(
    private val giftDataSource: GiftDataSource,
    private val giftPhotoDataSource: GiftPhotoDataSource,
    private val giftLocalDataSource: GiftLocalDataSource
) {
    fun addGift(isGuestMode: Boolean, gift: Gift, onComplete: (String?) -> Unit) {
        if (isGuestMode) {
            val currentDate = Date()
            val formatter = SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault())
            onComplete(formatter.format(currentDate))
            return
        }

        val photo = gift.photo
        giftDataSource.uploadData(gift.copy(photo = null)) { id ->
            if (id == null || photo == null) {
                onComplete(null)
            } else {
                giftPhotoDataSource.uploadData(photo, gift.uid, id) {
                    onComplete(id)
                }
            }
        }
    }

    fun getAllGift(uid: String, onComplete: (List<Gift>) -> (Unit)) {
        giftDataSource.loadAllData(uid) { gifts ->
            if (gifts.isEmpty()) {
                onComplete(listOf())
            } else {
                giftPhotoDataSource.downloadAllData(uid, gifts.map { it.id }) { photoMap ->
                    onComplete(gifts.map { it.copy(photo = photoMap[it.id]) })
                }
            }
        }
    }

    fun updateGift(isGuestMode: Boolean, gift: Gift, isPhoto: Boolean, onComplete: (Boolean) -> Unit){
        if (isGuestMode) {
            onComplete(true)
            return
        }

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

    fun removeGift(isGuestMode: Boolean, uid: String, document: String, onComplete: (Boolean) -> Unit) {
        if (isGuestMode) {
            onComplete(true)
            return
        }

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

    fun removeGifts(isGuestMode: Boolean, uid: String, documents: List<String>, onComplete: (Boolean) -> Unit) {
        if (isGuestMode) {
            onComplete(true)
            return
        }

        giftPhotoDataSource.removeMultipleData(uid, documents) { result ->
            if (!result) {
                onComplete(false) // 사진 삭제 실패이면 정보 삭제 X
            } else {
                giftDataSource.deleteMultipleData(documents) {
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