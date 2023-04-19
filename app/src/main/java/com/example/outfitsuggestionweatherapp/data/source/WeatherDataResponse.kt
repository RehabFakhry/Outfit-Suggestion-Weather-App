package com.example.outfitsuggestionweatherapp.data.source

import com.example.outfitsuggestionweatherapp.BuildConfig
import com.example.outfitsuggestionweatherapp.data.weatherModel.WeatherDetails
import com.example.outfitsuggestionweatherapp.data.weatherModel.WeatherResponse
import com.example.outfitsuggestionweatherapp.utils.Constants
import com.google.gson.Gson
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException

class WeatherDataResponse {
    fun getWeatherData(
        latitude: Double,
        longitude: Double,
        onResponse: (WeatherResponse) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val url =
            "${Constants.BASE_URL}lat=$latitude&lon=$longitude&appid=${BuildConfig.API_KEY}&units=metric"
        val client = OkHttpClient.Builder().addInterceptor(
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        ).build()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onFailure(Exception(e))
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    if (!response.isSuccessful) {
                        onFailure(Exception("Response code: ${response.code}"))
                        return
                    }
                    val responseBody: ResponseBody? = response.body
                    if (responseBody != null) {
                        val json = responseBody.string()
                        val dataResult = Gson().fromJson(json, WeatherResponse::class.java)
                        onResponse(dataResult)
                    } else {
                        onFailure(Exception("Response body is null"))
                    }
                } catch (e: Exception) {
                    onFailure(e)
                }
            }
        })
    }
}