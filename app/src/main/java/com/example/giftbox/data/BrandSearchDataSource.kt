package com.example.giftbox.data

import android.location.Location
import com.example.giftbox.BuildConfig
import com.example.giftbox.KaKaoSearchAPI
import com.example.giftbox.model.Brand
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BrandSearchDataSource {

    private val REST_API_KEY = "KakaoAK ${BuildConfig.KAKAO_REST_API_KEY}"
    private val BASE_URL_NAVER_API = "https://dapi.kakao.com/"

    val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL_NAVER_API)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun getBrandInfo(location: Location?, brandName: String, onComplete: (String, Brand?) -> Unit) {
        val api = retrofit.create(KaKaoSearchAPI::class.java)
        val call = api.searchBrand(REST_API_KEY, brandName , x = location?.longitude.toString(), y = location?.latitude.toString())

        call.enqueue(object : Callback<Brand> {
            override fun onResponse(call: Call<Brand>, response: Response<Brand>) {
                if (response.isSuccessful && response.body() != null) onComplete(brandName, response.body())
                else onComplete(brandName, null)
            }

            override fun onFailure(call: Call<Brand>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }
}