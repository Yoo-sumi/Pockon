package com.example.giftbox

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.giftbox.model.Gift

@Database(
    entities = [GiftEntity::class],
    version = 1
)
abstract class GiftDatabase : RoomDatabase() {
    abstract fun giftDao(): GiftDao
}

@Dao
interface GiftDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGift(item: GiftEntity)

    @Query("SELECT * FROM GiftEntity")
    fun getAllGift(): List<GiftEntity>

    @Query("DELETE FROM GiftEntity")
    fun deleteAllGift()

}

@Entity
data class GiftEntity(
    val document: String = "",
    val uid: String = "",
    val photo: String = "",
    val name: String = "",
    val brand: String = "",
    val endDt: String = "",
    val addDt: String = "",
    val memo: String = "",
    val usedDt: String = ""
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}