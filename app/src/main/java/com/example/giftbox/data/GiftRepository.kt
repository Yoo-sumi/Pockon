package com.example.giftbox.data

import android.net.Uri
import com.example.giftbox.GiftEntity
import com.example.giftbox.data.local.GiftLocalDataSource
import com.example.giftbox.data.remote.GiftDataSource
import com.example.giftbox.data.remote.GiftPhotoDataSource
import com.example.giftbox.model.Gift
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GiftRepository @Inject constructor(
    private val giftDataSource: GiftDataSource,
    private val giftPhotoDataSource: GiftPhotoDataSource,
    private val giftLocalDataSource: GiftLocalDataSource
) {
    fun addGift(gift: Gift, photo: Uri, onComplete: (Boolean) -> Unit) {
        giftDataSource.uploadData(gift) { id ->
            giftPhotoDataSource.uploadData(photo, gift.uid, id) {
                onComplete(it)
            }
        }
    }

    fun getGift(uid: String, id:String, onComplete: (Gift?) -> Unit) {
        giftDataSource.loadData(id) { gift ->
            giftPhotoDataSource.downloadData(uid, id) { uri ->
                onComplete(gift?.copy(photo = uri.toString()))
            }
        }
    }

    fun getAllGift(uid: String, onComplete: (List<Gift>) -> (Unit)) {
        val giftList = mutableListOf<Gift>()
        giftDataSource.loadAllData(uid) { gifts ->
            if (gifts.isEmpty()) onComplete(giftList)
            gifts.forEachIndexed { idx, gift ->
                giftPhotoDataSource.downloadData(gift.uid, gift.id) { uri ->
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

    fun removeGift(uid: String, document: String, onComplete: (Boolean) -> Unit) {
        giftDataSource.deleteData(document) {
            giftPhotoDataSource.removeData(uid, document) {
                onComplete(it)
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

    fun deleteAllGift() = giftLocalDataSource.deleteAllGift()
}