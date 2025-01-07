package com.example.giftbox

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.giftbox.model.Brands
import com.example.giftbox.model.Document
import com.google.gson.Gson

@Database(
    entities = [BrandsEntity::class],
    version = 1
)
@TypeConverters(DateListConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun brandsDao(): BrandsDao
}

@Dao
interface BrandsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBrands(item: BrandsEntity)

    @Query("SELECT * FROM BrandsEntity")
    fun getAllBrands(): List<BrandsEntity>

    @Query("DELETE FROM BrandsEntity")
    fun deleteAllBrands()

}

@Entity
data class BrandsEntity(
    @PrimaryKey val keyword: String,
    val documents: List<Document>
)

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