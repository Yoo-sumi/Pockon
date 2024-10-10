package com.example.giftbox.model

import android.net.Uri

data class Gift(
    val uid: String = "",
    val photo: Uri? = null,
    val name: String = "",
    val brand: String = "",
    val endDate: String = "",
    val memo: String = ""
)
