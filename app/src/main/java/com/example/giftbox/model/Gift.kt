package com.example.giftbox.model

import java.io.Serializable

data class Gift(
    var id: String = "",
    val uid: String = "",
    val photo: String = "",
    val name: String = "",
    val brand: String = "",
    val endDt: String = "",
    val addDt: String = "",
    val memo: String = "",
    val usedDt: String = ""
) : Serializable
