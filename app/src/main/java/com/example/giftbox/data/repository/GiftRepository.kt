package com.example.giftbox.data.repository

import android.content.Context
import com.example.giftbox.data.local.gift.GiftEntity
import com.example.giftbox.data.local.gift.GiftLocalDataSource
import com.example.giftbox.data.model.Gift
import com.example.giftbox.data.remote.gift.GiftDataRemoteSource
import com.example.giftbox.data.remote.gift.GiftPhotoRemoteDataSource
import com.example.giftbox.util.saveBitmapToFile
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class GiftRepository @Inject constructor(
    private val giftDataRemoteSource: GiftDataRemoteSource,
    private val giftPhotoRemoteDataSource: GiftPhotoRemoteDataSource,
    private val giftLocalDataSource: GiftLocalDataSource,
    @ApplicationContext private val context: Context
) {

    fun addGift(isGuestMode: Boolean, gift: Gift, onComplete: (String?) -> Unit) {
        if (isGuestMode) {
            val currentDate = Date()
            val formatter = SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault())
            onComplete(formatter.format(currentDate))
            return
        }

        val photo = gift.photo
        giftDataRemoteSource.uploadData(gift.copy(photo = null)) { id ->
            if (id == null || photo == null) {
                onComplete(null)
            } else {
                giftPhotoRemoteDataSource.uploadData(photo, gift.uid, id) {
                    onComplete(id)
                }
            }
        }
    }

    fun getAllGift(uid: String, onComplete: (List<Gift>) -> (Unit)) {
        giftDataRemoteSource.loadAllData(uid) { gifts ->
            if (gifts.isEmpty()) {
                onComplete(listOf())
            } else {
                giftPhotoRemoteDataSource.downloadAllData(uid, gifts.map { it.id }) { photoMap ->
                    onComplete(gifts.map { it.copy(photo = photoMap[it.id]) })
                }
            }
        }
    }

    fun updateGift(
        isGuestMode: Boolean,
        gift: Gift,
        isPhoto: Boolean,
        onComplete: (Boolean) -> Unit
    ) {
        if (isGuestMode) {
            onComplete(true)
            return
        }

        val photo = gift.photo
        giftDataRemoteSource.updateData(gift.copy(photo = null)) { result ->
            if (result) {
                if (!isPhoto) {
                    onComplete(true)
                } else if (photo != null) {
                    giftPhotoRemoteDataSource.uploadData(photo, gift.uid, gift.id) {
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

    fun removeGift(
        isGuestMode: Boolean,
        uid: String,
        document: String,
        onComplete: (Boolean) -> Unit
    ) {
        if (isGuestMode) {
            onComplete(true)
            return
        }

        giftPhotoRemoteDataSource.removeData(uid, document) { result ->
            if (!result) {
                onComplete(false) // 사진 삭제 실패이면 정보 삭제 X
            } else {
                giftDataRemoteSource.deleteData(document) {
                    onComplete(it)
                }
            }
        }
    }

    fun removeGifts(
        isGuestMode: Boolean,
        uid: String,
        documents: List<String>,
        onComplete: (Boolean) -> Unit
    ) {
        if (isGuestMode) {
            onComplete(true)
            return
        }

        giftPhotoRemoteDataSource.removeMultipleData(uid, documents) { result ->
            if (!result) {
                onComplete(false) // 사진 삭제 실패이면 정보 삭제 X
            } else {
                giftDataRemoteSource.deleteMultipleData(documents) {
                    onComplete(it)
                }
            }
        }
    }

    /* 로컬 */
    fun insertGift(gift: Gift) {
        val giftEntity = GiftEntity(
            id = gift.id,
            uid = gift.uid,
            photoPath = saveBitmapToFile(gift.photo, context),
            name = gift.name,
            brand = gift.brand,
            endDt = gift.endDt,
            addDt = gift.addDt,
            memo = gift.memo,
            usedDt = gift.usedDt,
            cash = gift.cash
        )
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
            GiftEntity(
                id = gift.id,
                uid = gift.uid,
                photoPath = saveBitmapToFile(gift.photo, context),
                name = gift.name,
                brand = gift.brand,
                endDt = gift.endDt,
                addDt = gift.addDt,
                memo = gift.memo,
                usedDt = gift.usedDt,
                cash = gift.cash
            )
        }
        giftLocalDataSource.deleteAllAndInsertGifts(giftEntityList)
    }
}