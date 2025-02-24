package com.example.giftbox.ui.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class RoomTypeConverter {

    // Bitmap byte[]로 변환
    @TypeConverter
    fun fromBitmap(bitmap: Bitmap?) : ByteArray? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    // byte[]를 Bitmap으로 변환
    @TypeConverter
    fun toBitmap(byteArray: ByteArray) : Bitmap? {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

}
