package com.example.giftbox

import com.example.giftbox.model.Brand
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KaKaoSearchAPI {
    @GET("v2/local/search/keyword.json")
    fun searchBrand(
        @Header("Authorization") authorization: String,
        @Query("query") query: String,
        @Query("category_group_code") category: String = CATEGORY_GROUP_CODE,
        @Query("x") x: String? = null,
        @Query("y") y: String? = null,
        @Query("radius") radius: Int? = 10000
    ): Call<Brand>

    companion object {
        private const val CATEGORY_GROUP_CODE = "MT1,CS2,CT1,AD5,FD6,CE7"
    }
}