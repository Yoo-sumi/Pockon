package com.example.giftbox.data.local.brand

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.giftbox.data.model.Document

@Entity
data class BrandEntity(
    @PrimaryKey val keyword: String,
    val documents: List<Document>
)