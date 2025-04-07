package com.example.giftbox.data.local.gift

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [GiftEntity::class],
    version = 1
)
abstract class GiftDatabase : RoomDatabase() {

    abstract fun giftDao(): GiftDao
}