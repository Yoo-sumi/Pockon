package com.example.giftbox

import com.example.giftbox.model.Brand
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KaKaoSearchAPI {
    @GET("v2/local/search/keyword.json")
    fun getSearchBrand(
        @Header("Authorization") authorization: String,
        @Query("query") query: String,
        @Query("x") display: String? = null,
        @Query("y") start: String? = null,
        @Query("radius") sort: Int? = 20000
    ): Call<Brand>
}