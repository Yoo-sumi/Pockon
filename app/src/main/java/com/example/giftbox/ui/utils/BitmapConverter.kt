package com.example.giftbox.ui.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

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

fun Bitmap.toByteArray(): ByteArray {
    val stream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 80, stream) // 압축률 80
    return stream.toByteArray()
}

fun ByteArray.toBitmap(): Bitmap {
    return BitmapFactory.decodeByteArray(this, 0, this.size)
}

fun saveBitmapToFile(bitmap: Bitmap?, context: Context): String {
    if (bitmap == null) return ""

    val directory = File(context.getExternalFilesDir(null), "images")
    if (!directory.exists()) {
        directory.mkdirs()  // 디렉토리가 없으면 생성
    }
    val file = File(context.cacheDir, "image_${System.currentTimeMillis()}.jpg")
    val outputStream = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
    outputStream.flush()
    outputStream.close()
    return file.absolutePath  // 파일 경로 반환
}

fun loadImageFromPath(imagePath: String): Bitmap? {
    val file = File(imagePath)
    return if (file.exists()) {
        BitmapFactory.decodeFile(file.absolutePath)  // 파일 경로로부터 Bitmap 객체 생성
    } else {
        null  // 파일이 존재하지 않으면 null 반환
    }
}