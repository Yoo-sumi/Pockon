package com.example.giftbox.model

import com.google.gson.annotations.SerializedName

data class Brand(
    var documents: List<Items>
)

data class Items(
    var place_name: String = "",
    var distance: String = "",
    var x: String = "",
    var y: String = ""
)