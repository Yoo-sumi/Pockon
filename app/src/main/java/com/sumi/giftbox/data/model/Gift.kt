package com.sumi.giftbox.data.model

import android.graphics.Bitmap
import java.io.Serializable

data class Gift(
    var id: String = "",
    val uid: String = "",
    val photo: Bitmap? = null,
    val name: String = "",
    val brand: String = "",
    val endDt: String = "",
    val addDt: String = "",
    val memo: String = "",
    val usedDt: String = "",
    val cash: String = ""
) : Serializable
