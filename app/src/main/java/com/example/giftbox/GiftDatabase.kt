package com.example.giftbox

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT * FROM GiftEntity WHERE NULLIF(usedDt, '') IS NULL")
    fun getAllGift(): Flow<List<GiftEntity>>

    @Query("SELECT * FROM GiftEntity WHERE id = :id")
    fun getGift(id: String): Flow<GiftEntity>

    @Query("SELECT * FROM GiftEntity WHERE NULLIF(usedDt, '') IS NOT NULL")
    fun getAllUsedGift(): Flow<List<GiftEntity>>

    @Query("DELETE FROM GiftEntity")
    fun deleteAllGift()

    @Query("DELETE FROM GiftEntity WHERE id = :id")
    fun deleteGift(id: String)

}

@Entity
data class GiftEntity(
    @PrimaryKey val id: String = "",
    val uid: String = "",
    val photo: String = "",
    val name: String = "",
    val brand: String = "",
    val endDt: String = "",
    val addDt: String = "",
    val memo: String = "",
    val usedDt: String = "",
    val cash: String = ""
)