package com.example.giftbox.ui.utils

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream

// Uri -> Bitmap
fun getBitmapFromUri(contentResolver: ContentResolver, uri: Uri): Bitmap? {
    return try {
        // URI에서 InputStream을 가져옵니다.
        val inputStream = contentResolver.openInputStream(uri)

        // InputStream을 통해 Bitmap을 디코딩하여 반환
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Bitmap -> Uri
fun getBytesFromBitmap(bitmap: Bitmap): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream) // JPEG로 압축
    return byteArrayOutputStream.toByteArray()
}