package com.example.giftbox.data.local.gift

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class GiftEntity(
    @PrimaryKey val id: String = "",
    val uid: String = "",
    val photoPath: String = "",
    val name: String = "",
    val brand: String = "",
    val endDt: String = "",
    val addDt: String = "",
    val memo: String = "",
    val usedDt: String = "",
    val cash: String = ""
)