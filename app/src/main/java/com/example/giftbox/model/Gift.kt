package com.example.giftbox.model

data class Gift(
    var document: String = "",
    val uid: String = "",
    val photo: String = "",
    val name: String = "",
    val brand: String = "",
    val endDt: String = "",
    val addDt: String = "",
    val memo: String = "",
    val usedDt: String = ""
)
