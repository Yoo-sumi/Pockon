package com.sumi.pockon.data.remote.brand

import com.sumi.pockon.BuildConfig
import com.sumi.pockon.data.model.Brands
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BrandSearchRemoteDataSource {

    private val REST_API_KEY = "KakaoAK ${BuildConfig.KAKAO_REST_API_KEY}"
    private val BASE_URL_NAVER_API = "https://dapi.kakao.com/"

    val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL_NAVER_API)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun getBrandInfo(
        longitude: Double,
        latitude: Double,
        brandName: String,
        onComplete: (String, Brands?) -> Unit
    ) {
        val api = retrofit.create(KaKaoSearchAPI::class.java)
        val call = api.searchBrand(
            REST_API_KEY,
            brandName,
            x = longitude.toString(),
            y = latitude.toString()
        )

        call.enqueue(object : Callback<Brands> {
            override fun onResponse(call: Call<Brands>, response: Response<Brands>) {
                if (response.isSuccessful && response.body() != null) onComplete(
                    brandName,
                    response.body()
                )
                else onComplete(brandName, null)
            }

            override fun onFailure(call: Call<Brands>, t: Throwable) {
                t.printStackTrace()
                onComplete(brandName, null)
            }
        })
    }
}