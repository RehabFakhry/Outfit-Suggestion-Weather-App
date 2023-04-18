package com.example.outfitsuggestionweatherapp.data.source

import com.example.outfitsuggestionweatherapp.data.weatherModel.WeatherDetails
import com.example.outfitsuggestionweatherapp.data.weatherModel.WeatherResponse
import com.example.outfitsuggestionweatherapp.utils.Constants
import com.google.gson.Gson
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException

class WeatherDataFetcher {

    fun getWeatherData(
        latitude: Double, longitude: Double, callback: (WeatherResponse?, Exception?) -> Unit
    ) {
        val url =
            "${Constants.BASE_URL}$latitude&lon=$longitude" + "&appid=${Constants.API_KEY}&units=metric"
        val client = OkHttpClient.Builder().addInterceptor(
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        ).build()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody: ResponseBody? = response.body
                    if (responseBody != null) {
                        val json = responseBody.string()
                        val dataResult = Gson().fromJson(json, WeatherResponse::class.java)
                        val weatherResponse = parseWeatherResponse(dataResult)
                        callback(weatherResponse, null)
                    } else {
                        callback(null, Exception("Response body is null"))
                    }
                } catch (e: Exception) {
                    callback(null, e)
                }
            }
        })
    }

    private fun parseWeatherResponse(jsonData: WeatherResponse): WeatherResponse? {
        return try {
            val cityName = jsonData.name
            val temp = jsonData.main.temp
            val feelsLike = jsonData.main.feelsLike
            val pressure = jsonData.main.pressure
            val humidity = jsonData.main.humidity
            WeatherResponse(WeatherDetails(temp, feelsLike, pressure, humidity), cityName)
        } catch (e: Exception) {
            null
        }
    }
}