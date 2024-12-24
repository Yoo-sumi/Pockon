package com.example.giftbox.data

import android.util.Log
import com.example.giftbox.BuildConfig
import com.example.giftbox.KaKaoSearchAPI
import com.example.giftbox.model.Brand
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject


class SearchDataSource @Inject constructor(
) {

    val REST_API_KEY = BuildConfig.KAKAO_REST_API_KEY
    val BASE_URL_NAVER_API = "https://dapi.kakao.com//"

    val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL_NAVER_API)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun searchBrand() {
        val api = retrofit.create(KaKaoSearchAPI::class.java)
        val call = api.getSearchBrand(REST_API_KEY, "스타벅스")

        call.enqueue(object : Callback<Brand> {
            override fun onResponse(call: Call<Brand>, response: Response<Brand>) {
                if (response.isSuccessful && response.body() != null) {
                    response.body()?.documents?.forEach {
                    }

                }

            }

            override fun onFailure(call: Call<Brand>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }
}