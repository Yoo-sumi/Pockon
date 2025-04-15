package com.sumi.pockon.data.local.brand

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.sumi.pockon.data.model.Document
import com.google.gson.Gson

@Database(
    entities = [BrandEntity::class],
    version = 1
)
@TypeConverters(DateListConverters::class)
abstract class BrandDatabase : RoomDatabase() {

    abstract fun brandDao(): BrandDao
}

class DateListConverters {

    @TypeConverter
    fun listToJson(value: List<Document>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun jsonToList(value: String): List<Document>? {
        return Gson().fromJson(value, Array<Document>::class.java)?.toList()
    }
}