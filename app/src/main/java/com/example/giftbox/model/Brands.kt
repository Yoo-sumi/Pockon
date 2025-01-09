package com.example.giftbox.model

import com.google.gson.annotations.SerializedName

data class Brands(
    var documents: List<Document>
)

data class Document(
    var id: String = "",
    @SerializedName("place_name")
    var placeName: String = "",
    var distance: String = "",
    var x: String = "",
    var y: String = ""
)