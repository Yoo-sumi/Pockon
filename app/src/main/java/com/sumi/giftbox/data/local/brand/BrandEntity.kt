package com.sumi.giftbox.data.local.brand

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sumi.giftbox.data.model.Document

@Entity
data class BrandEntity(
    @PrimaryKey val keyword: String,
    val documents: List<Document>
)