package com.example.giftbox.data.remote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.giftbox.ui.utils.getBytesFromBitmap
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.StorageReference
import javax.inject.Inject


class GiftPhotoDataSource @Inject constructor(
    private val storageRef: StorageReference
) {
    fun uploadData(data: Bitmap, uid: String, id: String, onComplete: (Boolean) -> Unit) {
        storageRef.child("${uid}/${id}.jpeg")
            .putBytes(getBytesFromBitmap(data))
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun downloadData(uid: String, id: String?, onComplete: (Bitmap?) -> Unit) {
        storageRef.child("${uid}/${id}.jpeg")
            .getBytes(Long.MAX_VALUE)
            .addOnSuccessListener { bytes ->
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                onComplete(bitmap)
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }


    fun downloadAllData(uid: String, ids: List<String>, onComplete: (Map<String, Bitmap?>) -> Unit) {
        val tasks: List<Task<ByteArray>> = ids.map { id ->
            storageRef.child("${uid}/${id}.jpeg").getBytes(Long.MAX_VALUE)
        }

        // 모든 다운로드가 완료될 때까지 기다린 후 결과를 처리
        Tasks.whenAllSuccess<ByteArray>(*tasks.toTypedArray()).addOnSuccessListener { results ->
            val bitmaps = ids.zip(results) { id, bytes ->
                id to BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }.toMap() // id와 비트맵을 Map으로 변환
            onComplete(bitmaps)  // 결과를 Map 형태로 전달
        }.addOnFailureListener {
            onComplete(emptyMap())  // 다운로드 실패시 빈 Map 반환
        }
    }

    fun removeData(uid: String, id: String, onComplete: (Boolean) -> Unit) {
        storageRef.child("${uid}/${id}.jpeg")
            .delete()
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }
}