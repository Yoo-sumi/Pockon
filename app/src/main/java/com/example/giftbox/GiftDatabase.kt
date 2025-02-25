package com.example.giftbox

import android.graphics.Bitmap
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverters
import com.example.giftbox.ui.utils.RoomTypeConverter
import kotlinx.coroutines.flow.Flow

@Database(
    entities = [GiftEntity::class],
    version = 1
)
@TypeConverters(RoomTypeConverter::class)
abstract class GiftDatabase : RoomDatabase() {
    abstract fun giftDao(): GiftDao
}

@Dao
interface GiftDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGift(item: GiftEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGifts(items: List<GiftEntity>)

    @Query("SELECT * FROM GiftEntity WHERE NULLIF(usedDt, '') IS NULL ORDER BY id")
    fun getAllGift(): Flow<List<GiftEntity>>

    @Query("SELECT * FROM GiftEntity WHERE id = :id")
    fun getGift(id: String): Flow<GiftEntity>

    @Query("SELECT * FROM GiftEntity WHERE NULLIF(usedDt, '') IS NOT NULL")
    fun getAllUsedGift(): Flow<List<GiftEntity>>

    @Query("DELETE FROM GiftEntity")
    fun deleteAllGift()

    @Query("DELETE FROM GiftEntity WHERE id = :id")
    fun deleteGift(id: String)

    @Query("DELETE FROM GiftEntity WHERE id IN (:ids)")
    fun deleteGifts(ids: List<String>)

    // 트랜잭션으로 묶기
    @Transaction
    fun deleteAllAndInsertGifts(gifts: List<GiftEntity>) {
        deleteAllGift()
        insertGifts(gifts)
    }
}

@Entity
data class GiftEntity(
    @PrimaryKey val id: String = "",
    val uid: String = "",
    val photo: Bitmap? = null,
    val name: String = "",
    val brand: String = "",
    val endDt: String = "",
    val addDt: String = "",
    val memo: String = "",
    val usedDt: String = "",
    val cash: String = ""
)