package com.sumi.pockon.util

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

// Uri -> Bitmap
fun getBitmapFromUri(
    contentResolver: ContentResolver,
    uri: Uri,
    reqWidth: Int = 1024,
    reqHeight: Int = 1024
): Bitmap? {
    val inputStream = contentResolver.openInputStream(uri) ?: return null

    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeStream(inputStream, null, options)
    inputStream.close()

    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
    options.inJustDecodeBounds = false

    val newInputStream = contentResolver.openInputStream(uri) ?: return null
    val bitmap = BitmapFactory.decodeStream(newInputStream, null, options)
    newInputStream.close()

    // EXIF 정보를 기반으로 이미지 회전 보정
    return bitmap?.let { rotateBitmapIfRequired(contentResolver, uri, it) }
}

fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2

        while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

fun rotateBitmapIfRequired(contentResolver: ContentResolver, uri: Uri, bitmap: Bitmap): Bitmap {
    val inputStream = contentResolver.openInputStream(uri) ?: return bitmap
    val exif = ExifInterface(inputStream)
    inputStream.close()

    val orientation =
        exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    val rotationAngle = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }

    return if (rotationAngle != 0f) {
        val matrix = Matrix().apply { postRotate(rotationAngle) }
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } else {
        bitmap
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